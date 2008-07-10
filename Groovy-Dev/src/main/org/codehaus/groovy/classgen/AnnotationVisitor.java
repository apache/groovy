/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.classgen;

import java.util.*;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.vmplugin.VMPluginFactory;


/**
 * An Annotation visitor responsible with:
 * - reading annotation metadata (@Retention, @Target, attribute types)
 * - verify that an <code>AnnotationNode</code> conforms to annotation meta
 * - enhancing an <code>AnnotationNode</code> AST to reflect real annotation meta
 * 
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class AnnotationVisitor {
    private SourceUnit source;
    private ErrorCollector errorCollector;
    
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

        if(!isValidAnnotationClass(node.getClassNode())) {
            addError("class " + node.getClassNode().getName() + " is not an annotation");
            return node;
        }
        
        Map attributes = node.getMembers();
        for(Iterator it = attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String attrName = (String) entry.getKey();
            Expression attrExpr = (Expression) entry.getValue();
            ClassNode attrType = getAttributeType(node, attrName);
            visitExpression(attrName, attrExpr, attrType);
        }

        VMPluginFactory.getPlugin().configureAnnotation(node);

        return this.annotation;
    }

    private ClassNode getAttributeType(AnnotationNode node, String attrName) {
        List methods = node.getClassNode().getMethods(attrName);
        // if size is >1, then the method was overwritten or something, we ignore that
        // if it is an error, we have to test it at another place. But size==0 is
        // an error, because it means that no such attribute exists.
        if (methods.size() == 0) {
            addError("'" + attrName + "'is not part of the annotation " + node.getClassNode(), node);
            return ClassHelper.OBJECT_TYPE;
        }
        MethodNode method = (MethodNode) methods.get(0);
        return method.getReturnType();
    }

    private boolean isValidAnnotationClass(ClassNode node) {
        return node.implementsInterface(ClassHelper.Annotation_TYPE);
    }

    protected void visitExpression(String attrName, Expression attrExp, ClassNode attrType) {
        if (attrType.isArray()) {
            // check needed as @Test(attr = {"elem"}) passes through the parser
            if (attrExp instanceof ListExpression) {
                ListExpression le = (ListExpression) attrExp;
                visitListExpression(attrName, (ListExpression) attrExp, attrType.getComponentType());
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
            visitConstantExpression(attrName, getConstantExpression(attrExp), ClassHelper.getWrapper(attrType));
        } else if (ClassHelper.STRING_TYPE.equals(attrType)) {
            visitConstantExpression(attrName, getConstantExpression(attrExp), ClassHelper.STRING_TYPE);
        } else if (ClassHelper.CLASS_Type.equals(attrType)) {
            if (!(attrExp instanceof ClassExpression)) {
                addError("Only classes can be used for attribute '"+attrName+"'",attrExp);
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
                addError("Expected annotation of type '"+attrType.getName()+"' for attribute "+attrName, attrExp);
            }
        } else {
            addError("Unexpected type "+attrType.getName(),attrExp); 
        }
    }

    public void checkReturnType(ClassNode attrType,ASTNode node) {
        if(attrType.isArray()) {
             checkReturnType(attrType.getComponentType(),node);
        } else if (ClassHelper.isPrimitiveType(attrType)) {
             return;
        } else if (ClassHelper.STRING_TYPE.equals(attrType)) {
             return;
        } else if (ClassHelper.CLASS_Type.equals(attrType)) {
             return;
        } else if (attrType.isDerivedFrom(ClassHelper.Enum_Type)) {
             return;
        } else if (isValidAnnotationClass(attrType)) {
            return;
        } else {
            addError("Unexpected return type "+attrType.getName(),node);
        }
    }

    private ConstantExpression getConstantExpression(Expression exp) {
        if (exp instanceof ConstantExpression) {
            return (ConstantExpression) exp;
        } else {
            addError("expected a constant",exp);
            return ConstantExpression.EMTPY_EXPRESSION;
        }
    }
    
    /**
     * @param attrName
     * @param expression
     * @param attrType
     */
    protected void visitAnnotationExpression(String attrName, AnnotationConstantExpression expression, ClassNode attrType) {
        AnnotationNode annotationNode = (AnnotationNode) expression.getValue();
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, this.errorCollector);
        visitor.visit(annotationNode);
    }

    protected void visitListExpression(String attrName, ListExpression listExpr, ClassNode elementType) {
        List expressions = listExpr.getExpressions();
        for (int i = 0; i < expressions.size(); i++) {
            visitExpression(attrName, (Expression) expressions.get(i), elementType);
        }
    }
    
    protected void visitConstantExpression(String attrName, ConstantExpression constExpr, ClassNode attrType) {
        if(!constExpr.getType().isDerivedFrom(attrType)) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName() + "'; "
                    + "but found type '" + constExpr.getType().getName() + "'",
                    constExpr);
        }
    }
    
    protected void visitEnumExpression(String attrName, PropertyExpression propExpr, ClassNode attrType) {
        if(!propExpr.getObjectExpression().getType().isDerivedFrom(attrType)) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName() +"' (Enum), but found "
                    + propExpr.getObjectExpression().getType().getName(), 
                    propExpr);
        }
    }
    
    protected void addError(String msg) {
        addError(msg,this.annotation);
    }
    
    protected void addError(String msg, ASTNode expr) {
        this.errorCollector.addErrorAndContinue(
          new SyntaxErrorMessage(new SyntaxException(msg 
                  + " in @" + this.reportClass.getName() + '\n',
                  expr.getLineNumber(), 
                  expr.getColumnNumber()), this.source)
        );
    }

    public void checkcircularReference(ClassNode searchClass, ClassNode attrType,Expression startExp) {
        if (!isValidAnnotationClass(attrType)) return;
        AnnotationConstantExpression ace = (AnnotationConstantExpression) startExp;
        AnnotationNode annotationNode = (AnnotationNode) ace.getValue();
        if (annotationNode.getClassNode().equals(searchClass)) {
            addError ("Cirecular reference discovered in "+searchClass.getName(),startExp);
            return;
        }
        ClassNode cn = annotationNode.getClassNode();
        List methods = cn.getMethods();
        for(Iterator it=methods.iterator(); it.hasNext();) {
            MethodNode method = (MethodNode) it.next();
            if (method.getReturnType().equals(searchClass)) {
                addError ("Cirecular reference discovered in "+cn.getName(),startExp);
            }
                        
            ReturnStatement code = (ReturnStatement) method.getCode();
            if (code==null) continue;
            checkcircularReference(searchClass,method.getReturnType(),code.getExpression());
        }
    }

}
