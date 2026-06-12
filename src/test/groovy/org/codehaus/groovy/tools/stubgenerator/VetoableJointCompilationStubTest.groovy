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

import java.beans.PropertyChangeEvent
import java.beans.PropertyVetoException
import java.beans.VetoableChangeListener

/**
 * Captures the joint-compilation surface for class-level {@code @Vetoable}.
 *
 * <p>Symmetric to {@code @Bindable}: stubber emits the seven
 * Vetoable-listener methods (note {@code fireVetoableChange} declares
 * {@code throws PropertyVetoException}); the full transform at
 * CANONICALIZATION discards the placeholders and installs the real
 * bodies.
 */
final class VetoableJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Constrained.groovy': '''
                package foo

                @groovy.beans.Vetoable
                class Constrained {
                    int value
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.beans.PropertyVetoException;
                import java.beans.VetoableChangeListener;

                public class JavaUser {
                    public static void attach(Constrained c, VetoableChangeListener l) {
                        c.addVetoableChangeListener(l);
                    }
                    public static int listenerCount(Constrained c) {
                        return c.getVetoableChangeListeners().length;
                    }
                    // PropertyVetoException must be declared on a Java caller of
                    // a fireVetoableChange-style API; this exercises the stubber's
                    // throws clause.
                    public static void fire(Constrained c, String name, Object oldV, Object newV)
                            throws PropertyVetoException {
                        c.fireVetoableChange(name, oldV, newV);
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: all seven methods are declared on Constrained,
        // including the throws clause on fireVetoableChange.
        String stub = stubJavaSourceFor('foo.Constrained')
        assert stub =~ /void\s+addVetoableChangeListener\(\s*java\.beans\.VetoableChangeListener\s+\w+\s*\)/
        assert stub =~ /void\s+removeVetoableChangeListener\(\s*java\.beans\.VetoableChangeListener\s+\w+\s*\)/
        assert stub =~ /void\s+fireVetoableChange\(\s*java\.lang\.String\s+\w+\s*,\s*java\.lang\.Object\s+\w+\s*,\s*java\.lang\.Object\s+\w+\s*\)\s*throws\s+java\.beans\.PropertyVetoException/
        assert stub =~ /java\.beans\.VetoableChangeListener\[\]\s+getVetoableChangeListeners\(\s*\)/

        // Runtime view: vetoing listener can block a property write.
        Class constrainedClass = loader.loadClass('foo.Constrained')
        def constrained = constrainedClass.newInstance()

        def vetoer = { PropertyChangeEvent ev ->
            if (((int) ev.newValue) < 0) {
                throw new PropertyVetoException('negative not allowed', ev)
            }
        } as VetoableChangeListener

        constrained.addVetoableChangeListener(vetoer)
        constrained.value = 10
        assert constrained.value == 10

        // Vetoing listener throws — value should not change.
        try {
            constrained.value = -1
            assert false : 'expected PropertyVetoException'
        } catch (PropertyVetoException ignore) {
            // expected
        }
        assert constrained.value == 10
    }
}
