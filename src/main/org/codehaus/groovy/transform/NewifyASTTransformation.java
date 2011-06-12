/*
 * Copyright 2008-2011 the original author or authors.
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
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Set;

/**
 * Handles generation of code for the @Newify annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class NewifyASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation {
    private static final ClassNode MY_TYPE = ClassHelper.make(Newify.class);
    private static final String MY_NAME = MY_TYPE.getNameWithoutPackage();
    private static final String BASE_BAD_PARAM_ERROR = "Error during @" + MY_NAME +
            " processing. Annotation parameter must be a class or list of classes but found ";
    private SourceUnit source;
    private ListExpression classesToNewify;
    private DeclarationExpression candidate;
    private boolean auto;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        this.source = source;
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            internalError("Expecting [AnnotationNode, AnnotatedClass] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) {
            internalError("Transformation called from wrong annotation: " + node.getClassNode().getName());
        }

        boolean autoFlag = determineAutoFlag(node.getMember("auto"));
        Expression value = node.getMember("value");

        if (parent instanceof ClassNode) {
            newifyClass((ClassNode) parent, autoFlag, determineClasses(value, false));
        } else if (parent instanceof MethodNode || parent instanceof FieldNode) {
            newifyMethodOrField(parent, autoFlag, determineClasses(value, false));
        } else if (parent instanceof DeclarationExpression) {
            newifyDeclaration((DeclarationExpression) parent, autoFlag, determineClasses(value, true));
        }
    }

    private void newifyDeclaration(DeclarationExpression de, boolean autoFlag, ListExpression list) {
        ClassNode cNode = de.getDeclaringClass();
        candidate = de;
        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        classesToNewify = list;
        auto = autoFlag;
        super.visitClass(cNode);
        classesToNewify = oldClassesToNewify;
        auto = oldAuto;
    }

    private boolean determineAutoFlag(Expression autoExpr) {
        return !(autoExpr instanceof ConstantExpression && ((ConstantExpression) autoExpr).getValue().equals(false));
    }

    /** allow non-strict mode in scripts because parsing not complete at that point */
    private ListExpression determineClasses(Expression expr, boolean searchSourceUnit) {
        ListExpression list = new ListExpression();
        if (expr instanceof ClassExpression) {
            list.addExpression(expr);
        } else if (expr instanceof VariableExpression && searchSourceUnit) {
            VariableExpression ve = (VariableExpression) expr;
            ClassNode fromSourceUnit = getSourceUnitClass(ve);
            if (fromSourceUnit != null) {
                ClassExpression found = new ClassExpression(fromSourceUnit);
                found.setSourcePosition(ve);
                list.addExpression(found);
            } else {
                addError(BASE_BAD_PARAM_ERROR + "an unresolvable reference to '" + ve.getName() + "'.", expr);
            }
        } else if (expr instanceof ListExpression) {
            list = (ListExpression) expr;
            final List<Expression> expressions = list.getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                Expression next = expressions.get(i);
                if (next instanceof VariableExpression && searchSourceUnit) {
                    VariableExpression ve = (VariableExpression) next;
                    ClassNode fromSourceUnit = getSourceUnitClass(ve);
                    if (fromSourceUnit != null) {
                        ClassExpression found = new ClassExpression(fromSourceUnit);
                        found.setSourcePosition(ve);
                        expressions.set(i, found);
                    } else {
                        addError(BASE_BAD_PARAM_ERROR + "a list containing an unresolvable reference to '" + ve.getName() + "'.", next);
                    }
                } else if (!(next instanceof ClassExpression)) {
                    addError(BASE_BAD_PARAM_ERROR + "a list containing type: " + next.getType().getName() + ".", next);
                }
            }
            checkDuplicateNameClashes(list);
        } else if (expr != null) {
            addError(BASE_BAD_PARAM_ERROR + "a type: " + expr.getType().getName() + ".", expr);
        }
        return list;
    }

    private ClassNode getSourceUnitClass(VariableExpression ve) {
        List<ClassNode> classes = source.getAST().getClasses();
        for (ClassNode classNode : classes) {
            if (classNode.getNameWithoutPackage().equals(ve.getName())) return classNode;
        }
        return null;
    }

    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof MethodCallExpression && candidate == null) {
            MethodCallExpression mce = (MethodCallExpression) expr;
            Expression args = transform(mce.getArguments());
            if (isNewifyCandidate(mce)) {
                Expression transformed = transformMethodCall(mce, args);
                transformed.setSourcePosition(mce);
                return transformed;
            }
            Expression method = transform(mce.getMethod());
            Expression object = transform(mce.getObjectExpression());
            MethodCallExpression transformed = new MethodCallExpression(object, method, args);
            transformed.setSourcePosition(mce);
            return transformed;
        } else if (expr instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) expr;
            if (de == candidate) {
                candidate = null;
                Expression left = de.getLeftExpression();
                Expression right = transform(de.getRightExpression());
                DeclarationExpression newDecl = new DeclarationExpression(left, de.getOperation(), right);
                newDecl.addAnnotations(de.getAnnotations());
                return newDecl;
            }
            return de;
        }
        return expr.transformExpression(this);
    }

    private void newifyClass(ClassNode cNode, boolean autoFlag, ListExpression list) {
        String cName = cNode.getName();
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cName + "'. @"
                    + MY_NAME + " not allowed for interfaces.", cNode);
        }
        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        classesToNewify = list;
        auto = autoFlag;
        super.visitClass(cNode);
        classesToNewify = oldClassesToNewify;
        auto = oldAuto;
    }

    private void newifyMethodOrField(AnnotatedNode parent, boolean autoFlag, ListExpression list) {
        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        checkClassLevelClashes(list);
        checkAutoClash(autoFlag, parent);
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
        final Set<String> seen = new HashSet<String>();
        @SuppressWarnings("unchecked")
        final List<ClassExpression> classes = (List)list.getExpressions();
        for (ClassExpression ce : classes) {
            final String name = ce.getType().getNameWithoutPackage();
            if (seen.contains(name)) {
                addError("Duplicate name '" + name + "' found during @" + MY_NAME + " processing.", ce);
            }
            seen.add(name);
        }
    }

    private void checkAutoClash(boolean autoFlag, AnnotatedNode parent) {
        if (auto && !autoFlag) {
            addError("Error during @" + MY_NAME + " processing. The 'auto' flag can't be false at " +
                    "method/constructor/field level if it is true at the class level.", parent);
        }
    }

    private void checkClassLevelClashes(ListExpression list) {
        @SuppressWarnings("unchecked")
        final List<ClassExpression> classes = (List)list.getExpressions();
        for (ClassExpression ce : classes) {
            final String name = ce.getType().getNameWithoutPackage();
            if (findClassWithMatchingBasename(name)) {
                addError("Error during @" + MY_NAME + " processing. Class '" + name + "' can't appear at " +
                        "method/constructor/field level if it already appears at the class level.", ce);
            }
        }
    }

    private boolean findClassWithMatchingBasename(String nameWithoutPackage) {
        if (classesToNewify == null) return false;
        @SuppressWarnings("unchecked")
        final List<ClassExpression> classes = (List)classesToNewify.getExpressions();
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
        // set the args as they might have gotten Newify transformed GROOVY-3491
        mce.setArguments(args);
        return mce;
    }

    private ClassNode findMatchingCandidateClass(MethodCallExpression mce) {
        if (classesToNewify == null) return null;
        @SuppressWarnings("unchecked")
        List<ClassExpression> classes = (List)classesToNewify.getExpressions();
        for (ClassExpression ce : classes) {
            final ClassNode type = ce.getType();
            if (type.getNameWithoutPackage().equals(mce.getMethodAsString())) {
                return type;
            }
        }
        return null;
    }

    private void internalError(String message) {
        throw new GroovyBugError("Internal error: " + message);
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
}
