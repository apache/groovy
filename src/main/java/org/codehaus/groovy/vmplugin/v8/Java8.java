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
package org.codehaus.groovy.vmplugin.v8;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * Java 8 based functions.
 *
 * @since 2.5.0
 */
public class Java8 implements VMPlugin {

    private static final Class<?>[] PLUGIN_DGM = {PluginDefaultGroovyMethods.class};
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    private static final Permission ACCESS_PERMISSION = new ReflectPermission("suppressAccessChecks");

    public Java8() {
        super();
    }

    public static GenericsType configureTypeVariableDefinition(ClassNode base, ClassNode[] cBounds) {
        ClassNode redirect = base.redirect();
        base.setRedirect(null);
        GenericsType gt;
        if (cBounds == null || cBounds.length == 0) {
            gt = new GenericsType(base);
        } else {
            gt = new GenericsType(base, cBounds, null);
            gt.setName(base.getName());
            gt.setPlaceholder(true);
        }
        base.setRedirect(redirect);
        return gt;
    }

    private static ClassNode configureClass(Class<?> c) {
        if (c.isPrimitive()) {
            return ClassHelper.make(c);
        } else {
            return ClassHelper.makeWithoutCaching(c, false);
        }
    }

    public static ClassNode configureTypeVariableReference(String name) {
        ClassNode cn = ClassHelper.makeWithoutCaching(name);
        cn.setGenericsPlaceHolder(true);
        ClassNode cn2 = ClassHelper.makeWithoutCaching(name);
        cn2.setGenericsPlaceHolder(true);
        GenericsType[] gts = new GenericsType[]{new GenericsType(cn2)};
        cn.setGenericsTypes(gts);
        cn.setRedirect(ClassHelper.OBJECT_TYPE);
        return cn;
    }

    private static void setRetentionPolicy(RetentionPolicy value, AnnotationNode node) {
        switch (value) {
            case RUNTIME:
                node.setRuntimeRetention(true);
                break;
            case SOURCE:
                node.setSourceRetention(true);
                break;
            case CLASS:
                node.setClassRetention(true);
                break;
            default:
                throw new GroovyBugError("unsupported Retention " + value);
        }
    }

    private static void setMethodDefaultValue(MethodNode mn, Method m) {
        ConstantExpression cExp = new ConstantExpression(m.getDefaultValue());
        mn.setCode(new ReturnStatement(cExp));
        mn.setAnnotationDefault(true);
    }

    private static Constructor<MethodHandles.Lookup> getLookupConstructor() {
        return LookupHolder.LOOKUP_Constructor;
    }

    @Override
    public Class<?>[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    @Override
    public int getVersion() {
        return 8;
    }

    protected int getElementCode(ElementType value) {
        switch (value) {
            case TYPE:
                return AnnotationNode.TYPE_TARGET;
            case CONSTRUCTOR:
                return AnnotationNode.CONSTRUCTOR_TARGET;
            case METHOD:
                return AnnotationNode.METHOD_TARGET;
            case FIELD:
                return AnnotationNode.FIELD_TARGET;
            case PARAMETER:
                return AnnotationNode.PARAMETER_TARGET;
            case LOCAL_VARIABLE:
                return AnnotationNode.LOCAL_VARIABLE_TARGET;
            case ANNOTATION_TYPE:
                return AnnotationNode.ANNOTATION_TARGET;
            case PACKAGE:
                return AnnotationNode.PACKAGE_TARGET;
            case TYPE_PARAMETER:
                return AnnotationNode.TYPE_PARAMETER_TARGET;
            case TYPE_USE:
                return AnnotationNode.TYPE_USE_TARGET;
        }
        if ("MODULE".equals(value.name())) {
            return AnnotationNode.TYPE_TARGET;
        } else {
            throw new GroovyBugError("unsupported Target " + value);
        }
    }

    protected Parameter[] processParameters(CompileUnit compileUnit, Method m) {
        java.lang.reflect.Parameter[] parameters = m.getParameters();
        Type[] types = m.getGenericParameterTypes();
        Parameter[] params = Parameter.EMPTY_ARRAY;
        if (types.length > 0) {
            params = new Parameter[types.length];
            for (int i = 0; i < params.length; i++) {
                java.lang.reflect.Parameter p = parameters[i];
                String name = p.isNamePresent() ? p.getName() : "param" + i;
                params[i] = makeParameter(compileUnit, types[i], m.getParameterTypes()[i], m.getParameterAnnotations()[i], name);
            }
        }
        return params;
    }

    public void setAdditionalClassInformation(ClassNode cn) {
        setGenericsTypes(cn);
    }

    private void setGenericsTypes(ClassNode cn) {
        TypeVariable[] tvs = cn.getTypeClass().getTypeParameters();
        GenericsType[] gts = configureTypeVariable(tvs);
        cn.setGenericsTypes(gts);
    }

    private GenericsType[] configureTypeVariable(TypeVariable[] tvs) {
        if (tvs.length == 0) return null;
        GenericsType[] gts = new GenericsType[tvs.length];
        for (int i = 0; i < tvs.length; i++) {
            gts[i] = configureTypeVariableDefinition(tvs[i]);
        }
        return gts;
    }

    private GenericsType configureTypeVariableDefinition(TypeVariable tv) {
        return configureTypeVariableDefinition(configureTypeVariableReference(tv.getName()), configureTypes(tv.getBounds()));
    }

    private ClassNode[] configureTypes(Type[] types) {
        if (types.length == 0) return null;
        ClassNode[] nodes = new ClassNode[types.length];
        for (int i = 0; i < types.length; i++) {
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
            return configureTypeVariableReference(((TypeVariable) type).getName());
        } else if (type instanceof Class) {
            return configureClass((Class<?>) type);
        } else if (type==null) {
            throw new GroovyBugError("Type is null. Most probably you let a transform reuse existing ClassNodes with generics information, that is now used in a wrong context.");
        } else {
            throw new GroovyBugError("unknown type: " + type + " := " + type.getClass());
        }
    }

    private ClassNode configureGenericArray(GenericArrayType genericArrayType) {
        Type component = genericArrayType.getGenericComponentType();
        ClassNode node = configureType(component);
        return node.makeArray();
    }

    private ClassNode configureWildcardType(final WildcardType wildcardType) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        base.setRedirect(ClassHelper.OBJECT_TYPE);

        ClassNode[] lowers = configureTypes(wildcardType.getLowerBounds());
        ClassNode[] uppers = configureTypes(wildcardType.getUpperBounds());
        // beware of [Object] upper bounds; often it's <?> or <? super T>
        if (lowers != null || wildcardType.getTypeName().equals("?")) {
            uppers = null;
        }

        GenericsType gt = new GenericsType(base, uppers, lowers != null ? lowers[0] : null);
        gt.setWildcard(true);

        ClassNode wt = ClassHelper.makeWithoutCaching(Object.class, false);
        wt.setGenericsTypes(new GenericsType[]{gt});
        return wt;
    }

    private ClassNode configureParameterizedType(ParameterizedType parameterizedType) {
        ClassNode base = configureType(parameterizedType.getRawType());
        GenericsType[] gts = configureTypeArguments(parameterizedType.getActualTypeArguments());
        base.setGenericsTypes(gts);
        return base;
    }

    private GenericsType[] configureTypeArguments(Type[] ta) {
        if (ta.length == 0) return null;
        GenericsType[] gts = new GenericsType[ta.length];
        for (int i = 0; i < ta.length; i++) {
            ClassNode t = configureType(ta[i]);
            if (ta[i] instanceof WildcardType) {
                GenericsType[] gen = t.getGenericsTypes();
                gts[i] = gen[0];
            } else {
                gts[i] = new GenericsType(t);
            }
        }
        return gts;
    }

    public Class<?>[] getPluginStaticGroovyMethods() {
        return EMPTY_CLASS_ARRAY;
    }

    private void setAnnotationMetaData(Annotation[] annotations, AnnotatedNode an) {
        for (Annotation annotation : annotations) {
            AnnotationNode node = new AnnotationNode(ClassHelper.make(annotation.annotationType()));
            configureAnnotation(node, annotation);
            an.addAnnotation(node);
        }
    }

    @Deprecated
    private void configureAnnotationFromDefinition(AnnotationNode definition, AnnotationNode root) {
        VMPlugin plugin = VMPluginFactory.getPlugin();
        plugin.configureAnnotationNodeFromDefinition(definition, root);
    }

    public void configureAnnotationNodeFromDefinition(AnnotationNode definition, AnnotationNode root) {
        ClassNode type = definition.getClassNode();
        if ("java.lang.annotation.Retention".equals(type.getName())) {
            Expression exp = definition.getMember("value");
            if (!(exp instanceof PropertyExpression)) return;
            PropertyExpression pe = (PropertyExpression) exp;
            String name = pe.getPropertyAsString();
            RetentionPolicy policy = RetentionPolicy.valueOf(name);
            setRetentionPolicy(policy, root);
        } else if ("java.lang.annotation.Target".equals(type.getName())) {
            Expression exp = definition.getMember("value");
            if (!(exp instanceof ListExpression)) return;
            ListExpression le = (ListExpression) exp;
            int bitmap = 0;
            for (Expression e : le.getExpressions()) {
                if (!(e instanceof PropertyExpression)) return;
                PropertyExpression element = (PropertyExpression) e;
                String name = element.getPropertyAsString();
                ElementType value = ElementType.valueOf(name);
                bitmap |= getElementCode(value);
            }
            root.setAllowedTargets(bitmap);
        }
    }

    public void configureAnnotation(AnnotationNode node) {
        ClassNode type = node.getClassNode();
        VMPlugin plugin = VMPluginFactory.getPlugin();
        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode an : annotations) {
            plugin.configureAnnotationNodeFromDefinition(an, node);
        }
        if (!node.getClassNode().getName().equals("java.lang.annotation.Retention")) {
            plugin.configureAnnotationNodeFromDefinition(node, node);
        }
    }

    private void configureAnnotation(AnnotationNode node, Annotation annotation) {
        Class<?> type = annotation.annotationType();
        if (type == Retention.class) {
            Retention r = (Retention) annotation;
            RetentionPolicy value = r.value();
            setRetentionPolicy(value, node);
            node.setMember("value", new PropertyExpression(
                    new ClassExpression(ClassHelper.makeWithoutCaching(RetentionPolicy.class, false)),
                    value.toString()));
        } else if (type == Target.class) {
            Target t = (Target) annotation;
            ElementType[] elements = t.value();
            ListExpression elementExprs = new ListExpression();
            for (ElementType element : elements) {
                elementExprs.addExpression(new PropertyExpression(
                        new ClassExpression(ClassHelper.ELEMENT_TYPE_TYPE), element.name()));
            }
            node.setMember("value", elementExprs);
        } else {
            Method[] declaredMethods;
            try {
                declaredMethods = type.getDeclaredMethods();
            } catch (SecurityException se) {
                declaredMethods = EMPTY_METHOD_ARRAY;
            }
            for (Method declaredMethod : declaredMethods) {
                try {
                    Object value = declaredMethod.invoke(annotation);
                    Expression valueExpression = annotationValueToExpression(value);
                    if (valueExpression == null)
                        continue;
                    node.setMember(declaredMethod.getName(), valueExpression);
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                }
            }
        }
    }

    private Expression annotationValueToExpression (Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Character || value instanceof Boolean)
            return new ConstantExpression(value);

        if (value instanceof Class)
            return new ClassExpression(ClassHelper.makeWithoutCaching((Class<?>)value));

        if (value.getClass().isArray()) {
            ListExpression elementExprs = new ListExpression();
            int len = Array.getLength(value);
            for (int i = 0; i != len; ++i)
                elementExprs.addExpression(annotationValueToExpression(Array.get(value, i)));
            return elementExprs;
        }

        return null;
    }

    public void configureClassNode(CompileUnit compileUnit, ClassNode classNode) {
        try {
            Class<?> clazz = classNode.getTypeClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                ClassNode ret = makeClassNode(compileUnit, f.getGenericType(), f.getType());
                FieldNode fn = new FieldNode(f.getName(), f.getModifiers(), ret, classNode, null);
                setAnnotationMetaData(f.getAnnotations(), fn);
                classNode.addField(fn);
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                ClassNode ret = makeClassNode(compileUnit, m.getGenericReturnType(), m.getReturnType());
                Parameter[] params = processParameters(compileUnit, m);
                ClassNode[] exceptions = makeClassNodes(compileUnit, m.getGenericExceptionTypes(), m.getExceptionTypes());
                MethodNode mn = new MethodNode(m.getName(), m.getModifiers(), ret, params, exceptions, null);
                mn.setSynthetic(m.isSynthetic());
                setMethodDefaultValue(mn, m);
                setAnnotationMetaData(m.getAnnotations(), mn);
                mn.setGenericsTypes(configureTypeVariable(m.getTypeParameters()));
                classNode.addMethod(mn);
            }
            Constructor[] constructors = clazz.getDeclaredConstructors();
            for (Constructor ctor : constructors) {
                Type[] types = ctor.getGenericParameterTypes();
                Parameter[] params1 = Parameter.EMPTY_ARRAY;
                if (types.length > 0) {
                    params1 = new Parameter[types.length];
                    for (int i = 0; i < params1.length; i++) {
                        params1[i] = makeParameter(compileUnit, types[i], ctor.getParameterTypes()[i], getConstructorParameterAnnotations(ctor)[i], "param" + i);
                    }
                }
                Parameter[] params = params1;
                ClassNode[] exceptions = makeClassNodes(compileUnit, ctor.getGenericExceptionTypes(), ctor.getExceptionTypes());
                ConstructorNode cn = classNode.addConstructor(ctor.getModifiers(), params, exceptions, null);
                setAnnotationMetaData(ctor.getAnnotations(), cn);
            }

            Class<?> sc = clazz.getSuperclass();
            if (sc != null) classNode.setUnresolvedSuperClass(makeClassNode(compileUnit, clazz.getGenericSuperclass(), sc));
            makeInterfaceTypes(compileUnit, classNode, clazz);
            setAnnotationMetaData(clazz.getAnnotations(), classNode);

            PackageNode packageNode = classNode.getPackage();
            if (packageNode != null) {
                setAnnotationMetaData(clazz.getPackage().getAnnotations(), packageNode);
            }
        } catch (NoClassDefFoundError e) {
            throw new NoClassDefFoundError("Unable to load class "+classNode.toString(false)+" due to missing dependency "+e.getMessage());
        } catch (MalformedParameterizedTypeException e) {
            throw new RuntimeException("Unable to configure class node for class "+classNode.toString(false)+" due to malformed parameterized types", e);
        }
    }

    /**
     * Synthetic parameters such as those added for inner class constructors may
     * not be included in the parameter annotations array. This is the case when
     * at least one parameter of an inner class constructor has an annotation with
     * a RUNTIME retention (this occurs for JDK8 and below). This method will
     * normalize the annotations array so that it contains the same number of
     * elements as the array returned from {@link Constructor#getParameterTypes()}.
     *
     * If adjustment is required, the adjusted array will be prepended with a
     * zero-length element. If no adjustment is required, the original array
     * from {@link Constructor#getParameterAnnotations()} will be returned.
     *
     * @param constructor the Constructor for which to return parameter annotations
     * @return array of arrays containing the annotations on the parameters of the given Constructor
     */
    private Annotation[][] getConstructorParameterAnnotations(Constructor<?> constructor) {
        /*
         * TODO: Remove after JDK9 is the minimum JDK supported
         *
         * JDK9+ correctly accounts for the synthetic parameter and when it becomes
         * the minimum version this method should no longer be required.
         */
        int parameterCount = constructor.getParameterTypes().length;
        Annotation[][] annotations = constructor.getParameterAnnotations();
        int diff = parameterCount - annotations.length;
        if (diff > 0) {
            // May happen on JDK8 and below. We add elements to the front of the array to account for the synthetic params:
            // - for an inner class we expect one param to account for the synthetic outer reference
            // - for an enum we expect two params to account for the synthetic name and ordinal
            if ((!constructor.getDeclaringClass().isEnum() && diff > 1) || diff > 2) {
                throw new GroovyBugError(
                        "Constructor parameter annotations length [" + annotations.length + "] " +
                        "does not match the parameter length: " + constructor
                );
            }
            Annotation[][] adjusted = new Annotation[parameterCount][];
            for (int i = 0; i < diff; i++) {
                adjusted[i] = EMPTY_ANNOTATION_ARRAY;
            }
            System.arraycopy(annotations, 0, adjusted, diff, annotations.length);
            return adjusted;
        }
        return annotations;
    }

    private void makeInterfaceTypes(CompileUnit cu, ClassNode classNode, Class<?> clazz) {
        Type[] interfaceTypes = clazz.getGenericInterfaces();
        if (interfaceTypes.length == 0) {
            classNode.setInterfaces(ClassNode.EMPTY_ARRAY);
        } else {
            ClassNode[] ret = new ClassNode[interfaceTypes.length];
            for (int i = 0; i < interfaceTypes.length; i++) {
                Type type = interfaceTypes[i];
                while (!(type instanceof Class)) {
                    ParameterizedType pt = (ParameterizedType) type;
                    Type t2 = pt.getRawType();
                    if (t2==type) {
                        throw new GroovyBugError("Cannot transform generic signature of "+clazz+
                                " with generic interface "+interfaceTypes[i]+" to a class.");
                    }
                    type = t2;
                }
                ret[i] = makeClassNode(cu, interfaceTypes[i], (Class<?>) type);
            }
            classNode.setInterfaces(ret);
        }
    }

    private ClassNode[] makeClassNodes(CompileUnit cu, Type[] types, Class<?>[] cls) {
        ClassNode[] nodes = new ClassNode[types.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = makeClassNode(cu, types[i], cls[i]);
        }
        return nodes;
    }

    private ClassNode makeClassNode(CompileUnit cu, Type t, Class<?> c) {
        ClassNode back = null;
        if (cu != null) back = cu.getClass(c.getName());
        if (back == null) back = ClassHelper.make(c);
        if (!(t instanceof Class)) {
            ClassNode front = configureType(t);
            front.setRedirect(back);
            return front;
        }
        return back.getPlainNodeReference();
    }

    protected Parameter makeParameter(CompileUnit cu, Type type, Class<?> cl, Annotation[] annotations, String name) {
        ClassNode cn = makeClassNode(cu, type, cl);
        Parameter parameter = new Parameter(cn, name);
        setAnnotationMetaData(annotations, parameter);
        return parameter;
    }

    /**
     * The following scenarios can not set accessible, i.e. the return value is false
     * 1) SecurityException occurred
     * 2) the accessible object is a Constructor object for the Class class
     *
     * @param accessibleObject the accessible object to check
     * @param callerClass the callerClass to invoke {@code setAccessible}
     * @return the check result
     */
    @Override
    public boolean checkCanSetAccessible(AccessibleObject accessibleObject, Class<?> callerClass) {
        SecurityManager sm = System.getSecurityManager();
        try {
            if (sm != null) {
                sm.checkPermission(ACCESS_PERMISSION);
            }
        } catch (SecurityException e) {
            return false;
        }

        if (accessibleObject instanceof Constructor) {
            Constructor c = (Constructor) accessibleObject;
            if (c.getDeclaringClass() == Class.class) {
                return false; // Cannot make a java.lang.Class constructor accessible
            }
        }

        return true;
    }

    @Override
    public boolean checkAccessible(Class<?> callerClass, Class<?> declaringClass, int memberModifiers, boolean allowIllegalAccess) {
        return true;
    }

    @Override
    public boolean trySetAccessible(AccessibleObject ao) {
        try {
            ao.setAccessible(true);
            return true;
        } catch (SecurityException e) {
            throw e;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public MetaMethod transformMetaMethod(MetaClass metaClass, MetaMethod metaMethod, Class<?> caller) {
        return metaMethod;
    }

    @Override
    public MetaMethod transformMetaMethod(MetaClass metaClass, MetaMethod metaMethod) {
        return transformMetaMethod(metaClass, metaMethod, null);
    }

    @Override
    public void invalidateCallSites() {
        IndyInterface.invalidateSwitchPoints();
    }

    @Override
    public Object getInvokeSpecialHandle(Method method, Object receiver) {
        final Class<?> receiverType = receiver.getClass();
        try {
            return of(receiverType).unreflectSpecial(method, receiverType).bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            return getInvokeSpecialHandleFallback(method, receiver);
        }
    }

    private Object getInvokeSpecialHandleFallback(Method method, Object receiver) {
        if (getLookupConstructor() == null) {
            throw new GroovyBugError("getInvokeSpecialHandle requires at least JDK 7 for private access to Lookup");
        }
        if (!method.isAccessible()) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                ReflectionUtils.trySetAccessible(method);
                return null;
            });
        }
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            return getLookupConstructor().newInstance(declaringClass, -1).
                    unreflectSpecial(method, declaringClass).
                    bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
    }

    @Override
    public Object invokeHandle(Object handle, Object[] args) throws Throwable {
        MethodHandle mh = (MethodHandle) handle;
        return mh.invokeWithArguments(args);
    }

    public static MethodHandles.Lookup of(final Class<?> declaringClass) {
        try {
            return getLookupConstructor().newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).in(declaringClass);
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvocationTargetException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    private static class LookupHolder {
        private static final Constructor<MethodHandles.Lookup> LOOKUP_Constructor;

        static {
            Constructor<MethodHandles.Lookup> lookup;
            try {
                lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
            } catch (final NoSuchMethodException ex) {
                throw new IllegalStateException("Incompatible JVM", ex);
            }
            try {
                if (!lookup.isAccessible()) {
                    final Constructor tmp = lookup;
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        ReflectionUtils.trySetAccessible(tmp);
                        return null;
                    });
                }
            } catch (SecurityException ignore) {
                lookup = null;
            } catch (RuntimeException re) {
                throw re;
            }
            LOOKUP_Constructor = lookup;
        }
    }
}
