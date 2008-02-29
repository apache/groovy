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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;


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
    private ClassNode annotationClass;
    
    public AnnotationVisitor(SourceUnit source, ErrorCollector errorCollector) {
        this.source = source;
        this.errorCollector = errorCollector;
    }
    
    public AnnotationNode visit(AnnotationNode node) {
        this.annotation = node;
        this.annotationClass = node.getClassNode();

        if(!isValidAnnotationClass(node.getClassNode())) {
            addError("class "+node.getClassNode().getName()+" is no annotation");
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
        
        return this.annotation;
    }

    private ClassNode getAttributeType(AnnotationNode node, String attrName) {
        List methods = node.getClassNode().getMethods(attrName);
        // if size is >1, then the method was overwriten or something, we ignore that
        // if it is an error, we have to test it at another place. But size==0 is
        // an error, because it means that no such attribute exists.
        if (methods.size()==0) {
            addError("'"+attrName+"'is not aprt of the annotation "+node.getClassNode(),node);
            return ClassHelper.OBJECT_TYPE;
        }
        MethodNode method = (MethodNode) methods.get(0);
        return method.getReturnType();
    }

    /**
     * @param node
     * @return
     */
    private boolean isValidAnnotationClass(ClassNode node) {
        return node.implementsInterface("java.lang.annotation.Annotation");
    }

    protected void visitExpression(String attrName, Expression attrExp, ClassNode attrType) {
        if(attrType.isArray()) {
            // check needed as @Test(attr = {"elem"}) passes through the parser
            if(attrExp instanceof ListExpression) {
                visitListExpression(attrName, (ListExpression) attrExp, attrType.getComponentType());
            }
            else {
                addError("Annotation list attributes must use Groovy notation [el1, el2]", attrExp);
            }
        }
        if (ClassHelper.isPrimitiveType(attrType)) {
            visitConstantExpression(attrName, getConstantExpression(attrExp), ClassHelper.getWrapper(attrType));
        } else if (ClassHelper.STRING_TYPE==attrType) {
            visitConstantExpression(attrName, getConstantExpression(attrExp), ClassHelper.STRING_TYPE);
        } else if (ClassHelper.CLASS_Type==attrType) {
            // there is nothing to check about ClassExpressions
        } else if (attrType.isDerivedFrom(ClassHelper.Enum_Type)) {
            if(attrExp instanceof PropertyExpression) {
                visitEnumExpression(attrName, (PropertyExpression) attrExp, attrType);
            } else {
                addError("Value not defined for annotation attribute " + attrName, attrExp);
            }
        } else if (isValidAnnotationClass(attrType)) {
            visitAnnotationExpression(attrName, (AnnotationConstantExpression) attrExp, attrType);
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
                  + " in @" + this.annotationClass.getName() + '\n',
                  expr.getLineNumber(), 
                  expr.getColumnNumber()), this.source)
        );
    }
}
