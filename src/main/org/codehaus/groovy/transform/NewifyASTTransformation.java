/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.transform;

import groovy.lang.Newify;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.HashSet;
import java.util.List;

/**
 * Handles generation of code for the @Newify annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class NewifyASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation {
    private static final ClassNode MY_TYPE = new ClassNode(Newify.class);
    private static final String MY_NAME = MY_TYPE.getNameWithoutPackage();
    private SourceUnit source;
    private ListExpression classesToNewify;
    private boolean auto;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        this.source = source;
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            internalError("Transformation called with wrong types: $node.class / $parent.class");
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) {
            internalError("Transformation called from wrong annotation: " + node.getClassNode().getName());
        }

        boolean autoFlag = determineAutoFlag(node.getMember("auto"));
        ListExpression list = determineClassesToNewify(node.getMember("value"));

        if (parent instanceof ClassNode) {
            newifyClass(parent, autoFlag, list);
        } else if (parent instanceof MethodNode || parent instanceof FieldNode) {
            newifyMethodOrField(parent, autoFlag, list);
        }
    }

    private boolean determineAutoFlag(Expression autoExpr) {
        return !(autoExpr instanceof ConstantExpression && ((ConstantExpression) autoExpr).getValue().equals(false));
    }

    private ListExpression determineClassesToNewify(Expression expr) {
        ListExpression list = new ListExpression();
        if (expr instanceof ClassExpression) {
            list.addExpression(expr);
        } else if (expr instanceof ListExpression) {
            list = (ListExpression) expr;
            final List<Expression> expressions = list.getExpressions();
            for (Expression ex : expressions) {
                if (!(ex instanceof ClassExpression)) {
                    throw new RuntimeException("Error during @" + MY_NAME
                            + " processing. Annotation parameter must be a list of classes.");
                }
            }
            checkDuplicateNameClashes(list);
        }
        return list;
    }

    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression) expr;
            Expression args = transform(mce.getArguments());
            Expression method = transform(mce.getMethod());
            Expression object = transform(mce.getObjectExpression());
            if (isNewifyCandidate(mce)) {
                return transformMethodCall(mce, args);
            }
            return new MethodCallExpression(object, method, args);
        }
        return expr.transformExpression(this);
    }

    private void newifyClass(AnnotatedNode parent, boolean autoFlag, ListExpression list) {
        ClassNode cNode = (ClassNode) parent;
        String cName = cNode.getName();
        if (cNode.isInterface()) {
            throw new RuntimeException("Error processing interface '" + cName + "'. @"
                    + MY_NAME + " not allowed for interfaces.");
        }
        classesToNewify = list;
        auto = autoFlag;
        super.visitClass(cNode);
    }

    private void newifyMethodOrField(AnnotatedNode parent, boolean autoFlag, ListExpression list) {
        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        checkClassLevelClashes(list);
        checkAutoClash(autoFlag);
        classesToNewify = list;
        auto = autoFlag;
        if (parent instanceof FieldNode) {
            super.visitField((FieldNode) parent);
        } else {
            super.visitMethod((MethodNode) parent);
        }
        classesToNewify = oldClassesToNewify;
        auto = oldAuto;
    }

    private void checkDuplicateNameClashes(ListExpression list) {
        final HashSet<String> seen = new HashSet<String>();
        final List<ClassExpression> classes = list.getExpressions();
        for (ClassExpression ce : classes) {
            final String name = ce.getType().getNameWithoutPackage();
            if (seen.contains(name)) {
                throw new RuntimeException("Duplicate name '" + name + "' found during @"
                        + MY_NAME + " processing.");
            }
            seen.add(name);
        }
    }

    private void checkAutoClash(boolean autoFlag) {
        if (auto && !autoFlag) {
            throw new RuntimeException("Error during @" + MY_NAME +
                    " processing. The 'auto' flag can't be false at method/constructor/field level if it is true at the class level.");
        }
    }

    private void checkClassLevelClashes(ListExpression list) {
        final List<ClassExpression> classes = list.getExpressions();
        for (ClassExpression ce : classes) {
            final String name = ce.getType().getNameWithoutPackage();
            if (findClassWithMatchingBasename(name)) {
                throw new RuntimeException("Error during @" + MY_NAME + " processing. Class '" + name
                        + "' can't appear at method/constructor/field level if it already appears at the class level.");
            }
        }
    }

    private boolean findClassWithMatchingBasename(String nameWithoutPackage) {
        if (classesToNewify == null) return false;
        final List<ClassExpression> classes = classesToNewify.getExpressions();
        for (ClassExpression ce : classes) {
            if (ce.getType().getNameWithoutPackage().equals(nameWithoutPackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNewifyCandidate(MethodCallExpression mce) {
        return mce.getObjectExpression() == VariableExpression.THIS_EXPRESSION
                || (auto && isNewMethodStyle(mce));
    }

    private boolean isNewMethodStyle(MethodCallExpression mce) {
        final Expression obj = mce.getObjectExpression();
        final Expression meth = mce.getMethod();
        return (obj instanceof ClassExpression && meth instanceof ConstantExpression
                && ((ConstantExpression) meth).getValue().equals("new"));
    }

    private Expression transformMethodCall(MethodCallExpression mce, Expression args) {
        ClassNode classType;
        if (isNewMethodStyle(mce)) {
            classType = mce.getObjectExpression().getType();
        } else {
            classType = findMatchingCandidateClass(mce);
        }
        if (classType != null) {
            return new ConstructorCallExpression(classType, args);
        }
        return mce;
    }

    private ClassNode findMatchingCandidateClass(MethodCallExpression mce) {
        if (classesToNewify == null) return null;
        List<ClassExpression> classes = classesToNewify.getExpressions();
        for (ClassExpression ce : classes) {
            final ClassNode type = ce.getType();
            if (type.getNameWithoutPackage().equals(mce.getMethodAsString())) {
                return type;
            }
        }
        return null;
    }

    private void internalError(String message) {
        throw new RuntimeException("Internal error: " + message);
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
}