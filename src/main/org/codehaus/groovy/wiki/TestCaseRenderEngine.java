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
        buf.append("void testDummy() {\n// this is a dummy test case\n}\n\n");
        buf.append("/*\n");
        buf.append(parts[0]);

        int count = 1;
        for (int i = 1; i < parts.length; i++ ) {
            buf.append("*/ \n\n  void testCase" + (count++) + "() {\n");
            
            buf.append(removeCloseCode(parts[i]));
        }
       
        buf.append("\n*/\n\n}\n");
        
        return buf.toString();
    }

    protected String removeCloseCode(String text) {
        return text.replaceFirst("\\{code\\}", "\n}\n\n /*");
    }

    public void render(Writer out, String content, RenderContext context) throws IOException {
        out.write(render(content, context));
    }
}
