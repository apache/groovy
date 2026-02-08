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

import static groovy.test.GroovyAssert.shouldFail

final class Groovy8657 {
    @Test
    void testAccessingResourceInFinallyBlock() {
        def err = shouldFail '''\
            try (
                    FromResource from = new FromResource("ARM was here!")
                    ToResource to = new ToResource()
            ) {
                to << from
            } finally {
                assert from.closed // should throw MissingPropertyException
                assert to.closed
                assert to.toString() == 'ARM was here!'
            }

            class FromResource implements AutoCloseable {
                String text
                boolean closed = false

                FromResource(String text) {
                    this.text = text
                }

                @Override
                void close() {
                    closed = true
                }

                @Override
                String toString() {
                    return text
                }
            }

            class ToResource implements AutoCloseable {
                String text
                boolean closed = false

                @Override
                void close() {
                    closed = true
                }

                @Override
                String toString() {
                    return text
                }
            }
        '''
        assert err =~ /No such property: from for class: /
    }
}
