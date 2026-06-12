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
 * Captures the desired joint-compilation surface for {@code @Sortable} on classes
 * with trait properties.
 */
final class SortableJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Task.groovy': '''
                package foo

                import groovy.transform.Sortable

                trait Prioritized {
                    Integer priority
                }

                @Sortable
                class Task implements Prioritized {
                    String label
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static Task createTask() {
                        return new Task();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // The joint-compilation stub view: only the surface contributed by the
        // CONVERSION-phase stubber piece is visible. The placeholder compareTo
        // returns 0 because trait properties have not yet been injected.
        String taskStub = stubJavaSourceFor('foo.Task')

        assert taskStub.contains('java.lang.Comparable<foo.Task>')
        assert taskStub =~ /int\s+compareTo\(foo\.Task\s+\w+\)/
        assert taskStub =~ /int\s+compareTo\([^)]+\)\s*\{\s*return\s+0;\s*\}/

        // The runtime view: the full transform at CANONICALIZATION runs after
        // TraitASTTransformation, so the trait property `priority` IS visible
        // to Sortable. Sort uses label then priority, and the priority
        // comparator method is generated.
        Class taskClass = loader.loadClass('foo.Task')
        def tasks = [
            taskClass.newInstance(label: 'same', priority: 2),
            taskClass.newInstance(label: 'same', priority: 1)
        ]

        assert tasks.sort()*.priority == [1, 2]
        assert taskClass.methods.any { it.name == 'comparatorByPriority' }
    }
}

