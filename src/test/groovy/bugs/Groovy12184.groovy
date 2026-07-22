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
import static groovy.test.GroovyAssert.shouldFail

/**
 * A union {@code instanceof} ({@code x instanceof A || x instanceof B}) makes the
 * type checker infer a union type for {@code x} inside the branch. When the
 * accessed property is declared on the individual delegates (with no getter shared
 * across all of them), static compilation bound the accessor of the <em>first</em>
 * delegate and cast the receiver to it — throwing {@link ClassCastException} at
 * runtime when the object was one of the other delegates (GROOVY-12184).
 * <p>
 * Property access on a union receiver whose accessor is not shared by every
 * delegate now falls back to dynamic resolution (as GROOVY-8965 already does for
 * method calls), so the correct accessor runs. When the delegates share a
 * supertype/interface declaring the accessor, the sound static binding is kept.
 */
final class Groovy12184 {

    // the reported shape: property declared on each bare delegate, no common supertype
    @Test
    void testBareUnionPropertyAccess() {
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
            assert computeId(new X1(id: 11)) == 11L
            assert computeId(new X2(id: 22)) == 22L   // was ClassCastException: X2 cannot be cast to X1
            assert computeId('other') == null
        '''
    }

    @Test
    void testThreeTypeUnionPropertyAccess() {
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

    // delegates sharing an interface that declares the accessor keep the sound static binding
    @Test
    void testUnionWithCommonInterfaceStaysSound() {
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

    // property types differing across delegates: the inferred type is their least upper bound
    @Test
    void testUnionPropertyOfDifferingTypes() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class X1 { Long value }
            @CompileStatic
            class X2 { Integer value }
            @CompileStatic
            Number readValue(Object test) {
                if (test instanceof X1 || test instanceof X2) {
                    return test.value
                }
                null
            }
            assert readValue(new X1(value: 10L)) == 10L
            assert readValue(new X2(value: 20)) == 20
        '''
    }

    // a single instanceof narrows to a concrete type and keeps its normal static binding
    @Test
    void testSingleInstanceofUnchanged() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class X1 { Long id }
            @CompileStatic
            Long computeId(Object test) {
                if (test instanceof X1) {
                    return test.id
                }
                null
            }
            assert computeId(new X1(id: 5)) == 5L
        '''
    }

    // an unknown property on the union must still be a static type-checking error
    @Test
    void testUnknownUnionPropertyStillFailsToCompile() {
        def error = shouldFail '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class X1 { Long id }
            @CompileStatic
            class X2 { Long id }
            @CompileStatic
            def read(Object test) {
                if (test instanceof X1 || test instanceof X2) {
                    return test.nope
                }
                null
            }
        '''
        assert error.message.contains('No such property: nope')
    }
}
