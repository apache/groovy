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
package groovy

/**
 * Checks that it's possible to call inner classes constructor from groovy
 */
class CallInnerClassCtorTest extends GroovyTestCase {

    void testCallCtor() {
        def user = new groovy.OuterUser()
        user.name = "Guillaume"
        user.age = 27

        assert user.name == "Guillaume"
        assert user.age == 27
    }

    void testCallInnerCtor() {
        def address = new groovy.OuterUser.InnerAddress()
        address.city = "Meudon"
        address.zipcode = 92360

        assert address.city == "Meudon"
        assert address.zipcode == 92360
    }

    void testCallInnerInnerCtor() {
        def address = new groovy.OuterUser.InnerAddress.Street()
        address.name = "rue de la paix"
        address.number = 17

        assert address.name == "rue de la paix"
        assert address.number == 17
    }

}