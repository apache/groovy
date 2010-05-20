package org.codehaus.groovy.transform;

import java.util.Arrays;
import java.util.logging.Logger;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

/**
 * @author Guillaume Laforge
 * @author Jochen Theodorou
 * @author Dinko Srkoc
 * @author Hamlet D'Arcy
 * @author Raffaele Cigni
 * @author Alberto Vilches Raton
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class LogASTTransformation implements ASTTransformation {
	
	public void visit(ASTNode[] nodes, final SourceUnit source) {
		if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            addError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes), nodes[0], source);
        }

		AnnotatedNode targetClass = (AnnotatedNode) nodes[1];
		AnnotationNode logAnnotation = (AnnotationNode) nodes[0];

		if (!(targetClass instanceof ClassNode)) throw new GroovyBugError("Class annotation @Log annotated no Class, this must not happen.");

		final ClassNode classNode = (ClassNode) targetClass;

		ClassCodeExpressionTransformer transformer = new ClassCodeExpressionTransformer() {
			private FieldNode logNode;

			@Override
			protected SourceUnit getSourceUnit() {
				return source;
			}

			public Expression transform(Expression exp) {
				if (exp==null) return null;
				if (exp instanceof MethodCallExpression) {
					return transformMethodCallExpression(exp);
				}
				return super.transform(exp);
			}

			@Override
			public void visitClass(ClassNode node) {
				FieldNode logField = node.getField("log");
				if (logField != null) {
					addError("Class annotated with Log annotation cannot have log field declared",
							logField);
				} else {
					ClassNode loggerClassNode = new ClassNode(Logger.class);
					logNode = node.addField("log",
							Opcodes.ACC_FINAL | Opcodes.ACC_TRANSIENT | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE,
							loggerClassNode,
							new MethodCallExpression(
									new ClassExpression(loggerClassNode),
									"getLogger",
									new ConstantExpression(node.getName())));
				}
				super.visitClass(node);
			}

			private Expression transformMethodCallExpression(Expression exp) {
				MethodCallExpression mce = (MethodCallExpression) exp;
				if (!(mce.getObjectExpression() instanceof VariableExpression)) {
					return exp;
				}
				VariableExpression variableExpression = (VariableExpression) mce.getObjectExpression();
				if (!variableExpression.getName().equals("log") 
						|| !(variableExpression.getAccessedVariable() instanceof DynamicVariable)) {
					return exp;
				}					
				String methodName = mce.getMethodAsString();
				if (methodName == null) return exp;
				if (usesSimpleMethodArgumentsOnly(mce)) return exp;

				variableExpression.setAccessedVariable(logNode);

				ArgumentListExpression args = new ArgumentListExpression();
				ClassNode levelClass = new ClassNode("java.util.logging.Level",0,ClassHelper.OBJECT_TYPE);
				AttributeExpression logLevelExpression = new AttributeExpression(
						new ClassExpression(levelClass),
						new ConstantExpression(methodName.toUpperCase()));
				args.addExpression(logLevelExpression);
				MethodCallExpression condition = new MethodCallExpression(variableExpression, "isLoggable", args);
				BooleanExpression booleanExpression = new BooleanExpression(condition);

				TernaryExpression isLoggableExpr = new TernaryExpression(booleanExpression,
						exp, ConstantExpression.NULL);

				return isLoggableExpr;
			}

			private boolean usesSimpleMethodArgumentsOnly(MethodCallExpression mce) {
				Expression arguments = mce.getArguments();
				if (arguments instanceof TupleExpression) {
					TupleExpression tuple = (TupleExpression) arguments;
					for (Expression exp : tuple.getExpressions()) {
						if (!isSimpleExpression(exp)) return false;
					}
					return true;
				}
				return !isSimpleExpression(arguments);
			}

			private boolean isSimpleExpression(Expression exp) {
				if (exp instanceof ConstantExpression) return true;
				if (exp instanceof VariableExpression) return true;
				return false;					
			}

		};        	
		transformer.visitClass(classNode);

	}

	public void addError(String msg, ASTNode expr, SourceUnit source){
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
	}
	
	
}
