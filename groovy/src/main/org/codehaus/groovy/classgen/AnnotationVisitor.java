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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.*;
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
    private static final Class[] EMPTY_ARG_TYPES = new Class[0];
    private static final Object[] EMPTY_ARGS = new Object[0];

    private final Class annotationRootClass;
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
        if(!node.getClassNode().isResolved()) {
            addError("Current type was not yet resolved. Cannot introspect it.");
            node.setValid(false);
            return node;
        }
        this.annotationClass = node.getClassNode().getTypeClass();

        extractAnnotationMeta(this.annotationClass);

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
                addError("Unknown attribute '" + attrName + "'", attrExpr);
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
        return node.getClassNode().implementsInterface(ClassHelper.Annotation_Type);
    }

    protected void visitExpression(String attrName, Expression attrExp, Class attrType) {
        if(attrType.isArray()) {
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
        }
        if(attrType.isPrimitive()) {
            visitConstantExpression(attrName, (ConstantExpression) attrExp, ClassHelper.getWrapper(ClassHelper.make(attrType)));
        } else if(String.class.equals(attrType)) {
            visitConstantExpression(attrName, (ConstantExpression) attrExp, ClassHelper.make(String.class));
        } else if(Class.class.equals(attrType)) {
            // there is nothing to check about ClassExpressions
        } else if(isEnum(attrType)) {
            if(attrExp instanceof PropertyExpression) {
                visitEnumExpression(attrName, (PropertyExpression) attrExp, ClassHelper.make(attrType));
            } else {
                addError("Value not defined for annotation attribute " + attrName, attrExp);
            }
        } else if(isAnnotation(attrType)) {
            visitAnnotationExpression(attrName, (AnnotationConstantExpression) attrExp, attrType);
        }
    }

    /**
     * @param attrName
     * @param expression
     * @param attrType
     */
    protected void visitAnnotationExpression(String attrName, AnnotationConstantExpression expression, Class attrType) {
        AnnotationNode annotationNode = (AnnotationNode) expression.getValue();
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, this.errorCollector);
        visitor.visit(annotationNode);
    }

    protected void visitListExpression(String attrName, ListExpression listExpr, Class elementType) {
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

    private boolean isAnnotation(Class clazz) {
        Boolean result = (Boolean) invoke(clazz.getClass(), "isAnnotation", EMPTY_ARG_TYPES, clazz, EMPTY_ARGS);
        return result.booleanValue();
    }

    private boolean isEnum(Class clazz) {
        Boolean result = (Boolean) invoke(clazz.getClass(), "isEnum", EMPTY_ARG_TYPES, clazz, EMPTY_ARGS);
        return result.booleanValue();
    }

    private void extractAnnotationMeta(Class annotationClass) {
        initializeAnnotationMeta(annotationClass);
        initializeAttributeTypes(annotationClass);
    }

    private void initializeAnnotationMeta(Class annotationClass) {
        Object[] annotations = (Object[]) invoke(annotationClass.getClass(),
                "getAnnotations", EMPTY_ARG_TYPES, annotationClass, EMPTY_ARGS);
        if (annotations == null) {
            addError("Cannot retrieve annotation meta information. "
                    + ExtendedVerifier.JVM_ERROR_MESSAGE);
            return;
        }

        for(int i = 0; i < annotations.length; i++) {
            Class annotationType = (Class) invoke(this.annotationRootClass,
                    "annotationType", EMPTY_ARG_TYPES, annotations[i], EMPTY_ARGS);
            if (annotationType == null) continue;

            if ("java.lang.annotation.Retention".equals(annotationType.getName())) {
                initializeRetention(annotationClass, annotationType, annotations[i]);
            }
            else if("java.lang.annotation.Target".equals(annotationType.getName())) {
                initializeTarget(annotationClass, annotationType, annotations[i]);
            }
        }
    }

    private void initializeAttributeTypes(Class annotationClass) {
        Method[] methods = annotationClass.getDeclaredMethods();
        for(int i = 0; i < methods.length; i++) {
            Object defaultValue = invoke(Method.class, "getDefaultValue", EMPTY_ARG_TYPES, methods[i], EMPTY_ARGS);
            if (defaultValue != null) {
                // by now we know JDK1.5 API is available so a null means no default value
                defaultAttrTypes.put(methods[i].getName(), methods[i].getReturnType());
            }
            else {
                requiredAttrTypes.put(methods[i].getName(), methods[i].getReturnType());
            }
        }
    }

    private void initializeRetention(Class annotationClass, Class retentionClass, Object retentionAnnotation) {
        Object retentionPolicyEnum =
            invoke(retentionClass, "value", EMPTY_ARG_TYPES, retentionAnnotation, EMPTY_ARGS);
        if (retentionPolicyEnum == null) {
            addError("Cannot read @RetentionPolicy on the @" + annotationClass.getName()
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

    private void initializeTarget(Class annotationClass, Class targetClass, Object targetAnnotation) {
        Object[] elementTypeEnum =
            (Object[]) invoke(targetClass, "value", EMPTY_ARG_TYPES, targetAnnotation, EMPTY_ARGS);
        if (elementTypeEnum == null) {
            addError("Cannot read @Target on the @" + annotationClass.getName()
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
                  + " in @" + this.annotationClass.getName() + '\n',
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
