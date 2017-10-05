///*
// *  Licensed to the Apache Software Foundation (ASF) under one
// *  or more contributor license agreements.  See the NOTICE file
// *  distributed with this work for additional information
// *  regarding copyright ownership.  The ASF licenses this file
// *  to you under the Apache License, Version 2.0 (the
// *  "License"); you may not use this file except in compliance
// *  with the License.  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// */
//package org.codehaus.groovy.transform
//
//import gls.CompilableTestSupport
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.junit.runners.JUnit4
//
//import groovy.transform.impl.autofinal.AutoFinalClosure
//import groovy.transform.ASTTest
//import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
//import static java.lang.reflect.Modifier.isFinal
//
//
///**
// * Tests for the {@code @AutoFinal} AST transform.
// */
//
//@RunWith(JUnit4)
//class AutoFinalClosureTransform_DEBUG extends CompilableTestSupport {
//
//    @AutoFinalClosure
//    class Person {
//        final String first, last
//        Person(String first, String last) {
//            this.first = first
//            this.last = last
//        }
//        String fullName(boolean reversed = false, String separator = ' ') {
//            final cls = { String s -> s = "abc"; s }
//            final clsResult = cls("xyz")
//            return clsResult
//        }
//    }
//
//
//    @Test
//    void testAutoFinalClosure_v1() {
//        final js = new Person('John', 'Smith')
//        assert js.fullName() == 'John Smith'
//        assert js.fullName(true, ', ') == 'Smith, John'
//     }
//}
