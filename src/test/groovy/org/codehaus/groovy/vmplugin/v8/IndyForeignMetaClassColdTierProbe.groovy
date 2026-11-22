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

import org.codehaus.groovy.vmplugin.v8.IndyForeignMetaClassSwitchPointTest.Receiver

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

/**
 * Child-process probe that runs the method-call scenarios of {@link IndyForeignMetaClassSwitchPointTest}, the calls
 * the reflective cold tier serves, with {@code -Dgroovy.indy.cold.reflection=true}, which {@link IndyInterface} reads
 * once at class init. Invoked from that test; prints {@code OK} on success.
 */
final class IndyForeignMetaClassColdTierProbe {

    static void main(String[] args) {
        def site = new CacheableCallSite(MethodType.methodType(Object, Object[]), MethodHandles.lookup())
        assert IndyInterface.coldReflectionEnabled(site) : 'probe requires -Dgroovy.indy.cold.reflection=true'

        def test = new IndyForeignMetaClassSwitchPointTest()
        for (receiver in Receiver.values()) {
            run(test, receiver) {
                test.'a call site sees a change to the metaclass a proxy shares with the type it stands in for'(receiver)
            }
            run(test, receiver) {
                test.'a call site sees a method replaced on that metaclass after it called the real one'(receiver)
            }
        }
        println 'OK'
    }

    private static void run(IndyForeignMetaClassSwitchPointTest test, Receiver receiver, Closure scenario) {
        test.installMetaClass()
        try {
            scenario()
        } catch (AssertionError e) {
            throw new AssertionError(receiver.name(), e)
        } finally {
            test.removeMetaClass()
        }
    }
}
