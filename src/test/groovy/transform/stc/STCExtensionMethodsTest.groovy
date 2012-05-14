/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

/**
 * Unit tests for static type checking : extension methods.
 *
 * @author Cedric Champeau
 */
class STCExtensionMethodsTest extends StaticTypeCheckingTestCase {

    void testShouldFindExtensionMethod() {
        assertScript '''
            // reverteToUpperCase is an extension method specific to unit tests
            def str = 'This is a string'
            assert str.reverseToUpperCase() == str.toUpperCase().reverse()
        '''
    }

}

