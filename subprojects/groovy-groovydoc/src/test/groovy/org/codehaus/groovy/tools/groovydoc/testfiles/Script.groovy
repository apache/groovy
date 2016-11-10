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
package org.codehaus.groovy.tools.groovydoc.testfiles

import groovy.transform.Field

@Field public static Integer staticField = 2 // should appear as a field
@groovy.transform.Field Integer instanceProp // a property (annotation with fqn ok)
def localVar             // should not appear
def localVarWithInit = 3 // should not appear
def (foo, bar) = [1, 2]  // should not appear

/**
 * Use this to say Hello
 */
void sayHello() {
    println 'hello'
}

/**
 * Use this to bid farewell
 */
void sayGoodbye() {
    println 'goodbye'
}

sayHello()
sayGoodbye()
