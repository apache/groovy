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
package groovy.bugs.groovy8531

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy8531 {

    @Test
    void testVisibleInnerTypes() {
        assertScript '''
            package groovy.bugs.groovy8531

            class Example extends Reducer {
                public void reduce1(PublicContext context) {}
                public void reduce2(ProtectedContext context) {}
                public void reduce3(PackagePrivateContext context) {}

                public void reduce4(PublicStaticContext context) {}
                public void reduce5(ProtectedStaticContext context) {}
                public void reduce6(PackagePrivateStaticContext context) {}

                public void reduce7(PublicBaseContext context) {}
                public void reduce8(ProtectedBaseContext context) {}
                public void reduce9(PackagePrivateBaseContext context) {}

                public void reduce10(PublicStaticBaseContext context) {}
                public void reduce11(ProtectedStaticBaseContext context) {}
                public void reduce12(PackagePrivateStaticBaseContext context) {}

                public void reduce13(InterfaceContext context) {}

                public boolean isDynamic(Type type) {
                    return Type.DYNAMIC == type
                }
            }

            new Example().reduce1(null)
            new Example().reduce2(null)
            new Example().reduce3(null)
            new Example().reduce4(null)
            new Example().reduce5(null)
            new Example().reduce6(null)
            new Example().reduce7(null)
            new Example().reduce8(null)
            new Example().reduce9(null)
            new Example().reduce10(null)
            new Example().reduce11(null)
            new Example().reduce12(null)
            new Example().reduce13(null)

            assert new Example().isDynamic(Reducer.Type.DYNAMIC)
        '''
    }

    @Test
    void testPrivateInnerType1() {
        def err = shouldFail '''
            package groovy.bugs.groovy8531
            class Example extends Reducer {
                void reduce(PrivateContext context) {}
            }
        '''

        assert err.message.contains('unable to resolve class PrivateContext')
    }

    @Test
    void testPrivateInnerType2() {
        def err = shouldFail '''
            package groovy.bugs.groovy8531
            class Example extends Reducer {
                void reduce(PrivateBaseContext context) {}
            }
        '''

        assert err.message.contains('unable to resolve class PrivateBaseContext')
    }

    @Test
    void testPackagePrivateInnerType() {
        def err = shouldFail '''
            package groovy.bugs.groovy9281

            import groovy.bugs.groovy8531.Reducer

            class Example extends Reducer {
                void reduce(PackagePrivateContext context) {}
            }
        '''

        assert err.message.contains('unable to resolve class PackagePrivateContext')
    }
}
