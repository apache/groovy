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

import org.apache.groovy.runtime.indy.AotDispatch
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.SwitchPoint

/**
 * The pre-selection stamp guard on AOT PIC writes ({@code IndyInterface.putSelected}),
 * deterministically: a wrapper selected while an invalidation landed reflects a MOP snapshot
 * that may predate the change, yet its construction-time stamp postdates it — caching it
 * would dispatch stale until an unrelated future invalidation, since under AOT no
 * SwitchPoint ever fires. The guard caches the sentinel instead, forcing re-selection on
 * the next hit. The race window is selection-start to PIC-write; these tests recreate it
 * by bumping the stamp between sampling and the write, no actual thread race required.
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
final class AotPutSelectedTest {

    private static CacheableCallSite siteLinkedWithAot(boolean aot) {
        String previous = System.getProperty(AotDispatch.FORCE_PROPERTY)
        if (aot) System.setProperty(AotDispatch.FORCE_PROPERTY, 'true')
        else System.clearProperty(AotDispatch.FORCE_PROPERTY)
        try {
            new CacheableCallSite(MethodType.methodType(Object, Object[]), MethodHandles.lookup())
        } finally {
            if (previous != null) {
                System.setProperty(AotDispatch.FORCE_PROPERTY, previous)
            } else {
                System.clearProperty(AotDispatch.FORCE_PROPERTY)
            }
        }
    }

    private static MethodHandleWrapper cacheableWrapper() {
        def mh = MethodHandles.empty(MethodType.methodType(Object, Object[]))
        new MethodHandleWrapper(mh, mh, null, true)
    }

    private static void bumpStamp() {
        AotDispatch.invalidateAll(new SwitchPoint[0])
    }

    @Test
    void 'a selection no invalidation raced is cached on an AOT site'() {
        def site = siteLinkedWithAot(true)
        def wrapper = cacheableWrapper()
        long pre = IndyInterface.preSelectionStamp(site)
        IndyInterface.putSelected(site, 'K', wrapper, pre)
        assert site.getIfPresent('K').is(wrapper)
    }

    @Test
    void 'a selection an invalidation raced caches the sentinel, never the possibly-stale wrapper'() {
        def site = siteLinkedWithAot(true)
        def wrapper = cacheableWrapper() // construction-time stamp postdates the bump below on a real race
        long pre = IndyInterface.preSelectionStamp(site)
        bumpStamp() // the invalidation landing mid-selection, made deterministic
        IndyInterface.putSelected(site, 'K', wrapper, pre)
        assert site.getIfPresent('K').is(MethodHandleWrapper.uncacheablePicSentinel)
    }

    @Test
    void 'an uncacheable selection stores the sentinel regardless of the stamp'() {
        def site = siteLinkedWithAot(true)
        def mh = MethodHandles.empty(MethodType.methodType(Object, Object[]))
        def uncacheable = new MethodHandleWrapper(mh, mh, null, false)
        long pre = IndyInterface.preSelectionStamp(site)
        IndyInterface.putSelected(site, 'K', uncacheable, pre)
        assert site.getIfPresent('K').is(MethodHandleWrapper.uncacheablePicSentinel)
    }

    @Test
    void 'on a non-AOT site the stamp is never consulted and the historical put is preserved'() {
        def site = siteLinkedWithAot(false)
        def wrapper = cacheableWrapper()
        long pre = IndyInterface.preSelectionStamp(site)
        assert pre == 0L
        bumpStamp() // irrelevant off the AOT path: SwitchPoint guards carry freshness there
        IndyInterface.putSelected(site, 'K', wrapper, pre)
        assert site.getIfPresent('K').is(wrapper)
    }
}
