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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy3749 {

    /**
     * Test various signatures of static main()
     */
    @Test
    void testScriptsProvidingStaticMainMethod() {
        shouldFail RuntimeException, '''
            static main(args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            static def main(args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            static void main(args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            static main(String[] args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            static def main(String[] args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            static void main(String[] args) {
                throw new RuntimeException('main called')
            }
        '''

        def err = shouldFail '''
            static main(args) {
                throw new RuntimeException('main called')
            }
            throw new Error()
        '''
        assert err.message.contains('The method public static void main(java.lang.String[] args) { ... } is a duplicate of the one declared for this script\'s body code')

        shouldFail RuntimeException, '''
            static void main() {
                throw new RuntimeException('main called')
            }
        '''

        // if param type doesn't match, this main won't execute
        assertScript '''
            static main(Date date) {
                throw new RuntimeException('main called')
            }
        '''
    }

    /**
     * Test various signatures of non-static main()
     */
    @Test
    void testScriptsProvidingInstanceMainMethod() {
        shouldFail RuntimeException, '''
            def main(String[] args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            void main(args) {
                throw new RuntimeException('main called')
            }
        '''

        shouldFail RuntimeException, '''
            void main() {
                throw new RuntimeException('main called')
            }
        '''

        // if param type doesn't match, this main won't execute
        assertScript '''
            def main(Date date) {
                throw new RuntimeException('main called')
            }
        '''
    }
}
