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
 * Captures the joint-compilation surface for {@code @ListenerList}.
 *
 * <p>The stubber walks the listener's generic type at CONVERSION,
 * derives the listener interface, and emits placeholders for the
 * {@code add/removeXxxListener}, {@code getXxxListeners}, and per-method
 * {@code fireYyy} APIs that the full transform produces at
 * CANONICALIZATION.
 *
 * <p>This spike uses {@link java.awt.event.ActionListener} (a small,
 * classpath-resolvable single-method listener) so the listener's method
 * set is fully known at CONVERSION.
 */
final class ListenerListJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Button.groovy': '''
                package foo

                import java.awt.event.ActionListener

                class Button {
                    @groovy.beans.ListenerList
                    List<ActionListener> listeners = []
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.awt.event.ActionEvent;
                import java.awt.event.ActionListener;

                public class JavaUser {
                    public static void attach(Button b, ActionListener l)  { b.addActionListener(l); }
                    public static void detach(Button b, ActionListener l)  { b.removeActionListener(l); }
                    public static int count(Button b)                      { return b.getActionListeners().length; }
                    public static void clickAll(Button b, ActionEvent ev)  { b.fireActionPerformed(ev); }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: add / remove / get / fire methods declared.
        String stub = stubJavaSourceFor('foo.Button')
        assert stub =~ /void\s+addActionListener\(\s*java\.awt\.event\.ActionListener\s+\w+\s*\)/
        assert stub =~ /void\s+removeActionListener\(\s*java\.awt\.event\.ActionListener\s+\w+\s*\)/
        assert stub =~ /java\.awt\.event\.ActionListener\[\]\s+getActionListeners\(\s*\)/
        assert stub =~ /void\s+fireActionPerformed\(\s*java\.awt\.event\.ActionEvent\s+\w+\s*\)/

        // Runtime view: real bodies installed.
        Class buttonClass = loader.loadClass('foo.Button')
        def button = buttonClass.newInstance()

        List<java.awt.event.ActionEvent> received = []
        java.awt.event.ActionListener listener = { java.awt.event.ActionEvent e -> received << e }
                as java.awt.event.ActionListener

        button.addActionListener(listener)
        assert button.actionListeners.length == 1

        def ev = new java.awt.event.ActionEvent(button, 0, 'click')
        button.fireActionPerformed(ev)
        assert received.size() == 1
        assert received[0] == ev

        button.removeActionListener(listener)
        assert button.actionListeners.length == 0
    }
}
