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
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy3839 {

    @Test
    void testGroovyASTTransformationWithOneClass() {
        assertScript '''
            import groovy.bugs.*

            @G3839A1
            class G3839V1 {}
            // verify if the ast transform added field 1
            assert G3839V1.class.fields.find{it.name == 'f1'} != null
        '''
    }

    @Test
    void testGroovyASTTransformationWithMultipleClass() {
        assertScript '''
            import groovy.bugs.*

            @G3839A2
            class G3839V2 {}
            // verify if the ast transforms added field f2 and f3
            assert G3839V2.class.fields.find{it.name == 'f2'} != null
            assert G3839V2.class.fields.find{it.name == 'f3'} != null
        '''
    }

    @Test
    void testGroovyASTTransformationWithNeitherTransClassNamesNorClasses() {
        def err = shouldFail '''
            import groovy.bugs.*

            @G3839A3
            class G3839V3 {}
            new G3839V3()
        '''
        assert err =~ '@GroovyASTTransformationClass in groovy.bugs.G3839A3 does not specify any transform class names or types'
    }

    @Test
    void testGroovyASTTransformationWithBothTransClassNamesAndClasses() {
        def err = shouldFail '''
            import groovy.bugs.*

            @G3839A4
            class G3839V4 {}
            new G3839V4()
        '''
        assert err =~ '@GroovyASTTransformationClass in groovy.bugs.G3839A4 should specify transforms by name or by type, not by both'
    }
}
