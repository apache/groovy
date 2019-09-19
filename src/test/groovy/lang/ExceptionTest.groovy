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

public class ExceptionTest extends GroovyTestCase {

    private int finallyCounter;
    
    def m1() {
        // this code is in a method, because we need to test
        // insertions for return here along with the method
        try { 
            throw new RuntimeException("1") 
        } catch (Throwable t) {
        } finally { 
            finallyCounter++
            throw new RuntimeException("2") 
        }
    }
    
    void testFinallyExceptionOverridingTryException() {
        finallyCounter = 0
        try {
            m1()
            assert false
        } catch (RuntimeException re) {
            assert re.message == "2"
        }
        assert finallyCounter == 1
    }
    
    def m2() {
        try {
            def x = 0
        } catch (Throwable t) {
        } finally { 
            finallyCounter++ 
            throw new RuntimeException("1") 
        }
    }
    
    void testFinallyExceptionAlone() {
        finallyCounter = 0
        try {
            m2()
            assert false
        } catch (RuntimeException re) {
            assert re.message == "1"
        }
        assert finallyCounter == 1
    }
    
    def m3() {    
        try {
          throw new RuntimeException("1")
        } catch (RuntimeException e) {
          finallyCounter++
          throw e
        } finally {
          finallyCounter++
        }
    }
    
    void testExceptionAndCatchBlock() {
        finallyCounter = 0
        try {
            m3()
            assert false
        } catch (RuntimeException re) {
            assert re.message == "1"
        }
        assert finallyCounter == 2
    }        
}
