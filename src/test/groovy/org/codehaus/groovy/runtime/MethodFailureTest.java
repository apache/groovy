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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.test.GroovyTestCase;

/**
 * Tests failing method invocations to ensure correct exceptions
 */
public class MethodFailureTest extends GroovyTestCase {

    public void testFailingMethod() {
        MockGroovyObject object = new MockGroovyObject();
        try {
            object.invokeMethod("nonExistentMethod", "hello");

            fail("Should have thrown an exception");
        }
        catch (GroovyRuntimeException e) {
            // expected
        }
    }

    public void testMethodWhichCallsTheFailingMethod() {
        MockGroovyObject object = new MockGroovyObject();
        try {
            object.invokeMethod("methodThatFails", null);

            fail("Should have thrown an exception");
        }
        catch (GroovyRuntimeException e) {
            // expected
        }
    }

    public void testMethodWhichCallsTheFailingMethodInsideAClosure() {
        MockGroovyObject object = new MockGroovyObject();
        try {
            object.invokeMethod("callClosure", new Closure(this) {
                protected Object doCall(GroovyObject object) {
                    return object.invokeMethod("nonExistentMethod", "hello");
                }
            });

            fail("Should have thrown an exception");
        }
        catch (GroovyRuntimeException e) {
            // expected
        }
    }

}
