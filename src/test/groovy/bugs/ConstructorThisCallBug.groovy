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
/**
 * ConstructorThisCallBug.groovy
 *
 *     Test Script for the Jira issue: GROOVY-994.
 *
 * @author    Pilho Kim
 * @date      2005.08.05.06.21
 */

package groovy.bugs

public class ConstructorThisCallBug extends GroovyTestCase {
    public void testCallA() {
        println "Testing for a class without call()"
        def a1 = new ConstructorCallA("foo") 
        def a2 = new ConstructorCallA(9) 
        def a3 = new ConstructorCallA() 
    }
}

public class ConstructorCallA { 
    public ConstructorCallA() {
        this(19)               // call another constructor
        println "(1) no argument consructor"
    } 

    public ConstructorCallA(String a) {
        println "(2) String value a = $a"
    } 

    public ConstructorCallA(int a) {
        this("" + (a*a))       // call another constructor
        println "(3) int value a = $a"
    } 
} 
