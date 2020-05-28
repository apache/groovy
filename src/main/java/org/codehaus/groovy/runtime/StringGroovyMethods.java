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

import groovy.lang.Closure;
import groovy.lang.EmptyRange;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.lang.Range;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FromString;
import groovy.transform.stc.PickFirstResolver;
import org.apache.groovy.io.StringBuilderWriter;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.util.CharSequenceReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.codehaus.groovy.ast.tools.ClosureUtils.hasSingleCharacterArg;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.callClosureForLine;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.each;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.join;

/**
 * This class defines new groovy methods which appear on String-related JDK
 * classes (String, CharSequence, Matcher) inside the Groovy environment.
 * Static methods are used with the first parameter being the destination class,
 * e.g. <code>public static String reverse(String self)</code>
 * provides a <code>reverse()</code> method for <code>String</code>.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 */
public class StringGroovyMethods extends DefaultGroovyMethodsSupport {

    static String lineSeparator;

    /**
     * Coerces a CharSequence to a boolean value. A sequence string is coerced to
     * {@code false} if it is of length 0, and to {@code true} otherwise.
     *
     * @param chars the character sequence
     * @return the boolean value
     *
     * @since 1.7.0
     */
    public static boolean asBoolean(final CharSequence chars) {
        if (chars != null) {
            return chars.length() > 0;
        }
        return false;
    }

    /**
     * Coerces a Matcher instance to a boolean value.
     *
     * @param matcher the matcher
     * @return the boolean value
     *
     * @since 1.7.0
     */
    public static boolean asBoolean(final Matcher matcher) {
        if (matcher != null) {
            RegexSupport.setLastMatcher(matcher);
            return matcher.find(0); //GROOVY-8855
        }
        return false;
    }

    /**
     * Provides a method to perform custom 'dynamic' type conversion
     * to the given class using the {@code as} operator.
     *
     * @param self a CharSequence
     * @param c    the desired class
     * @return the converted object
     *
     * @since 1.8.2
     *
     * @see #asType(String,Class)
     */
    public static <T> T asType(final CharSequence self, final Class<T> c) {
        return asType(self.toString(), c);
    }

    /**
     * Converts the GString to a File, or delegates to the default
     * {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#asType(Object,Class)}.
     *
     * @param self a GString
     * @param c    the desired class
     * @return the converted object
     *
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(final GString self, final Class<T> c) {
        if (c == File.class) {
            return (T) new File(self.toString());
        } else if (Number.class.isAssignableFrom(c) || c.isPrimitive()) {
            return asType(self.toString(), c);
        }
        return DefaultGroovyMethods.asType((Object) self, c);
    }

    /**
     * Provides a method to perform custom 'dynamic' type conversion
     * to the given class using the {@code as} operator.
     * <p>
     * <strong>Example:</strong> {@code '123' as Double}
     * <p>
     * By default, the following types are supported:
     * <ul>
     * <li>List</li>
     * <li>BigDecimal</li>
     * <li>BigInteger</li>
     * <li>Long</li>
     * <li>Integer</li>
     * <li>Short</li>
     * <li>Byte</li>
     * <li>Character</li>
     * <li>Double</li>
     * <li>Float</li>
     * <li>File</li>
     * <li>Subclasses of Enum</li>
     * </ul>
     * If any other type is given, the call is delegated to
     * {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#asType(Object,Class)}.
     *
     * @param self a String
     * @param c    the desired class
     * @return the converted object
     *
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(final String self, final Class<T> c) {
        if (c == List.class) {
            return (T) toList((CharSequence)self);
        } else if (c == BigDecimal.class) {
            return (T) toBigDecimal((CharSequence)self);
        } else if (c == BigInteger.class) {
            return (T) toBigInteger((CharSequence)self);
        } else if (c == Long.class || c == Long.TYPE) {
            return (T) toLong((CharSequence)self);
        } else if (c == Integer.class || c == Integer.TYPE) {
            return (T) toInteger((CharSequence)self);
        } else if (c == Short.class || c == Short.TYPE) {
            return (T) toShort((CharSequence)self);
        } else if (c == Byte.class || c == Byte.TYPE) {
            return (T) Byte.valueOf(self.trim());
        } else if (c == Character.class || c == Character.TYPE) {
            return (T) toCharacter(self);
        } else if (c == Double.class || c == Double.TYPE) {
            return (T) toDouble((CharSequence)self);
        } else if (c == Float.class || c == Float.TYPE) {
            return (T) toFloat((CharSequence)self);
        } else if (c == File.class) {
            return (T) new File(self);
        } else if (c.isEnum()) {
            return (T) InvokerHelper.invokeMethod(c, "valueOf", new Object[]{ self });
        }
        return DefaultGroovyMethods.asType((Object) self, c);
    }

    /**
     * Turns a CharSequence into a regular expression Pattern.
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     *
     * @since 1.8.2
     */
    public static Pattern bitwiseNegate(final CharSequence self) {
        return Pattern.compile(self.toString());
    }

    /**
     * Convenience method to uncapitalize the first letter of a CharSequence
     * (typically the first letter of a word). Example usage:
     * <pre class="groovyTestCase">
     * assert 'H'.uncapitalize() == 'h'
     * assert 'Hello'.uncapitalize() == 'hello'
     * assert 'Hello world'.uncapitalize() == 'hello world'
     * assert 'Hello World'.uncapitalize() == 'hello World'
     * assert 'hello world' == 'Hello World'.split(' ').collect{ it.uncapitalize() }.join(' ')
     * </pre>
     *
     * @param self The CharSequence to uncapitalize
     * @return A String containing the uncapitalized toString() of the CharSequence
     *
     * @since 2.4.8
     */
    public static String uncapitalize(final CharSequence self) {
        if (self.length() == 0) return "";
        return "" + Character.toLowerCase(self.charAt(0)) + self.subSequence(1, self.length());
    }

    /**
     * Convenience method to capitalize the first letter of a CharSequence
     * (typically the first letter of a word). Example usage:
     * <pre class="groovyTestCase">
     * assert 'h'.capitalize() == 'H'
     * assert 'hello'.capitalize() == 'Hello'
     * assert 'hello world'.capitalize() == 'Hello world'
     * assert 'Hello World' ==
     *     'hello world'.split(' ').collect{ it.capitalize() }.join(' ')
     * </pre>
     *
     * @param self The CharSequence to capitalize
     * @return A String containing the capitalized toString() of the CharSequence
     *
     * @since 1.8.2
     */
    public static String capitalize(final CharSequence self) {
        if (self.length() == 0) return "";
        return "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
    }

    /**
     * Pads a CharSequence to a minimum length specified by {@code numberOfChars} by adding the space character around it as many times as needed so that it remains centered.
     * <p>
     * If the String is already the same size or bigger than the target {@code numberOfChars}, then the original String is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println '|' + it.center(6) + '|' }
     * </pre>
     * will produce output like:
     * <pre>
     * |  A   |
     * |  BB  |
     * | CCC  |
     * | DDDD |
     * </pre>
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the result
     * @return the centered toString() of this CharSequence with padded characters around it
     *
     * @since 1.8.2
     */
    public static String center(final CharSequence self, final Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

    /**
     * Pad a CharSequence to a minimum length specified by {@code numberOfChars}, appending the supplied padding CharSequence around the original as many times as needed keeping it centered.
     *
     * If the String is already the same size or bigger than the target {@code numberOfChars}, then the original String is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println '|' + it.center(6, '+') + '|' }
     * </pre>
     * will produce output like:
     * <pre>
     * |++A+++|
     * |++BB++|
     * |+CCC++|
     * |+DDDD+|
     * </pre>
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the centered toString() of this CharSequence with padded characters around it
     *
     * @since 1.8.2
     */
    public static String center(final CharSequence self, final Number numberOfChars, final CharSequence padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self.toString();
        } else {
            int charsToAdd = numChars - self.length();
            String semiPad = charsToAdd % 2 == 1 ?
                    getPadding(padding, charsToAdd / 2 + 1) :
                    getPadding(padding, charsToAdd / 2);
            if (charsToAdd % 2 == 0)
                return semiPad + self + semiPad;
            else
                return semiPad.substring(0, charsToAdd / 2) + self + semiPad;
        }
    }

    /**
     * Provides an implementation of contains() like {@link java.util.Collection#contains(Object)} to make CharSequences more polymorphic.
     *
     * @param self a CharSequence
     * @param text the CharSequence to look for
     * @return true if this CharSequence contains the given text
     *
     * @since 1.8.2
     */
    public static boolean contains(final CharSequence self, final CharSequence text) {
        int idx = self.toString().indexOf(text.toString());
        return idx >= 0;
    }

    /**
     * Counts the number of occurrences of a sub CharSequence.
     *
     * @param self a CharSequence
     * @param text a sub CharSequence
     * @return the number of occurrences of the given CharSequence inside this CharSequence
     * @since 1.8.2
     */
    public static int count(final CharSequence self, final CharSequence text) {
        int answer = 0;
        for (int idx = 0; true; idx += 1) {
            idx = self.toString().indexOf(text.toString(), idx);
            // break once idx goes to -1 or for case of empty string once
            // we get to the end to avoid JDK library bug (see GROOVY-5858)
            if (idx < answer) break;
            answer += 1;
        }
        return answer;
    }

    /**
     * Return a CharSequence with lines (separated by LF, CR/LF, or CR)
     * terminated by the platform specific line separator.
     *
     * @param self a CharSequence object
     * @return the denormalized toString() of this CharSequence
     *
     * @since 1.8.2
     */
    public static String denormalize(final CharSequence self) {
        // Don't do this in static initializer because we may never be needed.
        // TODO: Put this lineSeparator property somewhere everyone can use it.
        if (lineSeparator == null) {
            final Writer sw = new StringBuilderWriter(2);
            // use BufferedWriter rather than System.getProperty because it has
            // the security manager rigamarole to deal with the possible exception
            try (final BufferedWriter bw = new BufferedWriter(sw)) {
                bw.newLine();
                bw.flush();
                lineSeparator = sw.toString();
            } catch (IOException ioe) {
                // This shouldn't happen, but this is the same default used by
                // BufferedWriter on a security exception.
                lineSeparator = "\n";
            }
        }

        final int len = self.length();

        if (len < 1) {
            return self.toString();
        }

        final StringBuilder sb = new StringBuilder((110 * len) / 100);

        int i = 0;

        // GROOVY-7873: GString calls toString() on each invocation of CharSequence methods such
        // as charAt which is very expensive for large GStrings.
        CharSequence cs = (self instanceof GString) ? self.toString() : self;
        while (i < len) {
            final char ch = cs.charAt(i++);

            switch (ch) {
                case '\r':
                    sb.append(lineSeparator);

                    // Eat the following LF if any.
                    if ((i < len) && (cs.charAt(i) == '\n')) {
                        ++i;
                    }

                    break;

                case '\n':
                    sb.append(lineSeparator);
                    break;

                default:
                    sb.append(ch);
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * Drops the given number of chars from the head of this CharSequence
     * if they are available.
     * <pre class="groovyTestCase">
     *     def text = "Groovy"
     *     assert text.drop( 0 ) == 'Groovy'
     *     assert text.drop( 2 ) == 'oovy'
     *     assert text.drop( 7 ) == ''
     * </pre>
     *
     * @param self the original CharSequence
     * @param num the number of characters to drop from this String
     * @return a CharSequence consisting of all characters except the first {@code num} ones,
     *         or else an empty String, if this CharSequence has less than {@code num} characters.
     *
     * @since 1.8.1
     */
    public static CharSequence drop(final CharSequence self, final int num) {
        if( num <= 0 ) {
            return self ;
        }
        if( self.length() <= num ) {
            return self.subSequence( 0, 0 ) ;
        }
        return self.subSequence(num, self.length()) ;
    }

    /**
     * A GString variant of the equivalent CharSequence method.
     *
     * @param self the original GString
     * @param num the number of characters to drop from this GString
     * @return a String consisting of all characters except the first {@code num} ones,
     *         or else an empty String, if the toString() of this GString has less than {@code num} characters.
     *
     * @since 2.3.7
     *
     * @see #drop(String,int)
     */
    public static String drop(final GString self, final int num) {
        return drop(self.toString(), num);
    }

    /**
     * A String variant of the equivalent CharSequence method.
     *
     * @param self the original String
     * @param num the number of characters to drop from this String
     * @return a String consisting of all characters except the first {@code num} ones,
     *         or else an empty String, if the String has less than {@code num} characters.
     *
     * @since 2.5.5
     *
     * @see #drop(CharSequence,int)
     */
    public static String drop(final String self, final int num) {
        return (String) drop((CharSequence) self, num);
    }

    /**
     * Creates a suffix of the given CharSequence by dropping as many characters as possible from the
     * front of the original CharSequence such that calling the given closure condition evaluates to
     * true when passed each of the dropped characters.
     * <p>
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.dropWhile{ false } == 'Groovy'
     * assert text.dropWhile{ true } == ''
     * assert text.dropWhile{ it {@code <} 'Z' } == 'roovy'
     * assert text.dropWhile{ it != 'v' } == 'vy'
     * </pre>
     *
     * @param self      the original CharSequence
     * @param condition the closure that while continuously evaluating to true will cause us to drop elements from
     *                  the front of the original CharSequence
     * @return the shortest suffix of the given CharSequence such that the given closure condition
     *         evaluates to true for each element dropped from the front of the CharSequence
     *
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    public static String dropWhile(final CharSequence self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure condition) {
        Iterator selfIter = hasSingleCharacterArg(condition) ? new CharacterIterator(self) : new StringIterator(self);
        return join(DefaultGroovyMethods.dropWhile(selfIter, condition), "");
    }

    @Deprecated // for binary compatibility only
    public static CharSequence dropWhile$$bridge(final CharSequence self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure condition) {
        return dropWhile(self, condition);
    }

    /**
     * A GString variant of the equivalent CharSequence method.
     *
     * @param self      the original GString
     * @param condition the closure that while continuously evaluating to true will cause us to drop elements from
     *                  the front of the original GString
     * @return the shortest suffix of the given GString such that the given closure condition
     *         evaluates to true for each element dropped from the front of the CharSequence
     *
     * @since 2.3.7
     *
     * @see #dropWhile(CharSequence,Closure)
     */
    public static String dropWhile(final GString self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure condition) {
        return dropWhile(self.toString(), condition);
    }

    private static final class CharacterIterator implements Iterator<Character> {
        private final CharSequence delegate;
        private final int length;
        private int index;

        public CharacterIterator(final CharSequence delegate) {
            this.delegate = delegate;
            length = delegate.length();
        }

        public boolean hasNext() {
            return index < length;
        }

        public Character next() {
            return delegate.charAt(index++);
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for CharSequence iterators");
        }
    }

    private static final class StringIterator implements Iterator<String> {
        private final CharSequence delegate;
        private final int length;
        private int index;

        public StringIterator(final CharSequence delegate) {
            this.delegate = delegate;
            length = delegate.length();
        }

        public boolean hasNext() {
            return index < length;
        }

        public String next() {
            return Character.toString(delegate.charAt(index++));
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported for CharSequence iterators");
        }
    }

    private static final class LineIterable implements Iterable<String> {
        private final CharSequence delegate;

        public LineIterable(final CharSequence cs) {
            // GROOVY-7873: GString calls toString() on each invocation of CharSequence methods such
            // as charAt which is very expensive for large GStrings.
            this.delegate = (cs instanceof GString) ? cs.toString() : cs;
        }

        @Override
        public Iterator<String> iterator() {
            return IOGroovyMethods.iterator(new CharSequenceReader(delegate));
        }
    }

    /**
     * Iterates through this CharSequence line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a CharSequence
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     *
     * @since 1.8.2
     */
    public static <T> T eachLine(final CharSequence self, @ClosureParams(value=FromString.class, options={"String","String,Integer"}) final Closure<T> closure) throws IOException {
        return eachLine((CharSequence)self.toString(), 0, closure);
    }

    /**
     * Iterates through this CharSequence line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a CharSequence
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     *
     * @since 1.8.2
     */
    public static <T> T eachLine(final CharSequence self, final int firstLine, @ClosureParams(value=FromString.class, options={"String","String,Integer"}) final Closure<T> closure) throws IOException {
        int count = firstLine;
        T result = null;
        for (String line : new LineIterable(self)) {
            result = callClosureForLine(closure, line, count);
            count += 1;
        }
        return result;
    }

    /**
     * Iterates through this String a character at a time collecting either the
     * original character or a transformed replacement String. The {@code transform}
     * Closure should return {@code null} to indicate that no transformation is
     * required for the given character.
     * <p>
     * <pre class="groovyTestCase">
     * assert "Groovy".collectReplacements{ it == 'o' ? '_O_' : null } == 'Gr_O__O_vy'
     * assert "Groovy".collectReplacements{ it.equalsIgnoreCase('O') ? '_O_' : null } == 'Gr_O__O_vy'
     * assert "Groovy".collectReplacements{ char c {@code ->} c == 'o' ? '_O_' : null } == 'Gr_O__O_vy'
     * assert "Groovy".collectReplacements{ Character c {@code ->} c == 'o' ? '_O_' : null } == 'Gr_O__O_vy'
     * assert "B&amp;W".collectReplacements{ {@code it == '&' ? '&' : null} } == 'B&amp;W'
     * </pre>
     *
     * @param self the original String
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding replacements
     *         as determined by the {@code transform} Closure.
     *
     * @since 2.1.0
     */
    public static String collectReplacements(final String self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure<String> transform) {
        if (self == null) return self;

        StringBuilder sb = null; // lazy create for edge-case efficiency
        for (int i = 0, len = self.length(); i < len; i++) {
            final char ch = self.charAt(i);
            final String replacement = transform.call(hasSingleCharacterArg(transform) ? ch : Character.toString(ch));

            if (replacement != null) {
                // output differs from input; we write to our local buffer
                if (sb == null) {
                    sb = new StringBuilder((int) (1.1 * len));
                    sb.append(self, 0, i);
                }
                sb.append(replacement);
            } else if (sb != null) {
                // earlier output differs from input; we write to our local buffer
                sb.append(ch);
            }
        }

        return sb == null ? self : sb.toString();
    }

    /**
     * Processes each regex group matched substring of the given CharSequence. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source CharSequence
     * @param regex   a Regex CharSequence
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source CharSequence
     *
     * @since 1.8.2
     *
     * @see #eachMatch(String,String,Closure)
     */
    public static <T extends CharSequence> T eachMatch(final T self, final CharSequence regex, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        eachMatch(self.toString(), regex.toString(), closure);
        return self;
    }

    /**
     * Processes each regex group matched substring of the given pattern. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source CharSequence
     * @param pattern a regex Pattern
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source CharSequence
     *
     * @since 1.8.2
     *
     * @see #eachMatch(String,Pattern,Closure)
     */
    public static <T extends CharSequence> T eachMatch(final T self, final Pattern pattern, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        eachMatch(self.toString(), pattern, closure);
        return self;
    }

    /**
     * Processes each regex group matched substring of the given pattern. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source string
     * @param pattern a regex Pattern
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source string
     *
     * @since 1.6.1
     */
    public static String eachMatch(final String self, final Pattern pattern, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        Matcher m = pattern.matcher(self);
        each(m, closure);
        return self;
    }

    /**
     * Process each regex group matched substring of the given string. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source string
     * @param regex   a Regex string
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source string
     *
     * @since 1.6.0
     */
    public static String eachMatch(final String self, final String regex, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        return eachMatch(self, Pattern.compile(regex), closure);
    }

    /**
     * Expands all tabs into spaces with tabStops of size 8.
     *
     * @param self A CharSequence to expand
     * @return The expanded toString() of this CharSequence
     *
     * @since 1.8.2
     */
    public static String expand(final CharSequence self) {
        return expand(self, 8);
    }

    /**
     * Expands all tabs into spaces. If the CharSequence has multiple
     * lines, expand each line - restarting tab stops at the start
     * of each line.
     *
     * @param self A CharSequence to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded toString() of this CharSequence
     *
     * @since 1.8.2
     */
    public static String expand(final CharSequence self, final int tabStop) {
        if (self.length() == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (String line : new LineIterable(self)) {
            builder.append(expandLine(line, tabStop));
            builder.append("\n");
        }
        // remove the normalized ending line ending if it was not present
        if (self.charAt(self.length() - 1) != '\n') {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * Expands all tabs into spaces. Assumes the CharSequence represents a single line of text.
     *
     * @param self A line to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded toString() of this CharSequence
     *
     * @since 1.8.2
     */
    public static String expandLine(final CharSequence self, final int tabStop) {
        String s = self.toString();
        int index;
        while ((index = s.indexOf('\t')) != -1) {
            StringBuilder builder = new StringBuilder(s);
            int count = tabStop - index % tabStop;
            builder.deleteCharAt(index);
            for (int i = 0; i < count; i++) builder.insert(index, " ");
            s = builder.toString();
        }
        return s;
    }

    /**
     * Finds the first occurrence of a regular expression String within a String.
     * If the regex doesn't match, null will be returned.
     * <p>
     * For example, if the regex doesn't match the result is null:
     * <pre class="groovyTestCase">
     *     assert "New York, NY".find(/\d{5}/) == null
     * </pre>
     *
     * If it does match, we get the matching string back:
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(/\d{5}/) == "10292"
     * </pre>
     *
     * If we have capture groups in our expression, we still get back the full match
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(/(\d{5})-?(\d{4})/) == "10292-0098"
     * </pre>
     *
     * @param self  a CharSequence
     * @param regex the capturing regex
     * @return a String containing the matched portion, or null if the regex doesn't match
     *
     * @since 1.8.2
     *
     * @see #find(CharSequence,Pattern)
     */
    public static String find(final CharSequence self, final CharSequence regex) {
        return find(self, Pattern.compile(regex.toString()));
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a CharSequence.
     * If the regex doesn't match, the closure will not be called and find will return null.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex CharSequence
     * @param closure the closure that will be passed the full match, plus each of the capturing groups (if any)
     * @return a String containing the result of calling the closure (calling toString() if needed), or null if the regex pattern doesn't match
     *
     * @since 1.8.2
     *
     * @see #find(CharSequence,Pattern,Closure)
     */
    public static String find(final CharSequence self, final CharSequence regex, @ClosureParams(value=FromString.class, options={"java.util.List<java.lang.String>","java.lang.String[]"}) final Closure closure) {
        return find(self, Pattern.compile(regex.toString()), closure);
    }

    /**
     * Finds the first occurrence of a compiled regular expression Pattern within a String.
     * If the pattern doesn't match, null will be returned.
     * <p>
     * For example, if the pattern doesn't match the result is null:
     * <pre class="groovyTestCase">
     *     assert "New York, NY".find(~/\d{5}/) == null
     * </pre>
     *
     * If it does match, we get the matching string back:
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(~/\d{5}/) == "10292"
     * </pre>
     *
     * If we have capture groups in our expression, the groups are ignored and
     * we get back the full match:
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) == "10292-0098"
     * </pre>
     * If you need to work with capture groups, then use the closure version
     * of this method or use Groovy's matcher operators or use {@code eachMatch}.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @return a String containing the matched portion, or null if the regex pattern doesn't match
     *
     * @since 1.8.2
     */
    public static String find(final CharSequence self, final Pattern pattern) {
        Matcher matcher = pattern.matcher(self.toString());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a compiled regular expression found within a String.
     * If the regex doesn't match, the closure will not be called and find will return null.
     * <p>
     * For example, if the pattern doesn't match, the result is null:
     * <pre class="groovyTestCase">
     *     assert "New York, NY".find(~/\d{5}/) { match {@code ->} return "-$match-"} == null
     * </pre>
     *
     * If it does match and we don't have any capture groups in our regex, there is a single parameter
     * on the closure that the match gets passed to:
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(~/\d{5}/) { match {@code ->} return "-$match-"} == "-10292-"
     * </pre>
     *
     * If we have capture groups in our expression, our closure has one parameter for the match, followed by
     * one for each of the capture groups:
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour {@code ->}
     *          assert match == "10292-0098"
     *          assert zip == "10292"
     *          assert plusFour == "0098"
     *          return zip
     *      } == "10292"
     * </pre>
     * If we have capture groups in our expression, and our closure has one parameter,
     * the closure will be passed an array with the first element corresponding to the whole match,
     * followed by an element for each of the capture groups:
     * <pre class="groovyTestCase">
     *      assert "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { array {@code ->}
     *          assert array[0] == "10292-0098"
     *          assert array[1] == "10292"
     *          assert array[2] == "0098"
     *          return array[1]
     *      } == "10292"
     * </pre>
     * If a capture group is optional, and doesn't match, then the corresponding value
     * for that capture group passed to the closure will be null as illustrated here:
     * <pre class="groovyTestCase">
     *      assert "adsf 233-9999 adsf".find(~/(\d{3})?-?(\d{3})-(\d{4})/) { match, areaCode, exchange, stationNumber {@code ->}
     *          assert "233-9999" == match
     *          assert null == areaCode
     *          assert "233" == exchange
     *          assert "9999" == stationNumber
     *          return "$exchange$stationNumber"
     *      } == "2339999"
     * </pre>
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @param closure the closure that will be passed the full match, plus each of the capturing groups (if any)
     * @return a String containing the result of calling the closure (calling toString() if needed), or null if the regex pattern doesn't match
     *
     * @since 1.8.2
     */
    public static String find(final CharSequence self, final Pattern pattern, @ClosureParams(value=FromString.class, options={"java.util.List<java.lang.String>","java.lang.String[]"}) final Closure closure) {
        Matcher matcher = pattern.matcher(self.toString());
        if (matcher.find()) {
            if (hasGroup(matcher)) {
                int count = matcher.groupCount();
                List<String> groups = new ArrayList<>(count);
                for (int i = 0; i <= count; i += 1) {
                    groups.add(matcher.group(i));
                }
                return InvokerHelper.toString(closure.call(groups));
            } else {
                return InvokerHelper.toString(closure.call(matcher.group(0)));
            }
        }
        return null;
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (provided as a CharSequence) found within a CharSequence.
     * <p>
     * For example, if the regex doesn't match, it returns an empty list:
     * <pre class="groovyTestCase">
     * assert "foo".findAll(/(\w*) Fish/) == []
     * </pre>
     * Any regular expression matches are returned in a list, and all regex capture groupings are ignored, only the full match is returned:
     * <pre class="groovyTestCase">
     * def expected = ["One Fish", "Two Fish", "Red Fish", "Blue Fish"]
     * assert "One Fish, Two Fish, Red Fish, Blue Fish".findAll(/(\w*) Fish/) == expected
     * </pre>
     * If you need to work with capture groups, then use the closure version
     * of this method or use Groovy's matcher operators or use {@code eachMatch}.
     *
     * @param self  a CharSequence
     * @param regex the capturing regex CharSequence
     * @return a List containing all full matches of the regex within the CharSequence, an empty list will be returned if there are no matches
     *
     * @since 1.8.2
     *
     * @see #findAll(CharSequence,Pattern)
     */
    public static List<String> findAll(final CharSequence self, final CharSequence regex) {
        return findAll(self, Pattern.compile(regex.toString()));
    }

    /**
     * Finds all occurrences of a regular expression string within a CharSequence.   Any matches are passed to the specified closure.  The closure
     * is expected to have the full match in the first parameter.  If there are any capture groups, they will be placed in subsequent parameters.
     * <p>
     * If there are no matches, the closure will not be called, and an empty List will be returned.
     * <p>
     * For example, if the regex doesn't match, it returns an empty list:
     * <pre class="groovyTestCase">
     * assert "foo".findAll(/(\w*) Fish/) { match, firstWord {@code ->} return firstWord } == []
     * </pre>
     * Any regular expression matches are passed to the closure, if there are no capture groups, there will be one parameter for the match:
     * <pre class="groovyTestCase">
     * assert "I could not, would not, with a fox.".findAll(/.ould/) { match {@code ->} "${match}n't"} == ["couldn't", "wouldn't"]
     * </pre>
     * If there are capture groups, the first parameter will be the match followed by one parameter for each capture group:
     * <pre class="groovyTestCase">
     * def orig = "There's a Wocket in my Pocket"
     * assert orig.findAll(/(.)ocket/) { match, firstLetter {@code ->} "$firstLetter {@code >} $match" } == ["W {@code >} Wocket", "P {@code >} Pocket"]
     * </pre>
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex CharSequence
     * @param closure will be passed the full match plus each of the capturing groups (if any)
     * @return a List containing all results from calling the closure with each full match (and potentially capturing groups) of the regex within the CharSequence, an empty list will be returned if there are no matches
     *
     * @since 1.8.2
     *
     * @see #findAll(CharSequence,Pattern,Closure)
     */
    public static <T> List<T> findAll(final CharSequence self, final CharSequence regex, @ClosureParams(value=FromString.class, options={"java.util.List<java.lang.String>","java.lang.String[]"}) final Closure<T> closure) {
        return findAll(self, Pattern.compile(regex.toString()), closure);
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in Pattern format) found within a CharSequence.
     * <p>
     * For example, if the pattern doesn't match, it returns an empty list:
     * <pre class="groovyTestCase">
     * assert "foo".findAll(~/(\w*) Fish/) == []
     * </pre>
     * Any regular expression matches are returned in a list, and all regex capture groupings are ignored, only the full match is returned:
     * <pre class="groovyTestCase">
     * def expected = ["One Fish", "Two Fish", "Red Fish", "Blue Fish"]
     * assert "One Fish, Two Fish, Red Fish, Blue Fish".findAll(~/(\w*) Fish/) == expected
     * </pre>
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @return a List containing all full matches of the Pattern within the CharSequence, an empty list will be returned if there are no matches
     *
     * @since 1.8.2
     */
    public static List<String> findAll(final CharSequence self, final Pattern pattern) {
        Matcher matcher = pattern.matcher(self.toString());
        boolean hasGroup = hasGroup(matcher);
        List<String> list = new ArrayList<>();
        for (Iterator iter = iterator(matcher); iter.hasNext();) {
            if (hasGroup) {
                list.add((String) ((List) iter.next()).get(0));
            } else {
                list.add((String) iter.next());
            }
        }
        return new ArrayList<String>(list);
    }

    /**
     * Finds all occurrences of a compiled regular expression Pattern within a CharSequence. Any matches are passed to
     * the specified closure.  The closure is expected to have the full match in the first parameter.  If there are any
     * capture groups, they will be placed in subsequent parameters.
     * <p>
     * If there are no matches, the closure will not be called, and an empty List will be returned.
     * <p>
     * For example, if the pattern doesn't match, it returns an empty list:
     * <pre class="groovyTestCase">
     * assert "foo".findAll(~/(\w*) Fish/) { match, firstWord {@code ->} return firstWord } == []
     * </pre>
     * Any regular expression matches are passed to the closure, if there are no capture groups, there will be one
     * parameter for the match:
     * <pre class="groovyTestCase">
     * assert "I could not, would not, with a fox.".findAll(~/.ould/) { match {@code ->} "${match}n't"} == ["couldn't", "wouldn't"]
     * </pre>
     * If there are capture groups, the first parameter will be the match followed by one parameter for each capture group:
     * <pre class="groovyTestCase">
     * def orig = "There's a Wocket in my Pocket"
     * assert orig.findAll(~/(.)ocket/) { match, firstLetter {@code ->} "$firstLetter {@code >} $match" } == ["W {@code >} Wocket", "P {@code >} Pocket"]
     * </pre>
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @param closure will be passed the full match plus each of the capturing groups (if any)
     * @return a List containing all results from calling the closure with each full match (and potentially capturing groups) of the regex pattern within the CharSequence, an empty list will be returned if there are no matches
     *
     * @since 1.8.2
     */
    public static <T> List<T> findAll(final CharSequence self, final Pattern pattern, @ClosureParams(value=FromString.class, options={"java.util.List<java.lang.String>","java.lang.String[]"}) final Closure<T> closure) {
        Matcher matcher = pattern.matcher(self.toString());
        return DefaultGroovyMethods.collect(matcher, closure);
    }

    // TODO: Expose this for stream based scenarios?
    private static int findMinimumLeadingSpaces(final String line, final int count) {
        int length = line.length();
        int index = 0;
        while (index < length && index < count && Character.isWhitespace(line.charAt(index))) {
            index += 1;
        }
        return index;
    }

    /**
     * Selects a List of characters from a CharSequence using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a CharSequence
     * @param indices a Collection of indices
     * @return a String consisting of the characters at the given indices
     *
     * @since 1.0
     */
    public static String getAt(final CharSequence self, final Collection indices) {
        StringBuilder answer = new StringBuilder();
        for (Object value : indices) {
            if (value instanceof Range) {
                answer.append(getAt(self, (Range) value));
            } else if (value instanceof Collection) {
                answer.append(getAt(self, (Collection) value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                answer.append(getAt(self, idx));
            }
        }
        return answer.toString();
    }

    /**
     * Supports the range subscript operator for CharSequence or StringBuffer with EmptyRange
     *
     * @param self  a CharSequence
     * @param range an EmptyRange
     * @return the empty String
     *
     * @since 1.5.0
     */
    public static String getAt(final CharSequence self, final EmptyRange range) {
        return "";
    }

    /**
     * Supports the subscript operator for CharSequence.
     *
     * @param self  a CharSequence
     * @param index the index of the Character to get
     * @return the Character at the given index
     *
     * @since 1.0
     */
    public static CharSequence getAt(final CharSequence self, int index) {
        index = normaliseIndex(index, self.length());
        return self.subSequence(index, index + 1);
    }

    /**
     * Supports the subscript operator for GString.
     *
     * @param self  a GString
     * @param index the index of the Character to get
     * @return the Character at the given index
     *
     * @since 2.3.7
     */
    public static String getAt(final GString self, final int index) {
        return getAt(self.toString(), index);
    }

    /**
     * Supports the range subscript operator for CharSequence with IntRange.
     *
     * @param self  a CharSequence
     * @param range an IntRange
     * @return the subsequence CharSequence
     *
     * @since 1.0
     */
    public static CharSequence getAt(final CharSequence self, final IntRange range) {
        return getAt(self, (Range) range);
    }

    /**
     * Supports the range subscript operator for GString with IntRange.
     *
     * @param self  a GString
     * @param range an IntRange
     * @return the String of characters corresponding to the provided range
     *
     * @since 2.3.7
     */
    public static String getAt(final GString self, final IntRange range) {
        return getAt(self, (Range) range);
    }

    /**
     * Supports the range subscript operator for CharSequence.
     *
     * @param self  a CharSequence
     * @param range a Range
     * @return the subsequence CharSequence
     *
     * @since 1.0
     */
    public static CharSequence getAt(final CharSequence self, final Range range) {
        RangeInfo info = subListBorders(self.length(), range);
        CharSequence sequence = self.subSequence(info.from, info.to);
        return info.reverse ? reverse(sequence) : sequence;
    }

    /**
     * Supports the range subscript operator for GString.
     *
     * @param self  a GString
     * @param range a Range
     * @return the String of characters corresponding to the provided range
     *
     * @since 2.3.7
     */
    public static String getAt(final GString self, final Range range) {
        return getAt(self.toString(), range);
    }

    /**
     * Selects a List of values from a Matcher using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a Matcher
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
     *
     * @since 1.6.0
     */
    public static List getAt(final Matcher self, Collection indices) {
        List result = new ArrayList();
        if (indices instanceof IntRange) {
            int size = (int) size(self);
            RangeInfo info = subListBorders(size, (Range) indices);
            indices = new IntRange(((IntRange)indices).getInclusive(), info.from, info.to - 1);
        }
        for (Object value : indices) {
            if (value instanceof Range) {
                result.addAll(getAt(self, (Range) value));
            } else {
                int idx = DefaultTypeTransformation.intUnbox(value);
                result.add(getAt(self, idx));
            }
        }
        return result;
    }

    /**
     * Supports the subscript operator, e.g. {@code matcher[index]}, for a {@code Matcher}.
     * <p>
     * For an example using no group match,
     * <pre class="groovyTestCase">
     *    def p = /ab[d|f]/
     *    def m = "abcabdabeabf" =~ p
     *    assert 2 == m.count
     *    assert 2 == m.size() // synonym for m.getCount()
     *    assert ! m.hasGroup()
     *    assert 0 == m.groupCount()
     *    def matches = ["abd", "abf"]
     *    for (i in 0..&lt;m.count) {
     *      assert m[i] == matches[i]
     *    }
     * </pre>
     * <p>
     * For an example using group matches,
     * <pre class="groovyTestCase">
     *    def p = /(?:ab([c|d|e|f]))/
     *    def m = "abcabdabeabf" =~ p
     *    assert 4 == m.count
     *    assert m.hasGroup()
     *    assert 1 == m.groupCount()
     *    def matches = [["abc", "c"], ["abd", "d"], ["abe", "e"], ["abf", "f"]]
     *    for (i in 0..&lt;m.count) {
     *      assert m[i] == matches[i]
     *    }
     * </pre>
     * <p>
     * For another example using group matches,
     * <pre class="groovyTestCase">
     *    def m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/
     *    assert 3 == m.count
     *    assert m.hasGroup()
     *    assert 1 == m.groupCount()
     *    def matches = [["abd", "d"], ["abxyz", "xyz"], ["abx", "x"]]
     *    for (i in 0..&lt;m.count) {
     *      assert m[i] == matches[i]
     *    }
     * </pre>
     *
     * @param self a Matcher
     * @param index an index
     * @return object a matched String if no groups matched, list of matched groups otherwise.
     *
     * @since 1.0
     */
    public static Object getAt(final Matcher self, int index) {
        try {
            int count = getCount(self);
            if (index < -count || index >= count) {
                throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + index + ")");
            }
            index = normaliseIndex(index, count);

            Iterator iter = iterator(self);
            Object result = null;
            for (int i = 0; i <= index; i++) {
                result = iter.next();
            }
            return result;
        }
        catch (IllegalStateException ex) {
            return null;
        }
    }

    /**
     * Given a matcher that matches a string against a pattern, returns true when
     * the string matches the pattern or if a longer string, could match the pattern.
     *
     * For example:
     * <pre class="groovyTestCase">
     *     def emailPattern = /\w+@\w+\.\w{2,}/
     *
     *     def matcher = "john@doe" =~ emailPattern
     *     assert matcher.matchesPartially()
     *
     *     matcher = "john@doe.com" =~ emailPattern
     *     assert matcher.matchesPartially()
     *
     *     matcher = "john@@" =~ emailPattern
     *     assert !matcher.matchesPartially()
     * </pre>
     *
     * @param self the Matcher
     * @return true if more input to the String could make the matcher match the associated pattern, false otherwise.
     *
     * @since 2.0.0
     */
    public static boolean matchesPartially(final Matcher self) {
        return self.matches() || self.hitEnd();
    }

    /**
     * Supports the subscript operator for String.
     *
     * @param self  a String
     * @param index the index of the Character to get
     * @return the Character at the given index
     *
     * @since 1.0
     */
    public static String getAt(final String self, int index) {
        index = normaliseIndex(index, self.length());
        return self.substring(index, index + 1);
    }

    /**
     * Supports the range subscript operator for String with IntRange.
     *
     * @param self  a String
     * @param range an IntRange
     * @return the resulting String
     *
     * @since 1.0
     */
    public static String getAt(final String self, final IntRange range) {
        return getAt(self, (Range) range);
    }

    /**
     * Supports the range subscript operator for String.
     *
     * @param self  a String
     * @param range a Range
     * @return a substring corresponding to the Range
     *
     * @since 1.0
     */
    public static String getAt(final String self, final Range range) {
        RangeInfo info = subListBorders(self.length(), range);
        String answer = self.substring(info.from, info.to);
        if (info.reverse) {
            answer = reverse(answer);
        }
        return answer;
    }

    /**
     * Converts the given CharSequence into an array of characters.
     *
     * @param self a CharSequence
     * @return an array of characters
     *
     * @since 1.8.2
     */
    public static char[] getChars(final CharSequence self) {
        return self.toString().toCharArray();
    }

    /**
     * Finds the number of Strings matched to the given Matcher.
     *
     * @param self a Matcher
     * @return int  the number of Strings matched to the given matcher.
     *
     * @since 1.0
     */
    public static int getCount(final Matcher self) {
        int counter = 0;
        self.reset();
        while (self.find()) {
            counter += 1;
        }
        return counter;
    }

    private static String getPadding(final CharSequence padding, final int length) {
        if (padding.length() < length) {
            return multiply(padding, length / padding.length() + 1).substring(0, length);
        } else {
            return "" + padding.subSequence(0, length);
        }
    }

    /**
     * Gets a replacement corresponding to the matched pattern for {@link org.codehaus.groovy.runtime.StringGroovyMethods#replaceAll(CharSequence,Pattern,Closure)}.
     * <p>
     * The closure take parameter:
     * <ul>
     * <li>Whole of match if the pattern include no capturing group</li>
     * <li>Object[] of capturing groups if the closure takes Object[] as parameter</li>
     * <li>List of capturing groups</li>
     * </ul>
     *
     * @param matcher the matcher object used for matching
     * @param closure specified with replaceAll() to get replacement
     * @return replacement correspond replacement for a match
     */
    private static String getReplacement(final Matcher matcher, final Closure closure) {
        if (!hasGroup(matcher)) {
            return InvokerHelper.toString(closure.call(matcher.group()));
        }

        int count = matcher.groupCount();
        List<String> groups = new ArrayList<String>();
        for (int i = 0; i <= count; i++) {
            groups.add(matcher.group(i));
        }

        if (closure.getParameterTypes().length == 1
                && closure.getParameterTypes()[0] == Object[].class) {
            return InvokerHelper.toString(closure.call(groups.toArray()));
        }
        return InvokerHelper.toString(closure.call(groups));
    }

    /**
     * Checks whether a Matcher contains a group or not.
     *
     * @param self a Matcher
     * @return boolean  {@code true} if matcher contains at least one group.
     *
     * @since 1.0
     */
    public static boolean hasGroup(final Matcher self) {
        return self.groupCount() > 0;
    }

    /**
     * Returns true if a CharSequence only contains whitespace characters.
     *
     * @param self The CharSequence to check the characters in
     * @return true If all characters are whitespace characters
     *
     * @since 1.8.2
     */
    public static boolean isAllWhitespace(final CharSequence self) {
        for (int i = 0, n = self.length(); i < n; i += 1) {
            if (!Character.isWhitespace(self.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if a CharSequence can be parsed as a BigDecimal.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     */
    public static boolean isBigDecimal(final CharSequence self) {
        try {
            new BigDecimal(self.toString().trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determines if a CharSequence can be parsed as a BigInteger.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     */
    public static boolean isBigInteger(final CharSequence self) {
        try {
            new BigInteger(self.toString().trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * 'Case' implementation for a CharSequence, which uses equals between the
     * toString() of the caseValue and the switchValue. This allows CharSequence
     * values to be used in switch statements. For example:
     * <pre>
     * switch( str ) {
     *   case 'one' :
     *   // etc...
     * }
     * </pre>
     * Note that this returns {@code true} for the case where both the
     * 'switch' and 'case' operand is {@code null}.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     *
     * @since 1.8.2
     */
    public static boolean isCase(final CharSequence caseValue, final Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        return caseValue.toString().equals(switchValue.toString());
    }

    /**
     * 'Case' implementation for the {@link java.util.regex.Pattern} class, which allows
     * testing a String against a number of regular expressions.
     * <p>
     * For example:
     * <pre>switch( str ) {
     *   case ~/one/ :
     *     // the regex 'one' matches the value of str
     * }
     * </pre>
     * Note that this returns true for the case where both the pattern and
     * the 'switch' values are {@code null}.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue is deemed to match the caseValue
     *
     * @since 1.0
     */
    public static boolean isCase(final Pattern caseValue, final Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        final Matcher matcher = caseValue.matcher(switchValue.toString());
        if (matcher.matches()) {
            RegexSupport.setLastMatcher(matcher);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines if a CharSequence can be parsed as a Double.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     */
    public static boolean isDouble(final CharSequence self) {
        try {
            Double.valueOf(self.toString().trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determines if a CharSequence can be parsed as a Float.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     */
    public static boolean isFloat(final CharSequence self) {
        try {
            Float.valueOf(self.toString().trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determines if a CharSequence can be parsed as an Integer.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     */
    public static boolean isInteger(final CharSequence self) {
        try {
            Integer.valueOf(self.toString().trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determines if a CharSequence can be parsed as a Long.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     */
    public static boolean isLong(final CharSequence self) {
        try {
            Long.valueOf(self.toString().trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determines if a CharSequence can be parsed as a Number.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     *
     * @since 1.8.2
     *
     * @see #isBigDecimal(CharSequence)
     */
    public static boolean isNumber(final CharSequence self) {
        return isBigDecimal(self);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses each match.
     *
     * @param self a Matcher object
     * @return an Iterator for a Matcher
     *
     * @since 1.0
     *
     * @see java.util.regex.Matcher#group()
     */
    public static Iterator iterator(final Matcher self) {
        self.reset();
        return new Iterator() {
            private boolean done, found;

            public boolean hasNext() {
                if (done) {
                    return false;
                }
                if (!found) {
                    found = self.find();
                    if (!found) {
                        done = true;
                    }
                }
                return found;
            }

            public Object next() {
                if (!found) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                found = false;

                if (hasGroup(self)) {
                    // are we using groups?
                    // yes, so return the specified group as list
                    List<String> list = new ArrayList<String>(self.groupCount());
                    for (int i = 0; i <= self.groupCount(); i += 1) {
                        list.add(self.group(i));
                    }
                    return list;
                } else {
                    // not using groups, so return the nth
                    // occurrence of the pattern
                    return self.group();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a CharSequence.
     *
     * @param self  a CharSequence
     * @param value an Object
     * @return a StringBuilder built from this CharSequence
     *
     * @since 1.8.2
     */
    public static StringBuilder leftShift(final CharSequence self, final Object value) {
        return new StringBuilder(self).append(value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a String.
     *
     * @param self  a String
     * @param value an Object
     * @return a StringBuffer built from this string
     *
     * @since 1.0
     */
    // TODO: Deprecate in favor of leftShift(CharSequence,Object): StringBuilder?
    public static StringBuffer leftShift(final String self, final Object value) {
        return new StringBuffer(self).append(value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuffer.
     *
     * @param self  a StringBuffer
     * @param value a value to append
     * @return the StringBuffer on which this operation was invoked
     *
     * @since 1.0
     */
    public static StringBuffer leftShift(final StringBuffer self, final Object value) {
        self.append(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide syntactic sugar for appending to a StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value an Object
     * @return the original StringBuilder
     *
     * @since 1.8.2
     */
    public static StringBuilder leftShift(final StringBuilder self, final Object value) {
        self.append(value);
        return self;
    }

    /**
     * Determines if a CharSequence matches the given regular expression.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @return  true if the CharSequence matches
     *
     * @since 1.8.2
     *
     * @see String#matches(String)
     */
    public static boolean matches(final CharSequence self, final Pattern pattern) {
        return pattern.matcher(self).matches();
    }

    /**
     * Removes a part of a CharSequence by replacing the first occurrence
     * of target within self with empty string and returns the result.
     *
     * @param self   a CharSequence
     * @param target an object representing the part to remove
     * @return a String containing the original minus the part to be removed
     *
     * @since 1.8.2
     */
    public static String minus(final CharSequence self, final Object target) {
        String s = self.toString();
        String text = DefaultGroovyMethods.toString(target);
        int index = s.indexOf(text);
        if (index == -1) return s;
        int end = index + text.length();
        if (s.length() > end) {
            return s.substring(0, index) + s.substring(end);
        }
        return s.substring(0, index);
    }

    /**
     * Removes a part of a CharSequence. This replaces the first occurrence
     * of the pattern within self with empty string and returns the result.
     *
     * @param self   a String
     * @param pattern a Pattern representing the part to remove
     * @return a String minus the part to be removed
     *
     * @since 2.2.0
     */
    public static String minus(final CharSequence self, final Pattern pattern) {
        return pattern.matcher(self).replaceFirst("");
    }

    /**
     * Repeats a CharSequence a certain number of times.
     *
     * @param self   a CharSequence to be repeated
     * @param factor the number of times the CharSequence should be repeated
     * @return a String composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
     *
     * @since 1.8.2
     */
    public static String multiply(final CharSequence self, final Number factor) {
        int size = factor.intValue();
        if (size == 0)
            return "";
        else if (size < 0) {
            throw new IllegalArgumentException("multiply() should be called with a number of 0 or greater not: " + size);
        }
        StringBuilder answer = new StringBuilder(self);
        for (int i = 1; i < size; i++) {
            answer.append(self);
        }
        return answer.toString();
    }

    /**
     * Overloads the {@code ++} operator for the class CharSequence.
     * It increments the last character in the given CharSequence. If the last
     * character in the CharSequence is Character.MAX_VALUE a Character.MIN_VALUE
     * will be appended. The empty CharSequence is incremented to a string
     * consisting of the character Character.MIN_VALUE.
     *
     * @param self a CharSequence
     * @return a value obtained by incrementing the toString() of the CharSequence
     *
     * @since 1.8.2
     */
    public static String next(final CharSequence self) {
        StringBuilder buffer = new StringBuilder(self);
        if (buffer.length() == 0) {
            buffer.append(Character.MIN_VALUE);
        } else {
            char last = buffer.charAt(buffer.length() - 1);
            if (last == Character.MAX_VALUE) {
                buffer.append(Character.MIN_VALUE);
            } else {
                char next = last;
                next++;
                buffer.setCharAt(buffer.length() - 1, next);
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a String with linefeeds and carriage returns normalized to linefeeds.
     *
     * @param self a CharSequence object
     * @return the normalized toString() for the CharSequence
     *
     * @since 1.8.2
     */
    public static String normalize(final CharSequence self) {
        final String s = self.toString();
        int nx = s.indexOf('\r');

        if (nx < 0) {
            return s;
        }

        final int len = s.length();
        final StringBuilder sb = new StringBuilder(len);

        int i = 0;

        do {
            sb.append(s, i, nx);
            sb.append('\n');

            if ((i = nx + 1) >= len) break;

            if (s.charAt(i) == '\n') {
                // skip the LF in CR LF
                if (++i >= len) break;
            }

            nx = s.indexOf('\r', i);
        } while (nx > 0);

        sb.append(s, i, len);

        return sb.toString();
    }

    /**
     * Pads a CharSequence to a minimum length specified by {@code numberOfChars} by adding the space character
     * to the left as many times as needed.
     *
     * If the String is already the same size or bigger than the target {@code numberOfChars}, then the original String is returned. An example:
     * <pre>
     * println 'Numbers:'
     * [1, 10, 100, 1000].each{ println it.toString().padLeft(5) }
     * </pre>
     * will produce output like:
     * <pre>
     * Numbers:
     *     1
     *    10
     *   100
     *  1000
     * </pre>
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @return the CharSequence padded to the left as a String
     *
     * @since 1.8.2
     *
     * @see #padLeft(CharSequence,Number,CharSequence)
     */
    public static String padLeft(final CharSequence self, final Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pads a CharSequence to a minimum length specified by {@code numberOfChars}, adding the supplied
     * padding CharSequence as many times as needed to the left.
     *
     * If the CharSequence is already the same size or bigger than the target {@code numberOfChars}, then the
     * toString() of the original CharSequence is returned. An example:
     * <pre>
     * println 'Numbers:'
     * [1, 10, 100, 1000].each{ println it.toString().padLeft(5, '*') }
     * [2, 20, 200, 2000].each{ println it.toString().padLeft(5, '*_') }
     * </pre>
     * will produce output like:
     * <pre>
     * Numbers:
     * ****1
     * ***10
     * **100
     * *1000
     * *_*_2
     * *_*20
     * *_200
     * *2000
     * </pre>
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence padded to the left as a String
     *
     * @since 1.8.2
     */
    public static String padLeft(final CharSequence self, final Number numberOfChars, final CharSequence padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self.toString();
        } else {
            return getPadding(padding.toString(), numChars - self.length()) + self;
        }
    }

    /**
     * Pads a CharSequence to a minimum length specified by {@code numberOfChars} by adding the space
     * character to the right as many times as needed.
     *
     * If the CharSequence is already the same size or bigger than the target {@code numberOfChars},
     * then the toString() of the original CharSequence is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println it.padRight(5) + it.size() }
     * </pre>
     * will produce output like:
     * <pre>
     * A    1
     * BB   2
     * CCC  3
     * DDDD 4
     * </pre>
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the CharSequence padded to the right as a String
     *
     * @since 1.8.2
     */
    public static String padRight(final CharSequence self, final Number numberOfChars) {
        return padRight(self, numberOfChars, " ");
    }

    /**
     * Pads a CharSequence to a minimum length specified by {@code numberOfChars}, adding the supplied padding
     * CharSequence as many times as needed to the right.
     *
     * If the CharSequence is already the same size or bigger than the target {@code numberOfChars},
     * then the toString() of the original CharSequence is returned. An example:
     * <pre>
     * ['A', 'BB', 'CCC', 'DDDD'].each{ println it.padRight(5, '#') + it.size() }
     * </pre>
     * will produce output like:
     * <pre>
     * A####1
     * BB###2
     * CCC##3
     * DDDD#4
     * </pre>
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence padded to the right as a String
     *
     * @since 1.8.2
     */
    public static String padRight(final CharSequence self, final Number numberOfChars, final CharSequence padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self.toString();
        } else {
            return self + getPadding(padding.toString(), numChars - self.length());
        }
    }

    /**
     * Appends the String representation of the given operand to this CharSequence.
     *
     * @param left  a CharSequence
     * @param right any Object
     * @return the original toString() of the CharSequence with the object appended
     *
     * @since 1.8.2
     */
    public static String plus(final CharSequence left, final Object right) {
        return left + DefaultGroovyMethods.toString(right);
    }

    /**
     * Appends a String to the string representation of this number.
     *
     * @param left a Number
     * @param right a String
     * @return a String
     *
     * @since 1.0
     */
    public static String plus(final Number left, final String right) {
        return DefaultGroovyMethods.toString(left) + right;
    }

    /**
     * Appends the String representation of the given operand to this string.
     *
     * @param left  a String
     * @param right any CharSequence
     * @return the new string with the object appended
     *
     * @since 2.2
     */
    public static String plus(final String left, final CharSequence right) {
        return left + right;
    }

    /**
     * Appends a String to this StringBuffer.
     *
     * @param left  a StringBuffer
     * @param right a String
     * @return a String
     *
     * @since 1.0
     */
    public static String plus(final StringBuffer left, final String right) {
        return left + right;
    }

    /**
     * Overloads the {@code --} operator for the class CharSequence.
     * It decrements the last character in the given CharSequence. If the
     * last character in the CharSequence is Character.MIN_VALUE it will be deleted.
     * The empty CharSequence can't be decremented.
     *
     * @param self a CharSequence
     * @return a String with a decremented character at the end
     *
     * @since 1.8.2
     */
    public static String previous(final CharSequence self) {
        StringBuilder buffer = new StringBuilder(self);
        if (buffer.length() == 0) throw new IllegalArgumentException("the string is empty");
        char last = buffer.charAt(buffer.length() - 1);
        if (last == Character.MIN_VALUE) {
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            char next = last;
            next--;
            buffer.setCharAt(buffer.length() - 1, next);
        }
        return buffer.toString();
    }

    /**
     * Supports the range subscript operator for StringBuffer.
     *
     * @param self  a StringBuffer
     * @param range a Range
     * @param value the object that's toString() will be inserted
     *
     * @since 1.0
     */
    public static void putAt(final StringBuffer self, final EmptyRange range, final Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Supports the range subscript operator for StringBuffer.  Index values are
     * treated as characters within the buffer.
     *
     * @param self  a StringBuffer
     * @param range a Range
     * @param value the object that's toString() will be inserted
     *
     * @since 1.0
     */
    public static void putAt(final StringBuffer self, final IntRange range, final Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Returns the lines of a CharSequence as a List of String.
     *
     * @param self a CharSequence object
     * @return a list of lines
     *
     * @since 1.8.2
     */
    public static List<String> readLines(final CharSequence self) {
        return DefaultGroovyMethods.toList(new LineIterable(self));
    }

    /**
     * Replaces each substring of this CharSequence that matches the given
     * regular expression with the given replacement.
     *
     * @param self        a CharSequence
     * @param regex       the capturing regex
     * @param replacement the string to be substituted for each match
     * @return the toString() of the CharSequence with content replaced
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     *
     * @since 1.8.2
     *
     * @see String#replaceAll(String,String)
     */
    public static String replaceAll(final CharSequence self, final CharSequence regex, final CharSequence replacement) {
        return self.toString().replaceAll(regex.toString(), replacement.toString());
    }

    /**
     * Replaces all occurrences of a captured group by the result of calling a closure on that text.
     * <p>
     * Examples:
     * <pre class="groovyTestCase">
     *     assert "hello world".replaceAll("(o)") { it[0].toUpperCase() } == "hellO wOrld"
     *
     *     assert "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { Object[] it {@code ->} it[0].toUpperCase() }) == "FOOBAR-FOOBAR-"
     *
     *     // Here,
     *     //   it[0] is the global string of the matched group
     *     //   it[1] is the first string in the matched group
     *     //   it[2] is the second string in the matched group
     *
     *     assert "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { x, y, z {@code ->} z.toUpperCase() }) == "FOO-FOO-"
     *
     *     // Here,
     *     //   x is the global string of the matched group
     *     //   y is the first string in the matched group
     *     //   z is the second string in the matched group
     * </pre>
     * Note that unlike String.replaceAll(String regex, String replacement), where the replacement string
     * treats '$' and '\' specially (for group substitution), the result of the closure is converted to a string
     * and that value is used literally for the replacement.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex
     * @param closure the closure to apply on each captured group
     * @return the toString() of the CharSequence with content replaced
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     *
     * @since 1.8.2
     *
     * @see #replaceAll(CharSequence,Pattern,Closure)
     */
    public static String replaceAll(final CharSequence self, final CharSequence regex, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        return replaceAll(self, Pattern.compile(regex.toString()), closure);
    }

    /**
     * Replaces all substrings of a CharSequence that match the given
     * compiled regular expression with the given replacement.
     * <p>
     * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceAll}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     * <p>
     * <pre class="groovyTestCase">
     * assert "foo".replaceAll('o', 'X') == 'fXX'
     * </pre>
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the CharSequence of interest is to be matched
     * @param   replacement the CharSequence to be substituted for the first match
     * @return  the toString() of the CharSequence with content replaced
     *
     * @since 1.8.2
     */
    public static String replaceAll(final CharSequence self, final Pattern pattern, final CharSequence replacement) {
        return pattern.matcher(self).replaceAll(replacement.toString());
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure call on that text.
     * <p>
     * For examples,
     * <pre class="groovyTestCase">
     *     assert "hello world".replaceAll(~"(o)") { it[0].toUpperCase() } == "hellO wOrld"
     *
     *     assert "foobar-FooBar-".replaceAll(~"(([fF][oO]{2})[bB]ar)", { it[0].toUpperCase() }) == "FOOBAR-FOOBAR-"
     *
     *     // Here,
     *     //   it[0] is the global string of the matched group
     *     //   it[1] is the first string in the matched group
     *     //   it[2] is the second string in the matched group
     *
     *     assert "foobar-FooBar-".replaceAll(~"(([fF][oO]{2})[bB]ar)", { Object[] it {@code ->} it[0].toUpperCase() }) == "FOOBAR-FOOBAR-"
     *
     *     // Here,
     *     //   it[0] is the global string of the matched group
     *     //   it[1] is the first string in the matched group
     *     //   it[2] is the second string in the matched group
     *
     *     assert "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { x, y, z {@code ->} z.toUpperCase() }) == "FOO-FOO-"
     *
     *     // Here,
     *     //   x is the global string of the matched group
     *     //   y is the first string in the matched group
     *     //   z is the second string in the matched group
     * </pre>
     * Note that unlike String.replaceAll(String regex, String replacement), where the replacement string
     * treats '$' and '\' specially (for group substitution), the result of the closure is converted to a string
     * and that value is used literally for the replacement.
     *
     * @param self    a CharSequence
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on each captured group
     * @return the toString() of the CharSequence with replaced content
     *
     * @since 1.8.2
     *
     * @see java.util.regex.Matcher#quoteReplacement(String)
     */
    public static String replaceAll(final CharSequence self, final Pattern pattern, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        final String s = self.toString();
        final Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(s.length() + 16);
            do {
                String replacement = getReplacement(matcher, closure);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } while (matcher.find());
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return s;
        }
    }

    /**
     * Replaces the first substring of this CharSequence that matches the given
     * regular expression with the given replacement.
     *
     * @param self        a CharSequence
     * @param regex       the capturing regex
     * @param replacement the CharSequence to be substituted for each match
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     *
     * @since 1.8.2
     *
     * @see String#replaceFirst(String,String)
     */
    public static String replaceFirst(final CharSequence self, final CharSequence regex, final CharSequence replacement) {
        return self.toString().replaceFirst(regex.toString(), replacement.toString());
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     * <p>
     * For example (with some replaceAll variants thrown in for comparison purposes),
     * <pre class="groovyTestCase">
     * assert "hello world".replaceFirst("(o)") { it[0].toUpperCase() } == "hellO world" // first match
     * assert "hello world".replaceAll("(o)") { it[0].toUpperCase() } == "hellO wOrld" // all matches
     *
     * assert "one fish, two fish".replaceFirst(/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() } == '1-FISH, two fish'
     * assert "one fish, two fish".replaceAll(/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() } == '1-FISH, 2-FISH'
     * </pre>
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex
     * @param closure the closure to apply on the first captured group
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     *
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final CharSequence regex, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        return replaceFirst(self, Pattern.compile(regex.toString()), closure);
    }

    /**
     * Replaces the first substring of a CharSequence that matches the given
     * compiled regular expression with the given replacement.
     * <p>
     * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceFirst}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     * <p>
     * <pre class="groovyTestCase">
     * assert "foo".replaceFirst('o', 'X') == 'fXo'
     * </pre>
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the CharSequence of interest is to be matched
     * @param   replacement the CharSequence to be substituted for the first match
     *
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final Pattern pattern, final CharSequence replacement) {
        return pattern.matcher(self).replaceFirst(replacement.toString());
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     * <p>
     * For example (with some replaceAll variants thrown in for comparison purposes),
     * <pre class="groovyTestCase">
     * assert "hellO world" == "hello world".replaceFirst(~"(o)") { it[0].toUpperCase() } // first match
     * assert "hellO wOrld" == "hello world".replaceAll(~"(o)") { it[0].toUpperCase() }   // all matches
     *
     * assert '1-FISH, two fish' == "one fish, two fish".replaceFirst(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * assert '1-FISH, 2-FISH' == "one fish, two fish".replaceAll(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * </pre>
     *
     * @param self    a CharSequence
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on the first captured group
     *
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final Pattern pattern, @ClosureParams(value=FromString.class, options={"List<String>","String[]"}) final Closure closure) {
        final String s = self.toString();
        final Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(s.length() + 16);
            String replacement = getReplacement(matcher, closure);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return s;
        }
    }

    /**
     * Helper class used by {@link #replace(CharSequence,Map)}.
     */
    private static class ReplaceState {
        public ReplaceState(Map<CharSequence, CharSequence> replacements) {
            this.noMoreMatches = new boolean[replacements.size()];
            this.replacementsList = DefaultGroovyMethods.toList((Iterable<Map.Entry<CharSequence,CharSequence>>) replacements.entrySet());
        }

        int textIndex = -1;
        int tempIndex = -1;
        int replaceIndex = -1;
        int start = -1;
        final boolean[] noMoreMatches;
        private final List<Map.Entry<CharSequence, CharSequence>> replacementsList;

        CharSequence key(final int i) {
            return replacementsList.get(i).getKey();
        }

        CharSequence value(final int i) {
            return replacementsList.get(i).getValue();
        }

        int numReplacements() {
            return replacementsList.size();
        }
    }

    /**
     * Replaces all occurrences of replacement CharSequences (supplied via a map) within a provided CharSequence.
     *
     * <pre class="groovyTestCase">
     * assert 'foobar'.replace(f:'b', foo:'bar') == 'boobar'
     * assert 'foobar'.replace(foo:'bar', f:'b') == 'barbar'
     * def replacements = [foo:'bar', f:'b', b: 'f', bar:'boo']
     * assert 'foobar'.replace(replacements) == 'barfar'
     * </pre>
     *
     * @param self a CharSequence
     * @param replacements a map of before (key) and after (value) pairs processed in the natural order of the map
     * @return a String formed from the provided CharSequence after performing all of the replacements
     *
     * @since 2.5.0
     */
    public static String replace(final CharSequence self, final Map<CharSequence, CharSequence> replacements) {
        return replace(self, -1, replacements);
    }

    /**
     * Replaces all occurrences of replacement CharSequences (supplied via a map) within a provided CharSequence
     * with control over the internally created StringBuilder's capacity. This method uses a StringBuilder internally.
     * Java auto-expands a StringBuilder's capacity if needed. In rare circumstances, the overhead involved with
     * repeatedly expanding the StringBuilder may become significant. If you have measured the performance of your
     * application and found this to be a significant bottleneck, use this variant to have complete control over
     * the internally created StringBuilder's capacity.
     *
     * <pre class="groovyTestCase">
     * assert 'foobar'.replace(9, [r:'rbaz']) == 'foobarbaz'
     * assert 'foobar'.replace(1, [fooba:'']) == 'r'
     * </pre>
     *
     * @param self a CharSequence
     * @param capacity an optimization parameter, set to size after replacements or a little larger to avoid resizing overheads
     * @param replacements a map of before (key) and after (value) pairs processed in the natural order of the map
     * @return a String formed from the provided CharSequence after performing all of the replacements
     *
     * @since 2.5.0
     */
    public static String replace(final CharSequence self, final int capacity, final Map<CharSequence, CharSequence> replacements) {
        // modelled very closely on the commons lang StringUtils replaceEach method
        if (self == null) return null;
        String text = self.toString();
        if (replacements == null || replacements.isEmpty() || text.isEmpty()) return text;
        ReplaceState state = new ReplaceState(replacements);
        nextMatch(text, state);
        if (state.textIndex == -1) {
            return text;
        }
        StringBuilder buf = new StringBuilder(guessCapacity(capacity, replacements));

        state.start = 0;
        while (state.textIndex != -1) {
            for (int i = state.start; i < state.textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(state.value(state.replaceIndex));
            state.start = state.textIndex + state.key(state.replaceIndex).length();
            state.textIndex = -1;
            state.replaceIndex = -1;
            nextMatch(text, state);
        }
        int textLength = text.length();
        for (int i = state.start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        return buf.toString();
    }

    /**
     * Crude heuristic for setting the created StringBuilder capacity if not supplied:
     * If at least one replacement text is bigger than the original text use a
     * capacity 50% larger than the original; otherwise, use the original size.
     */
    private static int guessCapacity(final int capacity, final Map<CharSequence, CharSequence> replacements) {
        if (capacity >= 0) {
            return capacity;
        }
        boolean possiblyBigger = false;
        for (Map.Entry<CharSequence, CharSequence> entry : replacements.entrySet()) {
            if (entry.getValue().length() > entry.getKey().length()) {
                possiblyBigger = true;
                break;
            }
        }
        return possiblyBigger ? replacements.size() * 3 / 2 : replacements.size();
    }

    /**
     * Helper method to find the next match for the replace method.
     */
    private static void nextMatch(final String text, final ReplaceState state) {
        for (int i = 0; i < state.numReplacements(); i += 1) {
            if (state.noMoreMatches[i] || state.key(i) == null ||
                    state.key(i).length() == 0 || state.value(i) == null) {
                continue;
            }
            state.tempIndex = text.indexOf(state.key(i).toString(), state.start);

            if (state.tempIndex == -1) {
                state.noMoreMatches[i] = true;
            } else {
                if (state.textIndex == -1 || state.tempIndex < state.textIndex) {
                    state.textIndex = state.tempIndex;
                    state.replaceIndex = i;
                }
            }
        }
    }

    /**
     * Creates a String which is the reverse (backwards) of this CharSequence
     *
     * @param self a CharSequence
     * @return a new String with all the characters reversed.
     *
     * @since 1.8.2
     */
    public static String reverse(final CharSequence self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * Sets the position of the given Matcher to the given index.
     *
     * @param self a Matcher
     * @param index the index number
     *
     * @since 1.0
     */
    public static void setIndex(final Matcher self, int index) {
        int count = getCount(self);
        if (index < -count || index >= count) {
            throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + index + ")");
        }
        if (index == 0) {
            self.reset();
        } else if (index > 0) {
            self.reset();
            for (int i = 0; i < index; i += 1) {
                self.find();
            }
        } else if (index < 0) {
            self.reset();
            index += getCount(self);
            for (int i = 0; i < index; i += 1) {
                self.find();
            }
        }
    }

    /**
     * Provides the standard Groovy {@code size()} method for {@code CharSequence}.
     *
     * @param self a CharSequence
     * @return the length of the CharSequence
     *
     * @since 1.8.2
     */
    public static int size(final CharSequence self) {
        return self.length();
    }

    /**
     * Provides the standard Groovy {@code size()} method for {@code Matcher}.
     *
     * @param self a matcher object
     * @return the matcher's size (count)
     *
     * @since 1.5.0
     */
    public static long size(final Matcher self) {
        return getCount(self);
    }

    /**
     * Provides the standard Groovy {@code size()} method for {@code String}.
     *
     * @param self a String
     * @return the length of the String
     *
     * @since 1.0
     *
     * @see #size(CharSequence)
     */
    @Deprecated
    public static int size(final String self) {
        return self.length();
    }

    /**
     * Provides the standard Groovy {@code size()} method for {@code StringBuffer}.
     *
     * @param self a StringBuffer
     * @return the length of the StringBuffer
     *
     * @since 1.0
     *
     * @see #size(CharSequence)
     */
    @Deprecated
    public static int size(final StringBuffer self) {
        return self.length();
    }

    /**
     * Compares a String representing a number to another. A fluent API style alias for {@code compareTo} on {@code BigDecimal}.
     *
     * @param left  a String representing a number
     * @param right a String representing a number
     * @return true if the value represented by left is equal to or bigger than the value represented by right
     * @since 3.0.1
     */
    public static Boolean isAtLeast(final String left, final String right) {
        return DefaultGroovyMethods.isAtLeast(new BigDecimal(left), right);
    }

    /**
     * Splits a CharSequence (with whitespace as delimiter). Similar to tokenize, but returns an Array of String instead of a List.
     *
     * @param self the CharSequence to split
     * @return String[] result of split
     *
     * @since 1.8.2
     */
    public static String[] split(final CharSequence self) {
        StringTokenizer st = new StringTokenizer(self.toString());
        final int n = st.countTokens();
        String[] strings = new String[n];
        for (int i = 0; i < n; i += 1) {
            strings[i] = st.nextToken();
        }
        return strings;
    }

    /**
     * Iterates through the given CharSequence line by line, splitting each line using
     * the given regex delimiter.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a CharSequence
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     *
     * @since 1.8.2
     *
     * @see #splitEachLine(CharSequence,Pattern,Closure)
     */
    public static <T> T splitEachLine(final CharSequence self, final CharSequence regex, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) final Closure<T> closure) throws IOException {
        return splitEachLine(self, Pattern.compile(regex.toString()), closure);
    }

    /**
     * Iterates through the given CharSequence line by line, splitting each line using
     * the given separator Pattern.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a CharSequence
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     *
     * @since 1.8.2
     */
    public static <T> T splitEachLine(final CharSequence self, final Pattern pattern, @ClosureParams(value=FromString.class,options={"List<String>","String[]"},conflictResolutionStrategy=PickFirstResolver.class) final Closure<T> closure) {
        T result = null;
        for (String line : new LineIterable(self)) {
            List vals = Arrays.asList(pattern.split(line));
            result = closure.call(vals);
        }
        return result;
    }

    /**
     * Strips leading spaces from every line in a CharSequence. The
     * line with the least number of leading spaces determines
     * the number to remove. Lines only containing whitespace are
     * ignored when calculating the number of leading spaces to strip.
     * <pre class="groovyTestCase">
     * assert '  A\n B\nC' == '   A\n  B\n C'.stripIndent()
     * </pre>
     *
     * @param self a CharSequence to strip the leading spaces from
     * @return the stripped {@code toString()} of the CharSequence
     *
     * @since 1.8.2
     */
    public static String stripIndent(final CharSequence self) {
        if (self.length() == 0) return self.toString();
        int runningCount = -1;
        for (String line : new LineIterable(self)) {
            // don't take blank lines into account for calculating the indent
            if (isAllWhitespace(line)) continue;
            if (runningCount == -1) runningCount = line.length();
            runningCount = findMinimumLeadingSpaces(line, runningCount);
            if (runningCount == 0) break;
        }
        return stripIndent(self, runningCount == -1 ? 0 : runningCount);
    }

    /**
     * Same logic as {@link #stripIndent(CharSequence)} if {@code forceGroovyBehavior} is {@code true},
     * otherwise Java 13's {@code stripIndent} will be invoked.
     *
     * @param self The CharSequence to strip the leading spaces from
     * @param forceGroovyBehavior force groovy behavior to avoid conflicts with Java13's stripIndent
     *
     * @since 3.0.0
     */
    @Incubating
    public static String stripIndent(final CharSequence self, final boolean forceGroovyBehavior) {
        if (!forceGroovyBehavior) {
            try {
                MethodHandle mh = MethodHandles.lookup().findVirtual(self.getClass(), "stripIndent", MethodType.methodType(String.class));
                return (String) mh.bindTo(self).invokeWithArguments();
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            } catch (Throwable t) {
                throw new GroovyRuntimeException(t);
            }
        }

        return stripIndent(self);
    }

    /**
     * Strips {@code numChars} leading characters from every line in a CharSequence.
     * <pre class="groovyTestCase">
     * assert 'DEF\n456' == '''ABCDEF\n123456'''.stripIndent(3)
     * </pre>
     *
     * @param self     The CharSequence to strip the characters from
     * @param numChars The number of characters to strip
     * @return the stripped String
     *
     * @since 1.8.2
     */
    public static String stripIndent(final CharSequence self, final int numChars) {
        if (self.length() == 0 || numChars <= 0) return self.toString();
        StringBuilder builder = new StringBuilder();
        for (String line : new LineIterable(self)) {
            // normalize an empty or whitespace line to \n
            // or strip the indent for lines containing non-space characters
            if (!isAllWhitespace(line)) {
                builder.append(stripIndentFromLine(line, numChars));
            }
            builder.append("\n");
        }
        // remove the normalized ending line ending if it was not present
        if (self.charAt(self.length() - 1) != '\n') {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    // TODO: Expose this for stream based stripping?
    private static String stripIndentFromLine(final String line, final int numChars) {
        int length = line.length();
        return numChars <= length ? line.substring(numChars) : "";
    }

    /**
     * Strips leading whitespace/control characters followed by '|' from
     * every line in a CharSequence.
     * <pre class="groovyTestCase">
     * assert 'ABC\n123\n456' == '''ABC
     *                             |123
     *                             |456'''.stripMargin()
     * </pre>
     *
     * @param self The CharSequence to strip the margin from
     * @return the stripped String
     *
     * @since 1.8.2
     *
     * @see #stripMargin(CharSequence,char)
     */
    public static String stripMargin(final CharSequence self) {
        return stripMargin(self, '|');
    }

    /**
     * Strips leading whitespace/control characters followed by {@code marginChar} from
     * every line in a CharSequence.
     * <pre class="groovyTestCase">
     * assert 'ABC\n123\n456' == '''ABC
     *                             *123
     *                             *456'''.stripMargin('*')
     * </pre>
     *
     * @param self       The CharSequence to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped String
     *
     * @since 1.8.2
     */
    public static String stripMargin(final CharSequence self, final char marginChar) {
        if (self.length() == 0) return self.toString();
        StringBuilder builder = new StringBuilder();
        for (String line : new LineIterable(self)) {
            builder.append(stripMarginFromLine(line, marginChar));
            builder.append("\n");
        }
        // remove the normalized ending line ending if it was not present
        if (self.charAt(self.length() - 1) != '\n') {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * Strips leading whitespace/control characters followed by {@code marginChar} from
     * every line in a CharSequence.
     *
     * @param self       The CharSequence to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped CharSequence
     *
     * @since 1.8.2
     */
    public static String stripMargin(final CharSequence self, final CharSequence marginChar) {
        String mc = marginChar.toString();
        if (mc.length() == 0) return stripMargin(self, '|');
        // TODO IllegalArgumentException for marginChar.length() > 1 ? Or support String as marker?
        return stripMargin(self, mc.charAt(0));
    }

    // TODO: Expose this for other usage scenarios, e.g. stream based stripping?
    private static String stripMarginFromLine(final String line, final char marginChar) {
        int length = line.length();
        int index = 0;
        while (index < length && line.charAt(index) <= ' ') index++;
        return (index < length && line.charAt(index) == marginChar) ? line.substring(index + 1) : line;
    }

    /**
     * Returns the first {@code num} elements from this CharSequence.
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.take( 0 ) == ''
     * assert text.take( 2 ) == 'Gr'
     * assert text.take( 7 ) == 'Groovy'
     * </pre>
     *
     * @param self the original CharSequence
     * @param num  the number of chars to take from this CharSequence
     * @return a CharSequence consisting of the first {@code num} chars,
     *         or else the whole CharSequence if it has less then {@code num} elements.
     *
     * @since 1.8.1
     */
    public static CharSequence take(final CharSequence self, final int num) {
        if (num < 0) {
            return self.subSequence(0, 0);
        }
        if (self.length() <= num) {
            return self;
        }
        return self.subSequence(0, num);
    }

    /**
     * A GString variant of the equivalent CharSequence method.
     *
     * @param self the original GString
     * @param num  the number of chars to take from this GString
     * @return a String consisting of the first {@code num} chars,
     *         or else the whole GString if it has less then {@code num} elements.
     *
     * @since 2.3.7
     */
    public static String take(final GString self, final int num) {
        return take(self.toString(), num);
    }

    /**
     * A String variant of the equivalent CharSequence method.
     *
     * @param self the original String
     * @param num  the number of chars to take from this String
     * @return a String consisting of the first {@code num} chars,
     *         or else the whole String if it has less then {@code num} elements.
     *
     * @since 2.5.5
     */
    public static String take(final String self, final int num) {
        return (String) take((CharSequence) self, num);
    }

    /**
     * Returns the longest prefix of this CharSequence where each
     * element passed to the given closure evaluates to true.
     * <p>
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.takeWhile{ it {@code <} 'A' } == ''
     * assert text.takeWhile{ it {@code <} 'Z' } == 'G'
     * assert text.takeWhile{ it != 'v' } == 'Groo'
     * assert text.takeWhile{ it {@code <} 'z' } == 'Groovy'
     * </pre>
     *
     * @param self      the original CharSequence
     * @param condition the closure that must evaluate to true to continue taking elements
     * @return a prefix of elements in the CharSequence where each
     *         element passed to the given closure evaluates to true
     *
     * @since 2.0.0
     */
    @SuppressWarnings("unchecked")
    public static String takeWhile(final CharSequence self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure condition) {
        Iterator selfIter = hasSingleCharacterArg(condition) ? new CharacterIterator(self) : new StringIterator(self);
        return join(DefaultGroovyMethods.takeWhile(selfIter, condition), "");
    }

    @Deprecated // for binary compatibility only
    public static CharSequence takeWhile$$bridge(final CharSequence self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure condition) {
        return takeWhile(self, condition);
    }

    /**
     * A GString variant of the equivalent GString method.
     *
     * @param self      the original GString
     * @param condition the closure that must evaluate to true to continue taking elements
     * @return a prefix of elements in the GString where each
     *         element passed to the given closure evaluates to true
     *
     * @since 2.3.7
     */
    public static String takeWhile(final GString self, @ClosureParams(value=FromString.class, conflictResolutionStrategy=PickFirstResolver.class, options={"String", "Character"}) final Closure condition) {
        return takeWhile(self.toString(), condition);
    }

    /**
     * Parses a CharSequence into a BigDecimal
     *
     * @param self a CharSequence
     * @return a BigDecimal
     *
     * @since 1.8.2
     */
    public static BigDecimal toBigDecimal(final CharSequence self) {
        return new BigDecimal(self.toString().trim());
    }

    /**
     * Parses a CharSequence into a BigInteger
     *
     * @param self a CharSequence
     * @return a BigInteger
     *
     * @since 1.8.2
     */
    public static BigInteger toBigInteger(final CharSequence self) {
        return new BigInteger(self.toString().trim());
    }

    /**
     * Converts the given string into a Boolean object. If the trimmed string is
     * "true", "y" or "1" (ignoring case) then the result is true otherwise it is false.
     *
     * @param self a String
     * @return The Boolean value
     *
     * @since 1.0
     */
    public static Boolean toBoolean(final String self) {
        final String trimmed = self.trim();

        if ("true".equalsIgnoreCase(trimmed) || "y".equalsIgnoreCase(trimmed) || "1".equals(trimmed)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * Converts the given string into a Character object
     * using the first character in the string.
     *
     * @param self a String
     * @return the first Character
     *
     * @since 1.0
     */
    public static Character toCharacter(final String self) {
        return self.charAt(0);
    }

    /**
     * Parses a CharSequence into a Double.
     *
     * @param self a CharSequence
     * @return a Double
     *
     * @since 1.8.2
     */
    public static Double toDouble(final CharSequence self) {
        return Double.valueOf(self.toString().trim());
    }

    /**
     * Parses a CharSequence into a Float.
     *
     * @param self a CharSequence
     * @return a Float
     *
     * @since 1.8.2
     */
    public static Float toFloat(final CharSequence self) {
        return Float.valueOf(self.toString().trim());
    }

    /**
     * Parses a CharSequence into an Integer.
     *
     * @param self a CharSequence
     * @return an Integer
     *
     * @since 1.8.2
     */
    public static Integer toInteger(final CharSequence self) {
        return Integer.valueOf(self.toString().trim());
    }

    /**
     * Tokenizes a CharSequence (with a whitespace as the delimiter).
     *
     * @param self a CharSequence
     * @return a List of tokens
     *
     * @since 1.8.2
     *
     * @see java.util.StringTokenizer#StringTokenizer(String)
     */
    @SuppressWarnings("unchecked")
    public static List<String> tokenize(final CharSequence self) {
        return InvokerHelper.asList(new StringTokenizer(self.toString()));
    }

    /**
     * Tokenizes a CharSequence based on the given character delimiter.
     * <p>
     * For example:
     * <pre class="groovyTestCase">
     * char pathSep = ':'
     * assert "/tmp:/usr".tokenize(pathSep) == ["/tmp", "/usr"]
     * </pre>
     *
     * @param self  a CharSequence
     * @param delimiter the delimiter
     * @return a List of tokens
     *
     * @since 1.8.2
     *
     * @see java.util.StringTokenizer#StringTokenizer(String,String)
     */
    public static List<String> tokenize(final CharSequence self, final Character delimiter) {
        return tokenize(self, delimiter.toString());
    }

    /**
     * Tokenizes a CharSequence based on the given CharSequence. Each character
     * in the CharSequence is a separate delimiter.
     *
     * @param self  a CharSequence
     * @param delimiters the delimiters
     * @return a List of tokens
     *
     * @since 1.8.2
     *
     * @see java.util.StringTokenizer#StringTokenizer(String,String)
     */
    @SuppressWarnings("unchecked")
    public static List<String> tokenize(final CharSequence self, final CharSequence delimiters) {
        return InvokerHelper.asList(new StringTokenizer(self.toString(), delimiters.toString()));
    }

    /**
     * Converts the given CharSequence into a List of Strings of one character.
     *
     * @param self a CharSequence
     * @return a List of characters (a 1-character String)
     *
     * @since 1.8.2
     */
    public static List<String> toList(final CharSequence self) {
        String s = self.toString();
        final int n = s.length();
        List<String> answer = new ArrayList<>(n);
        for (int i = 0; i < n; i += 1) {
            answer.add(s.substring(i, i + 1));
        }
        return answer;
    }

    /**
     * Parses a CharSequence into a Long
     *
     * @param self a CharSequence
     * @return a Long
     *
     * @since 1.8.2
     */
    public static Long toLong(final CharSequence self) {
        return Long.valueOf(self.toString().trim());
    }

    /**
     * Converts the given CharSequence into a Set of unique Strings of one character.
     *
     * @param self a CharSequence
     * @return a Set of unique characters (each a 1-character String)
     *
     * @since 1.8.2
     */
    public static Set<String> toSet(final CharSequence self) {
        return new HashSet<String>(toList(self));
    }

    /**
     * Parses a CharSequence into a Short.
     *
     * @param self a CharSequence
     * @return a Short
     *
     * @since 1.8.2
     */
    public static Short toShort(final CharSequence self) {
        return Short.valueOf(self.toString().trim());
    }

    /**
     * Translates a CharSequence by replacing characters from the sourceSet with characters from replacementSet.
     * If the first character from sourceSet appears in the CharSequence, it will be replaced with the first character from replacementSet.
     * If the second character from sourceSet appears in the CharSequence, it will be replaced with the second character from replacementSet.
     * and so on for all provided replacement characters.
     * <p>
     * Here is an example which converts the vowels in a word from lower to uppercase:
     * <pre class="groovyTestCase">
     * assert 'hello'.tr('aeiou', 'AEIOU') == 'hEllO'
     * </pre>
     * A character range using regex-style syntax can also be used, e.g. here is an example which converts a word from lower to uppercase:
     * <pre class="groovyTestCase">
     * assert 'hello'.tr('a-z', 'A-Z') == 'HELLO'
     * </pre>
     * Hyphens at the start or end of sourceSet or replacementSet are treated as normal hyphens and are not
     * considered to be part of a range specification. Similarly, a hyphen immediately after an earlier range
     * is treated as a normal hyphen. So, '-x', 'x-' have no ranges while 'a-c-e' has the range 'a-c' plus
     * the '-' character plus the 'e' character.
     * <p>
     * Unlike the unix tr command, Groovy's tr command supports reverse ranges, e.g.:
     * <pre class="groovyTestCase">
     * assert 'hello'.tr('z-a', 'Z-A') == 'HELLO'
     * </pre>
     * If replacementSet is smaller than sourceSet, then the last character from replacementSet is used as the replacement for all remaining source characters as shown here:
     * <pre class="groovyTestCase">
     * assert 'Hello World!'.tr('a-z', 'A') == 'HAAAA WAAAA!'
     * </pre>
     * If sourceSet contains repeated characters, the last specified replacement is used as shown here:
     * <pre class="groovyTestCase">
     * assert 'Hello World!'.tr('lloo', '1234') == 'He224 W4r2d!'
     * </pre>
     * The functionality provided by tr can be achieved using regular expressions but tr provides a much more compact
     * notation and efficient implementation for certain scenarios.
     *
     * @param   self the CharSequence that is to be translated
     * @param   sourceSet the set of characters to translate from
     * @param   replacementSet the set of replacement characters
     * @return  The resulting translated {@code String}
     *
     * @since 1.8.2
     *
     * @see org.codehaus.groovy.util.StringUtil#tr(String,String,String)
     */
    public static String tr(final CharSequence self, final CharSequence sourceSet, final CharSequence replacementSet) throws ClassNotFoundException {
        return (String) InvokerHelper.invokeStaticMethod("org.codehaus.groovy.util.StringUtil", "tr", new Object[]{self.toString(), sourceSet.toString(), replacementSet.toString()});
    }

    /**
     * Replaces sequences of whitespaces with tabs using tabStops of size 8.
     *
     * @param self A CharSequence to unexpand
     * @return an unexpanded String
     *
     * @since 1.8.2
     */
    public static String unexpand(final CharSequence self) {
        return unexpand(self, 8);
    }

    /**
     * Replaces sequences of whitespaces with tabs.
     *
     * @param self A CharSequence to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return an unexpanded String
     *
     * @since 1.8.2
     */
    public static String unexpand(final CharSequence self, final int tabStop) {
        if (self.length() == 0) return self.toString();
        StringBuilder builder = new StringBuilder();
        for (String line : new LineIterable(self)) {
            builder.append(unexpandLine(line, tabStop));
            builder.append("\n");
        }
        // remove the normalized ending line ending if it was not present
        if (self.charAt(self.length() - 1) != '\n') {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * Replaces sequences of whitespaces with tabs within a line.
     *
     * @param self A line to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return an unexpanded String
     *
     * @since 1.8.2
     */
    public static String unexpandLine(final CharSequence self, final int tabStop) {
        StringBuilder builder = new StringBuilder(self.toString());
        int index = 0;
        while (index + tabStop < builder.length()) {
            // cut original string in tabstop-length pieces
            String piece = builder.substring(index, index + tabStop);
            // count trailing whitespace characters
            int count = 0;
            while ((count < tabStop) && (Character.isWhitespace(piece.charAt(tabStop - (count + 1)))))
                count++;
            // replace if whitespace was found
            if (count > 0) {
                piece = piece.substring(0, tabStop - count) + '\t';
                builder.replace(index, index + tabStop, piece);
                index = index + tabStop - (count - 1);
            } else
                index = index + tabStop;
        }
        return builder.toString();
    }

    /**
     * Tests if this CharSequence starts with any specified prefixes.
     *
     * @param   prefixes   the prefixes.
     * @return  {@code true} if this CharSequence starts with any specified prefixes.
     *
     * @since   2.4.14
     */
    public static boolean startsWithAny(final CharSequence self, final CharSequence... prefixes) {
        String str = self.toString();

        for (CharSequence prefix : prefixes) {
            if (str.startsWith(prefix.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tests if this CharSequence ends with any specified suffixes.
     *
     * @param   suffixes   the suffixes.
     * @return  {@code true} if this CharSequence ends with any specified suffixes
     *
     * @since   2.4.14
     */
    public static boolean endsWithAny(final CharSequence self, final CharSequence... suffixes) {
        String str = self.toString();

        for (CharSequence suffix : suffixes) {
            if (str.endsWith(suffix.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tests if this CharSequence is blank.
     *
     * @return {@code true} if this CharSequence is blank
     *
     * @since  2.5.0
     *
     * @see #isAllWhitespace(CharSequence)
     */
    public static boolean isBlank(final CharSequence self) {
        if (self == null) {
            return true;
        }
        return self.toString().matches("\\s*");
    }

    /**
     * Returns the last {@code num} elements from this CharSequence.
     *
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.takeRight( 0 ) == ''
     * assert text.takeRight( 2 ) == 'vy'
     * assert text.takeRight( 7 ) == 'Groovy'
     * </pre>
     *
     * @param self the original CharSequence
     * @param num  the number of chars to take from this CharSequence from the right
     * @return a CharSequence consisting of the last {@code num} chars,
     * or else the whole CharSequence if it has less than {@code num} elements.
     *
     * @since 3.0.0
     */
    public static CharSequence takeRight(final CharSequence self, final int num) {
        if (num < 0)
            return self.subSequence(0, 0);

        int begin = Math.max(0, self.length() - num);
        return self.subSequence(begin, self.length());
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeRight(CharSequence,int)}.
     *
     * @param self the original CharSequence
     * @param num  the number of chars to take from this CharSequence from the right
     * @return a String consisting of the last {@code num} chars,
     * or else the whole CharSequence if it has less than {@code num} elements.
     *
     * @since 3.0.0
     */
    public static String takeRight(final String self, final int num) {
        return (String) takeRight((CharSequence) self, num);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeRight(CharSequence,int)}.
     *
     * @param self the original GString
     * @param num  the number of chars to take from this GString from the right
     * @return a String consisting of the last {@code num} chars,
     * or else the whole GString if it has less than {@code num} elements.
     *
     * @since 3.0.0
     */
    public static String takeRight(final GString self, final int num) {
        return takeRight(self.toString(), num);
    }

    /**
     * Returns the {@code CharSequence} that exists after the first occurrence of the given
     * {@code searchString} in this CharSequence.
     *
     * <pre class="groovyTestCase">
     * def text = "Groovy development. Groovy team"
     * assert text.takeAfter( 'Groovy' )           == ' development. Groovy team'
     * assert text.takeAfter( 'team' )             == ''
     * assert text.takeAfter( '' )                 == ''
     * assert text.takeAfter( 'Unavailable text' ) == ''
     * assert text.takeAfter( null )               == ''
     * </pre>
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence that is searched in this CharSequence
     * @return CharSequence that is after the given searchString and empty string if it does not exist
     *
     * @since 3.0.0
     */
    public static CharSequence takeAfter(final CharSequence self, final CharSequence searchString) {
        if (searchString == null || searchString.toString().isEmpty() || self.length() <= searchString.length())
            return self.subSequence(0, 0);

        String s = self.toString();

        int index = s.indexOf(searchString.toString());

        return (index == -1) ? self.subSequence(0, 0) : self.subSequence(index + searchString.length(), self.length());
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeAfter(CharSequence,CharSequence)}.
     *
     * @param self         the original CharSequence
     * @param searchString String that is searched in this CharSequence
     * @return String that is after the given searchString and empty string if it does not exist
     *
     * @since 3.0.0
     */
    public static String takeAfter(final String self, final CharSequence searchString) {
        return (String) takeAfter((CharSequence) self, searchString);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeAfter(CharSequence,CharSequence)}.
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence that is searched in this CharSequence
     * @return String that is after the given searchString and empty string if it does not exist
     *
     * @since 3.0.0
     */
    public static String takeAfter(final GString self, final CharSequence searchString) {
        return takeAfter(self.toString(), searchString);
    }

    /**
     * Returns the {@code CharSequence} that exists before the first occurrence of the given
     * {@code searchString} in this CharSequence.
     *
     * <pre class="groovyTestCase">
     * def text = "Groovy development. Groovy team"
     *
     * assert text.takeBefore( ' Groovy ' )         == 'Groovy development.'
     * assert text.takeBefore( ' ' )                == 'Groovy'
     * assert text.takeBefore( 'Unavailable text' ) == ''
     * assert text.takeBefore( null )               == ''
     * </pre>
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence that is searched in this CharSequence
     * @return CharSequence that is before the given searchString
     *
     * @since 3.0.0
     */
    public static CharSequence takeBefore(final CharSequence self, final CharSequence searchString) {
        if (searchString == null || searchString.toString().isEmpty() || self.length() <= searchString.length())
            return self.subSequence(0, 0);

        String s = self.toString();

        int index = s.indexOf(searchString.toString());

        return (index == -1) ? self.subSequence(0, 0) : self.subSequence(0, index);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeBefore(CharSequence,CharSequence)}.
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence that is searched in this CharSequence
     * @return String that is before the given searchString
     *
     * @since 3.0.0
     */
    public static String takeBefore(final GString self, final String searchString) {
        return takeBefore(self.toString(), searchString);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeBefore(CharSequence,CharSequence)}.
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence that is searched in this CharSequence
     * @return String that is before the given searchString
     *
     * @since 3.0.0
     */
    public static String takeBefore(final String self, final String searchString) {
        return (String) takeBefore((CharSequence) self, searchString);
    }

    /**
     * Returns new CharSequence after removing the right {@code num} chars.
     * Returns empty String if the {@code num} is greater than the length of the CharSequence.
     *
     * <pre class="groovyTestCase">
     * def text = "groovy"
     *
     * assert text.dropRight(  3 ) == 'gro'
     * assert text.dropRight(  6 ) == ''
     * assert text.dropRight(  0 ) == 'groovy'
     * assert text.dropRight( -1 ) == 'groovy'
     * assert text.dropRight( 10 ) == ''
     * </pre>
     *
     * @param self the original CharSequence
     * @param num  number of characters
     * @return CharSequence after removing the right {@code num} chars and empty of the {@code num} is greater than the
     * length of the CharSequence
     *
     * @since 3.0.0
     */
    public static CharSequence dropRight(final CharSequence self, final int num) {

        if (num < 0)
            return self;

        if (num >= self.length())
            return self.subSequence(0, 0);

        return take(self, self.length() - num);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #dropRight(CharSequence,int)}.
     *
     * @param self the original CharSequence
     * @param num  number of characters
     * @return String after removing the right {@code num} chars and empty of the {@code num} is greater than the
     * length of the CharSequence
     *
     * @since 3.0.0
     */
    public static String dropRight(final String self, final int num) {
        return (String) dropRight((CharSequence) self, num);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #dropRight(CharSequence,int)}.
     *
     * @param self the original CharSequence
     * @param num  number of characters
     * @return String after removing the right {@code num} chars and empty of the {@code num} is greater than the
     * length of the CharSequence
     *
     * @since 3.0.0
     */
    public static String dropRight(final GString self, final int num) {
        return dropRight(self.toString(), num);
    }

    /**
     * Returns the CharSequence that is in between the first occurrence of the given {@code from} and {@code to}
     * CharSequences and empty if the unavailable inputs are given.
     *
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     *
     * assert text.takeBetween( 'r', 'v' ) == 'oo'
     * assert text.takeBetween( 'r', 'z' ) == ''
     * assert text.takeBetween( 'a', 'r' ) == ''
     * </pre>
     *
     * @param self the original CharSequence
     * @param from beginning of search
     * @param to   end of search
     * @return the CharSequence that is in between the given two CharSequences and empty if the unavailable inputs are given
     *
     * @since 3.0.0
     *
     * @see #takeBetween(CharSequence,CharSequence,CharSequence,int)
     */
    public static CharSequence takeBetween(final CharSequence self, final CharSequence from, final CharSequence to) {
        if (from == null || to == null || from.length() == 0 || to.length() == 0 || from.length() > self.length() || to.length() > self.length())
            return self.subSequence(0, 0);

        String s = self.toString();
        String f = from.toString();

        int fi = s.indexOf(f);

        if (fi == -1)
            return self.subSequence(0, 0);

        String t = to.toString();

        int ti = s.indexOf(t, fi + from.length());

        if (ti == -1)
            return self.subSequence(0, 0);

        return self.subSequence(fi + from.length(), ti);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence,CharSequence)}.
     *
     * @param self the original CharSequence
     * @param from beginning of search
     * @param to   end of search
     * @return String that is in between the given two CharSequences and empty if the unavailable inputs are given
     *
     * @since 3.0.0
     */
    public static String takeBetween(final String self, final CharSequence from, final CharSequence to) {
        return (String) takeBetween((CharSequence) self, from, to);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence,CharSequence)}.
     *
     * @param self the original CharSequence
     * @param from beginning of search
     * @param to   end of search
     * @return String that is in between the given two CharSequences and empty if the unavailable inputs are given
     *
     * @since 3.0.0
     */
    public static String takeBetween(final GString self, final CharSequence from, final CharSequence to) {
        return takeBetween(self.toString(), from, to);
    }

    /**
     * Takes the characters between the first occurrence of the two subsequent {@code enclosure} strings.
     *
     * <pre class="groovyTestCase">
     * def text = "name = 'some name'"
     *
     * assert text.takeBetween( "'" ) == 'some name'
     * assert text.takeBetween( 'z' ) == ''
     * </pre>
     *
     * @param self      Original CharSequence
     * @param enclosure Enclosure CharSequence
     * @return CharSequence between the 2 subsequent {@code enclosure} strings
     *
     * @since 3.0.0
     *
     * @see #takeBetween(CharSequence,CharSequence,int)
     */
    public static CharSequence takeBetween(final CharSequence self, final CharSequence enclosure) {
        return takeBetween(self, enclosure, enclosure);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence)}.
     *
     * @param self      Original String
     * @param enclosure Enclosure CharSequence
     * @return String between the 2 subsequent {@code enclosure} strings
     *
     * @since 3.0.0
     */
    public static String takeBetween(final String self, final CharSequence enclosure) {
        return (String) takeBetween((CharSequence) self, enclosure);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence)}.
     *
     * @param self      Original GString
     * @param enclosure Enclosure CharSequence
     * @return String between the 2 subsequent {@code enclosure} strings
     *
     * @since 3.0.0
     */
    public static String takeBetween(final GString self, final CharSequence enclosure) {
        return takeBetween(self.toString(), enclosure);
    }

    /**
     * Returns the CharSequence that is in between the given the nth (specified by occurrence) pair of
     * {@code from} and {@code to} CharSequences and empty if the unavailable inputs are given.
     *
     * <pre class="groovyTestCase">
     * def text = "t1=10 ms, t2=100 ms"
     *
     * assert text.takeBetween( '=', ' ', 0 ) == '10'
     * assert text.takeBetween( '=', ' ', 1 ) == '100'
     * assert text.takeBetween( 't1', 'z' ) == ''
     * </pre>
     *
     * @param self       the original CharSequence
     * @param from       beginning of search
     * @param to         end of search
     * @param occurrence nth occurrence that is to be returned. 0 represents first one
     * @return the CharSequence that is in between the given the nth (specified by occurrence) pair of
     * {@code from} and {@code to} CharSequences and empty if the unavailable inputs are given.
     *
     * @since 3.0.0
     *
     * @see #takeBetween(CharSequence,CharSequence,CharSequence)
     */
    public static CharSequence takeBetween(final CharSequence self, final CharSequence from, final CharSequence to, final int occurrence) {
        if (from == null || to == null || from.length() > self.length() || to.length() > self.length() || (to.length() + from.length() >= self.length()) || occurrence < 0)
            return self.subSequence(0, 0);

        String s = self.toString();
        String f = from.toString();

        int start = 0;
        int counter = 0;

        while (counter <= occurrence) {
            int fi = s.indexOf(f, start);

            if (fi == -1)
                return self.subSequence(0, 0);

            int ti = s.indexOf(to.toString(), fi + f.length());

            if (ti == -1)
                return self.subSequence(0, 0);

            if (counter == occurrence)
                return self.subSequence(fi + f.length(), ti);

            start = ti + to.length() + 1;
            counter++;
        }

        return self.subSequence(0, 0);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence,CharSequence,int)}.
     *
     * @param self       the original String
     * @param from       beginning of search
     * @param to         end of search
     * @param occurrence nth occurrence that is to be returned. 0 represents first one
     * @return the String that is in between the given nth (specified by occurrence) pair of
     * {@code from} and {@code to} CharSequences and empty if the unavailable inputs are given.
     *
     * @since 3.0.0
     */
    public static String takeBetween(final String self, final CharSequence from, final CharSequence to, final int occurrence) {
        return (String) takeBetween((CharSequence) self, from, to, occurrence);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence,CharSequence,int)}.
     *
     * @param self       the original GString
     * @param from       beginning of search
     * @param to         end of search
     * @param occurrence nth occurrence that is to be returned. 0 represents first one
     * @return the String that is in between the given nth (specified by occurrence) pair of
     * {@code from} and {@code to} CharSequences and empty if the unavailable inputs are given.
     *
     * @since 3.0.0
     */
    public static String takeBetween(final GString self, final CharSequence from, final CharSequence to, final int occurrence) {
        return takeBetween(self.toString(), from, to, occurrence);
    }

    /**
     * Takes the characters between nth (specified by occurrence) pair of {@code enclosure} strings.
     *
     * <pre class="groovyTestCase">
     * def text = "t1='10' ms, t2='100' ms"
     *
     * assert text.takeBetween( "'", 0 ) == '10'
     * assert text.takeBetween( "'", 1 ) == '100'
     * assert text.takeBetween( "'", 2 ) == ''
     * </pre>
     *
     * @param self       Original CharSequence
     * @param enclosure  Enclosure CharSequence
     * @param occurrence nth occurrence being returned
     * @return CharSequence between the nth occurrence of pair of {@code enclosure} strings
     *
     * @since 3.0.0
     *
     * @see #takeBetween(CharSequence,CharSequence,int)
     */
    public static CharSequence takeBetween(final CharSequence self, final CharSequence enclosure, final int occurrence) {
        return takeBetween(self, enclosure, enclosure, occurrence);
    }

    /**
     * A String variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence,int)}.
     *
     * @param self       Original String
     * @param enclosure  Enclosure CharSequence
     * @param occurrence nth occurrence being returned
     * @return String between the nth occurrence of pair of {@code enclosure} strings
     *
     * @since 3.0.0
     */
    public static String takeBetween(final String self, final CharSequence enclosure, int occurrence) {
        return (String) takeBetween((CharSequence) self, enclosure, occurrence);
    }

    /**
     * A GString variant of the equivalent CharSequence method {@link #takeBetween(CharSequence,CharSequence,int)}.
     *
     * @param self       Original GString
     * @param enclosure  Enclosure CharSequence
     * @param occurrence nth occurrence being returned
     * @return String between the nth occurrence of pair of {@code enclosure} strings
     *
     * @since 3.0.0
     */
    public static String takeBetween(final GString self, final CharSequence enclosure, final int occurrence) {
        return takeBetween(self.toString(), enclosure, occurrence);
    }

    /**
     * Checks whether this CharSequence starts with the {@code searchString} ignoring the case considerations.
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence being checked against this
     * @return {@code true} if the character sequence represented by the argument is a prefix of this CharSequence
     * ignoring the case considerations. {@code false} otherwise. Returns false if the argument is null
     *
     * @since 3.0.0
     */
    public static boolean startsWithIgnoreCase(final CharSequence self, final CharSequence searchString) {
        if (searchString == null || searchString.length() == 0 || self.length() < searchString.length())
            return false;

        String s = take(self.toString(), searchString.length());

        return s.equalsIgnoreCase(searchString.toString());
    }

    /**
     * Checks whether this CharSequence ends with the {@code searchString} ignoring the case considerations.
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence bring checked against this
     * @return {@code true} if the character sequence represented by the argument is a suffix of this CharSequence
     * ignoring the case considerations. {@code false} otherwise. Returns false if the argument is null
     *
     * @since 3.0.0
     */
    public static boolean endsWithIgnoreCase(final CharSequence self, final CharSequence searchString) {
        if (searchString == null || searchString.length() == 0 || self.length() < searchString.length())
            return false;

        String s = takeRight(self.toString(), searchString.length());

        return s.equalsIgnoreCase(searchString.toString());
    }

    /**
     * Checks whether this CharSequence contains the {@code searchString} ignoring the caseConsiderations.
     *
     * @param self         the original CharSequence
     * @param searchString CharSequence being checked against this
     * @return {@code true} if the character sequence represented by the argument exists in this CharSequence
     * ignoring the case considerations. {@code false} otherwise. Returns false if the argument is null
     *
     * @since 3.0.0
     */
    public static boolean containsIgnoreCase(final CharSequence self, final CharSequence searchString) {
        if (searchString == null || searchString.length() == 0 || self.length() < searchString.length())
            return false;

        return self.toString().toLowerCase().contains(searchString.toString().toLowerCase());
    }
}
