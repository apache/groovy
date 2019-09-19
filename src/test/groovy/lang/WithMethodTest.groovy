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

/**
 * Tests the .with method
 */
class WithMethodTest extends GroovyTestCase {

     void testDelegateGetsFirstOpportunity() {
         def sb = new StringBuffer()

         sb.with {
             // this should call append() on the 
             // delegate not, the owner
             append 'some text'
         }

         assertEquals 'delegate had wrong value', 'some text', sb.toString()
     }

     void testOwnerGetsOpportunityIfDelegateCannotRespond() {
         def sb = new StringBuffer()

         def returnValue

         sb.with {
             // this should call ownerMethod() on the owner
             returnValue = ownerMethod()
         }

         assertEquals 'owner should have responded to method call', 
                      42, 
                      returnValue
     }

     void testCallingNonExistentMethod() {
         def sb = new StringBuffer()

         shouldFail(MissingMethodException) {
             sb.with {
                 someNoneExistentMethod()
             }
         }
     }

     void testClosureWithResolveStrategyExplicitlySet() {
         def closure = {
             append 'some text'
         }
         closure.resolveStrategy = Closure.OWNER_ONLY

         def sb = new StringBuffer()

         // .with should use DELEGATE_FIRST, even though
         // the closure has another strategy set
         sb.with closure

         assertEquals 'delegate had wrong value', 'some text', sb.toString()
     }

     void testBooleanVariant() {
         def p = new PersonWith(firstName: 'Johnny', lastName: 'Depp')
         def result1 = p.with(false) {
             "$firstName $lastName"
         }
         assert result1 instanceof GString
         assert result1.toString() == 'Johnny Depp'
         def result2 = p.with(true) {
             lastName = 'Cash'
         }
         assert result2 instanceof PersonWith
         assert result2.toString() == 'WithMethodTest$PersonWith(Johnny, Cash)'
     }

     def ownerMethod() {
         42
     }

     void append(String s) {
         fail 'this should never have been called'
     }

     @groovy.transform.ToString(includePackage = false)
     class PersonWith {
         String firstName
         String lastName
     }
 }