package org.codehaus.groovy.jsr223;

import junit.framework.TestCase;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.List;
import java.util.Iterator;

/**
 * Tests JSR-223 Groovy engine implementation.
 * @author Jim White
 */
public class JSR223Test extends TestCase {
    protected ScriptEngineManager manager;

    protected void setUp() {
        manager = new ScriptEngineManager();
    }

    public void testGetEngineByEngineName() {
        ScriptEngine engine = manager.getEngineByName("groovy");
        assertNotNull(engine);
    }

    public void testGetEngineByLanguageName() {
        ScriptEngine engine = manager.getEngineByName("Groovy");
        assertNotNull(engine);
    }

    public void testGetEngineByExtension() {
        ScriptEngine engine = manager.getEngineByExtension("groovy");
        assertNotNull(engine);
    }

    public void testGetEngineByMIMEType() {
        ScriptEngine engine = manager.getEngineByMimeType("application/x-groovy");
        assertNotNull(engine);
    }

    public void testCheckParameters() {
        ScriptEngine engine = manager.getEngineByName("groovy");
        assertNotNull(engine);
        
        ScriptEngineFactory factory = engine.getFactory();
        assertNotNull(factory);

        boolean gotname = false;

        Iterator names = factory.getNames().iterator();
        while (names.hasNext()) {
            if (names.next().equals("groovy")) gotname = true;
        }
        assertTrue("Short name missing from factory", gotname);

        assertEquals("Groovy", factory.getLanguageName());
        assertNotNull(factory.getEngineVersion());
        assertNotNull(factory.getExtensions());
        assertNotNull(factory.getMimeTypes());

        assertEquals(InvokerHelper.getVersion(), factory.getLanguageVersion());
    }

    public void testSimpleExpr() throws ScriptException {
        ScriptEngine engine = manager.getEngineByName("groovy");
        assertNotNull(engine);

        assertEquals(Integer.valueOf(3), engine.eval("1 + 2"));
    }

    public void testSyntaxError() {
        ScriptEngine engine = manager.getEngineByName("groovy");
        assertNotNull(engine);

        try {
            Object x =  engine.eval("z");
            assertFalse("Didn't get ScriptException for syntax error", true);
        } catch (ScriptException e) {
        }
    }
}
