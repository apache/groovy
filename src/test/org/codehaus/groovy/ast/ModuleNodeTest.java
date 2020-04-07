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

import org.codehaus.groovy.syntax.parser.TestParserSupport;

import java.util.List;

/**
 * Tests the ClassNode
 */
public class ModuleNodeTest extends TestParserSupport {

    public void testStatementClass() {
        ModuleNode module = parse("x = [1, 2, 3]; println(x)", "Cheese.groovy");
        assertFalse("Should have statements", module.getStatementBlock().isEmpty());

        List<ClassNode> classes = module.getClasses();
        assertEquals("Number of classes", 1, classes.size());

        ClassNode classNode = (ClassNode) classes.get(0);
        assertEquals("Class name", "Cheese", classNode.getName());
    }

    // GROOVY-9194
    public void testScriptStartingWithHash() {
        ModuleNode mn = new ModuleNode((CompileUnit) null);
        mn.setDescription("#script.groovy");
        ClassNode cn = mn.getScriptClassDummy();
        assertEquals("Dummy class name should not be empty", "#script", cn.getName());
    }
}
