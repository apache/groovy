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
package groovy.json

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Demonstrates the converse of the nesting-depth guard: when the cap is raised above the
 * document's depth (so it never fires), the recursive-descent parser falls back to its old
 * behaviour and overflows the stack on a deeply nested document.
 * <p>
 * This is the test that justifies the feature — it shows the cap is exactly what stands between
 * an untrusted document and a {@link StackOverflowError}. It runs in a freshly forked JVM with a
 * deliberately small thread stack ({@code -Xss256k}) so that the overflow is reached at a modest,
 * controlled depth and, crucially, the (recoverable but disruptive) {@link StackOverflowError} is
 * provoked in a disposable child JVM rather than in the shared test JVM.
 *
 * @see JsonSlurperNestingDepthTest
 */
@ForkedJvm(jvmArgs = ['-Xss256k'])
final class JsonSlurperStackOverflowTest {

    @Test
    void raisingTheCapReintroducesStackOverflow() {
        int depth = 50000
        String deeplyNested = '[' * depth + ']' * depth

        // Cap raised well above the document depth, so the guard never trips and the parser
        // recurses freely into the small (-Xss256k) stack.
        def slurper = new JsonSlurper().setMaxNestingDepth(depth * 2)

        shouldFail(StackOverflowError) {
            slurper.parseText(deeplyNested)
        }
    }

    @Test
    void capStillProtectsOnTheSameSmallStack() {
        // On the very same small stack, a low cap converts the overflow into a clean
        // JsonException instead — the guard fires before the recursion can exhaust the stack.
        int depth = 50000
        String deeplyNested = '[' * depth + ']' * depth

        def slurper = new JsonSlurper().setMaxNestingDepth(100)

        shouldFail(JsonException) {
            slurper.parseText(deeplyNested)
        }
    }
}
