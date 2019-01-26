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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GeneratedGroovyProxy;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.transform.Trait;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A proxy generator responsible for mapping a map of closures to a class implementing a list of interfaces. For
 * example, the following code:
 * <pre>
 *     abstract class Foo {
 *         abstract void bar();
 *         abstract void baz();
 *     }
 *     def dyn = [bar: { println 'hello' }, baz: { println 'world'}] as Foo
 * </pre>
 * will generate a proxy class which extends class <i>Foo</i> and delegates method calls to the provided closures.
 *
 * The generated proxy implements the {@link GroovyObject} interface.
 *
 * Additionally, this proxy generator supports delegation to another object. In that case, if a method is defined
 * both in the closure map and the delegate, the version from the map is preferred. This allows overriding methods
 * from delegates with ease.
 *
 * Internally, the proxy generator makes use of ASM to generate bytecode, for improved performance as compared
 * to the legacy proxy generation mechanism which made use of string templates.
 *
 * @since 2.0.0
 */
public class ProxyGeneratorAdapter extends ClassVisitor implements Opcodes {
    private static final Map<String, Boolean> EMPTY_DELEGATECLOSURE_MAP = Collections.emptyMap();
    private static final Set<String> EMPTY_STRING_SET = Collections.emptySet();

    private static final String CLOSURES_MAP_FIELD = "$closures$delegate$map";
    private static final String DELEGATE_OBJECT_FIELD = "$delegate";
    private static List<Method> OBJECT_METHODS = getInheritedMethods(Object.class, new ArrayList<>());
    private static List<Method> GROOVYOBJECT_METHODS = getInheritedMethods(GroovyObject.class, new ArrayList<>());

    private static final AtomicLong pxyCounter = new AtomicLong();
    private static final Set<String> GROOVYOBJECT_METHOD_NAMESS;
    private static final Object[] EMPTY_ARGS = new Object[0];

    static {
        List<String> names = new ArrayList<>();
        for (Method method : GroovyObject.class.getMethods()) {
            names.add(method.getName());
        }
        GROOVYOBJECT_METHOD_NAMESS = new HashSet<>(names);
    }

    private final Class superClass;
    private final Class delegateClass;
    private final InnerLoader loader;
    private final String proxyName;
    private final LinkedHashSet<Class> classList;
    private final Map<String, Boolean> delegatedClosures;

    // if emptyBody == true, then we generate an empty body instead throwing error on unimplemented methods
    private final boolean emptyBody;
    private final boolean hasWildcard;
    private final boolean generateDelegateField;
    private final Set<String> objectDelegateMethods;

    private final Set<Object> visitedMethods;

    // cached class
    private final Class cachedClass;
    private final Constructor cachedNoArgConstructor;

    /**
     * Construct a proxy generator. This generator is used when we need to create a proxy object for a class or an
     * interface given a map of closures.
     *
     * @param closureMap    the delegates implementations
     * @param superClass    corresponding to the superclass class visitor
     * @param interfaces    extra interfaces the proxy should implement
     * @param proxyLoader   the class loader which should be used to load the generated proxy
     * @param delegateClass if not null, generate a delegate field with the corresponding class
     * @param emptyBody     if set to true, the unimplemented abstract methods will receive an empty body instead of
     *                      throwing an {@link UnsupportedOperationException}.
     */
    public ProxyGeneratorAdapter(
            final Map<Object, Object> closureMap,
            final Class superClass,
            final Class[] interfaces,
            final ClassLoader proxyLoader,
            final boolean emptyBody,
            final Class delegateClass) {
        super(CompilerConfiguration.ASM_API_VERSION, new ClassWriter(0));
        this.loader = proxyLoader != null ? createInnerLoader(proxyLoader, interfaces) : findClassLoader(superClass, interfaces);
        this.visitedMethods = new LinkedHashSet<>();
        this.delegatedClosures = closureMap.isEmpty() ? EMPTY_DELEGATECLOSURE_MAP : new HashMap<>();
        boolean wildcard = false;
        for (Map.Entry<Object, Object> entry : closureMap.entrySet()) {
            String name = entry.getKey().toString();
            if ("*".equals(name)) {
                wildcard = true;
            }
            this.delegatedClosures.put(name, Boolean.FALSE);
        }
        this.hasWildcard = wildcard;

        Class fixedSuperClass = adjustSuperClass(superClass, interfaces);
        // if we have to delegate to another object, generate the appropriate delegate field
        // and collect the name of the methods for which delegation is active
        this.generateDelegateField = delegateClass != null;
        this.objectDelegateMethods = generateDelegateField ? createDelegateMethodList(fixedSuperClass, delegateClass, interfaces) : EMPTY_STRING_SET;
        this.delegateClass = delegateClass;

        // a proxy is supposed to be a concrete class, so it cannot extend an interface.
        // If the provided superclass is an interface, then we replace the superclass with Object
        // and add this interface to the list of implemented interfaces
        this.superClass = fixedSuperClass;

        // create the base list of classes which have possible methods to be overloaded
        this.classList = new LinkedHashSet<>();
        this.classList.add(superClass);
        if (generateDelegateField) {
            classList.add(delegateClass);
            Collections.addAll(this.classList, delegateClass.getInterfaces());
        }
        if (interfaces != null) {
            Collections.addAll(this.classList, interfaces);
        }
        this.proxyName = proxyName();
        this.emptyBody = emptyBody;

        // generate bytecode
        ClassWriter writer = (ClassWriter) cv;
        this.visit(Opcodes.V1_5, ACC_PUBLIC, proxyName, null, null, null);
        byte[] b = writer.toByteArray();
//        CheckClassAdapter.verify(new ClassReader(b), true, new PrintWriter(System.err));
        cachedClass = loader.defineClass(proxyName.replace('/', '.'), b);
        // cache no-arg constructor
        Class[] args = generateDelegateField ? new Class[]{Map.class, delegateClass} : new Class[]{Map.class};
        Constructor constructor;
        try {
            constructor = cachedClass.getConstructor(args);
        } catch (NoSuchMethodException e) {
            constructor = null;
        }
        cachedNoArgConstructor = constructor;
    }

    private Class adjustSuperClass(Class superClass, final Class[] interfaces) {
        boolean isSuperClassAnInterface = superClass.isInterface();
        if (!isSuperClassAnInterface) {
            return superClass;
        }
        Class result = Object.class;
        Set<ClassNode> traits = new LinkedHashSet<>();
        // check if it's a trait
        collectTraits(superClass, traits);
        if (interfaces != null) {
            for (Class anInterface : interfaces) {
                collectTraits(anInterface, traits);
            }
        }
        if (!traits.isEmpty()) {
            String name = superClass.getName() + "$TraitAdapter";
            ClassNode cn = new ClassNode(name, ACC_PUBLIC | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE, traits.toArray(ClassNode.EMPTY_ARRAY), null);
            CompilationUnit cu = new CompilationUnit(loader);
            CompilerConfiguration config = new CompilerConfiguration();
            SourceUnit su = new SourceUnit(name + "wrapper", "", config, loader, new ErrorCollector(config));
            cu.addSource(su);
            cu.compile(Phases.CONVERSION);
            su.getAST().addClass(cn);
            cu.compile(Phases.CLASS_GENERATION);
            @SuppressWarnings("unchecked")
            List<GroovyClass> classes = (List<GroovyClass>) cu.getClasses();
            for (GroovyClass groovyClass : classes) {
                if (groovyClass.getName().equals(name)) {
                    return loader.defineClass(name, groovyClass.getBytes());
                }
            }
        }
        return result;
    }

    private static void collectTraits(final Class clazz, final Set<ClassNode> traits) {
        Annotation annotation = clazz.getAnnotation(Trait.class);
        if (annotation != null) {
            ClassNode trait = ClassHelper.make(clazz);
            traits.add(trait.getPlainNodeReference());
            LinkedHashSet<ClassNode> selfTypes = new LinkedHashSet<>();
            Traits.collectSelfTypes(trait, selfTypes, true, true);
            for (ClassNode selfType : selfTypes) {
                if (Traits.isTrait(selfType)) {
                    traits.add(selfType.getPlainNodeReference());
                }
            }
        }
    }

    private static InnerLoader createInnerLoader(final ClassLoader parent, final Class[] interfaces) {
        return AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public InnerLoader run() {
                return new InnerLoader(parent, interfaces);
            }
        });
    }

    private InnerLoader findClassLoader(Class clazz, Class[] interfaces) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl == null) cl = this.getClass().getClassLoader();
        return createInnerLoader(cl, interfaces);
    }

    private static Set<String> createDelegateMethodList(Class superClass, Class delegateClass, Class[] interfaces) {
        Set<String> selectedMethods = new HashSet<>();
        List<Method> interfaceMethods = new ArrayList<>();
        List<Method> superClassMethods = new ArrayList<>();
        Collections.addAll(superClassMethods, superClass.getDeclaredMethods());
        if (interfaces != null) {
            for (Class thisInterface : interfaces) {
                getInheritedMethods(thisInterface, interfaceMethods);
            }
            for (Method method : interfaceMethods) {
                if (!(containsEquivalentMethod(superClassMethods, method))) {
                    selectedMethods.add(method.getName() + Type.getMethodDescriptor(method));
                }
            }
        }
        List<Method> additionalMethods = getInheritedMethods(delegateClass, new ArrayList<>());
        for (Method method : additionalMethods) {
            if (method.getName().indexOf('$') != -1)
                continue;
            if (!containsEquivalentMethod(interfaceMethods, method) &&
                    !containsEquivalentMethod(OBJECT_METHODS, method) &&
                    !containsEquivalentMethod(GROOVYOBJECT_METHODS, method)) {
                selectedMethods.add(method.getName() + Type.getMethodDescriptor(method));
            }
        }
        return selectedMethods;
    }

    private static List<Method> getInheritedMethods(Class baseClass, List<Method> methods) {
        Collections.addAll(methods, baseClass.getMethods());
        Class currentClass = baseClass;
        while (currentClass != null) {
            Method[] protectedMethods = currentClass.getDeclaredMethods();
            for (Method method : protectedMethods) {
                if (method.getName().indexOf('$') != -1)
                    continue;
                if (Modifier.isProtected(method.getModifiers()) && !containsEquivalentMethod(methods, method))
                    methods.add(method);
            }
            currentClass = currentClass.getSuperclass();
        }
        return methods;
    }

    private static boolean containsEquivalentMethod(Collection<Method> publicAndProtectedMethods, Method candidate) {
        for (Method method : publicAndProtectedMethods) {
            if (candidate.getName().equals(method.getName()) &&
                    candidate.getReturnType().equals(method.getReturnType()) &&
                    hasMatchingParameterTypes(candidate, method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMatchingParameterTypes(Method method, Method candidate) {
        Class[] candidateParamTypes = candidate.getParameterTypes();
        Class[] methodParamTypes = method.getParameterTypes();
        if (candidateParamTypes.length != methodParamTypes.length) return false;
        for (int i = 0; i < methodParamTypes.length; i++) {
            if (!candidateParamTypes[i].equals(methodParamTypes[i])) return false;
        }
        return true;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        Set<String> interfacesSet = new LinkedHashSet<>();
        if (interfaces != null) Collections.addAll(interfacesSet, interfaces);
        for (Class extraInterface : classList) {
            if (extraInterface.isInterface()) interfacesSet.add(BytecodeHelper.getClassInternalName(extraInterface));
        }
        final boolean addGroovyObjectSupport = !GroovyObject.class.isAssignableFrom(superClass);
        if (addGroovyObjectSupport) interfacesSet.add("groovy/lang/GroovyObject");
        if (generateDelegateField) {
            classList.add(GeneratedGroovyProxy.class);
            interfacesSet.add("groovy/lang/GeneratedGroovyProxy");
        }
        super.visit(V1_5, ACC_PUBLIC, proxyName, signature, BytecodeHelper.getClassInternalName(superClass), interfacesSet.toArray(new String[0]));
        visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        addDelegateFields();
        if (addGroovyObjectSupport) {
            createGroovyObjectSupport();
        }
        for (Class clazz : classList) {
            visitClass(clazz);
        }
    }

    /**
     * Visit every class/interface this proxy should implement, and generate the appropriate
     * bytecode for delegation if available.
     *
     * @param clazz an class for which to generate bytecode
     */
    private void visitClass(final Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            String[] exceptions = new String[exceptionTypes.length];
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = BytecodeHelper.getClassInternalName(exceptionTypes[i]);
            }
            // for each method defined in the class, generate the appropriate delegation bytecode
            visitMethod(method.getModifiers(),
                    method.getName(),
                    BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes()),
                    null,
                    exceptions);
        }
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (Constructor method : constructors) {
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            String[] exceptions = new String[exceptionTypes.length];
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = BytecodeHelper.getClassInternalName(exceptionTypes[i]);
            }
            // for each method defined in the class, generate the appropriate delegation bytecode
            visitMethod(method.getModifiers(),
                    "<init>",
                    BytecodeHelper.getMethodDescriptor(Void.TYPE, method.getParameterTypes()),
                    null,
                    exceptions);
        }

        for (Class intf : clazz.getInterfaces()) {
            visitClass(intf);
        }
        Class superclass = clazz.getSuperclass();
        if (superclass != null) visitClass(superclass);

        // Ultimately, methods can be available in the closure map which are not defined by the superclass
        // nor the interfaces
        for (Map.Entry<String, Boolean> entry : delegatedClosures.entrySet()) {
            Boolean visited = entry.getValue();
            if (!visited) {
                String name = entry.getKey();
                if (!"*".equals(name)) {
                    // generate a new method
                    visitMethod(ACC_PUBLIC, name, "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
                }
            }
        }
    }

    /**
     * When an object doesn't implement the GroovyObject interface, we generate bytecode for the
     * {@link GroovyObject} interface methods. Otherwise, the superclass is expected to implement them.
     */
    private void createGroovyObjectSupport() {
        visitField(ACC_PRIVATE + ACC_TRANSIENT, "metaClass", "Lgroovy/lang/MetaClass;", null, null);

        // getMetaClass
        MethodVisitor mv;
        {
            mv = super.visitMethod(ACC_PUBLIC, "getMetaClass", "()Lgroovy/lang/MetaClass;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, proxyName, "metaClass", "Lgroovy/lang/MetaClass;");
            Label l1 = new Label();
            mv.visitJumpInsn(IFNONNULL, l1);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "getMetaClass", "(Ljava/lang/Class;)Lgroovy/lang/MetaClass;", false);
            mv.visitFieldInsn(PUTFIELD, proxyName, "metaClass", "Lgroovy/lang/MetaClass;");
            mv.visitLabel(l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, proxyName, "metaClass", "Lgroovy/lang/MetaClass;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }

        // getProperty
        {
            mv = super.visitMethod(ACC_PUBLIC, "getProperty", "(Ljava/lang/String;)Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitIntInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/GroovyObject", "getMetaClass", "()Lgroovy/lang/MetaClass;", true);
            mv.visitIntInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "getProperty", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;", true);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }

        // setProperty
        {
            mv = super.visitMethod(ACC_PUBLIC, "setProperty", "(Ljava/lang/String;Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, proxyName, "getMetaClass", "()Lgroovy/lang/MetaClass;", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V", true);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        // invokeMethod
        {
            mv = super.visitMethod(ACC_PUBLIC, "invokeMethod", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, proxyName, "getMetaClass", "()Lgroovy/lang/MetaClass;", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "invokeMethod", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        // setMetaClass
        {
            mv = super.visitMethod(ACC_PUBLIC, "setMetaClass", "(Lgroovy/lang/MetaClass;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, proxyName, "metaClass", "Lgroovy/lang/MetaClass;");
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
    }

    /**
     * Creates delegate fields for every closure defined in the map.
     */
    private void addDelegateFields() {
        visitField(ACC_PRIVATE + ACC_FINAL, CLOSURES_MAP_FIELD, "Ljava/util/Map;", null, null);
        if (generateDelegateField) {
            visitField(ACC_PRIVATE + ACC_FINAL, DELEGATE_OBJECT_FIELD, BytecodeHelper.getTypeDescription(delegateClass), null, null);
        }
    }

    private String proxyName() {
        String name = delegateClass != null ? delegateClass.getName() : superClass.getName();
        if (name.startsWith("[") && name.endsWith(";")) {
            name = name.substring(1, name.length() - 1) + "_array";
        }
        int index = name.lastIndexOf('.');
        if (index == -1) return name + pxyCounter.incrementAndGet() + "_groovyProxy";
        return name.substring(index + 1) + pxyCounter.incrementAndGet() + "_groovyProxy";
    }

    private static boolean isImplemented(Class clazz, String name, String desc) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                if (desc.equals(Type.getMethodDescriptor(method))) {
                    return !Modifier.isAbstract(method.getModifiers());
                }
            }
        }
        Class parent = clazz.getSuperclass();
        return parent != null && isImplemented(parent, name, desc);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        Object key = Arrays.asList(name, desc);
        if (visitedMethods.contains(key)) return null;
        if (Modifier.isPrivate(access) || Modifier.isNative(access) || ((access & ACC_SYNTHETIC) != 0)) {
            // do not generate bytecode for private methods
            return null;
        }
        int accessFlags = access;
        visitedMethods.add(key);
        if ((objectDelegateMethods.contains(name + desc) || delegatedClosures.containsKey(name) || (!"<init>".equals(name) && hasWildcard)) && !Modifier.isStatic(access) && !Modifier.isFinal(access)) {
            if (!GROOVYOBJECT_METHOD_NAMESS.contains(name)) {
                if (Modifier.isAbstract(access)) {
                    // prevents the proxy from being abstract
                    accessFlags -= ACC_ABSTRACT;
                }
                if (delegatedClosures.containsKey(name) || (!"<init>".equals(name) && hasWildcard)) {
                    delegatedClosures.put(name, Boolean.TRUE);
                    return makeDelegateToClosureCall(name, desc, signature, exceptions, accessFlags);
                }
                if (generateDelegateField && objectDelegateMethods.contains(name + desc)) {
                    return makeDelegateCall(name, desc, signature, exceptions, accessFlags);
                }
                delegatedClosures.put(name, Boolean.TRUE);
                return makeDelegateToClosureCall(name, desc, signature, exceptions, accessFlags);
            }
        } else if ("getProxyTarget".equals(name) && "()Ljava/lang/Object;".equals(desc)) {
            return createGetProxyTargetMethod(access, name, desc, signature, exceptions);
        } else if ("<init>".equals(name) && (Modifier.isPublic(access) || Modifier.isProtected(access))) {
            return createConstructor(access, name, desc, signature, exceptions);
        } else if (Modifier.isAbstract(access) && !GROOVYOBJECT_METHOD_NAMESS.contains(name)) {
            if (isImplemented(superClass, name, desc)) {
                return null;
            }
            accessFlags -= ACC_ABSTRACT;
            MethodVisitor mv = super.visitMethod(accessFlags, name, desc, signature, exceptions);
            mv.visitCode();
            Type[] args = Type.getArgumentTypes(desc);
            if (emptyBody) {
                Type returnType = Type.getReturnType(desc);
                if (returnType == Type.VOID_TYPE) {
                    mv.visitInsn(RETURN);
                } else {
                    int loadIns = getLoadInsn(returnType);
                    switch (loadIns) {
                        case ILOAD:
                            mv.visitInsn(ICONST_0);
                            break;
                        case LLOAD:
                            mv.visitInsn(LCONST_0);
                            break;
                        case FLOAD:
                            mv.visitInsn(FCONST_0);
                            break;
                        case DLOAD:
                            mv.visitInsn(DCONST_0);
                            break;
                        default:
                            mv.visitInsn(ACONST_NULL);
                    }
                    mv.visitInsn(getReturnInsn(returnType));
                    mv.visitMaxs(2, registerLen(args) + 1);
                }
            } else {
                // for compatibility with the legacy proxy generator, we should throw an UnsupportedOperationException
                // instead of an AbtractMethodException
                mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "()V", false);
                mv.visitInsn(ATHROW);
                mv.visitMaxs(2, registerLen(args) + 1);
            }
            mv.visitEnd();
        }
        return null;
    }

    private MethodVisitor createGetProxyTargetMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, name, desc, signature, exceptions);
        mv.visitCode();
        mv.visitIntInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, proxyName, DELEGATE_OBJECT_FIELD, BytecodeHelper.getTypeDescription(delegateClass));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        return null;
    }

    private static int registerLen(Type[] args) {
        int i = 0;
        for (Type arg : args) {
            i += registerLen(arg);
        }
        return i;
    }

    private static int registerLen(final Type arg) {
        return arg == Type.DOUBLE_TYPE || arg == Type.LONG_TYPE ? 2 : 1;
    }

    private MethodVisitor createConstructor(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        Type[] args = Type.getArgumentTypes(desc);
        StringBuilder newDesc = new StringBuilder("(");
        for (Type arg : args) {
            newDesc.append(arg.getDescriptor());
        }
        newDesc.append("Ljava/util/Map;"); // the closure map
        if (generateDelegateField) {
            newDesc.append(BytecodeHelper.getTypeDescription(delegateClass));
        }
        newDesc.append(")V");
        MethodVisitor mv = super.visitMethod(access, name, newDesc.toString(), signature, exceptions);
        mv.visitCode();
        initializeDelegateClosure(mv, args);
        if (generateDelegateField) {
            initializeDelegateObject(mv, args);
        }
        mv.visitVarInsn(ALOAD, 0);
        int idx = 1;
        for (Type arg : args) {
            if (isPrimitive(arg)) {
                mv.visitIntInsn(getLoadInsn(arg), idx);
            } else {
                mv.visitVarInsn(ALOAD, idx); // load argument i
            }
            idx += registerLen(arg);
        }
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(superClass), "<init>", desc, false);
        mv.visitInsn(RETURN);
        int max = idx + 1 + (generateDelegateField ? 1 : 0);
        mv.visitMaxs(max, max);
        mv.visitEnd();
        return null;
    }

    private void initializeDelegateClosure(final MethodVisitor mv, Type[] args) {
        int idx = 1 + getTypeArgsRegisterLength(args);

        mv.visitIntInsn(ALOAD, 0); // this
        mv.visitIntInsn(ALOAD, idx); // constructor arg n is the closure map

        mv.visitFieldInsn(PUTFIELD, proxyName, CLOSURES_MAP_FIELD, "Ljava/util/Map;");
    }

    private void initializeDelegateObject(final MethodVisitor mv, Type[] args) {
        int idx = 2 + getTypeArgsRegisterLength(args);

        mv.visitIntInsn(ALOAD, 0); // this
        mv.visitIntInsn(ALOAD, idx); // constructor arg n is the closure map
        mv.visitFieldInsn(PUTFIELD, proxyName, DELEGATE_OBJECT_FIELD, BytecodeHelper.getTypeDescription(delegateClass));
    }

    private static int getTypeArgsRegisterLength(Type[] args) {
        int length = 0;
        for (Type type : args) {
            length += registerLen(type);
        }
        return length;
    }

    /**
     * Generate a call to the delegate object.
     */
    protected MethodVisitor makeDelegateCall(final String name, final String desc, final String signature, final String[] exceptions, final int accessFlags) {
        MethodVisitor mv = super.visitMethod(accessFlags, name, desc, signature, exceptions);
        mv.visitVarInsn(ALOAD, 0); // load this
        mv.visitFieldInsn(GETFIELD, proxyName, DELEGATE_OBJECT_FIELD, BytecodeHelper.getTypeDescription(delegateClass)); // load delegate
        // using InvokerHelper to allow potential intercepted calls
        int size;
        mv.visitLdcInsn(name); // method name
        Type[] args = Type.getArgumentTypes(desc);
        BytecodeHelper.pushConstant(mv, args.length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        size = 6;
        int idx = 1;
        for (int i = 0; i < args.length; i++) {
            Type arg = args[i];
            mv.visitInsn(DUP);
            BytecodeHelper.pushConstant(mv, i);
            // primitive types must be boxed
            boxPrimitiveType(mv, idx, arg);
            size = Math.max(size, 5 + registerLen(arg));
            idx += registerLen(arg);
            mv.visitInsn(AASTORE); // store value into array
        }
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "invokeMethod", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", false);
        unwrapResult(mv, desc);
        mv.visitMaxs(size, registerLen(args) + 1);

        return mv;
    }

    protected MethodVisitor makeDelegateToClosureCall(final String name, final String desc, final String signature, final String[] exceptions, final int accessFlags) {
        MethodVisitor mv = super.visitMethod(accessFlags, name, desc, signature, exceptions);
//        TraceMethodVisitor tmv = new TraceMethodVisitor(mv);
//        mv = tmv;
        mv.visitCode();
        int stackSize = 0;
        // method body should be:
        //  this.$delegate$closure$methodName.call(new Object[] { method arguments })
        Type[] args = Type.getArgumentTypes(desc);
        int arrayStore = args.length + 1;
        BytecodeHelper.pushConstant(mv, args.length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // stack size = 1
        stackSize = 1;
        int idx = 1;
        for (int i = 0; i < args.length; i++) {
            Type arg = args[i];
            mv.visitInsn(DUP); // stack size = 2
            BytecodeHelper.pushConstant(mv, i); // array index, stack size = 3
            // primitive types must be boxed
            boxPrimitiveType(mv, idx, arg);
            idx += registerLen(arg);
            stackSize = Math.max(4, 3 + registerLen(arg));
            mv.visitInsn(AASTORE); // store value into array
        }
        mv.visitVarInsn(ASTORE, arrayStore); // store array
        int arrayIndex = arrayStore;
        mv.visitVarInsn(ALOAD, 0); // load this
        mv.visitFieldInsn(GETFIELD, proxyName, CLOSURES_MAP_FIELD, "Ljava/util/Map;"); // load closure map
        mv.visitLdcInsn(name); // load method name
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        arrayStore++;
        mv.visitVarInsn(ASTORE, arrayStore);
        // if null, test if wildcard exists
        Label notNull = new Label();
        mv.visitIntInsn(ALOAD, arrayStore);
        mv.visitJumpInsn(IFNONNULL, notNull);
        mv.visitVarInsn(ALOAD, 0); // load this
        mv.visitFieldInsn(GETFIELD, proxyName, CLOSURES_MAP_FIELD, "Ljava/util/Map;"); // load closure map
        mv.visitLdcInsn("*"); // load wildcard
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitVarInsn(ASTORE, arrayStore);
        mv.visitLabel(notNull);
        mv.visitVarInsn(ALOAD, arrayStore);
        mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(this.getClass()), "ensureClosure", "(Ljava/lang/Object;)Lgroovy/lang/Closure;", false);
        mv.visitVarInsn(ALOAD, arrayIndex); // load argument array
        stackSize++;
        mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Closure", "call", "([Ljava/lang/Object;)Ljava/lang/Object;", false); // call closure
        unwrapResult(mv, desc);
        mv.visitMaxs(stackSize, arrayStore + 1);
        mv.visitEnd();
//        System.out.println("tmv.getText() = " + tmv.getText());
        return null;
    }

    private void boxPrimitiveType(MethodVisitor mv, int idx, Type arg) {
        if (isPrimitive(arg)) {
            mv.visitIntInsn(getLoadInsn(arg), idx);
            String wrappedType = getWrappedClassDescriptor(arg);
            mv.visitMethodInsn(INVOKESTATIC, wrappedType, "valueOf", "(" + arg.getDescriptor() + ")L" + wrappedType + ";", false);
        } else {
            mv.visitVarInsn(ALOAD, idx); // load argument i
        }
    }

    private static void unwrapResult(final MethodVisitor mv, final String desc) {
        Type returnType = Type.getReturnType(desc);
        if (returnType == Type.VOID_TYPE) {
            mv.visitInsn(POP);
            mv.visitInsn(RETURN);
        } else {
            if (isPrimitive(returnType)) {
                BytecodeHelper.unbox(mv, ClassHelper.make(returnType.getClassName()));
            } else {
                mv.visitTypeInsn(CHECKCAST, returnType.getInternalName());
            }
            mv.visitInsn(getReturnInsn(returnType));
        }
    }

    @SuppressWarnings("unchecked")
    public GroovyObject proxy(Map<Object, Object> map, Object... constructorArgs) {
        if (constructorArgs == null && cachedNoArgConstructor != null) {
            // if there isn't any argument, we can make invocation faster using the cached constructor
            try {
                return (GroovyObject) cachedNoArgConstructor.newInstance(map);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new GroovyRuntimeException(e);
            }
        }
        if (constructorArgs == null) constructorArgs = EMPTY_ARGS;
        Object[] values = new Object[constructorArgs.length + 1];
        System.arraycopy(constructorArgs, 0, values, 0, constructorArgs.length);
        values[values.length - 1] = map;
        return DefaultGroovyMethods.<GroovyObject>newInstance(cachedClass, values);
    }

    @SuppressWarnings("unchecked")
    public GroovyObject delegatingProxy(Object delegate, Map<Object, Object> map, Object... constructorArgs) {
        if (constructorArgs == null && cachedNoArgConstructor != null) {
            // if there isn't any argument, we can make invocation faster using the cached constructor
            try {
                return (GroovyObject) cachedNoArgConstructor.newInstance(map, delegate);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new GroovyRuntimeException(e);
            }
        }
        if (constructorArgs == null) constructorArgs = EMPTY_ARGS;
        Object[] values = new Object[constructorArgs.length + 2];
        System.arraycopy(constructorArgs, 0, values, 0, constructorArgs.length);
        values[values.length - 2] = map;
        values[values.length - 1] = delegate;
        return DefaultGroovyMethods.<GroovyObject>newInstance(cachedClass, values);
    }

    /**
     * Ensures that the provided object is wrapped into a closure if it's not
     * a closure.
     * Do not trust IDEs, this method is used in bytecode.
     */
    @SuppressWarnings("unchecked")
    public static Closure ensureClosure(Object o) {
        if (o == null) throw new UnsupportedOperationException();
        if (o instanceof Closure) return (Closure) o;
        return new ReturnValueWrappingClosure(o);
    }

    private static int getLoadInsn(Type type) {
        return TypeUtil.getLoadInsnByType(type);
    }

    private static int getReturnInsn(Type type) {
        return TypeUtil.getReturnInsnByType(type);
    }

    private static boolean isPrimitive(Type type) {
        return TypeUtil.isPrimitiveType(type);
    }

    private static String getWrappedClassDescriptor(Type type) {
        return TypeUtil.getWrappedClassDescriptor(type);
    }

    private static class InnerLoader extends GroovyClassLoader {
        List<ClassLoader> internalClassLoaders = null;

        protected InnerLoader(final ClassLoader parent, final Class[] interfaces) {
            super(parent);
            if (interfaces != null) {
                for (Class c : interfaces) {
                    if (c.getClassLoader() != parent) {
                        if (internalClassLoaders == null)
                            internalClassLoaders = new ArrayList<>(interfaces.length);
                        if (!internalClassLoaders.contains(c.getClassLoader())) {
                            internalClassLoaders.add(c.getClassLoader());
                        }
                    }
                }
            }
        }

        public Class defineClass(String name, byte[] data) {
            return super.defineClass(name, data, 0, data.length);
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // First check whether it's already been loaded, if so use it
            Class loadedClass = findLoadedClass(name);

            if (loadedClass != null) return loadedClass;

            // Check this class loader
            try {
                loadedClass = findClass(name);
            } catch (ClassNotFoundException ignore) {
            }

            if (loadedClass != null) return loadedClass;

            // Check parent classloader, keep the exception for future use
            ClassNotFoundException ex = null;
            try {
                loadedClass = super.loadClass(name);
            } catch (ClassNotFoundException e) {
                ex = e;
            }

            if (loadedClass != null) return loadedClass;

            // Not loaded, try to load it
            if (internalClassLoaders != null) {
                for (ClassLoader i : internalClassLoaders) {
                    try {
                        // Ignore parent delegation and just try to load locally
                        loadedClass = i.loadClass(name);
                        if (loadedClass != null) return loadedClass;
                    } catch (ClassNotFoundException e) {
                        // Swallow exception - does not exist locally
                    }
                }
            }

            // Throw earlier exception from parent loader if it exists, otherwise create a new exception
            if (ex != null) throw ex;
            throw new ClassNotFoundException(name);
        }
    }

    private static class ReturnValueWrappingClosure<V> extends Closure<V>{
        private static final long serialVersionUID = 1313135457715304501L;
        private final V value;

        public ReturnValueWrappingClosure(V returnValue) {
            super(null);
            value = returnValue;
        }

        @Override
        public V call(final Object... args) {
            return value;
        }
    }

}
