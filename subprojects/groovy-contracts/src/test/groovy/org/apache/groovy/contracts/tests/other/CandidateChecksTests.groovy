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
package org.apache.groovy.contracts.tests.other

import org.codehaus.groovy.ast.ClassHelper
import org.apache.groovy.contracts.generation.CandidateChecks
import org.junit.Test
import org.codehaus.groovy.ast.Parameter

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class A {

}

interface B {

}

enum C {

}

class D {
    private D() {}

    private def method() {}
}

/**
 * all test cases for {@link CandidateChecks}.
 *
 * @see CandidateChecks
 */
class CandidateChecksTests {

    @Test
    void testContractsCandidateChecks() {
        assert !CandidateChecks.isContractsCandidate(ClassHelper.make(B.class))
        assert !CandidateChecks.isContractsCandidate(ClassHelper.make(C.class))
        assert CandidateChecks.isContractsCandidate(ClassHelper.make(A.class))
    }

    // refs #22
    @Test
    void testPrivateConstructors() {
        def classNode = ClassHelper.make(D.class)
        assertTrue "private constructors should support preconditions",
                CandidateChecks.isPreconditionCandidate(classNode, classNode.getDeclaredConstructors().first())
        assertTrue "private methods should support preconditions",
                CandidateChecks.isPreconditionCandidate(classNode, classNode.getMethod("method", [] as Parameter[]))

        assertFalse "private constructors should by now NOT support class invariants",
                CandidateChecks.isClassInvariantCandidate(classNode, classNode.getDeclaredConstructors().first())
    }
}
