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
import org.codehaus.groovy.ast.CodeVisitorSupport;
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
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
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
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
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
    private int packedCounter;
    // Closures found in a syntactic position that lets them outlive the method are cached per method.
    private final Map<MethodNode, Set<ClosureExpression>> escapingClosuresByMethod = new HashMap<>();
    // Names assigned anywhere in a method (excluding declarations), cached per method: a capture must
    // be Reference-threaded if it is written ANYWHERE in the enclosing method, not just inside the
    // closure -- e.g. `def fib; fib = { n -> ... fib(n-1) ... }` writes fib after the closure is
    // constructed, so a by-value capture would see the stale (null) value.
    private final Map<MethodNode, Set<String>> writtenNamesByMethod = new HashMap<>();

    /** Name prefix of the synthetic methods holding hoisted closure bodies. */
    private static final String PACKED_METHOD_PREFIX = "$packed$closure$";

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
        if (chooseStrategy(expression) == PackStrategy.PACKED_ADAPTER) {
            writePackedClosure(expression);
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
        if (!isPackable(expression)) return PackStrategy.FULL_CLASS;
        // The most-specific @PackedClosures mode in scope (method wins over class), or null if not
        // annotated. DISABLED is a fine-grained opt-out that beats an enclosing opt-in AND the flag.
        PackMode mode = annotatedMode();
        if (mode == PackMode.DISABLED) return PackStrategy.FULL_CLASS;
        boolean annotated = (mode != null); // LENIENT/WARN/STRICT all opt in (DISABLED handled above)
        if (!isPackTriggered(expression, annotated)) return PackStrategy.FULL_CLASS;
        if (controller.isInGeneratedFunction()) return PackStrategy.FULL_CLASS;
        if (escapesEnclosingMethod(expression)) return PackStrategy.FULL_CLASS;
        return PackStrategy.PACKED_ADAPTER;
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
        return annotated;
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
     * {@code @PackedClosures} annotation (WARN => compiler warning, STRICT => compiler error). Only the
     * annotation opts in; the automatic {@code groovy.target.closure.pack} path is always lenient. A
     * nested closure is a structural decline (its enclosing context is a generated function, not the
     * owner class), so it is not reported.
     */
    private void reportUnpacked(final ClosureExpression expression) {
        if (controller.isInGeneratedFunction()) return;
        PackMode mode = annotatedMode();
        // LENIENT/DISABLED are both silent -- DISABLED asked NOT to pack, so a decline is expected
        if (mode == null || mode == PackMode.LENIENT || mode == PackMode.DISABLED) return;
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

    /** Captured variables that are written (reassigned) in the body — these need Reference threading. */
    private Set<String> writtenCaptureNames(final ClosureExpression expression) {
        Set<String> captured = capturedNames(expression);
        Set<String> written = new HashSet<>(collectWrittenNames(expression.getCode()));
        MethodNode m = controller.getMethodNode();
        if (m != null && m.getCode() != null) {
            written.addAll(writtenNamesByMethod.computeIfAbsent(m, k -> collectWrittenNames(k.getCode())));
        }
        written.retainAll(captured);
        return written;
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
                    // read-only captures become plain by-value params; written captures stay
                    // closure-shared so the ASM generator routes them through the holder
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
    private void writePackedClosure(final ClosureExpression expression) {
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

        Parameter[] closureParams = expression.isParameterSpecified()
                ? expression.getParameters()
                : new Parameter[]{new Parameter(ClassHelper.OBJECT_TYPE, "it")};
        int arity = closureParams.length;

        Set<String> written = writtenCaptureNames(expression);
        Parameter[] methodParams = new Parameter[captured.size() + closureParams.length];
        Map<Variable, Parameter> capturedByVar = new IdentityHashMap<>(); // original variable -> hoisted param
        boolean typedBody = compilesHoistedBodyStatically();
        for (int i = 0; i < captured.size(); i++) {
            String cn = captured.get(i);
            boolean isWritten = written.contains(cn);
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
                p = new Parameter(typedBody ? capturedTypes.get(cn) : ClassHelper.OBJECT_TYPE, cn);
            }
            methodParams[i] = p;
            capturedByVar.put(capturedVars.get(cn), p);
        }
        System.arraycopy(closureParams, 0, methodParams, captured.size(), closureParams.length);

        Statement body = expression.getCode();
        rebindCaptured(body, capturedByVar);  // read-only -> plain param; written -> holder param

        // In a static method there is no `this`: the owner is the enclosing class and the hoisted
        // method must be static (loaded/dispatched against the class, not local slot 0).
        boolean staticContext = controller.isStaticMethod();
        String name = PACKED_METHOD_PREFIX + (packedCounter++);
        // PRIVATE, not public: the body is reached only reflectively via PackedClosure.invokeHoisted, and
        // a private method is invoked exactly (no virtual dispatch), so an inherited packed closure never
        // dispatches to a same-named hoisted method a subclass happens to declare (GROOVY-12151 review).
        int mods = ACC_PRIVATE | ACC_SYNTHETIC | (staticContext ? ACC_STATIC : 0);
        // Same return type a generated closure class would give doCall, so implicit return-value
        // coercion (e.g. GString -> String for a Closure<String>) happens identically.
        ClassNode hoistedReturnType = typedBody ? inferredClosureReturnType(expression) : ClassHelper.OBJECT_TYPE;
        MethodNode hoisted = new MethodNode(name, mods, hoistedReturnType,
                methodParams, ClassNode.EMPTY_ARRAY, body);
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
        // GROOVY-12150 visibility: a packed closure has no generated class, so mark it with the hoisted
        // method it became. The AST browser then renders it via the same path it uses for the generated
        // closure class of an unpacked closure; the trailing () distinguishes a method from a class.
        expression.putNodeMetaData(GENERATED_CLOSURE_CLASS, enclosing.getName() + "." + name + "()");

        String pc = "org/codehaus/groovy/runtime/PackedClosure";
        mv.visitTypeInsn(NEW, pc);
        mv.visitInsn(DUP);
        if (staticContext) {
            new ClassExpression(enclosing).visit(acg);   // owner = the enclosing class (static dispatch)
        } else {
            mv.visitVarInsn(ALOAD, 0);
            os.push(ClassHelper.OBJECT_TYPE);            // owner = this
        }
        // A MethodHandle constant to the private hoisted method (resolved in this hosting class): no
        // reflection, no name/arity lookup, and it binds the exact method so an inherited packed closure
        // cannot misdispatch to a subclass's same-named method.
        String hoistedDesc = BytecodeHelper.getMethodDescriptor(hoisted.getReturnType(), hoisted.getParameters());
        mv.visitLdcInsn(new org.objectweb.asm.Handle(
                staticContext ? org.objectweb.asm.Opcodes.H_INVOKESTATIC : org.objectweb.asm.Opcodes.H_INVOKESPECIAL,
                BytecodeHelper.getClassInternalName(enclosing), name, hoistedDesc, false));
        os.push(ClassHelper.make(java.lang.invoke.MethodHandle.class));
        mv.visitLdcInsn(name);
        os.push(ClassHelper.STRING_TYPE);   // method name (diagnostics only)
        if (captured.isEmpty()) {
            mv.visitInsn(ACONST_NULL);
            os.push(ClassHelper.OBJECT_TYPE.makeArray());
        } else {
            // Build Object[] of captured values: written vars pass their shared Reference (so writes
            // propagate), read-only vars pass their value.
            BytecodeHelper.pushConstant(mv, captured.size());
            os.push(ClassHelper.int_TYPE);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            os.replace(ClassHelper.OBJECT_TYPE.makeArray());
            for (int i = 0; i < captured.size(); i++) {
                String cn = captured.get(i);
                mv.visitInsn(DUP);
                os.push(ClassHelper.OBJECT_TYPE.makeArray());
                BytecodeHelper.pushConstant(mv, i);
                os.push(ClassHelper.int_TYPE);
                if (written.contains(cn)) {
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
        BytecodeHelper.pushConstant(mv, arity);
        os.push(ClassHelper.int_TYPE);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
        os.replace(ClassHelper.CLASS_Type.makeArray());
        for (int i = 0; i < arity; i++) {
            mv.visitInsn(DUP);
            os.push(ClassHelper.CLASS_Type.makeArray());
            BytecodeHelper.pushConstant(mv, i);
            os.push(ClassHelper.int_TYPE);
            BytecodeHelper.visitClassLiteral(mv, erasedType(closureParams[i].getType()));
            os.push(ClassHelper.CLASS_Type);
            mv.visitInsn(AASTORE);
            os.remove(3);
        }
        // strict guard only for the dynamic trust path; a statically PROVEN delegate-independent
        // closure stores-and-ignores a caller-set delegate, like a @CompileStatic closure class.
        mv.visitInsn(packedClosureUsesDelegateGuard() ? org.objectweb.asm.Opcodes.ICONST_1 : org.objectweb.asm.Opcodes.ICONST_0);
        os.push(ClassHelper.boolean_TYPE);
        mv.visitMethodInsn(INVOKESPECIAL, pc, "<init>",
                "(Ljava/lang/Object;Ljava/lang/invoke/MethodHandle;Ljava/lang/String;[Ljava/lang/Object;[Ljava/lang/Class;Z)V", false);
        os.replace(ClassHelper.CLOSURE_TYPE, 6); // owner, handle, name, captured[], paramTypes[], strict -> Closure
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
