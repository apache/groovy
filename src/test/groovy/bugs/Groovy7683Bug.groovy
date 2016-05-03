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

import org.codehaus.groovy.reflection.ClassInfo

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue

/**
 * NOTE: Test uses multiple calls to System.gc() and throws an OutOfMemoryError in attempt to
 * demonstrate changes fix the issue.  This test should not be included if the PR is accepted.
 */
class Groovy7683Bug extends GroovyTestCase {

    private static final int NUM_OBJECTS = 31

    ReferenceQueue<ClassLoader> classLoaderQueue = new ReferenceQueue<ClassLoader>()
    ReferenceQueue<Class<?>> classQueue = new ReferenceQueue<Class<?>>()
    ReferenceQueue<ClassInfo> classInfoQueue = new ReferenceQueue<ClassInfo>()

    // Used to keep a hard reference to the PhantomReferences so they are not collected
    List<Object> refList = new ArrayList<Object>(NUM_OBJECTS)

    void testLeak() {
        assert !Boolean.getBoolean('groovy.use.classvalue')
        for (int i = 0; i < NUM_OBJECTS; i++) {
            GroovyClassLoader gcl = new GroovyClassLoader()
            Class scriptClass = gcl.parseClass("int myvar = " + i)
            ClassInfo ci = ClassInfo.getClassInfo(scriptClass)
            PhantomReference<ClassLoader> classLoaderRef = new PhantomReference<>(gcl, classLoaderQueue)
            PhantomReference<Class<?>> classRef = new PhantomReference<Class<?>>(scriptClass, classQueue)
            PhantomReference<ClassInfo> classInfoRef = new PhantomReference<ClassInfo>(ci, classInfoQueue)
            refList.add(classLoaderRef)
            refList.add(classRef)
            refList.add(classInfoRef)
            System.gc()
        }
        System.gc()
        // Encourage GC to collect soft references
        try { throw new OutOfMemoryError() } catch(OutOfMemoryError oom) { }
        System.gc()

        // Ensure that at least 90% of objects should have been collected, we can't guarantee 100% because
        // System.gc() is not guaranteed to run on each call.
        int targetCollectedCount = Math.floor(NUM_OBJECTS * 0.9f)
        assert queueSize(classLoaderQueue) >= targetCollectedCount //GroovyClassLoaders not collected by GC
        assert queueSize(classQueue) >= targetCollectedCount //Script Classes not collected by GC
        assert queueSize(classInfoQueue) >= targetCollectedCount //ClassInfo objects not collected by GC
    }

    private int queueSize(ReferenceQueue<?> queue) {
        int size = 0
        while (queue.poll() != null) {
            ++size
        }
        return size
    }

}