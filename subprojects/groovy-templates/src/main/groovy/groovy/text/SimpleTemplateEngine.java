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
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Processes template source files substituting variables and expressions into
 * placeholders in a template source text to produce the desired output.
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
 * def engine = new groovy.text.SimpleTemplateEngine()
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
 * The template engine can also be used as the engine for {@code groovy.servlet.TemplateServlet} by placing the
 * following in your <code>web.xml</code> file (plus a corresponding servlet-mapping element):
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;SimpleTemplate&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;template.engine&lt;/param-name&gt;
 *     &lt;param-value&gt;groovy.text.SimpleTemplateEngine&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * </pre>
 * In this case, your template source file should be HTML with the appropriate embedded placeholders.
 */
public class SimpleTemplateEngine extends TemplateEngine {
    private boolean verbose;
    private static AtomicInteger counter = new AtomicInteger(0);
    private GroovyShell groovyShell;
    private boolean escapeBackslash;

    /**
     * Creates an engine that parses template scripts with the default Groovy class loader.
     */
    public SimpleTemplateEngine() {
        this(GroovyShell.class.getClassLoader());
    }

    /**
     * Creates an engine with optional debug output enabled.
     *
     * @param verbose {@code true} to print generated script source while compiling templates
     */
    public SimpleTemplateEngine(boolean verbose) {
        this(GroovyShell.class.getClassLoader());
        setVerbose(verbose);
    }

    /**
     * Creates an engine that uses a {@link GroovyShell} built from the supplied parent loader.
     *
     * @param parentLoader class loader used to compile generated template scripts
     */
    public SimpleTemplateEngine(ClassLoader parentLoader) {
        this(new GroovyShell(parentLoader));
    }

    /**
     * Creates an engine backed by the supplied {@link GroovyShell}.
     *
     * @param groovyShell shell used to compile generated template scripts
     */
    public SimpleTemplateEngine(GroovyShell groovyShell) {
        this.groovyShell = groovyShell;
    }

    /**
     * Compiles template source from the supplied reader.
     *
     * @param reader template source
     * @return a compiled template instance
     * @throws CompilationFailedException if Groovy fails to compile the generated template script
     * @throws IOException if reading the template source fails
     */
    @Override
    public Template createTemplate(Reader reader) throws CompilationFailedException, IOException {
        SimpleTemplate template = new SimpleTemplate(escapeBackslash);
        String script = template.parse(reader);
        if (verbose) {
            System.out.println("\n-- script source --");
            System.out.print(script);
            System.out.println("\n-- script end --\n");
        }
        try {
            template.script = groovyShell.parse(script, "SimpleTemplateScript" + counter.incrementAndGet() + ".groovy");
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
        }
        return template;
    }

    /**
     * @param verbose true if you want the engine to display the template source file for debugging purposes
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Indicates whether generated template scripts are printed during compilation.
     *
     * @return {@code true} if debug output is enabled
     */
    public boolean isVerbose() {
        return verbose;
    }

    private static class SimpleTemplate implements Template {

        /**
         * Parsed Groovy script backing this template instance.
         */
        protected Script script;
        private boolean escapeBackslash;

        SimpleTemplate() {
            this(false);
        }

        SimpleTemplate(boolean escapeBackslash) {
            this.escapeBackslash = escapeBackslash;
        }


        /**
         * Creates a writable template view using an empty binding.
         *
         * @return a writable template instance
         */
        @Override
        public Writable make() {
            return make(null);
        }

        /**
         * Creates a writable template view using the supplied binding.
         *
         * @param map binding values made available to the template
         * @return a writable template instance
         */
        @Override
        public Writable make(final Map map) {
            return new Writable() {
                /**
                 * Write the template document with the set binding applied to the writer.
                 *
                 * @see groovy.lang.Writable#writeTo(java.io.Writer)
                 */
                @Override
                public Writer writeTo(Writer writer) {
                    Binding binding;
                    if (map == null)
                        binding = new Binding();
                    else
                        binding = new Binding(map);
                    Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
                    PrintWriter pw = new PrintWriter(writer);
                    scriptObject.setProperty("out", pw);
                    scriptObject.run();
                    pw.flush();
                    return writer;
                }

                /**
                 * Convert the template and binding into a result String.
                 *
                 * @see java.lang.Object#toString()
                 */
                @Override
                public String toString() {
                    Writer sw = new StringBuilderWriter();
                    writeTo(sw);
                    return sw.toString();
                }
            };
        }

        /**
         * Parse the text document looking for &lt;% or &lt;%= and then call out to the appropriate handler, otherwise copy the text directly
         * into the script while escaping quotes.
         *
         * @param reader a reader for the template text
         * @return the parsed text
         * @throws IOException if something goes wrong
         */
        protected String parse(Reader reader) throws IOException {
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader);
            }
            StringBuilderWriter sw = new StringBuilderWriter();
            startScript(sw);
            int c;
            while ((c = reader.read()) != -1) {
                if (c == '<') {
                    reader.mark(1);
                    c = reader.read();
                    if (c != '%') {
                        sw.write('<');
                        reader.reset();
                    } else {
                        reader.mark(1);
                        c = reader.read();
                        if (c == '=') {
                            groovyExpression(reader, sw);
                        } else {
                            reader.reset();
                            groovySection(reader, sw);
                        }
                    }
                    continue; // at least '<' is consumed ... read next chars.
                }
                if (c == '$') {
                    reader.mark(1);
                    c = reader.read();
                    if (c != '{') {
                        sw.write('$');
                        reader.reset();
                    } else {
                        reader.mark(1);
                        sw.write("${");
                        processGSstring(reader, sw);
                    }
                    continue; // at least '$' is consumed ... read next chars.
                }
                if (c == '\"') {
                    sw.write('\\');
                }

                /*
                 *  GROOVY-4585
                 *  Handle backslash characters.
                 */
                if (escapeBackslash) {
                    if (c == '\\') {
                        reader.mark(1);
                        c = reader.read();
                        if (c != '$') {
                            sw.write("\\\\");
                            reader.reset();
                        } else {
                            sw.write("\\$");
                        }

                        continue;
                    }
                }
                /*
                 * Handle raw new line characters.
                 */
                if (c == '\n' || c == '\r') {
                    if (c == '\r') { // on Windows, "\r\n" is a new line.
                        reader.mark(1);
                        c = reader.read();
                        if (c != '\n') {
                            reader.reset();
                        }
                    }
                    sw.write("\n");
                    continue;
                }
                sw.write(c);
            }
            endScript(sw);
            return sw.toString();
        }

        private void startScript(StringBuilderWriter sw) {
            sw.write("out.print(\"\"\"");
        }

        private void endScript(StringBuilderWriter sw) {
            sw.write("\"\"\");\n");
            sw.write("\n/* Generated by SimpleTemplateEngine */");
        }

        private void processGSstring(Reader reader, StringBuilderWriter sw) throws IOException {
            int c;
            while ((c = reader.read()) != -1) {
                if (c != '\n' && c != '\r') {
                    sw.write(c);
                }
                if (c == '}') {
                    break;
                }
            }
        }

        /**
         * Closes the currently open write and writes out the following text as a GString expression until it reaches an end %>.
         *
         * @param reader a reader for the template text
         * @param sw     a StringBuilderWriter to write expression content
         * @throws IOException if something goes wrong
         */
        private void groovyExpression(Reader reader, StringBuilderWriter sw) throws IOException {
            sw.write("${");
            int c;
            while ((c = reader.read()) != -1) {
                if (c == '%') {
                    c = reader.read();
                    if (c != '>') {
                        sw.write('%');
                    } else {
                        break;
                    }
                }
                if (c != '\n' && c != '\r') {
                    sw.write(c);
                }
            }
            sw.write("}");
        }

        /**
         * Closes the currently open write and writes the following text as normal Groovy script code until it reaches an end %>.
         *
         * @param reader a reader for the template text
         * @param sw     a StringBuilderWriter to write expression content
         * @throws IOException if something goes wrong
         */
        private void groovySection(Reader reader, StringBuilderWriter sw) throws IOException {
            sw.write("\"\"\");");
            int c;
            while ((c = reader.read()) != -1) {
                if (c == '%') {
                    c = reader.read();
                    if (c != '>') {
                        sw.write('%');
                    } else {
                        break;
                    }
                }
                /* Don't eat EOL chars in sections - as they are valid instruction separators.
                 * See https://issues.apache.org/jira/browse/GROOVY-980
                 */
                // if (c != '\n' && c != '\r') {
                sw.write(c);
                //}
            }
            sw.write(";\nout.print(\"\"\"");
        }
    }

    /**
     * Indicates whether backslashes are escaped while parsing template source.
     *
     * @return {@code true} if backslash escaping is enabled
     */
    public boolean isEscapeBackslash() {
        return escapeBackslash;
    }

    /**
     * Controls whether backslashes in template source should be preserved using the legacy GROOVY-4585 behavior.
     *
     * @param escapeBackslash {@code true} to escape backslashes while parsing the template
     */
    public void setEscapeBackslash(boolean escapeBackslash) {
        this.escapeBackslash = escapeBackslash;
    }
}
