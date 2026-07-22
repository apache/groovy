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
 * A union {@code instanceof} ({@code x instanceof A || x instanceof B}) makes the
 * type checker infer a {@code UnionTypeClassNode} for {@code x} inside the branch.
 * That node is documented to never leave the type checker, but a member access on
 * the union-typed receiver — dynamic subscript {@code x['p']} or property {@code x.p}
 * — let it leak into {@code @CompileStatic} instruction selection, where the node's
 * unsupported structural methods threw {@link UnsupportedOperationException}:
 * <pre>General error during instruction selection: java.lang.UnsupportedOperationException</pre>
 * The pattern compiled and ran under Groovy 4 and dynamically, but crashed the static
 * compiler from Groovy 5 (GROOVY-12172). These tests pin that member access on a union
 * receiver compiles and yields the same result as the dynamic path.
 */
final class Groovy12172 {

    // the reporter's example: union instanceof + dynamic subscript under @CompileStatic
    @Test
    void testUnionInstanceofSubscriptAccess() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class X1 { Long id }
            @CompileStatic
            class X2 { Long id }
            @CompileStatic
            class Y {
                Long computeId(Object test) {
                    if (test instanceof X1 || test instanceof X2) {
                        return test['id'] as Long
                    }
                    null
                }
            }
            assert new Y().computeId(new X1(id: 12)) == 12L
            assert new Y().computeId(new X2(id: 34)) == 34L
            assert new Y().computeId('other') == null
        '''
    }

    // the same union receiver reached via direct property access rather than subscript
    @Test
    void testUnionInstanceofPropertyAccess() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class X1 { Long id }
            @CompileStatic
            class X2 { Long id }
            @CompileStatic
            Long computeId(Object test) {
                if (test instanceof X1 || test instanceof X2) {
                    return test.id
                }
                null
            }
            assert computeId(new X1(id: 12)) == 12L
            assert computeId(new X2(id: 34)) == 34L
        '''
    }

    // a union of three types exercises the same LUB/union path
    @Test
    void testUnionOfThreeTypesPropertyAccess() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class X1 { Long id }
            @CompileStatic
            class X2 { Long id }
            @CompileStatic
            class X3 { Long id }
            @CompileStatic
            Long computeId(Object test) {
                if (test instanceof X1 || test instanceof X2 || test instanceof X3) {
                    return test.id
                }
                null
            }
            assert computeId(new X1(id: 1)) == 1L
            assert computeId(new X2(id: 2)) == 2L
            assert computeId(new X3(id: 3)) == 3L
        '''
    }

    // a shared supertype: the union collapses to a common interface with the member
    @Test
    void testUnionWithCommonInterfacePropertyAccess() {
        assertScript '''
            import groovy.transform.CompileStatic
            interface HasId { Long getId() }
            @CompileStatic
            class X1 implements HasId { Long id }
            @CompileStatic
            class X2 implements HasId { Long id }
            @CompileStatic
            Long computeId(Object test) {
                if (test instanceof X1 || test instanceof X2) {
                    return test.id
                }
                null
            }
            assert computeId(new X1(id: 7)) == 7L
            assert computeId(new X2(id: 8)) == 8L
        '''
    }

    // @TypeChecked shares the union inference machinery; the member access must resolve there too
    @Test
    void testUnionInstanceofSubscriptTypeChecked() {
        assertScript '''
            import groovy.transform.TypeChecked
            @TypeChecked
            class X1 { Long id }
            @TypeChecked
            class X2 { Long id }
            @TypeChecked
            Long computeId(Object test) {
                if (test instanceof X1 || test instanceof X2) {
                    return test['id'] as Long
                }
                null
            }
            assert computeId(new X1(id: 12)) == 12L
        '''
    }
}
