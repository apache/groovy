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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;

/**
 * An Annotation visitor responsible for:
 * <ul>
 * <li>reading annotation metadata (@Retention, @Target, attribute types)</li>
 * <li>verify that an <code>AnnotationNode</code> conforms to annotation meta</li>
 * <li>enhancing an <code>AnnotationNode</code> AST to reflect real annotation meta</li>
 * </ul>
 */
public class AnnotationVisitor {

    private final SourceUnit source;
    private final ErrorCollector errorCollector;

    private AnnotationNode annotation;
    private ClassNode reportClass;

    public AnnotationVisitor(final SourceUnit source, final ErrorCollector errorCollector) {
        this.source = source;
        this.errorCollector = errorCollector;
    }

    public void setReportClass(final ClassNode node) {
        this.reportClass = node;
    }

    public AnnotationNode visit(final AnnotationNode node) {
        this.annotation = node;
        setReportClass(node.getClassNode());

        if (!isValidAnnotationClass(node.getClassNode())) {
            addError("class " + node.getClassNode().getName() + " is not an annotation");
            return node;
        }

        // check if values have been passed for all annotation attributes that don't have defaults
        if (!checkIfMandatoryAnnotationValuesPassed(node)) {
            return node;
        }

        // if enum constants have been used, check if they are all valid
        if (!checkIfValidEnumConstsAreUsed(node)) {
            return node;
        }

        for (Map.Entry<String, Expression> entry : node.getMembers().entrySet()) {
            String attrName = entry.getKey();
            ClassNode attrType = getAttributeType(node, attrName);
            Expression attrExpr = transformInlineConstants(entry.getValue(), attrType);
            entry.setValue(attrExpr);
            visitExpression(attrName, attrExpr, attrType);
        }
        VMPluginFactory.getPlugin().configureAnnotation(node);
        return this.annotation;
    }

    private boolean checkIfValidEnumConstsAreUsed(final AnnotationNode node) {
        Map<String, Expression> attributes = node.getMembers();
        for (Map.Entry<String, Expression> entry : attributes.entrySet()) {
            if (!validateEnumConstant(entry.getValue()))
                return false;
        }
        return true;
    }

    private boolean validateEnumConstant(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            String name = pe.getPropertyAsString();
            if (pe.getObjectExpression() instanceof ClassExpression && name != null) {
                ClassExpression ce = (ClassExpression) pe.getObjectExpression();
                ClassNode type = ce.getType();
                if (type.isEnum()) {
                    boolean ok = false;
                    try {
                        FieldNode enumField = type.getDeclaredField(name);
                        ok = enumField != null && enumField.getType().equals(type);
                    } catch(Exception ex) {
                        // ignore
                    }
                    if(!ok) {
                        addError("No enum const " + type.getName() + "." + name, pe);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkIfMandatoryAnnotationValuesPassed(final AnnotationNode node) {
        boolean ok = true;
        for (MethodNode mn : node.getClassNode().getMethods()) {
            if (!mn.hasAnnotationDefault() && !node.getMembers().containsKey(mn.getName()) && !"dataVariableNames".equals(mn.getName())) {
                // TODO: https://github.com/spockframework/spock/issues/1549               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                addError("No explicit/default value found for annotation attribute '" + mn.getName() + "'", node);
                ok = false;
            }
        }
        return ok;
    }

    private ClassNode getAttributeType(final AnnotationNode node, final String attrName) {
        ClassNode classNode = node.getClassNode();
        List<MethodNode> methods = classNode.getMethods(attrName);
        // if size is >1, then the method was overwritten or something, we ignore that
        // if it is an error, we have to test it at another place. But size==0 is
        // an error, because it means that no such attribute exists.
        if (methods.isEmpty()) {
            addError("'" + attrName + "' is not part of the annotation " + classNode.getNameWithoutPackage(), node);
            return ClassHelper.OBJECT_TYPE;
        }
        return methods.get(0).getReturnType();
    }

    private static boolean isValidAnnotationClass(final ClassNode type) {
        return type.implementsInterface(ClassHelper.Annotation_TYPE);
    }

    protected void visitExpression(final String attrName, final Expression valueExpr, final ClassNode attrType) {
        if (attrType.isArray()) {
            // check needed as @Test(attr = {"elem"}) passes through the parser
            if (valueExpr instanceof ListExpression) {
                ListExpression le = (ListExpression) valueExpr;
                visitListExpression(attrName, le, attrType.getComponentType());
            } else if (valueExpr instanceof ClosureExpression) {
                addError("Annotation list attributes must use Groovy notation [el1, el2]", valueExpr);
            } else {
                // treat like a singleton list as per Java
                ListExpression listExp = new ListExpression();
                listExp.addExpression(valueExpr);
                if (annotation != null) {
                    annotation.setMember(attrName, listExp);
                }
                visitExpression(attrName, listExp, attrType);
            }
        } else if (ClassHelper.isPrimitiveType(attrType) || ClassHelper.isStringType(attrType)) {
            visitConstantExpression(attrName, getConstantExpression(valueExpr, attrType), ClassHelper.getWrapper(attrType));
        } else if (ClassHelper.isClassType(attrType)) {
            if (!(valueExpr instanceof ClassExpression || valueExpr instanceof ClosureExpression)) {
                addError("Only classes and closures can be used for attribute '" + attrName + "'", valueExpr);
            }
        } else if (attrType.isDerivedFrom(ClassHelper.Enum_Type)) {
            if (valueExpr instanceof PropertyExpression) {
                visitEnumExpression(attrName, (PropertyExpression) valueExpr, attrType);
            } else if (valueExpr instanceof ConstantExpression) {
                visitConstantExpression(attrName, getConstantExpression(valueExpr, attrType), attrType);
            } else {
                addError("Expected enum value for attribute " + attrName, valueExpr);
            }
        } else if (isValidAnnotationClass(attrType)) {
            if (valueExpr instanceof AnnotationConstantExpression) {
                visitAnnotationExpression(attrName, (AnnotationConstantExpression) valueExpr, attrType);
            } else {
                addError("Expected annotation of type '" + attrType.getName() + "' for attribute " + attrName, valueExpr);
            }
        } else {
            addError("Unexpected type " + attrType.getName(), valueExpr);
        }
    }

    public void checkReturnType(final ClassNode attrType, final ASTNode node) {
        if (attrType.isArray()) {
            checkReturnType(attrType.getComponentType(), node);
        } else if (ClassHelper.isPrimitiveType(attrType)) {
        } else if (ClassHelper.isStringType(attrType)) {
        } else if (ClassHelper.isClassType(attrType)) {
        } else if (attrType.isDerivedFrom(ClassHelper.Enum_Type)) {
        } else if (isValidAnnotationClass(attrType)) {
        } else {
            addError("Unexpected return type " + attrType.getName(), node);
        }
    }

    private ConstantExpression getConstantExpression(final Expression exp, final ClassNode attrType) {
        Expression result = exp;
        if (!(result instanceof ConstantExpression)) {
            result = transformInlineConstants(result, attrType);
        }
        if (result instanceof ConstantExpression) {
            return (ConstantExpression) result;
        }

        String base = "Expected '" + exp.getText() + "' to be an inline constant of type " + attrType.getName();
        if (exp instanceof PropertyExpression) {
            addError(base + " not a property expression", exp);
        } else if (exp instanceof VariableExpression && ((VariableExpression)exp).getAccessedVariable() instanceof FieldNode) {
            addError(base + " not a field expression", exp);
        } else {
            addError(base, exp);
        }

        ConstantExpression ret = new ConstantExpression(null);
        ret.setSourcePosition(exp);
        return ret;
    }

    protected void visitListExpression(final String attrName, final ListExpression listExpr, final ClassNode elementType) {
        for (Expression expression : listExpr.getExpressions()) {
            visitExpression(attrName, expression, elementType);
        }
    }

    protected void visitEnumExpression(final String attrName, final PropertyExpression valueExpr, final ClassNode attrType) {
        ClassNode valueType = valueExpr.getObjectExpression().getType();
        if (!valueType.isDerivedFrom(attrType)) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName() + "' (Enum), but found " + valueType.getName(), valueExpr);
        }
    }

    protected void visitConstantExpression(final String attrName, final ConstantExpression valueExpr, final ClassNode attrType) {
        ClassNode valueType = valueExpr.getType();
        if (!ClassHelper.getWrapper(valueType).isDerivedFrom(ClassHelper.getWrapper(attrType))) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName() + "'; but found type '" + valueType.getName() + "'", valueExpr);
        }
    }

    protected void visitAnnotationExpression(final String attrName, final AnnotationConstantExpression valueExpr, final ClassNode attrType) {
        AnnotationNode annotationNode = (AnnotationNode) valueExpr.getValue();
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, this.errorCollector);
        // TODO: Track @Deprecated usage and give a warning?
        visitor.visit(annotationNode);
    }

    protected void addError(final String msg) {
        addError(msg, this.annotation);
    }

    protected void addError(final String msg, final ASTNode node) {
        this.errorCollector.addErrorAndContinue(msg + " in @" + this.reportClass.getName() + '\n', node, this.source);
    }

    public void checkCircularReference(final ClassNode searchClass, final ClassNode attrType, final Expression startExp) {
        if (!isValidAnnotationClass(attrType)) return;
        if (!(startExp instanceof AnnotationConstantExpression)) {
            addError("Found '" + startExp.getText() + "' when expecting an Annotation Constant", startExp);
            return;
        }
        AnnotationConstantExpression ace = (AnnotationConstantExpression) startExp;
        AnnotationNode annotationNode = (AnnotationNode) ace.getValue();
        if (annotationNode.getClassNode().equals(searchClass)) {
            addError("Circular reference discovered in " + searchClass.getName(), startExp);
            return;
        }
        ClassNode cn = annotationNode.getClassNode();
        for (MethodNode method : cn.getMethods()) {
            if (method.getReturnType().equals(searchClass)) {
                addError("Circular reference discovered in " + cn.getName(), startExp);
            }
            ReturnStatement code = (ReturnStatement) method.getCode();
            if (code == null) continue;
            checkCircularReference(searchClass, method.getReturnType(), code.getExpression());
        }
    }
}
