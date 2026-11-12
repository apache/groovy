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

final class Groovy11617 {

    @Test
    void testEmptyLastCase() {
        assertScript '''
            def xxx = 1
            def yyy = 1
            switch (xxx) {
              case yyy:
            }

            int hit = 0
            switch (xxx) {
                case 0:
                    hit = 7
                case yyy:
            }
            assert hit == 0

            switch (xxx) {
                case 1:
                    hit = 1
                case 2:
                case 3:
            }
            assert hit == 1

            switch (xxx) {
                case { it == 1 }:
                    hit = 2
                case { it == 9 }:
            }
            assert hit == 2

            def closure = {
                switch (xxx) {
                    case yyy:
                }
                4
            }
            assert closure() == 4
        '''
    }

    @Test
    void testEmptyLastDefault() {
        assertScript '''
            def xxx = 1
            switch (xxx) {
              default:
            }

            int hit = 0
            switch (xxx) {
                case 0:
                    hit = 7
                default:
            }
            assert hit == 0

            switch (xxx) {
                case 1:
                    hit = 1
                default:
            }
            assert hit == 1

            switch (xxx) {
                case 1:
                    hit = 3
                case 2:
                default:
            }
            assert hit == 3

            @groovy.transform.CompileStatic
            class C {
                static int emptyCase(int value) {
                    int hit = 0
                    switch (value) {
                        case 1:
                            hit = 1
                        case 2:
                    }
                    return hit
                }

                static int emptyDefault(int value) {
                    int hit = 0
                    switch (value) {
                        case 1:
                            hit = 1
                        default:
                    }
                    return hit
                }
            }
            assert C.emptyCase(1) == 1
            assert C.emptyCase(2) == 0
            assert C.emptyCase(3) == 0
            assert C.emptyDefault(1) == 1
            assert C.emptyDefault(9) == 0
        '''
    }

    @Test
    void testEmptyTrailingLabelsStillValidated() {
        def notLast = shouldFail '''
            switch (xxx) {
                default:
                case 1:
            }
        '''
        assert notLast.message.contains('A default branch must appear as the last branch of a switch')

        def afterBody = shouldFail '''
            switch (xxx) {
                default:
                    break
                case 1:
            }
        '''
        assert afterBody.message.contains('A default branch must appear as the last branch of a switch')

        def notLastWithBody = shouldFail '''
            switch (xxx) {
                default:
                case 1:
                    break
            }
        '''
        assert notLastWithBody.message.contains('A default branch must appear as the last branch of a switch')

        def duplicate = shouldFail '''
            switch (xxx) {
                default:
                default:
            }
        '''
        assert duplicate.message.contains('A switch can have only one default branch')
    }
}
