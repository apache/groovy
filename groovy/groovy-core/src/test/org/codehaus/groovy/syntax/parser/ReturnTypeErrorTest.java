package org.codehaus.groovy.syntax.parser;

import java.io.ByteArrayInputStream;

import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Tests that void/value return mismatches can be detected.
 * @author Steve Goetze
 */
public class ReturnTypeErrorTest extends TestSupport {

    public void testInvalidValueReturnStatement() throws Exception {
    	doCompile(
   				"class zup {\n"
                + "    void foo() {\n"
                + "        return 3;"
                + "    }\n"
                + "}\n");
    }

    public void testInvalidValueReturnStatement2() throws Exception {
    	doCompile(
   				"class zup {\n"
                + "    void foo() {\n"
                + "        if (true) \n"
                + "            return \n"
                + "        else \n"
                + "            return 'Foo' \n"
                + "    }\n"
                + "}\n");
    }

    protected void doCompile(String code) throws Exception {
        try {
            loader.parseClass(new ByteArrayInputStream(code.getBytes()), getMethodName() + ".groovy");
        }
        catch(CompilationFailedException e ) {
			if (e.getCause() instanceof RuntimeParserException) {
				return;
			}
        }
        
        fail("Should have caught a RuntimeParserException");
    }

}
