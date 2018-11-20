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
package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-8895: Checks that a trait with an abstract getter isn't included in stub by mistake
 */
class TraitAbstractGetterStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'Foo8895.java': '''
                public class Foo8895 { }
            ''',

            'GetFoo.groovy': '''
                trait GetFoo {
                    abstract Foo8895 getFoo()
                }
            ''',

            'BaseFooSpec.groovy': '''
                class BaseFooSpec {
                    Foo8895 foo = new Foo8895()
                }
            ''',

            'FooSpec.groovy': '''
                class FooSpec extends BaseFooSpec implements GetFoo { }
            ''',

            'Control.groovy': '''
                class Control implements GetFoo { }
            ''',
        ]
    }

//    protected void init() {
//        debug = true
//        delete = false
//    }

    @Override
    void verifyStubs() {
        String stubSource = stubJavaSourceFor('FooSpec')
        assert !stubSource.contains('Foo8895 getFoo()')
        stubSource = stubJavaSourceFor('Control')
        assert !stubSource.contains('Foo8895 getFoo()')
    }

}
