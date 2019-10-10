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

import groovy.transform.CompileStatic
import org.junit.Assert
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy3852 {

    @Test
    void testDuplicationAnnotationOnClassNoParams() {
        def err = shouldFail '''
            @Deprecated
            @Deprecated
            @Deprecated
            class A {}
        '''

        assert err =~ /Cannot specify duplicate annotation/
    }

    @Test
    void testDuplicationAnnotationOnClassWithParams() {
        def err = shouldFail '''
            import java.lang.annotation.*
            @Retention(value=RetentionPolicy.CLASS)
            @Retention(value=RetentionPolicy.CLASS)
            @interface B {}
        '''

        assert err =~ /Cannot specify duplicate annotation/
    }

    @Test
    void testDuplicationAnnotationOnOtherTargets() {
        def err = shouldFail '''
            class C {
                @Deprecated
                @Deprecated
                @Deprecated
                def m() {}
            }
        '''

        assert err =~ /Cannot specify duplicate annotation/

        err = shouldFail '''
            class D {
                @Deprecated
                @Deprecated
                @Deprecated
                def p
            }
        '''

        assert err =~ /Cannot specify duplicate annotation/
    }

    @Test
    void testDuplicationNonRuntimeRetentionPolicyAnnotations() {
        try {
            assertScript '''
                @Newify(auto=false, value=String)
                @Newify(auto=false, value=String)
                class Groovy3930 {
                    static void main(args) {
                        println 'success'
                    }
                }
            '''
        } catch (any) {
            Assert.fail('Compilation should have succeeded as it has duplication annotations but with retention policy "not RUNTIME"')
        }
    }

    @Test
    void testDuplicationAnnotationsForImport() {
        // TODO: replace with better test - Newify doesn't really make sense for import
        try {
            assertScript '''
                @Newify(auto=false, value=String)
                @Newify(auto=false, value=String)
                import java.lang.String
                class Groovy3925 {
                    static void main(args) {
                        println 'success'
                    }
                }
            '''
        } catch (any) {
            Assert.fail('Compilation should have succeeded as it has duplication annotations but with retention policy "not RUNTIME"')
        }
    }
}
