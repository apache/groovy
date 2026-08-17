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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-12166: statement-level {@code instanceof} narrowing keyed by a shared
 * field node must not leak out of the member being visited into other members
 * of the class (spurious {@code checkcast} → {@code ClassCastException}).
 */
final class Groovy12166 {

    private static final String TYPES = '''
        abstract class Base {
            String describe() { 'base' }
            abstract void unrelated()
        }
        class Alpha extends Base { void unrelated() {} }
        class Beta  extends Base { void unrelated() {} }
    '''

    @Test
    void testExplicitReturnInstanceofDoesNotNarrowFieldClassWide() {
        assertScript TYPES + '''
            @groovy.transform.CompileStatic
            class Holder {
                private Base field
                Holder(Base f) { this.field = f }
                boolean isAlpha() { return field instanceof Alpha }
                String use() { field.describe() }
            }
            def h = new Holder(new Beta())
            assert !h.isAlpha()
            assert h.use() == 'base'
        '''
    }

    @Test
    void testAssertInstanceofDoesNotNarrowFieldClassWide() {
        assertScript TYPES + '''
            @groovy.transform.CompileStatic
            class Holder {
                private Base field
                Holder(Base f) { this.field = f }
                void check() { assert field instanceof Alpha }
                String use() { field.describe() }
            }
            // check() is never called; field holds a Beta
            assert new Holder(new Beta()).use() == 'base'
        '''
    }

    @Test
    void testNarrowingIsIndependentOfMethodDeclarationOrder() {
        assertScript TYPES + '''
            @groovy.transform.CompileStatic
            class Holder {
                private Base field
                Holder(Base f) { this.field = f }
                String use() { field.describe() }
                boolean isAlpha() { return field instanceof Alpha }
                String useAfter() { field.describe() }
            }
            def h = new Holder(new Beta())
            assert !h.isAlpha()
            assert h.use() == 'base'
            assert h.useAfter() == 'base'
        '''
    }

    @Test
    void testFieldInitializerInstanceofDoesNotNarrowClassWide() {
        assertScript TYPES + '''
            @groovy.transform.CompileStatic
            class Holder {
                private Base field
                private boolean flag = (field instanceof Alpha)
                Holder(Base f) { this.field = f }
                String use() { field.describe() }
            }
            assert new Holder(new Beta()).use() == 'base'
        '''
    }

    @Test
    void testIntraMethodNarrowingStillWorks() {
        // narrowing within a member must be unaffected: assert flow typing and
        // if-branch narrowing still apply to subsequent statements
        assertScript TYPES + '''
            class Gamma extends Base {
                void unrelated() {}
                String extra() { 'gamma' }
            }
            @groovy.transform.CompileStatic
            class Holder {
                private Base field
                Holder(Base f) { this.field = f }
                String viaAssert() {
                    assert field instanceof Gamma
                    field.extra() // narrowing from assert applies here
                }
                String viaIf() {
                    if (field instanceof Gamma) {
                        return field.extra()
                    }
                    'other'
                }
            }
            def h = new Holder(new Gamma())
            assert h.viaAssert() == 'gamma'
            assert h.viaIf() == 'gamma'
        '''
    }
}
