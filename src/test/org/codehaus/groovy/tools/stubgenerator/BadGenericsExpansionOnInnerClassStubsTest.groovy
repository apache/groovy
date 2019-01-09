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
package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-5675:
 * Stub compiler expands generic-inner-class variable declaration incorrectly
 *
 * GROOVY-6048
 * Stub compiler expends generics for outer class while using static inner class 
 * like a non-static inner class
 */
class BadGenericsExpansionOnInnerClassStubsTest extends StringSourcesStubTestCase  {

    Map<String, String> provideSources() {
        [
                    'AbstractProcessingQueue.groovy': '''
                    abstract class AbstractProcessingQueue<T> extends AbstractAgent {
                        protected Queue<ProcessingQueueMember<T>> items
                        private class ProcessingQueueMember<E> {}

                        interface ItemGenerator1{}
                        static <T> AbstractProcessingQueue<List<T>> createQueue1(List<Closure<T>> closures, List<AbstractProcessingQueue.ItemGenerator1> generators) {}
                        static class ItemGenerator2{}
                        static <T> AbstractProcessingQueue<List<T>> createQueue2(List<Closure<T>> closures, List<AbstractProcessingQueue.ItemGenerator2> generators) {}
                    }
                ''',
                    'AbstractAgent.java': '''
                    public abstract class AbstractAgent {}
                '''
                ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('AbstractProcessingQueue')

        assert stubSource.contains('protected java.util.Queue<AbstractProcessingQueue<T>.ProcessingQueueMember<T>> items;')
        assert !stubSource.contains('protected java.util.Queue<AbstractProcessingQueue.ProcessingQueueMember<T>> items;')
        assert stubSource.contains('public static <T> AbstractProcessingQueue<java.util.List<T>> createQueue1(java.util.List<groovy.lang.Closure<T>> closures, java.util.List<AbstractProcessingQueue.ItemGenerator1> generators)')
        assert stubSource.contains('public static <T> AbstractProcessingQueue<java.util.List<T>> createQueue2(java.util.List<groovy.lang.Closure<T>> closures, java.util.List<AbstractProcessingQueue.ItemGenerator2> generators)')
    }
}
