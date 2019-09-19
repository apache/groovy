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
package groovy.lang

import groovy.test.GroovyTestCase

class ExpandoMetaClassConstructorTest extends GroovyTestCase {
    void testVariousConstructorForms() {
        try {
            assertScript """
                Short.metaClass {
                    constructor { List l -> l.size() as short }
                }
                Integer.metaClass {
                    constructor = { List l -> l.size() }
                }
                Long.metaClass {
                    constructor << { List l -> l.size() as long }
                }
    
                def result = [[] as Short, ['foo'] as Integer, ['bar', 'baz'] as Long]
                assert result.toString() == '[0, 1, 2]'
                assert result*.class == [Short, Integer, Long]
            """
        } finally {
            Integer.metaClass = null
            Short.metaClass = null
            Long.metaClass = null
        }
    }
}
