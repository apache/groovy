/*
 * Copyright 2003-2015 the original author or authors.
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
package groovy.bugs

/**
 * @version $Revision$
 */
class Bytecode7Bug extends GroovyTestCase {

    void testDuplicateVariables() {
        if (true) {
            def a = 123
        }
        if (true) {
            def a = 456
        }
    }

    void testDuplicateVariablesInClosures() {
        def coll = [1]

        coll.each {
            def a = 123
        }
        coll.each {
            def a = 456
        }
    }
}