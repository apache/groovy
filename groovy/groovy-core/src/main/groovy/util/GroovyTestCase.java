/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.util;

import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A default JUnit TestCase in Groovy. This provides a number of helper methods
 * plus avoids the JUnit restriction of requiring all test* methods to be void
 * return type.
 * 
 * @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyTestCase extends TestCase {

    protected Logger log = Logger.getLogger(getClass().getName());
    private static int counter;
    private boolean useAgileDoxNaming = false;

    public GroovyTestCase() {
    }

    /**
     * Overload the getName() method to make the test cases look more like AgileDox
     * (thanks to Joe Walnes for this tip!)
     */
    public String getName() {
        if (useAgileDoxNaming) {
            return super.getName().substring(4).replaceAll("([A-Z])", " $1").toLowerCase();
        }
        else {
            return super.getName();
        }
    }

    public String getMethodName() {
        return super.getName();
    }

    protected void assertArrayEquals(Object[] expected, Object[] value) {
        String message =
            "expected array: " + InvokerHelper.toString(expected) + " value array: " + InvokerHelper.toString(value);
        assertNotNull(message + ": expected should not be null", value);
        assertNotNull(message + ": value should not be null", value);
        assertEquals(message, expected.length, value.length);
        for (int i = 0, size = expected.length; i < size; i++) {
            assertEquals("value[" + i + "] when " + message, expected[i], value[i]);
        }
    }
    protected void assertLength(int length, char[] array) {
        assertEquals(length, array.length);
    }

    protected void assertLength(int length, int[] array) {
        assertEquals(length, array.length);
    }

    protected void assertLength(int length, Object[] array) {
        assertEquals(length, array.length);
    }

    protected void assertContains(char expected, char[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

        message.append(expected + " not in {");

        for (int i = 0; i < array.length; ++i) {
            message.append("'" + array[i] + "'");

            if (i < (array.length - 1)) {
                message.append(", ");
            }
        }

        message.append(" }");

        fail(message.toString());
    }

    protected void assertContains(int expected, int[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

        message.append(expected + " not in {");

        for (int i = 0; i < array.length; ++i) {
            message.append("'" + array[i] + "'");

            if (i < (array.length - 1)) {
                message.append(", ");
            }
        }

        message.append(" }");

        fail(message.toString());
    }

    /**
     * Asserts that the value of toString() on the given object matches the
     * given text string
     * 
     * @param value the object to be output to the console
     * @param expected the expected String representation
     */
    protected void assertToString(Object value, String expected) {
        Object console = InvokerHelper.invokeMethod(value, "toString", null);
        assertEquals("toString() on value: " + value, expected, console);
    }

    /**
     * Asserts that the value of inspect() on the given object matches the
     * given text string
     * 
     * @param value the object to be output to the console
     * @param expected the expected String representation
     */
    protected void assertInspect(Object value, String expected) {
        Object console = InvokerHelper.invokeMethod(value, "inspect", null);
        assertEquals("inspect() on value: " + value, expected, console);
    }

    /**
     * Asserts that the script runs without any exceptions
     * @param script
     */
    protected void assertScript(final String script) throws Exception {
        log.info("About to execute script");
        //log.info(script);

        // lets write the file to the target directory so its available 
        // to the MetaClass.getClassNode()
        String testClassName = getTestClassName();

        File file = new File("target/test-classes/" + testClassName);

        log.info("Creating file " + file);

        DefaultGroovyMethods.withPrintWriter(file, new Closure(null) {
            protected void doCall(PrintWriter writer) {
                writer.println(script);
            }
        });

        GroovyShell shell = new GroovyShell();
        shell.evaluate(script, testClassName);
    }

    protected String getTestClassName() {
        return "TestScript" + getMethodName() + (counter++) + ".groovy";
    }

    /**
     * Asserts that the given code closure fails when it is evaluated
     * 
     * @param code
     */
    protected void shouldFail(Closure code) {
        boolean failed = false;
        try {
            code.call();
        }
        catch (Exception e) {
            failed = true;
            log.info("Worked: caught expected exception: " + e);
            //log.log(Level.INFO, "Worked: caught expected exception: " + e, e);
        }
        assertTrue("Closure " + code + " should have failed", failed);
    }



    /**
     *  Returns a copy of a string in which all EOLs are \n.
     */

    protected String fixEOLs( String value )
    {
        return value.replaceAll( "(\\r\\n?)|\n", "\n" );
    }
}
