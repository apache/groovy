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

import org.codehaus.groovy.classgen.InstanceofFlowBindings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Codegen helper that publishes/hides {@link CompileStack} local <em>slots</em>
 * for {@code instanceof} pattern variables according to
 * {@link InstanceofFlowBindings} (GROOVY-12242 / JEP 394).
 * <p>
 * Distinct from {@link InstanceofFlowBindings}: that type is AST flow
 * <em>analysis</em>; this type is bytecode <em>slot control</em>.
 * <p>
 * {@code evaluateInstanceof} defines pattern slots while the condition runs
 * (needed for short-circuit {@code &&} RHS). After the condition, this helper
 * <em>hides</em> every captured slot and then <em>publishes</em> only names
 * live on the current control-flow path:
 * <ul>
 *   <li>then-block → {@link InstanceofFlowBindings#whenTrueNames()}</li>
 *   <li>else-block → {@link InstanceofFlowBindings#whenFalseNames()}</li>
 *   <li>after the if → opposite path when a branch cannot complete normally</li>
 * </ul>
 * That “hide all, publish path” rule keeps CompileStack polarity aligned with
 * {@link org.codehaus.groovy.classgen.VariableScopeVisitor}.
 *
 * @see InstanceofFlowBindings
 * @since 6.0.0
 */
final class InstanceofFlowSlotPublisher {

    private static final InstanceofFlowSlotPublisher NONE =
            new InstanceofFlowSlotPublisher(InstanceofFlowBindings.of(null), Map.of());

    private final InstanceofFlowBindings bindings;
    private final Map<String, BytecodeVariable> captured;

    private InstanceofFlowSlotPublisher(final InstanceofFlowBindings bindings,
                                        final Map<String, BytecodeVariable> captured) {
        this.bindings = bindings;
        this.captured = captured;
    }

    /**
     * Snapshots pattern slots defined while evaluating {@code bindings}' condition
     * and removes them from {@code compileStack} so no branch sees unscoped slots.
     *
     * @param compileStack current compile stack (condition already evaluated)
     * @param bindings     flow-analysis result for that condition
     * @return a publisher for path-scoped reintroduction of the captured slots
     */
    static InstanceofFlowSlotPublisher captureAndHide(final CompileStack compileStack,
                                                      final InstanceofFlowBindings bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return NONE;
        }
        Map<String, BytecodeVariable> captured = new HashMap<>();
        for (String name : bindings.allNames()) {
            BytecodeVariable bv = compileStack.getVariable(name, false);
            if (bv != null) {
                captured.put(name, bv);
                compileStack.removeVariable(name);
            }
        }
        if (captured.isEmpty()) {
            return NONE;
        }
        return new InstanceofFlowSlotPublisher(bindings, Collections.unmodifiableMap(captured));
    }

    boolean isEmpty() {
        return captured.isEmpty();
    }

    /** Makes true-path pattern locals visible on the current CompileStack frame. */
    void publishTrue(final CompileStack compileStack) {
        publish(compileStack, bindings.whenTrueNames());
    }

    /** Makes false-path pattern locals visible on the current CompileStack frame. */
    void publishFalse(final CompileStack compileStack) {
        publish(compileStack, bindings.whenFalseNames());
    }

    /** Hides true-path pattern locals (end of then-block). */
    void hideTrue(final CompileStack compileStack) {
        hide(compileStack, bindings.whenTrueNames());
    }

    /** Hides false-path pattern locals (end of else-block). */
    void hideFalse(final CompileStack compileStack) {
        hide(compileStack, bindings.whenFalseNames());
    }

    /**
     * Publishes bindings that remain in scope after the if, matching Java's
     * abrupt-completion rule: opposite-path bindings survive when a branch
     * cannot complete normally.
     *
     * @param ifFallsThrough  whether the then-block may complete normally
     * @param elseEmpty       whether there is no else branch
     * @param elseFallsThrough whether the else-block may complete normally
     */
    void publishAfterIf(final CompileStack compileStack,
                        final boolean ifFallsThrough,
                        final boolean elseEmpty,
                        final boolean elseFallsThrough) {
        if (!ifFallsThrough) {
            publishFalse(compileStack);
        }
        if (!elseEmpty && !elseFallsThrough) {
            publishTrue(compileStack);
        }
    }

    private void publish(final CompileStack compileStack, final Set<String> names) {
        if (captured.isEmpty() || names.isEmpty()) return;
        for (String name : names) {
            BytecodeVariable bv = captured.get(name);
            if (bv != null) {
                compileStack.putVariable(bv);
            }
        }
    }

    private void hide(final CompileStack compileStack, final Set<String> names) {
        if (captured.isEmpty() || names.isEmpty()) return;
        for (String name : names) {
            if (captured.containsKey(name)) {
                compileStack.removeVariable(name);
            }
        }
    }
}
