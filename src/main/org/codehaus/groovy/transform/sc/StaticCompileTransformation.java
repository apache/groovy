/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.sc;

import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesWriterControllerFactoryImpl;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.OptimizerVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.stc.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;

/**
 * Handles the implementation of the {@link groovy.transform.CompileStatic} transformation.
 *
 * @author Cedric Champeau
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class StaticCompileTransformation extends StaticTypesTransformation {

    public static final ClassNode COMPILE_STATIC_ANNOTATION = ClassHelper.make(CompileStatic.class);
    private final StaticTypesWriterControllerFactoryImpl factory = new StaticTypesWriterControllerFactoryImpl();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        BinaryExpressionTransformer transformer = new BinaryExpressionTransformer(source);

        AnnotatedNode node = (AnnotatedNode) nodes[1];
        StaticTypeCheckingVisitor visitor = null;
        if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            classNode.putNodeMetaData(WriterControllerFactory.class, factory);
            node.putNodeMetaData(STATIC_COMPILE_NODE, Boolean.TRUE);
            visitor = newVisitor(source, classNode, null);
            visitor.visitClass(classNode);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)node;
            methodNode.putNodeMetaData(STATIC_COMPILE_NODE, Boolean.TRUE);
            ClassNode declaringClass = methodNode.getDeclaringClass();
            if (declaringClass.getNodeMetaData(WriterControllerFactory.class)==null) {
                declaringClass.putNodeMetaData(WriterControllerFactory.class, factory);
            }
            visitor = newVisitor(source, declaringClass, null);
            visitor.setMethodsToBeVisited(Collections.singleton(methodNode));
            visitor.visitMethod(methodNode);
        } else {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type", node.getLineNumber(), node.getColumnNumber()));
        }
        if (visitor!=null) {
            visitor.performSecondPass();
        }
        if (node instanceof ClassNode) {
            transformer.visitClass((ClassNode)node);
        } else if (node instanceof MethodNode) {
            transformer.visitMethod((MethodNode)node);
        }
    }

    @Override
    protected StaticTypeCheckingVisitor newVisitor(final SourceUnit unit, final ClassNode node, final TypeCheckerPluginFactory pluginFactory) {
        return new StaticCompilationVisitor(unit, node, pluginFactory);
    }

    /**
     * Some expressions use symbols as aliases to method calls (<<, +=, ...). In static compilation,
     * if such a method call is found, we transform the original binary expression into a method
     * call expression so that the call gets statically compiled.
     *
     * @author Cedric Champeau
     */
    private static class BinaryExpressionTransformer extends ClassCodeExpressionTransformer {
        private final SourceUnit unit;

        private final StaticTypesTypeChooser typeChooser = new StaticTypesTypeChooser();
        private ClassNode classNode;

        private BinaryExpressionTransformer(final SourceUnit unit) {
            this.unit = unit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return unit;
        }

        @Override
        public Expression transform(Expression expr) {
            if (expr instanceof StaticMethodCallExpression) {
                return transformStaticMethodCallExpression((StaticMethodCallExpression) expr);
            }
            if (expr instanceof BinaryExpression) {
                return transformBinaryExpression(expr);
            }
            if (expr instanceof MethodCallExpression) {
                return transformMethodCallExpression((MethodCallExpression) expr);
            }
            return super.transform(expr);
        }

        @Override
        public void visitClass(final ClassNode node) {
            ClassNode prec = classNode;
            classNode = node;
            super.visitClass(node);
            classNode = prec;
        }

        private Expression transformMethodCallExpression(final MethodCallExpression expr) {
            Expression objectExpression = expr.getObjectExpression();
            ClassNode type = typeChooser.resolveType(objectExpression, classNode);
            if (type!=null && type.isArray()) {
                String method = expr.getMethodAsString();
                if ("getAt".equals(method)) {
                    Expression arguments = expr.getArguments();
                    if (arguments instanceof TupleExpression) {
                        List<Expression> argList = ((TupleExpression)arguments).getExpressions();
                        if (argList.size()==1) {
                            Expression indexExpr = argList.get(0);
                            ClassNode argType = typeChooser.resolveType(indexExpr, classNode);
                            if (argType!=null && ClassHelper.Integer_TYPE==ClassHelper.getWrapper(argType)) {
                                BinaryExpression binaryExpression = new BinaryExpression(
                                        objectExpression,
                                        Token.newSymbol("[", indexExpr.getLineNumber(), indexExpr.getColumnNumber()),
                                        indexExpr
                                );
                                binaryExpression.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, type.getComponentType());
                                return binaryExpression;
                            }
                        }
                    }
                }
                if ("putAt".equals(method)) {
                    Expression arguments = expr.getArguments();
                    if (arguments instanceof TupleExpression) {
                        List<Expression> argList = ((TupleExpression)arguments).getExpressions();
                        if (argList.size()==2) {
                            Expression indexExpr = argList.get(0);
                            Expression objExpr = argList.get(1);
                            ClassNode argType = typeChooser.resolveType(indexExpr, classNode);
                            if (argType!=null && ClassHelper.Integer_TYPE==ClassHelper.getWrapper(argType)) {
                                BinaryExpression arrayGet = new BinaryExpression(
                                        objectExpression,
                                        Token.newSymbol("[", indexExpr.getLineNumber(), indexExpr.getColumnNumber()),
                                        indexExpr
                                );
                                arrayGet.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, type.getComponentType());
                                BinaryExpression assignment = new BinaryExpression(
                                        arrayGet,
                                        Token.newSymbol("=", objExpr.getLineNumber(), objExpr.getColumnNumber()),
                                        objExpr
                                );
                                return assignment;
                            }
                        }
                    }
                }
            }
            return super.transform(expr);
        }

        private Expression transformBinaryExpression(final Expression expr) {
            Object[] list = (Object[]) expr.getNodeMetaData(BINARY_EXP_TARGET);
            if (list!=null) {
                BinaryExpression bin = (BinaryExpression) expr;
                Token operation = bin.getOperation();
                boolean isAssignment = StaticTypeCheckingSupport.isAssignment(operation.getType());

                MethodNode node = (MethodNode) list[0];
                String name = (String) list[1];
                Expression left = transform(bin.getLeftExpression());
                Expression right = transform(bin.getRightExpression());
                MethodCallExpression call = new MethodCallExpression(
                        left,
                        name,
                        new ArgumentListExpression(right)
                );
                call.setMethodTarget(node);
                if (!isAssignment) return call;
                // case of +=, -=, /=, ...
                // the method represents the operation type only, and we must add an assignment
                return new BinaryExpression(left, Token.newSymbol("=", operation.getStartLine(), operation.getStartColumn()), call);
            }
            return super.transform(expr);
        }

        private Expression transformStaticMethodCallExpression(final StaticMethodCallExpression orig) {
            MethodNode target = (MethodNode) orig.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
            if (target!=null) {
                MethodCallExpression call = new MethodCallExpression(
                        new ClassExpression(orig.getOwnerType()),
                        orig.getMethod(),
                        orig.getArguments()
                );
                call.setMethodTarget(target);
                return call;
            }
            return super.transform(orig);
        }
    }

}
