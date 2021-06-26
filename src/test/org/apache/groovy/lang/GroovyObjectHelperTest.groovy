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
package org.apache.groovy.lang

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

class GroovyObjectHelperTest {
    @Test
    void testLookup() {
        assertScript '''
            import org.apache.groovy.lang.GroovyObjectHelper
            class Base {}
            class Outer {
                static class StaticInner {
                    static class StaticInnest {}
                    static class StaticInnest2 extends StaticInner2 {}
                }
                static class StaticInner2 extends Base {}
                class Inner {}
                class Inner2 extends Inner3 {}
                class Inner3 extends Base {}
            }
            
            assert Outer.class === GroovyObjectHelper.lookup(new Outer()).get().lookupClass()
            assert Outer.Inner.class === GroovyObjectHelper.lookup(new Outer().new Inner()).get().lookupClass()
            assert Outer.Inner2.class === GroovyObjectHelper.lookup(new Outer().new Inner2()).get().lookupClass()
            assert Outer.Inner3.class === GroovyObjectHelper.lookup(new Outer().new Inner3()).get().lookupClass()
            assert Outer.StaticInner.class === GroovyObjectHelper.lookup(new Outer.StaticInner()).get().lookupClass()
            assert Outer.StaticInner2.class === GroovyObjectHelper.lookup(new Outer.StaticInner2()).get().lookupClass()
            assert Outer.StaticInner.StaticInnest.class === GroovyObjectHelper.lookup(new Outer.StaticInner.StaticInnest()).get().lookupClass()
            assert Outer.StaticInner.StaticInnest2.class === GroovyObjectHelper.lookup(new Outer.StaticInner.StaticInnest2()).get().lookupClass()
        '''
    }
}
