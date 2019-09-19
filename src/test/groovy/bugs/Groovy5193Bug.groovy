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
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy5193Bug extends GroovyTestCase {
    void testMixingMethodsWithPrivatePublicAccessInSameClassV1() {
        try{
            assertScript """
                class Repository5193V1 {
                  def find(String id) {}
                  private <T> T find(Class<T> type, String id, boolean suppressNotFoundExceptions) { }
                }
            """
            fail("compilation should have failed saying that mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden.")
        } catch(MultipleCompilationErrorsException ex) {
            assertTrue ex.message.contains("Mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden")
        }
    }

    void testMixingMethodsWithPrivatePublicAccessInSameClassV2() {
        try{
            assertScript """
                class Repository5193V2 {
                  def find(String id) {}
                  private <T> T find(Class<T> type, String id, boolean suppressNotFoundExceptions = true) { }
                }
            """
            fail("compilation should have failed saying that mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden.")
        } catch(MultipleCompilationErrorsException ex) {
            assertTrue ex.message.contains("Mixing private and public/protected methods of the same name causes multimethods to be disabled and is forbidden")
        }
    }
}
