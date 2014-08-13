/*
 * Copyright 2003-2013 the original author or authors.
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

import groovy.lang.*;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processes template source files substituting variables and expressions into
 * placeholders in a template source text to produce the desired output using a
 * streaming approach. This engine has equivalent functionality to the
 * {@link groovy.text.SimpleTemplateEngine} but creates the template using
 * writable closures making it more scalable for large templates or in streaming
 * scenarios.
 * <p>
 * Specifically this template engine can handle strings larger than 64k which
 * still causes problems for the other groovy template engines.
 * </p>
 * <p>
 * The template engine uses JSP style &lt;% %&gt; script and &lt;%= %&gt;
 * expression syntax or GString style expressions. The variable
 * '<code>out</code>' is bound to the writer that the template is being written
 * to.
 * </p>
 * Frequently, the template source will be in a file but here is a simple
 * example providing the template as a string:
 * <pre>
 * def binding = [
 *     firstname : "Grace",
 *     lastname  : "Hopper",
 *     accepted  : true,
 *     title     : 'Groovy for COBOL programmers'
 * ]
 * def engine = new groovy.text.StreamingTemplateEngine()
 * def text = '''\
 * Dear &lt;%= firstname %&gt; $lastname,
 * <p/>
 * We &lt;% if (accepted) print 'are pleased' else print 'regret' %&gt; \
 * to inform you that your paper entitled
 * '$title' was ${ accepted ? 'accepted' : 'rejected' }.
 * <p/>
 * The conference committee.
 * '''
 * def template = engine.createTemplate(text).make(binding)
 * println template.toString()
 * </pre> This example uses a mix of the JSP style and GString style
 * placeholders but you can typically use just one style if you wish. Running
 * this example will produce this output:
 * <pre>
 * Dear Grace Hopper,
 * <p/>
 * We are pleased to inform you that your paper entitled
 * 'Groovy for COBOL programmers' was accepted.
 * <p/>
 * The conference committee.
 * </pre> The template engine can also be used as the engine for
 * {@link groovy.servlet.TemplateServlet} by placing the following in your
 * <code>web.xml</code> file (plus a corresponding servlet-mapping element):
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;StreamingTemplate&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;template.engine&lt;/param-name&gt;
 *     &lt;param-value&gt;groovy.text.StreamingTemplateEngine&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * </pre> In this case, your template source file should be HTML with the
 * appropriate embedded placeholders.
 *
 * @author mbjarland@gmail.com
 * @author Matias Bjarland
 */
public class StreamingTemplateEngine extends TemplateEngine {
    private static final String TEMPLATE_SCRIPT_PREFIX = "StreamingTemplateScript";

    private final ClassLoader parentLoader;
    private static int counter = 1;

    public StreamingTemplateEngine() {
        this(StreamingTemplate.class.getClassLoader());
    }

    public StreamingTemplateEngine(ClassLoader parentLoader) {
        this.parentLoader = parentLoader;
    }

    /* (non-Javadoc)
     * @see groovy.text.TemplateEngine#createTemplate(java.io.Reader)
     */
    @Override
    public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new StreamingTemplate(reader, parentLoader);
    }

    private static class StreamingTemplate implements Template {
        private static final String SCRIPT_HEAD
                = "package groovy.tmp.templates;"
                + "def getTemplate() { "
                + //the below params are:
                //  _p - parent class, for handling exceptions
                //  _s - sections, string sections list
                //  _b - binding map
                //  out - out stream
                //the three first parameters will be curried in as we move along
                "return { _p, _s, _b, out -> "
                + "int _i = 0;"
                + "try {"
                + "delegate = new Binding(_b);";

        private static final String SCRIPT_TAIL
                = "} catch (Throwable e) { "
                + "_p.error(_i, _s, e);"
                + "}"
                + "}.asWritable()"
                + "}";

        // we use a hard index instead of incrementing the _i variable due to previous
        // bug where the increment was not executed when hitting non-executed if branch
        private int index = 0;
        final Closure template;

        String scriptSource;

        private static class FinishedReadingException extends Exception {}

        //WE USE THIS AS REUSABLE        
        //CHECKSTYLE.OFF: ConstantNameCheck - special case with a reusable exception
        private static final FinishedReadingException finishedReadingException;
        //CHECKSTYLE.ON: ConstantNameCheck

        static {
            finishedReadingException = new FinishedReadingException();
            finishedReadingException.setStackTrace(new StackTraceElement[0]);
        }

        private static final class Position {
            //CHECKSTYLE.OFF: VisibilityModifierCheck - special case, direct access for performance
            public int row;
            public int column;
            //CHECKSTYLE.ON: VisibilityModifierCheck

            private Position(int row, int column) {
                this.row = row;
                this.column = column;
            }

            private Position(Position p) {
                set(p);
            }

            private void set(Position p) {
                this.row = p.row;
                this.column = p.column;
            }
        }

        private static final class StringSection {
            StringBuilder data;
            Position firstSourcePosition;
            Position lastSourcePosition;
            Position lastTargetPosition;

            private StringSection(Position firstSourcePosition) {
                this.data = new StringBuilder();
                this.firstSourcePosition = new Position(firstSourcePosition);
            }

            @Override
            public String toString() {
                return data.toString();
            }
        }

        private void finishStringSection(List<StringSection> sections, StringSection currentSection,
                                         StringBuilder templateExpressions,
                                         Position lastSourcePosition, Position targetPosition) {
            //when we get exceptions from the parseXXX methods in the main loop, we might try to
            //re-finish a section
            if (currentSection.lastSourcePosition != null) {
                return;
            }
            currentSection.lastSourcePosition = new Position(lastSourcePosition);
            sections.add(currentSection);
            //templateExpressions.append("//lines[").append(currentSection.firstLine).append(",").append(currentSection.lastLine).append("]\n");           
            //append(templateExpressions, targetPosition, "out<<_s[_i++];");
            append(templateExpressions, targetPosition, "out<<_s[_i=" + index++ + "];");
            currentSection.lastTargetPosition = new Position(targetPosition.row, targetPosition.column);
        }

        public void error(int index, List<StringSection> sections, Throwable e) throws Throwable {
            int i = Math.max(0, index - 1);
            StringSection precedingSection = sections.get(i);
            int traceLine = -1;
            for (StackTraceElement element : e.getStackTrace()) {
                if (element.getClassName().contains(TEMPLATE_SCRIPT_PREFIX)) {
                    traceLine = element.getLineNumber();
                    break;
                }
            }

            if (traceLine != -1) {
                int actualLine = precedingSection.lastSourcePosition.row + traceLine - 1;
                throw new TemplateExecutionException("Error at line " + actualLine + ", message: " + e.getMessage(), e);
            } else {
                throw e;
            }
        }

        /**
         * Turn the template into a writable Closure When executed the closure
         * evaluates all the code embedded in the template and then writes a
         * GString containing the fixed and variable items to the writer passed
         * as a parameter
         * <p/>
         * For example:
         * <p/>
         * '<%= "test" %> of expr and <% test = 1 %>${test} script.'
         * <p/>
         * would compile into:
         * <p/>
         * { out -> out << "${"test"} of expr and "; test = 1 ; out << "${test}
         * script."}.asWritable()
         *
         * @param source
         * @param parentLoader
         * @throws CompilationFailedException
         * @throws ClassNotFoundException
         * @throws IOException
         */
        StreamingTemplate(final Reader source, final ClassLoader parentLoader) throws CompilationFailedException, ClassNotFoundException, IOException {
            final StringBuilder target = new StringBuilder();
            List<StringSection> sections = new ArrayList<StringSection>();
            Position sourcePosition = new Position(1, 1);
            Position targetPosition = new Position(1, 1);
            Position lastSourcePosition = new Position(1, 1);
            StringSection currentSection = new StringSection(sourcePosition);

            //we use the lookahead to make sure that a template file ending in say "abcdef\\"
            //will give a result of "abcdef\\" even though we have special handling for \\
            StringBuilder lookAhead = new StringBuilder(10);

            append(target, targetPosition, SCRIPT_HEAD);
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    lastSourcePosition.set(sourcePosition);
                    int c = read(source, sourcePosition, lookAhead);
                    if (c == '\\') {
                        handleEscaping(source, sourcePosition, currentSection, lookAhead);
                        continue;
                    } else if (c == '<') {
                        c = read(source, sourcePosition, lookAhead);
                        if (c == '%') {
                            c = read(source, sourcePosition);
                            clear(lookAhead);
                            if (c == '=') {
                                finishStringSection(sections, currentSection, target, lastSourcePosition, targetPosition);
                                parseExpression(source, target, sourcePosition, targetPosition);
                                currentSection = new StringSection(sourcePosition);
                                continue;
                            } else {
                                finishStringSection(sections, currentSection, target, lastSourcePosition, targetPosition);
                                parseSection(c, source, target, sourcePosition, targetPosition);
                                currentSection = new StringSection(sourcePosition);
                                continue;
                            }
                        } else {
                            currentSection.data.append('<');
                        }
                    } else if (c == '$') {
                        c = read(source, sourcePosition);
                        clear(lookAhead);
                        if (c == '{') {
                            finishStringSection(sections, currentSection, target, lastSourcePosition, targetPosition);
                            parseGString(source, target, sourcePosition, targetPosition);
                            currentSection = new StringSection(sourcePosition);
                            continue;
                        } else {
                            currentSection.data.append('$');
                        }
                    }
                    currentSection.data.append((char) c);
                    clear(lookAhead);
                }
            } catch (FinishedReadingException e) {
                if (lookAhead.length() > 0) {
                    currentSection.data.append(lookAhead);
                }
                //Ignored here, just used for exiting the read loop. Yeah I know we don't like
                //empty catch blocks or expected behavior trowing exceptions, but this just cleaned out the code
                //_so_ much that I thought it worth it...this once -Matias Bjarland 20100126
            }

            finishStringSection(sections, currentSection, target, sourcePosition, targetPosition);
            append(target, targetPosition, SCRIPT_TAIL);

            scriptSource = target.toString();

            this.template = createTemplateClosure(sections, parentLoader, target);
        }

        private static void clear(StringBuilder lookAhead) {
            lookAhead.delete(0, lookAhead.length());
        }

        private void handleEscaping(final Reader source,
                                    final Position sourcePosition,
                                    final StringSection currentSection,
                                    final StringBuilder lookAhead) throws IOException, FinishedReadingException {
            //if we get here, we just read in a back-slash from the source, now figure out what to do with it
            int c = read(source, sourcePosition, lookAhead);

            /*
             The _only_ special escaping this template engine allows is to escape the sequences:
             ${ and <% and potential slashes in front of these. Escaping in any other sections of the
             source string is ignored. The following is a source -> result mapping of a few values, assume a
             binding of [alice: 'rabbit'].

             Note: we don't do java escaping of slashes in the below
             example, i.e. the source string is what you would see in a text editor when looking at your template
             file: 
             source string     result
             'bob'            -> 'bob'
             '\bob'           -> '\bob'
             '\\bob'          -> '\\bob'
             '${alice}'       -> 'rabbit'
             '\${alice}'      -> '${alice}'
             '\\${alice}'     -> '\rabbit'
             '\\$bob'         -> '\\$bob'
             '\\'             -> '\\'
             '\\\'             -> '\\\'
             '%<= alice %>'   -> 'rabbit'
             '\%<= alice %>'  -> '%<= alice %>'
             */
            if (c == '\\') {
                //this means we have received a double backslash sequence
                //if this is followed by ${ or <% we output one backslash
                //and interpret the following sequences with groovy, if followed by anything
                //else we output the two backslashes and continue as usual 
                source.mark(3);
                int d = read(source, sourcePosition, lookAhead);
                c = read(source, sourcePosition, lookAhead);
                clear(lookAhead);
                if ((d == '$' && c == '{') ||
                    (d == '<' && c == '%')) {
                    source.reset();
                    currentSection.data.append('\\');
                    return;
                } else {
                    currentSection.data.append('\\');
                    currentSection.data.append('\\');
                    currentSection.data.append((char) d);
                }
            } else if (c == '$') {
                c = read(source, sourcePosition, lookAhead);
                if (c == '{') {
                    currentSection.data.append('$');
                } else {
                    currentSection.data.append('\\');
                    currentSection.data.append('$');
                }
            } else if (c == '<') {
                c = read(source, sourcePosition, lookAhead);
                if (c == '%') {
                    currentSection.data.append('<');
                } else {
                    currentSection.data.append('\\');
                    currentSection.data.append('<');
                }
            } else {
                currentSection.data.append('\\');
            }

            currentSection.data.append((char) c);
            clear(lookAhead);
        }

        private Closure createTemplateClosure(List<StringSection> sections, final ClassLoader parentLoader, StringBuilder target) throws ClassNotFoundException {
            final GroovyClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
                public GroovyClassLoader run() {
                    return new GroovyClassLoader(parentLoader);
                }
            });
            final Class groovyClass;
            try {
                groovyClass = loader.parseClass(new GroovyCodeSource(target.toString(), TEMPLATE_SCRIPT_PREFIX + counter++ + ".groovy", "x"));
            } catch (MultipleCompilationErrorsException e) {
                throw mangleMultipleCompilationErrorsException(e, sections);

            } catch (Exception e) {
                throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
            }

            Closure result;
            try {
                final GroovyObject object = (GroovyObject) groovyClass.newInstance();
                Closure chicken = (Closure) object.invokeMethod("getTemplate", null);
                //bind the two first parameters of the generated closure to this class and the sections list
                result = chicken.curry(new Object[]{this, sections});
            } catch (InstantiationException e) {
                throw new ClassNotFoundException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new ClassNotFoundException(e.getMessage());
            }

            return result;
        }

        private void parseGString(final Reader reader,
                                  final StringBuilder target,
                                  final Position sourcePosition,
                                  final Position targetPosition) throws IOException, FinishedReadingException {
            append(target, targetPosition, "out<<\"\"\"${");

            while (true) {
                int c = read(reader, sourcePosition);
                append(target, targetPosition, (char) c);
                if (c == '}') break;
            }

            append(target, targetPosition, "\"\"\";");
        }

        /**
         * Parse a <% .... %> section if we are writing a GString close and
         * append ';' then write the section as a statement
         *
         * @param pendingC
         * @param reader
         * @param target
         * @param sourcePosition
         * @param targetPosition
         * @return
         * @throws IOException
         * @throws
         *
         */
        private void parseSection(final int pendingC,
                final Reader reader,
                final StringBuilder target,
                final Position sourcePosition,
                final Position targetPosition) throws IOException, FinishedReadingException {
            //the below is a quirk, we do this so that every non-string-section is prefixed by
            //the same number of characters (the others have "out<<\"\"\"${"), this allows us to
            //figure out the exception row and column later on
            append(target, targetPosition, "          ");
            append(target, targetPosition, (char) pendingC);

            while (true) {
                int c = read(reader, sourcePosition);
                if (c == '%') {
                    c = read(reader, sourcePosition);
                    if (c == '>') break;
                    append(target, targetPosition, '%');
                }
                append(target, targetPosition, (char) c);
            }

            append(target, targetPosition, ';');
        }

        /**
         * Parse a <%= .... %> expression
         *
         * @param reader
         * @param target
         * @return
         * @throws IOException
         */
        private void parseExpression(final Reader reader,
                final StringBuilder target,
                final Position sourcePosition,
                final Position targetPosition) throws IOException, FinishedReadingException {
            append(target, targetPosition, "out<<\"\"\"${");

            while (true) {
                int c = read(reader, sourcePosition);
                if (c == '%') {
                    c = read(reader, sourcePosition);
                    if (c == '>') break;
                    append(target, targetPosition, '%');
                }
                append(target, targetPosition, (char) c);
            }

            append(target, targetPosition, "}\"\"\";");
        }

        @Override
        public Writable make() {
            return make(null);
        }

        @Override
        public Writable make(final Map map) {
            //we don't need a template.clone here as curry calls clone under the hood
            final Closure template = this.template.curry(new Object[]{map});
            return (Writable) template;
        }

        /*
         * Create groovy assertion style error message for template error. Example:
         *
         * Error parsing expression on line 71 column 15, message: no such property jboss for for class DUMMY
         * templatedata${jboss}templateddatatemplateddata
         *             ^------^
         *                 |
         *           syntax error
         */
        private RuntimeException mangleMultipleCompilationErrorsException(MultipleCompilationErrorsException e, List<StringSection> sections) {
            RuntimeException result = e;

            ErrorCollector collector = e.getErrorCollector();
            @SuppressWarnings({"unchecked"})
            List<Message> errors = (List<Message>) collector.getErrors();
            if (errors.size() > 0) {
                Message firstMessage = errors.get(0);
                if (firstMessage instanceof SyntaxErrorMessage) {
                    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
                    SyntaxException syntaxException = ((SyntaxErrorMessage) firstMessage).getCause();
                    Position errorPosition = new Position(syntaxException.getLine(), syntaxException.getStartColumn());

                    //find the string section which precedes the row/col of the thrown exception
                    StringSection precedingSection = findPrecedingSection(errorPosition, sections);

                    //and now use the string section to mangle the line numbers so that they refer to the
                    //appropriate line in the source template data
                    if (precedingSection != null) {
                        //if the error was thrown on the same row as where the last string section ended, fix column value
                        offsetPositionFromSection(errorPosition, precedingSection);
                        //the below being true indicates that we had an unterminated ${ or <% sequence and
                        //the column is thus meaningless, we reset it to where the %{ or <% starts to at
                        //least give the user a sporting chance
                        if (sections.get(sections.size() - 1) == precedingSection) {
                            errorPosition.column = precedingSection.lastSourcePosition.column;
                        }

                        String message = mangleExceptionMessage(e.getMessage(), errorPosition);
                        result = new GroovyRuntimeException(message);
                    }
                }
            }

            return result;
        }

        private String mangleExceptionMessage(String original, Position p) {
            String result = original;
            int index = result.indexOf("@ line ");
            if (index != -1) {
                result = result.substring(0, index);
            }

            int count = 0;
            index = 0;
            for (char c : result.toCharArray()) {
                if (c == ':') {
                    count++;
                    if (count == 2) {
                        result = result.substring(index + 2);
                        break;
                    }
                }
                index++;
            }

            return "Template parse error '" + result + "' at line " + p.row + ", column " + p.column;
        }

        private void offsetPositionFromSection(Position p, StringSection s) {
            if (p.row == s.lastTargetPosition.row) {
                //The number 8 below represents the number of characters in the header of a non-string-section such as
                //<% ... %>. A section like this is represented in the target script as:
                //out<<"""......."""
                //12345678
                p.column -= s.lastTargetPosition.column + 8;
                p.column += s.lastSourcePosition.column;
            }

            p.row += s.lastSourcePosition.row - 1;
        }

        private StringSection findPrecedingSection(Position p, List<StringSection> sections) {
            StringSection result = null;
            for (StringSection s : sections) {
                if (s.lastTargetPosition.row > p.row
                        || (s.lastTargetPosition.row == p.row && s.lastTargetPosition.column > p.column)) {
                    break;
                }
                result = s;
            }

            return result;
        }

        private void append(final StringBuilder target, Position targetPosition, char c) {
            if (c == '\n') {
                targetPosition.row++;
                targetPosition.column = 1;
            } else {
                targetPosition.column++;
            }

            target.append(c);
        }

        private void append(final StringBuilder target, Position targetPosition, String s) {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                append(target, targetPosition, s.charAt(i));
            }
        }

        private int read(final Reader reader, Position position, StringBuilder lookAhead) throws IOException, FinishedReadingException {
            int c = read(reader, position);
            lookAhead.append((char) c);
            return c;
        }

        // SEE BELOW
        boolean useLastRead = false;
        private int lastRead = -1;

        /* All \r\n sequences are treated as a single \n. By doing this we
         * produce the same output as the GStringTemplateEngine. Otherwise, some
         * of our output is on a newline when it should not be.
         *
         * Instead of using a pushback reader, we just keep a private instance
         * variable 'last'.
         */
        private int read(final Reader reader, Position position) throws IOException, FinishedReadingException {
            int c;

            if (useLastRead) {
                // use last one if we stored a character
                c = lastRead;
                // reset last
                useLastRead = false;
                lastRead = -1;
            } else {
                c = reader.read();
                if (c == '\r') {
                    // IF CR FOLLOWED BY NEWLINE THEN JUST KEEP NEWLINE
                    c = reader.read();
                    if (c != '\n') {
                        // ELSE keep original char
                        // and pushback the one we just read
                        lastRead = c;
                        useLastRead = true;
                        c = '\r';
                    }
                }
            }

            if (c == -1) {
                throw finishedReadingException;
            }

            if (c == '\n') {
                position.row++;
                position.column = 1;
            } else {
                position.column++;
            }

            return c;
        }
    }
}
