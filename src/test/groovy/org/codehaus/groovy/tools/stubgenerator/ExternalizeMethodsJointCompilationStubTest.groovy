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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Externalizable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Captures the joint-compilation surface for {@code @ExternalizeMethods}.
 *
 * <p>The stubber adds the {@link Externalizable} interface to the class
 * header and emits {@code writeExternal(ObjectOutput)} /
 * {@code readExternal(ObjectInput)} placeholders. The full transform
 * replaces the bodies via the metadata-key handoff. Java consumers in a
 * joint compilation see the class as a valid {@code Externalizable}
 * implementation.
 */
final class ExternalizeMethodsJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Record.groovy': '''
                package foo

                @groovy.transform.ExternalizeMethods
                class Record {
                    String name
                    int count
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.io.Externalizable;

                public class JavaUser {
                    // Java consumer that statically depends on Record being
                    // Externalizable — would not compile against the stub
                    // without the stubber piece.
                    public static Externalizable asExternalizable(Record r) {
                        return r;
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: Externalizable on the implements clause; both methods
        // declared on the class.
        String recordStub = stubJavaSourceFor('foo.Record')
        assert recordStub.contains('java.io.Externalizable')
        assert recordStub =~ /void\s+writeExternal\(\s*java\.io\.ObjectOutput\s+\w+\s*\)/
        assert recordStub =~ /void\s+readExternal\(\s*java\.io\.ObjectInput\s+\w+\s*\)/

        // Runtime view: real bodies present, Externalizable round-trip works.
        Class recordClass = loader.loadClass('foo.Record')
        assert Externalizable.isAssignableFrom(recordClass)

        // Round-trip via direct writeExternal / readExternal (matching levels;
        // mixing with writeObject/readObject would invoke the higher-level
        // Java serialization protocol which adds class metadata headers).
        def original = recordClass.newInstance(name: 'alpha', count: 42)
        def baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).withCloseable {
            ((Externalizable) original).writeExternal(it)
        }

        def restored = recordClass.newInstance()
        new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).withCloseable {
            ((Externalizable) restored).readExternal(it)
        }
        assert restored.name == 'alpha'
        assert restored.count == 42
    }
}
