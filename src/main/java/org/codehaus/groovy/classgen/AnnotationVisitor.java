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
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
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

    public AnnotationVisitor(SourceUnit source, ErrorCollector errorCollector) {
        this.source = source;
        this.errorCollector = errorCollector;
    }

    public void setReportClass(ClassNode cn) {
        reportClass = cn;
    }

    public AnnotationNode visit(AnnotationNode node) {
        this.annotation = node;
        this.reportClass = node.getClassNode();

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
        
        Map<String, Expression> attributes = node.getMembers();
        for (Map.Entry<String, Expression> entry : attributes.entrySet()) {
            String attrName = entry.getKey();
            ClassNode attrType = getAttributeType(node, attrName);
            Expression attrExpr = transformInlineConstants(entry.getValue(), attrType);
            entry.setValue(attrExpr);
            visitExpression(attrName, attrExpr, attrType);
        }
        VMPluginFactory.getPlugin().configureAnnotation(node);
        return this.annotation;
    }
    
    private boolean checkIfValidEnumConstsAreUsed(AnnotationNode node) {
        Map<String, Expression> attributes = node.getMembers();
        for (Map.Entry<String, Expression> entry : attributes.entrySet()) {
            if (!validateEnumConstant(entry.getValue()))
                return false;
        }
        return true;
    }
    
    private boolean validateEnumConstant(Expression exp) {
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

    private boolean checkIfMandatoryAnnotationValuesPassed(AnnotationNode node) {
        boolean ok = true;
        Map attributes = node.getMembers();
        ClassNode classNode = node.getClassNode();
        for (MethodNode mn : classNode.getMethods()) {
            String methodName = mn.getName();
            // if the annotation attribute has a default, getCode() returns a ReturnStatement with the default value
            if (mn.getCode() == null && !attributes.containsKey(methodName)) {
                addError("No explicit/default value found for annotation attribute '" + methodName + "'", node);
                ok = false;
            }
        }
        return ok;
    }

    private ClassNode getAttributeType(AnnotationNode node, String attrName) {
        ClassNode classNode = node.getClassNode();
        List methods = classNode.getMethods(attrName);
        // if size is >1, then the method was overwritten or something, we ignore that
        // if it is an error, we have to test it at another place. But size==0 is
        // an error, because it means that no such attribute exists.
        if (methods.isEmpty()) {
            addError("'" + attrName + "'is not part of the annotation " + classNode, node);
            return ClassHelper.OBJECT_TYPE;
        }
        MethodNode method = (MethodNode) methods.get(0);
        return method.getReturnType();
    }

    private static boolean isValidAnnotationClass(ClassNode node) {
        return node.implementsInterface(ClassHelper.Annotation_TYPE);
    }

    protected void visitExpression(String attrName, Expression attrExp, ClassNode attrType) {
        if (attrType.isArray()) {
            // check needed as @Test(attr = {"elem"}) passes through the parser
            if (attrExp instanceof ListExpression) {
                ListExpression le = (ListExpression) attrExp;
                visitListExpression(attrName, le, attrType.getComponentType());
            } else if (attrExp instanceof ClosureExpression) {
                addError("Annotation list attributes must use Groovy notation [el1, el2]", attrExp);
            } else {
                // treat like a singleton list as per Java
                ListExpression listExp = new ListExpression();
                listExp.addExpression(attrExp);
                if (annotation != null) {
                    annotation.setMember(attrName, listExp);
                }
                visitExpression(attrName, listExp, attrType);
            }
        } else if (ClassHelper.isPrimitiveType(attrType)) {
            visitConstantExpression(attrName, getConstantExpression(attrExp, attrType), ClassHelper.getWrapper(attrType));
        } else if (ClassHelper.STRING_TYPE.equals(attrType)) {
            visitConstantExpression(attrName, getConstantExpression(attrExp, attrType), ClassHelper.STRING_TYPE);
        } else if (ClassHelper.CLASS_Type.equals(attrType)) {
            if (!(attrExp instanceof ClassExpression || attrExp instanceof ClosureExpression)) {
                addError("Only classes and closures can be used for attribute '" + attrName + "'", attrExp);
            }
        } else if (attrType.isDerivedFrom(ClassHelper.Enum_Type)) {
            if (attrExp instanceof PropertyExpression) {
                visitEnumExpression(attrName, (PropertyExpression) attrExp, attrType);
            } else {
                addError("Expected enum value for attribute " + attrName, attrExp);
            }
        } else if (isValidAnnotationClass(attrType)) {
            if (attrExp instanceof AnnotationConstantExpression) {
                visitAnnotationExpression(attrName, (AnnotationConstantExpression) attrExp, attrType);
            } else {
                addError("Expected annotation of type '" + attrType.getName() + "' for attribute " + attrName, attrExp);
            }
        } else {
            addError("Unexpected type " + attrType.getName(), attrExp);
        }
    }

    public void checkReturnType(ClassNode attrType, ASTNode node) {
        if (attrType.isArray()) {
            checkReturnType(attrType.getComponentType(), node);
        } else if (ClassHelper.isPrimitiveType(attrType)) {
        } else if (ClassHelper.STRING_TYPE.equals(attrType)) {
        } else if (ClassHelper.CLASS_Type.equals(attrType)) {
        } else if (attrType.isDerivedFrom(ClassHelper.Enum_Type)) {
        } else if (isValidAnnotationClass(attrType)) {
        } else {
            addError("Unexpected return type " + attrType.getName(), node);
        }
    }

    private ConstantExpression getConstantExpression(Expression exp, ClassNode attrType) {
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
        return ConstantExpression.EMPTY_EXPRESSION;
    }

    /**
     * @param attrName   the name
     * @param expression the expression
     * @param attrType   the type
     */
    protected void visitAnnotationExpression(String attrName, AnnotationConstantExpression expression, ClassNode attrType) {
        AnnotationNode annotationNode = (AnnotationNode) expression.getValue();
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, this.errorCollector);
        // TODO track Deprecated usage and give a warning?
        visitor.visit(annotationNode);
    }

    protected void visitListExpression(String attrName, ListExpression listExpr, ClassNode elementType) {
        for (Expression expression : listExpr.getExpressions()) {
            visitExpression(attrName, expression, elementType);
        }
    }

    protected void visitConstantExpression(String attrName, ConstantExpression constExpr, ClassNode attrType) {
        ClassNode constType = constExpr.getType();
        ClassNode wrapperType = ClassHelper.getWrapper(constType);
        if (!hasCompatibleType(attrType, wrapperType)) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName()
                    + "'; but found type '" + constType.getName() + "'", constExpr);
        }
    }

    private static boolean hasCompatibleType(ClassNode attrType, ClassNode wrapperType) {
        return wrapperType.isDerivedFrom(ClassHelper.getWrapper(attrType));
    }

    protected void visitEnumExpression(String attrName, PropertyExpression propExpr, ClassNode attrType) {
        if (!propExpr.getObjectExpression().getType().isDerivedFrom(attrType)) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName() + "' (Enum), but found "
                    + propExpr.getObjectExpression().getType().getName(),
                    propExpr);
        }
    }

    protected void addError(String msg) {
        addError(msg, this.annotation);
    }

    protected void addError(String msg, ASTNode expr) {
        this.errorCollector.addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + " in @" + this.reportClass.getName() + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), this.source)
        );
    }

    public void checkCircularReference(ClassNode searchClass, ClassNode attrType, Expression startExp) {
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
