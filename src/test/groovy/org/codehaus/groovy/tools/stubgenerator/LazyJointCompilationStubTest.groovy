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
 * Captures the joint-compilation surface for {@code @Lazy}.
 *
 * <p>GEP-21 learnings:
 * <ol>
 *   <li>The CONVERSION-phase invoker correctly dispatches a field-level
 *       annotation to its {@code FieldNode} parent (not the enclosing class).</li>
 *   <li>Stub/runtime alignment is achievable for {@code @Lazy} with a
 *       small, targeted mutation in the stubber: marking the property
 *       final causes {@code Verifier.visitProperty} to skip setter
 *       generation. No need to duplicate the full transform's
 *       backing-field rewiring — we only need to influence what the stub
 *       generator emits.</li>
 *   <li>That generalises: a stubber doesn't have to duplicate the full
 *       transform's work; it just has to ensure the stub-time AST
 *       presents the right surface. Knowing how Verifier and the stub
 *       generator interpret the AST is enough.</li>
 * </ol>
 */
final class LazyJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Holder.groovy': '''
                package foo

                class Holder {
                    @Lazy String value = { "computed" }()
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static String fetch() {
                        return new Holder().getValue();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String holderStub = stubJavaSourceFor('foo.Holder')

        // Stub view: getter present, setter absent — matches what the
        // runtime class actually exposes after the full transform runs.
        assert holderStub =~ /java\.lang\.String\s+getValue\(\s*\)/
        assert !(holderStub =~ /void\s+setValue\(/)

        // Runtime confirmation: only getValue, no setValue.
        Class holderClass = loader.loadClass('foo.Holder')
        assert holderClass.declaredMethods.any { it.name == 'getValue' }
        assert !holderClass.declaredMethods.any { it.name == 'setValue' }

        // Java consumer's call path works.
        assert holderClass.newInstance().value == 'computed'
    }
}
