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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.classgen.asm.InstructionSequence
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

/**
 * GROOVY-11792 bytecode documentation tests.
 * <p>
 * Behavioural coverage lives in {@link Groovy11792}. These tests pin the
 * classgen contract for for-in holder stores after {@code Iterator#next}:
 * <ul>
 *   <li><b>new (default)</b> — allocate a fresh {@code groovy.lang.Reference}
 *       each iteration ({@code NEW} + {@code <init>(Object)} + {@code ASTORE})</li>
 *   <li><b>old (legacy opt-out)</b> — in-place {@code Reference#set} on the
 *       shared holder ({@code ALOAD} + {@code CHECKCAST} + {@code set})</li>
 * </ul>
 * Both modes still create an initial holder {@code Reference} before the loop
 * ({@code defineVariable}); the distinguishing site is the store that follows
 * {@code Iterator.next} inside the loop.
 */
final class Groovy11792BytecodeTest extends AbstractBytecodeTestCase {

    /**
     * Minimal for-in + deferred closure capture (no body assignment to the
     * loop variable, so the only {@code Reference#set} would be the legacy
     * loop-head store).
     */
    private static final String FOR_IN_CAPTURE_SCRIPT = '''
def suppliers = []
for (n in [1, 2, 3]) {
    suppliers << { n }
}
'''

    /**
     * New loop-head store: after {@code next()}, wrap the element in a fresh
     * {@code Reference} and store it into the loop-variable slot.
     */
    private static final List<String> NEW_LOOP_STORE = [
            'INVOKEINTERFACE java/util/Iterator.next ()Ljava/lang/Object;',
            'NEW groovy/lang/Reference',
            'DUP_X1',
            'SWAP',
            'INVOKESPECIAL groovy/lang/Reference.<init> (Ljava/lang/Object;)V',
    ]

    /**
     * Historical loop-head store: after {@code next()}, update the existing
     * shared {@code Reference} in place.
     */
    private static final List<String> OLD_LOOP_STORE = [
            'INVOKEINTERFACE java/util/Iterator.next ()Ljava/lang/Object;',
            'ALOAD',
            'CHECKCAST groovy/lang/Reference',
            'SWAP',
            'INVOKEVIRTUAL groovy/lang/Reference.set (Ljava/lang/Object;)V',
    ]

    private InstructionSequence compileForInCapture(final boolean forLoopCaptureEnabled) {
        CompilerConfiguration config = new CompilerConfiguration()
        config.setForLoopCaptureEnabled(forLoopCaptureEnabled)
        CompilationUnit cu = new CompilationUnit(config)
        cu.addSource('script', FOR_IN_CAPTURE_SCRIPT)
        cu.compile(Phases.CLASS_GENERATION)
        def gc = cu.classes.find { it.name == cu.firstClassNode.name }
        extractSequence(gc.bytes, [method: 'run'])
    }

    @Test
    void testNewBytecodePatternWhenForLoopCaptureEnabled() {
        // Default language-compat: per-iteration fresh Reference after next()
        InstructionSequence bytecode = compileForInCapture(true)

        assert bytecode.hasStrictSequence(NEW_LOOP_STORE) :
                "expected fresh Reference after Iterator.next:\n${bytecode}"
        // No in-place set of the shared holder in the loop head
        assert !bytecode.hasSequence(OLD_LOOP_STORE) :
                "legacy Reference#set loop store must not appear:\n${bytecode}"
        assert !bytecode.hasSequence([
                'INVOKEVIRTUAL groovy/lang/Reference.set (Ljava/lang/Object;)V',
        ]) : "Reference#set must not appear when body does not assign:\n${bytecode}"
    }

    @Test
    void testOldBytecodePatternWhenForLoopCaptureDisabled() {
        // Historical opt-out: single Reference updated with set() after next()
        InstructionSequence bytecode = compileForInCapture(false)

        assert bytecode.hasStrictSequence(OLD_LOOP_STORE) :
                "expected in-place Reference#set after Iterator.next:\n${bytecode}"
        // Loop head must not allocate a new Reference after next()
        assert !bytecode.hasStrictSequence(NEW_LOOP_STORE) :
                "fresh Reference after Iterator.next must not appear:\n${bytecode}"
    }
}
