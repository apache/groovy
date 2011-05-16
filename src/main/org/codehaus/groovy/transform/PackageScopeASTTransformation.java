/*
 * Copyright 2008-2010 the original author or authors.
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

import groovy.transform.PackageScope;
import groovy.transform.PackageScopeTarget;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles transformation for the @PackageScope annotation.
 * <p>
 * Both the deprecated groovy.lang.PackageScope and groovy.transform.PackageScope
 * annotations are supported. The former will be removed in a future version of Groovy.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class PackageScopeASTTransformation implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = PackageScope.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final String LEGACY_TYPE_NAME = "groovy.lang.PackageScope";
    private static final Class TARGET_CLASS = groovy.transform.PackageScopeTarget.class;
    private static final String TARGET_CLASS_NAME = ClassHelper.make(TARGET_CLASS).getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        boolean legacyMode = LEGACY_TYPE_NAME.equals(node.getClassNode().getName());
        if (!MY_TYPE.equals(node.getClassNode()) && !legacyMode) return;

        Expression value = node.getMember("value");
        if (parent instanceof ClassNode) {
            List<groovy.transform.PackageScopeTarget> targets;
            if (value == null) targets = Arrays.asList(legacyMode ? PackageScopeTarget.FIELDS: PackageScopeTarget.CLASS);
            else targets = determineTargets(value);
            visitClassNode((ClassNode) parent, targets);
            parent.getAnnotations();
        } else {
            if (value != null)
                throw new RuntimeException("Error during " + MY_TYPE_NAME
                            + " processing: " + TARGET_CLASS_NAME + " only allowed at class level.");
            if (parent instanceof MethodNode) {
                visitMethodNode((MethodNode) parent);
            } else if (parent instanceof FieldNode) {
                visitFieldNode((FieldNode) parent);
            }
        }
    }

    private void visitMethodNode(MethodNode methodNode) {
        if (methodNode.isSyntheticPublic()) revertVisibility(methodNode);
        else throw new RuntimeException("Can't use " + MY_TYPE_NAME + " for method '" + methodNode.getName() + "' which has explicit visibility.");
    }

    private void visitClassNode(ClassNode cNode, List<PackageScopeTarget> value) {
        String cName = cNode.getName();
        if (cNode.isInterface() && value.size() != 1 && value.get(0) != PackageScopeTarget.CLASS) {
            throw new RuntimeException("Error processing interface '" + cName + "'. " + MY_TYPE_NAME + " not allowed for interfaces except when targeting Class level.");
        }
        if (value.contains(groovy.transform.PackageScopeTarget.CLASS)) {
            if (cNode.isSyntheticPublic()) revertVisibility(cNode);
            else throw new RuntimeException("Can't use " + MY_TYPE_NAME + " for class '" + cNode.getName() + "' which has explicit visibility.");
        }
        if (value.contains(groovy.transform.PackageScopeTarget.METHODS)) {
            final List<MethodNode> mList = cNode.getMethods();
            for (MethodNode mNode : mList) {
                if (mNode.isSyntheticPublic()) revertVisibility(mNode);
            }
        }
        if (value.contains(PackageScopeTarget.FIELDS)) {
            final List<PropertyNode> pList = cNode.getProperties();
            List<PropertyNode> foundProps = new ArrayList<PropertyNode>();
            List<String> foundNames = new ArrayList<String>();
            for (PropertyNode pNode : pList) {
                foundProps.add(pNode);
                foundNames.add(pNode.getName());
            }
            for (PropertyNode pNode : foundProps) {
                pList.remove(pNode);
            }
            final List<FieldNode> fList = cNode.getFields();
            for (FieldNode fNode : fList) {
                if (foundNames.contains(fNode.getName())) {
                    revertVisibility(fNode);
                }
            }
        }
    }

    private void visitFieldNode(FieldNode fNode) {
        final ClassNode cNode = fNode.getDeclaringClass();
        final List<PropertyNode> pList = cNode.getProperties();
        PropertyNode foundProp = null;
        for (PropertyNode pNode : pList) {
            if (pNode.getName().equals(fNode.getName())) {
                foundProp = pNode;
                break;
            }
        }
        if (foundProp != null) {
            revertVisibility(fNode);
            pList.remove(foundProp);
        }
    }

    private void revertVisibility(FieldNode fNode) {
        fNode.setModifiers(fNode.getModifiers() & ~ACC_PRIVATE);
    }

    private void revertVisibility(MethodNode mNode) {
        mNode.setModifiers(mNode.getModifiers() & ~ACC_PUBLIC);
    }

    private void revertVisibility(ClassNode cNode) {
        cNode.setModifiers(cNode.getModifiers() & ~ACC_PUBLIC);
    }

    private List<groovy.transform.PackageScopeTarget> determineTargets(Expression expr) {
        List<groovy.transform.PackageScopeTarget> list = new ArrayList<groovy.transform.PackageScopeTarget>();
        if (expr instanceof PropertyExpression) {
            list.add(extractTarget((PropertyExpression) expr));
        } else if (expr instanceof ListExpression) {
            final ListExpression expressionList = (ListExpression) expr;
            final List<Expression> expressions = expressionList.getExpressions();
            for (Expression ex : expressions) {
                if (ex instanceof PropertyExpression) {
                    list.add(extractTarget((PropertyExpression) ex));
                }
            }
        }
        return list;
    }

    private groovy.transform.PackageScopeTarget extractTarget(PropertyExpression expr) {
        Expression oe = expr.getObjectExpression();
        if (oe instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression) oe;
            if (ce.getType().getName().equals("groovy.transform.PackageScopeTarget")) {
                Expression prop = expr.getProperty();
                if (prop instanceof ConstantExpression) {
                    String propName = (String) ((ConstantExpression) prop).getValue();
                    try {
                        return PackageScopeTarget.valueOf(propName);
                    } catch(IllegalArgumentException iae) {
                        /* ignore */
                    }
                }
            }
        }
        throw new RuntimeException("Internal error during " + MY_TYPE_NAME
                + " processing. Annotation parameters must be of type: " + TARGET_CLASS_NAME + ".");
    }

}
