/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.text;

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
        final StringBuffer templateExpressions = new StringBuffer("package groovy.tmp.templates\n def getTemplate() { return { out -> delegate = new Binding(delegate); out << \"\"\"");
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
                                parseSection(c, reader, writingString, templateExpressions);
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
         * @param pendingC
         * @param reader
         * @param writingString
         * @param templateExpressions
         * @throws IOException
         */
        private static void parseSection(final int pendingC,
                                         final Reader reader,
                                         final boolean writingString,
                                         final StringBuffer templateExpressions)
            throws IOException
        {
            if (writingString) {
                templateExpressions.append("\"\"\"; ");
            }
            templateExpressions.append((char)pendingC);

                while (true) {
                    int c = reader.read();

                    if (c == -1) break;

                    if (c =='%') {
                        c = reader.read();

                        if (c == '>') break;
                        
                        templateExpressions.append('%');
                    }

                    templateExpressions.append((char)c);
                }

                templateExpressions.append(";\n ");
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
                        
                        templateExpressions.append('%');
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
           
           template.setDelegate(map);
           
           return (Writable)template;
       }
    }
}
