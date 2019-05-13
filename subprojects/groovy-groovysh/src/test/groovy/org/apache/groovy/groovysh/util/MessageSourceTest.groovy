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
package org.codehaus.groovy.tools.shell.util

/**
 * Unit tests for the {@link MessageSource} class.
 */
class MessageSourceTest extends GroovyTestCase {
    private MessageSource messages

    @Override
    void setUp() {
        super.setUp()
        messages = new MessageSource(this.class)
    }

    void testLoadAndGetMessage() {
        def a = messages['a']
        assert '1' == a

        def b = messages['b']
        assert '2' == b

        def c = messages['c']
        assert '3' == c

        def f = messages.format('f', a, b, c)
        assert '1 2 3' == f
    }
}
