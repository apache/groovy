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
package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

/**
 * GROOVY-11968 (follow-up to GROOVY-11817 / GROOVY-11907): a sub-expression marked
 * DYNAMIC_RESOLUTION inside a @CompileStatic method delegates mid-method to the
 * regular CallSiteWriter via getCallSiteWriterFor. The regular writer's per-method
 * state must be initialized at method entry; otherwise its prepareCallSite()
 * emits ALOAD against an unallocated local slot, producing methods whose first
 * instruction references a local beyond max_locals (VerifyError at class load).
 *
 * Trait static-field access is the most accessible reproducer: $static$self.T__d$set
 * is marked DO_DYNAMIC by TraitReceiverTransformer and translated to DYNAMIC_RESOLUTION
 * by TraitTypeCheckingExtension. With invokedynamic the cached call-site array is not
 * used and the bug is masked; with indy=false the verifier rejects the helper.
 */
final class Groovy11968 {

    private static final String SCRIPT = '''
        @groovy.transform.CompileStatic
        trait T {
            static double  d = 1.0d
            static long    l = 1L
            static String  s = 'hello'

            static double  bumpD() { d = d + 1.0d; d }
            static long    bumpL() { l = l + 1L;   l }
            static String  wrap(String x) { "[$s/$d/$l] $x" }
        }

        @groovy.transform.CompileStatic
        class C implements T {
            String run() {
                d = 41.0d
                l = 41L
                s = 'world'
                bumpD()
                bumpL()
                wrap('ok')
            }
        }

        assert new C().run() == '[world/42.0/42] ok'
    '''

    @Test
    void testTraitStaticFieldHelperLoadsAndRunsUnderIndy() {
        runWithIndy(true)
    }

    @Test
    void testTraitStaticFieldHelperLoadsAndRunsWithoutIndy() {
        runWithIndy(false)
    }

    private static void runWithIndy(boolean indy) {
        def config = new CompilerConfiguration()
        config.optimizationOptions[CompilerConfiguration.INVOKEDYNAMIC] = indy
        // Loading the helper class is what triggers the verifier; an assertion
        // inside the script proves runtime semantics are also correct.
        new GroovyShell(this.class.classLoader, new Binding(), config).evaluate(SCRIPT)
    }
}
