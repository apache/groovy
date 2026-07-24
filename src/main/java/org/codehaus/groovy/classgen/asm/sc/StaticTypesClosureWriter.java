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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.asm.ClosureWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DYNAMIC_RESOLUTION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.IMPLICIT_RECEIVER;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.LAMBDA_MARKERS;

/**
 * Writer responsible for generating closure classes in statically compiled mode.
 */
public class StaticTypesClosureWriter extends ClosureWriter {
    /**
     * Creates a closure writer for statically compiled code generation.
     */
    public StaticTypesClosureWriter(WriterController wc) {
        super(wc);
    }

    // Under @CompileStatic the type checker's owner-vs-delegate resolution is authoritative, so the
    // capability analysis can prove a closure delegate-independent and pack it automatically.
    @Override
    protected boolean isPackTriggered(final ClosureExpression expression, final boolean annotated) {
        // Even the @PackedClosures opt-in needs the proof here: a delegate-resolved body compiles its
        // IMPLICIT_RECEIVER expressions against Closure.getDelegate(), which a hoisted method cannot honour.
        return (annotated || SystemUtil.getBooleanSafe(CompilerConfiguration.CLOSURE_PACKING))
                && isDelegateIndependent(expression)
                && !touchesMapOwnerProperties(expression);
    }

    @Override
    protected String triggerDeclineReason(final ClosureExpression expression) {
        if (!isDelegateIndependent(expression)) {
            return "it resolves a name against a delegate (e.g. via @DelegatesTo or with{})";
        }
        if (touchesMapOwnerProperties(expression)) {
            return "it accesses properties of a Map owner, whose closure-context resolution "
                    + "(map entry for members not visible to the closure class) a hoisted body cannot reproduce";
        }
        return null;
    }

    /**
     * Inside a closure, {@code this.x} (and a bare field-bound {@code x}) on an owner that implements
     * {@code Map} deliberately resolves through the property MOP: a member not visible to the closure
     * class falls through to the map entry (the map-property contract pinned by
     * {@code FieldsAndPropertiesSTCTest}). A hoisted body is a real method of the owner, where the
     * same expression legally reaches the field directly — a different answer. Decline packing for
     * closures in Map-implementing owners that touch {@code this}-properties or field-bound names.
     */
    private boolean touchesMapOwnerProperties(final ClosureExpression expression) {
        ClassNode enclosing = controller.getClassNode();
        if (!isOrImplements(enclosing, ClassHelper.MAP_TYPE)) return false;
        Statement code = expression.getCode();
        if (code == null) return false;
        boolean[] touches = {false};
        code.visit(new CodeVisitorSupport() {
            @Override public void visitPropertyExpression(final PropertyExpression pexp) {
                Expression obj = pexp.getObjectExpression();
                if (obj instanceof VariableExpression && ((VariableExpression) obj).isThisExpression()) {
                    touches[0] = true;
                }
                super.visitPropertyExpression(pexp);
            }
            @Override public void visitVariableExpression(final VariableExpression ve) {
                Object av = ve.getAccessedVariable();
                if (av instanceof FieldNode
                        || av instanceof PropertyNode) {
                    touches[0] = true;
                }
                super.visitVariableExpression(ve);
            }
        });
        return touches[0];
    }

    // The static writer types read-only captures (declared or flow-inferred) so the hoisted body
    // compiles with static dispatch, and marks the body for static compilation -- the two seams the
    // base (dynamic) emitter leaves as Object / dynamic.
    @Override
    protected ClassNode readOnlyCaptureType(final String name, final ClassNode declaredType, final Variable variable) {
        if (variable instanceof ASTNode) {
            Object inferred = ((ASTNode) variable).getNodeMetaData(INFERRED_TYPE);
            if (inferred instanceof ClassNode) return erasedType((ClassNode) inferred);
        }
        return declaredType;
    }

    @Override
    protected void markHoistedBody(final MethodNode hoisted) {
        hoisted.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
    }

    @Override
    protected boolean packedClosureUsesDelegateGuard() {
        return false; // the type checker proved delegate-independence, so no runtime guard is needed
    }

    /**
     * True when the type checker resolved every implicitly-received name in the closure (and its
     * nested closures) against the owner/parameters rather than a delegate — the delegate-independence
     * proof of the capability analysis. Under {@code @CompileStatic} STC records each name's resolution
     * path as {@code IMPLICIT_RECEIVER}: a path ending in {@code "owner"} was resolved against the
     * owner (safe), anything else (e.g. {@code "delegate"}, from {@code @DelegatesTo} or {@code with})
     * went through a delegate — the same distinction STC itself makes. The marker can sit on a method
     * call, a property expression, or a bare variable (e.g. {@code length} inside {@code with}), so
     * all three are checked. If nothing resolved through a delegate, packing preserves resolution.
     */
    private boolean isDelegateIndependent(final ClosureExpression expression) {
        Statement code = expression.getCode();
        if (code == null) return false;
        boolean[] delegateResolved = {false};
        // Walk the whole closure tree, including nested closures: a delegate resolution anywhere in
        // the body (e.g. an owner-passed closure whose inner closure resolves a name against the
        // delegate) means the top closure carries a delegate chain that packing would break.
        code.visit(new CodeVisitorSupport() {
            @Override public void visitMethodCallExpression(final MethodCallExpression call) {
                flag(call.getNodeMetaData(IMPLICIT_RECEIVER));
                // an implicit-this call STC left to runtime resolution — DYNAMIC_RESOLUTION from a
                // type-checking extension's makeDynamic (whose virtual MethodNode also lands in
                // DIRECT_METHOD_CALL_TARGET, so that alone cannot be trusted), or no recorded
                // resolution at all — goes through the closure's owner/delegate chain (mixed-mode
                // builder DSLs), so independence is unproven, exactly as for a delegate-resolved name
                if (call.isImplicitThis()
                        && (call.getNodeMetaData(DYNAMIC_RESOLUTION) != null
                            || (call.getNodeMetaData(IMPLICIT_RECEIVER) == null
                                && call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET) == null))) {
                    delegateResolved[0] = true;
                }
                super.visitMethodCallExpression(call);
            }
            @Override public void visitPropertyExpression(final PropertyExpression pexp) {
                flag(pexp.getNodeMetaData(IMPLICIT_RECEIVER));
                if (pexp.isImplicitThis() && pexp.getNodeMetaData(DYNAMIC_RESOLUTION) != null) {
                    delegateResolved[0] = true; // extension-forced dynamic property (see above)
                }
                super.visitPropertyExpression(pexp);
            }
            @Override public void visitVariableExpression(final VariableExpression ve) {
                flag(ve.getNodeMetaData(IMPLICIT_RECEIVER));
                // a name STC left dynamic (no local/param binding, no recorded receiver path) will be
                // resolved at runtime through the closure's owner/delegate chain -- not provably owner
                if (ve.getAccessedVariable() instanceof DynamicVariable
                        && ve.getNodeMetaData(IMPLICIT_RECEIVER) == null) {
                    delegateResolved[0] = true;
                }
                super.visitVariableExpression(ve);
            }
            private void flag(final Object receiver) {
                if (receiver instanceof String && !((String) receiver).endsWith("owner")) delegateResolved[0] = true;
            }
        });
        return !delegateResolved[0];
    }

    /** {@inheritDoc} */
    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int mods) {
        ClassNode closureClass = super.createClosureClass(expression, mods);
        List<MethodNode> methods = closureClass.getDeclaredMethods("call");
        List<MethodNode> doCalls = closureClass.getMethods("doCall");
        if (doCalls.size() != 1) {
            throw new GroovyBugError("Expected to find one (1) doCall method on generated closure, but found " + doCalls.size());
        }
        MethodNode doCallMethod = doCalls.get(0);
        if (methods.isEmpty() && doCallMethod.getParameters().length == 1) {
            createDirectCallMethod(closureClass, doCallMethod);
        }
        MethodTargetCompletionVisitor visitor = new MethodTargetCompletionVisitor(doCallMethod);
        for (MethodNode method : methods) {
            visitor.visitMethod(method);
        }
        // GROOVY-11998: when the closure literal is the source of an intersection
        // cast, declare the additional marker interfaces on the generated class so
        // the resulting object IS-A every component without going through a
        // runtime proxy. Markers are only added when they are true marker
        // interfaces (no abstract methods we'd be obliged to implement).
        addIntersectionMarkers(closureClass, expression);
        closureClass.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        return closureClass;
    }

    @SuppressWarnings("unchecked")
    private static void addIntersectionMarkers(final ClassNode closureClass, final ClosureExpression expression) {
        Object md = expression.getNodeMetaData(LAMBDA_MARKERS);
        if (!(md instanceof List)) return;
        List<ClassNode> markers = (List<ClassNode>) md;
        for (ClassNode marker : markers) {
            if (marker == null || !marker.isInterface()) continue;
            if (closureClass.implementsInterface(marker)) continue;
            // Only add interfaces with no abstract methods (true markers). For
            // interfaces that declare unimplemented abstract methods, we'd
            // have to synthesize method bodies — out of scope here, fall back
            // to the runtime proxy path in IntersectionCastSupport.asType.
            if (hasAbstractMethods(marker)) continue;
            closureClass.addInterface(marker);
        }
    }

    private static boolean hasAbstractMethods(final ClassNode iface) {
        for (MethodNode m : iface.getMethods()) {
            if (m.isAbstract() && !m.isDefault() && !m.isStatic()) return true;
        }
        return false;
    }

    private static void createDirectCallMethod(final ClassNode closureClass, final MethodNode doCallMethod) {
        // in case there is no "call" method on the closure, create a "fast invocation" path
        // to avoid going through ClosureMetaClass by call(Object...) method

        // we can't have a specialized version of call(Object...) because the dispatch logic
        // in ClosureMetaClass is too complex!

        // call(Object)
        Parameter doCallParam = doCallMethod.getParameters()[0];
        Parameter args = new Parameter(doCallParam.getType(), "args");
        addGeneratedCallMethod(closureClass, doCallMethod, varX(args), new Parameter[]{args});

        // call()
        addGeneratedCallMethod(closureClass, doCallMethod, defaultArgument(doCallParam), Parameter.EMPTY_ARRAY);
    }

    private static Expression defaultArgument(final Parameter parameter) {
        Expression argument;
        if (parameter.hasInitialExpression()) {
            argument = parameter.getInitialExpression();
        } else if (parameter.getType().isArray()) {
            ClassNode elementType = parameter.getType().getComponentType();
            argument = new ArrayExpression(elementType, null, Collections.singletonList(constX(0, true)));
        } else {
            argument = nullX();
        }
        return argument;
    }

    private static void addGeneratedCallMethod(ClassNode closureClass, MethodNode doCallMethod, Expression expression, Parameter[] params) {
        MethodCallExpression callDoCall = callX(varX("this", closureClass), "doCall", args(expression));
        callDoCall.setImplicitThis(true);
        callDoCall.setMethodTarget(doCallMethod);
        MethodNode call = new MethodNode("call",
                Opcodes.ACC_PUBLIC,
                ClassHelper.OBJECT_TYPE,
                params,
                ClassNode.EMPTY_ARRAY,
                returnS(callDoCall));
        addGeneratedMethod(closureClass, call, true);
    }

    //--------------------------------------------------------------------------

    private static final class MethodTargetCompletionVisitor extends ClassCodeVisitorSupport {

        private final MethodNode doCallMethod;

        private MethodTargetCompletionVisitor(final MethodNode doCallMethod) {
            this.doCallMethod = doCallMethod;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitMethodCallExpression(final MethodCallExpression call) {
            super.visitMethodCallExpression(call);
            MethodNode mn = call.getMethodTarget();
            if (mn == null) {
                call.setMethodTarget(doCallMethod);
            }
        }
    }
}
