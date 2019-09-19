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

class Groovy5101Test extends GroovyTestCase {

    static class ClassA {
        Runnable r
        ClassA(Runnable r) { this.r = r }
        void run() { "hello" }
    }

    static class Factory {
        void getClassA1(Runnable r) {
            new ClassA(r) // OK
        }
        void getClassA2(Runnable r) {
            new ClassA(r) {
                void run() { "a fixed message" } // OK
            }
        }
        void getClassA3(Runnable r) {
            new ClassA(r) {
                void run() { r } // NG
            }
        }
    }

    Runnable r
    Factory factory

    void setUp() {
        this.r = new Runnable() {
            void run() { "A!" }
        }
        this.factory = new Factory()
    }

    void test_NotInnerAnonymousClass() {
        factory.getClassA1(r)
    }

    void test_InnerAnonymousClass_NotUsingArgument() {
        factory.getClassA2(r)
    }

    void test_InnerAnonymousClass_UsingArgument() {
        factory.getClassA3(r) // it should be OK but the result is NG
    }

}
