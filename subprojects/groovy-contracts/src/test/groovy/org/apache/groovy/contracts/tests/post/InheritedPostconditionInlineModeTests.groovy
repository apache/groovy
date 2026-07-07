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
package org.apache.groovy.contracts.tests.post

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * The inline-mode flag is per contract *kind*: preconditions are processed before
 * postconditions, and a single shared flag let the precondition pass's "no
 * inherited preconditions" verdict leak into the postcondition generator, whose
 * inline path then dropped composed inherited postconditions — a child declaring
 * its own {@code @Requires} silently disabled its parent's {@code @Ensures}.
 */
class InheritedPostconditionInlineModeTests extends BaseTestClass {

    @Test
    void inheritedEnsuresEnforcedWithoutChildRequires() {
        assertScript '''
            import groovy.contracts.*
            import org.apache.groovy.contracts.PostconditionViolation

            class Base1 {
                @Ensures({ result >= 0 })
                int f(int n) { n }
            }
            class Child1 extends Base1 {
                @Ensures({ result != 5 })
                @Override int f(int n) { n }
            }
            try {
                new Child1().f(-3)     // violates ONLY the parent ensures
                assert false
            } catch (PostconditionViolation expected) {
            }
        '''
    }

    @Test
    void childRequiresDoesNotSuppressInheritedEnsures() {
        assertScript '''
            import groovy.contracts.*
            import org.apache.groovy.contracts.PostconditionViolation

            class Base2 {
                @Ensures({ result >= 0 })
                int f(int n) { n }
            }
            class Child2 extends Base2 {
                // no @Requires anywhere in the hierarchy: the precondition pass sees an empty
                // inherited set — which must not push the postcondition pass onto the inline
                // path that drops the composed parent ensures
                @Requires({ n != 42 })
                @Ensures({ result != 5 })
                @Override int f(int n) { n }
            }
            assert new Child2().f(3) == 3
            try {
                new Child2().f(-3)     // violates ONLY the parent ensures
                assert false
            } catch (PostconditionViolation expected) {
            }
        '''
    }

    @Test
    void unwovenParentRequiresDoesNotSuppressInheritedEnsures() {
        assertScript '''
            import groovy.contracts.*
            import org.apache.groovy.contracts.PostconditionViolation

            class Base3 {
                @Requires(value = { n > -100 }, woven = false)   // filtered from weaving entirely
                @Ensures({ result >= 0 })
                int f(int n) { n }
            }
            class Child3 extends Base3 {
                // the parent arm filters to an empty inherited-precondition set (the unwoven
                // widening of the same hazard) — the parent ensures must still be enforced
                @Requires({ n != 42 })
                @Ensures({ result != 5 })
                @Override int f(int n) { n }
            }
            try {
                new Child3().f(-3)
                assert false
            } catch (PostconditionViolation expected) {
            }
        '''
    }
}
