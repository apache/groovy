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
package org.codehaus.groovy.classgen.asm;

import groovy.transform.PackedClosures.PackMode;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.MethodVisitor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_RETURN_TYPE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Generates bytecode for closure expressions.
 */
public class ClosureWriter {

    /**
     * Field name for the outer instance reference.
     */
    public static final String OUTER_INSTANCE = "_outerInstance";
    /**
     * Field name for the this object reference.
     */
    public static final String THIS_OBJECT = "_thisObject";

    /**
     * Marker interface for using existing reference.
     */
    protected interface UseExistingReference {
    }

    /** The controller coordinating all bytecode writers for the current class. */
    protected final WriterController controller;
    private final Map<Expression, ClassNode> closureClasses = new HashMap<>();
    // Closures found in a syntactic position that lets them outlive the method are cached per method.
    private final Map<MethodNode, Set<ClosureExpression>> escapingClosuresByMethod = new HashMap<>();
    // Closure literals visibly bound for serialization (cast/coerced to Serializable, or passed
    // directly to writeObject), cached per method like the escape gate above.
    private final Map<MethodNode, Set<ClosureExpression>> serializationBoundByMethod = new HashMap<>();
    // Names assigned anywhere in a method (excluding declarations), cached per method: a capture must
    // be Reference-threaded if it is written ANYWHERE in the enclosing method, not just inside the
    // closure -- e.g. `def fib; fib = { n -> ... fib(n-1) ... }` writes fib after the closure is
    // constructed, so a by-value capture would see the stale (null) value.
    private final Map<MethodNode, Set<String>> writtenNamesByMethod = new HashMap<>();

    /** Name prefix of the synthetic methods holding hoisted closure bodies. */
    /** Name prefix of hoisted packed-closure bodies (also consulted by the static call-site writer). */
    public static final String PACKED_METHOD_PREFIX = "$packed$closure$";

    // The class's registered dispatch targets (hoisted closure bodies), in id order. Kept as node
    // metadata on the CLASS, not on this writer: in a class mixing dynamic and @CompileStatic methods
    // the controller routes closures to two different ClosureWriter instances (see
    // StaticTypesWriterController#getClosureWriter), and both must share one id space. The end-of-class
    // hook (writePackedDispatcher) turns the list into the class's GeneratedDispatcher table.
    private static final String PACKED_TARGETS = "org.codehaus.groovy.classgen.asm.ClosureWriter.packedTargets";
    private static final String DISPATCHER_TYPE = "org/codehaus/groovy/runtime/GeneratedDispatcher";
    private static final String BUNDLE_TYPE = DISPATCHER_TYPE + "$Bundle";
    // The general table covers every target via a packed argument array; the per-arity tables
    // cover the hot one/two-value shapes with plain parameters (no array to allocate or escape).
    private static final String DISPATCH_METHOD = org.codehaus.groovy.runtime.GeneratedDispatcher.TABLE_METHOD;
    private static final String DISPATCH_DESC = "(I[Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String DISPATCH1_METHOD = org.codehaus.groovy.runtime.GeneratedDispatcher.TABLE1_METHOD;
    private static final String DISPATCH1_DESC = "(ILjava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String DISPATCH2_METHOD = org.codehaus.groovy.runtime.GeneratedDispatcher.TABLE2_METHOD;
    private static final String DISPATCH2_DESC = "(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    // The accessor's single invokedynamic site links all three tables through
    // GeneratedDispatcher.bootstrap into one constant Bundle, lazily on first adapter creation.
    private static final String DISPATCHERS_GETTER = "$getPackedDispatchers$";
    private static final String DISPATCHERS_GETTER_DESC = "()L" + BUNDLE_TYPE + ";";
    // Max tableswitch cases per dispatch method (power of two: the two-level entry method selects a
    // chunk with a shift); sized so a full chunk stays well under the JIT's 325-byte inlining budget.
    private static final int DISPATCH_CHUNK = 8;

    private static List<MethodNode> packedTargets(final ClassNode classNode) {
        List<MethodNode> targets = classNode.getNodeMetaData(PACKED_TARGETS);
        if (targets == null) {
            targets = new ArrayList<>();
            classNode.putNodeMetaData(PACKED_TARGETS, targets);
        }
        return targets;
    }

    /**
     * Creates a closure writer with the given controller.
     *
     * @param controller the writer controller
     */
    public ClosureWriter(final WriterController controller) {
        this.controller = controller;
    }

    /**
     * Generates bytecode for a closure expression.
     *
     * @param expression the closure expression
     */
    public void writeClosure(final ClosureExpression expression) {
        // @PackedClosures spike: for an eligible top-level closure in a @PackedClosures scope, hoist the
        // body to a method on the enclosing class and emit a shared PackedClosure adapter, instead of
        // generating a per-closure inner class. Captured variables are threaded by value. Closures nested
        // inside another closure are left alone (their enclosing context is a generated function, not the
        // owner class), as are closures needing real Closure semantics.
        PackStrategy strategy = chooseStrategy(expression);
        if (strategy == PackStrategy.PACKED_ADAPTER || strategy == PackStrategy.OPEN_ADAPTER) {
            writePackedClosure(expression, strategy == PackStrategy.OPEN_ADAPTER);
            return;
        }
        reportUnpacked(expression); // @PackedClosures(mode = WARN | STRICT) diagnostics for declines

        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode classNode = controller.getClassNode();
        AsmClassGenerator acg = controller.getAcg();

        // generate closure as public class to make sure it can be properly invoked by classes of the
        // Groovy runtime without circumventing JVM access checks (see CachedMethod for example).
        int mods = ACC_PUBLIC | ACC_FINAL;
        if (classNode.isInterface()) {
            mods |= ACC_STATIC;
        }
        ClassNode closureClass = getOrAddClosureClass(expression, mods);
        String closureClassinternalName = BytecodeHelper.getClassInternalName(closureClass);
        List<ConstructorNode> constructors = closureClass.getDeclaredConstructors();
        ConstructorNode node = constructors.get(0);

        Parameter[] localVariableParams = node.getParameters();

        mv.visitTypeInsn(NEW, closureClassinternalName);
        mv.visitInsn(DUP);
        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall()) {
            new ClassExpression(classNode).visit(acg);
            new ClassExpression(controller.getOutermostClass()).visit(acg);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
            loadThis();
        }

        // now let's load the various parameters we're passing
        // we start at index 2 because the first variable we pass
        // is the owner instance and at this point it is already
        // on the stack
        for (int i = 2; i < localVariableParams.length; i++) {
            Parameter param = localVariableParams[i];
            String name = param.getName();
            loadReference(name, controller);
            if (param.getNodeMetaData(ClosureWriter.UseExistingReference.class) == null) {
                param.setNodeMetaData(ClosureWriter.UseExistingReference.class, Boolean.TRUE);
            }
        }

        // we may need to pass in some other constructors
        //cv.visitMethodInsn(INVOKESPECIAL, innerClassinternalName, "<init>", prototype + ")V");
        mv.visitMethodInsn(INVOKESPECIAL, closureClassinternalName, "<init>", BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, localVariableParams), false);
        controller.getOperandStack().replace(ClassHelper.CLOSURE_TYPE, localVariableParams.length);
    }

    /**
     * GEP-27 capability analysis: choose the compilation strategy for a closure literal.
     * <p>
     * A closure is packed (S1, {@link PackStrategy#PACKED_ADAPTER}) when it is <em>triggered</em> —
     * either the enclosing scope opts in via {@code @PackedClosures} (the dynamic trust path), or the
     * capability analysis <em>proves</em> it delegate-independent under {@code @CompileStatic} (the
     * automatic path, see {@link #isDelegateIndependent}) — and it is in a packable position:
     * it needs no real {@code Closure} semantics we cannot reproduce ({@link #isPackable}), it is
     * written directly in a method rather than nested in another closure (owner-retargeting for
     * nested closures is later work), and it does not visibly escape its method. Otherwise it stays a
     * full generated class (S0, {@link PackStrategy#FULL_CLASS}) exactly as today.
     */
    private PackStrategy chooseStrategy(final ClosureExpression expression) {
        // The complete packability decision, in order (GEP-27 "The packability decision procedure";
        // any decline keeps the closure a generated class, exactly as today):
        // 1. needs real Closure semantics (owner/delegate/thisObject/resolveStrategy/metaClass/
        //    super, default parameter values, anonymous inner class)?
        if (!isPackable(expression)) return PackStrategy.FULL_CLASS;
        // 2. scope opted out? The most-specific @PackedClosures mode wins (method over class), and
        //    DISABLED beats an enclosing opt-in AND the flag.
        PackMode mode = annotatedMode();
        if (mode == PackMode.DISABLED) return PackStrategy.FULL_CLASS;
        boolean annotated = (mode != null); // LENIENT/WARN/STRICT all opt in (DISABLED handled above)
        // 3. triggered and sound? annotation (dynamic trust), flag + syntactic no-free-name proof
        //    (dynamic), or flag/annotation + the type checker's delegate-independence proof (CS,
        //    which also declines mixed-mode dynamic islands and Map-owner property semantics).
        boolean open = false;
        if (!isPackTriggered(expression, annotated)) {
            // GEP-27 OpenClosure spike (Groovy 7 reference implementation): a dynamic closure whose
            // only obstacle is free names takes the open path -- the free uses are rewritten into
            // calls on a leading Resolver parameter and a ClassicResolver restores the full
            // owner/delegate/resolveStrategy contract. packedClosureUsesDelegateGuard() limits this
            // to the dynamic writer (the CS writer proves or declines instead).
            open = packedClosureUsesDelegateGuard()
                    && SystemUtil.getBooleanSafe("groovy.spike.openclosure")
                    && isOpenRewritable(expression);
            if (!open) return PackStrategy.FULL_CLASS;
        }
        // 4. structural position: directly in a method of the owner class (nested closures await
        //    owner-retargeting)?
        if (controller.isInGeneratedFunction()) return PackStrategy.FULL_CLASS;
        // 5. visibly escapes (field/property/index store, return, <<, collection literal)?
        if (escapesEnclosingMethod(expression)) return PackStrategy.FULL_CLASS;
        // 6. visibly serialization-bound (Serializable cast/coercion, writeObject directly or via
        //    a literal-holding local)?
        if (serializationBound(expression)) return PackStrategy.FULL_CLASS;
        // 7. a field initializer (a field store the escape walk cannot see)?
        if (isFieldInitializer(expression)) return PackStrategy.FULL_CLASS;
        // 8.-10. a context the adapter cannot inhabit: intersection cast (per-literal marker
        //    interfaces), trait body ($Trait$Helper's synthetic receiver), or a this(...)/super(...)
        //    argument (uninitializedThis cannot be the dispatch receiver)?
        if (isIntersectionTyped(expression)) return PackStrategy.FULL_CLASS;
        if (isInTraitContext()) return PackStrategy.FULL_CLASS;
        if (controller.getCompileStack().isInSpecialConstructorCall()) return PackStrategy.FULL_CLASS;
        return open ? PackStrategy.OPEN_ADAPTER : PackStrategy.PACKED_ADAPTER;
    }

    /**
     * A closure in a field initializer ({@code def action = { ... }} at class level) is stored
     * into a field by definition — the same escape the escape gate declines for in-method stores —
     * but field initializers compile outside the enclosing method's visible code, so the escape
     * walk cannot see them. Detected directly against the class's fields (identity match).
     */
    private boolean isFieldInitializer(final ClosureExpression expression) {
        for (FieldNode fn : controller.getClassNode().getFields()) {
            if (fn.getInitialValueExpression() == expression) return true;
        }
        return false;
    }

    /**
     * A closure literal cast to an intersection type (e.g. {@code (Runnable & Serializable) { }})
     * needs its <em>generated class</em> to declare the marker interfaces (see
     * {@code StaticTypesClosureWriter#addIntersectionMarkers}), which the one shared
     * {@code PackedClosure} adapter cannot do per-literal — so keep it a class. The marker list is
     * recorded by the type checker; a non-empty one is the signal.
     */
    private static boolean isIntersectionTyped(final ClosureExpression expression) {
        Object markers = expression.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.LAMBDA_MARKERS);
        return markers instanceof List && !((List<?>) markers).isEmpty();
    }

    /**
     * Whether the closure is being compiled inside a trait. A trait's method bodies (and their
     * closures) are moved into a static {@code $Trait$Helper} nested class whose {@code this} is a
     * synthetic {@code $self} parameter, not an instance receiver — a shape the packed dispatch
     * codegen does not reproduce (it produced invalid bytecode). Decline and keep the closure as a
     * class until the trait context is supported. Detected by walking the enclosing class's outer
     * chain to the {@code @Trait}-annotated interface.
     */
    private boolean isInTraitContext() {
        for (ClassNode c = controller.getClassNode(); c != null; c = c.getOuterClass()) {
            if (org.codehaus.groovy.transform.trait.Traits.isTrait(c)) return true;
        }
        return false;
    }

    /**
     * Whether a packable, non-escaping closure literal should actually be packed. Dynamic compilation
     * has no proof of delegate-independence, so the only trigger is the {@code @PackedClosures} trust
     * assertion; {@code StaticTypesClosureWriter} overrides this to add the
     * {@code groovy.target.closure.pack} flag and the delegate-independence proof.
     *
     * @param annotated whether a non-DISABLED {@code @PackedClosures} is in scope
     */
    protected boolean isPackTriggered(final ClosureExpression expression, final boolean annotated) {
        if (annotated) return true;
        // Dynamic compilation has no delegate-independence proof from the type checker, so the
        // annotation is normally the only trigger. But a closure with NO free names -- no
        // implicit-this call, no unqualified/dynamic variable -- cannot be affected by any
        // caller-set delegate at all, so it is delegate-independent by syntax alone, no types
        // needed. The flag auto-packs exactly that provably-safe subset (GROOVY-12151 dynamic
        // syntactic path); a set delegate on such a closure is a harmless no-op, so it needs no
        // strict runtime guard (marked here for writePackedClosure).
        if (SystemUtil.getBooleanSafe("groovy.target.closure.pack") && isSyntacticallyDelegateIndependent(expression)) {
            expression.putNodeMetaData(SYNTACTIC_PACK, Boolean.TRUE);
            return true;
        }
        return false;
    }

    /** Metadata marking a closure packed because it is syntactically delegate-independent (no strict guard). */
    private static final String SYNTACTIC_PACK = "org.codehaus.groovy.classgen.asm.ClosureWriter.syntacticPack";

    /**
     * Whether the closure's body contains no name a caller-set delegate could intercept — no
     * implicit-this method call or property access, and no unqualified/dynamic variable (a free
     * name the runtime would resolve through the owner/delegate chain). Only parameters, locals,
     * captured variables, constants and explicit/parameter receivers remain, none of which a
     * delegate can touch, so such a closure is delegate-independent by construction regardless of
     * types. Nested closures are walked too (a free name anywhere carries the delegate chain).
     * Conservative: any doubt returns {@code false} (keep the closure as a class).
     */
    private boolean isSyntacticallyDelegateIndependent(final ClosureExpression expression) {
        Statement code = expression.getCode();
        if (code == null) return false;
        boolean[] dependent = {false};
        code.visit(new CodeVisitorSupport() {
            @Override public void visitMethodCallExpression(final MethodCallExpression call) {
                if (call.isImplicitThis()) dependent[0] = true; // foo() -> could be a delegate method
                super.visitMethodCallExpression(call);
            }
            @Override public void visitPropertyExpression(final PropertyExpression pexp) {
                // implicit-this (bar) could be a delegate property; explicit this.bar inside a
                // dynamic closure routes through the MOP (getProperty -- where Map-owner entry
                // semantics and metaclass interception live), which a hoisted body's direct
                // field shortcut would bypass
                Expression obj = pexp.getObjectExpression();
                if (pexp.isImplicitThis()
                        || (obj instanceof VariableExpression && ((VariableExpression) obj).isThisExpression())) {
                    dependent[0] = true;
                }
                super.visitPropertyExpression(pexp);
            }
            @Override public void visitVariableExpression(final VariableExpression ve) {
                // only a parameter/local binding is safe: a bare name bound to an owner FIELD or
                // PROPERTY by VariableScopeVisitor is still a free name at runtime -- a dynamic
                // closure resolves it through the delegate chain first under DELEGATE_FIRST, and
                // through the MOP (e.g. an ExpandoMetaClass property) -- as is a DynamicVariable
                Variable av = ve.getAccessedVariable();
                if (av instanceof DynamicVariable || av instanceof FieldNode || av instanceof PropertyNode) {
                    dependent[0] = true;
                }
                super.visitVariableExpression(ve);
            }
        });
        return !dependent[0];
    }

    /**
     * A trigger-specific decline reason for {@link #declineReason}, or {@code null} if the trigger did
     * not decline this closure. Only the static writer has one (a delegate-resolved body); the dynamic
     * path trusts the annotation and never declines on trigger grounds.
     */
    protected String triggerDeclineReason(final ClosureExpression expression) {
        return null;
    }

    /** Whether the hoisted body is compiled statically (true only for {@code @CompileStatic}). */
    protected boolean compilesHoistedBodyStatically() {
        return false;
    }

    /**
     * Whether the packed adapter installs the runtime delegate guard. The dynamic trust path needs it
     * (an unverifiable assertion must fail fast on misuse); the static writer proved independence, so
     * it overrides this to {@code false} and a caller-set delegate is then stored and ignored.
     */
    protected boolean packedClosureUsesDelegateGuard() {
        return true;
    }

    /** The closure's inferred return type, normalised the same way {@code createClosureClass} does for doCall. */
    private static ClassNode inferredClosureReturnType(final ClosureExpression expression) {
        ClassNode returnType = expression.getNodeMetaData(INFERRED_RETURN_TYPE);
        if (returnType == null) returnType = ClassHelper.OBJECT_TYPE;
        else if (returnType.isPrimaryClassNode()) returnType = returnType.getPlainNodeReference();
        else if (ClassHelper.isPrimitiveType(returnType)) returnType = ClassHelper.getWrapper(returnType);
        else if (GenericsUtils.hasUnresolvedGenerics(returnType)) returnType = GenericsUtils.nonGeneric(returnType);
        return returnType;
    }

    /**
     * The erasure of a captured variable's type, safe to declare on the hoisted method: generic
     * placeholders are replaced by their bound (the hoisted method does not declare the enclosing
     * method's type variables) and type arguments are dropped ({@code List<T>} → {@code List}).
     */
    private static ClassNode erasedType(final ClassNode type) {
        ClassNode t = type;
        if (t == null) return ClassHelper.OBJECT_TYPE;
        if (t.isGenericsPlaceHolder()) t = t.redirect();
        return t.getPlainNodeReference();
    }

    /**
     * Reports a top-level closure that was NOT packed, per the {@code mode} of the enclosing
     * {@code @PackedClosures} annotation (WARN => compiler warning, STRICT => compiler error). The
     * automatic {@code groovy.target.closure.pack} path is lenient by default, but setting
     * {@code groovy.target.closure.pack.report=true} alongside it surfaces every decline (with its
     * reason) as a warning — the operational way to see where the packability boundary falls in a
     * real codebase before opting scopes in or out. A nested closure is a structural decline (its
     * enclosing context is a generated function, not the owner class), so it is not reported.
     */
    private void reportUnpacked(final ClosureExpression expression) {
        if (controller.isInGeneratedFunction()) return;
        PackMode mode = annotatedMode();
        if (mode == null) {
            if (SystemUtil.getBooleanSafe("groovy.target.closure.pack")
                    && SystemUtil.getBooleanSafe("groovy.target.closure.pack.report")) {
                controller.getSourceUnit().addWarning(WarningMessage.LIKELY_ERRORS,
                        "closure packing: closure was not packed -- " + declineReason(expression), expression);
            }
            return;
        }
        // LENIENT/DISABLED are both silent -- DISABLED asked NOT to pack, so a decline is expected
        if (mode == PackMode.LENIENT || mode == PackMode.DISABLED) return;
        String msg = "@PackedClosures: closure was not packed -- " + declineReason(expression);
        if (mode == PackMode.STRICT) {
            controller.getSourceUnit().getErrorCollector()
                    .addErrorAndContinue(msg, expression, controller.getSourceUnit());
        } else {
            // WARN is opt-in, so emit at LIKELY_ERRORS to show at the default warning level (the plain
            // addWarning(text, node) uses POSSIBLE_ERRORS, which the default level suppresses). groovyc
            // surfaces collected warnings on success since GROOVY-12132; Gradle/IDEs already do.
            controller.getSourceUnit().addWarning(WarningMessage.LIKELY_ERRORS, msg, expression);
        }
    }

    /** The most specific {@code @PackedClosures} mode in scope (method wins over class), or null if not annotated. */
    private PackMode annotatedMode() {
        MethodNode m = controller.getMethodNode();
        PackMode mode = (m != null) ? packModeOf(m.getAnnotations()) : null;
        if (mode != null) return mode;
        for (ClassNode c = controller.getClassNode(); c != null; c = c.getOuterClass()) {
            mode = packModeOf(c.getAnnotations());
            if (mode != null) return mode;
        }
        return null;
    }

    /** The mode of a {@code @PackedClosures} in the given set (LENIENT if present without an explicit mode), or null. */
    private static PackMode packModeOf(final List<AnnotationNode> annotations) {
        for (AnnotationNode a : annotations) {
            if (a.getClassNode() != null && "groovy.transform.PackedClosures".equals(a.getClassNode().getName())) {
                Expression member = a.getMember("mode");
                if (member instanceof PropertyExpression) {
                    try {
                        return PackMode.valueOf(((PropertyExpression) member).getPropertyAsString());
                    } catch (IllegalArgumentException ignore) {
                        // malformed member; treat as the default
                    }
                }
                return PackMode.LENIENT;
            }
        }
        return null;
    }

    /** Why {@link #chooseStrategy} declined this (packable, top-level) closure -- the message WARN/STRICT report. */
    private String declineReason(final ClosureExpression expression) {
        if (!isPackable(expression)) {
            return "it needs a real Closure (uses owner/delegate/thisObject/resolveStrategy/super or metaClass, "
                    + "has default parameter values, or contains an anonymous inner class)";
        }
        String triggerReason = triggerDeclineReason(expression);
        if (triggerReason != null) return triggerReason;
        if (escapesEnclosingMethod(expression)) {
            return "it escapes its method (returned, stored to a field/property/index, appended, or in a collection literal)";
        }
        if (serializationBound(expression)) {
            return "it is visibly serialization-bound (cast or coerced to a Serializable type, or passed directly "
                    + "to writeObject) and packed closures are not serializable";
        }
        if (isFieldInitializer(expression)) {
            return "it initialises a field (a visible escape: the closure outlives its construction context)";
        }
        if (isIntersectionTyped(expression)) {
            return "it is cast to an intersection type, whose marker interfaces the shared adapter cannot declare";
        }
        if (isInTraitContext()) {
            return "it is declared inside a trait, whose helper-class context packed dispatch does not yet support";
        }
        if (controller.getCompileStack().isInSpecialConstructorCall()) {
            return "it is an argument to a this(...)/super(...) constructor call, where the enclosing instance "
                    + "is not yet initialised, so it cannot be the packed adapter's owner";
        }
        return "it is not eligible for packing in this scope";
    }

    /**
     * Conservative compile-time escape gate. A packed closure is bound to the owner and backed by a
     * shared adapter that fails fast if a delegate is later set on it. To avoid that surprise for the
     * clearest cases, decline (keep a real closure class for) a closure literal that visibly escapes
     * the method it is written in: stored into a field/property/index (e.g. {@code attrs.optionValue =
     * { ... }}), returned, appended with {@code <<}, or placed in a list/map literal. Transitive escapes
     * (via a local that is later returned) are intentionally not chased here; the runtime guard remains
     * the backstop for those.
     */
    private boolean escapesEnclosingMethod(final ClosureExpression expression) {
        MethodNode m = controller.getMethodNode();
        if (m == null || m.getCode() == null) return false;
        return escapingClosuresByMethod
                .computeIfAbsent(m, k -> collectEscapingClosures(k.getCode()))
                .contains(expression);
    }

    private static Set<ClosureExpression> collectEscapingClosures(final Statement code) {
        Set<ClosureExpression> escaping = Collections.newSetFromMap(new IdentityHashMap<>());
        code.visit(new CodeVisitorSupport() {
            private void mark(final Expression e) {
                if (e instanceof ClosureExpression) escaping.add((ClosureExpression) e);
            }
            @Override public void visitReturnStatement(final ReturnStatement statement) {
                mark(statement.getExpression());
                super.visitReturnStatement(statement);
            }
            @Override public void visitBinaryExpression(final BinaryExpression be) {
                int op = be.getOperation().getType();
                if (op == Types.EQUAL && !isLocalTarget(be.getLeftExpression())) {
                    mark(be.getRightExpression());          // assigned to a field/property/index
                } else if (op == Types.LEFT_SHIFT) {
                    mark(be.getRightExpression());          // appended, e.g. list << { ... }
                }
                super.visitBinaryExpression(be);
            }
            @Override public void visitListExpression(final ListExpression le) {
                le.getExpressions().forEach(this::mark);
                super.visitListExpression(le);
            }
            @Override public void visitMapEntryExpression(final MapEntryExpression me) {
                mark(me.getValueExpression());
                super.visitMapEntryExpression(me);
            }
        });
        return escaping;
    }

    /** True when an assignment target is a plain local variable (not a field/property that escapes). */
    private static boolean isLocalTarget(final Expression lhs) {
        if (!(lhs instanceof VariableExpression)) return false;   // property/field/index target
        Variable v = ((VariableExpression) lhs).getAccessedVariable();
        return !(v instanceof FieldNode) && !(v instanceof PropertyNode);
    }

    /**
     * Conservative compile-time serialization gate, the same shape as the escape gate: a packed
     * closure is not serializable, so decline (keep a real closure class for) a closure literal
     * that is <em>visibly</em> serialization-bound — cast or coerced ({@code as}) to a type that is
     * or implements {@code Serializable} (including serializable SAM coercions, whose proxy carries
     * the closure), or passed directly as an argument to a {@code writeObject} call. Transitive
     * routes (a local later serialized, a callee that serializes its argument) are intentionally
     * not chased; the adapter's fail-fast {@code writeObject} remains the backstop for those.
     * Declines are reported by {@code @PackedClosures(mode = WARN | STRICT)} like any other.
     */
    private boolean serializationBound(final ClosureExpression expression) {
        MethodNode m = controller.getMethodNode();
        if (m == null || m.getCode() == null) return false;
        return serializationBoundByMethod
                .computeIfAbsent(m, k -> collectSerializationBoundClosures(k.getCode()))
                .contains(expression);
    }

    private static Set<ClosureExpression> collectSerializationBoundClosures(final Statement code) {
        Set<ClosureExpression> bound = Collections.newSetFromMap(new IdentityHashMap<>());
        // pass 1: locals directly assigned a closure literal, so pass 2 can follow the canonical
        // one-hop shape `def c = { ... }; out.writeObject(c)` (a local reassigned a second literal
        // maps to both -- conservative: either literal declines)
        Map<String, List<ClosureExpression>> literalsByLocal = new HashMap<>();
        code.visit(new CodeVisitorSupport() {
            @Override public void visitBinaryExpression(final BinaryExpression be) {
                if (Types.isAssignment(be.getOperation().getType())
                        && be.getLeftExpression() instanceof VariableExpression
                        && isLocalTarget(be.getLeftExpression())
                        && be.getRightExpression() instanceof ClosureExpression) {
                    literalsByLocal.computeIfAbsent(((VariableExpression) be.getLeftExpression()).getName(),
                            k -> new ArrayList<>()).add((ClosureExpression) be.getRightExpression());
                }
                super.visitBinaryExpression(be);
            }
        });
        code.visit(new CodeVisitorSupport() {
            private void mark(final Expression e) {
                if (e instanceof ClosureExpression) {
                    bound.add((ClosureExpression) e);
                } else if (e instanceof VariableExpression) {
                    List<ClosureExpression> held = literalsByLocal.get(((VariableExpression) e).getName());
                    if (held != null) bound.addAll(held);
                }
            }
            @Override public void visitCastExpression(final CastExpression ce) {
                ClassNode target = ce.getType();
                if (ClassHelper.SERIALIZABLE_TYPE.equals(target) || target.implementsInterface(ClassHelper.SERIALIZABLE_TYPE)) {
                    mark(ce.getExpression());
                }
                super.visitCastExpression(ce);
            }
            @Override public void visitMethodCallExpression(final MethodCallExpression mce) {
                if ("writeObject".equals(mce.getMethodAsString()) && mce.getArguments() instanceof TupleExpression) {
                    ((TupleExpression) mce.getArguments()).getExpressions().forEach(this::mark);
                }
                super.visitMethodCallExpression(mce);
            }
        });
        return bound;
    }

    /**
     * Spike packability gate: pack read-only closures (including nested and captures). Captured-variable
     * writes are supported via Reference threading for a flat closure only (declined if the body also
     * contains a nested closure, or uses an unsupported compound-assignment operator). Anything needing a
     * real Closure — delegate/owner/thisObject/resolveStrategy or {@code super} — is declined.
     */
    private static boolean isPackable(final ClosureExpression expression) {
        Statement code = expression.getCode();
        if (code == null) return false;
        // Default parameter values generate arity-overloaded doCall methods on a closure class;
        // the single hoisted method cannot reproduce that dispatch, so decline.
        if (expression.isParameterSpecified()) {
            for (Parameter p : expression.getParameters()) {
                if (p.hasInitialExpression()) return false;
            }
        }
        boolean[] ok = {true};
        code.visit(new CodeVisitorSupport() {
            @Override public void visitClosureExpression(final ClosureExpression e) {
                // A nested closure constructed inside the hoisted method gets the enclosing INSTANCE
                // as its owner instead of the (packed) outer closure object. That is observationally
                // equivalent for owner-chain resolution, but not for a nested body that names
                // owner/delegate/thisObject explicitly -- so the outer packs only if every nested
                // closure is itself packable.
                if (!isPackable(e)) ok[0] = false;
            }
            @Override public void visitVariableExpression(final VariableExpression ve) {
                if (ve.isSuperExpression() || FORBIDDEN_CLOSURE_NAMES.contains(ve.getName())) ok[0] = false;
            }
            @Override public void visitMethodCallExpression(final MethodCallExpression call) {
                // implicit-this accessor/mutator forms of the same pseudo-properties, e.g. getDelegate()
                if (call.isImplicitThis() && FORBIDDEN_CLOSURE_CALLS.contains(call.getMethodAsString())) ok[0] = false;
                super.visitMethodCallExpression(call);
            }
            @Override public void visitConstructorCallExpression(final ConstructorCallExpression cce) {
                // an anonymous inner class in the body is generated against the enclosing closure
                // class (its outer instance); hoisting would hand it the wrong enclosing instance
                if (cce.isUsingAnonymousInnerClass()) ok[0] = false;
                super.visitConstructorCallExpression(cce);
            }
        });
        // All write forms to captured variables are supported: the shared Reference is passed as a
        // holder parameter, so the ASM generator emits the implicit get()/set() exactly as it does
        // for a closure class -- no operator restrictions.
        return ok[0];
    }

    private static Set<String> capturedNames(final ClosureExpression expression) {
        Set<String> captured = new HashSet<>();
        VariableScope vs = expression.getVariableScope();
        if (vs != null) {
            for (Iterator<Variable> it = vs.getReferencedLocalVariablesIterator(); it.hasNext(); ) {
                captured.add(it.next().getName());
            }
        }
        return captured;
    }

    /**
     * Captured variables that must be threaded as the shared {@code groovy.lang.Reference} (holder)
     * rather than by value: those written (reassigned) anywhere in the enclosing method, and those a
     * <em>nested</em> closure inside the body also captures. The nested case is a bytecode-shape
     * requirement, not a mutation one: a nested closure that compiles as a class takes the shared
     * Reference in its constructor (every captured local is Reference-boxed in the class world), so a
     * by-value slot would fail verification there; a nested closure that itself packs simply
     * dereferences the holder. The enclosing frame always has the Reference to pass — any local
     * captured by a closure is boxed in its declaring frame.
     */
    private Set<String> holderCaptureNames(final ClosureExpression expression) {
        Set<String> captured = capturedNames(expression);
        Set<String> holder = new HashSet<>(collectWrittenNames(expression.getCode()));
        MethodNode m = controller.getMethodNode();
        if (m != null && m.getCode() != null) {
            holder.addAll(writtenNamesByMethod.computeIfAbsent(m, k -> collectWrittenNames(k.getCode())));
        }
        Statement code = expression.getCode();
        if (code != null) {
            code.visit(new CodeVisitorSupport() {
                @Override public void visitClosureExpression(final ClosureExpression nested) {
                    VariableScope nvs = nested.getVariableScope();
                    if (nvs != null) {
                        for (Iterator<Variable> it = nvs.getReferencedLocalVariablesIterator(); it.hasNext(); ) {
                            holder.add(it.next().getName());
                        }
                    }
                    super.visitClosureExpression(nested);
                }
            });
        }
        holder.retainAll(captured);
        return holder;
    }

    /** Names assigned or incremented in the given code (declarations with initializers excluded). */
    private static Set<String> collectWrittenNames(final Statement code) {
        Set<String> written = new HashSet<>();
        if (code == null) return written;
        code.visit(new CodeVisitorSupport() { // descends into nested closures
            @Override public void visitBinaryExpression(final BinaryExpression be) {
                if (Types.isAssignment(be.getOperation().getType())
                        && !(be instanceof org.codehaus.groovy.ast.expr.DeclarationExpression)
                        && be.getLeftExpression() instanceof VariableExpression) {
                    written.add(((VariableExpression) be.getLeftExpression()).getName());
                }
                super.visitBinaryExpression(be);
            }
            @Override public void visitPostfixExpression(final PostfixExpression pe) {
                recordIncrement(pe.getExpression());
                super.visitPostfixExpression(pe);
            }
            @Override public void visitPrefixExpression(final PrefixExpression pe) {
                recordIncrement(pe.getExpression());
                super.visitPrefixExpression(pe);
            }
            private void recordIncrement(final Expression operand) {
                if (operand instanceof VariableExpression) {
                    written.add(((VariableExpression) operand).getName());
                }
            }
        });
        return written;
    }

    // NB: metaClass resolves against the closure object ITSELF (per-closure-class identity), which
    // the shared adapter cannot reproduce, so it declines like the other closure pseudo-properties.
    private static final Set<String> FORBIDDEN_CLOSURE_NAMES =
            new HashSet<>(java.util.Arrays.asList("owner", "delegate", "thisObject", "directive",
                    "resolveStrategy", "metaClass"));

    /** Accessor/mutator forms of the same pseudo-properties, called with implicit this. */
    private static final Set<String> FORBIDDEN_CLOSURE_CALLS =
            new HashSet<>(java.util.Arrays.asList("getOwner", "getDelegate", "getThisObject", "getDirective",
                    "getResolveStrategy", "setDelegate", "setDirective", "setResolveStrategy",
                    "getMaximumNumberOfParameters", "getParameterTypes", "getMetaClass", "setMetaClass",
                    "invokeMethod", "getProperty", "setProperty"));

    /**
     * Rebinds captured-variable references in the moved body to the hoisted method's parameters,
     * matched by the accessed {@link Variable}'s <em>identity</em> rather than by name. Groovy forbids
     * shadowing an in-scope local/parameter, so a name match would be correct today; keying on identity
     * keeps it correct even if that scoping rule ever relaxed, and never rewrites an unrelated binding
     * that happens to share a captured name.
     */
    private static void rebindCaptured(final Statement body, final Map<Variable, Parameter> capturedParams) {
        body.visit(new CodeVisitorSupport() {
            @Override public void visitVariableExpression(final VariableExpression ve) {
                Parameter p = capturedParams.get(ve.getAccessedVariable());
                if (p != null) {
                    ve.setAccessedVariable(p);
                    // plain read-only captures become by-value params; holder-threaded captures
                    // (written, or re-captured by a nested closure) stay closure-shared so the
                    // ASM generator routes them through the Reference
                    ve.setClosureSharedVariable(p.isClosureSharedVariable());
                }
            }
        });
    }

    /**
     * Emits a packed closure: hoists the body to a synthetic method on the enclosing class (captured
     * variables become leading, by-value parameters) and constructs a shared {@code PackedClosure}
     * adapter that dispatches to it — no inner class.
     */
    // ---- GEP-27 OpenClosure spike (Groovy 7 reference implementation) ----------------------------

    private static final String RESOLVER_PARAM = "$resolver";
    private static final ClassNode RESOLVER_TYPE =
            ClassHelper.make(org.codehaus.groovy.runtime.OpenClosure.Resolver.class);
    private static final String CLASSIC_RESOLVER_INTERNAL = "org/codehaus/groovy/runtime/OpenClosure$ClassicResolver";

    /**
     * Whether every free-name use in the body can be rewritten into a call on the leading
     * {@code Resolver} parameter: free reads ({@code bar}) and unqualified calls ({@code foo(x)})
     * qualify; writes to free names, {@code this}-qualified accesses, spreads, method pointers on
     * {@code this}, and nested closures decline (spike scope). Requires at least one free use —
     * otherwise the closure is the ordinary packed case.
     */
    private static boolean isOpenRewritable(final ClosureExpression expression) {
        Statement code = expression.getCode();
        if (code == null) return false;
        boolean[] free = {false};
        boolean[] blocked = {false};
        code.visit(new CodeVisitorSupport() {
            private boolean isFreeName(final Expression e) {
                if (!(e instanceof VariableExpression)) return false;
                Variable av = ((VariableExpression) e).getAccessedVariable();
                return av instanceof DynamicVariable || av instanceof FieldNode || av instanceof PropertyNode;
            }
            @Override public void visitClosureExpression(final ClosureExpression nested) {
                blocked[0] = true; // nested literals keep today's treatment (spike scope)
            }
            @Override public void visitMethodCallExpression(final MethodCallExpression call) {
                if (call.isImplicitThis()) {
                    if (!(call.getMethod() instanceof ConstantExpression)) blocked[0] = true;
                    else free[0] = true;
                }
                super.visitMethodCallExpression(call);
            }
            @Override public void visitVariableExpression(final VariableExpression ve) {
                if (isFreeName(ve)) free[0] = true;
                super.visitVariableExpression(ve);
            }
            @Override public void visitBinaryExpression(final BinaryExpression be) {
                if (Types.isAssignment(be.getOperation().getType()) && isFreeName(be.getLeftExpression())) {
                    free[0] = true; // rewritten to resolver.setProperty (compound: read-then-write)
                }
                super.visitBinaryExpression(be);
            }
            @Override public void visitPostfixExpression(final PostfixExpression pe) {
                if (isFreeName(pe.getExpression())) blocked[0] = true;
                super.visitPostfixExpression(pe);
            }
            @Override public void visitPrefixExpression(final PrefixExpression pe) {
                if (isFreeName(pe.getExpression())) blocked[0] = true;
                super.visitPrefixExpression(pe);
            }
            @Override public void visitPropertyExpression(final PropertyExpression pexp) {
                Expression obj = pexp.getObjectExpression();
                if (pexp.isImplicitThis()
                        || (obj instanceof VariableExpression && ((VariableExpression) obj).isThisExpression())) {
                    blocked[0] = true; // this-qualified property semantics: not in the spike
                }
                super.visitPropertyExpression(pexp);
            }
            @Override public void visitSpreadExpression(final org.codehaus.groovy.ast.expr.SpreadExpression se) {
                blocked[0] = true;
                super.visitSpreadExpression(se);
            }
            @Override public void visitMethodPointerExpression(final org.codehaus.groovy.ast.expr.MethodPointerExpression mpe) {
                Expression obj = mpe.getExpression();
                if (obj instanceof VariableExpression && ((VariableExpression) obj).isThisExpression()) blocked[0] = true;
                super.visitMethodPointerExpression(mpe);
            }
        });
        return free[0] && !blocked[0];
    }

    /**
     * Rewrites the hoisted body's free-name uses into resolver calls: a free read {@code bar}
     * becomes {@code $resolver.property('bar')} and an unqualified call {@code foo(a, b)} becomes
     * {@code $resolver.invoke('foo', new Object[]{a, b})} — the single boundary through which the
     * dynamic world reaches an open body.
     */
    private static final class OpenBodyRewriter extends org.codehaus.groovy.ast.ClassCodeExpressionTransformer {
        private final org.codehaus.groovy.control.SourceUnit sourceUnit;
        private final Parameter resolverParam;

        OpenBodyRewriter(final org.codehaus.groovy.control.SourceUnit sourceUnit, final Parameter resolverParam) {
            this.sourceUnit = sourceUnit;
            this.resolverParam = resolverParam;
        }

        @Override
        protected org.codehaus.groovy.control.SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        private static boolean isFreeTarget(final Expression e) {
            if (!(e instanceof VariableExpression)) return false;
            Variable av = ((VariableExpression) e).getAccessedVariable();
            return av instanceof DynamicVariable || av instanceof FieldNode || av instanceof PropertyNode;
        }

        @Override
        public Expression transform(final Expression exp) {
            if (exp instanceof BinaryExpression
                    && Types.isAssignment(((BinaryExpression) exp).getOperation().getType())
                    && isFreeTarget(((BinaryExpression) exp).getLeftExpression())) {
                BinaryExpression be = (BinaryExpression) exp;
                String name = ((VariableExpression) be.getLeftExpression()).getName();
                Expression rhs = transform(be.getRightExpression());
                int op = be.getOperation().getType();
                Expression value;
                if (op == Types.EQUAL) {
                    value = rhs;
                } else {
                    // bar OP= x  =>  setProperty('bar', property('bar') OP x) — the same
                    // read-then-write MOP pair a classic compound assignment performs
                    MethodCallExpression read = new MethodCallExpression(
                            new VariableExpression(resolverParam), "property",
                            new ArgumentListExpression(new ConstantExpression(name)));
                    read.setImplicitThis(false);
                    read.setSourcePosition(be.getLeftExpression());
                    value = new BinaryExpression(read,
                            org.codehaus.groovy.syntax.Token.newSymbol(
                                    org.codehaus.groovy.syntax.TokenUtil.removeAssignment(op),
                                    be.getOperation().getStartLine(), be.getOperation().getStartColumn()),
                            rhs);
                    value.setSourcePosition(be);
                }
                MethodCallExpression set = new MethodCallExpression(
                        new VariableExpression(resolverParam), "setProperty",
                        new ArgumentListExpression(new ConstantExpression(name), value));
                set.setImplicitThis(false);
                set.setSourcePosition(exp);
                return set;
            }
            if (exp instanceof VariableExpression) {
                Variable av = ((VariableExpression) exp).getAccessedVariable();
                if (av instanceof DynamicVariable || av instanceof FieldNode || av instanceof PropertyNode) {
                    MethodCallExpression get = new MethodCallExpression(
                            new VariableExpression(resolverParam), "property",
                            new ArgumentListExpression(new ConstantExpression(((VariableExpression) exp).getName())));
                    get.setImplicitThis(false);
                    get.setSourcePosition(exp);
                    return get;
                }
                return exp;
            }
            if (exp instanceof MethodCallExpression && ((MethodCallExpression) exp).isImplicitThis()
                    && ((MethodCallExpression) exp).getMethod() instanceof ConstantExpression) {
                MethodCallExpression mce = (MethodCallExpression) exp;
                java.util.List<Expression> rewritten = new ArrayList<>();
                for (Expression arg : ((TupleExpression) mce.getArguments()).getExpressions()) {
                    rewritten.add(transform(arg));
                }
                MethodCallExpression call = new MethodCallExpression(
                        new VariableExpression(resolverParam), "invoke",
                        new ArgumentListExpression(
                                new ConstantExpression(mce.getMethodAsString()),
                                new ArrayExpression(ClassHelper.OBJECT_TYPE, rewritten)));
                call.setImplicitThis(false);
                call.setSourcePosition(exp);
                return call;
            }
            return exp.transformExpression(this);
        }
    }

    private void writePackedClosure(final ClosureExpression expression, final boolean open) {
        ClassNode enclosing = controller.getClassNode();
        MethodVisitor mv = controller.getMethodVisitor();
        AsmClassGenerator acg = controller.getAcg();
        OperandStack os = controller.getOperandStack();

        List<String> captured = new ArrayList<>();
        Map<String, ClassNode> capturedTypes = new HashMap<>();
        Map<String, Variable> capturedVars = new HashMap<>(); // name -> the original captured variable
        VariableScope vs = expression.getVariableScope();
        if (vs != null) {
            for (Iterator<Variable> it = vs.getReferencedLocalVariablesIterator(); it.hasNext(); ) {
                Variable v = it.next();
                captured.add(v.getName());
                capturedTypes.put(v.getName(), erasedType(v.getOriginType()));
                capturedVars.put(v.getName(), v);
            }
        }

        // An EMPTY parameter array is the implicit-it literal ({ ... }); null is the explicit
        // zero-parameter form ({ -> ... }), which must report arity 0 (GString's writer-vs-call
        // branch and SAM matching key on it), exactly as its generated class would.
        Parameter[] declared = expression.getParameters();
        boolean implicitParam = (declared != null && declared.length == 0);
        Parameter[] closureParams = implicitParam
                ? new Parameter[]{new Parameter(ClassHelper.OBJECT_TYPE, "it")}
                : (declared != null ? declared : Parameter.EMPTY_ARRAY);
        int arity = closureParams.length;

        Set<String> holderThreaded = holderCaptureNames(expression);
        // OpenClosure spike: the resolver is the hoisted body's leading parameter (and rides as
        // captured[0] at the creation site), so the whole dispatch pipeline is unchanged.
        int prefix = open ? 1 : 0;
        Parameter resolverParam = open ? new Parameter(RESOLVER_TYPE, RESOLVER_PARAM) : null;
        Parameter[] methodParams = new Parameter[prefix + captured.size() + closureParams.length];
        if (open) methodParams[0] = resolverParam;
        Map<Variable, Parameter> capturedByVar = new IdentityHashMap<>(); // original variable -> hoisted param
        boolean typedBody = compilesHoistedBodyStatically();
        for (int i = 0; i < captured.size(); i++) {
            String cn = captured.get(i);
            boolean isWritten = holderThreaded.contains(cn);
            Parameter p;
            if (isWritten) {
                // A written capture arrives as the SHARED groovy.lang.Reference itself: declare the
                // parameter exactly like a closure-class constructor does (originType = value type,
                // descriptor type = Reference, closure-shared + use-existing-reference), so
                // CompileStack.init registers it as a holder and the ASM generator emits the implicit
                // get()/set() on every read/write -- no AST rewriting, and the untouched body keeps
                // the STC metadata that lets it compile statically.
                ClassNode vt = capturedTypes.get(cn);
                if (ClassHelper.isPrimitiveType(vt)) vt = ClassHelper.getWrapper(vt);
                p = new Parameter(vt, cn);
                p.setType(ClassHelper.makeReference());
                p.setClosureSharedVariable(true);
                p.putNodeMetaData(UseExistingReference.class, Boolean.TRUE);
                // the static writer's type chooser must see the VALUE type: the holder load already
                // dereferences, so resolving this variable as Reference would add a bogus checkcast
                p.putNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE, vt);
            } else {
                // For a statically-compiled body, type the read-only capture from its declared type so
                // the loads match the STC metadata already on the body expressions (otherwise the
                // static writer falls back to dynamic dispatch on the type mismatch).
                // an untyped capture may carry a flow-inferred type STC recorded on the original
                // variable (e.g. an enclosing closure's inferred parameter, captured by a nested
                // literal whose method reference expects the narrowed type). DECLARE the hoisted
                // parameter by it: the dispatch table's case checkcasts to declared parameter
                // types, so the body's load then carries the type the use sites were compiled
                // against — where a plain Object parameter loads unverifiably wide (the classed
                // emission gets the equivalent cast on its shared-Reference dereference).
                ClassNode capturedType = typedBody ? capturedTypes.get(cn) : ClassHelper.OBJECT_TYPE;
                Variable v = capturedVars.get(cn);
                if (typedBody && v instanceof org.codehaus.groovy.ast.ASTNode) {
                    Object inferred = ((org.codehaus.groovy.ast.ASTNode) v)
                            .getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE);
                    if (inferred instanceof ClassNode) {
                        capturedType = erasedType((ClassNode) inferred);
                    }
                }
                p = new Parameter(capturedType, cn);
            }
            methodParams[prefix + i] = p;
            capturedByVar.put(capturedVars.get(cn), p);
        }
        System.arraycopy(closureParams, 0, methodParams, prefix + captured.size(), closureParams.length);

        Statement body = expression.getCode();
        rebindCaptured(body, capturedByVar);  // read-only -> plain param; written -> holder param

        // In a static method there is no `this`: the owner is the enclosing class and the hoisted
        // method must be static (loaded/dispatched against the class, not local slot 0).
        boolean staticContext = controller.isStaticMethod();
        List<MethodNode> targets = packedTargets(enclosing);
        int id = targets.size(); // ids are dense per class: the dispatcher emits a tableswitch over them
        String name = PACKED_METHOD_PREFIX + id;
        // PRIVATE, not public: the body is reached only through the class's $packedDispatch$ table
        // (a tableswitch case invoking it directly via INVOKESPECIAL/INVOKESTATIC), and a private
        // method is invoked exactly (no virtual dispatch), so an inherited packed closure never
        // dispatches to a same-named hoisted method a subclass happens to declare (GROOVY-12151 review).
        int mods = ACC_PRIVATE | ACC_SYNTHETIC | (staticContext ? ACC_STATIC : 0);
        // Same return type a generated closure class would give doCall, so implicit return-value
        // coercion (e.g. GString -> String for a Closure<String>) happens identically. This uses
        // whatever the type checker recorded (@CompileStatic AND @TypeChecked -- the dynamically
        // compiled body coerces at return like any declared-return dynamic method), falling back
        // to Object when no inference ran.
        ClassNode hoistedReturnType = inferredClosureReturnType(expression);
        MethodNode hoisted = new MethodNode(name, mods, hoistedReturnType,
                methodParams, ClassNode.EMPTY_ARRAY, body);
        if (open) {
            // rewrite free-name uses into resolver calls; everything lexical is already bound
            new OpenBodyRewriter(controller.getSourceUnit(), resolverParam).visitMethod(hoisted);
        }
        // Added after the ReturnAdder phase, so run it ourselves: it wires implicit returns through
        // every statement shape (trailing expressions, if/else branches, try/catch, ...).
        new org.codehaus.groovy.classgen.ReturnAdder().visitMethod(hoisted);
        // Typed static dispatch (GEP-27 phase 4): the original body expressions were already visited
        // by StaticCompilationVisitor as part of the enclosing method, so they carry the metadata the
        // static writer needs (DIRECT_METHOD_CALL_TARGET, inferred types) -- including types that were
        // only inferred (@ClosureParams, implicit it), which arrive for free via checkcasts. Written
        // captures ride the holder machinery, so their bodies compile statically too.
        hoisted.putNodeMetaData(STATIC_COMPILE_NODE, typedBody ? Boolean.TRUE : Boolean.FALSE);
        addGeneratedMethod(enclosing, hoisted);
        targets.add(hoisted); // claim the id; writePackedDispatcher renders the table at end of class
        // GROOVY-12150 visibility: a packed closure has no generated class, so mark it with the hoisted
        // method it became. The AST browser then renders it via the same path it uses for the generated
        // closure class of an unpacked closure; the trailing () distinguishes a method from a class.
        expression.putNodeMetaData(GENERATED_CLOSURE_CLASS, enclosing.getName() + "." + name + "()");

        // Instantiate the fixed-arity family member matching the literal's declared parameter count
        // (FixedN, with a varargs doCall, serves arities above four; the base class is abstract):
        // the arity is then visible to class-level introspection — notably MetaClassHelper's
        // SAM-overload disambiguation, which reflects the argument class's declared doCall —
        // exactly as on a generated closure class. An implicit-parameter literal gets FixedIt
        // (doCall() + doCall(Object)), matching the fuzzy "0 or 1" arity a generated class
        // declares for it. A vararg-shaped literal (trailing array parameter, e.g. { ...z -> })
        // also gets FixedN: its generated class would declare a VARARG doCall, whose selection
        // flexibility (zero args collect to an empty array where a fixed one-param doCall would
        // null-fill) only a varargs declaration reproduces.
        boolean varargShape = !implicitParam && arity > 0 && closureParams[arity - 1].getType().isArray();
        String pc;
        if (open) {
            pc = "org/codehaus/groovy/runtime/OpenClosure$AsClosure"; // classic contract via ClassicResolver
        } else {
            pc = "org/codehaus/groovy/runtime/PackedClosure";
            if (implicitParam) pc = pc + "$FixedIt";
            else if (arity <= 4 && !varargShape) pc = pc + "$Fixed" + arity;
            else pc = pc + "$FixedN";
        }
        mv.visitTypeInsn(NEW, pc);
        mv.visitInsn(DUP);
        if (staticContext) {
            new ClassExpression(enclosing).visit(acg);   // owner = the enclosing class (static dispatch)
        } else {
            mv.visitVarInsn(ALOAD, 0);
            os.push(ClassHelper.OBJECT_TYPE);            // owner = this
        }
        // The class's shared dispatch table plus this body's compile-time id: dispatch is a plain
        // interface call into a tableswitch (JIT-friendly, unlike a per-instance MethodHandle, which
        // cannot be constant-folded), and the id binds the exact method so an inherited packed closure
        // cannot misdispatch to a subclass's same-named method.
        mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(enclosing), DISPATCHERS_GETTER, DISPATCHERS_GETTER_DESC, false);
        os.push(ClassHelper.make(org.codehaus.groovy.runtime.GeneratedDispatcher.Bundle.class));
        BytecodeHelper.pushConstant(mv, id);
        os.push(ClassHelper.int_TYPE);
        mv.visitLdcInsn(name);
        os.push(ClassHelper.STRING_TYPE);   // method name (diagnostics only)
        if (captured.isEmpty() && !open) {
            mv.visitInsn(ACONST_NULL);
            os.push(ClassHelper.OBJECT_TYPE.makeArray());
        } else {
            // Build Object[] of captured values: written vars pass their shared Reference (so writes
            // propagate), read-only vars pass their value. An open closure's ClassicResolver rides
            // in slot 0, matching the hoisted body's leading Resolver parameter.
            BytecodeHelper.pushConstant(mv, prefix + captured.size());
            os.push(ClassHelper.int_TYPE);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            os.replace(ClassHelper.OBJECT_TYPE.makeArray());
            if (open) {
                mv.visitInsn(DUP);
                os.push(ClassHelper.OBJECT_TYPE.makeArray());
                BytecodeHelper.pushConstant(mv, 0);
                os.push(ClassHelper.int_TYPE);
                mv.visitTypeInsn(NEW, CLASSIC_RESOLVER_INTERNAL);
                mv.visitInsn(DUP);
                if (staticContext) {
                    new ClassExpression(enclosing).visit(acg);
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    os.push(ClassHelper.OBJECT_TYPE);
                }
                mv.visitMethodInsn(INVOKESPECIAL, CLASSIC_RESOLVER_INTERNAL, "<init>", "(Ljava/lang/Object;)V", false);
                mv.visitInsn(AASTORE);
                os.remove(3);
            }
            for (int i = 0; i < captured.size(); i++) {
                String cn = captured.get(i);
                mv.visitInsn(DUP);
                os.push(ClassHelper.OBJECT_TYPE.makeArray());
                BytecodeHelper.pushConstant(mv, prefix + i);
                os.push(ClassHelper.int_TYPE);
                if (holderThreaded.contains(cn)) {
                    loadReference(cn, controller);       // shared Reference holder
                } else {
                    new VariableExpression(cn).visit(acg); // value
                    os.box();
                }
                mv.visitInsn(AASTORE);
                os.remove(3);
            }
        }
        // The closure's declared parameter types, so getParameterTypes()-keyed caller behaviour
        // (DGM arity decisions, vararg collection) matches a generated closure class exactly.
        // A condy constant (resolved once per literal site, shared by every adapter created
        // there — as a closure class shares its cached parameter-type array), NOT a fresh array:
        // building one per creation measurably taxes creation-heavy code. The types travel as a
        // method descriptor because primitive parameter types have no Class constant-pool form.
        StringBuilder typesDescriptor = new StringBuilder("(");
        for (int i = 0; i < arity; i++) {
            typesDescriptor.append(BytecodeHelper.getTypeDescription(erasedType(closureParams[i].getType())));
        }
        typesDescriptor.append(")V");
        mv.visitLdcInsn(new org.objectweb.asm.ConstantDynamic("paramTypes", "[Ljava/lang/Class;",
                new org.objectweb.asm.Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC, DISPATCHER_TYPE, "paramTypes",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;)[Ljava/lang/Class;",
                        true), // the bootstrap is a static interface method
                typesDescriptor.toString()));
        os.push(ClassHelper.CLASS_Type.makeArray());
        // strict guard only for the dynamic trust path; a PROVEN delegate-independent closure --
        // statically (StaticTypesClosureWriter) or syntactically (SYNTACTIC_PACK, dynamic) --
        // stores-and-ignores a caller-set delegate, like a @CompileStatic closure class.
        boolean strict = !open && packedClosureUsesDelegateGuard() && expression.getNodeMetaData(SYNTACTIC_PACK) == null;
        mv.visitInsn(strict ? org.objectweb.asm.Opcodes.ICONST_1 : org.objectweb.asm.Opcodes.ICONST_0);
        os.push(ClassHelper.boolean_TYPE);
        mv.visitMethodInsn(INVOKESPECIAL, pc, "<init>",
                "(Ljava/lang/Object;L" + BUNDLE_TYPE + ";ILjava/lang/String;[Ljava/lang/Object;[Ljava/lang/Class;Z)V", false);
        os.replace(ClassHelper.CLOSURE_TYPE, 7); // owner, dispatchers, id, name, captured[], paramTypes[], strict -> Closure
    }

    /**
     * Emits the class's packed-closure dispatch machinery, once per class at the end of class
     * generation (so every hoisted body — including one a hoisted body itself hoisted — has claimed
     * its id): the {@code $packedDispatch$(int, Object[])} table, whose case {@code k} casts the
     * packed argument array ({@code [owner, captured..., args...]}) to target {@code k}'s declared
     * parameter types and invokes it directly; the array-free per-arity tables
     * ({@code $packedDispatch1$}/{@code $packedDispatch2$}) doing the same from plain parameter
     * slots for the hot one/two-value shapes; and the {@code $getPackedDispatchers$()} accessor,
     * whose single {@code invokedynamic} site links all three through
     * {@link org.codehaus.groovy.runtime.GeneratedDispatcher#bootstrap} into one constant
     * {@code Bundle}, lazily on first adapter creation. A no-op for classes that packed nothing.
     */
    public void writePackedDispatcher() {
        ClassNode enclosing = controller.getClassNode();
        List<MethodNode> targets = enclosing.getNodeMetaData(PACKED_TARGETS);
        if (targets == null || targets.isEmpty()) return;
        enclosing.removeNodeMetaData(PACKED_TARGETS);
        String internal = BytecodeHelper.getClassInternalName(enclosing);
        org.objectweb.asm.ClassVisitor cv = controller.getClassVisitor();

        // the accessor: return INDY packedDispatchers()Bundle — GeneratedDispatcher.bootstrap links
        // the class's three dispatch tables (through LambdaMetafactory, with this class's lookup)
        // once, on first adapter creation, and every later call returns the constant bundle, so
        // the accessor is also the cache
        MethodVisitor mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, DISPATCHERS_GETTER, DISPATCHERS_GETTER_DESC, null, null);
        mv.visitCode();
        org.objectweb.asm.Handle bootstrap = new org.objectweb.asm.Handle(
                org.objectweb.asm.Opcodes.H_INVOKESTATIC, DISPATCHER_TYPE, "bootstrap",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                true); // the bootstrap is a static interface method
        mv.visitInvokeDynamicInsn("packedDispatchers", DISPATCHERS_GETTER_DESC, bootstrap);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // the array-free per-arity tables, over the targets whose captured-plus-argument count
        // matches (membership is sparse over the id space, so cases use lookupswitch); routing is
        // the adapter's responsibility, so any other id landing here is a compiler bug
        writeArityTable(cv, internal, enclosing, targets, 1, DISPATCH1_METHOD, DISPATCH1_DESC);
        writeArityTable(cv, internal, enclosing, targets, 2, DISPATCH2_METHOD, DISPATCH2_DESC);

        // The table. It must stay under the JIT's inlining budget (FreqInlineSize, 325 bytecode
        // bytes) or a hot caller cannot fold a constant id through to a direct call of the target
        // (and the packed argument array then escapes instead of being scalar-replaced). A switch
        // case costs ~20 bytes, so past DISPATCH_CHUNK targets the table goes two-level: the entry
        // method shifts the id down to a chunk index and forwards -- small enough to always inline
        // -- and each chunk method switches over its slice of at most DISPATCH_CHUNK cases.
        int n = targets.size();
        if (n <= DISPATCH_CHUNK) {
            mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, DISPATCH_METHOD, DISPATCH_DESC, null, null);
            mv.visitCode();
            writeDispatchSwitch(mv, internal, enclosing, targets, 0, n);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        } else {
            int chunks = (n + DISPATCH_CHUNK - 1) / DISPATCH_CHUNK;
            mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, DISPATCH_METHOD, DISPATCH_DESC, null, null);
            mv.visitCode();
            org.objectweb.asm.Label dflt = new org.objectweb.asm.Label();
            org.objectweb.asm.Label[] labels = new org.objectweb.asm.Label[chunks];
            for (int c = 0; c < chunks; c += 1) labels[c] = new org.objectweb.asm.Label();
            mv.visitVarInsn(ILOAD, 0);
            BytecodeHelper.pushConstant(mv, Integer.numberOfTrailingZeros(DISPATCH_CHUNK));
            mv.visitInsn(ISHR);
            mv.visitTableSwitchInsn(0, chunks - 1, dflt, labels);
            for (int c = 0; c < chunks; c += 1) {
                mv.visitLabel(labels[c]);
                mv.visitVarInsn(ILOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKESTATIC, internal, DISPATCH_METHOD + c, DISPATCH_DESC, false);
                mv.visitInsn(ARETURN);
            }
            mv.visitLabel(dflt);
            writeDispatchDefault(mv, enclosing);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            for (int c = 0; c < chunks; c += 1) {
                mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, DISPATCH_METHOD + c, DISPATCH_DESC, null, null);
                mv.visitCode();
                writeDispatchSwitch(mv, internal, enclosing, targets, c * DISPATCH_CHUNK, Math.min(n, (c + 1) * DISPATCH_CHUNK));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }

    /** A dispatch table slice: a tableswitch over ids {@code [lo, hi)} into direct target invocations. */
    private static void writeDispatchSwitch(final MethodVisitor mv, final String internal, final ClassNode enclosing, final List<MethodNode> targets, final int lo, final int hi) {
        org.objectweb.asm.Label dflt = new org.objectweb.asm.Label();
        org.objectweb.asm.Label[] labels = new org.objectweb.asm.Label[hi - lo];
        for (int i = 0; i < hi - lo; i += 1) labels[i] = new org.objectweb.asm.Label();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitTableSwitchInsn(lo, hi - 1, dflt, labels);
        for (int k = lo; k < hi; k += 1) {
            mv.visitLabel(labels[k - lo]);
            MethodNode target = targets.get(k);
            boolean staticTarget = target.isStatic();
            if (!staticTarget) {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, internal);
            }
            Parameter[] params = target.getParameters();
            for (int i = 0; i < params.length; i += 1) {
                mv.visitVarInsn(ALOAD, 1);
                BytecodeHelper.pushConstant(mv, i + 1); // [0] is the receiver slot, params start at [1]
                mv.visitInsn(AALOAD);
                BytecodeHelper.doCast(mv, params[i].getType()); // checkcast, or unbox for a primitive
            }
            String desc = BytecodeHelper.getMethodDescriptor(target.getReturnType(), params);
            // a hoisted body is private, so INVOKESPECIAL dispatches it exactly (never virtually)
            mv.visitMethodInsn(staticTarget ? INVOKESTATIC : INVOKESPECIAL, internal, target.getName(), desc, false);
            // hoisted return types are reference types by construction (see inferredClosureReturnType);
            // box defensively so a future primitive-returning target cannot emit invalid bytecode
            ClassNode returnType = target.getReturnType();
            if (ClassHelper.isPrimitiveVoid(returnType)) {
                mv.visitInsn(ACONST_NULL);
            } else if (ClassHelper.isPrimitiveType(returnType)) {
                BytecodeHelper.doCastToWrappedType(mv, returnType, ClassHelper.getWrapper(returnType));
            }
            mv.visitInsn(ARETURN);
        }
        mv.visitLabel(dflt);
        writeDispatchDefault(mv, enclosing);
    }

    /**
     * An array-free dispatch table for the targets whose captured-plus-argument count is exactly
     * {@code paramCount}: each case loads the receiver and arguments from parameter slots — no
     * packed array — and invokes the target directly. Membership is sparse over the id space, so
     * cases switch with {@code lookupswitch}; ids of other arities land on the default (routing is
     * the adapter's responsibility, so reaching it is a compiler bug). Past {@link #DISPATCH_CHUNK}
     * members the table goes two-level under the same inline-budget discipline as the array table:
     * the entry method shifts the id down to a chunk index and forwards, and each chunk method
     * switches over its id-range's members (at most {@code DISPATCH_CHUNK}, since a range spans
     * {@code DISPATCH_CHUNK} consecutive ids).
     */
    private static void writeArityTable(final org.objectweb.asm.ClassVisitor cv, final String internal,
            final ClassNode enclosing, final List<MethodNode> targets, final int paramCount,
            final String tableMethod, final String tableDesc) {
        List<Integer> memberIds = new ArrayList<>();
        for (int k = 0; k < targets.size(); k += 1) {
            if (targets.get(k).getParameters().length == paramCount) memberIds.add(k);
        }
        MethodVisitor mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, tableMethod, tableDesc, null, null);
        mv.visitCode();
        if (memberIds.size() <= DISPATCH_CHUNK) {
            writeAritySwitch(mv, internal, enclosing, targets, memberIds, paramCount);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        int shift = Integer.numberOfTrailingZeros(DISPATCH_CHUNK);
        int chunks = ((targets.size() - 1) >> shift) + 1;
        List<List<Integer>> byChunk = new ArrayList<>(chunks);
        for (int c = 0; c < chunks; c += 1) byChunk.add(new ArrayList<>());
        for (int memberId : memberIds) byChunk.get(memberId >> shift).add(memberId);
        org.objectweb.asm.Label dflt = new org.objectweb.asm.Label();
        org.objectweb.asm.Label[] labels = new org.objectweb.asm.Label[chunks];
        for (int c = 0; c < chunks; c += 1) {
            // an id-range with no members of this arity shares the default (compiler-bug) label
            labels[c] = byChunk.get(c).isEmpty() ? dflt : new org.objectweb.asm.Label();
        }
        mv.visitVarInsn(ILOAD, 0);
        BytecodeHelper.pushConstant(mv, shift);
        mv.visitInsn(ISHR);
        mv.visitTableSwitchInsn(0, chunks - 1, dflt, labels);
        for (int c = 0; c < chunks; c += 1) {
            if (byChunk.get(c).isEmpty()) continue;
            mv.visitLabel(labels[c]);
            mv.visitVarInsn(ILOAD, 0);
            for (int p = 0; p <= paramCount; p += 1) {
                mv.visitVarInsn(ALOAD, 1 + p); // the receiver, then the value parameters
            }
            mv.visitMethodInsn(INVOKESTATIC, internal, tableMethod + c, tableDesc, false);
            mv.visitInsn(ARETURN);
        }
        mv.visitLabel(dflt);
        writeDispatchDefault(mv, enclosing);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        for (int c = 0; c < chunks; c += 1) {
            if (byChunk.get(c).isEmpty()) continue;
            mv = cv.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, tableMethod + c, tableDesc, null, null);
            mv.visitCode();
            writeAritySwitch(mv, internal, enclosing, targets, byChunk.get(c), paramCount);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    /** An arity-table slice: a lookupswitch over the given member ids into direct target invocations. */
    private static void writeAritySwitch(final MethodVisitor mv, final String internal,
            final ClassNode enclosing, final List<MethodNode> targets, final List<Integer> memberIds, final int paramCount) {
        org.objectweb.asm.Label dflt = new org.objectweb.asm.Label();
        int members = memberIds.size();
        int[] keys = new int[members];
        org.objectweb.asm.Label[] labels = new org.objectweb.asm.Label[members];
        for (int i = 0; i < members; i += 1) {
            keys[i] = memberIds.get(i); // ids are claimed in ascending order, as lookupswitch requires
            labels[i] = new org.objectweb.asm.Label();
        }
        mv.visitVarInsn(ILOAD, 0);
        mv.visitLookupSwitchInsn(dflt, keys, labels);
        for (int i = 0; i < members; i += 1) {
            mv.visitLabel(labels[i]);
            MethodNode target = targets.get(keys[i]);
            boolean staticTarget = target.isStatic();
            if (!staticTarget) {
                mv.visitVarInsn(ALOAD, 1); // the receiver parameter
                mv.visitTypeInsn(CHECKCAST, internal);
            }
            Parameter[] params = target.getParameters();
            for (int p = 0; p < paramCount; p += 1) {
                mv.visitVarInsn(ALOAD, 2 + p); // value parameters follow (id, receiver)
                BytecodeHelper.doCast(mv, params[p].getType()); // checkcast, or unbox for a primitive
            }
            String desc = BytecodeHelper.getMethodDescriptor(target.getReturnType(), params);
            // a hoisted body is private, so INVOKESPECIAL dispatches it exactly (never virtually)
            mv.visitMethodInsn(staticTarget ? INVOKESTATIC : INVOKESPECIAL, internal, target.getName(), desc, false);
            ClassNode returnType = target.getReturnType();
            if (ClassHelper.isPrimitiveVoid(returnType)) {
                mv.visitInsn(ACONST_NULL);
            } else if (ClassHelper.isPrimitiveType(returnType)) {
                BytecodeHelper.doCastToWrappedType(mv, returnType, ClassHelper.getWrapper(returnType));
            }
            mv.visitInsn(ARETURN);
        }
        mv.visitLabel(dflt);
        writeDispatchDefault(mv, enclosing);
    }

    private static void writeDispatchDefault(final MethodVisitor mv, final ClassNode enclosing) {
        mv.visitTypeInsn(NEW, "org/codehaus/groovy/GroovyBugError");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Invalid packed closure dispatch id in " + enclosing.getName());
        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/GroovyBugError", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }

    /**
     * Loads a closure-shared variable reference onto the operand stack, looking it up
     * as a field, local variable, or outer closure field as appropriate.
     *
     * @param name       the variable name to load
     * @param controller the writer controller for the enclosing class
     */
    public static void loadReference(final String name, final WriterController controller) {
        CompileStack compileStack = controller.getCompileStack();
        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode classNode = controller.getClassNode();
        AsmClassGenerator acg = controller.getAcg();

        // compileStack.containsVariable(name) means to ask if the variable is already declared
        // compileStack.getScope().isReferencedClassVariable(name) means to ask if the variable is a field
        // If it is no field and is not yet declared, then it is either a closure shared variable or
        // an already declared variable.
        if (!compileStack.containsVariable(name) && compileStack.getScope().isReferencedClassVariable(name)) {
            acg.visitFieldExpression(new FieldExpression(classNode.getDeclaredField(name)));
        } else {
            BytecodeVariable v = compileStack.getVariable(name, !classNodeUsesReferences(controller.getClassNode()));
            if (v == null) {
                // variable is not on stack because we are
                // inside a nested Closure and this variable
                // was not used before
                // then load it from the Closure field
                FieldNode field = classNode.getDeclaredField(name);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, controller.getInternalClassName(), name, BytecodeHelper.getTypeDescription(field.getType()));
            } else {
                mv.visitVarInsn(ALOAD, v.getIndex());
            }
            controller.getOperandStack().push(ClassHelper.REFERENCE_TYPE);
        }
    }

    /**
     * Node metadata key set on a {@link ClosureExpression} once it has been compiled to a generated
     * closure class, holding that class's name. Tooling such as the AST browser can read it to show
     * which synthetic class a closure literal became; the compiler itself never consults it. The mark
     * only becomes available after the class-generation phase, since that is when the class is made.
     */
    public static final String GENERATED_CLOSURE_CLASS = ClosureWriter.class.getName() + ".generatedClass";

    /**
     * Returns the existing generated class for a closure expression, or creates and registers
     * a new one if none exists yet.
     *
     * @param expression the closure expression to compile as an inner class
     * @param modifiers  the access modifiers for the generated class
     * @return the generated closure class node
     */
    public ClassNode getOrAddClosureClass(final ClosureExpression expression, final int modifiers) {
        ClassNode closureClass = closureClasses.get(expression);
        if (closureClass == null) {
            closureClass = createClosureClass(expression, modifiers);
            closureClasses.put(expression, closureClass);
            controller.getAcg().addInnerClass(closureClass);
            closureClass.putNodeMetaData(WriterControllerFactory.class, (WriterControllerFactory) x -> controller);
            expression.putNodeMetaData(GENERATED_CLOSURE_CLASS, closureClass.getName());
        }
        return closureClass;
    }

    private static boolean classNodeUsesReferences(final ClassNode classNode) {
        boolean ret = classNode.getSuperClass().equals(ClassHelper.CLOSURE_TYPE);
        if (ret) return ret;
        if (classNode instanceof InnerClassNode inner) {
            return inner.isAnonymous();
        }
        return false;
    }

    private static boolean isNotObjectOrObjectArray(final ClassNode classNode) {
        return !ClassHelper.isObjectType(classNode) && !ClassHelper.isObjectType(classNode.getComponentType());
    }

    /**
     * Creates a new inner class node representing the compiled form of a closure expression.
     *
     * @param expression the closure expression to compile
     * @param modifiers  the access modifiers for the generated class
     * @return the newly created closure class node
     */
    protected ClassNode createClosureClass(final ClosureExpression expression, final int modifiers) {
        ClassNode classNode = controller.getClassNode();
        ClassNode rootClass = controller.getOutermostClass();
        MethodNode enclosingMethod = controller.getMethodNode();
        String name = classNode.getName() + "$" + controller.getContext().getNextClosureInnerName(rootClass, classNode, enclosingMethod);

        Parameter[] parameters = expression.getParameters();
        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        } else if (parameters.length == 0) {
            // provide a default parameter
            parameters = new Parameter[1];
            parameters[0] = new Parameter(ClassHelper.OBJECT_TYPE, "it", nullX());
            Variable decl = expression.getVariableScope().getDeclaredVariable("it");
            if (decl != null) parameters[0].setClosureSharedVariable(decl.isClosureSharedVariable());
        }

        Parameter[] localVariableParams = getClosureSharedVariables(expression);
        removeInitialValues(localVariableParams);

        // GROOVY-9971: closure return type is mapped to Groovy cast by classgen
        ClassNode returnType = expression.getNodeMetaData(INFERRED_RETURN_TYPE);
        if (returnType == null) returnType = ClassHelper.OBJECT_TYPE; // not STC or unknown path
        else if (returnType.isPrimaryClassNode()) returnType = returnType.getPlainNodeReference();
        else if (ClassHelper.isPrimitiveType(returnType)) returnType = ClassHelper.getWrapper(returnType);
        else if (GenericsUtils.hasUnresolvedGenerics(returnType)) returnType = GenericsUtils.nonGeneric(returnType);

        var answer = new InnerClassNode(classNode, name, modifiers, ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
        answer.addInterface(ClassHelper.GENERATED_CLOSURE_Type.getPlainNodeReference());
        answer.setEnclosingMethod(enclosingMethod);
        answer.setScriptBody(controller.isInScriptBody());
        answer.setSourcePosition(expression);
        answer.setStaticClass(controller.isStaticMethod() || classNode.isStaticClass());
        answer.setSynthetic(true);

        {
            MethodNode doCall = answer.addMethod("doCall", ACC_PUBLIC, returnType, parameters, ClassNode.EMPTY_ARRAY, expression.getCode());
            doCall.setSourcePosition(expression);
            VariableScope varScope = expression.getVariableScope();
            if (varScope == null) {
                throw new RuntimeException("Must have a VariableScope by now for expression: " + expression + " class: " + name);
            }
            doCall.setVariableScope(varScope.copy());

            new CodeVisitorSupport() {
                /**
                 * Associates anonymous inner classes with the generated {@code doCall} method.
                 */
                @Override
                public void visitConstructorCallExpression(final ConstructorCallExpression cce) {
                    if (cce.isUsingAnonymousInnerClass()) { // GROOVY-11846
                        cce.getType().setEnclosingMethod(doCall);
                    }
                    super.visitConstructorCallExpression(cce);
                }
            }
            .visit(expression.getCode());
        }

        if (parameters.length > 1 || (parameters.length == 1 && (isNotObjectOrObjectArray(parameters[0].getType())
                || !parameters[0].getAnnotations().isEmpty() || !parameters[0].getType().getTypeAnnotations().isEmpty()))) { // GROOVY-11311
            var call = new MethodNode(
                    "call",
                    ACC_PUBLIC,
                    returnType,
                    parameters,
                    ClassNode.EMPTY_ARRAY,
                    returnS(callThisX("doCall", args(parameters))));
            addGeneratedMethod(answer, call, true);
        }

        BlockStatement block = createBlockStatementForConstructor(expression, rootClass, classNode);
        addConstructor(expression, localVariableParams, answer, block);

        addFieldsForLocalVariables(answer, localVariableParams);

        correctAccessedVariable(answer, expression);

        addSerialVersionUIDField(answer);

        return answer;
    }

    /**
     * Adds a synthetic {@code serialVersionUID} field to the closure class,
     * derived from a hash of the class name.
     *
     * @param classNode the closure class node to add the field to
     */
    protected void addSerialVersionUIDField(final ClassNode classNode) {
        // just to hash the full class name for better performance.
        // The full spec for `serialVersionUID` is here:
        //      https://docs.oracle.com/en/java/javase/21/docs/specs/serialization/class.html#stream-unique-identifiers
        // As we could see, it's too complex for closures.
        long serialVersionUID = hash(classNode.getName());
        classNode.addFieldFirst("serialVersionUID", ACC_PRIVATE | ACC_STATIC | ACC_FINAL, ClassHelper.long_TYPE, constX(serialVersionUID, true));
    }

    private static long hash(String str) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new GroovyBugError("Failed to find SHA", e);
        }
        final byte[] hashBytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
        long hash = 0;
        for (int i = Math.min(hashBytes.length, 7); i >= 0; i--) {
            hash = (hash << 8) | (hashBytes[i] & 0xFF);
        }
        return hash;
    }

    /**
     * Adds a synthetic {@code public} constructor to the closure inner class that accepts
     * the outer instance, {@code this} object, and all captured local variable references.
     *
     * @param expression          the closure expression
     * @param localVariableParams parameters for closure-shared local variables
     * @param answer              the closure inner class node
     * @param block               the constructor body
     * @return the created constructor node
     */
    protected ConstructorNode addConstructor(final ClosureExpression expression, final Parameter[] localVariableParams, final InnerClassNode answer, final BlockStatement block) {
        Parameter[] params = new Parameter[2 + localVariableParams.length];
        params[0] = new Parameter(ClassHelper.OBJECT_TYPE, OUTER_INSTANCE);
        params[1] = new Parameter(ClassHelper.OBJECT_TYPE, THIS_OBJECT);
        System.arraycopy(localVariableParams, 0, params, 2, localVariableParams.length);

        ConstructorNode constructorNode = answer.addConstructor(ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, block);
        constructorNode.setSourcePosition(expression);

        return constructorNode;
    }

    /**
     * Adds synthetic private fields to the closure inner class for each captured
     * local variable parameter, promoting them to {@code Reference} holders.
     *
     * @param closureClass        the closure inner class node
     * @param localVariableParams the closure-shared local variable parameters
     */
    protected void addFieldsForLocalVariables(final InnerClassNode closureClass, final Parameter[] localVariableParams) {
        for (Parameter param : localVariableParams) {
            String     paramName = param.getName();
            ClassNode  paramType = param.getOriginType();
            if (ClassHelper.isPrimitiveType(paramType)) {
                paramType = ClassHelper.getWrapper(paramType);
            } else {
                paramType = paramType.getPlainNodeReference();
            }

            VariableExpression initialValue = varX(paramName);
            initialValue.setAccessedVariable(param);
            initialValue.setUseReferenceDirectly(true);
            param.setType(ClassHelper.makeReference());

            FieldNode paramField = closureClass.addField(paramName, ACC_PRIVATE | ACC_SYNTHETIC, param.getType(), initialValue);
            paramField.setOriginType(paramType);
            paramField.setHolder(true);
        }
    }

    /**
     * Creates the block statement for the closure's synthetic constructor, setting up
     * the {@code super(outerInstance, thisObject)} call and captured variable references.
     *
     * @param expression      the closure expression
     * @param outerClass      the class declaring the closure
     * @param thisClassNode   the {@code this} type in scope at the closure declaration site
     * @return the block statement for the constructor body
     */
    protected BlockStatement createBlockStatementForConstructor(final ClosureExpression expression, final ClassNode outerClass, final ClassNode thisClassNode) {
        BlockStatement block = new BlockStatement();
        // this block does not get a source position, because we don't
        // want this synthetic constructor to show up in corbertura reports
        VariableExpression outer = varX(OUTER_INSTANCE, outerClass);
        outer.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(outer);
        VariableExpression thisObject = varX(THIS_OBJECT, thisClassNode);
        thisObject.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(thisObject);
        TupleExpression conArgs = new TupleExpression(outer, thisObject);
        block.addStatement(stmt(ctorSuperX(conArgs)));
        return block;
    }

    /**
     * Visitor that rewrites {@link org.codehaus.groovy.ast.expr.VariableExpression} nodes
     * whose accessed variable is a {@link org.codehaus.groovy.ast.FieldNode} in an outer class
     * to instead reference the corresponding field in the generated closure inner class.
     */
    protected static class CorrectAccessedVariableVisitor extends CodeVisitorSupport {
        private InnerClassNode icn;

        /**
         * Creates a visitor for the generated closure class.
         *
         * @param icn the generated closure class
         */
        public CorrectAccessedVariableVisitor(final InnerClassNode icn) {
            this.icn = icn;
        }

        /** {@inheritDoc} */
        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            Variable v = expression.getAccessedVariable();
            if (v == null) return;
            if (!(v instanceof FieldNode)) return;
            String name = expression.getName();
            FieldNode fn = icn.getDeclaredField(name);
            if (fn != null) { // only overwrite if we find something more specific
                expression.setAccessedVariable(fn);
            }
        }
    }

    private static void correctAccessedVariable(final InnerClassNode closureClass, final ClosureExpression ce) {
        new CorrectAccessedVariableVisitor(closureClass).visitClosureExpression(ce);
    }

    /*
     * this method is called for local variables shared between scopes.
     * These variables must not have init values because these would
     * then in later steps be used to create multiple versions of the
     * same method, in this case the constructor. A closure should not
     * have more than one constructor!
     */
    /**
     * Strips initial-value expressions from parameters that are closure-shared,
     * ensuring the closure constructor is not duplicated.
     *
     * @param params the parameters to mutate in-place
     */
    protected static void removeInitialValues(final Parameter[] params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i].hasInitialExpression()) {
                Parameter p = new Parameter(params[i].getType(), params[i].getName());
                p.setOriginType(p.getOriginType());
                params[i] = p;
            }
        }
    }

    /**
     * Emits a {@code super(outerInstance, thisObject)} constructor call for a generated
     * closure class. Returns {@code false} if the current class is not a generated closure.
     *
     * @param call the constructor call expression representing {@code super(...)}
     * @return {@code true} if the closure constructor call was emitted
     */
    public boolean addGeneratedClosureConstructorCall(final ConstructorCallExpression call) {
        ClassNode classNode = controller.getClassNode();
        if (!classNode.declaresInterface(ClassHelper.GENERATED_CLOSURE_Type)) return false;

        AsmClassGenerator acg = controller.getAcg();
        OperandStack operandStack = controller.getOperandStack();

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitVarInsn(ALOAD, 0);
        ClassNode callNode = classNode.getSuperClass();
        TupleExpression arguments = (TupleExpression) call.getArguments();
        if (arguments.getExpressions().size() != 2)
            throw new GroovyBugError("expected 2 arguments for closure constructor super call, but got" + arguments.getExpressions().size());
        arguments.getExpression(0).visit(acg);
        operandStack.box();
        arguments.getExpression(1).visit(acg);
        operandStack.box();
        //TODO: replace with normal String, p not needed
        Parameter p = new Parameter(ClassHelper.OBJECT_TYPE, "_p");
        String descriptor = BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, new Parameter[]{p, p});
        mv.visitMethodInsn(INVOKESPECIAL, BytecodeHelper.getClassInternalName(callNode), "<init>", descriptor, false);
        operandStack.remove(2);
        return true;
    }

    /**
     * Collects the closure-shared local variables referenced by a closure expression
     * as an array of {@link Parameter}s, using the type chooser to infer each variable's type.
     *
     * @param expression the closure expression
     * @return the array of parameters representing captured shared variables
     */
    protected Parameter[] getClosureSharedVariables(final ClosureExpression expression) {
        ClassNode classNode = controller.getClassNode();
        TypeChooser typeChooser = controller.getTypeChooser();
        VariableScope variableScope = expression.getVariableScope();

        Parameter[] refs = new Parameter[variableScope.getReferencedLocalVariablesCount()]; int index = 0;
        for (Iterator<Variable> iter = variableScope.getReferencedLocalVariablesIterator(); iter.hasNext(); ) {
            Variable variable = iter.next();
            Expression varExp = variable instanceof VariableExpression
                                ? (VariableExpression) variable : varX(variable); // GROOVY-11471

            ClassNode inferenceType = typeChooser.resolveType(varExp, classNode); // GROOVY-11068
            Parameter p = new Parameter(inferenceType, variable.getName());
            p.setClosureSharedVariable(variable.isClosureSharedVariable());

            refs[index++] = p;
        }
        return refs;
    }

    /**
     * Loads the effective {@code this} reference onto the operand stack — either the
     * actual receiver for a regular method, or the result of {@code getThisObject()} for
     * a generated closure/lambda.
     */
    protected void loadThis() {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitVarInsn(ALOAD, 0);
        if (controller.isInGeneratedFunction()) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/Closure", "getThisObject", "()Ljava/lang/Object;", false);
            controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);
        } else {
            controller.getOperandStack().push(controller.getClassNode());
        }
    }
}
