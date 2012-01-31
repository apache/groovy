/*
 * Copyright 2003-2009 the original author or authors.
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
package groovy.util;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
 * @author Cedric Champeau
 */
public class ProxyGeneratorAdapter extends ClassAdapter implements Opcodes {
    private static final Map<String, DelegateClosure> EMPTY_CLOSURE_MAP = Collections.emptyMap();

    private final static AtomicLong pxyCounter = new AtomicLong();
    private static final Set<String> GROOVYOBJECT_METHODS;
    private static final String WILDCARD = "*";
    private static final String WILDCARD_FIELD = "$wildcard";
    private static final Object[] EMPTY_ARGS = new Object[0];

    static {
        List<String> names = new ArrayList<String>();
        for (Method method : GroovyObject.class.getMethods()) {
            names.add(method.getName());
        }
        GROOVYOBJECT_METHODS = new HashSet<String>(names);
    }
    
    private final Class superClass;
    private final InnerLoader loader;
    private final String proxyName;
    private final List<Class> classList;
    private final Map<String, DelegateClosure> closureMap;
    private boolean addGroovyObjectSupport = false;

    // if emptyBody == true, then we generate an empty body instead throwing error on unimplemented methods
    private final boolean emptyBody;
    private final boolean hasWildcard;
    
    private final Set<Object> visitedMethods = new LinkedHashSet<Object>();
    
    // cached class
    private final Class cachedClass;

    /**
     * Construct a proxy generator. This generator is used when we need to create a proxy object for a class or an
     * interface given a map of closures.
     *
     * @param closureMap the delegates implementations
     * @param superClass corresponding to the superclass class visitor
     * @param interfaces extra interfaces the proxy should implement
     * @param proxyLoader the class loader which should be used to load the generated proxy
     */
    public ProxyGeneratorAdapter(final Map<Object, Object> closureMap, final Class superClass, final Class[] interfaces, ClassLoader proxyLoader, boolean emptyBody) {
        super(new ClassWriter(0));
        this.closureMap = closureMap.isEmpty()?EMPTY_CLOSURE_MAP:new TreeMap<String, DelegateClosure>();
        boolean wildcard = false;
        for (Map.Entry<Object, Object> entry : closureMap.entrySet()) {
            final Object value = entry.getValue();
            Closure cl = value instanceof Closure?(Closure)value:new Closure(null) {
                @Override
                public Object call(final Object... args) {
                    // if the supplied value in the map is not a closure, then this value is wrapped in a closure
                    return value;
                }
            };
            String name = entry.getKey().toString();
            if ("*".equals(name)) {
                name= WILDCARD_FIELD;
                wildcard = true;
            }
            this.closureMap.put(findFieldName(entry.getKey()), new DelegateClosure(name,cl));
        }
        this.hasWildcard = wildcard;
        // a proxy is supposed to be a concrete class, so it cannot extend an interface.
        // If the provided superclass is an interface, then we replace the superclass with Object
        // and add this interface to the list of implemented interfaces
        boolean isSuperClassAnInterface = superClass.isInterface();
        this.superClass = isSuperClassAnInterface ?Object.class:superClass;

        // create the base list of classes which have possible methods to be overloaded
        this.classList = new LinkedList<Class>();
        this.classList.add(superClass);
        if (interfaces!=null) {
            Collections.addAll(this.classList, interfaces);
        }
        this.proxyName = proxyName(superClass.getSimpleName());
        this.loader = proxyLoader!=null?new InnerLoader(proxyLoader):findClassLoader(superClass);
        this.emptyBody = emptyBody;

        // generate bytecode
        ClassWriter writer = (ClassWriter) cv;
        ClassReader cr = createClassVisitor(Object.class);
        cr.accept(this, 0);
        byte[] b = writer.toByteArray();
//        CheckClassAdapter.verify(new ClassReader(b), true, new PrintWriter(System.err) );
        cachedClass = loader.defineClass(proxyName, b);
    }

    private InnerLoader findClassLoader(Class clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if (cl==null) cl = this.getClass().getClassLoader();
        return new InnerLoader(cl);
    }

    /**
     * Creates a visitor which will be used as a base class for initiating the visit.
     * It is not necessary that the class is the superclass, any will do, as long as
     * it can be loaded from a byte[].
     * @param baseClass
     * @return
     */
    private ClassReader createClassVisitor(final Class baseClass) {
        try {
            String name = baseClass.getName();
            String path = name.replace('.', '/') + ".class";
            InputStream in = loader.getResourceAsStream(path);
            return new ClassReader(in);
        } catch (IOException e) {
            throw new GroovyRuntimeException("Unable to generate a proxy for " + baseClass +" from class loader "+loader,e);
        }
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        Set<String> interfacesSet = new LinkedHashSet<String>();
        if (interfaces != null) Collections.addAll(interfacesSet, interfaces);
        for (Class extraInterface : classList) {
            if (extraInterface.isInterface()) interfacesSet.add(BytecodeHelper.getClassInternalName(extraInterface));
        }
        addGroovyObjectSupport = !GroovyObject.class.isAssignableFrom(superClass);
        if (addGroovyObjectSupport) interfacesSet.add("groovy/lang/GroovyObject");
        super.visit(V1_5, ACC_PUBLIC, proxyName, signature, BytecodeHelper.getClassInternalName(superClass), interfacesSet.toArray(new String[interfacesSet.size()]));
        addClosureDelegates();
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
        if (superclass!=null) visitClass(superclass);

        // Ultimately, methods can be available in the closure map which are not defined by the superclass
        // nor the interfaces
        for (Map.Entry<String, DelegateClosure> entry : closureMap.entrySet()) {
            DelegateClosure delegate = entry.getValue();
            if (!delegate.visited) {
                // generate a new method
                visitMethod(ACC_PUBLIC, delegate.name, "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "getMetaClass", "(Ljava/lang/Class;)Lgroovy/lang/MetaClass;");
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
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/GroovyObject", "getMetaClass", "()Lgroovy/lang/MetaClass;");
            mv.visitIntInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "getProperty", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
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
            mv.visitMethodInsn(INVOKEVIRTUAL, proxyName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "setProperty", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V");
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
            mv.visitMethodInsn(INVOKEVIRTUAL, proxyName, "getMetaClass", "()Lgroovy/lang/MetaClass;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "groovy/lang/MetaClass", "invokeMethod", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");
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
    private void addClosureDelegates() {
        for (String fieldName : closureMap.keySet()) {
            visitField(ACC_PRIVATE + ACC_FINAL, fieldName, "Lgroovy/lang/Closure;", null, null);
        }
    }

    private static String findFieldName(final Object keyObject) {
        if ("*".equals(keyObject.toString())) return "$delegate$closure$"+WILDCARD_FIELD;
        return "$delegate$closure$" + keyObject.toString();
    }

    private static String proxyName(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) return "$Proxy" + name + pxyCounter.incrementAndGet();
        return "$Proxy" + name.substring(index + 1, name.length()) + pxyCounter.incrementAndGet();
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        Object key = Arrays.asList(name, desc);
        if (visitedMethods.contains(key)) return new EmptyVisitor();
        if (Modifier.isPrivate(access) || Modifier.isNative(access)) {
            // do not generate bytecode for private methods
            return new EmptyVisitor();
        }
        int accessFlags = access;
        String fieldName = findFieldName(name);
        visitedMethods.add(key);
        if ((closureMap.containsKey(fieldName) || (!"<init>".equals(name) && hasWildcard)) && !Modifier.isStatic(access) && !Modifier.isFinal(access)) {
            DelegateClosure delegate = closureMap.get(fieldName);
            if (delegate==null) {
                fieldName = findFieldName(WILDCARD);
                delegate = closureMap.get(fieldName);
            }
            delegate.visited = true;
            if (Modifier.isAbstract(access)) {
                // prevents the proxy from being abstract
                accessFlags -= ACC_ABSTRACT;
            }
            return makeDelegateToClosureCall(name, desc, signature, exceptions, accessFlags, fieldName);
        } else if ("<init>".equals(name) && (Modifier.isPublic(access) || Modifier.isProtected(access))) {
            return createConstructor(access, name, desc, signature, exceptions);
        } else if (Modifier.isAbstract(access) && !GROOVYOBJECT_METHODS.contains(name)) {
            accessFlags -= ACC_ABSTRACT;
            MethodVisitor mv = super.visitMethod(accessFlags, name, desc, signature, exceptions);
            mv.visitCode();
            if (emptyBody) {
                Type returnType = Type.getReturnType(desc);
                if (returnType==Type.VOID_TYPE) {
                    mv.visitInsn(RETURN);
                } else {
                    int loadIns = getLoadInsn(returnType);
                    switch (loadIns) {
                        case ILOAD: mv.visitInsn(ICONST_0);
                            break;
                        case LLOAD: mv.visitInsn(LCONST_0);
                           break;
                        case FLOAD: mv.visitInsn(FCONST_0);
                            break;
                        case DLOAD: mv.visitInsn(DCONST_0);
                            break;
                        default:
                            mv.visitInsn(ACONST_NULL);
                    }
                    mv.visitInsn(getReturnInsn(returnType));
                    mv.visitMaxs(2, 2);
                }
            } else {
                // for compatibility with the legacy proxy generator, we should throw an UnsupportedOperationException
                // instead of an AbtractMethodException
                mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "()V");
                mv.visitInsn(ATHROW);
                mv.visitMaxs(2, 1);
            }
            mv.visitEnd();
        }
        return new EmptyVisitor();
    }

    private MethodVisitor createConstructor(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        Type[] args = Type.getArgumentTypes(desc);
        StringBuilder newDesc = new StringBuilder("(");
        for (Type arg : args) {
            newDesc.append(arg.getDescriptor());
        }
        for (int i = 0; i < closureMap.size(); i++) {
            newDesc.append("Lgroovy/lang/Closure;");
        }
        newDesc.append(")V");
        MethodVisitor mv = super.visitMethod(access, name, newDesc.toString(), signature, exceptions);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        for (int i = 0; i < args.length; i++) {
            Type arg = args[i];
            if (isPrimitive(arg)) {
                mv.visitIntInsn(getLoadInsn(arg), i + 1);
            } else {
                mv.visitVarInsn(ALOAD, i + 1); // load argument i
            }
        }
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(superClass), "<init>", desc);
        initializeDelegates(mv, args.length);
        if (addGroovyObjectSupport) {
            // create metaclass
            mv.visitIntInsn(ALOAD, 0);
            // this.metaClass = InvokerHelper.getMetaClass(this.getClass());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "getMetaClass", "(Ljava/lang/Class;)Lgroovy/lang/MetaClass;");
            mv.visitFieldInsn(PUTFIELD, proxyName, "metaClass", "Lgroovy/lang/MetaClass;");
        }
        mv.visitInsn(RETURN);
        int max = 1 + args.length + closureMap.size();
        mv.visitMaxs(addGroovyObjectSupport?1+max:max, max);
        mv.visitEnd();
        return new EmptyVisitor();
    }

    private void initializeDelegates(final MethodVisitor mv, int argStart) {
        int idx = argStart+1;
        for (String name : closureMap.keySet()) {
            mv.visitIntInsn(ALOAD, 0); // this
            mv.visitIntInsn(ALOAD, idx++); // constructor arg n
            mv.visitFieldInsn(PUTFIELD, proxyName, name, "Lgroovy/lang/Closure;");
        }
    }

    private MethodVisitor makeDelegateToClosureCall(final String name, final String desc, final String signature, final String[] exceptions, final int accessFlags, final String fieldName) {
        MethodVisitor mv = super.visitMethod(accessFlags, name, desc, signature, exceptions);
//        TraceMethodVisitor tmv = new TraceMethodVisitor(mv);
//        mv = tmv;
        mv.visitCode();
        int stackSize = 0;
        // method body should be:
        //  this.$delegate$closure$methodName.call(new Object[] { method arguments })
        Type[] args = Type.getArgumentTypes(desc);
        int arrayStore = args.length+1;
        BytecodeHelper.pushConstant(mv, args.length);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // stack size = 1
        stackSize = 1;
        for (int i = 0; i < args.length; i++) {
            Type arg = args[i];
            mv.visitInsn(DUP); // stack size = 2
            BytecodeHelper.pushConstant(mv, i); // array index, stack size = 3
            stackSize = 3;
            // primitive types must be boxed
            if (isPrimitive(arg)) {
                mv.visitIntInsn(getLoadInsn(arg), i + 1);
                String wrappedType = getWrappedClassDescriptor(arg);
                mv.visitMethodInsn(INVOKESTATIC,
                        wrappedType,
                        "valueOf",
                        "(" + arg.getDescriptor() + ")L" + wrappedType + ";");
            } else {
                mv.visitVarInsn(ALOAD, i + 1); // load argument i
            }
            stackSize = 4;
            mv.visitInsn(AASTORE); // store value into array
        }
        mv.visitVarInsn(ASTORE, arrayStore); // store array
        mv.visitVarInsn(ALOAD, 0); // load this
        mv.visitFieldInsn(GETFIELD, proxyName, fieldName, "Lgroovy/lang/Closure;");
        mv.visitVarInsn(ALOAD, arrayStore); // load argument array
        stackSize++;
        mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Closure", "call", "([Ljava/lang/Object;)Ljava/lang/Object;"); // call closure
        Type returnType = Type.getReturnType(desc);
        if (returnType==Type.VOID_TYPE) {
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
        mv.visitMaxs(stackSize, arrayStore+1);
        mv.visitEnd();
//        System.out.println("tmv.getText() = " + tmv.getText());
        return new EmptyVisitor();
    }

    @SuppressWarnings("unchecked")
    public GroovyObject proxy(Object... constructorArgs) {
        if (constructorArgs==null) constructorArgs= EMPTY_ARGS;
        DelegateClosure[] delegates = closureMap.values().toArray(new DelegateClosure[closureMap.size()]);
        Object[] values = new Object[constructorArgs.length + delegates.length];
        System.arraycopy(constructorArgs, 0, values, 0, constructorArgs.length);
        for (int i = 0; i < delegates.length; i++) {
            DelegateClosure delegate = delegates[constructorArgs.length + i];
            values[i] = delegate.closure;
        }
        return DefaultGroovyMethods.<GroovyObject>newInstance(cachedClass, values);
    }

    private static int getLoadInsn(final Type type) {
        if (type == Type.BOOLEAN_TYPE) return ILOAD;
        if (type == Type.BYTE_TYPE) return ILOAD;
        if (type == Type.CHAR_TYPE) return ILOAD;
        if (type == Type.DOUBLE_TYPE) return DLOAD;
        if (type == Type.FLOAT_TYPE) return FLOAD;
        if (type == Type.INT_TYPE) return ILOAD;
        if (type == Type.LONG_TYPE) return LLOAD;
        if (type == Type.SHORT_TYPE) return ILOAD;
        return ALOAD;
    }

    private static int getReturnInsn(final Type type) {
        if (type == Type.BOOLEAN_TYPE) return IRETURN;
        if (type == Type.BYTE_TYPE) return IRETURN;
        if (type == Type.CHAR_TYPE) return IRETURN;
        if (type == Type.DOUBLE_TYPE) return DRETURN;
        if (type == Type.FLOAT_TYPE) return FRETURN;
        if (type == Type.INT_TYPE) return IRETURN;
        if (type == Type.LONG_TYPE) return LRETURN;
        if (type == Type.SHORT_TYPE) return IRETURN;
        return ARETURN;
    }

    private boolean isPrimitive(final Type arg) {
        return arg == Type.BOOLEAN_TYPE
                || arg == Type.BYTE_TYPE
                || arg == Type.CHAR_TYPE
                || arg == Type.DOUBLE_TYPE
                || arg == Type.FLOAT_TYPE
                || arg == Type.INT_TYPE
                || arg == Type.LONG_TYPE
                || arg == Type.SHORT_TYPE;
    }

    private String getWrappedClassDescriptor(Type type) {
        if (type == Type.BOOLEAN_TYPE) return "java/lang/Boolean";
        if (type == Type.BYTE_TYPE) return "java/lang/Byte";
        if (type == Type.CHAR_TYPE) return "java/lang/Character";
        if (type == Type.DOUBLE_TYPE) return "java/lang/Double";
        if (type == Type.FLOAT_TYPE) return "java/lang/Float";
        if (type == Type.INT_TYPE) return "java/lang/Integer";
        if (type == Type.LONG_TYPE) return "java/lang/Long";
        if (type == Type.SHORT_TYPE) return "java/lang/Short";
        throw new IllegalArgumentException("Unexpected type class [" + type + "]");
    }

    private static class InnerLoader extends ClassLoader {
        protected InnerLoader(final ClassLoader parent) {
            super(parent);
        }

        protected Class defineClass(String name, byte[] data) {
            return super.defineClass(name, data, 0, data.length);
        }

    }    

    private static class DelegateClosure {
        private final Closure closure;
        private final String name;
        
        private boolean visited = false;

        private DelegateClosure(final String name, final Closure closure) {
            this.name = name;
            this.closure = closure;
        }
    }
    
}
