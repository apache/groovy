/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;

/**
 * Analysis helper for lambda expressions that determines whether a lambda
 * captures its enclosing instance or only references static members.
 * <p>
 * When a lambda is non-capturing (no shared variables and no instance member
 * access), the lambda writer can emit it as a static method with
 * {@code LambdaMetafactory}, avoiding per-call allocation.
 * <p>
 * This analyzer also qualifies outer-class static member references so that
 * the generated {@code doCall} method can invoke them without an enclosing
 * instance receiver.
 */
class StaticTypesLambdaAnalyzer {

    StaticTypesLambdaAnalyzer(final SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    boolean isNonCapturing(final MethodNode lambdaMethod, final Parameter[] sharedVariables) {
        return (sharedVariables == null || sharedVariables.length == 0)
            && !accessesInstanceMembers(lambdaMethod);
    }

    boolean accessesInstanceMembers(final MethodNode lambdaMethod) {
        Boolean accessingInstanceMembers = lambdaMethod.getNodeMetaData(MetaDataKey.ACCESSES_INSTANCE_MEMBERS);
        if (accessingInstanceMembers != null) return accessingInstanceMembers;

        InstanceMemberAccessFinder finder = new InstanceMemberAccessFinder(getOrCreateResolver(lambdaMethod));
        lambdaMethod.getCode().visit(finder);

        accessingInstanceMembers = finder.isAccessingInstanceMembers();
        lambdaMethod.putNodeMetaData(MetaDataKey.ACCESSES_INSTANCE_MEMBERS, accessingInstanceMembers);
        return accessingInstanceMembers;
    }

    /**
     * Qualifies unqualified outer-class static member references in a lambda
     * body into class-qualified references (e.g., {@code label} becomes
     * {@code Outer.label}).
     * <p>
     * <b>Safety note:</b> This method mutates the AST in place. It is called
     * during the bytecode generation phase — the last phase of the compilation
     * pipeline. No subsequent compiler phases read these nodes, so the in-place
     * mutation is safe. Do not move this call to an earlier compilation phase
     * without ensuring no downstream consumers depend on the original
     * unqualified references.
     */
    void qualifyOuterStaticMemberReferences(final MethodNode lambdaMethod) {
        lambdaMethod.getCode().visit(new OuterStaticMemberQualifier(sourceUnit, getOrCreateResolver(lambdaMethod)));
    }

    private OuterStaticMemberResolver getOrCreateResolver(final MethodNode lambdaMethod) {
        return resolverCache.computeIfAbsent(lambdaMethod,
            m -> new OuterStaticMemberResolver(m.getDeclaringClass().getOuterClasses()));
    }

    private static boolean isThisReceiver(final Expression expression) {
        return expression instanceof VariableExpression receiver && receiver.isThisExpression();
    }

    private static boolean isEnclosingInstanceReceiver(final Expression expression) {
        return isThisReceiver(expression) || isQualifiedEnclosingInstanceReference(expression);
    }

    private static boolean isQualifiedEnclosingInstanceReference(final Expression expression) {
        if (!(expression instanceof PropertyExpression propertyExpression)) return false;
        if (!(propertyExpression.getObjectExpression() instanceof ClassExpression)) return false;

        String property = propertyExpression.getPropertyAsString();
        return "this".equals(property) || "super".equals(property);
    }

    /**
     * Transforms unqualified outer-class static member references in a lambda
     * body into class-qualified references (e.g., {@code label} becomes
     * {@code Outer.label}), enabling the static {@code doCall} method to
     * invoke them without an enclosing instance.
     */
    private static final class OuterStaticMemberQualifier extends ClassCodeExpressionTransformer {

        private final SourceUnit sourceUnit;
        private final OuterStaticMemberResolver resolver;

        private OuterStaticMemberQualifier(final SourceUnit sourceUnit, final OuterStaticMemberResolver resolver) {
            this.sourceUnit = sourceUnit;
            this.resolver = resolver;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        @Override
        public Expression transform(final Expression expression) {
            if (expression instanceof VariableExpression variableExpression) {
                Expression qualifiedReference = qualify(variableExpression);
                if (qualifiedReference != null) return qualifiedReference;
            } else if (expression instanceof AttributeExpression attributeExpression) {
                Expression qualifiedReference = qualify(attributeExpression);
                if (qualifiedReference != null) return qualifiedReference;
            } else if (expression instanceof PropertyExpression propertyExpression) {
                Expression qualifiedReference = qualify(propertyExpression);
                if (qualifiedReference != null) return qualifiedReference;
            } else if (expression instanceof MethodCallExpression methodCallExpression) {
                Expression qualifiedReference = qualify(methodCallExpression);
                if (qualifiedReference != null) return qualifiedReference;
            }
            return super.transform(expression);
        }

        private Expression qualify(final VariableExpression expression) {
            ClassNode owner = resolver.findOwner(expression);
            if (owner == null) return null;

            PropertyExpression qualifiedReference = new PropertyExpression(classX(owner), expression.getName());
            qualifiedReference.setImplicitThis(false);
            qualifiedReference.copyNodeMetaData(expression);
            setSourcePosition(qualifiedReference, expression);
            return qualifiedReference;
        }

        private Expression qualify(final AttributeExpression expression) {
            ClassNode owner = resolver.findOwner(expression);
            if (owner == null) return null;

            AttributeExpression qualifiedReference = new AttributeExpression(
                classX(owner),
                transform(expression.getProperty()),
                expression.isSafe()
            );
            qualifiedReference.setImplicitThis(false);
            qualifiedReference.setSpreadSafe(expression.isSpreadSafe());
            qualifiedReference.copyNodeMetaData(expression);
            setSourcePosition(qualifiedReference, expression);
            return qualifiedReference;
        }

        private Expression qualify(final PropertyExpression expression) {
            ClassNode owner = resolver.findOwner(expression);
            if (owner == null) return null;

            PropertyExpression qualifiedReference = new PropertyExpression(
                classX(owner),
                transform(expression.getProperty()),
                expression.isSafe()
            );
            qualifiedReference.setImplicitThis(false);
            qualifiedReference.setSpreadSafe(expression.isSpreadSafe());
            qualifiedReference.copyNodeMetaData(expression);
            setSourcePosition(qualifiedReference, expression);
            return qualifiedReference;
        }

        private Expression qualify(final MethodCallExpression expression) {
            ClassNode owner = resolver.findOwner(expression);
            if (owner == null) return null;

            MethodCallExpression qualifiedReference = new MethodCallExpression(
                classX(owner),
                transform(expression.getMethod()),
                transform(expression.getArguments())
            );
            qualifiedReference.setImplicitThis(false);
            qualifiedReference.setSafe(expression.isSafe());
            qualifiedReference.setSpreadSafe(expression.isSpreadSafe());
            qualifiedReference.setGenericsTypes(expression.getGenericsTypes());
            qualifiedReference.setMethodTarget(expression.getMethodTarget());
            qualifiedReference.copyNodeMetaData(expression);
            setSourcePosition(qualifiedReference, expression);
            return qualifiedReference;
        }
    }

    /**
     * Resolves outer-class static member references for a lambda body by
     * walking the enclosing class hierarchy (including supertypes and
     * interfaces) to determine ownership of static fields, properties, and
     * methods.
     */
    private static final class OuterStaticMemberResolver {
        private final Map<String, ClassNode> referenceOwnerIndex;

        private OuterStaticMemberResolver(final List<ClassNode> outerClasses) {
            this.referenceOwnerIndex = new LinkedHashMap<>();
            for (ClassNode outerClass : outerClasses) {
                collectReferenceOwners(outerClass, referenceOwnerIndex);
            }
        }

        private ClassNode findOwner(final VariableExpression expression) {
            ClassNode owner = expression.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
            if (!isReferenceOwner(owner)) return null;

            return isStaticReference(expression) ? owner : null;
        }

        private ClassNode findOwner(final MethodCallExpression expression) {
            if (!expression.isImplicitThis() && !isQualifiedEnclosingInstanceReference(expression.getObjectExpression())) {
                return null;
            }

            MethodNode directMethodCallTarget = expression.getMethodTarget();
            if (directMethodCallTarget == null || !directMethodCallTarget.isStatic()) return null;

            ClassNode owner = directMethodCallTarget.getDeclaringClass();
            if (!isReferenceOwner(owner)) return null;

            return isEnclosingInstanceReceiver(expression.getObjectExpression()) ? owner : null;
        }

        private ClassNode findOwner(final PropertyExpression expression) {
            if (!expression.isImplicitThis() && !isQualifiedEnclosingInstanceReference(expression.getObjectExpression())) {
                return null;
            }

            MethodNode directMethodCallTarget = expression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (directMethodCallTarget != null && directMethodCallTarget.isStatic() && isReferenceOwner(directMethodCallTarget.getDeclaringClass())) {
                return directMethodCallTarget.getDeclaringClass();
            }

            String propertyName = expression.getPropertyAsString();
            if (propertyName == null) return null;

            for (ClassNode referenceOwner : referenceOwnerIndex.values()) {
                if (isStaticMemberNamed(propertyName, referenceOwner)) {
                    return referenceOwner;
                }
            }

            return null;
        }

        private boolean isReferenceOwner(final ClassNode owner) {
            return owner != null && referenceOwnerIndex.containsKey(owner.redirect().getName());
        }

        private static void collectReferenceOwners(final ClassNode owner, final Map<String, ClassNode> referenceOwnerIndex) {
            if (owner == null) return;

            ClassNode redirectedOwner = owner.redirect();
            if (referenceOwnerIndex.putIfAbsent(redirectedOwner.getName(), redirectedOwner) != null) return;

            collectReferenceOwners(redirectedOwner.getSuperClass(), referenceOwnerIndex);
            for (ClassNode interfaceNode : redirectedOwner.getInterfaces()) {
                collectReferenceOwners(interfaceNode, referenceOwnerIndex);
            }
        }

        private static boolean isStaticReference(final VariableExpression expression) {
            Variable accessedVariable = expression.getAccessedVariable();

            if (accessedVariable instanceof Parameter) return false;

            return (accessedVariable instanceof FieldNode || accessedVariable instanceof PropertyNode)
                && accessedVariable.isStatic();
        }

        private static boolean isStaticMemberNamed(final String propertyName, final ClassNode owner) {
            FieldNode field = owner.getField(propertyName);
            if (field != null && field.isStatic()) return true;

            PropertyNode property = owner.getProperty(propertyName);
            if (property != null && property.isStatic()) return true;

            String capitalizedPropertyName = capitalize(propertyName);
            MethodNode getter = owner.getGetterMethod("is" + capitalizedPropertyName);
            if (getter == null) getter = owner.getGetterMethod("get" + capitalizedPropertyName);

            return getter != null && getter.isStatic();
        }
    }

    /**
     * Visits a lambda body to detect any reference to enclosing-instance
     * members.  Short-circuits on the first instance member access found.
     */
    private static final class InstanceMemberAccessFinder extends CodeVisitorSupport {

        private final OuterStaticMemberResolver resolver;
        private boolean accessingInstanceMembers;

        private InstanceMemberAccessFinder(final OuterStaticMemberResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            if (accessingInstanceMembers) return;
            if (expression.isThisExpression() || expression.isSuperExpression() || "thisObject".equals(expression.getName())) {
                accessingInstanceMembers = true;
                return;
            }

            ClassNode owner = expression.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
            if (owner != null && resolver.isReferenceOwner(owner) && resolver.findOwner(expression) == null) {
                accessingInstanceMembers = true;
                return;
            }

            super.visitVariableExpression(expression);
        }

        @Override
        public void visitPropertyExpression(final PropertyExpression expression) {
            if (accessingInstanceMembers) return;
            if (resolver.findOwner(expression) != null) {
                expression.getProperty().visit(this);
                return;
            }
            if (isQualifiedEnclosingInstanceReference(expression)) {
                accessingInstanceMembers = true;
                return;
            }

            super.visitPropertyExpression(expression);
        }

        @Override
        public void visitAttributeExpression(final AttributeExpression expression) {
            if (accessingInstanceMembers) return;
            if (resolver.findOwner(expression) != null) {
                expression.getProperty().visit(this);
                return;
            }
            if (isQualifiedEnclosingInstanceReference(expression.getObjectExpression())) {
                accessingInstanceMembers = true;
                return;
            }

            super.visitAttributeExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(final MethodCallExpression expression) {
            if (accessingInstanceMembers) return;
            if (resolver.findOwner(expression) != null) {
                expression.getMethod().visit(this);
                expression.getArguments().visit(this);
                return;
            }

            super.visitMethodCallExpression(expression);
        }

        private boolean isAccessingInstanceMembers() {
            return accessingInstanceMembers;
        }
    }

    private enum MetaDataKey {
        ACCESSES_INSTANCE_MEMBERS
    }

    private final SourceUnit sourceUnit;
    private final Map<MethodNode, OuterStaticMemberResolver> resolverCache = new HashMap<>();
}
