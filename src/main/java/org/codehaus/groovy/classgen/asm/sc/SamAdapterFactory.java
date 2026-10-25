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
package org.codehaus.groovy.classgen.asm.sc;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Tuple2;
import groovy.transform.CompileStatic;
import groovy.transform.stc.POJO;
import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.BytecodeInstruction;
import org.codehaus.groovy.classgen.BytecodeSequence;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PARAMETER_TYPE;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SWAP;

/**
 * Converts a statically compiled closure to the functional interface its literal is passed, assigned,
 * returned or cast to with a class generated at compile time, the adapter, in place of the
 * {@code java.lang.reflect.Proxy} and {@link ConvertedClosure} the conversion creates at run time
 * otherwise. An instance is stored on the closure class under
 * {@link StaticCompilationMetadataKeys#FUNCTIONAL_INTERFACE_ADAPTER} and gives the adapter for a conversion
 * to the interface or one of its super-interfaces, generating it the first time, and {@code null} for any
 * other type.
 * <p>
 * The adapter is a final class nested in the closure class that implements the interface and
 * {@link Serializable} and wraps an instance of the closure class. Like the proxy, it is neither a
 * {@code Closure} nor a {@code GroovyObject}, its {@code toString} is the closure's, and its implementation
 * of the interface method calls the closure as the proxy does (see {@link AdapterMethod}).
 */
final class SamAdapterFactory implements Function<ClassNode, ClassNode> {

    /**
     * Lets the closure class be adapted to the functional interface the type checker found its literal
     * converted to. A trait, which the run-time conversion handles differently, a sealed interface, which
     * only the classes it permits may implement, an interface with a default method the adapter's method
     * would override, and an interface whose method the adapter cannot name keep the run-time conversion.
     */
    static void prepare(final WriterController controller, final ClassNode closureClass, final MethodNode doCall, final ClosureExpression expression) {
        if (expression instanceof LambdaExpression) return; // a lambda only gets a closure class when it has no functional target
        if (!(expression.getNodeMetaData(PARAMETER_TYPE) instanceof ClassNode target)) return;
        ClassNode type = target.getPlainNodeReference();
        if (!type.isInterface() || closureClass.implementsInterface(type)) return; // a closure already is a Runnable and a Callable
        if (type.isSealed()) return; // the adapter is not a permitted subclass
        MethodNode sam = ClassHelper.findSAM(type);
        if (sam == null) return;
        for (ClassNode face : type.getAllInterfaces()) {
            if (Traits.isTrait(face)) return;
        }
        List<MethodNode> methods = adapterMethods(type, sam);
        List<String> descriptors = methods.stream().map(BytecodeHelper::getMethodDescriptor).toList();
        for (ClassNode face : type.getAllInterfaces()) {
            for (MethodNode method : face.getDeclaredMethods(sam.getName())) {
                // a default method with the descriptor of an adapter method, such as the bridge javac adds to an interface that
                // redeclares the method it inherits, would be overridden; an overload with another descriptor is inherited
                if (method.isDefault() && descriptors.contains(BytecodeHelper.getMethodDescriptor(erasure(method, 0)))) return;
            }
        }
        for (MethodNode method : methods) {
            if (!isAccessible(method, type, closureClass)) return;
        }
        boolean implicitParameter = (expression.getParameters() != null && expression.getParameters().length == 0);
        closureClass.putNodeMetaData(StaticCompilationMetadataKeys.FUNCTIONAL_INTERFACE_ADAPTER,
                new SamAdapterFactory(controller, closureClass, doCall, type, methods, implicitParameter));
    }

    /**
     * The interface method as the adapter implements it, with the types the interface binds for the type
     * parameters of its super-interfaces and its own erased, followed, as bridges, by the erasure of every
     * declaration of the method in the interface and its super-interfaces with another descriptor. Each has
     * the same body, so a caller reaches the closure the same way whichever declaration it calls.
     */
    private static List<MethodNode> adapterMethods(final ClassNode type, final MethodNode sam) {
        Tuple2<ClassNode[], ClassNode> signature = GenericsUtils.parameterizeSAM(type);
        List<MethodNode> methods = new ArrayList<>();
        methods.add(adapterMethod(sam, signature.getV1(), signature.getV2(), ACC_PUBLIC));
        List<String> descriptors = new ArrayList<>(List.of(BytecodeHelper.getMethodDescriptor(methods.get(0))));
        for (ClassNode face : type.getAllInterfaces()) {
            for (MethodNode declared : face.getDeclaredMethods(sam.getName())) {
                if (!declared.isAbstract() || declared.getParameters().length != signature.getV1().length) continue;
                MethodNode bridge = erasure(declared, ACC_PUBLIC | ACC_SYNTHETIC | ACC_BRIDGE);
                String descriptor = BytecodeHelper.getMethodDescriptor(bridge);
                if (!descriptors.contains(descriptor)) {
                    descriptors.add(descriptor);
                    methods.add(bridge);
                }
            }
        }
        return methods;
    }

    /** The method with its parameter, return and exception types erased, as the adapter would declare it. */
    private static MethodNode erasure(final MethodNode method, final int modifiers) {
        ClassNode[] parameterTypes = Arrays.stream(method.getParameters()).map(Parameter::getType).toArray(ClassNode[]::new);
        return adapterMethod(method, parameterTypes, method.getReturnType(), modifiers);
    }

    private static MethodNode adapterMethod(final MethodNode declared, final ClassNode[] parameterTypes, final ClassNode returnType, final int modifiers) {
        Parameter[] parameters = new Parameter[parameterTypes.length];
        for (int i = 0; i < parameters.length; i += 1) {
            parameters[i] = new Parameter(erasure(parameterTypes[i]), "arg" + i);
        }
        ClassNode[] exceptions = declared.getExceptions() == null ? ClassNode.EMPTY_ARRAY : declared.getExceptions().clone();
        for (int i = 0; i < exceptions.length; i += 1) {
            exceptions[i] = erasure(exceptions[i]);
        }
        return new MethodNode(declared.getName(), modifiers, erasure(returnType), parameters, exceptions, null);
    }

    /**
     * Whether the adapter can name the interface and the types of the method. A type that is not public must
     * be in the package of the closure class and compiled with it: a class loader other than the closure's
     * may have defined a type loaded from elsewhere, and a package of the same name in another class loader
     * is another run-time package.
     */
    private static boolean isAccessible(final MethodNode method, final ClassNode type, final ClassNode from) {
        List<ClassNode> types = new ArrayList<>(List.of(type, method.getReturnType()));
        types.addAll(Arrays.asList(method.getExceptions()));
        for (Parameter parameter : method.getParameters()) types.add(parameter.getType());
        for (ClassNode t : types) {
            ClassNode c = t;
            while (c.isArray()) c = c.getComponentType();
            if (!ClassHelper.isPrimitiveType(c) && !Modifier.isPublic(c.getModifiers())
                    && (!c.isPrimaryClassNode() || !Objects.equals(c.getPackageName(), from.getPackageName()))) return false;
        }
        return true;
    }

    private static ClassNode erasure(final ClassNode type) {
        return type.isArray() ? erasure(type.getComponentType()).makeArray() : GenericsUtils.erasure(type);
    }

    private static void loadClosure(final MethodVisitor mv, final FieldNode closure) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, BytecodeHelper.getClassInternalName(closure.getOwner()), closure.getName(), BytecodeHelper.getTypeDescription(closure.getType()));
    }

    //--------------------------------------------------------------------------

    private final WriterController controller;
    private final ClassNode closureClass;
    private final MethodNode doCall;
    private final ClassNode type;
    private final List<MethodNode> methods;
    private final boolean implicitParameter;
    private ClassNode adapter;

    private SamAdapterFactory(final WriterController controller, final ClassNode closureClass, final MethodNode doCall,
                              final ClassNode type, final List<MethodNode> methods, final boolean implicitParameter) {
        this.controller = controller;
        this.closureClass = closureClass;
        this.doCall = doCall;
        this.type = type;
        this.methods = methods;
        this.implicitParameter = implicitParameter;
    }

    @Override
    public ClassNode apply(final ClassNode target) {
        if (!ClassNodeUtils.isCompatibleWith(type, target)) return null;
        if (adapter == null) adapter = createAdapter();
        return adapter;
    }

    private ClassNode createAdapter() {
        ClassNode[] interfaces = type.implementsInterface(ClassHelper.SERIALIZABLE_TYPE)
                ? new ClassNode[]{type} : new ClassNode[]{type, ClassHelper.SERIALIZABLE_TYPE}; // a proxy is Serializable
        InnerClassNode adapter = new InnerClassNode(closureClass, closureClass.getName() + "$SamAdapter",
                ACC_PUBLIC | ACC_FINAL | ACC_STATIC | ACC_SYNTHETIC, ClassHelper.OBJECT_TYPE, interfaces, MixinNode.EMPTY_ARRAY);
        adapter.setStaticClass(true);
        adapter.setSynthetic(true);
        adapter.setSourcePosition(closureClass);
        // like a proxy, the adapter does not implement GroovyObject, and it needs no MOP methods
        adapter.addAnnotation(new AnnotationNode(ClassHelper.make(POJO.class)));
        adapter.addAnnotation(new AnnotationNode(ClassHelper.make(CompileStatic.class)));
        adapter.putNodeMetaData(MopWriter.Factory.class, StaticCompilationMopWriter.FACTORY);

        ClassNode closureType = closureClass.getPlainNodeReference();
        FieldNode closure = adapter.addField("closure", ACC_PRIVATE | ACC_FINAL | ACC_SYNTHETIC, closureType, null);
        adapter.addConstructor(ACC_PUBLIC, new Parameter[]{new Parameter(closureType, "closure")}, ClassNode.EMPTY_ARRAY,
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                        mv.visitVarInsn(ALOAD, 1); // the compiler passes a new closure, but a reflective caller may not
                        mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                        mv.visitInsn(POP);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitFieldInsn(PUTFIELD, BytecodeHelper.getClassInternalName(adapter), closure.getName(), BytecodeHelper.getTypeDescription(closureType));
                        mv.visitInsn(RETURN);
                    }
                }));
        for (MethodNode method : methods) {
            method.setCode(new BytecodeSequence(new AdapterMethod(closure, doCall, method, implicitParameter)));
            addGeneratedMethod(adapter, method, true);
        }
        addGeneratedMethod(adapter, new MethodNode("toString", ACC_PUBLIC, ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        loadClosure(mv, closure);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
                        mv.visitInsn(ARETURN);
                    }
                })), true);

        controller.getAcg().addInnerClass(adapter);
        return adapter;
    }

    //--------------------------------------------------------------------------

    /**
     * The functional interface method of an adapter, which calls the wrapped closure as the
     * {@link ConvertedClosure} proxy does: it passes the arguments to
     * {@link Closure#call(Object...) call(Object...)}, converts the result to the return type with
     * {@code castToType}, unwraps a {@link GroovyRuntimeException}, and wraps a checked exception the
     * method does not declare in an {@link UndeclaredThrowableException}.
     * Where {@code call(Object...)} would hand every argument to {@code doCall} unchanged, which is when each
     * is an instance of the parameter type, or {@code null} for a reference type, and no parameter is an array,
     * it calls {@code doCall} directly instead: no reflection, and the same result.
     */
    private static final class AdapterMethod extends BytecodeInstruction {

        /** How an argument reaches its {@code doCall} parameter on the direct path. */
        private enum Conversion { NONE, BOX, CHECK, CALL_ONLY }

        private final FieldNode closure;
        private final MethodNode doCall;
        private final Parameter[] parameters;
        private final ClassNode returnType;
        private final ClassNode[] exceptions;
        private final boolean implicitParameter;
        private final int[] slots;

        AdapterMethod(final FieldNode closure, final MethodNode doCall, final MethodNode method, final boolean implicitParameter) {
            this.closure = closure;
            this.doCall = doCall;
            this.parameters = method.getParameters();
            this.returnType = method.getReturnType();
            this.exceptions = method.getExceptions();
            this.implicitParameter = implicitParameter;
            this.slots = new int[parameters.length];
            for (int i = 0, slot = 1; i < parameters.length; i += 1) {
                slots[i] = slot;
                ClassNode t = parameters[i].getType();
                slot += (ClassHelper.isPrimitiveLong(t) || ClassHelper.isPrimitiveDouble(t)) ? 2 : 1;
            }
        }

        @Override
        public void visit(final MethodVisitor mv) {
            Label start = new Label(), end = new Label(), handler = new Label();
            mv.visitTryCatchBlock(start, end, handler, "java/lang/Throwable");
            mv.visitLabel(start);

            Parameter[] doCallParameters = doCall.getParameters();
            int n = parameters.length;
            // a closure with an implicit parameter called without arguments gets null for it, as from call()
            boolean noArgument = (n == 0 && implicitParameter && doCallParameters.length == 1);
            Conversion[] conversions = new Conversion[n];
            boolean direct = (noArgument || n == doCallParameters.length), checked = false;
            for (int i = 0; i < n && direct; i += 1) {
                conversions[i] = conversion(parameters[i].getType(), erasure(doCallParameters[i].getType()));
                direct = (conversions[i] != Conversion.CALL_ONLY);
                checked |= (conversions[i] == Conversion.CHECK);
            }
            Label callPath = new Label();
            if (direct) {
                writeDirectCall(mv, conversions, noArgument, callPath);
            }
            if (!direct || checked) {
                mv.visitLabel(callPath);
                writeCall(mv);
            }
            mv.visitLabel(end);

            mv.visitLabel(handler);
            writeExceptionTranslation(mv);
        }

        /**
         * Calls {@code doCall} with the arguments, after checking those whose conversion needs a check and
         * jumping to the {@code call(Object...)} path if one fails.
         */
        private void writeDirectCall(final MethodVisitor mv, final Conversion[] conversions, final boolean noArgument, final Label callPath) {
            Parameter[] doCallParameters = doCall.getParameters();
            for (int i = 0; i < parameters.length; i += 1) {
                if (conversions[i] != Conversion.CHECK) continue;
                ClassNode target = erasure(doCallParameters[i].getType());
                Label pass = new Label();
                BytecodeHelper.load(mv, parameters[i].getType(), slots[i]);
                if (!ClassHelper.isPrimitiveType(target)) {
                    mv.visitJumpInsn(IFNULL, pass);
                    BytecodeHelper.load(mv, parameters[i].getType(), slots[i]);
                }
                mv.visitTypeInsn(INSTANCEOF, BytecodeHelper.getClassInternalName(ClassHelper.getWrapper(target)));
                mv.visitJumpInsn(IFEQ, callPath);
                mv.visitLabel(pass);
            }
            loadClosure(mv, closure);
            if (noArgument) {
                mv.visitInsn(ACONST_NULL);
            }
            for (int i = 0; i < parameters.length; i += 1) {
                ClassNode source = parameters[i].getType(), target = erasure(doCallParameters[i].getType());
                BytecodeHelper.load(mv, source, slots[i]);
                if (conversions[i] == Conversion.BOX) {
                    BytecodeHelper.doCastToWrappedType(mv, source, ClassHelper.getWrapper(source));
                } else if (conversions[i] == Conversion.CHECK) {
                    ClassNode wrapper = ClassHelper.getWrapper(target);
                    mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(wrapper));
                    if (ClassHelper.isPrimitiveType(target)) {
                        BytecodeHelper.doCastToPrimitive(mv, wrapper, target);
                    }
                }
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, BytecodeHelper.getClassInternalName(closure.getType()), doCall.getName(),
                    BytecodeHelper.getMethodDescriptor(doCall.getReturnType(), doCallParameters), false);
            writeReturn(mv, erasure(doCall.getReturnType()));
        }

        /** Calls {@code call(Object...)} with the arguments, boxed, as the proxy does. */
        private void writeCall(final MethodVisitor mv) {
            loadClosure(mv, closure);
            BytecodeHelper.pushConstant(mv, parameters.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            for (int i = 0; i < parameters.length; i += 1) {
                ClassNode source = parameters[i].getType();
                mv.visitInsn(DUP);
                BytecodeHelper.pushConstant(mv, i);
                BytecodeHelper.load(mv, source, slots[i]);
                if (ClassHelper.isPrimitiveType(source)) {
                    BytecodeHelper.doCastToWrappedType(mv, source, ClassHelper.getWrapper(source));
                }
                mv.visitInsn(AASTORE);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Closure", "call", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
            writeReturn(mv, ClassHelper.OBJECT_TYPE);
        }

        /**
         * Treats the exception on the stack as {@code ConversionHandler} and the proxy do: unwraps a
         * {@code GroovyRuntimeException}, then rethrows an unchecked or declared exception and wraps any
         * other in an {@code UndeclaredThrowableException}.
         */
        private void writeExceptionTranslation(final MethodVisitor mv) {
            Label check = new Label(), rethrow = new Label();
            mv.visitInsn(DUP);
            mv.visitTypeInsn(INSTANCEOF, "groovy/lang/GroovyRuntimeException");
            mv.visitJumpInsn(IFEQ, check);
            mv.visitTypeInsn(CHECKCAST, "groovy/lang/GroovyRuntimeException");
            mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/ScriptBytecodeAdapter", "unwrap",
                    "(Lgroovy/lang/GroovyRuntimeException;)Ljava/lang/Throwable;", false);
            mv.visitLabel(check);
            List<String> rethrown = new ArrayList<>(List.of("java/lang/RuntimeException", "java/lang/Error"));
            for (ClassNode exception : exceptions) rethrown.add(BytecodeHelper.getClassInternalName(exception));
            for (String exception : rethrown) {
                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, exception);
                mv.visitJumpInsn(IFNE, rethrow);
            }
            mv.visitTypeInsn(NEW, "java/lang/reflect/UndeclaredThrowableException");
            mv.visitInsn(DUP_X1);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(rethrow);
            mv.visitInsn(ATHROW);
        }

        /**
         * How an argument of the interface method reaches the {@code doCall} parameter on the direct path:
         * as it is or boxed when {@code call(Object...)} would always pass it unchanged, after a type check
         * when it would only for some values, and not at all when it may convert the argument.
         */
        private static Conversion conversion(final ClassNode source, final ClassNode target) {
            if (target.isArray()) return Conversion.CALL_ONLY; // array parameters have their own call semantics
            if (ClassHelper.isPrimitiveType(source)) {
                if (ClassHelper.isPrimitiveType(target)) return source.equals(target) ? Conversion.NONE : Conversion.CALL_ONLY;
                return isAssignable(ClassHelper.getWrapper(source), target) ? Conversion.BOX : Conversion.CALL_ONLY;
            }
            if (ClassHelper.isPrimitiveType(target)) return Conversion.CHECK;
            return isAssignable(source, target) ? Conversion.NONE : Conversion.CHECK;
        }

        private static boolean isAssignable(final ClassNode source, final ClassNode target) {
            return ClassHelper.isObjectType(target) || ClassNodeUtils.isCompatibleWith(source, target);
        }

        /**
         * Converts the value on the stack to the return type as the proxy does, with {@code castToType}, and
         * returns it. The call is left out where it would return the value unchanged, and for a primitive type
         * other than {@code double}, where it amounts to the unboxing that follows it. A primitive value is
         * first boxed, and nothing from a {@code void} method replaced by {@code null}, as
         * {@code call(Object...)} returns them; the {@code doCall} of a generated closure returns a reference
         * type, so this only guards against one that does not.
         */
        private void writeReturn(final MethodVisitor mv, final ClassNode type) {
            ClassNode valueType = type;
            if (ClassHelper.isPrimitiveVoid(valueType)) {
                mv.visitInsn(ACONST_NULL);
                valueType = ClassHelper.OBJECT_TYPE;
            } else if (ClassHelper.isPrimitiveType(valueType)) {
                valueType = ClassHelper.getWrapper(valueType);
                BytecodeHelper.doCastToWrappedType(mv, type, valueType);
            }
            if (ClassHelper.isPrimitiveVoid(returnType)) {
                mv.visitInsn(POP);
                mv.visitInsn(RETURN);
                return;
            }
            boolean primitive = ClassHelper.isPrimitiveType(returnType);
            if (primitive ? ClassHelper.isPrimitiveDouble(returnType) : !isAssignable(valueType, returnType)) {
                BytecodeHelper.visitClassLiteral(mv, returnType);
                mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation",
                        "castToType", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;", false);
            }
            if (primitive || !isAssignable(valueType, returnType)) {
                BytecodeHelper.doCast(mv, returnType); // DefaultTypeTransformation.<type>Unbox, or a checkcast
            }
            BytecodeHelper.doReturn(mv, returnType);
        }
    }
}
