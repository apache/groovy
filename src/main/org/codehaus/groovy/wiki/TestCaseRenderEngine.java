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
package org.codehaus.groovy.wiki;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;

/**
 * @author James Strachan
 * @version $Revision$
 */
public class TestCaseRenderEngine implements RenderEngine {
    Pattern groovyCodePattern = Pattern.compile("\\{code:groovy\\}");
    Pattern groovyShellPattern = Pattern.compile("\\{code:groovysh\\}");
    Pattern codePattern = Pattern.compile("\\{code\\}");

    public TestCaseRenderEngine() {
    }

    public String getName() {
        return "TestCase";
    }

    public String render(String content, RenderContext context) {
        String name = (String) context.get("name");
        if (name == null) {
            name = "UknownName.wiki";
        }
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            name = name.substring(0, idx);
        }
        name = name + "Test";

        // lets replace {code:groovy} with a unit test case method name
        StringBuffer buf = new StringBuffer();

        String[] parts = groovyCodePattern.split(content);

        buf.append( "package wiki\nclass " + name + " extends GroovyTestCase {\n\n");
        buf.append("/*\n");
        buf.append(processShellScripts(parts[0]));

        for (int count = 1; count < parts.length; count++ ) {
            buf.append("*/ \n\n  void testCase" + count + "() {\n");

            buf.append(processShellScripts(removeCloseCode(parts[count])));
        }

        buf.append("\n*/\n\n");
        buf.append("void testDummy() {\n// this is a dummy test case\n}\n\n}\n");

        return buf.toString();
    }

    /**
     * Splits the comment block extracting any scripts that need to be tested
     * @param text
     * @return
     */
    protected String processShellScripts(String text) {
        StringBuffer buf = new StringBuffer();

        String[] parts = groovyShellPattern.split(text);

        buf.append(parts[0]);

        for (int count = 1; count < parts.length; count++ ) {
            buf.append("*/ \n\n  void testScript" + count + "() {\n");
            buf.append("    assertScript( <<<SCRIPT_EOF" + count + "\n");

            String code = parts[count].replaceFirst("\\{code\\}", "\nSCRIPT_EOF" + count + " )\n}    \n\n /*");

            // lets escape ${foo} expressions
            StringBuffer temp = new StringBuffer(code);
            for (int idx = 0; true; idx++) {
                idx = temp.indexOf("$", idx);
                if (idx >= 0) {
                    String next = temp.substring(++idx, idx+1);
                    if (next.equals("{")) {

                        //
                        // It's a hack, but we aren't escaping all \, so
                        // we just let \${ stand...

                        if( idx-2 >= 0 && !temp.substring(idx-2,idx-1).equals("\\") )
                        {
                            temp.insert(idx-1, "\\");
                            idx++;
                        }
                    }
                }
                else {
                    break;
                }
            }

            buf.append(temp.toString());
        }
        return buf.toString();
    }

    protected String removeCloseCode(String text) {
        return text.replaceFirst("\\{code\\}", "\n}\n\n /*");
    }

    public void render(Writer out, String content, RenderContext context) throws IOException {
        out.write(render(content, context));
    }
}
