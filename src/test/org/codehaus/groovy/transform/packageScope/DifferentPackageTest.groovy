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
package org.codehaus.groovy.transform.packageScope

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class DifferentPackageTest extends GroovyTestCase {
    void _FIXME_testSamePackageShouldSeeInstanceProps() {
        assertScript '''
            package org.codehaus.groovy.transform.packageScope.p

            @groovy.transform.CompileStatic
            class Two extends One {
                void valSize() {
                    value.size()
                }
            }

            assert new Two().valSize() == 3
        '''
    }

    void _FIXME_testSamePackageShouldSeeStaticProps() {
        assertScript '''
            package org.codehaus.groovy.transform.packageScope.p

            @groovy.transform.CompileStatic
            class Three {
                static void halfNum() {
                    One.NUM / 2
                }
            }

            assert Three.halfNum() == 21
        '''
    }

    void _FIXME_testDifferentPackageShouldNotSeeInstanceProps() {
        def message = shouldFail(MultipleCompilationErrorsException, '''
            package org.codehaus.groovy.transform.packageScope.q

            import org.codehaus.groovy.transform.packageScope.p.One

            @groovy.transform.CompileStatic
            class Two extends One {
                void valSize() {
                    value.size()
                }
            }

            assert new Two().valSize() == 3
        ''')
        assert message.matches('(?s).*Access to .*value is forbidden.*')
    }

    void testDifferentPackageShouldNotSeeStaticProps() {
        def message = shouldFail(MultipleCompilationErrorsException, '''
            package org.codehaus.groovy.transform.packageScope.q

            import org.codehaus.groovy.transform.packageScope.p.One

            @groovy.transform.CompileStatic
            class Three {
                static void halfNum() {
                    One.NUM / 2
                }
            }

            assert Three.halfNum() == 21
        ''')
        assert message.matches('(?s).*Access to .*One#NUM is forbidden.*')
    }
}
