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
package org.apache.groovy.util

import org.junit.jupiter.api.Test

final class BeanUtilsTest {

    @Test
    void testCapitalize() {
        [
            Prop    : 'Prop',
            prop    : 'Prop',
            someProp: 'SomeProp',
            X       : 'X',
            DB      : 'DB',
            XML     : 'XML',
            aProp   : 'aProp', // GROOVY-3211
            pNAME   : 'pNAME', // GROOVY-3211
            AProp   : 'AProp', // GROOVY-3211
            '_prop' : '_prop'
        ].each { string, expect ->
            assert BeanUtils.capitalize(string) == expect
        }
    }

    @Test
    void testDecapitalize() {
        [
            Prop    : 'prop',
            prop    : 'prop',
            SomeProp: 'someProp',
            X       : 'x',
            DB      : 'DB', // GROOVY-9451
            XML     : 'XML',
            aProp   : 'aProp',
            AProp   : 'AProp',
            '_Prop' : '_Prop'
        ].each { string, expect ->
            assert BeanUtils.decapitalize(string) == expect
        }
    }
}
