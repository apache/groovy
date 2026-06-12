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
import java.beans.PropertyChangeListener

/**
 * Captures the joint-compilation surface for class-level {@code @Bindable}.
 *
 * <p>The stubber emits placeholder bodies for the seven JavaBeans
 * property-change methods plus the two getters; the full transform at
 * CANONICALIZATION discards the placeholders, installs the
 * {@code propertyChangeSupport} field, and adds the real method bodies.
 */
final class BindableJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Watched.groovy': '''
                package foo

                @groovy.beans.Bindable
                class Watched {
                    String name
                    int count
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.beans.PropertyChangeListener;

                public class JavaUser {
                    public static void attach(Watched w, PropertyChangeListener l) {
                        w.addPropertyChangeListener(l);
                    }
                    public static void detach(Watched w, PropertyChangeListener l) {
                        w.removePropertyChangeListener(l);
                    }
                    public static int listenerCount(Watched w) {
                        return w.getPropertyChangeListeners().length;
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: all seven methods are declared on Watched.
        String stub = stubJavaSourceFor('foo.Watched')
        assert stub =~ /void\s+addPropertyChangeListener\(\s*java\.beans\.PropertyChangeListener\s+\w+\s*\)/
        assert stub =~ /void\s+addPropertyChangeListener\(\s*java\.lang\.String\s+\w+\s*,\s*java\.beans\.PropertyChangeListener\s+\w+\s*\)/
        assert stub =~ /void\s+removePropertyChangeListener\(\s*java\.beans\.PropertyChangeListener\s+\w+\s*\)/
        assert stub =~ /void\s+removePropertyChangeListener\(\s*java\.lang\.String\s+\w+\s*,\s*java\.beans\.PropertyChangeListener\s+\w+\s*\)/
        assert stub =~ /void\s+firePropertyChange\(\s*java\.lang\.String\s+\w+\s*,\s*java\.lang\.Object\s+\w+\s*,\s*java\.lang\.Object\s+\w+\s*\)/
        assert stub =~ /java\.beans\.PropertyChangeListener\[\]\s+getPropertyChangeListeners\(\s*\)/
        assert stub =~ /java\.beans\.PropertyChangeListener\[\]\s+getPropertyChangeListeners\(\s*java\.lang\.String\s+\w+\s*\)/

        // Runtime view: a real listener attaches and fires for property changes.
        Class watchedClass = loader.loadClass('foo.Watched')
        def watched = watchedClass.newInstance()

        List<PropertyChangeEvent> events = []
        def listener = { PropertyChangeEvent ev -> events << ev } as PropertyChangeListener

        watched.addPropertyChangeListener(listener)
        assert watched.propertyChangeListeners.length == 1

        watched.name = 'first'
        watched.count = 1
        assert events.size() == 2
        assert events[0].propertyName == 'name'
        assert events[0].newValue == 'first'
        assert events[1].propertyName == 'count'
        assert events[1].newValue == 1

        watched.removePropertyChangeListener(listener)
        assert watched.propertyChangeListeners.length == 0
    }
}
