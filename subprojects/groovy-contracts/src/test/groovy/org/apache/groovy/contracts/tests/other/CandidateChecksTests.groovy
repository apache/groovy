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

import org.apache.groovy.contracts.generation.CandidateChecks
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

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
        assertTrue(CandidateChecks.isPreconditionCandidate(classNode, classNode.getDeclaredConstructors().first()),
                "private constructors should support preconditions")
        assertTrue(CandidateChecks.isPreconditionCandidate(classNode, classNode.getMethod("method", [] as Parameter[])),
                "private methods should support preconditions")

        assertFalse(CandidateChecks.isClassInvariantCandidate(classNode, classNode.getDeclaredConstructors().first()),
                "private constructors should by now NOT support class invariants")
    }
}
