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
package gls.syntax

import gls.CompilableTestSupport

class OldClosureSyntaxRemovalTest extends CompilableTestSupport {
    def a = 2
    def b = 3

    void testOneParameter() {
        def newClosure = { a -> a }
        def oldClosure = { a | b }
        assert newClosure(1) == 1
        assert oldClosure.getMaximumNumberOfParameters() == 1
        // the old closure would have cimply returned b
        // after removal this is the logic or
        assert oldClosure(1) == (a | b)
    }

    void testMultipleParameters() {
        shouldNotCompile """
            c = {a,b|a+b}
        """
        shouldCompile """
            c = { a,b -> a+b }
        """
    }
}