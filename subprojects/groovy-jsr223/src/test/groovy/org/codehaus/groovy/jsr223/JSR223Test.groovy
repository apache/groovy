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
package org.codehaus.groovy.jsr223

import groovy.test.GroovyTestCase

import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import javax.script.SimpleScriptContext

/**
 * Tests JSR-223 Groovy engine implementation.
 */
class JSR223Test extends GroovyTestCase {
    protected ScriptEngineManager manager

    static final Object[] EMPTY_ARGS = new Object[0]

    protected void setUp() {
        manager = new ScriptEngineManager()
    }

    void testGetEngineByEngineName() {
        ScriptEngine engine = manager.getEngineByName("groovy")
        assertNotNull(engine)
    }

    void testGetEngineByLanguageName() {
        ScriptEngine engine = manager.getEngineByName("Groovy")
        assertNotNull(engine)
    }

    void testGetEngineByExtension() {
        ScriptEngine engine = manager.getEngineByExtension("groovy")
        assertNotNull(engine)
    }

    void testGetEngineByMIMEType() {
        ScriptEngine engine = manager.getEngineByMimeType("application/x-groovy")
        assertNotNull(engine)
    }

    void testCheckParameters() {
        ScriptEngine engine = manager.getEngineByName("groovy")
        assertNotNull(engine)
        
        ScriptEngineFactory factory = engine.getFactory()
        assertNotNull(factory)

        boolean gotname = false

        Iterator names = factory.getNames().iterator()
        while (names.hasNext()) {
            if (names.next().equals("groovy")) gotname = true
        }
        assertTrue("Short name missing from factory", gotname)

        assertEquals("Groovy", factory.getLanguageName())
        assertNotNull(factory.getEngineVersion())
        assertNotNull(factory.getExtensions())
        assertNotNull(factory.getMimeTypes())

        assertEquals(GroovySystem.getVersion(), factory.getLanguageVersion())
    }

    void testSimpleExpr() {
        ScriptEngine engine = manager.getEngineByName("groovy")
        assertNotNull(engine)

        assertEquals(Integer.valueOf(3), engine.eval("1 + 2"))
    }

    void testSyntaxError() {
        ScriptEngine engine = manager.getEngineByName("groovy")
        assertNotNull(engine)

        try {
            Object x =  engine.eval("z")
            assertFalse("Didn't get ScriptException for syntax error", true)
        } catch (ScriptException e) {
        }
    }

    void testEmptyScriptWithJustImports() {
        ScriptEngine engine = manager.getEngineByName("groovy")
        assertNotNull(engine)

        // GROOVY-3711: The eval of the following script earlier resulted in NPE
        // as groovy did not generate a script class in this case
        engine.eval("import java.lang.*")
    }

    /**
     * Fix for GROOVY-3669:
     * Can't use several times the same JSR-223 ScriptContext for different groovy script 
     */
    void testGroovy3669() {
        def scriptContext = new SimpleScriptContext()

        def sem = new ScriptEngineManager()

        def engine1 = sem.getEngineByName("groovy")
        def sw1 = new StringWriter()
        scriptContext.writer = sw1
        engine1.eval("print 'one'", scriptContext)

        assert sw1.toString() == 'one', "'one' should have been printed"

        def engine2 = sem.getEngineByName("groovy")
        def sw2 = new StringWriter()
        scriptContext.writer = sw2
        engine2.eval("print 'two'", scriptContext)

        assert sw2.toString() == 'two', "'two' should have been printed instead of '${sw2.toString()}'"
        assert sw1.toString() == 'one', "The output shouldn't have changed and still be 'one' instead of '${sw1.toString()}'"

        assert !scriptContext.getAttribute("out"), "Groovy's 'out' variable should not be left in the script context"
        assert !scriptContext.getAttribute("context"), "The 'context' should not be left in the script context"
    }

    /**
     * Fix for GROOVY-3816
     * Evaluating a class should return the class, and not attempt to run it
     */
    void testGroovy3816() {
        def engine = new ScriptEngineManager().getEngineByName('groovy')

        def clazz = engine.eval('class Jsr223Foo {}')
        assert clazz

        def instance = engine.eval('new Jsr223Foo()')
        assert instance

        assert instance.class.isAssignableFrom(clazz)
    }

    void testInvokeFunctionRedirectsOutputToContextWriter() {
        def engine = manager.getEngineByName('groovy')
        StringWriter writer = new StringWriter()
        engine.getContext().setWriter(writer)
        String code = 'def myFunction() { print "Hello World!" }'
        engine.eval(code)
        ((Invocable) engine).invokeFunction('myFunction', EMPTY_ARGS)
        assert writer.toString() == 'Hello World!'

        // make sure changes to writer are handled
        writer = new StringWriter()
        StringWriter writer2 = new StringWriter()
        engine.getContext().setWriter(writer2)
        ((Invocable) engine).invokeFunction('myFunction', EMPTY_ARGS)
        assert writer.toString() == ''
        assert writer2.toString() == 'Hello World!'
    }

    void testInvokeFunctionRedirectsOutputToContextOut() {
        def engine = manager.getEngineByName('groovy')
        StringWriter writer = new StringWriter()
        StringWriter unusedWriter = new StringWriter()
        engine.getContext().setWriter(unusedWriter)
        engine.put('out', writer)
        String code = 'def myFunction() { print "Hello World!" }'
        engine.eval(code)
        ((Invocable) engine).invokeFunction('myFunction', EMPTY_ARGS)
        assert unusedWriter.toString() == ''
        assert writer.toString() == 'Hello World!'

        // make sure changes to writer are handled
        writer = new StringWriter()
        StringWriter writer2 = new StringWriter()
        engine.put('out', writer2)
        ((Invocable) engine).invokeFunction('myFunction', EMPTY_ARGS)
        assert unusedWriter.toString() == ''
        assert writer.toString() == ''
        assert writer2.toString() == 'Hello World!'
    }

    void testEngineContextAccessibleToScript() {
        def engine = manager.getEngineByName('groovy')
        ScriptContext engineContext = engine.getContext()
        engine.put('theEngineContext', engineContext)
        String code = '[answer: theEngineContext.is(context)]'
        assert engine.eval(code).answer == true
    }

    void testContextBindingOverridesEngineContext() {
        def engine = manager.getEngineByName('groovy')
        ScriptContext engineContext = engine.getContext()
        def otherContext = [foo: 'bar']
        engine.put('context', otherContext)
        engine.put('theEngineContext', engineContext)
        String code = '[answer: context.is(theEngineContext) ? "wrong" : context.foo]'
        assert engine.eval(code).answer == 'bar'
    }

    void testScriptFactorySameAsEngineFactory() {
        ScriptEngineFactory factory = new GroovyScriptEngineFactory()
        ScriptEngine engine = factory.getScriptEngine()
        assert engine.getFactory() == factory
    }

    void testGetInterfaceScenarios() {
        assertScript '''
        interface Test { def foo(); def bar(); def baz() }
        def engine = new javax.script.ScriptEngineManager().getEngineByName("groovy")
        engine.eval("def foo() { 42 }")
        engine.eval("def bar() { throw new Exception('Boom!') }")
        def test = engine.getInterface(Test)
        assert test.foo() == 42

        try {
            test.bar()
            assert false
        } catch(RuntimeException re) {
            assert re.message.endsWith('Boom!')
        }

        try {
            test.baz()
            assert false
        } catch(RuntimeException re) {
            assert re.cause.class.name.endsWith('MissingMethodException')
        }
        '''
    }

    void testGroovy9430() {
        ScriptEngineFactory factory = new GroovyScriptEngineFactory()
        ScriptEngine engine = factory.getScriptEngine()
        ScriptContext context = new SimpleScriptContext()
        context.setAttribute(ScriptEngine.FILENAME, "testGroovy9430.groovy", ScriptContext.ENGINE_SCOPE)
        assert 'testGroovy9430.groovy' == engine.generateScriptName(context)
    }
}
