/*
 * $Id$version Mar 8, 2004 2:11:00 AM $user Exp $
 * 
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
 package groovy.text;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.SyntaxException;


/**
 * @author sam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SimpleTemplateEngine extends TemplateEngine {

    /* (non-Javadoc)
     * @see groovy.util.TemplateEngine#createTemplate(java.io.Reader)
     */
    public Template createTemplate(Reader reader) throws SyntaxException, ClassNotFoundException, IOException {
        SimpleTemplate template = new SimpleTemplate();
        GroovyShell shell = new GroovyShell();
        String script = template.parse(reader);
        template.script = shell.parse(script);
        return template;
    }
        
    private static class SimpleTemplate implements Template {
        
        private Script script;
        private Binding binding;
        
        public void setBinding(Binding binding) {
            this.binding = binding;
        }
        
        public void writeTo(Writer writer) throws IOException {
            if (binding == null) binding = new Binding();
    		Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
    		scriptObject.setProperty("result", writer);
    		scriptObject.run();
        }
        
        public String toString() {
            try {
                StringWriter sw = new StringWriter();
                writeTo(sw);
                return sw.toString();
            } catch (Exception e) {
                return e.toString();
            }
        }
        
        /**
         * @param stream
         */
        private String parse(Reader reader) throws IOException {
            BufferedReader br = new BufferedReader(reader);
            StringWriter sw = new StringWriter();
            startScript(sw);
            int c;
            while((c = br.read()) != -1) {
                if (c == '<') {
                    c = br.read();
                    if (c != '%') {
                        sw.write('<');
                    } else {
                        br.mark(1);
                        c = br.read();
                        if (c == '=') {
                            groovyExpression(br, sw);
                        } else {
                            br.reset();
                            groovySection(br, sw);
                        }
                        continue;
                    }
                }
                sw.write(c);
            }
            endScript(sw);
            return sw.toString();
        }

        /**
         * @param sw
         */
        private void startScript(StringWriter sw) {
            sw.write("result.write(<<<SECTION\n");
        }

        /**
         * @param sw
         */
        private void endScript(StringWriter sw) {
            sw.write("\nSECTION);\n");
        }

        /**
         * @param br
         * @param sw
         */
        private void groovyExpression(BufferedReader br, StringWriter sw) throws IOException {
            sw.write("\nSECTION);\nresult.write(\"${");
            int c;
            while((c = br.read()) != -1) {
                if (c == '%') {
                    c = br.read();
                    if (c != '>') {
                        sw.write('%');
                    } else {
                        break;
                    }
                }
                if (c == '\"') {
                    sw.write('\\');
                }
                sw.write(c);
            }
            sw.write("}\");\nresult.write(<<<SECTION\n");
        }

        /**
         * @param br
         * @param sw
         */
        private void groovySection(BufferedReader br, StringWriter sw) throws IOException {
            sw.write("\nSECTION);\n");
            int c;
            while((c = br.read()) != -1) {
                if (c == '%') {
                    c = br.read();
                    if (c != '>') {
                        sw.write('%');
                    } else {
                        break;
                    }
                }
                sw.write(c);
            }
            sw.write("\nresult.write(<<<SECTION\n");
        }

    }

}
