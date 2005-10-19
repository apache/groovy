/* $Id$

Copyright 2004 (C) John Wilson. All Rights Reserved.

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
package groovy.text;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;
import groovy.lang.Writable;

import java.io.IOException;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;


/**
* @author tug@wilson.co.uk
*
*/
public class GStringTemplateEngine extends TemplateEngine {
    /* (non-Javadoc)
     * @see groovy.text.TemplateEngine#createTemplate(java.io.Reader)
     */
    public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new GStringTemplate(reader);
    }

    private static class GStringTemplate implements Template {
        final Closure template;

        /**
         * Turn the template into a writable Closure
         * When executed the closure evaluates all the code embedded in the
         * template and then writes a GString containing the fixed and variable items
         * to the writer passed as a paramater
         *
         * For example:
         *
         * '<%= "test" %> of expr and <% test = 1 %>${test} script.'
         *
         * would compile into:
         *
         * { |out| out << "${"test"} of expr and "; test = 1 ; out << "${test} script."}.asWritable()
         *
         * @param reader
         * @throws CompilationFailedException
         * @throws ClassNotFoundException
         * @throws IOException
         */
        public GStringTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
            final StringBuffer templateExpressions = new StringBuffer("package groovy.tmp.templates\n def getTemplate() { return { out -> out << \"\"\"");
            boolean writingString = true;
       
            while(true) {
                int c = reader.read();

                    if (c == -1) break;

                if (c == '<') {
                    c = reader.read();

                    if (c == '%') {
                        c = reader.read();

                        if (c == '=') {
                                parseExpression(reader, writingString, templateExpressions);
                                writingString = true;
                                continue;
                        } else {
                                parseSection(reader, writingString, templateExpressions);
                                writingString = false;
                                continue;
                        }
                    } else {
                        appendCharacter('<', templateExpressions, writingString);
                        writingString = true;
                    }
                } else if (c == '"') {
                        appendCharacter('\\', templateExpressions, writingString);
                        writingString = true;
                   }

                    appendCharacter((char)c, templateExpressions, writingString);
                    writingString = true;
            }

            if (writingString) {
                    templateExpressions.append("\"\"\"");
            }

            templateExpressions.append("}.asWritable()}");

//            System.out.println(templateExpressions.toString());

            final ClassLoader parentLoader = getClass().getClassLoader();
            final GroovyClassLoader loader =
                (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return new GroovyClassLoader(parentLoader);
                    }
                });
            final Class groovyClass = loader.parseClass(new GroovyCodeSource(templateExpressions.toString(), "C", "x"));

            try {
                final GroovyObject object = (GroovyObject) groovyClass.newInstance();

                this.template = (Closure)object.invokeMethod("getTemplate", null);
            } catch (InstantiationException e) {
                throw new ClassNotFoundException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new ClassNotFoundException(e.getMessage());
            }
        }

        private static void appendCharacter(final char c,
                                          final StringBuffer templateExpressions,
                                          final boolean writingString)
        {
            if (!writingString) {
                templateExpressions.append("out << \"\"\"");
            }

            templateExpressions.append(c);
        }

        /**
         * Parse a <% .... %> section
         * if we are writing a GString close and append ';'
         * then write the section as a statement
         *
         * @param reader
         * @param writingString
         * @param templateExpressions
         * @throws IOException
         */
        private static void parseSection(final Reader reader,
                                        final boolean writingString,
                                         final StringBuffer templateExpressions)
            throws IOException
        {
            if (writingString) {
                templateExpressions.append("\"\"\"; ");
            }

                while (true) {
                    int c = reader.read();

                    if (c == -1) break;

                    if (c =='%') {
                        c = reader.read();

                        if (c == '>') break;
                    }

                    templateExpressions.append((char)c);
                }

                templateExpressions.append("; ");
        }

        /**
         * Parse a <%= .... %> expression
         *
         * @param reader
         * @param writingString
         * @param templateExpressions
         * @throws IOException
         */
        private static void parseExpression(final Reader reader,
                                          final boolean writingString,
                                          final StringBuffer templateExpressions)
            throws IOException
        {
            if (!writingString) {
                templateExpressions.append("out << \"\"\"");
            }

            templateExpressions.append("${");

                while (true) {
                    int c = reader.read();

                    if (c == -1) break;

                    if (c =='%') {
                        c = reader.read();

                        if (c == '>') break;
                    }

                    templateExpressions.append((char)c);
                }

            templateExpressions.append('}');
        }

        public Writable make() {
           return make(null);
       }

       public Writable make(final Map map) {
       final Closure template = (Closure)this.template.clone();
           
           template.setDelegate(new Binding(map));
           
           return (Writable)template;
       }
    }
}
