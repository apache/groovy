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

import groovy.test.GroovyTestCase
import junit.framework.AssertionFailedError

/**
 * VarargsMethodTest.groovy
 *
 *   1) Test to fix the Jira issues GROOVY-1023 and GROOVY-1026.
 *   2) Test the feature that the length of arguments can be variable
 *      when invoking methods with or without parameters.
 *   3) Test that in type-checked mode, single non-array args
 *      are wrapped in an array using compiletime metadata (GROOVY-10099)
 */
class VarargsMethodTest extends GroovyTestCase {

    String testNullProperty = null

    void testVarargsOnly() {  
        assertEquals 1, varargsOnlyMethod('')
        assertEquals 1, varargsOnlyMethod(1)
        assertEquals 2, varargsOnlyMethod('','')
        assertEquals 1, varargsOnlyMethod( ['',''] )
        assertEquals 2, varargsOnlyMethod( ['',''] as Object[])
        assertEquals 2, varargsOnlyMethod( *['',''] )

        // GROOVY-1023
        assertEquals 0, varargsOnlyMethod()

        // GROOVY-1026
        assertEquals(-1, varargsOnlyMethod(null))

        // GROOVY-10099
        // cast to non-array
        shouldFail(AssertionFailedError) { assertEquals(1, varargsOnlyMethod((Object)null)) }
        // cast to array
        assertEquals(-1, varargsOnlyMethod((Object[])null))
        // non-array typed variable == null
        String arg = null
        shouldFail(AssertionFailedError) { assertEquals(1, varargsOnlyMethod(arg)) }
        // array-typed variable == null
        Object[] argArray = null
        assertEquals(-1, varargsOnlyMethod(argArray))
        // non-array typed POGO property == null
        shouldFail(AssertionFailedError) { assertEquals(1, varargsOnlyMethod(this.testNullProperty)) }
        // non-array typed POJO property (ie: via getter) returns null
        shouldFail(AssertionFailedError) { assertEquals(1, varargsOnlyMethod(java.net.URI.create('http://example.com').query)) }
        // non-array returning method returns null
        shouldFail(AssertionFailedError) { assertEquals(1, varargsOnlyMethod(Objects.toString(null, null))) }

        assertEquals(2, varargsOnlyMethod(null, null))
    }

    @groovy.transform.TypeChecked
    void testVarargsOnlyTypeChecked() {  
        assertEquals 1, varargsOnlyMethod('')
        assertEquals 1, varargsOnlyMethod(1)
        assertEquals 2, varargsOnlyMethod('','')
        assertEquals 1, varargsOnlyMethod( ['',''] )
        assertEquals 2, varargsOnlyMethod( ['',''] as Object[])

        // GROOVY-1023
        assertEquals 0, varargsOnlyMethod()

        // GROOVY-10099
        assertEquals(1, varargsOnlyMethod(null))
        // cast to non-array
        assertEquals(1, varargsOnlyMethod((Object)null))
        // cast to array
        assertEquals(-1, varargsOnlyMethod((Object[])null))
        // non-array typed variable == null
        String arg = null
        assertEquals(1, varargsOnlyMethod(arg))
        // array-typed variable == null
        Object[] argArray = null
        assertEquals(-1, varargsOnlyMethod(argArray))
        // non-array typed POGO property == null
        assertEquals(1, varargsOnlyMethod(this.testNullProperty))
        // non-array typed POJO property (ie: via getter) returns null
        assertEquals(1, varargsOnlyMethod(java.net.URI.create('http://example.com').query))
        // non-array returning method returns null
        assertEquals(1, varargsOnlyMethod(Objects.toString(null, null)))
        
        assertEquals(2, varargsOnlyMethod(null, null))
    }

     Integer varargsOnlyMethod(Object[] args) {
         // GROOVY-1023 (Java 5 feature)
         //     If this method having varargs is invoked with no parameter,
         //     then args is not null, but an array of length 0.
         // GROOVY-1026 (Java 5 feature)
         //     If this method having varargs is invoked with one parameter
         //     null, then args is null, and so -1 is returned here.
         if (args == null)
               return -1
         return args.size()
     }

     void testVarargsLast() {
         assertEquals 0, varargsLastMethod('')
         assertEquals 0, varargsLastMethod(1)
         assertEquals 1, varargsLastMethod('','')
         assertEquals 2, varargsLastMethod('','','')
         assertEquals 1, varargsLastMethod('', ['',''] )
         assertEquals 2, varargsLastMethod('', ['',''] as Object[])
         assertEquals 2, varargsLastMethod('', *['',''] )

        // GROOVY-1026
        assertEquals(-1, varargsLastMethod('', null))
        assertEquals(2, varargsLastMethod('', null, null))
    }

    @groovy.transform.TypeChecked
     void testVarargsLastTypeChecked() {
         assertEquals 0, varargsLastMethod('')
         assertEquals 0, varargsLastMethod(1)
         assertEquals 1, varargsLastMethod('','')
         assertEquals 2, varargsLastMethod('','','')
         assertEquals 1, varargsLastMethod('', ['',''] )
         assertEquals 2, varargsLastMethod('', ['',''] as Object[])

        // GROOVY-10099
        assertEquals(1, varargsLastMethod('', null))
        assertEquals(2, varargsLastMethod('', null, null))
    }

    Integer varargsLastMethod(Object first, Object[] args) {
        // GROOVY-1026 (Java 5 feature)
        //     If this method having varargs is invoked with two parameters
        //     1 and null, then args is null, and so -1 is returned here.
        if (args == null)
            return -1
        return args.size()
    }
}  
