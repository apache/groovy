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
package groovy.bugs

class Groovy3339Bug extends GroovyTestCase {
    void testConstantCachingInClosureClasses() {
        // In all the cases below, the OptimizerVisitor replaces number 10 by
        // the cached constant in the closure class, which it is not supposed
        // to do as the current implementation does not add synthetic fields
        // for constant caching in closure classes. So the closure invocation currently
        // fails in these cases with errors like "java.lang.NoSuchFieldError: $const$0"
        for (myVal in evaluate('[10,11,12]', 2, 'foo') { [10, 11, 12] }) {
            println myVal
        }

        while (evaluate2({10})) {
            // do nothing
        }

        synchronized (evaluate({10})) {
            // do nothing
        }
    }

    def evaluate(closure) {
        def res = closure.call()
        assert res == 10
        return res
    }

    def evaluate(text, num, thing, closure) {
        closure.call()
    }

    def evaluate2(closure) {
        def res = closure.call()
        assert res == 10
        return null
    }
}