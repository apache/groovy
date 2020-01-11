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
package groovy.text;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Writable;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Processes template source files substituting variables and expressions into
 * placeholders in a template source text to produce the desired output using
 * a streaming approach. This engine has equivalent functionality to the
 * {@link SimpleTemplateEngine} but creates the template using writable
 * closures making it potentially more scalable for large templates or in streaming scenarios.
 * <p>
 * The template engine uses JSP style &lt;% %&gt; script and &lt;%= %&gt; expression syntax
 * or GString style expressions. The variable '<code>out</code>' is bound to the writer that the template
 * is being written to.
 * <p>
 * Frequently, the template source will be in a file but here is a simple
 * example providing the template as a string:
 * <pre>
 * def binding = [
 *     firstname : "Grace",
 *     lastname  : "Hopper",
 *     accepted  : true,
 *     title     : 'Groovy for COBOL programmers'
 * ]
 * def engine = new groovy.text.GStringTemplateEngine()
 * def text = '''\
 * Dear &lt;%= firstname %&gt; $lastname,
 *
 * We &lt;% if (accepted) print 'are pleased' else print 'regret' %&gt; \
 * to inform you that your paper entitled
 * '$title' was ${ accepted ? 'accepted' : 'rejected' }.
 *
 * The conference committee.
 * '''
 * def template = engine.createTemplate(text).make(binding)
 * println template.toString()
 * </pre>
 * This example uses a mix of the JSP style and GString style placeholders
 * but you can typically use just one style if you wish. Running this
 * example will produce this output:
 * <pre>
 * Dear Grace Hopper,
 *
 * We are pleased to inform you that your paper entitled
 * 'Groovy for COBOL programmers' was accepted.
 *
 * The conference committee.
 * </pre>
 * The template engine can also be used as the engine for {@link groovy.servlet.TemplateServlet} by placing the
 * following in your <code>web.xml</code> file (plus a corresponding servlet-mapping element):
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;GStringTemplate&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;template.engine&lt;/param-name&gt;
 *     &lt;param-value&gt;groovy.text.GStringTemplateEngine&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * </pre>
 * In this case, your template source file should be HTML with the appropriate embedded placeholders.
 */
public class GStringTemplateEngine extends TemplateEngine {
    private final ClassLoader parentLoader;
    private static AtomicInteger counter = new AtomicInteger();
    private static final boolean reuseClassLoader = SystemUtil.getBooleanSafe("groovy.GStringTemplateEngine.reuseClassLoader");

    public GStringTemplateEngine() {
        this(GStringTemplate.class.getClassLoader());
    }

    public GStringTemplateEngine(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
    }

    /* (non-Javadoc)
    * @see groovy.text.TemplateEngine#createTemplate(java.io.Reader)
    */
    public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new GStringTemplate(reader, parentLoader);
    }

    private static class GStringTemplate implements Template {
        final Closure template;

        /**
         * Turn the template into a writable Closure
         * When executed the closure evaluates all the code embedded in the
         * template and then writes a GString containing the fixed and variable items
         * to the writer passed as a parameter
         * <p>
         * For example:
         * <pre>
         * '<%= "test" %> of expr and <% test = 1 %>${test} script.'
         * </pre>
         * <p>
         * would compile into:
         * <pre>
         * { out -> out << "${"test"} of expr and "; test = 1 ; out << "${test} script."}.asWritable()
         * </pre>
         *
         * @param reader
         * @param parentLoader
         * @throws CompilationFailedException
         * @throws ClassNotFoundException
         * @throws IOException
         */
        GStringTemplate(final Reader reader, final ClassLoader parentLoader) throws CompilationFailedException, ClassNotFoundException, IOException {
            final StringBuilder templateExpressions = new StringBuilder("package groovy.tmp.templates\n def getTemplate() { return { out -> out << \"\"\"");
            boolean writingString = true;

            while (true) {
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
                } else if (c == '$') {
                    appendCharacter('$', templateExpressions, writingString);
                    writingString = true;
                    c = reader.read();
                    if (c == '{') {
                        appendCharacter('{', templateExpressions, writingString);
                        writingString = true;
                        parseGSstring(reader, writingString, templateExpressions);
                        writingString = true;
                        continue;
                    }
                }
                appendCharacter((char) c, templateExpressions, writingString);
                writingString = true;
            }

            if (writingString) {
                templateExpressions.append("\"\"\"");
            }

            templateExpressions.append("}}");

            // Use a new class loader by default for each class so each class can be independently garbage collected
            final GroovyClassLoader loader =
                    reuseClassLoader && parentLoader instanceof GroovyClassLoader
                            ? (GroovyClassLoader) parentLoader
                            : AccessController.doPrivileged((PrivilegedAction<GroovyClassLoader>) () -> new GroovyClassLoader(parentLoader));
            final Class groovyClass;
            try {
                groovyClass = loader.parseClass(new GroovyCodeSource(templateExpressions.toString(), "GStringTemplateScript" + counter.incrementAndGet() + ".groovy", "x"));
            } catch (Exception e) {
                throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
            }

            try {
                final GroovyObject script = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();

                this.template = (Closure) script.invokeMethod("getTemplate", null);
                // GROOVY-6521: must set strategy to DELEGATE_FIRST, otherwise writing
                // books = 'foo' in a template would store 'books' in the binding of the template script itself ("script")
                // instead of storing it in the delegate, which is a Binding too
                this.template.setResolveStrategy(Closure.DELEGATE_FIRST);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new ClassNotFoundException(e.getMessage());
            }
        }

        private static void appendCharacter(final char c,
                                            final StringBuilder templateExpressions,
                                            final boolean writingString) {
            if (!writingString) {
                templateExpressions.append("out << \"\"\"");
            }
            templateExpressions.append(c);
        }

        private void parseGSstring(Reader reader, boolean writingString, StringBuilder templateExpressions) throws IOException {
            if (!writingString) {
                templateExpressions.append("\"\"\"; ");
            }
            while (true) {
                int c = reader.read();
                if (c == -1) break;
                templateExpressions.append((char) c);
                if (c == '}') {
                    break;
                }
            }
        }

        /**
         * Parse a &lt;% .... %&gt; section
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
                                         final StringBuilder templateExpressions)
                throws IOException {
            if (writingString) {
                templateExpressions.append("\"\"\"; ");
            }
            templateExpressions.append((char) pendingC);

            readAndAppend(reader, templateExpressions);

            templateExpressions.append(";\n ");
        }

        private static void readAndAppend(Reader reader, StringBuilder templateExpressions) throws IOException {
            while (true) {
                int c = reader.read();
                if (c == -1) break;
                if (c == '%') {
                    c = reader.read();
                    if (c == '>') break;
                    templateExpressions.append('%');
                }
                templateExpressions.append((char) c);
            }
        }

        /**
         * Parse a &lt;%= .... %&gt; expression
         *
         * @param reader
         * @param writingString
         * @param templateExpressions
         * @throws IOException
         */
        private static void parseExpression(final Reader reader,
                                            final boolean writingString,
                                            final StringBuilder templateExpressions)
                throws IOException {
            if (!writingString) {
                templateExpressions.append("out << \"\"\"");
            }

            templateExpressions.append("${");

            readAndAppend(reader, templateExpressions);

            templateExpressions.append('}');
        }

        public Writable make() {
            return make(null);
        }

        public Writable make(final Map map) {
            final Closure template = ((Closure) this.template.clone()).asWritable();
            Binding binding = new Binding(map);
            template.setDelegate(binding);
            return (Writable) template;
        }
    }
}
