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
package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ClassNode

class Groovy7846Bug extends GroovyTestCase {

    void testTraitsShouldAllowGenerifiedReturnTypesInStaticMethods() {
        def cl = new GroovyClassLoader()
        cl.parseClass('''
            class Foo {
                static <T> T withClient(@DelegatesTo(Foo) Closure<T> callable ) {
                    callable.call()
                }
            }

            trait TraitWithStaticMethod<D>  {
                Collection<D> asCollection(D type) {
                    return [type]
                }

                static <T> T withClient(@DelegatesTo(Foo) Closure<T> callable ) {
                    callable.call()
                }
            }
        ''')
        Class cls = cl.parseClass('''
            class Bar implements TraitWithStaticMethod<Bar> {}
        ''')

        assert new ClassNode(cls).methods
        assert cls.withClient { true }
    }
}

