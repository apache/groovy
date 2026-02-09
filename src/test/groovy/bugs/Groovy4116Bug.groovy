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

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import static groovy.test.GroovyAssert.shouldFail

final class Groovy4116Bug {

    void testAnInterfaceMethodNotImplementedPublic() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class C4116 implements I4116 {
                protected foo() {}
            }
            interface I4116 {
                def foo()
            }
        '''
        assert err.message =~ /The method foo should be public as it implements the corresponding method from interface I4116/
    }

    void testAnInterfaceMethodNotImplementedPublicV2SuperClassInterface() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            abstract class A4116 implements I4116 {
            }
            class C4116 extends A4116 {
                protected foo() {}
            }
            interface I4116 {
                def foo()
            }
        '''
        assert err.message =~ /The method foo should be public as it implements the corresponding method from interface I4116/
    }
}
