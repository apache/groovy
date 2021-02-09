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
package org.codehaus.groovy.ast.builder;

import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport.closeQuietly;

/**
 * This class handles converting Strings to ASTNode lists.
 */
public class AstStringCompiler {

    /**
     * Compiles the specified source code and returns its statement block and
     * any declared types.
     *
     * @param script
     *      a Groovy script in String form
     *
     * @since 3.0.0
     */
    public List<ASTNode> compile(final String script) {
        return compile(script, CompilePhase.CONVERSION, true);
    }

    /**
     * Compiles the specified source code and returns its statement block, the
     * script class (if desired) and any declared types.
     *
     * @param script
     *      a Groovy script in String form
     * @param compilePhase
     *      the last compilation phase to complete
     * @param statementsOnly
     *      if {@code true}, exclude the script class from the result
     *
     * @since 1.7.0
     */
    public List<ASTNode> compile(final String script, final CompilePhase compilePhase, final boolean statementsOnly) {
        String scriptClassName = makeScriptClassName();
        GroovyCodeSource codeSource = new GroovyCodeSource(script, scriptClassName + ".groovy", "/groovy/script");
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, codeSource.getCodeSource(), null);
        try {
            cu.addSource(codeSource.getName(), script);
            cu.compile(compilePhase.getPhaseNumber());
        } finally {
            closeQuietly(cu.getClassLoader());
        }

        List<ASTNode> nodes = new ArrayList<>();
        for (ModuleNode mn : cu.getAST().getModules()) {
            ASTNode statementBlock = mn.getStatementBlock();
            if (statementBlock != null) {
                nodes.add(statementBlock);
            }
            for (ClassNode cn : mn.getClasses()) {
                if (!(statementsOnly && scriptClassName.equals(cn.getName()))) {
                    nodes.add(cn);
                }
            }
        }
        return nodes;
    }

    private static String makeScriptClassName() {
        return "Script" + System.nanoTime();
    }
}
