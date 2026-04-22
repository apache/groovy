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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.tools.GroovyClass
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Boundary test for GROOVY-11955 — routing shape of receiver-chain method calls.
 *
 * <p>This is NOT a language requirement. The JVM's 64KB method-size limit caps
 * any large generated method and Spock's {@code verifyAll} will eventually hit
 * that ceiling no matter what we do. What this test guards is the <em>routing
 * decision</em> that determines how quickly that ceiling is reached: when an
 * AST transform pre-resolves a method call's target (Spock's transform does
 * this for every {@code ValueRecorder.record} call), the indy writer should
 * emit a direct {@code invokevirtual} for short receiver chains, not route
 * through an {@code invokedynamic} callsite. Routing through indy inflates
 * per-call bytecode by ~2 bytes and is measurably slower at runtime (~40% on
 * the original Spock reproducer after JIT warmup).
 *
 * <p>If this test fails, the fix is <em>not</em> to bump the threshold. Think
 * about whether the routing change is deliberate, re-measure the Spock
 * {@code verifyAll} cliff (the original regression shifted it from 596 to
 * 586 asserts at 500 assertions tested), and update the comment with the new
 * rationale.
 *
 * <p>See also {@code InvokeDynamicWriter.CHAIN_FLATTEN_THRESHOLD} and the
 * companion GROOVY-7785 test which exercises the iterative/flatten path.
 */
final class Groovy11955 {

    /**
     * Fixture: {@code endObj()} returns {@code Object} so that a trailing
     * property access on the chain result forces the *outer* expression
     * through {@code InvokeDynamicWriter.prepareIndyCall} (the code path the
     * patch modifies). Without that, {@code InvocationWriter.makeDirectCall}
     * short-circuits the whole chain onto invokevirtual before the indy
     * writer ever sees it.
     */
    private static final String HELPER_SRC = '''
        class Helper {
            Helper step() { this }
            Object endObj() { [field: 'x'] }
        }
    '''

    /**
     * Mimics what Spock's AST transform does: for every MCE whose method name
     * matches a Helper method, set {@code methodTarget} so
     * {@code InvocationWriter.makeDirectCall} takes the invokevirtual fast
     * path when the recursive visitor sees it.
     */
    static class ResolveHelperTargets extends CompilationCustomizer {
        ResolveHelperTargets() { super(CompilePhase.SEMANTIC_ANALYSIS) }

        @Override
        void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            ClassNode helperCN = source.AST.classes.find { it.nameWithoutPackage == 'Helper' }
            if (!helperCN) return
            classNode.visitContents(new ClassCodeVisitorSupport() {
                @Override protected SourceUnit getSourceUnit() { source }

                @Override
                void visitMethodCallExpression(MethodCallExpression mce) {
                    super.visitMethodCallExpression(mce)
                    if (mce.methodTarget != null) return
                    String name = mce.method.text
                    List<MethodNode> candidates = helperCN.getMethods(name)
                    if (candidates.size() == 1) mce.methodTarget = candidates[0]
                }
            })
        }
    }

    /** Compile Helper + the given script body and return the script class bytes. */
    private static byte[] compile(String scriptBody) {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ResolveHelperTargets())
        def cu = new CompilationUnit(config)
        cu.addSource('Groovy11955Src.groovy', HELPER_SRC + scriptBody)
        cu.compile(Phases.CLASS_GENERATION)
        GroovyClass scriptClass = cu.classes.find { it.name == 'Groovy11955Src' }
        assert scriptClass : "no script class; got: ${cu.classes*.name}"
        return scriptClass.bytes
    }

    /**
     * Count method-call instruction shapes in {@code run()} of the compiled
     * class, filtered to those targeting Helper. The iterative chain-flatten
     * indy calls all carry call site name {@code "invoke"} and the target
     * method name as the first BSM arg.
     */
    private static Map<String, Integer> helperCalls(byte[] classBytes) {
        def out = [invokevirtual: 0, invokedynamic: 0]
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exs) {
                if (name != 'run') return null
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    void visitMethodInsn(int op, String owner, String n, String d, boolean itf) {
                        if (op == Opcodes.INVOKEVIRTUAL && owner == 'Helper') {
                            out.invokevirtual++
                        }
                    }
                    @Override
                    void visitInvokeDynamicInsn(String n, String d, Handle bsm, Object... args) {
                        if (n == 'invoke' && args.length > 0) {
                            String mname = String.valueOf(args[0])
                            if (mname == 'step' || mname == 'endObj') out.invokedynamic++
                        }
                    }
                }
            }
        }, 0)
        return out
    }

    // -- Tests ---------------------------------------------------------------

    @Test
    void shortChainKeepsChainReceiverCallsOnInvokeVirtual() {
        // Chain depth 4 as the receiver of a property access (well below
        // CHAIN_FLATTEN_THRESHOLD). With pre-resolved targets, all four
        // Helper calls must emit invokevirtual — not invokedynamic.
        def src = '''
            def h = new Helper()
            h.step().step().step().endObj().field
        '''
        def c = helperCalls(compile(src))

        assertTrue(c.invokevirtual >= 4,
            "expected >= 4 Helper invokevirtual calls, got ${c}")
        assertTrue(c.invokedynamic == 0,
            "expected 0 Helper invokedynamic calls for short chain, got ${c}")
    }

    @Test
    void longChainFlattensToInvokeDynamic() {
        // Depth > CHAIN_FLATTEN_THRESHOLD (64): iterative path engages;
        // every Helper call in the receiver chain is emitted as an indy
        // callsite rather than invokevirtual.
        def sb = new StringBuilder('def h = new Helper(); h')
        80.times { sb << '.step()' }
        sb << '.endObj().field'
        def c = helperCalls(compile(sb.toString()))

        // Exact count isn't the point — presence of many indy calls is.
        assertTrue(c.invokedynamic >= 60,
            "expected flatten path to emit many Helper indy calls, got ${c}")
    }
}
