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
package org.codehaus.groovy.vmplugin.v8

import org.junit.jupiter.api.Test

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

import static org.junit.jupiter.api.Assertions.assertEquals

final class IndyInterfaceDeprecatedTest {

    private String foo() {
        return "foo-result"
    }

    @Test
    void testSelectMethodDeprecatedInstanceFoo() {
        // Prepare call site type: (IndyInterfaceDeprecatedTest) -> Object
        MethodHandles.Lookup lookup = MethodHandles.lookup()
        MethodType type = MethodType.methodType(Object, IndyInterfaceDeprecatedTest)
        CacheableCallSite callSite = new CacheableCallSite(type, lookup)

        // Provide non-null default/fallback targets (needed for guards in Selector)
        def dummyTarget = MethodHandles.dropArguments(
            MethodHandles.constant(Object, null), 0, type.parameterArray()
        )
        callSite.defaultTarget = dummyTarget
        callSite.fallbackTarget = dummyTarget

        // Prepare invocation arguments
        def receiver = new IndyInterfaceDeprecatedTest()
        Object[] args = [receiver] as Object[]

        // Call deprecated selectMethod with the requested flags
        int callID = IndyInterface.CallType.METHOD.getOrderNumber() // expected to be 0
        Object result = IndyInterface.selectMethod(
            callSite,
            IndyInterfaceDeprecatedTest,        // sender
            'foo',                              // methodName
            callID,
            Boolean.FALSE,                      // safeNavigation
            Boolean.TRUE,                       // thisCall (instance method)
            Boolean.FALSE,                      // spreadCall
            1,                                  // dummyReceiver (marker only)
            args
        )

        // Verify the local method foo was actually called
        assertEquals(receiver.foo(), result)
    }

    @Test
    void testFromCacheDeprecatedInstanceFoo() {
        // Prepare call site type: (IndyInterfaceDeprecatedTest) -> Object
        MethodHandles.Lookup lookup = MethodHandles.lookup()
        MethodType type = MethodType.methodType(Object, IndyInterfaceDeprecatedTest)
        CacheableCallSite callSite = new CacheableCallSite(type, lookup)

        // Provide non-null default/fallback targets (needed for guards in Selector)
        def dummyTarget = MethodHandles.dropArguments(
            MethodHandles.constant(Object, null), 0, type.parameterArray()
        )
        callSite.defaultTarget = dummyTarget
        callSite.fallbackTarget = dummyTarget

        // Prepare invocation arguments
        def receiver = new IndyInterfaceDeprecatedTest()
        Object[] args = [receiver] as Object[]

        // Call deprecated fromCache with the requested flags
        int callID = IndyInterface.CallType.METHOD.getOrderNumber() // expected to be 0
        Object result = IndyInterface.fromCache(
            callSite,
            IndyInterfaceDeprecatedTest,        // sender
            'foo',                              // methodName
            callID,
            Boolean.FALSE,                      // safeNavigation
            Boolean.TRUE,                       // thisCall (instance method)
            Boolean.FALSE,                      // spreadCall
            1,                                  // dummyReceiver (marker only)
            args
        )

        // Verify the local method foo was actually called
        assertEquals(receiver.foo(), result)
    }
}
