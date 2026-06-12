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
import java.lang.invoke.MethodType
import static org.junit.jupiter.api.Assertions.*

final class IndyInterfacePicTest {

    static class Receiver1 { String foo() { "r1" } }
    static class Receiver2 { String foo() { "r2" } }
    static class Receiver3 { String foo() { "r3" } }
    static class Receiver4 { String foo() { "r4" } }
    static class Receiver5 { String foo() { "r5" } }

    @Test
    void testPicChainGrowthAndLimit() {
        MethodType type = MethodType.methodType(Object, Object)
        // Use bootstrap for proper initialization
        CacheableCallSite callSite = (CacheableCallSite) IndyInterface.bootstrap(
                java.lang.invoke.MethodHandles.lookup(), "invoke", type, "foo", 0)

        // Initial state
        assertEquals(0, callSite.getPicCount())
        assertNull(callSite.getPicChain())

        def receivers = [new Receiver1(), new Receiver2(), new Receiver3(), new Receiver4(), new Receiver5()]
        int picLimit = IndyInterface.INDY_PIC_SIZE
        int optimizeThreshold = (int) IndyInterface.INDY_OPTIMIZE_THRESHOLD

        receivers.eachWithIndex { receiver, i ->
            Object[] args = [receiver] as Object[]

            // Trigger method selection and optimization
            // We need to exceed INDY_OPTIMIZE_THRESHOLD
            (optimizeThreshold + 10).times {
                callSite.getTarget().invokeWithArguments(args)
            }

            if (i < picLimit) {
                assertEquals(i + 1, callSite.getPicCount(), "PIC should grow for receiver ${i+1}")
                assertNotNull(callSite.getPicChain())
            } else {
                assertEquals(picLimit, callSite.getPicCount(), "PIC should stop growing at limit")
            }
        }
    }

    @Test
    void testPicResetOnMetaClassChange() {
        MethodType type = MethodType.methodType(Object, Object)
        CacheableCallSite callSite = (CacheableCallSite) IndyInterface.bootstrap(
                java.lang.invoke.MethodHandles.lookup(), "invoke", type, "foo", 0)

        def receiver = new Receiver1()
        Object[] args = [receiver] as Object[]

        int optimizeThreshold = (int) IndyInterface.INDY_OPTIMIZE_THRESHOLD

        // Fill PIC
        (optimizeThreshold + 10).times {
            callSite.getTarget().invokeWithArguments(args)
        }
        assertTrue(callSite.getPicCount() > 0)

        // Trigger global invalidation (reset)
        callSite.resetFallbackCount()

        assertEquals(0, callSite.getPicCount())
        assertNull(callSite.getPicChain())
    }

}
