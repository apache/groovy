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
package groovy.bugs

class Groovy3311Bug extends GroovyTestCase {
    static period = new Groovy3311Bug()
    static period2 = 24

    def Groovy3311Bug() {
        // the pre-defined constant for 24 should be correctly init before next statement
        assert (24 != null)
    }

    def void testStaticInitUsingOwnConstructorUsingAPredefinedConstant() {
        assert (Groovy3311Bug.period2 == 24)
    }
}
