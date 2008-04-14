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

package groovy.bugs;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.classgen.TestSupport;

/**
 * @author Sean Timm
 * @version $Revision$
 */
public class SeansBug extends TestSupport {

    public void testBug() throws Exception {
        String code = "for (i in 1..10) \n{\n  println(i)\n}";
        GroovyShell shell = new GroovyShell();
        shell.evaluate(code);
    }

    public void testMarkupBug() throws Exception {
        String[] lines =
                {
                        "package groovy.xml",
                        "",
                        "b = new MarkupBuilder()",
                        "",
                        "b.root1(a:5, b:7) { ",
                        "    elem1('hello1') ",
                        "    elem2('hello2') ",
                        "    elem3(x:7) ",
                        "}"};
        String code = asCode(lines);
        GroovyShell shell = new GroovyShell();
        shell.evaluate(code);
    }

    /**
     * Converts the array of lines of text into one string with newlines
     */
    protected String asCode(String[] lines) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            buffer.append(lines[i]);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
