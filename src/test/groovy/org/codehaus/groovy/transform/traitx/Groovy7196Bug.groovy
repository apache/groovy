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

class Groovy7196Bug extends GroovyTestCase {
    void testShouldNotThrowDuplicateMethodWithPrecompiledTrait() {
        assertScript '''import org.codehaus.groovy.transform.traitx.Groovy7196SupportTrait
            class SomeTestClass implements Groovy7196SupportTrait {}
            assert SomeTestClass.org_codehaus_groovy_transform_traitx_Groovy7196SupportTrait__someString == 'ok'
        '''
    }
    void testStaticFieldShouldBeInitialized() {
        assert Groovy7196SupportTraitImpl.org_codehaus_groovy_transform_traitx_Groovy7196SupportTrait__someString == 'ok'
    }
}
