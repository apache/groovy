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

import groovy.io.FileType;
import groovy.io.FileVisitResult;
import groovy.io.GroovyPrintWriter;
import groovy.lang.Closure;
import groovy.lang.MetaClass;
import groovy.lang.Writable;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FromString;
import groovy.transform.stc.PickFirstResolver;
import groovy.transform.stc.SimpleType;
import org.codehaus.groovy.runtime.callsite.BooleanReturningMethodInvoker;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.get;

/**
 * This class defines new groovy methods for Readers, Writers, InputStreams and
 * OutputStreams which appear on normal JDK classes inside the Groovy environment.
 * Static methods are used with the first parameter being the destination class,
 * i.e. <code>public static T eachLine(InputStream self, Closure c)</code>
 * provides a <code>eachLine(Closure c)</code> method for <code>InputStream</code>.
 * <p/>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 */

public class NioGroovyMethods extends DefaultGroovyMethodsSupport {

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Path</code>.
     *
     * @param self a {@code Path} object
     * @return the file's size (length)
     * @since 2.3.0
     */
    public static long size(Path self) throws IOException {
        return Files.size(self);
    }

    /**
     * Create an object output stream for this path.
     *
     * @param self a {@code Path} object
     * @return an object output stream
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static ObjectOutputStream newObjectOutputStream(Path self) throws IOException {
        return new ObjectOutputStream(Files.newOutputStream(self));
    }

    /**
     * Create a new ObjectOutputStream for this path and then pass it to the
     * closure.  This method ensures the stream is closed after the closure
     * returns.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see IOGroovyMethods#withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T withObjectOutputStream(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.ObjectOutputStream") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withStream(newObjectOutputStream(self), closure);
    }

    /**
     * Create an object input stream for this file.
     *
     * @param self a {@code Path} object
     * @return an object input stream
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static ObjectInputStream newObjectInputStream(Path self) throws IOException {
        return new ObjectInputStream(Files.newInputStream(self));
    }

    /**
     * Create an object input stream for this path using the given class loader.
     *
     * @param self        a {@code Path} object
     * @param classLoader the class loader to use when loading the class
     * @return an object input stream
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static ObjectInputStream newObjectInputStream(Path self, final ClassLoader classLoader) throws IOException {
        return IOGroovyMethods.newObjectInputStream(Files.newInputStream(self), classLoader);
    }

    /**
     * Iterates through the given file object by object.
     *
     * @param self    a {@code Path} object
     * @param closure a closure
     * @throws java.io.IOException    if an IOException occurs.
     * @throws ClassNotFoundException if the class  is not found.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#eachObject(java.io.ObjectInputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachObject(Path self, Closure closure) throws IOException, ClassNotFoundException {
        IOGroovyMethods.eachObject(newObjectInputStream(self), closure);
    }

    /**
     * Create a new ObjectInputStream for this file and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param path    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T withObjectInputStream(Path path, @ClosureParams(value = SimpleType.class, options = "java.io.ObjectInputStream") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withStream(newObjectInputStream(path), closure);
    }

    /**
     * Create a new ObjectInputStream for this file associated with the given class loader and pass it to the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param self        a Path
     * @param classLoader the class loader to use when loading the class
     * @param closure     a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T withObjectInputStream(Path self, ClassLoader classLoader, @ClosureParams(value = SimpleType.class, options = "java.io.ObjectInputStream") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withStream(newObjectInputStream(self, classLoader), closure);
    }

    /**
     * Iterates through this path line by line.  Each line is passed to the
     * given 1 or 2 arg closure.  The file is read using a reader which
     * is closed before this method returns.
     *
     * @param self    a Path
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see #eachLine(Path, int, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T eachLine(Path self, @ClosureParams(value = FromString.class, options = {"String", "String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(self, 1, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed to the
     * given 1 or 2 arg closure.  The file is read using a reader which
     * is closed before this method returns.
     *
     * @param self    a Path
     * @param charset opens the file with a specified charset
     * @param closure a closure (arg 1 is line, optional arg 2 is line number starting at line 1)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see #eachLine(Path, String, int, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T eachLine(Path self, String charset, @ClosureParams(value = FromString.class, options = {"String", "String,Integer"}) Closure<T> closure) throws IOException {
        return eachLine(self, charset, 1, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed
     * to the given 1 or 2 arg closure.  The file is read using a reader
     * which is closed before this method returns.
     *
     * @param self      a Path
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T eachLine(Path self, int firstLine, @ClosureParams(value = FromString.class, options = {"String", "String,Integer"}) Closure<T> closure) throws IOException {
        return IOGroovyMethods.eachLine(newReader(self), firstLine, closure);
    }

    /**
     * Iterates through this file line by line.  Each line is passed
     * to the given 1 or 2 arg closure.  The file is read using a reader
     * which is closed before this method returns.
     *
     * @param self      a Path
     * @param charset   opens the file with a specified charset
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure   a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#eachLine(java.io.Reader, int, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T eachLine(Path self, String charset, int firstLine, @ClosureParams(value = FromString.class, options = {"String", "String,Integer"}) Closure<T> closure) throws IOException {
        return IOGroovyMethods.eachLine(newReader(self, charset), firstLine, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a Path
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException                    if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#splitEachLine(java.io.Reader, String, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T splitEachLine(Path self, String regex, @ClosureParams(value = FromString.class, options = {"List<String>", "String[]"}, conflictResolutionStrategy = PickFirstResolver.class) Closure<T> closure) throws IOException {
        return IOGroovyMethods.splitEachLine(newReader(self), regex, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression Pattern.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a Path
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T splitEachLine(Path self, Pattern pattern, @ClosureParams(value = FromString.class, options = {"List<String>", "String[]"}, conflictResolutionStrategy = PickFirstResolver.class) Closure<T> closure) throws IOException {
        return IOGroovyMethods.splitEachLine(newReader(self), pattern, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given regex separator. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a Path
     * @param regex   the delimiting regular expression
     * @param charset opens the file with a specified charset
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException                    if an IOException occurs.
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#splitEachLine(java.io.Reader, String, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T splitEachLine(Path self, String regex, String charset, @ClosureParams(value = FromString.class, options = {"List<String>", "String[]"}, conflictResolutionStrategy = PickFirstResolver.class) Closure<T> closure) throws IOException {
        return IOGroovyMethods.splitEachLine(newReader(self, charset), regex, closure);
    }

    /**
     * Iterates through this file line by line, splitting each line using
     * the given regex separator Pattern. For each line, the given closure is called with
     * a single parameter being the list of strings computed by splitting the line
     * around matches of the given regular expression.
     * Finally the resources used for processing the file are closed.
     *
     * @param self    a Path
     * @param pattern the regular expression Pattern for the delimiter
     * @param charset opens the file with a specified charset
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#splitEachLine(java.io.Reader, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T splitEachLine(Path self, Pattern pattern, String charset, @ClosureParams(value = FromString.class, options = {"List<String>", "String[]"}, conflictResolutionStrategy = PickFirstResolver.class) Closure<T> closure) throws IOException {
        return IOGroovyMethods.splitEachLine(newReader(self, charset), pattern, closure);
    }

    /**
     * Reads the file into a list of Strings, with one item for each line.
     *
     * @param self a Path
     * @return a List of lines
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#readLines(java.io.Reader)
     * @since 2.3.0
     */
    public static List<String> readLines(Path self) throws IOException {
        return IOGroovyMethods.readLines(newReader(self));
    }

    /**
     * Reads the file into a list of Strings, with one item for each line.
     *
     * @param self    a Path
     * @param charset opens the file with a specified charset
     * @return a List of lines
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#readLines(java.io.Reader)
     * @since 2.3.0
     */
    public static List<String> readLines(Path self, String charset) throws IOException {
        return IOGroovyMethods.readLines(newReader(self, charset));
    }

    /**
     * Read the content of the Path using the specified encoding and return it
     * as a String.
     *
     * @param self    the file whose content we want to read
     * @param charset the charset used to read the content of the file
     * @return a String containing the content of the file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static String getText(Path self, String charset) throws IOException {
        return IOGroovyMethods.getText(newReader(self, charset));
    }

    /**
     * Read the content of the Path and returns it as a String.
     *
     * @param self the file whose content we want to read
     * @return a String containing the content of the file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static String getText(Path self) throws IOException {
        return IOGroovyMethods.getText(newReader(self));
    }

    /**
     * Read the content of the Path and returns it as a byte[].
     *
     * @param self the file whose content we want to read
     * @return a String containing the content of the file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static byte[] getBytes(Path self) throws IOException {
        return IOGroovyMethods.getBytes(Files.newInputStream(self));
    }

    /**
     * Write the bytes from the byte array to the Path.
     *
     * @param self  the file to write to
     * @param bytes the byte[] to write to the file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void setBytes(Path self, byte[] bytes) throws IOException {
        IOGroovyMethods.setBytes(Files.newOutputStream(self), bytes);
    }

    /**
     * Write the text to the Path without writing a BOM .
     *
     * @param self a Path
     * @param text the text to write to the Path
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void write(Path self, String text) throws IOException {
        write(self, text, false);
    }

    /**
     * Write the text to the Path.  If the default charset is
     * "UTF-16BE" or "UTF-16LE" (or an equivalent alias) and
     * <code>writeBom</code> is <code>true</code>, the requisite byte order
     * mark is written to the file before the text.
     *
     * @param self     a Path
     * @param text     the text to write to the Path
     * @param writeBom whether to write the BOM
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void write(Path self, String text, boolean writeBom) throws IOException {
        write(self, text, Charset.defaultCharset().name(), writeBom);
    }

    /**
     * Synonym for write(text) allowing file.text = 'foo'.
     *
     * @param self a Path
     * @param text the text to write to the Path
     * @throws java.io.IOException if an IOException occurs.
     * @see #write(Path, String)
     * @since 2.3.0
     */
    public static void setText(Path self, String text) throws IOException {
        write(self, text);
    }

    /**
     * Synonym for write(text, charset) allowing:
     * <pre>
     * myFile.setText('some text', charset)
     * </pre>
     * or with some help from <code>ExpandoMetaClass</code>, you could do something like:
     * <pre>
     * myFile.metaClass.setText = { String s -> delegate.setText(s, 'UTF-8') }
     * myfile.text = 'some text'
     * </pre>
     *
     * @param self    A Path
     * @param charset The charset used when writing to the file
     * @param text    The text to write to the Path
     * @throws java.io.IOException if an IOException occurs.
     * @see #write(Path, String, String)
     * @since 2.3.0
     */
    public static void setText(Path self, String text, String charset) throws IOException {
        write(self, text, charset);
    }

    /**
     * Write the text to the Path.
     *
     * @param self a Path
     * @param text the text to write to the Path
     * @return the original file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static Path leftShift(Path self, Object text) throws IOException {
        append(self, text);
        return self;
    }

    /**
     * Write bytes to a Path.
     *
     * @param self  a Path
     * @param bytes the byte array to append to the end of the Path
     * @return the original file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static Path leftShift(Path self, byte[] bytes) throws IOException {
        append(self, bytes);
        return self;
    }

    /**
     * Append binary data to the file.  See {@link #append(Path, java.io.InputStream)}
     *
     * @param path a Path
     * @param data an InputStream of data to write to the file
     * @return the file
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static Path leftShift(Path path, InputStream data) throws IOException {
        append(path, data);
        return path;
    }

    /**
     * Write the text to the Path without writing a BOM, using the specified encoding.
     *
     * @param self    a Path
     * @param text    the text to write to the Path
     * @param charset the charset used
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void write(Path self, String text, String charset) throws IOException {
        write(self, text, charset, false);
    }

    /**
     * Write the text to the Path, using the specified encoding.  If the given
     * charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias) and
     * <code>writeBom</code> is <code>true</code>, the requisite byte order
     * mark is written to the file before the text.
     *
     * @param self     a Path
     * @param text     the text to write to the Path
     * @param charset  the charset used
     * @param writeBom whether to write a BOM
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void write(Path self, String text, String charset, boolean writeBom) throws IOException {
        Writer writer = null;
        try {
            OutputStream out = Files.newOutputStream(self);
            if (writeBom) {
                IOGroovyMethods.writeUTF16BomIfRequired(out, charset);
            }
            writer = new OutputStreamWriter(out, Charset.forName(charset));
            writer.write(text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWithWarning(writer);
        }
    }

    /**
     * Append the text at the end of the Path without writing a BOM.
     *
     * @param self a Path
     * @param text the text to append at the end of the Path
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path self, Object text) throws IOException {
        append(self, text, Charset.defaultCharset().name(), false);
    }

    /**
     * Append the text supplied by the Writer at the end of the File without
     * writing a BOM.
     *
     * @param file   a Path
     * @param reader the Reader supplying the text to append at the end of the File
     * @throws IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path file, Reader reader) throws IOException {
        append(file, reader, Charset.defaultCharset().name());
    }

    /**
     * Append the text supplied by the Writer at the end of the File without writing a BOM.
     *
     * @param file   a File
     * @param writer the Writer supplying the text to append at the end of the File
     * @throws IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path file, Writer writer) throws IOException {
        append(file, writer, Charset.defaultCharset().name());
    }

    /**
     * Append bytes to the end of a Path.  It <strong>will not</strong> be
     * interpreted as text.
     *
     * @param self  a Path
     * @param bytes the byte array to append to the end of the Path
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path self, byte[] bytes) throws IOException {
        OutputStream stream = null;
        try {
            stream = Files.newOutputStream(self, CREATE, APPEND);
            stream.write(bytes, 0, bytes.length);
            stream.flush();

            OutputStream temp = stream;
            stream = null;
            temp.close();
        } finally {
            closeWithWarning(stream);
        }
    }

    /**
     * Append binary data to the file.  It <strong>will not</strong> be
     * interpreted as text.
     *
     * @param self   a Path
     * @param stream stream to read data from.
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path self, InputStream stream) throws IOException {
        OutputStream out = Files.newOutputStream(self, CREATE, APPEND);
        try {
            IOGroovyMethods.leftShift(out, stream);
        } finally {
            closeWithWarning(out);
        }
    }

    /**
     * Append the text at the end of the Path.  If the default charset is
     * "UTF-16BE" or "UTF-16LE" (or an equivalent alias) and
     * <code>writeBom</code> is <code>true</code>, the requisite byte order
     * mark is written to the file before the text.
     *
     * @param self     a Path
     * @param text     the text to append at the end of the Path
     * @param writeBom whether to write the BOM
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void append(Path self, Object text, boolean writeBom) throws IOException {
        append(self, text, Charset.defaultCharset().name(), writeBom);
    }

    /**
     * Append the text at the end of the Path without writing a BOM, using a specified
     * encoding.
     *
     * @param self    a Path
     * @param text    the text to append at the end of the Path
     * @param charset the charset used
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path self, Object text, String charset) throws IOException {
        append(self, text, charset, false);
    }

    /**
     * Append the text at the end of the Path, using a specified encoding.  If
     * the given charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias),
     * <code>writeBom</code> is <code>true</code>, and the file doesn't already
     * exist, the requisite byte order mark is written to the file before the
     * text is appended.
     *
     * @param self     a Path
     * @param text     the text to append at the end of the Path
     * @param charset  the charset used
     * @param writeBom whether to write the BOM
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void append(Path self, Object text, String charset, boolean writeBom) throws IOException {
        Writer writer = null;
        try {
            Charset resolvedCharset = Charset.forName(charset);
            boolean shouldWriteBom = writeBom && !self.toFile().exists();
            OutputStream out = Files.newOutputStream(self, CREATE, APPEND);
            if (shouldWriteBom) {
                IOGroovyMethods.writeUTF16BomIfRequired(out, resolvedCharset);
            }
            writer = new OutputStreamWriter(out, resolvedCharset);
            InvokerHelper.write(writer, text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWithWarning(writer);
        }
    }

    /**
     * Append the text supplied by the Writer at the end of the File, using a specified encoding.
     * If the given charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias),
     * <code>writeBom</code> is <code>true</code>, and the file doesn't already
     * exist, the requisite byte order mark is written to the file before the
     * text is appended.
     *
     * @param file     a File
     * @param writer   the Writer supplying the text to append at the end of the File
     * @param writeBom whether to write the BOM
     * @throws IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void append(Path file, Writer writer, boolean writeBom) throws IOException {
        append(file, writer, Charset.defaultCharset().name(), writeBom);
    }

    /**
     * Append the text supplied by the Writer at the end of the File without writing a BOM,
     * using a specified encoding.
     *
     * @param file    a File
     * @param writer  the Writer supplying the text to append at the end of the File
     * @param charset the charset used
     * @throws IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path file, Writer writer, String charset) throws IOException {
        appendBuffered(file, writer, charset, false);
    }

    /**
     * Append the text supplied by the Writer at the end of the File, using a specified encoding.
     * If the given charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias),
     * <code>writeBom</code> is <code>true</code>, and the file doesn't already
     * exist, the requisite byte order mark is written to the file before the
     * text is appended.
     *
     * @param file     a File
     * @param writer   the Writer supplying the text to append at the end of the File
     * @param charset  the charset used
     * @param writeBom whether to write the BOM
     * @throws IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void append(Path file, Writer writer, String charset, boolean writeBom) throws IOException {
        appendBuffered(file, writer, charset, writeBom);
    }

    /**
     * Append the text supplied by the Reader at the end of the File, using a specified encoding.
     * If the given charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias),
     * <code>writeBom</code> is <code>true</code>, and the file doesn't already
     * exist, the requisite byte order mark is written to the file before the
     * text is appended.
     *
     * @param file     a File
     * @param reader   the Reader supplying the text to append at the end of the File
     * @param writeBom whether to write the BOM
     * @throws IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void append(Path file, Reader reader, boolean writeBom) throws IOException {
        appendBuffered(file, reader, Charset.defaultCharset().name(), writeBom);
    }

    /**
     * Append the text supplied by the Reader at the end of the File without writing
     * a BOM, using a specified encoding.
     *
     * @param file    a File
     * @param reader  the Reader supplying the text to append at the end of the File
     * @param charset the charset used
     * @throws IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static void append(Path file, Reader reader, String charset) throws IOException {
        append(file, reader, charset, false);
    }

    /**
     * Append the text supplied by the Reader at the end of the File, using a specified encoding.
     * If the given charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias),
     * <code>writeBom</code> is <code>true</code>, and the file doesn't already
     * exist, the requisite byte order mark is written to the file before the
     * text is appended.
     *
     * @param file     a File
     * @param reader   the Reader supplying the text to append at the end of the File
     * @param charset  the charset used
     * @param writeBom whether to write the BOM
     * @throws IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static void append(Path file, Reader reader, String charset, boolean writeBom) throws IOException {
        appendBuffered(file, reader, charset, writeBom);
    }

    private static void appendBuffered(Path file, Object text, String charset, boolean writeBom) throws IOException {
        BufferedWriter writer = null;
        try {
            boolean shouldWriteBom = writeBom && !file.toFile().exists();
            writer = newWriter(file, charset, true);
            if (shouldWriteBom) {
                IOGroovyMethods.writeUTF16BomIfRequired(writer, charset);
            }
            InvokerHelper.write(writer, text);
            writer.flush();

            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWithWarning(writer);
        }
    }

    /**
     * This method is used to throw useful exceptions when the eachFile* and eachDir closure methods
     * are used incorrectly.
     *
     * @param self The directory to check
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @since 2.3.0
     */
    private static void checkDir(Path self) throws FileNotFoundException, IllegalArgumentException {
        if (!Files.exists(self))
            throw new FileNotFoundException(self.toAbsolutePath().toString());
        if (!Files.isDirectory(self))
            throw new IllegalArgumentException("The provided Path object is not a directory: " + self.toAbsolutePath());
    }

    /**
     * Invokes the closure for each 'child' file in this 'parent' folder/directory.
     * Both regular files and subfolders/subdirectories can be processed depending
     * on the fileType enum value.
     *
     * @param self     a Path (that happens to be a folder/directory)
     * @param fileType if normal files or directories or both should be processed
     * @param closure  the closure to invoke
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @since 2.3.0
     */
    public static void eachFile(final Path self, final FileType fileType, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure) throws IOException {
        //throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);

        // TODO GroovyDoc doesn't parse this file as our java.g doesn't handle this JDK7 syntax
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(self)) {
            for (Path path : stream) {
                if (fileType == FileType.ANY ||
                        (fileType != FileType.FILES && Files.isDirectory(path)) ||
                        (fileType != FileType.DIRECTORIES && Files.isRegularFile(path))) {
                    closure.call(path);
                }
            }
        }
    }

    /**
     * Invokes the closure for each 'child' file in this 'parent' folder/directory.
     * Both regular files and subfolders/subdirectories are processed.
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param closure a closure (the parameter is the Path for the 'child' file)
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #eachFile(Path, groovy.io.FileType, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachFile(final Path self, final Closure closure) throws IOException { // throws FileNotFoundException, IllegalArgumentException {
        eachFile(self, FileType.ANY, closure);
    }

    /**
     * Invokes the closure for each subdirectory in this directory,
     * ignoring regular files.
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param closure a closure (the parameter is the Path for the subdirectory file)
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #eachFile(Path, groovy.io.FileType, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachDir(Path self, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") Closure closure) throws IOException { // throws FileNotFoundException, IllegalArgumentException {
        eachFile(self, FileType.DIRECTORIES, closure);
    }

    /**
     * Processes each descendant file in this directory and any sub-directories.
     * Processing consists of potentially calling <code>closure</code> passing it the current
     * file (which may be a normal file or subdirectory) and then if a subdirectory was encountered,
     * recursively processing the subdirectory. Whether the closure is called is determined by whether
     * the file was a normal file or subdirectory and the value of fileType.
     *
     * @param self     a Path (that happens to be a folder/directory)
     * @param fileType if normal files or directories or both should be processed
     * @param closure  the closure to invoke on each file
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @since 2.3.0
     */
    public static void eachFileRecurse(final Path self, final FileType fileType, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure) throws IOException { // throws FileNotFoundException, IllegalArgumentException {
        // throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(self)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    if (fileType != FileType.FILES) closure.call(path);
                    eachFileRecurse(path, fileType, closure);
                } else if (fileType != FileType.DIRECTORIES) {
                    closure.call(path);
                }
            }
        }
    }

    /**
     * Processes each descendant file in this directory and any sub-directories.
     * Processing consists of potentially calling <code>closure</code> passing it the current
     * file (which may be a normal file or subdirectory) and then if a subdirectory was encountered,
     * recursively processing the subdirectory.
     * <p>
     * The traversal can be adapted by providing various options in the <code>options</code> Map according
     * to the following keys:<dl>
     * <dt>type</dt><dd>A {@link groovy.io.FileType} enum to determine if normal files or directories or both are processed</dd>
     * <dt>preDir</dt><dd>A {@link groovy.lang.Closure} run before each directory is processed and optionally returning a {@link groovy.io.FileVisitResult} value
     * which can be used to control subsequent processing.</dd>
     * <dt>preRoot</dt><dd>A boolean indicating that the 'preDir' closure should be applied at the root level</dd>
     * <dt>postDir</dt><dd>A {@link groovy.lang.Closure} run after each directory is processed and optionally returning a {@link groovy.io.FileVisitResult} value
     * which can be used to control subsequent processing.</dd>
     * <dt>postRoot</dt><dd>A boolean indicating that the 'postDir' closure should be applied at the root level</dd>
     * <dt>visitRoot</dt><dd>A boolean indicating that the given closure should be applied for the root dir
     * (not applicable if the 'type' is set to {@link groovy.io.FileType#FILES})</dd>
     * <dt>maxDepth</dt><dd>The maximum number of directory levels when recursing
     * (default is -1 which means infinite, set to 0 for no recursion)</dd>
     * <dt>filter</dt><dd>A filter to perform on traversed files/directories (using the {@link DefaultGroovyMethods#isCase(Object, Object)} method). If set,
     * only files/dirs which match are candidates for visiting.</dd>
     * <dt>nameFilter</dt><dd>A filter to perform on the name of traversed files/directories (using the {@link DefaultGroovyMethods#isCase(Object, Object)} method). If set,
     * only files/dirs which match are candidates for visiting. (Must not be set if 'filter' is set)</dd>
     * <dt>excludeFilter</dt><dd>A filter to perform on traversed files/directories (using the {@link DefaultGroovyMethods#isCase(Object, Object)} method).
     * If set, any candidates which match won't be visited.</dd>
     * <dt>excludeNameFilter</dt><dd>A filter to perform on the names of traversed files/directories (using the {@link DefaultGroovyMethods#isCase(Object, Object)} method).
     * If set, any candidates which match won't be visited. (Must not be set if 'excludeFilter' is set)</dd>
     * <dt>sort</dt><dd>A {@link groovy.lang.Closure} which if set causes the files and subdirectories for each directory to be processed in sorted order.
     * Note that even when processing only files, the order of visited subdirectories will be affected by this parameter.</dd>
     * </dl>
     * This example prints out file counts and size aggregates for groovy source files within a directory tree:
     * <pre>
     * def totalSize = 0
     * def count = 0
     * def sortByTypeThenName = { a, b ->
     *     a.isFile() != b.isFile() ? a.isFile() <=> b.isFile() : a.name <=> b.name
     * }
     * rootDir.traverse(
     *         type         : FILES,
     *         nameFilter   : ~/.*\.groovy/,
     *         preDir       : { if (it.name == '.svn') return SKIP_SUBTREE },
     *         postDir      : { println "Found $count files in $it.name totalling $totalSize bytes"
     *                         totalSize = 0; count = 0 },
     *         postRoot     : true
     *         sort         : sortByTypeThenName
     * ) {it -> totalSize += it.size(); count++ }
     * </pre>
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param options a Map of options to alter the traversal behavior
     * @param closure the Closure to invoke on each file/directory and optionally returning a {@link groovy.io.FileVisitResult} value
     *                which can be used to control subsequent processing
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory or illegal filter combinations are supplied
     * @see DefaultGroovyMethods#sort(java.util.Collection, groovy.lang.Closure)
     * @see groovy.io.FileVisitResult
     * @see groovy.io.FileType
     * @since 2.3.0
     */
    public static void traverse(final Path self, final Map<String, Object> options, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure)
            throws IOException {
        // throws FileNotFoundException, IllegalArgumentException {
        Number maxDepthNumber = DefaultGroovyMethods.asType(options.remove("maxDepth"), Number.class);
        int maxDepth = maxDepthNumber == null ? -1 : maxDepthNumber.intValue();
        Boolean visitRoot = DefaultGroovyMethods.asType(get(options, "visitRoot", false), Boolean.class);
        Boolean preRoot = DefaultGroovyMethods.asType(get(options, "preRoot", false), Boolean.class);
        Boolean postRoot = DefaultGroovyMethods.asType(get(options, "postRoot", false), Boolean.class);
        final Closure pre = (Closure) options.get("preDir");
        final Closure post = (Closure) options.get("postDir");
        final FileType type = (FileType) options.get("type");
        final Object filter = options.get("filter");
        final Object nameFilter = options.get("nameFilter");
        final Object excludeFilter = options.get("excludeFilter");
        final Object excludeNameFilter = options.get("excludeNameFilter");
        Object preResult = null;
        if (preRoot && pre != null) {
            preResult = pre.call(self);
        }
        if (preResult == FileVisitResult.TERMINATE ||
                preResult == FileVisitResult.SKIP_SUBTREE) return;

        FileVisitResult terminated = traverse(self, options, closure, maxDepth);

        if (type != FileType.FILES && visitRoot) {
            if (closure != null && notFiltered(self, filter, nameFilter, excludeFilter, excludeNameFilter)) {
                Object closureResult = closure.call(self);
                if (closureResult == FileVisitResult.TERMINATE) return;
            }
        }

        if (postRoot && post != null && terminated != FileVisitResult.TERMINATE) post.call(self);
    }

    private static boolean notFiltered(Path path, Object filter, Object nameFilter, Object excludeFilter, Object excludeNameFilter) {
        if (filter == null && nameFilter == null && excludeFilter == null && excludeNameFilter == null) return true;
        if (filter != null && nameFilter != null)
            throw new IllegalArgumentException("Can't set both 'filter' and 'nameFilter'");
        if (excludeFilter != null && excludeNameFilter != null)
            throw new IllegalArgumentException("Can't set both 'excludeFilter' and 'excludeNameFilter'");
        Object filterToUse = null;
        Object filterParam = null;
        if (filter != null) {
            filterToUse = filter;
            filterParam = path;
        } else if (nameFilter != null) {
            filterToUse = nameFilter;
            filterParam = path.getFileName().toString();
        }
        Object excludeFilterToUse = null;
        Object excludeParam = null;
        if (excludeFilter != null) {
            excludeFilterToUse = excludeFilter;
            excludeParam = path;
        } else if (excludeNameFilter != null) {
            excludeFilterToUse = excludeNameFilter;
            excludeParam = path.getFileName().toString();
        }
        final MetaClass filterMC = filterToUse == null ? null : InvokerHelper.getMetaClass(filterToUse);
        final MetaClass excludeMC = excludeFilterToUse == null ? null : InvokerHelper.getMetaClass(excludeFilterToUse);
        boolean included = filterToUse == null || DefaultTypeTransformation.castToBoolean(filterMC.invokeMethod(filterToUse, "isCase", filterParam));
        boolean excluded = excludeFilterToUse != null && DefaultTypeTransformation.castToBoolean(excludeMC.invokeMethod(excludeFilterToUse, "isCase", excludeParam));
        return included && !excluded;
    }

    /**
     * Processes each descendant file in this directory and any sub-directories.
     * Convenience method for {@link #traverse(Path, java.util.Map, groovy.lang.Closure)} when
     * no options to alter the traversal behavior are required.
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param closure the Closure to invoke on each file/directory and optionally returning a {@link groovy.io.FileVisitResult} value
     *                which can be used to control subsequent processing
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #traverse(Path, java.util.Map, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void traverse(final Path self, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure)
            throws IOException {
        traverse(self, new HashMap<String, Object>(), closure);
    }

    /**
     * Invokes the closure specified with key 'visit' in the options Map
     * for each descendant file in this directory tree. Convenience method
     * for {@link #traverse(Path, java.util.Map, groovy.lang.Closure)} allowing the 'visit' closure
     * to be included in the options Map rather than as a parameter.
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param options a Map of options to alter the traversal behavior
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory or illegal filter combinations are supplied
     * @see #traverse(Path, java.util.Map, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void traverse(final Path self, final Map<String, Object> options)
            throws IOException {
        final Closure visit = (Closure) options.remove("visit");
        traverse(self, options, visit);
    }

    private static FileVisitResult traverse(final Path self, final Map<String, Object> options, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure, final int maxDepth)
            throws IOException {
        checkDir(self);
        final Closure pre = (Closure) options.get("preDir");
        final Closure post = (Closure) options.get("postDir");
        final FileType type = (FileType) options.get("type");
        final Object filter = options.get("filter");
        final Object nameFilter = options.get("nameFilter");
        final Object excludeFilter = options.get("excludeFilter");
        final Object excludeNameFilter = options.get("excludeNameFilter");
        final Closure sort = (Closure) options.get("sort");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(self)) {
            final Iterator<Path> itr = stream.iterator();
            List<Path> files = new LinkedList<Path>();
            while (itr.hasNext()) {
                files.add(itr.next());
            }

            if (sort != null) files = DefaultGroovyMethods.sort((Iterable<Path>) files, sort);

            for (Path path : files) {
                if (Files.isDirectory(path)) {
                    if (type != FileType.FILES) {
                        if (closure != null && notFiltered(path, filter, nameFilter, excludeFilter, excludeNameFilter)) {
                            Object closureResult = closure.call(path);
                            if (closureResult == FileVisitResult.SKIP_SIBLINGS) break;
                            if (closureResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                        }
                    }
                    if (maxDepth != 0) {
                        Object preResult = null;
                        if (pre != null) {
                            preResult = pre.call(path);
                        }
                        if (preResult == FileVisitResult.SKIP_SIBLINGS) break;
                        if (preResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                        if (preResult != FileVisitResult.SKIP_SUBTREE) {
                            FileVisitResult terminated = traverse(path, options, closure, maxDepth - 1);
                            if (terminated == FileVisitResult.TERMINATE) return terminated;
                        }
                        Object postResult = null;
                        if (post != null) {
                            postResult = post.call(path);
                        }
                        if (postResult == FileVisitResult.SKIP_SIBLINGS) break;
                        if (postResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                    }
                } else if (type != FileType.DIRECTORIES) {
                    if (closure != null && notFiltered(path, filter, nameFilter, excludeFilter, excludeNameFilter)) {
                        Object closureResult = closure.call(path);
                        if (closureResult == FileVisitResult.SKIP_SIBLINGS) break;
                        if (closureResult == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE;
                    }
                }
            }

            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Processes each descendant file in this directory and any sub-directories.
     * Processing consists of calling <code>closure</code> passing it the current
     * file (which may be a normal file or subdirectory) and then if a subdirectory was encountered,
     * recursively processing the subdirectory.
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param closure a Closure
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #eachFileRecurse(Path, groovy.io.FileType, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachFileRecurse(Path self, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") Closure closure) throws IOException { // throws FileNotFoundException, IllegalArgumentException {
        eachFileRecurse(self, FileType.ANY, closure);
    }

    /**
     * Recursively processes each descendant subdirectory in this directory.
     * Processing consists of calling <code>closure</code> passing it the current
     * subdirectory and then recursively processing that subdirectory.
     * Regular files are ignored during traversal.
     *
     * @param self    a Path (that happens to be a folder/directory)
     * @param closure a closure
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #eachFileRecurse(Path, groovy.io.FileType, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachDirRecurse(final Path self, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure) throws IOException { //throws FileNotFoundException, IllegalArgumentException {
        eachFileRecurse(self, FileType.DIRECTORIES, closure);
    }

    /**
     * Invokes the closure for each file whose name (file.name) matches the given nameFilter in the given directory
     * - calling the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#isCase(Object, Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Both regular files and subdirectories may be candidates for matching depending
     * on the value of fileType.
     * <pre>
     * // collect names of files in baseDir matching supplied regex pattern
     * import static groovy.io.FileType.*
     * def names = []
     * baseDir.eachFileMatch FILES, ~/foo\d\.txt/, { names << it.name }
     * assert names == ['foo1.txt', 'foo2.txt']
     *
     * // remove all *.bak files in baseDir
     * baseDir.eachFileMatch FILES, ~/.*\.bak/, { Path bak -> bak.delete() }
     *
     * // print out files > 4K in size from baseDir
     * baseDir.eachFileMatch FILES, { new Path(baseDir, it).size() > 4096 }, { println "$it.name ${it.size()}" }
     * </pre>
     *
     * @param self       a Path (that happens to be a folder/directory)
     * @param fileType   whether normal files or directories or both should be processed
     * @param nameFilter the filter to perform on the name of the file/directory (using the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#isCase(Object, Object)} method)
     * @param closure    the closure to invoke
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @since 2.3.0
     */
    public static void eachFileMatch(final Path self, final FileType fileType, final Object nameFilter, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure) throws IOException {
        // throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(self)) {
            Iterator<Path> itr = stream.iterator();
            BooleanReturningMethodInvoker bmi = new BooleanReturningMethodInvoker("isCase");
            while (itr.hasNext()) {
                Path currentPath = itr.next();
                if ((fileType != FileType.FILES && Files.isDirectory(currentPath)) ||
                        (fileType != FileType.DIRECTORIES && Files.isRegularFile(currentPath))) {
                    if (bmi.invoke(nameFilter, currentPath.getFileName().toString()))
                        closure.call(currentPath);
                }
            }
        }
    }

    /**
     * Invokes the closure for each file whose name (file.name) matches the given nameFilter in the given directory
     * - calling the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#isCase(Object, Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Both regular files and subdirectories are matched.
     *
     * @param self       a Path (that happens to be a folder/directory)
     * @param nameFilter the nameFilter to perform on the name of the file (using the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#isCase(Object, Object)} method)
     * @param closure    the closure to invoke
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #eachFileMatch(Path, groovy.io.FileType, Object, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachFileMatch(final Path self, final Object nameFilter, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure) throws IOException {
        // throws FileNotFoundException, IllegalArgumentException {
        eachFileMatch(self, FileType.ANY, nameFilter, closure);
    }

    /**
     * Invokes the closure for each subdirectory whose name (dir.name) matches the given nameFilter in the given directory
     * - calling the {@link DefaultGroovyMethods#isCase(java.lang.Object, java.lang.Object)} method to determine if a match occurs.  This method can be used
     * with different kinds of filters like regular expressions, classes, ranges etc.
     * Only subdirectories are matched; regular files are ignored.
     *
     * @param self       a Path (that happens to be a folder/directory)
     * @param nameFilter the nameFilter to perform on the name of the directory (using the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#isCase(Object, Object)} method)
     * @param closure    the closure to invoke
     * @throws java.io.FileNotFoundException if the given directory does not exist
     * @throws IllegalArgumentException      if the provided Path object does not represent a directory
     * @see #eachFileMatch(Path, groovy.io.FileType, Object, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachDirMatch(final Path self, final Object nameFilter, @ClosureParams(value = SimpleType.class, options = "java.nio.file.Path") final Closure closure) throws IOException {  // throws FileNotFoundException, IllegalArgumentException {
        eachFileMatch(self, FileType.DIRECTORIES, nameFilter, closure);
    }

    /**
     * Deletes a directory with all contained files and subdirectories.
     * <p>The method returns
     * <ul>
     * <li>true, when deletion was successful</li>
     * <li>true, when it is called for a non existing directory</li>
     * <li>false, when it is called for a file which isn't a directory</li>
     * <li>false, when directory couldn't be deleted</li>
     * </ul>
     * </p>
     *
     * @param self a Path
     * @return true if the file doesn't exist or deletion was successful
     * @since 2.3.0
     */
    public static boolean deleteDir(final Path self) {
        if (!Files.exists(self))
            return true;

        if (!Files.isDirectory(self))
            return false;

        // delete contained files
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(self)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    if (!deleteDir(path)) {
                        return false;
                    }
                } else {
                    Files.delete(path);
                }
            }

            // now delete directory itself
            Files.delete(self);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Renames a file.
     *
     * @param self        a Path
     * @param newPathName The new pathname for the named file
     * @return <code>true</code> if and only if the renaming succeeded;
     * <code>false</code> otherwise
     * @since 2.3.0
     */
    public static boolean renameTo(final Path self, String newPathName) {
        try {
            Files.move(self, Paths.get(newPathName));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Renames a file.
     *
     * @param self        a Path
     * @param newPathName The new target path specified as a URI object
     * @return <code>true</code> if and only if the renaming succeeded;
     * <code>false</code> otherwise
     * @since 2.3.0
     */
    public static boolean renameTo(final Path self, URI newPathName) {
        try {
            Files.move(self, Paths.get(newPathName));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Converts this Path to a {@link groovy.lang.Writable}.
     *
     * @param self a Path
     * @return a Path which wraps the input file and which implements Writable
     * @since 2.3.0
     */
    public static Path asWritable(Path self) {
        return new WritablePath(self);
    }

    /**
     * Converts this Path to a {@link groovy.lang.Writable} or delegates to default
     * {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#asType(Object, Class)}.
     *
     * @param path a Path
     * @param c    the desired class
     * @return the converted object
     * @since 2.3.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(Path path, Class<T> c) {
        if (c == Writable.class) {
            return (T) asWritable(path);
        }
        return DefaultGroovyMethods.asType((Object) path, c);
    }

    /**
     * Allows a file to return a Writable implementation that can output itself
     * to a Writer stream.
     *
     * @param self     a Path
     * @param encoding the encoding to be used when reading the file's contents
     * @return Path which wraps the input file and which implements Writable
     * @since 2.3.0
     */
    public static Path asWritable(Path self, String encoding) {
        return new WritablePath(self, encoding);
    }

    /**
     * Create a buffered reader for this file.
     *
     * @param self a Path
     * @return a BufferedReader
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static BufferedReader newReader(Path self) throws IOException {
        return Files.newBufferedReader(self, Charset.defaultCharset());
    }

    /**
     * Create a buffered reader for this file, using the specified
     * charset as the encoding.
     *
     * @param self    a Path
     * @param charset the charset for this Path
     * @return a BufferedReader
     * @throws java.io.FileNotFoundException        if the Path was not found
     * @throws java.io.UnsupportedEncodingException if the encoding specified is not supported
     * @since 2.3.0
     */
    public static BufferedReader newReader(Path self, String charset) throws IOException {
        return Files.newBufferedReader(self, Charset.forName(charset));
    }

    /**
     * Create a new BufferedReader for this file and then
     * passes it into the closure, ensuring the reader is closed after the
     * closure returns.
     *
     * @param self    a file object
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withReader(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.Reader") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withReader(newReader(self), closure);
    }

    /**
     * Create a new BufferedReader for this file using the specified charset and then
     * passes it into the closure, ensuring the reader is closed after the
     * closure returns.  The writer will use the given charset encoding,
     * but will not write a BOM.
     *
     * @param self    a file object
     * @param charset the charset for this input stream
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withReader(Path self, String charset, @ClosureParams(value = SimpleType.class, options = "java.io.Reader") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withReader(newReader(self, charset), closure);
    }

    /**
     * Create a buffered output stream for this file.
     *
     * @param self a file object
     * @return the created OutputStream
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static BufferedOutputStream newOutputStream(Path self) throws IOException {
        return new BufferedOutputStream(Files.newOutputStream(self));
    }

    /**
     * Creates a new data output stream for this file.
     *
     * @param self a file object
     * @return the created DataOutputStream
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static DataOutputStream newDataOutputStream(Path self) throws IOException {
        return new DataOutputStream(Files.newOutputStream(self));
    }

    /**
     * Creates a new OutputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static Object withOutputStream(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.OutputStream") Closure closure) throws IOException {
        return IOGroovyMethods.withStream(newOutputStream(self), closure);
    }

    /**
     * Create a new InputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static Object withInputStream(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.InputStream") Closure closure) throws IOException {
        return IOGroovyMethods.withStream(newInputStream(self), closure);
    }

    /**
     * Create a new DataOutputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withStream(java.io.OutputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T withDataOutputStream(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.DataOutputStream") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withStream(newDataOutputStream(self), closure);
    }

    /**
     * Create a new DataInputStream for this file and passes it into the closure.
     * This method ensures the stream is closed after the closure returns.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withStream(java.io.InputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static <T> T withDataInputStream(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.DataInputStream") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withStream(newDataInputStream(self), closure);
    }

    /**
     * Create a buffered writer for this file.
     *
     * @param self a Path
     * @return a BufferedWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static BufferedWriter newWriter(Path self) throws IOException {
        return Files.newBufferedWriter(self, Charset.defaultCharset());
    }

    /**
     * Creates a buffered writer for this file, optionally appending to the
     * existing file content.
     *
     * @param self   a Path
     * @param append true if data should be appended to the file
     * @return a BufferedWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static BufferedWriter newWriter(Path self, boolean append) throws IOException {
        if (append) {
            return Files.newBufferedWriter(self, Charset.defaultCharset(), CREATE, APPEND);
        }
        return Files.newBufferedWriter(self, Charset.defaultCharset());
    }

    /**
     * Helper method to create a buffered writer for a file without writing a BOM.
     *
     * @param self    a Path
     * @param charset the name of the encoding used to write in this file
     * @param append  true if in append mode
     * @return a BufferedWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static BufferedWriter newWriter(Path self, String charset, boolean append) throws IOException {
        return newWriter(self, charset, append, false);
    }

    /**
     * Helper method to create a buffered writer for a file.  If the given
     * charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias), the
     * requisite byte order mark is written to the stream before the writer
     * is returned.
     *
     * @param self     a Path
     * @param charset  the name of the encoding used to write in this file
     * @param append   true if in append mode
     * @param writeBom whether to write a BOM
     * @return a BufferedWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static BufferedWriter newWriter(Path self, String charset, boolean append, boolean writeBom) throws IOException {
        boolean shouldWriteBom = writeBom && !self.toFile().exists();
        if (append) {
            BufferedWriter writer = Files.newBufferedWriter(self, Charset.forName(charset), CREATE, APPEND);
            if (shouldWriteBom) {
                IOGroovyMethods.writeUTF16BomIfRequired(writer, charset);
            }
            return writer;
        } else {
            OutputStream out = Files.newOutputStream(self);
            if (shouldWriteBom) {
                IOGroovyMethods.writeUTF16BomIfRequired(out, charset);
            }
            return new BufferedWriter(new OutputStreamWriter(out, Charset.forName(charset)));
        }
    }

    /**
     * Creates a buffered writer for this file without writing a BOM, writing data using the given
     * encoding.
     *
     * @param self    a Path
     * @param charset the name of the encoding used to write in this file
     * @return a BufferedWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static BufferedWriter newWriter(Path self, String charset) throws IOException {
        return newWriter(self, charset, false);
    }

    /**
     * Creates a new BufferedWriter for this file, passes it to the closure, and
     * ensures the stream is flushed and closed after the closure returns.
     * The writer will not write a BOM.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withWriter(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.Writer") Closure<T> closure) throws IOException {
        return withWriter(self, Charset.defaultCharset().name(), closure);
    }

    /**
     * Creates a new BufferedWriter for this file, passes it to the closure, and
     * ensures the stream is flushed and closed after the closure returns.
     * The writer will use the given charset encoding, but will not write a BOM.
     *
     * @param self    a Path
     * @param charset the charset used
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withWriter(Path self, String charset, @ClosureParams(value = SimpleType.class, options = "java.io.Writer") Closure<T> closure) throws IOException {
        return withWriter(self, charset, false, closure);
    }

    /**
     * Creates a new BufferedWriter for this file, passes it to the closure, and
     * ensures the stream is flushed and closed after the closure returns.
     * The writer will use the given charset encoding.  If the given charset is
     * "UTF-16BE" or "UTF-16LE" (or an equivalent alias), <code>writeBom</code>
     * is <code>true</code>, and the file doesn't already exist, the requisite
     * byte order mark is written to the stream when the writer is created.
     *
     * @param self     a Path
     * @param charset  the charset used
     * @param writeBom whether to write the BOM
     * @param closure  a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static <T> T withWriter(Path self, String charset, boolean writeBom, @ClosureParams(value = SimpleType.class, options = "java.io.Writer") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withWriter(newWriter(self, charset, false, writeBom), closure);
    }

    /**
     * Create a new BufferedWriter which will append to this
     * file.  The writer is passed to the closure and will be closed before
     * this method returns.  The writer will use the given charset encoding,
     * but will not write a BOM.
     *
     * @param self    a Path
     * @param charset the charset used
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withWriterAppend(Path self, String charset, @ClosureParams(value = SimpleType.class, options = "java.io.Writer") Closure<T> closure) throws IOException {
        return withWriterAppend(self, charset, false, closure);
    }

    /**
     * Create a new BufferedWriter which will append to this
     * file.  The writer is passed to the closure and will be closed before
     * this method returns.  The writer will use the given charset encoding.
     * If the given charset is "UTF-16BE" or "UTF-16LE" (or an equivalent alias),
     * <code>writeBom</code> is <code>true</code>, and the file doesn't already exist,
     * the requisite byte order mark is written to the stream when the writer is created.
     *
     * @param self     a Path
     * @param charset  the charset used
     * @param writeBom whether to write the BOM
     * @param closure  a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.5.0
     */
    public static <T> T withWriterAppend(Path self, String charset, boolean writeBom, @ClosureParams(value = SimpleType.class, options = "java.io.Writer") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withWriter(newWriter(self, charset, true, writeBom), closure);
    }

    /**
     * Create a new BufferedWriter for this file in append mode.  The writer
     * is passed to the closure and is closed after the closure returns.
     * The writer will not write a BOM.
     *
     * @param self    a Path
     * @param closure a closure
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withWriterAppend(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.Writer") Closure<T> closure) throws IOException {
        return withWriterAppend(self, Charset.defaultCharset().name(), closure);
    }

    /**
     * Create a new PrintWriter for this file.
     *
     * @param self a Path
     * @return the created PrintWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static PrintWriter newPrintWriter(Path self) throws IOException {
        return new GroovyPrintWriter(newWriter(self));
    }

    /**
     * Create a new PrintWriter for this file, using specified
     * charset.
     *
     * @param self    a Path
     * @param charset the charset
     * @return a PrintWriter
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static PrintWriter newPrintWriter(Path self, String charset) throws IOException {
        return new GroovyPrintWriter(newWriter(self, charset));
    }

    /**
     * Create a new PrintWriter for this file which is then
     * passed it into the given closure.  This method ensures its the writer
     * is closed after the closure returns.
     *
     * @param self    a Path
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withPrintWriter(Path self, @ClosureParams(value = SimpleType.class, options = "java.io.PrintWriter") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withWriter(newPrintWriter(self), closure);
    }

    /**
     * Create a new PrintWriter with a specified charset for
     * this file.  The writer is passed to the closure, and will be closed
     * before this method returns.
     *
     * @param self    a Path
     * @param charset the charset
     * @param closure the closure to invoke with the PrintWriter
     * @return the value returned by the closure
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static <T> T withPrintWriter(Path self, String charset, @ClosureParams(value = SimpleType.class, options = "java.io.PrintWriter") Closure<T> closure) throws IOException {
        return IOGroovyMethods.withWriter(newPrintWriter(self, charset), closure);
    }

    /**
     * Creates a buffered input stream for this file.
     *
     * @param self a Path
     * @return a BufferedInputStream of the file
     * @throws java.io.FileNotFoundException if the file is not found.
     * @since 2.3.0
     */
    public static BufferedInputStream newInputStream(Path self) throws IOException { // throws FileNotFoundException {
        return new BufferedInputStream(Files.newInputStream(self));
    }

    /**
     * Create a data input stream for this file
     *
     * @param self a Path
     * @return a DataInputStream of the file
     * @throws java.io.FileNotFoundException if the file is not found.
     * @since 2.3.0
     */
    public static DataInputStream newDataInputStream(Path self) throws IOException { // throws FileNotFoundException {
        return new DataInputStream(Files.newInputStream(self));
    }

    /**
     * Traverse through each byte of this Path
     *
     * @param self    a Path
     * @param closure a closure
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#eachByte(java.io.InputStream, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachByte(Path self, @ClosureParams(value = SimpleType.class, options = "byte") Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        IOGroovyMethods.eachByte(is, closure);
    }

    /**
     * Traverse through the bytes of this Path, bufferLen bytes at a time.
     *
     * @param self      a Path
     * @param bufferLen the length of the buffer to use.
     * @param closure   a 2 parameter closure which is passed the byte[] and a number of bytes successfully read.
     * @throws java.io.IOException if an IOException occurs.
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#eachByte(java.io.InputStream, int, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void eachByte(Path self, int bufferLen, @ClosureParams(value = FromString.class, options = "byte[],Integer") Closure closure) throws IOException {
        BufferedInputStream is = newInputStream(self);
        IOGroovyMethods.eachByte(is, bufferLen, closure);
    }

    /**
     * Filters the lines of a Path and creates a Writable in return to
     * stream the filtered lines.
     *
     * @param self    a Path
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws java.io.IOException if <code>self</code> is not readable
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#filterLine(java.io.Reader, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static Writable filterLine(Path self, @ClosureParams(value = SimpleType.class, options = "java.lang.String") Closure closure) throws IOException {
        return IOGroovyMethods.filterLine(newReader(self), closure);
    }

    /**
     * Filters the lines of a Path and creates a Writable in return to
     * stream the filtered lines.
     *
     * @param self    a Path
     * @param charset opens the file with a specified charset
     * @param closure a closure which returns a boolean indicating to filter
     *                the line or not
     * @return a Writable closure
     * @throws java.io.IOException if an IOException occurs
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#filterLine(java.io.Reader, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static Writable filterLine(Path self, String charset, @ClosureParams(value = SimpleType.class, options = "java.lang.String") Closure closure) throws IOException {
        return IOGroovyMethods.filterLine(newReader(self, charset), closure);
    }

    /**
     * Filter the lines from this Path, and write them to the given writer based
     * on the given closure predicate.
     *
     * @param self    a Path
     * @param writer  a writer destination to write filtered lines to
     * @param closure a closure which takes each line as a parameter and returns
     *                <code>true</code> if the line should be written to this writer.
     * @throws java.io.IOException if <code>self</code> is not readable
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void filterLine(Path self, Writer writer, @ClosureParams(value = SimpleType.class, options = "java.lang.String") Closure closure) throws IOException {
        IOGroovyMethods.filterLine(newReader(self), writer, closure);
    }

    /**
     * Filter the lines from this Path, and write them to the given writer based
     * on the given closure predicate.
     *
     * @param self    a Path
     * @param writer  a writer destination to write filtered lines to
     * @param charset opens the file with a specified charset
     * @param closure a closure which takes each line as a parameter and returns
     *                <code>true</code> if the line should be written to this writer.
     * @throws java.io.IOException if an IO error occurs
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#filterLine(java.io.Reader, java.io.Writer, groovy.lang.Closure)
     * @since 2.3.0
     */
    public static void filterLine(Path self, Writer writer, String charset, @ClosureParams(value = SimpleType.class, options = "java.lang.String") Closure closure) throws IOException {
        IOGroovyMethods.filterLine(newReader(self, charset), writer, closure);
    }

    /**
     * Reads the content of the file into a byte array.
     *
     * @param self a Path
     * @return a byte array with the contents of the file.
     * @throws java.io.IOException if an IOException occurs.
     * @since 2.3.0
     */
    public static byte[] readBytes(Path self) throws IOException {
        return Files.readAllBytes(self);
    }

    /**
     * #deprecated use the variant in IOGroovyMethods
     *
     * @see org.codehaus.groovy.runtime.IOGroovyMethods#withCloseable(java.io.Closeable, groovy.lang.Closure)
     * @since 2.3.0
     */
    @Deprecated
    public static <T> T withCloseable(Closeable self, @ClosureParams(value = SimpleType.class, options = "java.io.Closeable") Closure<T> action) throws IOException {
        return IOGroovyMethods.withCloseable(self, action);
    }

}
