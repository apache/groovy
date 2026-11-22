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

/**
 * GROOVY-9965: a static generic {@code reference(T)} on a superclass used to
 * hide the instance method {@code reference(String)}, so
 * {@code super.reference(schema)} threw {@code MissingMethodException}.
 * The instance method must be selected.
 */
final class Groovy9965 {

    @Test
    void testSuperReferenceStringIsNotHiddenByStaticGenericMethod() {
        assertScript '''
            import groovy.transform.Canonical
            import groovy.transform.InheritConstructors

            @Canonical
            class Table {
                final String name = 'defaultTable'

                Table reference(String schema) {
                    assert schema == "${name}_schema"
                    return this
                }

                static <T extends Table> T reference(T table) {
                    throw new AssertionError('static generic reference(T) should not be selected')
                }
            }

            @InheritConstructors
            class TableGeneric extends Table {
                TableGeneric reference() {
                    final String schema = "${name}_schema"
                    return (TableGeneric) super.reference(schema)
                }
            }

            def tableGeneric = new TableGeneric('FOO')
            def result = tableGeneric.reference()
            assert result instanceof TableGeneric
            assert result.name == 'FOO'
        '''
    }
}
