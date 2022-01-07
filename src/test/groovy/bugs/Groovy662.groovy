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
import groovy.transform.PackageScope
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy662 {

    @Test
    void testJavaClass() {
        def object = new Groovy662_JavaClass()
        assert object.getMyProperty() == 'Hello'
        assert object.@myProperty == 'Hello'
        assert object.myProperty == 'Hello'
    }

    @Test @CompileStatic
    void testJavaClassCS() {
        def object = new Groovy662_JavaClass()
        assert object.getMyProperty() == 'Hello'
        assert object.@myProperty == 'Hello'
        assert object.myProperty == 'Hello'
    }

    @Test
    void testJavaClassAsScript() {
        assertScript '''
            def object = new groovy.bugs.Groovy662_JavaClass()
            assert object.getMyProperty() == 'Hello'
            assert object.@myProperty == 'Hello'
            assert object.myProperty == 'Hello'
        '''
    }

    //

    @Test
    void testGroovyClass() {
        def object = new Groovy662_GroovyClass()
        assert object.getMyProperty() == 'Hello'
        assert object.@myProperty == 'Hello'
        assert object.myProperty == 'Hello'
    }

    @Test @CompileStatic
    void testGroovyClassCS() {
        def object = new Groovy662_GroovyClass()
        assert object.getMyProperty() == 'Hello'
        assert object.@myProperty == 'Hello'
        assert object.myProperty == 'Hello'
    }

    @Test
    void testGroovyClassAsScript() {
        assertScript '''
            def object = new groovy.bugs.Groovy662_GroovyClass()
            assert object.getMyProperty() == 'Hello'
            assert object.@myProperty == 'Hello'
            assert object.myProperty == 'Hello'
        '''
    }
}

class Groovy662_GroovyClass extends HashMap {
    @PackageScope String myProperty = 'Hello'

    String getMyProperty() {
        return myProperty
    }
}
