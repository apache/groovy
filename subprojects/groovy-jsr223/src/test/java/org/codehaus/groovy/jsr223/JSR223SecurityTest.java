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
package org.codehaus.groovy.jsr223;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.junit.Before;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Test for GROOVY-3946 and GROOVY-5255.
 */
public class JSR223SecurityTest {

    class TestFixture {
        String script = "System.exit 2";
        String forbiddenInstruction = "java.lang.System";
    }

    TestFixture testFixture;

    @Before
    public void resetTestFixture() {
        testFixture = new TestFixture();
    }

    @Test(expected = ScriptException.class)
    public void should_forbid_an_instruction_when_overriding_GroovyClassLoader_using_reflection() throws Exception {
        secureEval(ClassLoaderDefinitionType.REFLECTION);
    }

    @Test(expected = ScriptException.class)
    public void should_forbid_an_instruction_when_overriding_GroovyClassLoader_using_injection() throws Exception {
        secureEval(ClassLoaderDefinitionType.INJECTION);
    }

    @Test(expected = ScriptException.class)
    public void should_forbid_an_instruction_when_overriding_GroovyClassLoader_using_constructor() throws Exception {
        secureEval(ClassLoaderDefinitionType.CONSTRUCTOR);
    }

    private void secureEval(ClassLoaderDefinitionType classLoaderDefType) throws Exception {
        ScriptEngine engine = createScriptEngine(classLoaderDefType);

        GroovySecurityManager securityMgr = GroovySecurityManager.instance();
        securityMgr.overrideGroovyClassLoader(engine, classLoaderDefType);
        securityMgr.forbid(testFixture.forbiddenInstruction);

        engine.eval(testFixture.script);
    }

    private ScriptEngine createScriptEngine(ClassLoaderDefinitionType classLoaderDefType) {
        return (classLoaderDefType == ClassLoaderDefinitionType.CONSTRUCTOR)
                ? new GroovyScriptEngineImpl(new CustomGroovyClassLoader())
                : new ScriptEngineManager().getEngineByName("groovy");
    }
}

enum ClassLoaderDefinitionType {
    CONSTRUCTOR,
    INJECTION,
    REFLECTION
}

class GroovySecurityManager {

    private static GroovySecurityManager instance = new GroovySecurityManager();

    private Set<String> blacklist = new HashSet<String>();

    private GroovySecurityManager() { }

    public synchronized static GroovySecurityManager instance() {
        return instance;
    }

    public void overrideGroovyClassLoader(ScriptEngine engine, ClassLoaderDefinitionType classLoaderDefType) {
        try {
            if (classLoaderDefType == ClassLoaderDefinitionType.REFLECTION) {
                overrideDefaultGroovyClassLoaderUsingReflection(engine);
            }
            else if (classLoaderDefType == ClassLoaderDefinitionType.INJECTION) {
                overrideDefaultGroovyClassLoaderUsingInjection(engine);
            }
        }
        catch (Throwable ex) {
            throw new RuntimeException("Could not initialize the security manager", ex);
        }
    }

    public void forbid(String instruction) {
        blacklist.add(instruction);
    }

    public boolean isForbidden(String instruction) {
        for (String forbidden : blacklist)
            if (instruction.startsWith(forbidden))
                return true;

        return false;
    }

    private void overrideDefaultGroovyClassLoaderUsingReflection(ScriptEngine engine) throws Exception {
        Field classLoader = engine.getClass().getDeclaredField("loader");
        classLoader.setAccessible(true);
        classLoader.set(engine, new CustomGroovyClassLoader());
    }

    private void overrideDefaultGroovyClassLoaderUsingInjection(ScriptEngine engine) throws Exception {
        GroovyScriptEngineImpl concreteEngine = (GroovyScriptEngineImpl) engine;
        concreteEngine.setClassLoader(new CustomGroovyClassLoader());
    }
}

class GroovySecurityException extends RuntimeException {

    public GroovySecurityException(String message) {
        super(message);
    }
}

class CustomGroovyClassLoader extends GroovyClassLoader {

    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
        CompilationUnit unit = super.createCompilationUnit(config, source);
        unit.addPhaseOperation(new CustomPrimaryClassNodeOperation(), Phases.SEMANTIC_ANALYSIS);
        return unit;
    }
}

class CustomPrimaryClassNodeOperation extends PrimaryClassNodeOperation {

    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        for (Object statement : source.getAST().getStatementBlock().getStatements())
            ((ExpressionStatement) statement).accept(new CustomCodeVisitorSupport());
    }
}

class CustomCodeVisitorSupport extends CodeVisitorSupport {

    private GroovySecurityManager groovySecurityManager = GroovySecurityManager.instance();

    public void visitMethodCallExpression(MethodCallExpression call) {
        if (groovySecurityManager.isForbidden(call.getText()))
            throw new GroovySecurityException("The following code is forbidden in the script: " + call.getText());
    }
}
