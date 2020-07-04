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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.Test;

import static org.codehaus.groovy.control.ParserPlugin.buildAST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ModuleNode}.
 */
public final class ModuleNodeTest {

    @Test
    public void testStatementClass() {
        ModuleNode mn = buildAST("x = [1, 2, 3]; println(x)", null, null, null);

        assertEquals(1, mn.getClasses().size());
        assertTrue(mn.getClasses().get(0).getName().startsWith("Script"));
        assertFalse("Should have statements", mn.getStatementBlock().isEmpty());
    }

    @Test // GROOVY-9194
    public void testScriptStartingWithHash() {
        ModuleNode mn = new ModuleNode((CompileUnit) null);
        mn.setDescription("#script.groovy");

        assertEquals("Dummy class name should not be empty", "#script", mn.getScriptClassDummy().getName());
    }

    @Test // GROOVY-9577
    public void testDuplicateImports() {
        //@formatter:off
        String source =
            "import java.lang.Object\n" +
            "import java.lang.Object\n" +
            "import java.lang.Object as X\n";
        //@formatter:on
        ModuleNode mn = buildAST(source, null, null, null);

        assertEquals(3, mn.getImports().size());
        assertEquals(3, mn.getImport("X").getLineNumber());
        assertEquals(2, mn.getImport("Object").getLineNumber());
        assertEquals("X", DefaultGroovyMethods.last(mn.getImports()).getAlias());
    }
}
