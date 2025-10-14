// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.activeobject;

import groovyx.gpars.util.ASTUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transforms active objects so that their active methods can be invoked asynchronously through an internal actor.
 * <p>
 * Inspired by org.codehaus.groovy.transform.LogASTTransformation
 * </p>
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"CallToStringEquals"})
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ActiveObjectASTTransformation implements ASTTransformation {
    private static boolean isRootActiveObject(final ClassNode classNode) {
        ClassNode superClass = classNode.getSuperClass();
        while (superClass != null) {
            final List<AnnotationNode> annotations = superClass.getAnnotations(new ClassNode(ActiveObject.class));
            if (!annotations.isEmpty()) return false;
            superClass = superClass.getSuperClass();
        }
        return true;
    }

    private static String lookupActorFieldName(final AnnotationNode logAnnotation) {
        final Expression member = logAnnotation.getMember("actorName");
        if (member != null) {
            return member.getText();
        } else {
            return ActiveObject.INTERNAL_ACTIVE_OBJECT_ACTOR;
        }
    }

    private static String lookupActorGroupName(final AnnotationNode logAnnotation) {
        final Expression member = logAnnotation.getMember("value");
        if (member != null) {
            return member.getText();
        } else {
            return "";
        }
    }

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            ASTUtils.addError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes), nodes[0], source);
        }

        final AnnotatedNode targetClass = (AnnotatedNode) nodes[1];
        final AnnotationNode activeObjectAnnotation = (AnnotationNode) nodes[0];

        final String actorFieldName = lookupActorFieldName(activeObjectAnnotation);
        final String actorGroupName = lookupActorGroupName(activeObjectAnnotation);

        if (!(targetClass instanceof ClassNode))
            throw new GroovyBugError("Class annotation " + activeObjectAnnotation.getClassNode().getName() + " annotated no Class, this must not happen.");

        final ClassNode classNode = (ClassNode) targetClass;

        final boolean rootActiveObject = isRootActiveObject(classNode);
        if (!rootActiveObject && !actorFieldName.equals(ActiveObject.INTERNAL_ACTIVE_OBJECT_ACTOR)) {
            ASTUtils.addError("Actor field name can only be specified at the top of the active object hierarchy. Apparently a superclass of this class is also an active object.", classNode, source);
        }
        if (!rootActiveObject && actorGroupName.length() != 0) {
            ASTUtils.addError("Active object's actor group can only be specified at the top of the active object hierarchy. Apparently a superclass of this class is also an active object.", classNode, source);
        }

        final GroovyClassVisitor transformer = new MyClassCodeExpressionTransformer(source, actorFieldName, actorGroupName);
        transformer.visitClass(classNode);
    }

    @SuppressWarnings({"StringToUpperCaseOrToLowerCaseWithoutLocale", "CallToStringEquals"})
    private static class MyClassCodeExpressionTransformer extends ClassCodeExpressionTransformer {
        private final SourceUnit source;
        private final String actorFieldName;
        private final String actorGroupName;
        private FieldNode actorNode;

        private MyClassCodeExpressionTransformer(final SourceUnit source, final String actorFieldName, final String actorGroupName) {
            this.source = source;
            this.actorFieldName = actorFieldName;
            this.actorGroupName = actorGroupName;
        }

        private static void addActiveMethod(final FieldNode actorNode, final ClassNode owner, final MethodNode original, final boolean blocking) {
            if (original.isSynthetic())
                return;

            final ArgumentListExpression args = new ArgumentListExpression();
            final Parameter[] params = original.getParameters();
            final Parameter[] newParams = new Parameter[params.length];

            args.addExpression(new VariableExpression("this"));
            args.addExpression(new ConstantExpression(original.getName()));

            for (int i = 0; i < newParams.length; i++) {
                final Parameter newParam = new Parameter(nonGeneric(params[i].getType()), params[i].getName());
                newParam.setInitialExpression(params[i].getInitialExpression());
                newParams[i] = newParam;
                args.addExpression(new VariableExpression(newParam));
            }

            final MethodNode newMethod = owner.addMethod(findSuitablePrivateMethodName(owner, original),
                    Modifier.FINAL & Modifier.PRIVATE,
                    new ClassNode(Object.class),
                    newParams,
                    original.getExceptions(),
                    original.getCode());
            newMethod.setGenericsTypes(original.getGenericsTypes());

            final String submitMethodName = blocking ? "submitAndWait" : "submit";

            VariableExpression actor = new VariableExpression(actorNode);

            MethodCallExpression submitCall = new MethodCallExpression(actor, submitMethodName, args);
            Expression newCode = submitCall;

            if (!blocking) {
                newCode = new MethodCallExpression(actor,
                        InternalActor.PREPROCESS_METHOD_NAME,
                        new ArgumentListExpression(submitCall, new VariableExpression("this"), new ConstantExpression(original.getName())));
            }

            original.setCode(new ExpressionStatement(newCode));
        }

        @SuppressWarnings({"OverlyComplexBooleanExpression"})
        private static boolean blockingMandated(final String text) {
            assert text != null;
            return !("java.lang.Object".equals(text) || "void".equals(text) || text.contains("groovyx.gpars.dataflow.DataflowVariable") || text.contains("groovyx.gpars.dataflow.Promise"));
        }

        private static String findSuitablePrivateMethodName(final ClassNode owner, final MethodNode original) {
            String newMethodName = InternalActor.METHOD_NAME_PREFIX + original.getName();
            int counter = 1;
            while (owner.hasMethod(newMethodName, original.getParameters())) {
                newMethodName = InternalActor.METHOD_NAME_PREFIX + original.getName() + counter;
                counter++;
            }
            return newMethodName;
        }

        private static ClassNode nonGeneric(final ClassNode type) {
            if (type.isUsingGenerics()) {
                final ClassNode nonGen = ClassHelper.makeWithoutCaching(type.getName());
                nonGen.setRedirect(type);
                nonGen.setGenericsTypes(null);
                nonGen.setUsingGenerics(false);
                return nonGen;
            } else {
                return type;
            }
        }

        private static FieldNode addActorFieldToClass(final ClassNode classNode, final String logFieldName, final String actorGroupName) {
            final ArgumentListExpression args = new ArgumentListExpression();
            args.addExpression(new ConstantExpression(actorGroupName));

            return classNode.addField(logFieldName,
                    Modifier.FINAL | Modifier.TRANSIENT | Modifier.PROTECTED,
                    new ClassNode(InternalActor.class),
                    new MethodCallExpression(
                            new ClassExpression(new ClassNode(InternalActor.class)),
                            "create",
                            args));
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return source;
        }

        @Override
        public Expression transform(final Expression exp) {
            if (exp == null) return null;
            return super.transform(exp);
        }

        @Override
        public void visitClass(final ClassNode node) {
            final FieldNode actorField = node.getField(actorFieldName);
            if (actorField != null) {
                if (actorField.getType().getName().contains("groovyx.gpars.activeobject.InternalActor")) {
                    actorNode = actorField;
                } else {
                    this.addError("Active Object cannot have a field named " + actorFieldName + " declared", actorField);
                }
            } else {
                actorNode = addActorFieldToClass(node, actorFieldName, actorGroupName);
            }

            final Iterable<MethodNode> copyOfMethods = new ArrayList<MethodNode>(node.getMethods());

            for (final MethodNode method : copyOfMethods) {
                final List<AnnotationNode> annotations = method.getAnnotations(new ClassNode(ActiveMethod.class));

                if (annotations.isEmpty()) {
                    continue;
                }
                if (method.isStatic()) {
                    this.addError("Static methods cannot be active", method);
                }

                addActiveMethod(actorNode, node, method, checkBlockingMethod(method, annotations));
            }
            super.visitClass(node);
        }

        private boolean checkBlockingMethod(final MethodNode method, final Iterable<AnnotationNode> annotations) {
            boolean blocking = false;

            for (final AnnotationNode annotation : annotations) {
                final Expression member = annotation.getMember("blocking");

                if (member != null) {
                    if ("true".equals(member.getText())) blocking = true; // getText() is @NotNull
                }
            }

            final ClassNode returnType = method.getReturnType();
            final String text = returnType.getName();
            if (!blocking && blockingMandated(text)) {
                this.addError("Non-blocking methods must not return a specific type. Use def or void instead.", method);
                return true;
            }
            return blocking;
        }
    }
}
