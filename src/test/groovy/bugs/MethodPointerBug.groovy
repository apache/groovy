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

class MethodPointerBug extends GroovyTestCase {

    void sayHello() {
        println "hello" 
    } 

    MethodPointerBug getThisObject() {
        return this
    } 

    // Test a standard method pointer operator ".&".  For example, foo.&bar.
    void testMethodPointer() {
        def bug = new MethodPointerBug()
        def x = bug.&sayHello
        x()
    } 

    // Test a standard method pointer operator ".&" with this object.  For example, this.&bar.
    void testThisMethodPointer() {
        def y = this.&sayHello
        y()
    } 

    // Test a default method pointer operator "&" with this object.  For example, &bar.
    // This shows that the issue GROOVY-826 has been fixed in groovy-1.0-jar-02.
/*
  todo - commented out due to groovy.g non-determinisms
    void testDefaultMethodPointer() {
        def z = &sayHello
        z()
    } 
*/

    // Test a default method pointer operator ".&" with returned object.  For example, someMethod().&bar.
    void testMethodPointerWithReturn() {
        def u = getThisObject().&sayHello
        u()
        def v = thisObject.&sayHello
        v()
    } 
}
