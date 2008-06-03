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

package org.codehaus.groovy.vmplugin.v5;

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.ClassHelper;

/**
 * java 5 based functions
 * @author Jochen Theodorou
 *
 */
public class Java5 implements VMPlugin {
    private static Class[] PLUGIN_DGM={PluginDefaultGroovyMethods.class};

    public void setAdditionalClassInformation(ClassNode cn) {
        setGenericsTypes(cn);
    }

    private void setGenericsTypes(ClassNode cn) {
        TypeVariable[] tvs = cn.getTypeClass().getTypeParameters();
        GenericsType[] gts = configureTypeVariable(tvs);
        cn.setGenericsTypes(gts);
    }

    private GenericsType[] configureTypeVariable(TypeVariable[] tvs) {
        if (tvs.length==0) return null;
        GenericsType[] gts = new GenericsType[tvs.length];
        for (int i = 0; i < tvs.length; i++) {
            gts[i] = configureTypeVariableDefintion(tvs[i]);
        }
        return gts;
    }

    private GenericsType configureTypeVariableDefintion(TypeVariable tv) {
       ClassNode base = configureTypeVariableReference(tv);
       ClassNode redirect = base.redirect();
       base.setRedirect(null);
       Type[] tBounds = tv.getBounds();
       GenericsType gt;
       if (tBounds.length==0) {
           gt = new GenericsType(base);
       } else {
           ClassNode[] cBounds = configureTypes(tBounds);
           gt = new GenericsType(base,cBounds,null);
           gt.setName(base.getName());
           gt.setPlaceholder(true);
       }
       base.setRedirect(redirect);
       return gt;
    }

    private ClassNode[] configureTypes(Type[] types){
        if (types.length==0) return null;
        ClassNode[] nodes = new ClassNode[types.length];
        for (int i=0; i<types.length; i++){
            nodes[i] = configureType(types[i]);
        }
        return nodes;
    }

    private ClassNode configureType(Type type) {
        if (type instanceof WildcardType) {
            return configureWildcardType((WildcardType) type);
        } else if (type instanceof ParameterizedType) {
            return configureParameterizedType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            return configureGenericArray((GenericArrayType) type);
        } else if (type instanceof TypeVariable) {
            return configureTypeVariableReference((TypeVariable) type);
        } else if (type instanceof Class) {
            return configureClass((Class) type);
        } else {
            throw new GroovyBugError("unknown type: " + type + " := " + type.getClass());
        }
    }

    private ClassNode configureClass(Class c){
        if (c.isPrimitive()) {
            return ClassHelper.make(c);
        } else {
            return ClassHelper.makeWithoutCaching(c, false);
        }
    }

    private ClassNode configureGenericArray(GenericArrayType genericArrayType) {
        Type component = genericArrayType.getGenericComponentType();
        ClassNode node = configureType(component);
        return node.makeArray();
    }

    private ClassNode configureWildcardType(WildcardType wildcardType) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        //TODO: more than one lower bound for wildcards?
        ClassNode[] lowers = configureTypes(wildcardType.getLowerBounds());
        ClassNode lower=null;
        if (lower!=null) lower = lowers[0];

        ClassNode[] upper = configureTypes(wildcardType.getUpperBounds());
        GenericsType t = new GenericsType(base,upper,lower);
        t.setWildcard(true);

        ClassNode ref = ClassHelper.makeWithoutCaching(Object.class,false);
        ref.setGenericsTypes(new GenericsType[]{t});

        return ref;
    }

    private ClassNode configureParameterizedType(ParameterizedType parameterizedType) {
        ClassNode base = configureType(parameterizedType.getRawType());
        GenericsType[] gts = configureTypeArguments(parameterizedType.getActualTypeArguments());
        base.setGenericsTypes(gts);
        return base;
    }

    private ClassNode configureTypeVariableReference(TypeVariable tv) {
        ClassNode cn = ClassHelper.makeWithoutCaching(tv.getName());
        cn.setGenericsPlaceHolder(true);
        ClassNode cn2 = ClassHelper.makeWithoutCaching(tv.getName());
        GenericsType[] gts = new GenericsType[]{new GenericsType(cn2)};
        cn.setGenericsTypes(gts);
        cn.setRedirect(ClassHelper.OBJECT_TYPE);
        return cn;
    }

    private GenericsType[] configureTypeArguments(Type[] ta) {
        if (ta.length==0) return null;
        GenericsType[] gts = new GenericsType[ta.length];
        for (int i = 0; i < ta.length; i++) {
            gts[i] = new GenericsType(configureType(ta[i]));
        }
        return gts;
    }

    public Class[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    private void setAnnotationMetaData(ClassNode cn) {
        Annotation[] annotations =  cn.getTypeClass().getAnnotations();
        for (int i=0; i<annotations.length; i++) {
            Annotation annotation = annotations[i];
            AnnotationNode node = new AnnotationNode(ClassHelper.make(annotation.annotationType()));
            configureAnnotation(node,annotation);
            cn.addAnnotation(node);
        }
    }

    private void configureAnnotationFromDefinition(AnnotationNode definition, AnnotationNode root) {
        ClassNode type = definition.getClassNode();
        if (!type.isResolved()) return;
        Class clazz = type.getTypeClass();
        if (clazz==Retention.class) {
            Expression exp = definition.getMember("value");
            if (!(exp instanceof PropertyExpression)) return;
            PropertyExpression pe = (PropertyExpression) exp;
            String name = pe.getPropertyAsString();
            RetentionPolicy policy = RetentionPolicy.valueOf(name);
            setRetentionPolicy(policy,root);
        } else if (clazz==Target.class) {
            Expression exp = definition.getMember("value");
            if (!(exp instanceof ListExpression)) return;
            ListExpression le = (ListExpression) exp;
            int bitmap = 0;
            for (Iterator it=le.getExpressions().iterator(); it.hasNext();) {
                PropertyExpression element = (PropertyExpression) it.next();
                String name = element.getPropertyAsString();
                ElementType value = ElementType.valueOf(name);
                bitmap |= getElementCode(value);
            }
            root.setAllowedTargets(bitmap);
        }
    }

    public void configureAnnotation(AnnotationNode node) {
        ClassNode type = node.getClassNode();
        List annotations = type.getAnnotations();
        for (Iterator it=annotations.iterator(); it.hasNext();) {
            AnnotationNode an = (AnnotationNode) it.next();
            configureAnnotationFromDefinition(an,node);
        }

        configureAnnotationFromDefinition(node,node);
    }

    private void configureAnnotation(AnnotationNode node, Annotation annotation) {
        Class type = annotation.annotationType();
        if (type == Retention.class) {
            Retention r = (Retention) annotation;
            RetentionPolicy value = r.value();
            setRetentionPolicy(value,node);
            node.setMember("value",new PropertyExpression(
                    new ClassExpression(ClassHelper.makeWithoutCaching(RetentionPolicy.class,false)),
                    value.toString()));
        } else if (type == Target.class) {
            Target t = (Target) annotation;
            ElementType[] elements = t.value();
            int bitmap = 0;
            for (int i=0; i<elements.length; i++) {
                bitmap |= getElementCode(elements[i]);
            }
            node.setAllowedTargets(bitmap);
        }
    }

    private void setRetentionPolicy(RetentionPolicy value,  AnnotationNode node) {
        switch (value) {
          case RUNTIME: node.setRuntimeRetention(true); break;
          case SOURCE:  node.setSourceRetention(true); break;
          case CLASS:   node.setClassRetention(true); break;
          default: throw new GroovyBugError("unsupported Retention "+value);
        }
    }

    private int getElementCode(ElementType value) {
        switch (value) {
            case TYPE:            return AnnotationNode.TYPE_TARGET;
            case CONSTRUCTOR:     return AnnotationNode.CONSTRUCTOR_TARGET;
            case METHOD:          return AnnotationNode.METHOD_TARGET;
            case FIELD:           return AnnotationNode.FIELD_TARGET;
            case PARAMETER:       return AnnotationNode.PARAMETER_TARGET;
            case LOCAL_VARIABLE:  return AnnotationNode.LOCAL_VARIABLE_TARGET;
            case ANNOTATION_TYPE: return AnnotationNode.ANNOTATION_TARGET;
            case PACKAGE:         return AnnotationNode.PACKAGE_TARGET;
            default: throw new GroovyBugError("unsupported Target " + value);
        }
    }

    private void setMethodDefaultValue(MethodNode mn, Method m) {
        Object defaultValue = m.getDefaultValue();
        mn.setCode(new ReturnStatement(new ConstantExpression(defaultValue)));
        mn.setAnnotationDefault(true);
    }

    public void configureClassNode(CompileUnit compileUnit, ClassNode classNode) {
        Class clazz = classNode.getTypeClass();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            ClassNode ret = makeClassNode(compileUnit,f.getGenericType(),f.getType());
            classNode.addField(fields[i].getName(), fields[i].getModifiers(), ret, null);
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            ClassNode ret = makeClassNode(compileUnit,m.getGenericReturnType(),m.getReturnType());
            Parameter[] params = makeParameters(compileUnit,m.getGenericParameterTypes(),m.getParameterTypes());
            ClassNode[] exceptions = makeClassNodes(compileUnit,m.getGenericExceptionTypes(),m.getExceptionTypes());
            MethodNode mn = new MethodNode(m.getName(), m.getModifiers(), ret, params, exceptions, null);
            setMethodDefaultValue(mn,m);
            classNode.addMethod(mn);
        }
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Constructor ctor = constructors[i];
            Parameter[] params = makeParameters(compileUnit,ctor.getGenericParameterTypes(), ctor.getParameterTypes());
            ClassNode[] exceptions = makeClassNodes(compileUnit,ctor.getGenericExceptionTypes(),ctor.getExceptionTypes());
            classNode.addConstructor(ctor.getModifiers(), params, exceptions, null);
        }

        Class sc = clazz.getSuperclass();
        if (sc != null) classNode.setUnresolvedSuperClass(makeClassNode(compileUnit,clazz.getGenericSuperclass(),sc));
        makeInterfaceTypes(compileUnit,classNode,clazz);
        setAnnotationMetaData(classNode);

    }

    private void makeInterfaceTypes(CompileUnit cu, ClassNode classNode, Class clazz) {
        Type[] interfaceTypes = clazz.getGenericInterfaces();
        if (interfaceTypes.length==0) {
            classNode.setInterfaces(ClassNode.EMPTY_ARRAY);
        } else {
            Class[] interfaceClasses = clazz.getInterfaces();
            ClassNode[] ret = new ClassNode[interfaceTypes.length];
            for (int i=0;i<interfaceTypes.length;i++){
                ret[i] = makeClassNode(cu, interfaceTypes[i], interfaceClasses[i]);
            }
            classNode.setInterfaces(ret);
        }
    }

    private ClassNode[] makeClassNodes(CompileUnit cu, Type[] types, Class[] cls) {
        ClassNode[] nodes = new ClassNode[types.length];
        for (int i=0;i<nodes.length;i++) {
            nodes[i] = makeClassNode(cu, types[i],cls[i]);
        }
        return nodes;
    }

    private ClassNode makeClassNode(CompileUnit cu, Type t, Class c) {
        ClassNode back = null;
        if (cu!=null)   back = cu.getClass(c.getName());
        if (back==null) back = ClassHelper.make(c);
        if (!(t instanceof Class)) {
            ClassNode front = configureType(t);
            front.setRedirect(back);
            return front;
        }
        return back;
    }

    private Parameter[] makeParameters(CompileUnit cu, Type[] types, Class[] cls) {
        Parameter[] params = Parameter.EMPTY_ARRAY;
        if (types.length>0) {
            params = new Parameter[types.length];
            for (int i=0;i<params.length;i++) {
                params[i] = makeParameter(cu,types[i],cls[i],i);
            }
        }
        return params;
    }

    private Parameter makeParameter(CompileUnit cu, Type type, Class cl,int idx) {
        ClassNode cn = makeClassNode(cu,type,cl);
        return new Parameter(cn, "param" + idx);
    }
}

