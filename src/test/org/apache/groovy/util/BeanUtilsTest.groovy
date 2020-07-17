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

import org.junit.Test
import static org.apache.groovy.util.BeanUtils.decapitalize
import static org.apache.groovy.util.BeanUtils.capitalize

class BeanUtilsTest {
    @Test
    void testJavaBeanDecapitalize() {
        assert decapitalize('Prop') == 'prop'
        assert decapitalize('prop') == 'prop'
        assert decapitalize('SomeProp') == 'someProp'
        assert decapitalize('X') == 'x'
        assert decapitalize('DB') == 'DB' // GROOVY-9451
        assert decapitalize('XML') == 'XML'
        assert decapitalize('aProp') == 'aProp'
        assert decapitalize('AProp') == 'AProp'
    }

    @Test
    void testJavaBeanCapitalize() {
        assert capitalize('Prop') == 'Prop'
        assert capitalize('prop') == 'Prop'
        assert capitalize('someProp') == 'SomeProp'
        assert capitalize('X') == 'X'
        assert capitalize('DB') == 'DB'
        assert capitalize('XML') == 'XML'
        assert capitalize('aProp') == 'aProp' // GROOVY-3211
        assert capitalize('pNAME') == 'pNAME' // GROOVY-3211
        assert capitalize('AProp') == 'AProp' // GROOVY-3211
    }
}