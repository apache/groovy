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
package gls.scope

import gls.CompilableTestSupport

class FinalAccessTest extends CompilableTestSupport {

    void testFinalField() {
        shouldNotCompile """
            class Person {
                final String name = "scott"
                def foo() {
                    name = "Dierk"
                }
            }
        """
    }

    void testStaticFinalField1() {
        shouldCompile """
            class G3911C1 {
              static final String foo;
              static {
                if (true) {
                  foo = "roshan";
                } else {
                  foo = "jochen";
                }
              }
            }
        """
    }

    void testStaticFinalField2() {
        shouldNotCompile """
            class G3911C2 {
              static final String foo;
              static foo() {
                if (true) {
                  foo = "roshan";
                } else {
                  foo = "jochen";
                }
              }
            }
        """
    }
}