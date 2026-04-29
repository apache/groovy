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

import java.lang.reflect.Modifier

/**
 * Captures the joint-compilation surface for {@code @Final}.
 *
 * <p>The stubber flips {@code ACC_FINAL} on the annotated class / field /
 * method at CONVERSION so the stub presents the runtime final-ness to
 * {@code javac}. Without this, Java code in a joint compilation can
 * declare {@code class JavaSub extends ImmutableThing} against the stub —
 * and the runtime would either fail or break the immutability invariants.
 *
 * <p>This is the smallest possible stubber: a single modifier OR per
 * target. No metadata-key handoff is needed because the full transform's
 * {@code OR ACC_FINAL} is idempotent.
 */
final class FinalJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Sealed.groovy': '''
                package foo

                @groovy.transform.Final
                class Sealed {
                    String tag = 'fixed'
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static String tagOf(Sealed s) { return s.getTag(); }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: the class is declared final.
        String sealedStub = stubJavaSourceFor('foo.Sealed')
        assert sealedStub =~ /\bfinal\s+class\s+Sealed\b/

        // Runtime view: ACC_FINAL is set on the runtime class.
        Class sealedClass = loader.loadClass('foo.Sealed')
        assert Modifier.isFinal(sealedClass.modifiers)

        // Java compile-time check is implicit: a Java source declaring
        // 'class JavaSub extends Sealed' would fail to compile in this
        // joint-compilation unit — covered by the absence of any such
        // source above and the successful compilation of JavaUser.java.
    }
}
