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

class Groovy3770Bug extends GroovyTestCase {
    void testSetDelegateAndResolveStrategyOnACurriedClosure() {
        assertScript """
            void hello(who) {
                println ("Hello " + who)
            }
            
            def c = { x ->
                hello(x)
            }
            
            def d = c.curry("Ian")
            d.call()
            
            d.delegate = null

            assert d.delegate == null

            d.resolveStrategy = Closure.DELEGATE_ONLY

            try {
                d.call()
                throw new RuntimeException("The curried closure call should have failed here with MME")
            } catch(MissingMethodException ex) {
                // ok if closure call returned in an exception (MME)
            }
        """
    }
    
    void testCurriedClosuresShouldNotAffectParent() {
        // GROOVY-3875
        def orig = { tmp -> assert tmp == 1 }
        def curriedOrig = orig.curry(1)
        assert orig != curriedOrig.getOwner()
    }
}
