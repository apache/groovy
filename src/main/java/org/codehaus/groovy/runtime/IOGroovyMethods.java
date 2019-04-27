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
package org.codehaus.groovy.runtime;

import groovy.io.GroovyPrintWriter;
import groovy.lang.Closure;
import groovy.lang.StringWriterIOException;
import groovy.lang.Writable;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.FromString;
import groovy.transform.stc.PickFirstResolver;
import groovy.transform.stc.SimpleType;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.codehaus.groovy.ast.tools.ClosureUtils.hasSingleStringArg;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.callClosureForLine;

/**
 * This class defines new groovy methods for Files, URLs, URIs which appear
 * on normal JDK classes inside the Groovy environment.
 * Static methods are used with the first parameter being the destination class,
 * i.e. <code>public static long size(File self)</code>
 * provides a <code>size()</code> method for <code>File</code>.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 */
public class IOGroovyMethods extends DefaultGroovyMethodsSupport {

    private static final Logger LOG = Logger.getLogger(IOGroovyMethods.class.getName());

    /**
     * Overloads the leftShift operator for Writer to allow an object to be written
     * using Groovy's default representation for the object.
     *
     * @param self  a Writer
     * @param value an Object whose default representation will be written to the Writer
     * @return the writer on which this operation was invoked
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    public static Writer leftShift(Writer self, Object value) throws IOException {
        InvokerHelper.write(self, value);
        return self;
    }

    /**
     * Overloads the leftShift operator for Appendable to allow an object to be appended
     * using Groovy's default representation for the object.
     *
     * @param self  an Appendable
     * @param value an Object whose default representation will be appended to the Appendable
     * @return the Appendable on which this operation was invoked
     * @throws IOException if an I/O error occurs.
     * @since 2.1.0
     */
    public static Appendable leftShift(Appendable self, Object value) throws IOException {
        InvokerHelper.append(self, value);
        return self;
    }

    /**
     * Invokes a Closure that uses a Formatter taking care of resource handling.
     * A Formatter is created and passed to the Closure as its argument.
     * After the Closure executes, the Formatter is flushed and closed releasing any
     * associated resources.
     *
     * @param self    an Appendable
     * @param closure a 1-arg Closure which will be called with a Formatter as its argument
     * @return the Appendable on which this operation was invoked
     * @since 2.1.0
     */
    public static Appendable withFormatter(Appendable self, @ClosureParams(value=SimpleType.class, options="java.util.Formatter") Closure closure) {
        Formatter formatter = new Formatter(self);
        callWithFormatter(closure, formatter);
        return self;
    }

    /**
     * Invokes a Closure that uses a Formatter taking care of resource handling.
     * A Formatter is created using the given Locale and passed to the Closure as its argument.
     * After the Closure executes, the Formatter is flushed and closed releasing any
     * associated resources.
     *
     * @param self    an Appendable
     * @param locale  a Locale used when creating the Formatter
     * @param closure a 1-arg Closure which will be called with a Formatter as its argument
     * @return the Appendable on which this operation was invoked
     * @since 2.1.0
     */
    public static Appendable withFormatter(Appendable self, Locale locale, @ClosureParams(value=SimpleType.class, options="java.util.Formatter") Closure closure) {
        Formatter formatter = new Formatter(self, locale);
        callWithFormatter(closure, formatter);
        return self;
    }

    private static void callWithFormatter(Closure closure, Formatter formatter) {
        try {
            closure.call(formatter);
        } finally {
            formatter.flush();
            formatter.close();
        }
    }

    /**
     * A helper method so that dynamic dispatch of the writer.write(object) method
     * will always use the more efficient Writable.writeTo(writer) mechanism if the
     * object implements the Writable interface.
     *
     * @param self     a Writer
     * @param writable an object implementing the Writable interface
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    public static void write(Writer self, Writable writable) throws IOException {
        writable.writeTo(self);
    }

    /**
     * Overloads the leftShift operator to provide an append mechanism to add values to a stream.
     *
     * @param self  an OutputStream
     * @param value a value to append
     * @return a Writer
     * @throws java.io.IOException if an I/O error occurs.
     * @since 1.0
     */

    public static Writer leftShift(OutputStream self, Object value) throws IOException {
        OutputStreamWriter writer = new FlushingStreamWriter(self);
        leftShift(writer, value);
        return writer;
    }

    /**
     * Overloads the leftShift operator to add objects to an ObjectOutputStream.
     *
     * @param self  an ObjectOutputStream
     * @param value an object to write to the stream
     * @throws IOException if an I/O error occurs.
     * @since 1.5.0
     */
    public static void leftShift(ObjectOutputStream self, Object value) throws IOException {
        self.writeObject(value);
    }

    /**
     * Pipe an InputStream into an OutputStream for efficient stream copying.
     *
     * @param self stream on which to write
     * @param in   stream to read from
     * @return the outputstream itself
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    public static OutputStream leftShift(OutputStream self, InputStream in) throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        for (int count; -1 != (count = in.read(buf)); ) {
            self.write(buf, 0, count);
        }
        self.flush();
        return self;
    }

    /**
     * Overloads the leftShift operator to provide an append mechanism to add bytes to a stream.
     *
     * @param self  an OutputStream
     * @param value a value to append
     * @return an OutputStream
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    public static OutputStream leftShift(OutputStream self, byte[] value) throws IOException {
        self.write(value);
        self.flush();
        return self;
    }

    /**
     * Create an object output stream for this output stream.
     *
     * @param outputStream an output stream
     * @return an object output stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectOutputStream newObjectOutputStream(OutputStream outputStream) throws IOException {
        return new ObjectOutputStream(outputStream);
    }

    /**
     * Create a new ObjectOutputStream for this output stream and then pass it to the
     * closure.  This method ensures the stream is closed after the closure
     * returns.
     *
     * @param outputStream am output stream
     * @param closure      a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectOutputStream(OutputStream outputStream, @ClosureParams(value=SimpleType.class, options="java.io.ObjectOutputStream") Closure<T> closure) throws IOException {
        return withStream(newObjectOutputStream(outputStream), closure);
    }

    /**
     * Create an object input stream for this input stream.
     *
     * @param inputStream an input stream
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectInputStream newObjectInputStream(InputStream inputStream) throws IOException {
        return new ObjectInputStream(inputStream);
    }

    /**
     * Create an object input stream for this input stream using the given class loader.
     *
     * @param inputStream an input stream
     * @param classLoader the class loader to use when loading the class
     * @return an object input stream
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static ObjectInputStream newObjectInputStream(InputStream inputStream, final ClassLoader classLoader) throws IOException {
        return new ObjectInputStream(inputStream) {
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), true, classLoader);

            }
        };
    }

    /**
     * Iterates through the given object stream object by object. The
     * ObjectInputStream is closed afterwards.
     *
     * @param ois     an ObjectInputStream, closed after the operation
     * @param closure a closure
     * @throws IOException            if an IOException occurs.
     * @throws ClassNotFoundException if the class  is not found.
     * @since 1.0
     */
    public static void eachObject(ObjectInputStream ois, Closure closure) throws IOException, ClassNotFoundException {
        try {
            while (true) {
                try {
                    Object obj = ois.readObject();
                    // we allow null objects in the object stream
                    closure.call(obj);
                } catch (EOFException e) {
                    break;
                }
            }
            InputStream temp = ois;
            ois = null;
            temp.close();
        } finally {
            closeWithWarning(ois);
        }
    }

    /**
     * Create a new ObjectInputStream for this file and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param inputStream an input stream
     * @param closure     a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectInputStream(InputStream inputStream, @ClosureParams(value=SimpleType.class, options="java.io.ObjectInputStream") Closure<T> closure) throws IOException {
        return withStream(newObjectInputStream(inputStream), closure);
    }

    /**
     * Create a new ObjectInputStream for this file and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param inputStream an input stream
     * @param classLoader the class loader to use when loading the class
     * @param closure     a closure
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 1.5.0
     */
    public static <T> T withObjectInputStream(InputStream inputStream, ClassLoader classLoader, @ClosureParams(value=SimpleType.class, options="java.io.ObjectInputStream") Closure<T> closure) throws IOException {
        return withStream(newObjectInputStream(inputStream, classLoader), closure);
    }

    /**
     * Iterates through this stream reading with the provided charset, passing each line to the
     * given 1 or 2 arg closure.  The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param charset opens the stream with a specified charset
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.InputStream, java.lang.String, int, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T eachLine(InputStream stream, String charset, @ClosureParams(value=FromString.class,options={"String","String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(stream, charset, 1, closure);
    }

    /**
     * Iterates through this stream reading with the provided charset, passing each line to
     * the given 1 or 2 arg closure.  The stream is closed after this method returns.
     *
     * @param stream    a stream
     * @param charset   opens the stream with a specified charset
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.7
     */
    public static <T> T eachLine(InputStream stream, String charset, int firstLine, @ClosureParams(value=FromString.class,options={"String","String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(new InputStreamReader(stream, charset), firstLine, closure);
    }

    /**
     * Iterates through this stream, passing each line to the given 1 or 2 arg closure.
     * The stream is closed before this method returns.
     *
     * @param stream  a stream
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.InputStream, int, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static <T> T eachLine(InputStream stream, @ClosureParams(value=FromString.class,options={"String","String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(stream, 1, closure);
    }

    /**
     * Iterates through this stream, passing each line to the given 1 or 2 arg closure.
     * The stream is closed before this method returns.
     *
     * @param stream    a stream
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.7
     */
    public static <T> T eachLine(InputStream stream, int firstLine, @ClosureParams(value=FromString.class,options={"String","String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(new InputStreamReader(stream), firstLine, closure);
    }

    /**
     * Iterates through the given reader line by line.  Each line is passed to the
     * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
     * as the second argument. The Reader is closed before this method returns.
     *
     * @param self    a Reader, closed after the method returns
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static <T> T eachLine(Reader self, @ClosureParams(value=FromString.class,options={"String","String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(self, 1, closure);
    }

    /**
     * Iterates through the given reader line by line.  Each line is passed to the
     * given 1 or 2 arg closure. If the closure has two arguments, the line count is passed
     * as the second argument. The Reader is closed before this method returns.
     *
     * @param self      a Reader, closed after the method returns
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure which will be passed each line (or for 2 arg closures the line and line count)
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.7
     */
    public static <T> T eachLine(Reader self, int firstLine, @ClosureParams(value=FromString.class,options={"String","String,Integer"}) Closure<T> closure) throws IOException {
        BufferedReader br;
        int count = firstLine;
        T result = null;

        if (self instanceof BufferedReader)
            br = (BufferedReader) self;
        else
            br = new BufferedReader(self);

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                } else {
                    result = callClosureForLine(closure, line, count);
                    count++;
                }
            }
            Reader temp = self;
            self = null;
            temp.close();
            return result;
        } finally {
            closeWithWarning(self);
            closeWithWarning(br);
        }
    }

    /**
     * Iterates through the given reader line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.  The Reader is closed afterwards.
     * <p>
     * Here is an example:
     * <pre>
     * def s = 'The 3 quick\nbrown 4 fox'
     * def result = ''
     * new StringReader(s).splitEachLine(/\d/){ parts {@code ->}
     *     result += "${parts[0]}_${parts[1]}|"
     * }
     * assert result == 'The _ quick|brown _ fox|'
     * </pre>
     *
     * @param self    a Reader, closed after the method returns
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException
     *                     if the regular expression's syntax is invalid
     * @see java.lang.String#split(java.lang.String)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(Reader self, String regex, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) Closure<T> closure) throws IOException {
        return splitEachLine(self, Pattern.compile(regex), closure);
    }

    /**
     * Iterates through the given reader line by line, splitting each line using
     * the given regex separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.  The Reader is closed afterwards.
     * <p>
     * Here is an example:
     * <pre>
     * def s = 'The 3 quick\nbrown 4 fox'
     * def result = ''
     * new StringReader(s).splitEachLine(~/\d/){ parts {@code ->}
     *     result += "${parts[0]}_${parts[1]}|"
     * }
     * assert result == 'The _ quick|brown _ fox|'
     * </pre>
     *
     * @param self    a Reader, closed after the method returns
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException
     *                     if the regular expression's syntax is invalid
     * @see java.lang.String#split(java.lang.String)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(Reader self, Pattern pattern, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) Closure<T> closure) throws IOException {
        BufferedReader br;
        T result = null;

        if (self instanceof BufferedReader)
            br = (BufferedReader) self;
        else
            br = new BufferedReader(self);

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                } else {
                    List vals = Arrays.asList(pattern.split(line));
                    result = closure.call(hasSingleStringArg(closure) ? vals.get(0) : vals);
                }
            }
            Reader temp = self;
            self = null;
            temp.close();
            return result;
        } finally {
            closeWithWarning(self);
            closeWithWarning(br);
        }
    }

    /**
     * Iterates through the given InputStream line by line using the specified
     * encoding, splitting each line using the given separator.  The list of tokens
     * for each line is then passed to the given closure. Finally, the stream
     * is closed.
     *
     * @param stream  an InputStream
     * @param regex   the delimiting regular expression
     * @param charset opens the stream with a specified charset
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException
     *                     if the regular expression's syntax is invalid
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(InputStream stream, String regex, String charset, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream, charset)), regex, closure);
    }

    /**
     * Iterates through the given InputStream line by line using the specified
     * encoding, splitting each line using the given separator Pattern.  The list of tokens
     * for each line is then passed to the given closure. Finally, the stream
     * is closed.
     *
     * @param stream  an InputStream
     * @param pattern the regular expression Pattern for the delimiter
     * @param charset opens the stream with a specified charset
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(InputStream stream, Pattern pattern, String charset, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream, charset)), pattern, closure);
    }

    /**
     * Iterates through the given InputStream line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure. The stream is closed before the method returns.
     *
     * @param stream  an InputStream
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException
     *                     if the regular expression's syntax is invalid
     * @see #splitEachLine(java.io.Reader, java.lang.String, groovy.lang.Closure)
     * @since 1.5.6
     */
    public static <T> T splitEachLine(InputStream stream, String regex, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream)), regex, closure);
    }

    /**
     * Iterates through the given InputStream line by line, splitting each line using
     * the given separator Pattern.  The list of tokens for each line is then passed to
     * the given closure. The stream is closed before the method returns.
     *
     * @param stream  an InputStream
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(InputStream stream, Pattern pattern, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) Closure<T> closure) throws IOException {
        return splitEachLine(new BufferedReader(new InputStreamReader(stream)), pattern, closure);
    }

    /**
     * Read a single, whole line from the given Reader. This method is designed for use with
     * Readers that support the {@code mark()} operation like BufferReader. It has a fallback
     * behavior for Readers that don't support mark() but the behavior doesn't correctly
     * detect multi-character line termination (e.g. carriage return followed by linefeed).
     * We recommend for Readers that don't support mark() you consider using one of the
     * following methods instead: eachLine, readLines, or iterator.
     *
     * @param self a Reader
     * @return a line
     * @throws IOException if an IOException occurs.
     * @see #readLines(java.io.Reader)
     * @see #iterator(java.io.Reader)
     * @see #eachLine(java.io.Reader, groovy.lang.Closure)
     * @since 1.0
     */
    public static String readLine(Reader self) throws IOException {
        if (self instanceof BufferedReader) {
            BufferedReader br = (BufferedReader) self;
            return br.readLine();
        }
        if (self.markSupported()) {
            return readLineFromReaderWithMark(self);
        }
        return readLineFromReaderWithoutMark(self);
    }

    private static int charBufferSize = 4096;     // half the default stream buffer size
    private static int expectedLineLength = 160;  // double the default line length
    private static int EOF = -1;                  // End Of File

    /*
    * This method tries to read subsequent buffers from the reader using a mark
    */
    private static String readLineFromReaderWithMark(final Reader input)
            throws IOException {
        char[] cbuf = new char[charBufferSize];
        try {
            input.mark(charBufferSize);
        } catch (IOException e) {
            // this should never happen
            LOG.warning("Caught exception setting mark on supporting reader: " + e);
            // fallback
            return readLineFromReaderWithoutMark(input);
        }

        // could be changed into do..while, but then
        // we might create an additional StringBuilder
        // instance at the end of the stream
        int count = input.read(cbuf);
        if (count == EOF) // we are at the end of the input data
            return null;

        StringBuilder line = new StringBuilder(expectedLineLength);
        // now work on the buffer(s)
        int ls = lineSeparatorIndex(cbuf, count);
        while (ls == -1) {
            line.append(cbuf, 0, count);
            count = input.read(cbuf);
            if (count == EOF) {
                // we are at the end of the input data
                return line.toString();
            }
            ls = lineSeparatorIndex(cbuf, count);
        }
        line.append(cbuf, 0, ls);

        // correct ls if we have \r\n
        int skipLS = 1;
        if (ls + 1 < count) {
            // we are not at the end of the buffer
            if (cbuf[ls] == '\r' && cbuf[ls + 1] == '\n') {
                skipLS++;
            }
        } else {
            if (cbuf[ls] == '\r' && input.read() == '\n') {
                skipLS++;
            }
        }

        //reset() and skip over last linesep
        input.reset();
        input.skip(line.length() + skipLS);
        return line.toString();
    }

    /*
    * This method reads without a buffer.
    * It returns too many empty lines if \r\n combinations
    * are used. Nothing can be done because we can't push
    * back the character we have just read.
    */
    private static String readLineFromReaderWithoutMark(Reader input)
            throws IOException {

        int c = input.read();
        if (c == -1)
            return null;
        StringBuilder line = new StringBuilder(expectedLineLength);

        while (c != EOF && c != '\n' && c != '\r') {
            char ch = (char) c;
            line.append(ch);
            c = input.read();
        }
        return line.toString();
    }

    /*
     * searches for \n or \r
     * Returns -1 if not found.
     */
    private static int lineSeparatorIndex(char[] array, int length) {
        for (int k = 0; k < length; k++) {
            if (isLineSeparator(array[k])) {
                return k;
            }
        }
        return -1;
    }

    /*
    * true if either \n or \r
    */
    private static boolean isLineSeparator(char c) {
        return c == '\n' || c == '\r';
    }

    /**
     * Reads the stream into a list, with one element for each line.
     *
     * @param stream a stream
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(java.io.Reader)
     * @since 1.0
     */
    public static List<String> readLines(InputStream stream) throws IOException {
        return readLines(newReader(stream));
    }

    /**
     * Reads the stream into a list, with one element for each line.
     *
     * @param stream  a stream
     * @param charset opens the stream with a specified charset
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @see #readLines(java.io.Reader)
     * @since 1.6.8
     */
    public static List<String> readLines(InputStream stream, String charset) throws IOException {
        return readLines(newReader(stream, charset));
    }

    /**
     * Reads the reader into a list of Strings, with one entry for each line.
     * The reader is closed before this method returns.
     *
     * @param reader a Reader
     * @return a List of lines
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static List<String> readLines(Reader reader) throws IOException {
        IteratorClosureAdapter<String> closure = new IteratorClosureAdapter<String>(reader);
        eachLine(reader, closure);
        return closure.asList();
    }

    /**
     * Read the content of this InputStream and return it as a String.
     * The stream is closed before this method returns.
     *
     * @param is an input stream
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static String getText(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return getText(reader);
    }

    /**
     * Read the content of this InputStream using specified charset and return
     * it as a String.  The stream is closed before this method returns.
     *
     * @param is      an input stream
     * @param charset opens the stream with a specified charset
     * @return the text from that URL
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static String getText(InputStream is, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        return getText(reader);
    }

    /**
     * Read the content of the Reader and return it as a String.  The reader
     * is closed before this method returns.
     *
     * @param reader a Reader whose content we want to read
     * @return a String containing the content of the buffered reader
     * @throws IOException if an IOException occurs.
     * @see #getText(java.io.BufferedReader)
     * @since 1.0
     */
    public static String getText(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        return getText(bufferedReader);
    }

    /**
     * Read the content of the BufferedReader and return it as a String.
     * The BufferedReader is closed afterwards.
     *
     * @param reader a BufferedReader whose content we want to read
     * @return a String containing the content of the buffered reader
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static String getText(BufferedReader reader) throws IOException {
        StringBuilder answer = new StringBuilder();
        // reading the content of the file within a char buffer
        // allow to keep the correct line endings
        char[] charBuffer = new char[8192];
        int nbCharRead /* = 0*/;
        try {
            while ((nbCharRead = reader.read(charBuffer)) != -1) {
                // appends buffer
                answer.append(charBuffer, 0, nbCharRead);
            }
            Reader temp = reader;
            reader = null;
            temp.close();
        } finally {
            closeWithWarning(reader);
        }
        return answer.toString();
    }

    /**
     * Read the content of this InputStream and return it as a byte[].
     * The stream is closed before this method returns.
     *
     * @param is an input stream
     * @return the byte[] from that InputStream
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream answer = new ByteArrayOutputStream();
        // reading the content of the file within a byte buffer
        byte[] byteBuffer = new byte[8192];
        int nbByteRead /* = 0*/;
        try {
            while ((nbByteRead = is.read(byteBuffer)) != -1) {
                // appends buffer
                answer.write(byteBuffer, 0, nbByteRead);
            }
        } finally {
            closeWithWarning(is);
        }
        return answer.toByteArray();
    }

    /**
     * Write the byte[] to the output stream.
     * The stream is closed before this method returns.
     *
     * @param os    an output stream
     * @param bytes the byte[] to write to the output stream
     * @throws IOException if an IOException occurs.
     * @since 1.7.1
     */
    public static void setBytes(OutputStream os, byte[] bytes) throws IOException {
        try {
            os.write(bytes);
        } finally {
            closeWithWarning(os);
        }
    }

    /**
     * Write the text and append a newline (using the platform's line-ending).
     *
     * @param writer a BufferedWriter
     * @param line   the line to write
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    /**
     * Creates an iterator which will traverse through the reader a line at a time.
     *
     * @param self a Reader object
     * @return an Iterator for the Reader
     * @see java.io.BufferedReader#readLine()
     * @since 1.5.0
     */
    public static Iterator<String> iterator(Reader self) {
        final BufferedReader bufferedReader;
        if (self instanceof BufferedReader)
            bufferedReader = (BufferedReader) self;
        else
            bufferedReader = new BufferedReader(self);
        return new Iterator<String>() {
            String nextVal /* = null */;
            boolean nextMustRead = true;
            boolean hasNext = true;

            public boolean hasNext() {
                if (nextMustRead && hasNext) {
                    try {
                        nextVal = readNext();
                        nextMustRead = false;
                    } catch (IOException e) {
                        hasNext = false;
                    }
                }
                return hasNext;
            }

            public String next() {
                String retval = null;
                if (nextMustRead) {
                    try {
                        retval = readNext();
                    } catch (IOException e) {
                        hasNext = false;
                    }
                } else
                    retval = nextVal;
                nextMustRead = true;
                return retval;
            }

            private String readNext() throws IOException {
                String nv = bufferedReader.readLine();
                if (nv == null)
                    hasNext = false;
                return nv;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from a Reader Iterator");
            }
        };
    }

    /**
     * Standard iterator for a input stream which iterates through the stream
     * content in a byte-based fashion.
     *
     * @param self an InputStream object
     * @return an Iterator for the InputStream
     * @since 1.5.0
     */
    public static Iterator<Byte> iterator(InputStream self) {
        return iterator(new DataInputStream(self));
    }

    /**
     * Standard iterator for a data input stream which iterates through the
     * stream content a Byte at a time.
     *
     * @param self a DataInputStream object
     * @return an Iterator for the DataInputStream
     * @since 1.5.0
     */
    public static Iterator<Byte> iterator(final DataInputStream self) {
        return new Iterator<Byte>() {
            Byte nextVal;
            boolean nextMustRead = true;
            boolean hasNext = true;

            public boolean hasNext() {
                if (nextMustRead && hasNext) {
                    try {
                        nextVal = self.readByte();
                        nextMustRead = false;
                    } catch (IOException e) {
                        hasNext = false;
                    }
                }
                return hasNext;
            }

            public Byte next() {
                Byte retval = null;
                if (nextMustRead) {
                    try {
                        retval = self.readByte();
                    } catch (IOException e) {
                        hasNext = false;
                    }
                } else
                    retval = nextVal;
                nextMustRead = true;
                return retval;
            }

            public void remove() {
                throw new UnsupportedOperationException("Cannot remove() from a DataInputStream Iterator");
            }
        };
    }

    /**
     * Creates a reader for this input stream.
     *
     * @param self an input stream
     * @return a reader
     * @since 1.0
     */
    public static BufferedReader newReader(InputStream self) {
        return new BufferedReader(new InputStreamReader(self));
    }

    /**
     * Creates a reader for this input stream, using the specified
     * charset as the encoding.
     *
     * @param self    an input stream
     * @param charset the charset for this input stream
     * @return a reader
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     * @since 1.6.0
     */
    public static BufferedReader newReader(InputStream self, String charset) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(self, charset));
    }

    /**
     * Create a new PrintWriter for this Writer.
     *
     * @param writer a Writer
     * @return a PrintWriter
     * @since 1.6.0
     */
    public static PrintWriter newPrintWriter(Writer writer) {
        return new GroovyPrintWriter(writer);
    }

    /**
     * Create a new PrintWriter for this OutputStream.
     *
     * @param stream an OutputStream
     * @return a PrintWriter
     * @since 2.2.0
     */
    public static PrintWriter newPrintWriter(OutputStream stream) {
        return new GroovyPrintWriter(stream);
    }

    /**
     * Create a new PrintWriter for this Writer.  The writer is passed to the
     * closure, and will be closed before this method returns.
     *
     * @param writer  a writer
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.6.0
     */
    public static <T> T withPrintWriter(Writer writer, @ClosureParams(value=SimpleType.class, options="java.io.PrintWriter") Closure<T> closure) throws IOException {
        return withWriter(newPrintWriter(writer), closure);
    }

    /**
     * Create a new PrintWriter for this OutputStream.  The writer is passed to the
     * closure, and will be closed before this method returns.
     *
     * @param stream  an OutputStream
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 2.2.0
     */
    public static <T> T withPrintWriter(OutputStream stream, @ClosureParams(value=SimpleType.class, options="java.io.PrintWriter") Closure<T> closure) throws IOException {
        return withWriter(newPrintWriter(stream), closure);
    }

    /**
     * Allows this writer to be used within the closure, ensuring that it
     * is flushed and closed before this method returns.
     *
     * @param writer  the writer which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
     */
    public static <T> T withWriter(Writer writer, @ClosureParams(FirstParam.class) Closure<T> closure) throws IOException {
        try {
            T result = closure.call(writer);

            try {
                writer.flush();
            } catch (IOException e) {
                // try to continue even in case of error
            }
            Writer temp = writer;
            writer = null;
            temp.close();
            return result;
        } finally {
            closeWithWarning(writer);
        }
    }

    /**
     * Allows this reader to be used within the closure, ensuring that it
     * is closed before this method returns.
     *
     * @param reader  the reader which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
     */
    public static <T> T withReader(Reader reader, @ClosureParams(FirstParam.class) Closure<T> closure) throws IOException {
        try {
            T result = closure.call(reader);

            Reader temp = reader;
            reader = null;
            temp.close();

            return result;
        } finally {
            closeWithWarning(reader);
        }
    }

    /**
     * Allows this input stream to be used within the closure, ensuring that it
     * is flushed and closed before this method returns.
     *
     * @param stream  the stream which is used and then closed
     * @param closure the closure that the stream is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
     */
    public static <T, U extends InputStream> T withStream(U stream, @ClosureParams(value=FirstParam.class) Closure<T> closure) throws IOException {
        try {
            T result = closure.call(stream);

            InputStream temp = stream;
            stream = null;
            temp.close();

            return result;
        } finally {
            closeWithWarning(stream);
        }
    }

    /**
     * Helper method to create a new Reader for a stream and then
     * passes it into the closure.  The reader (and this stream) is closed after
     * the closure returns.
     *
     * @param in      a stream
     * @param closure the closure to invoke with the InputStream
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see java.io.InputStreamReader
     * @since 1.5.2
     */
    public static <T> T withReader(InputStream in, @ClosureParams(value=SimpleType.class, options="java.io.Reader") Closure<T> closure) throws IOException {
        return withReader(new InputStreamReader(in), closure);
    }

    /**
     * Helper method to create a new Reader for a stream and then
     * passes it into the closure.  The reader (and this stream) is closed after
     * the closure returns.
     *
     * @param in      a stream
     * @param charset the charset used to decode the stream
     * @param closure the closure to invoke with the reader
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see java.io.InputStreamReader
     * @since 1.5.6
     */
    public static <T> T withReader(InputStream in, String charset, @ClosureParams(value=SimpleType.class, options="java.io.Reader") Closure<T> closure) throws IOException {
        return withReader(new InputStreamReader(in, charset), closure);
    }

    /**
     * Creates a writer from this stream, passing it to the given closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param stream  the stream which is used and then closed
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withWriter(java.io.Writer, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withWriter(OutputStream stream, @ClosureParams(value=SimpleType.class, options="java.io.Writer") Closure<T> closure) throws IOException {
        return withWriter(new OutputStreamWriter(stream), closure);
    }

    /**
     * Creates a writer for this stream.
     *
     * @param stream the stream which is used and then closed
     * @return the newly created Writer
     * @since 2.2.0
     */
    public static Writer newWriter(OutputStream stream) {
        return new OutputStreamWriter(stream);
    }

    /**
     * Creates a writer from this stream, passing it to the given closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param stream  the stream which is used and then closed
     * @param charset the charset used
     * @param closure the closure that the writer is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @see #withWriter(java.io.Writer, groovy.lang.Closure)
     * @since 1.5.2
     */
    public static <T> T withWriter(OutputStream stream, String charset, @ClosureParams(value=SimpleType.class, options="java.io.Writer") Closure<T> closure) throws IOException {
        return withWriter(new OutputStreamWriter(stream, charset), closure);
    }

    /**
     * Creates a writer for this stream using the given charset.
     *
     * @param stream the stream which is used and then closed
     * @param charset the charset used
     * @return the newly created Writer
     * @throws UnsupportedEncodingException if an encoding exception occurs.
     * @since 2.2.0
     */
    public static Writer newWriter(OutputStream stream, String charset) throws UnsupportedEncodingException {
        return new OutputStreamWriter(stream, charset);
    }

    /**
     * Passes this OutputStream to the closure, ensuring that the stream
     * is closed after the closure returns, regardless of errors.
     *
     * @param os      the stream which is used and then closed
     * @param closure the closure that the stream is passed into
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 1.5.2
     */
    public static <T, U extends OutputStream> T withStream(U os, @ClosureParams(value=FirstParam.class) Closure<T> closure) throws IOException {
        try {
            T result = closure.call(os);
            os.flush();

            OutputStream temp = os;
            os = null;
            temp.close();

            return result;
        } finally {
            closeWithWarning(os);
        }
    }

    /**
     * Traverse through each byte of the specified stream. The
     * stream is closed after the closure returns.
     *
     * @param is      stream to iterate over, closed after the method call
     * @param closure closure to apply to each byte
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static void eachByte(InputStream is, @ClosureParams(value=SimpleType.class, options="byte") Closure closure) throws IOException {
        try {
            while (true) {
                int b = is.read();
                if (b == -1) {
                    break;
                } else {
                    closure.call((byte) b);
                }
            }

            InputStream temp = is;
            is = null;
            temp.close();
        } finally {
            closeWithWarning(is);
        }
    }

    /**
     * Traverse through each the specified stream reading bytes into a buffer
     * and calling the 2 parameter closure with this buffer and the number of bytes.
     *
     * @param is        stream to iterate over, closed after the method call.
     * @param bufferLen the length of the buffer to use.
     * @param closure   a 2 parameter closure which is passed the byte[] and a number of bytes successfully read.
     * @throws IOException if an IOException occurs.
     * @since 1.8
     */
    public static void eachByte(InputStream is, int bufferLen, @ClosureParams(value=FromString.class, options="byte[],Integer") Closure closure) throws IOException {
        byte[] buffer = new byte[bufferLen];
        int bytesRead;
        try {
            while ((bytesRead = is.read(buffer, 0, bufferLen)) > 0) {
                closure.call(buffer, bytesRead);
            }

            InputStream temp = is;
            is = null;
            temp.close();
        } finally {
            closeWithWarning(is);
        }
    }

    /**
     * Transforms each character from this reader by passing it to the given
     * closure.  The Closure should return each transformed character, which
     * will be passed to the Writer.  The reader and writer will both be
     * closed before this method returns.
     *
     * @param self    a Reader object
     * @param writer  a Writer to receive the transformed characters
     * @param closure a closure that performs the required transformation
     * @throws IOException if an IOException occurs.
     * @since 1.5.0
     */
    public static void transformChar(Reader self, Writer writer, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure closure) throws IOException {
        int c;
        try {
            char[] chars = new char[1];
            while ((c = self.read()) != -1) {
                chars[0] = (char) c;
                Object o = closure.call(new String(chars));
                if (o != null) {
                    writer.write(o.toString());
                }
            }
            writer.flush();

            Writer temp2 = writer;
            writer = null;
            temp2.close();
            Reader temp1 = self;
            self = null;
            temp1.close();
        } finally {
            closeWithWarning(self);
            closeWithWarning(writer);
        }
    }

    /**
     * Transforms the lines from a reader with a Closure and
     * write them to a writer. Both Reader and Writer are
     * closed after the operation.
     *
     * @param reader  Lines of text to be transformed. Reader is closed afterwards.
     * @param writer  Where transformed lines are written. Writer is closed afterwards.
     * @param closure Single parameter closure that is called to transform each line of
     *                text from the reader, before writing it to the writer.
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static void transformLine(Reader reader, Writer writer, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure closure) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                Object o = closure.call(line);
                if (o != null) {
                    bw.write(o.toString());
                    bw.newLine();
                }
            }
            bw.flush();

            Writer temp2 = writer;
            writer = null;
            temp2.close();
            Reader temp1 = reader;
            reader = null;
            temp1.close();
        } finally {
            closeWithWarning(br);
            closeWithWarning(reader);
            closeWithWarning(bw);
            closeWithWarning(writer);
        }
    }

    /**
     * Filter the lines from a reader and write them on the writer,
     * according to a closure which returns true if the line should be included.
     * Both Reader and Writer are closed after the operation.
     *
     * @param reader  a reader, closed after the call
     * @param writer  a writer, closed after the call
     * @param closure the closure which returns booleans
     * @throws IOException if an IOException occurs.
     * @since 1.0
     */
    public static void filterLine(Reader reader, Writer writer, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure closure) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        String line;
        try {
            BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
            while ((line = br.readLine()) != null) {
                if (bcw.call(line)) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.flush();

            Writer temp2 = writer;
            writer = null;
            temp2.close();
            Reader temp1 = reader;
            reader = null;
            temp1.close();
        } finally {
            closeWithWarning(br);
            closeWithWarning(reader);
            closeWithWarning(bw);
            closeWithWarning(writer);
        }

    }

    /**
     * Filter the lines from this Reader, and return a Writable which can be
     * used to stream the filtered lines to a destination.  The closure should
     * return <code>true</code> if the line should be passed to the writer.
     *
     * @param reader  this reader
     * @param closure a closure used for filtering
     * @return a Writable which will use the closure to filter each line
     *         from the reader when the Writable#writeTo(Writer) is called.
     * @since 1.0
     */
    public static Writable filterLine(Reader reader, @ClosureParams(value=SimpleType.class, options="java.lang.String") final Closure closure) {
        final BufferedReader br = new BufferedReader(reader);
        return new Writable() {
            public Writer writeTo(Writer out) throws IOException {
                BufferedWriter bw = new BufferedWriter(out);
                String line;
                BooleanClosureWrapper bcw = new BooleanClosureWrapper(closure);
                while ((line = br.readLine()) != null) {
                    if (bcw.call(line)) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
                bw.flush();
                return out;
            }

            public String toString() {
                Writer buffer = new StringBuilderWriter();
                try {
                    writeTo(buffer);
                } catch (IOException e) {
                    throw new StringWriterIOException(e);
                }
                return buffer.toString();
            }
        };
    }

    /**
     * Filter lines from an input stream using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      an input stream
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
     * @since 1.0
     */
    public static Writable filterLine(InputStream self, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure predicate) {
        return filterLine(newReader(self), predicate);
    }

    /**
     * Filter lines from an input stream using a closure predicate.  The closure
     * will be passed each line as a String, and it should return
     * <code>true</code> if the line should be passed to the writer.
     *
     * @param self      an input stream
     * @param charset   opens the stream with a specified charset
     * @param predicate a closure which returns boolean and takes a line
     * @return a writable which writes out the filtered lines
     * @throws UnsupportedEncodingException if the encoding specified is not supported
     * @see #filterLine(java.io.Reader, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static Writable filterLine(InputStream self, String charset, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure predicate)
            throws UnsupportedEncodingException {
        return filterLine(newReader(self, charset), predicate);
    }

    /**
     * Uses a closure to filter lines from this InputStream and pass them to
     * the given writer. The closure will be passed each line as a String, and
     * it should return <code>true</code> if the line should be passed to the
     * writer.
     *
     * @param self      the InputStream
     * @param writer    a writer to write output to
     * @param predicate a closure which returns true if a line should be accepted
     * @throws IOException if an IOException occurs.
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
     * @since 1.0
     */
    public static void filterLine(InputStream self, Writer writer, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure predicate)
            throws IOException {
        filterLine(newReader(self), writer, predicate);
    }

    /**
     * Uses a closure to filter lines from this InputStream and pass them to
     * the given writer. The closure will be passed each line as a String, and
     * it should return <code>true</code> if the line should be passed to the
     * writer.
     *
     * @param self      the InputStream
     * @param writer    a writer to write output to
     * @param charset   opens the stream with a specified charset
     * @param predicate a closure which returns true if a line should be accepted
     * @throws IOException if an IOException occurs.
     * @see #filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
     * @since 1.6.8
     */
    public static void filterLine(InputStream self, Writer writer, String charset, @ClosureParams(value=SimpleType.class, options="java.lang.String") Closure predicate)
            throws IOException {
        filterLine(newReader(self, charset), writer, predicate);
    }

    /**
     * Allows this closeable to be used within the closure, ensuring that it
     * is closed once the closure has been executed and before this method returns.
     * <p>
     * As with the try-with-resources statement, if multiple exceptions are thrown
     * the exception from the closure will be returned and the exception from closing
     * will be added as a suppressed exception.
     *
     * @param self the Closeable
     * @param action the closure taking the Closeable as parameter
     * @return the value returned by the closure
     * @throws IOException if an IOException occurs.
     * @since 2.4.0
     */
    public static <T, U extends Closeable> T withCloseable(U self, @ClosureParams(value=FirstParam.class) Closure<T> action) throws IOException {
        Throwable thrown = null;
        try {
            return action.call(self);
        } catch (Throwable e) {
            thrown = e;
            throw e;
        } finally {
            if (thrown != null) {
                Throwable suppressed = tryClose(self, true);
                if (suppressed != null) {
                    thrown.addSuppressed(suppressed);
                }
            } else {
                self.close();
            }
        }
    }

    /**
     * Allows this AutoCloseable to be used within the closure, ensuring that it
     * is closed once the closure has been executed and before this method returns.
     * <p>
     * As with the try-with-resources statement, if multiple exceptions are thrown
     * the exception from the closure will be returned and the exception from closing
     * will be added as a suppressed exception.
     *
     * @param self the AutoCloseable
     * @param action the closure taking the AutoCloseable as parameter
     * @return the value returned by the closure
     * @throws Exception if an Exception occurs.
     * @since 2.5.0
     */
    public static <T, U extends AutoCloseable> T withCloseable(U self, @ClosureParams(value=FirstParam.class) Closure<T> action) throws Exception {
        Throwable thrown = null;
        try {
            return action.call(self);
        } catch (Throwable e) {
            thrown = e;
            throw e;
        } finally {
            if (thrown != null) {
                Throwable suppressed = tryClose(self, true);
                if (suppressed != null) {
                    thrown.addSuppressed(suppressed);
                }
            } else {
                self.close();
            }
        }
    }

    static void writeUTF16BomIfRequired(final Writer writer, final String charset) throws IOException {
        writeUTF16BomIfRequired(writer, Charset.forName(charset));
    }

    static void writeUTF16BomIfRequired(final Writer writer, final Charset charset) throws IOException {
        if ("UTF-16BE".equals(charset.name())) {
            writeUtf16Bom(writer, true);
        } else if ("UTF-16LE".equals(charset.name())) {
            writeUtf16Bom(writer, false);
        }
    }

    static void writeUTF16BomIfRequired(final OutputStream stream, final String charset) throws IOException {
        writeUTF16BomIfRequired(stream, Charset.forName(charset));
    }

    static void writeUTF16BomIfRequired(final OutputStream stream, final Charset charset) throws IOException {
        if ("UTF-16BE".equals(charset.name())) {
            writeUtf16Bom(stream, true);
        } else if ("UTF-16LE".equals(charset.name())) {
            writeUtf16Bom(stream, false);
        }
    }

    private static void writeUtf16Bom(OutputStream stream, boolean bigEndian) throws IOException {
        if (bigEndian) {
            stream.write(-2);  // FE
            stream.write(-1);  // FF
        } else {
            stream.write(-1);  // FF
            stream.write(-2);  // FE
        }
    }

    private static void writeUtf16Bom(Writer writer, boolean bigEndian) throws IOException {
        if (bigEndian) {
            writer.write(-2);  // FE
            writer.write(-1);  // FF
        } else {
            writer.write(-1);  // FF
            writer.write(-2);  // FE
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 8192; // 8k
}
