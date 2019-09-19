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

import groovy.test.GroovyTestCase


class Groovy4999Bug extends GroovyTestCase {
    void testStaticOverloadedMixinMethods() {
        assertScript """
            @Mixin(UtilClass)
            class MainClass {
                public static void main(String []s) {
                    MainClass mc = new MainClass()
                    mc.callMixinMethods()
                }
            
                void callMixinMethods() {
                    println callClassOverloadedMethod("")
                    println callStaticOverloadedMethod("")
                }
            }
            
            class UtilClass {
                def callClassOverloadedMethod(String s) {
                    "callClassOverloadedMethod(String)"
                }
                def callClassOverloadedMethod(String s, Object o) {
                    "callClassOverloadedMethod(String, Object)"
                }
                static callStaticOverloadedMethod(String s) {
                    "callStaticOverloadedMethod(String)"
                }
                static callStaticOverloadedMethod(String s, Object o) {
                    "callStaticOverloadedMethod(String, Object)"
                }
            }
        """
    }
}

