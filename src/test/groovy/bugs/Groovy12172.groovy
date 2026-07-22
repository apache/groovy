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
 * A dynamic subscript on that receiver — {@code x['p']} — compiles to a
 * {@code LEFT_SQUARE_BRACKET} binary expression whose static-compilation path
 * ({@code BinaryExpressionMultiTypeDispatcher}) probes {@code getComponentType()}
 * on the receiver type before checking {@code isArray()}. The union node threw
 * {@link UnsupportedOperationException} there, aborting compilation with
 * "General error during instruction selection" (GROOVY-12172). A union of
 * {@code instanceof} types is never an array, so {@code getComponentType()} now
 * answers {@code null} like any other non-array receiver, and the subscript
 * resolves through the normal {@code getAt} path.
 * <p>
 * Scope: this covers the reported subscript form. Direct property access on the
 * same union receiver ({@code x.p}) is a distinct code path with its own,
 * separate soundness handling and is not exercised here.
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

    // a union of three types exercises the same union receiver in the subscript path
    @Test
    void testUnionOfThreeTypesSubscriptAccess() {
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
                    return test['id'] as Long
                }
                null
            }
            assert computeId(new X1(id: 1)) == 1L
            assert computeId(new X2(id: 2)) == 2L
            assert computeId(new X3(id: 3)) == 3L
        '''
    }

    // union members sharing a supertype still take the dynamic subscript path
    @Test
    void testUnionWithCommonInterfaceSubscriptAccess() {
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
                    return test['id'] as Long
                }
                null
            }
            assert computeId(new X1(id: 7)) == 7L
            assert computeId(new X2(id: 8)) == 8L
        '''
    }

    // @TypeChecked shares the union inference machinery; the subscript must resolve there too
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

    // regression guard: array subscript under @CompileStatic still uses the optimized path
    @Test
    void testArraySubscriptStillOptimized() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            int firstPlusSecond(int[] a) {
                a[0] + a[1]
            }
            assert firstPlusSecond([10, 20, 30] as int[]) == 30
        '''
    }
}
