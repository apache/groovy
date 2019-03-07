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
package org.codehaus.groovy.tools.shell.commands

/**
 * Tests for the {@link ImportCommand} class.
 */
class ImportCommandTest
    extends CommandTestSupport
{
    void testPatternClassOrMethodName() {
        assert 'java.util.*'.matches(ImportCommand.IMPORTED_ITEM_PATTERN)
        assert 'java.util.Pattern'.matches(ImportCommand.IMPORTED_ITEM_PATTERN)
        assert 'static java.util.Pattern.match'.matches(ImportCommand.IMPORTED_ITEM_PATTERN)
        assert 'java.util.Pattern as Fattern'.matches(ImportCommand.IMPORTED_ITEM_PATTERN)
        assert 'java.util.Pattern'.matches(ImportCommand.IMPORTED_ITEM_PATTERN)
        assert 'java.util.P_attern'.matches(ImportCommand.IMPORTED_ITEM_PATTERN)
    }

    void testImport() {
        assert null == shell << 'import'
        assert 'java.awt.TextField' == shell << 'import java.awt.TextField'
        // test semicolon does not lead to duplicate import
        assert 'java.awt.TextField' == shell << 'import java.awt.TextField;'
        // test last import is added at the end
        assert 'java.awt.TextField, java.awt.TextArea' == shell << 'import java.awt.TextArea;'
        assert 'java.awt.TextArea, java.awt.TextField' == shell << 'import java.awt.TextField;'
        // test multiple commands are not allowed (as they would be executed on every next buffer evaluation)
        assert null == shell << 'import java.awt.TextField; println("foo")'
        // test *, recognizing unnecessary imports sadly not implemented
        assert 'java.awt.TextArea, java.awt.TextField, java.awt.*' == shell << 'import java.awt.*'
        // test numerics being allowed in class/package names
        assert 'java.awt.TextArea, java.awt.TextField, java.awt.*, org.w3c.dom.*' == shell << 'import org.w3c.dom.*'
        assert 'java.awt.TextArea, java.awt.TextField, java.awt.*, org.w3c.dom.*, java.awt.Graphics2D' == shell << 'import java.awt.Graphics2D'
    }
}
