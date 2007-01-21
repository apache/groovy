/*
 $Id: AnnotationNode.java 10909 2006-12-02 19:31:27Z blackdrag $

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.classgen;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
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
 * Current limitations:
 * - array attributes are not supported
 * - annotation attributes are not supported
 * 
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class AnnotationVisitor {
    private Class annotationRootClass;
    private SourceUnit source;
    private ErrorCollector errorCollector;
    
    private AnnotationNode annotation;
    private Class annotationClass;
    private Map requiredAttrTypes = new HashMap(); // Map<String, Class>
    private Map defaultAttrTypes = new HashMap();  // Map<String, Class>
    
    public AnnotationVisitor(SourceUnit source, ErrorCollector errorCollector) {
        this.source = source;
        this.errorCollector = errorCollector;
        this.annotationRootClass = loadAnnotationRootClass();
    }
    
    public AnnotationNode visit(AnnotationNode node) {
        if(!isValidAnnotationClass(node)) {
            node.setValid(false);
            return node;
        }
        
        this.annotation = node;
        this.annotationClass = node.getClassNode().getTypeClass();
        
        extractAnnotationMeta();
        
        if(this.errorCollector.hasErrors()) {
            this.annotation.setValid(false);
            return this.annotation;
        }
        
        Map attributes = this.annotation.getMembers();
        for(Iterator it = attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String attrName = (String) entry.getKey();
            Expression attrExpr = (Expression) entry.getValue();
            Class attrType = getAttributeType(attrName);
            if(attrType == null) {
                addError("Unknown attribute '" + attrName, attrExpr);
                break;
            }
            visitExpression(attrName, attrExpr, attrType);
        }
        
        if(!this.requiredAttrTypes.isEmpty()) {
            addError("Required attributes " + this.requiredAttrTypes.keySet() + " not found",
                    this.annotation);
        }
        
        this.annotation.setValid(!this.errorCollector.hasErrors());
        return this.annotation;
    }
    
    /**
     * @param node
     * @return
     */
    private boolean isValidAnnotationClass(AnnotationNode node) {
        if(this.annotationRootClass == null) {
            addError("java.lang.annotation.Annotation class is not available."
                    + ExtendedVerifier.JVM_ERROR_MESSAGE);
            return false;
        }
        
        return this.annotationRootClass.isAssignableFrom(node.getClassNode().getTypeClass());
    }

    protected void visitExpression(String attrName, Expression attrAst, Class attrType) {
        if(attrType.isArray()) {
            addError("Attribute '" + attrName + "' is of Array type which is not yet supported", attrAst);
            return;
        }
        if(attrType.isPrimitive()) {
            visitConstantExpression(attrName, (ConstantExpression) attrAst, attrType);
        }
        else if(String.class.equals(attrType)) {
            visitConstantExpression(attrName, (ConstantExpression) attrAst, String.class);
        }
        else if(Class.class.equals(attrType)) {
            // there is nothing to check about ClassExpressions
        }
        else if(isEnum(attrType)) {
            visitEnumExpression(attrName, (PropertyExpression) attrAst, attrType);
        }
    }
    
    protected void visitConstantExpression(String attrName, ConstantExpression constExpr, Class attrType) {
        if(!isAssignable(attrType, constExpr.getType().getTypeClass())) {
            addError("Attribute '" + attrName + "' should have type '" + attrType.getName() + "'; "
                    + "but found type '" + constExpr.getType().getTypeClass().getName(),
                    constExpr);
        }
    }
    
    /**
     * Tests if two <code>Class</code> are assignable, considering also autoboxing.
     */
    private boolean isAssignable(Class attrType, Class typeClass) {
        Class clazz = attrType;
        if(attrType.isPrimitive()) {
            if(boolean.class.equals(attrType)) {
                clazz = Boolean.class;
            }
            else if(byte.class.equals(attrType)) {
                clazz = Byte.class;
            }
            else if(char.class.equals(attrType)) {
                clazz = Character.class;
            }
            else if(double.class.equals(attrType)) {
                clazz = Double.class;
            }
            else if(float.class.equals(attrType)) {
                clazz = Float.class;
            }
            else if(int.class.equals(attrType)) {
                clazz = Integer.class;
            }
            else if(long.class.equals(attrType)) {
                clazz = Long.class;
            }
            else if(short.class.equals(attrType)) {
                clazz = Short.class;
            }
        }

        return clazz.equals(typeClass);
    }
    
    protected void visitEnumExpression(String attrName, PropertyExpression propExpr, Class attrType) {
        if(!isEnum(propExpr.getObjectExpression().getType().getTypeClass())) {
            addError("Attribute '" + attrName + "' should have type Enum, but found "
                    + propExpr.getObjectExpression().getType().getTypeClass().getName(), 
                    propExpr);
        }
    }
    
    private boolean isEnum(Class clazz) {
        Boolean result = (Boolean) invoke(clazz.getClass(), "isEnum", new Class[0], clazz, new Object[0]);
        return result.booleanValue();
    }
    
    private void extractAnnotationMeta() {
        initializeAnnotationMeta();
        initializeAttributeTypes();
    }
    
    private void initializeAnnotationMeta() {
        Object[] annotations = (Object[]) invoke(this.annotationClass.getClass(), 
                "getAnnotations", new Class[0], this.annotationClass, new Object[0]);
        if (annotations == null) {
            addError("Cannot retrieve annotation meta information. " 
                    + ExtendedVerifier.JVM_ERROR_MESSAGE);
            return;
        }
        
        for(int i = 0; i < annotations.length; i++) {
            Class annotationType = (Class) invoke(this.annotationRootClass, "annotationType", new Class[0], annotations[i], new Object[0]);
            if (annotationType == null) continue;
            
            if ("java.lang.annotation.Retention".equals(annotationType.getName())) {
                initializeRetention(annotationType, annotations[i]);
            }
            else if("java.lang.annotation.Target".equals(annotationType.getName())) {
                initializeTarget(annotationType, annotations[i]);
            }
        }
    }
    
    private void initializeAttributeTypes() {
        Method[] methods = this.annotationClass.getDeclaredMethods();
        for(int i = 0; i < methods.length; i++) {
            Object defaultValue = invoke(Method.class, "getDefaultValue", new Class[0], methods[i], new Object[0]);
            if (defaultValue != null) { 
                // by now we know JDK1.5 API is available so a null means no default value
                defaultAttrTypes.put(methods[i].getName(), methods[i].getReturnType());
            }
            else {
                requiredAttrTypes.put(methods[i].getName(), methods[i].getReturnType());
            }
        }
    }
    
    private void initializeRetention(Class retentionClass, Object retentionAnnotation) {
        Object retentionPolicyEnum = 
            invoke(retentionClass, "value", new Class[0], retentionAnnotation, new Object[0]);
        if (retentionPolicyEnum == null) {
            addError("Cannot read @RetentionPolicy on the @" + this.annotationClass.getName() 
                    + ExtendedVerifier.JVM_ERROR_MESSAGE);
            return;
        }
        
        if("RUNTIME".equals(retentionPolicyEnum.toString())) {
            this.annotation.setRuntimeRetention(true);
        }
        else if("SOURCE".equals(retentionPolicyEnum.toString())) {
            this.annotation.setSourceRetention(true);
        }
    }
    
    private void initializeTarget(Class targetClass, Object targetAnnotation) {
        Object[] elementTypeEnum =
            (Object[]) invoke(targetClass, "value", new Class[0], targetAnnotation, new Object[0]);
        if (elementTypeEnum == null) {
            addError("Cannot read @Target on the @" + this.annotationClass.getName() 
                    + ExtendedVerifier.JVM_ERROR_MESSAGE);
            return;
        }
        int bitmap = 0;
        for (int i = 0; i < elementTypeEnum.length; i++) {
            String targetName = elementTypeEnum[i].toString();
            if("TYPE".equals(targetName)) {
                bitmap |= AnnotationNode.TYPE_TARGET;
            }
            else if("CONSTRUCTOR".equals(targetName)) {
                bitmap |= AnnotationNode.CONSTRUCTOR_TARGET;
            }
            else if("METHOD".equals(targetName)) {
                bitmap |= AnnotationNode.METHOD_TARGET;
            }
            else if("FIELD".equals(targetName)) {
                bitmap |= AnnotationNode.FIELD_TARGET;
            }
            else if("PARAMETER".equals(targetName)) {
                bitmap |= AnnotationNode.PARAMETER_TARGET;
            }
            else if("LOCAL_VARIABLE".equals(targetName)) {
                bitmap |= AnnotationNode.LOCAL_VARIABLE_TARGET;
            }
            else if("ANNOTATION".equals(targetName)) {
                bitmap |= AnnotationNode.ANNOTATION_TARGET;
            }
        }
        this.annotation.setAllowedTargets(bitmap);
    }
    
    protected void addError(String msg) {
        this.errorCollector.addErrorAndContinue(
          new SyntaxErrorMessage(new SyntaxException(msg 
                  + " in @" + this.annotationClass.getName() + '\n', 
                  this.annotation.getLineNumber(), 
                  this.annotation.getColumnNumber()), this.source)
        );
    }
    
    protected void addError(String msg, ASTNode expr) {
        this.errorCollector.addErrorAndContinue(
          new SyntaxErrorMessage(new SyntaxException(msg 
                  + "in @" + this.annotationClass.getName() + '\n', 
                  expr.getLineNumber(), 
                  expr.getColumnNumber()), this.source)
        );
    }
    
    private Class getAttributeType(String attr) {
        if(this.requiredAttrTypes.containsKey(attr)) {
            return (Class) this.requiredAttrTypes.remove(attr);
        }
        
        return (Class) this.defaultAttrTypes.remove(attr);
    }
    
    private Object invoke(Class clazz, String methodName, Class[] argTypes, Object target, Object[] args) {
        try {
            Method m = clazz.getMethod(methodName, argTypes);
            return m.invoke(target, args);
        }
        catch(Throwable cause) {
            // we report an error on called side
        }
     
        return null;
    }
    
    private Class loadAnnotationRootClass() {
        try {
            return Class.forName("java.lang.annotation.Annotation");
        }
        catch(Throwable cause) {
            // report the error later
        }
        
        return null;
    }
}
