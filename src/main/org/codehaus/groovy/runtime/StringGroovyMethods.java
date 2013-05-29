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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.EmptyRange;
import groovy.lang.GString;
import groovy.lang.IntRange;
import groovy.lang.Range;

import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.callClosureForLine;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.each;

/**
 * This class defines new groovy methods which appear on String-related JDK
 * classes (String, CharSequence, Matcher) inside the Groovy environment.
 * Static methods are used with the
 * first parameter being the destination class,
 * e.g.. <code>public static String reverse(String self)</code>
 * provides a <code>reverse()</code> method for <code>String</code>.
 * <p>
 * NOTE: While this class contains many 'public' static methods, it is
 * primarily regarded as an internal class (its internal package name
 * suggests this also). We value backwards compatibility of these
 * methods when used within Groovy but value less backwards compatibility
 * at the Java method call level. I.e. future versions of Groovy may
 * remove or move a method call in this file but would normally
 * aim to keep the method available from within Groovy.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Jeremy Rayner
 * @author Sam Pullara
 * @author Rod Cope
 * @author Guillaume Laforge
 * @author John Wilson
 * @author Hein Meling
 * @author Dierk Koenig
 * @author Pilho Kim
 * @author Marc Guillemot
 * @author Russel Winder
 * @author bing ran
 * @author Jochen Theodorou
 * @author Paul King
 * @author Michael Baehr
 * @author Joachim Baumann
 * @author Alex Tkachman
 * @author Ted Naleid
 * @author Brad Long
 * @author Jim Jagielski
 * @author Rodolfo Velasco
 * @author jeremi Joslin
 * @author Hamlet D'Arcy
 * @author Cedric Champeau
 * @author Tim Yates
 * @author Dinko Srkoc
 * @author Pascal Lombard
 * @author Christophe Charles
 */
public class StringGroovyMethods extends DefaultGroovyMethodsSupport {

    static String lineSeparator = null;

    /**
     * Coerce a string (an instance of CharSequence) to a boolean value.
     * A string is coerced to false if it is of length 0,
     * and to true otherwise.
     *
     * @param string the character sequence
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(CharSequence string) {
        return string.length() > 0;
    }

    /**
     * Coerce a Matcher instance to a boolean value.
     *
     * @param matcher the matcher
     * @return the boolean value
     * @since 1.7.0
     */
    public static boolean asBoolean(Matcher matcher) {
        RegexSupport.setLastMatcher(matcher);
        return matcher.find();
    }

    /**
     * <p>Provides a method to perform custom 'dynamic' type conversion
     * to the given class using the <code>as</code> operator.
     *
     * @param self a CharSequence
     * @param c    the desired class
     * @return the converted object
     * @see #asType(String, Class)
     * @since 1.8.2
     */
    public static <T> T asType(CharSequence self, Class<T> c) {
        return asType(self.toString(), c);
    }

    /**
     * Converts the GString to a File, or delegates to the default
     * {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#asType(Object, Class)}
     *
     * @param self a GString
     * @param c    the desired class
     * @return the converted object
     * @since 1.5.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(GString self, Class<T> c) {
        if (c == File.class) {
            return (T) new File(self.toString());
        } else if (Number.class.isAssignableFrom(c) || c.isPrimitive()) {
            return asType(self.toString(), c);
        }
        return DefaultGroovyMethods.asType((Object) self, c);
    }

    /**
     * Provides a method to perform custom 'dynamic' type conversion
     * to the given class using the <code>as</code> operator.
     * <strong>Example:</strong> <code>'123' as Double</code>
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
     * <li>Subclasses of Enum (Java 5 and above)</li>
     * </ul>
     * If any other type is given, the call is delegated to
     * {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#asType(Object, Class)}.
     *
     * @param self a String
     * @param c    the desired class
     * @return the converted object
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T asType(String self, Class<T> c) {
        if (c == List.class) {
            return (T) toList(self);
        } else if (c == BigDecimal.class) {
            return (T) toBigDecimal(self);
        } else if (c == BigInteger.class) {
            return (T) toBigInteger(self);
        } else if (c == Long.class || c == Long.TYPE) {
            return (T) toLong(self);
        } else if (c == Integer.class || c == Integer.TYPE) {
            return (T) toInteger(self);
        } else if (c == Short.class || c == Short.TYPE) {
            return (T) toShort(self);
        } else if (c == Byte.class || c == Byte.TYPE) {
            return (T) Byte.valueOf(self.trim());
        } else if (c == Character.class || c == Character.TYPE) {
            return (T) toCharacter(self);
        } else if (c == Double.class || c == Double.TYPE) {
            return (T) toDouble(self);
        } else if (c == Float.class || c == Float.TYPE) {
            return (T) toFloat(self);
        } else if (c == File.class) {
            return (T) new File(self);
        } else if (DefaultTypeTransformation.isEnumSubclass(c)) {
            return (T) InvokerHelper.invokeMethod(c, "valueOf", new Object[]{ self });
        }
        return DefaultGroovyMethods.asType((Object) self, c);
    }

    /**
     * Turns a CharSequence into a regular expression Pattern
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     * @since 1.8.2
     */
    public static Pattern bitwiseNegate(CharSequence self) {
        return Pattern.compile(self.toString());
    }

    /**
     * Turns a String into a regular expression Pattern
     *
     * @param self a String to convert into a regular expression
     * @return the regular expression pattern
     * @since 1.5.0
     */
    public static Pattern bitwiseNegate(String self) {
        return Pattern.compile(self);
    }

    /**
     * Convenience method to capitalize the first letter of a CharSequence.
     *
     * @param self The CharSequence to capitalize
     * @return The capitalized CharSequence
     * @see #capitalize(String)
     * @since 1.8.2
     */
    public static CharSequence capitalize(CharSequence self) {
        return capitalize(self.toString());
    }

    /**
     * Convenience method to capitalize the first letter of a string
     * (typically the first letter of a word). Example usage:
     * <pre class="groovyTestCase">
     * assert 'h'.capitalize() == 'H'
     * assert 'hello'.capitalize() == 'Hello'
     * assert 'hello world'.capitalize() == 'Hello world'
     * assert 'Hello World' ==
     *     'hello world'.split(' ').collect{ it.capitalize() }.join(' ')
     * </pre>
     *
     * @param self The string to capitalize
     * @return The capitalized String
     * @since 1.7.3
     */
    public static String capitalize(String self) {
        if (self == null || self.length() == 0) return self;
        return Character.toUpperCase(self.charAt(0)) + self.substring(1);
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt> by adding the space character around it as many times as needed so that it remains centered.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @return the CharSequence centered with padded characters around it
     * @see #center(String, Number)
     * @since 1.8.2
     */
    public static CharSequence center(CharSequence self, Number numberOfChars) {
        return center(self.toString(), numberOfChars);
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt>, appending the supplied padding CharSequence around the original as many times as needed keeping it centered.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence centered with padded characters around it
     * @see #center(String, Number, String)
     * @since 1.8.2
     */
    public static CharSequence center(CharSequence self, Number numberOfChars, CharSequence padding) {
        return center(self.toString(), numberOfChars, padding.toString());
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt> by adding the space character around it as many times as needed so that it remains centered.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
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
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the String centered with padded characters around it
     * @see #center(String, Number, String)
     * @since 1.0
     */
    public static String center(String self, Number numberOfChars) {
        return center(self, numberOfChars, " ");
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt>, appending the supplied padding String around the original as many times as needed keeping it centered.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
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
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @param padding       the characters used for padding
     * @return the String centered with padded characters around it
     * @since 1.0
     */
    public static String center(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
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
     * Provide an implementation of contains() like
     * {@link java.util.Collection#contains(Object)} to make CharSequences more polymorphic.
     *
     * @param self a CharSequence
     * @param text the CharSequence to look for
     * @return true if this CharSequence contains the given text
     * @see #contains(String, String)
     * @since 1.8.2
     */
    public static boolean contains(CharSequence self, CharSequence text) {
        return contains(self.toString(), text.toString());
    }

    /**
     * Provide an implementation of contains() like
     * {@link java.util.Collection#contains(Object)} to make Strings more polymorphic.
     * This method is not required on JDK 1.5 onwards
     *
     * @param self a String
     * @param text a String to look for
     * @return true if this string contains the given text
     * @since 1.0
     */
    public static boolean contains(String self, String text) {
        int idx = self.indexOf(text);
        return idx >= 0;
    }

    /**
     * Count the number of occurrences of a sub CharSequence.
     *
     * @param self a CharSequence
     * @param text a sub CharSequence
     * @return the number of occurrences of the given CharSequence inside this CharSequence
     * @see #count(String, String)
     * @since 1.8.2
     */
    public static int count(CharSequence self, CharSequence text) {
        return count(self.toString(), text.toString());
    }

    /**
     * Count the number of occurrences of a substring.
     *
     * @param self a String
     * @param text a substring
     * @return the number of occurrences of the given string inside this String
     * @since 1.0
     */
    public static int count(String self, String text) {
        int answer = 0;
        for (int idx = 0; true; idx++) {
            idx = self.indexOf(text, idx);
            // break once idx goes to -1 or for case of empty string once
            // we get to the end to avoid JDK library bug (see GROOVY-5858)
            if (idx < answer) break;
            ++answer;
        }
        return answer;
    }

    private static StringBufferWriter createStringBufferWriter(StringBuffer self) {
        return new StringBufferWriter(self);
    }

    private static StringWriter createStringWriter(String self) {
        StringWriter answer = new StringWriter();
        answer.write(self);
        return answer;
    }

    /**
     * Return a CharSequence with lines (separated by LF, CR/LF, or CR)
     * terminated by the platform specific line separator.
     *
     * @param self a CharSequence object
     * @return the denormalized CharSequence
     * @see #denormalize(String)
     * @since 1.8.2
     */
    public static CharSequence denormalize(final CharSequence self) {
        return denormalize(self.toString());
    }

    /**
     * Return a String with lines (separated by LF, CR/LF, or CR)
     * terminated by the platform specific line separator.
     *
     * @param self a String object
     * @return the denormalized string
     * @since 1.6.0
     */
    public static String denormalize(final String self) {
        // Don't do this in static initializer because we may never be needed.
        // TODO: Put this lineSeparator property somewhere everyone can use it.
        if (lineSeparator == null) {
            final StringWriter sw = new StringWriter(2);
            try {
                // We use BufferedWriter rather than System.getProperty because
                // it has the security manager rigamarole to deal with the possible exception.
                final BufferedWriter bw = new BufferedWriter(sw);
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
            return self;
        }

        final StringBuilder sb = new StringBuilder((110 * len) / 100);

        int i = 0;

        while (i < len) {
            final char ch = self.charAt(i++);

            switch (ch) {
                case '\r':
                    sb.append(lineSeparator);

                    // Eat the following LF if any.
                    if ((i < len) && (self.charAt(i) == '\n')) {
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
     * @param num the number of characters to drop from this iterator
     * @return a CharSequence consisting of all characters except the first <code>num</code> ones,
     *         or else an empty String, if this CharSequence has less than <code>num</code> characters.
     * @since 1.8.1
     */
    public static CharSequence drop(CharSequence self, int num) {
        if( num <= 0 ) {
            return self ;
        }
        if( self.length() <= num ) {
            return self.subSequence( 0, 0 ) ;
        }
        return self.subSequence(num, self.length()) ;
    }

    /**
     * Create a suffix of the given CharSequence by dropping as many characters as possible from the
     * front of the original CharSequence such that calling the given closure condition evaluates to
     * true when passed each of the dropped characters.
     * <p>
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.dropWhile{ false } == 'Groovy'
     * assert text.dropWhile{ true } == ''
     * assert text.dropWhile{ it < 'Z' } == 'roovy'
     * assert text.dropWhile{ it != 'v' } == 'vy'
     * </pre>
     *
     * @param self      the original CharSequence
     * @param condition the closure that while continuously evaluating to true will cause us to drop elements from
     *                  the front of the original CharSequence
     * @return the shortest suffix of the given CharSequence such that the given closure condition
     *         evaluates to true for each element dropped from the front of the CharSequence
     * @since 2.0.0
     */
    public static CharSequence dropWhile(CharSequence self, Closure condition) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (num < self.length()) {
            char value = self.charAt(num);
            if (bcw.call(value)) {
                num += 1;
            } else {
                break;
            }
        }
        return drop(self, num);
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
     * @see #eachLine(String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> T eachLine(CharSequence self, Closure<T> closure) throws IOException {
        return eachLine(self.toString(), closure);
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
     * @see #eachLine(String, int, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> T eachLine(CharSequence self, int firstLine, Closure<T> closure) throws IOException {
        return eachLine(self.toString(), firstLine, closure);
    }

    /**
     * Iterates through this String line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a String
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see #eachLine(String, int, groovy.lang.Closure)
     * @since 1.5.5
     */
    public static <T> T eachLine(String self, Closure<T> closure) throws IOException {
        return eachLine(self, 0, closure);
    }

    /**
     * Iterates through this String line by line.  Each line is passed
     * to the given 1 or 2 arg closure. If a 2 arg closure is found
     * the line count is passed as the second argument.
     *
     * @param self    a String
     * @param firstLine the line number value used for the first line (default is 1, set to 0 to start counting from 0)
     * @param closure a closure (arg 1 is line, optional arg 2 is line number)
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @since 1.5.7
     */
    public static <T> T eachLine(String self, int firstLine, Closure<T> closure) throws IOException {
        int count = firstLine;
        T result = null;
        for (String line : readLines(self)) {
            result = callClosureForLine(closure, line, count);
            count++;
        }
        return result;
    }

    /**
     * Iterate through this String a character at a time collecting either the
     * original character or a transformed replacement String. The {@code transform}
     * Closure should return {@code null} to indicate that no transformation is
     * required for the given character.
     * <p>
     * <pre class="groovyTestCase">
     * assert "Groovy".collectReplacements{ it == 'o' ? '_O_' : null } == 'Gr_O__O_vy'
     * assert "B&W".collectReplacements{ it == '&' ? '&amp;' : null } == 'B&amp;W'
     * </pre>
     *
     * @param orig the original String
     * @return A new string in which all characters that require escaping
     *         have been replaced with the corresponding replacements
     *         as determined by the {@code transform} Closure.
     */
    public static String collectReplacements(String orig, Closure<String> transform) {
        if (orig == null) return orig;

        StringBuilder sb = null; // lazy create for edge-case efficiency
        for (int i = 0, len = orig.length(); i < len; i++) {
            final char ch = orig.charAt(i);
            final String replacement = transform.call(ch);

            if (replacement != null) {
                // output differs from input; we write to our local buffer
                if (sb == null) {
                    sb = new StringBuilder((int) (1.1 * len));
                    sb.append(orig.substring(0, i));
                }
                sb.append(replacement);
            } else if (sb != null) {
                // earlier output differs from input; we write to our local buffer
                sb.append(ch);
            }
        }

        return sb == null ? orig : sb.toString();
    }

    /**
     * Process each regex group matched substring of the given CharSequence. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source CharSequence
     * @param regex   a Regex CharSequence
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source CharSequence
     * @see #eachMatch(String, String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static String eachMatch(CharSequence self, CharSequence regex, Closure closure) {
        return eachMatch(self.toString(), regex.toString(), closure);
    }

    /**
     * Process each regex group matched substring of the given pattern. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source CharSequence
     * @param pattern a regex Pattern
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source CharSequence
     * @see #eachMatch(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static String eachMatch(CharSequence self, Pattern pattern, Closure closure) {
        return eachMatch(self.toString(), pattern, closure);
    }

    /**
     * Process each regex group matched substring of the given pattern. If the closure
     * parameter takes one argument, an array with all match groups is passed to it.
     * If the closure takes as many arguments as there are match groups, then each
     * parameter will be one match group.
     *
     * @param self    the source string
     * @param pattern a regex Pattern
     * @param closure a closure with one parameter or as much parameters as groups
     * @return the source string
     * @since 1.6.1
     */
    public static String eachMatch(String self, Pattern pattern, Closure closure) {
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
     * @since 1.6.0
     */
    public static String eachMatch(String self, String regex, Closure closure) {
        return eachMatch(self, Pattern.compile(regex), closure);
    }

    /**
     * Expands all tabs into spaces with tabStops of size 8.
     *
     * @param self A CharSequence to expand
     * @return The expanded CharSequence
     * @see #expand(String)
     * @since 1.8.2
     */
    public static CharSequence expand(CharSequence self) {
        return expand(self.toString(), 8);
    }

    /**
     * Expands all tabs into spaces. If the CharSequence has multiple
     * lines, expand each line - restarting tab stops at the start
     * of each line.
     *
     * @param self A CharSequence to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded CharSequence
     * @see #expand(String, int)
     * @since 1.8.2
     */
    public static CharSequence expand(CharSequence self, int tabStop) {
        return expand(self.toString(), tabStop);
    }

    /**
     * Expands all tabs into spaces with tabStops of size 8.
     *
     * @param self A String to expand
     * @return The expanded String
     * @since 1.7.3
     * @see #expand(String, int)
     */
    public static String expand(String self) {
        return expand(self, 8);
    }

    /**
     * Expands all tabs into spaces. If the String has multiple
     * lines, expand each line - restarting tab stops at the start
     * of each line.
     *
     * @param self A String to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded String
     * @since 1.7.3
     */
    public static String expand(String self, int tabStop) {
        if (self.length() == 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                builder.append(expandLine(line, tabStop));
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * Expands all tabs into spaces. Assumes the CharSequence represents a single line of text.
     *
     * @param self A line to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded CharSequence
     * @see #expandLine(String, int)
     * @since 1.8.2
     */
    public static CharSequence expandLine(CharSequence self, int tabStop) {
        return expandLine(self.toString(), tabStop);
    }

    /**
     * Expands all tabs into spaces. Assumes the String represents a single line of text.
     *
     * @param self A line to expand
     * @param tabStop The number of spaces a tab represents
     * @return The expanded String
     * @since 1.7.3
     */
    public static String expandLine(String self, int tabStop) {
        int index;
        while ((index = self.indexOf('\t')) != -1) {
            StringBuilder builder = new StringBuilder(self);
            int count = tabStop - index % tabStop;
            builder.deleteCharAt(index);
            for (int i = 0; i < count; i++) builder.insert(index, " ");
            self = builder.toString();
        }
        return self;
    }

    /**
     * Finds the first occurrence of a regular expression CharSequence within a CharSequence.
     *
     * @param self  a CharSequence
     * @param regex the capturing regex
     * @return a CharSequence containing the matched portion, or null if the regex doesn't match
     * @see #find(String, java.util.regex.Pattern)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, CharSequence regex) {
        return find(self.toString(), Pattern.compile(regex.toString()));
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a CharSequence.
     * If the regex doesn't match, the closure will not be called and find will return null.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex CharSequence
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a CharSequence containing the result of the closure, or null if the regex pattern doesn't match
     * @see #find(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, CharSequence regex, Closure closure) {
        return find(self.toString(), Pattern.compile(regex.toString()), closure);
    }

    /**
     * Finds the first occurrence of a compiled regular expression Pattern within a CharSequence.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @return a CharSequence containing the matched portion, or null if the regex pattern doesn't match
     * @see #find(String, java.util.regex.Pattern)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, Pattern pattern) {
        return find(self.toString(), pattern);
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a
     * CharSequence.&#160;If the regex doesn't match, the closure will not be called and find will return null.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a CharSequence containing the result of the closure, or null if the regex pattern doesn't match
     * @see #find(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static CharSequence find(CharSequence self, Pattern pattern, Closure closure) {
        return find(self.toString(), pattern, closure);
    }

    /**
     * Finds the first occurrence of a compiled regular expression Pattern within a String.
     * If the pattern doesn't match, null will be returned.
     * <p>
     * For example, if the pattern doesn't match the result is null:
     * <pre>
     *     assert null == "New York, NY".find(~/\d{5}/)
     * </pre>
     *
     * If it does match, we get the matching string back:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/\d{5}/)
     * </pre>
     *
     * If we have capture groups in our expression, the groups are ignored and
     * we get back the full match:
     * <pre>
     *      assert "10292-0098" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/)
     * </pre>
     * If you need to work with capture groups, then use the closure version
     * of this method or use Groovy's matcher operators or use <tt>eachMatch</tt>.
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @return a String containing the matched portion, or null if the regex pattern doesn't match
     * @since 1.6.1
     */
    public static String find(String self, Pattern pattern) {
        Matcher matcher = pattern.matcher(self);
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
     * <pre>
     *     assert null == "New York, NY".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     *
     * If it does match and we don't have any capture groups in our regex, there is a single parameter
     * on the closure that the match gets passed to:
     * <pre>
     *      assert "-10292-" == "New York, NY 10292-0098".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     *
     * If we have capture groups in our expression, our closure has one parameter for the match, followed by
     * one for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert match == "10292-0098"
     *          assert zip == "10292"
     *          assert plusFour == "0098"
     *          return zip
     *      }
     * </pre>
     * If we have capture groups in our expression, and our closure has one parameter,
     * the closure will be passed an array with the first element corresponding to the whole match,
     * followed by an element for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert array[0] == "10292-0098"
     *          assert array[1] == "10292"
     *          assert array[2] == "0098"
     *          return array[1]
     *      }
     * </pre>
     * If a capture group is optional, and doesn't match, then the corresponding value
     * for that capture group passed to the closure will be null as illustrated here:
     * <pre>
     *      assert "2339999" == "adsf 233-9999 adsf".find(~/(\d{3})?-?(\d{3})-(\d{4})/) { match, areaCode, exchange, stationNumber ->
     *          assert "233-9999" == match
     *          assert null == areaCode
     *          assert "233" == exchange
     *          assert "9999" == stationNumber
     *          return "$exchange$stationNumber"
     *      }
     * </pre>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a String containing the result of the closure, or null if the regex pattern doesn't match
     * @since 1.6.1
     */
    public static String find(String self, Pattern pattern, Closure closure) {
        Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            if (hasGroup(matcher)) {
                int count = matcher.groupCount();
                List groups = new ArrayList(count);
                for (int i = 0; i <= count; i++) {
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
     * Finds the first occurrence of a regular expression String within a String.
     * If the regex doesn't match, null will be returned.
     * <p>
     * For example, if the regex doesn't match the result is null:
     * <pre>
     *     assert null == "New York, NY".find(/\d{5}/)
     * </pre>
     *
     * If it does match, we get the matching string back:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(/\d{5}/)
     * </pre>
     *
     * If we have capture groups in our expression, we still get back the full match
     * <pre>
     *      assert "10292-0098" == "New York, NY 10292-0098".find(/(\d{5})-?(\d{4})/)
     * </pre>
     *
     * @param self  a String
     * @param regex the capturing regex
     * @return a String containing the matched portion, or null if the regex doesn't match
     * @since 1.6.1
     */
    public static String find(String self, String regex) {
        return find(self, Pattern.compile(regex));
    }

    /**
     * Returns the result of calling a closure with the first occurrence of a regular expression found within a String.
     * If the regex doesn't match, the closure will not be called and find will return null.
     * <p>
     * For example, if the regex doesn't match, the result is null:
     * <pre>
     *     assert null == "New York, NY".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     *
     * If it does match and we don't have any capture groups in our regex, there is a single parameter
     * on the closure that the match gets passed to:
     * <pre>
     *      assert "-10292-" == "New York, NY 10292-0098".find(~/\d{5}/) { match -> return "-$match-"}
     * </pre>
     *
     * If we have capture groups in our expression, our closure has one parameter for the match, followed by
     * one for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert match == "10292-0098"
     *          assert zip == "10292"
     *          assert plusFour == "0098"
     *          return zip
     *      }
     * </pre>
     * If we have capture groups in our expression, and our closure has one parameter,
     * the closure will be passed an array with the first element corresponding to the whole match,
     * followed by an element for each of the capture groups:
     * <pre>
     *      assert "10292" == "New York, NY 10292-0098".find(~/(\d{5})-?(\d{4})/) { match, zip, plusFour ->
     *          assert array[0] == "10292-0098"
     *          assert array[1] == "10292"
     *          assert array[2] == "0098"
     *          return array[1]
     *      }
     * </pre>
     * If a capture group is optional, and doesn't match, then the corresponding value
     * for that capture group passed to the closure will be null as illustrated here:
     * <pre>
     *      assert "2339999" == "adsf 233-9999 adsf".find(~/(\d{3})?-?(\d{3})-(\d{4})/) { match, areaCode, exchange, stationNumber ->
     *          assert "233-9999" == match
     *          assert null == areaCode
     *          assert "233" == exchange
     *          assert "9999" == stationNumber
     *          return "$exchange$stationNumber"
     *      }
     * </pre>
     *
     * @param self    a String
     * @param regex   the capturing regex string
     * @param closure the closure that will be passed the full match, plus each of the capturing groups
     * @return a String containing the result of the closure, or null if the regex pattern doesn't match
     * @since 1.6.1
     */
    public static String find(String self, String regex, Closure closure) {
        return find(self, Pattern.compile(regex), closure);
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in CharSequence format) found within a CharSequence.
     *
     * @param self  a CharSequence
     * @param regex the capturing regex CharSequence
     * @return a List containing all full matches of the regex within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, String)
     * @since 1.8.2
     */
    public static List<CharSequence> findAll(CharSequence self, CharSequence regex) {
        return new ArrayList<CharSequence>(findAll(self.toString(), regex.toString()));
    }

    /**
     * Finds all occurrences of a capturing regular expression CharSequence within a CharSequence.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex CharSequence
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> List<T> findAll(CharSequence self, CharSequence regex, Closure<T> closure) {
        return findAll(self.toString(), regex.toString(), closure);
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in Pattern format) found within a CharSequence.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @return a List containing all full matches of the Pattern within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, java.util.regex.Pattern)
     * @since 1.8.2
     */
    public static List<CharSequence> findAll(CharSequence self, Pattern pattern) {
        return new ArrayList<CharSequence>(findAll(self.toString(), pattern));
    }

    /**
     * Finds all occurrences of a compiled regular expression Pattern within a CharSequence.
     *
     * @param self    a CharSequence
     * @param pattern the compiled regex Pattern
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex Pattern within the CharSequence, an empty list will be returned if there are no matches
     * @see #findAll(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> List<T> findAll(CharSequence self, Pattern pattern, Closure<T> closure) {
        return findAll(self.toString(), pattern, closure);
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in Pattern format) found within a String.
     * <p>
     * For example, if the pattern doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(~/(\w*) Fish/)
     * </pre>
     * Any regular expression matches are returned in a list, and all regex capture groupings are ignored, only the full match is returned:
     * <pre>
     * def expected = ["One Fish", "Two Fish", "Red Fish", "Blue Fish"]
     * assert expected == "One Fish, Two Fish, Red Fish, Blue Fish".findAll(~/(\w*) Fish/)
     * </pre>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @return a List containing all full matches of the Pattern within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static List<String> findAll(String self, Pattern pattern) {
        Matcher matcher = pattern.matcher(self);
        List<String> list = new ArrayList<String>();
        for (Iterator iter = iterator(matcher); iter.hasNext();) {
            if (hasGroup(matcher)) {
                list.add((String) ((List) iter.next()).get(0));
            } else {
                list.add((String) iter.next());
            }
        }
        return list;
    }

    /**
     * Finds all occurrences of a compiled regular expression Pattern within a String.   Any matches are passed to the specified closure.  The closure
     * is expected to have the full match in the first parameter.  If there are any capture groups, they will be placed in subsequent parameters.
     * <p>
     * If there are no matches, the closure will not be called, and an empty List will be returned.
     * <p>
     * For example, if the pattern doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(~/(\w*) Fish/) { match, firstWord -> return firstWord }
     * </pre>
     * Any regular expression matches are passed to the closure, if there are no capture groups, there will be one parameter for the match:
     * <pre>
     * assert ["couldn't", "wouldn't"] == "I could not, would not, with a fox.".findAll(~/.ould/) { match -> "${match}n't"}
     * </pre>
     * If there are capture groups, the first parameter will be the match followed by one parameter for each capture group:
     * <pre>
     * def orig = "There's a Wocket in my Pocket"
     * assert ["W > Wocket", "P > Pocket"] == orig.findAll(~/(.)ocket/) { match, firstLetter -> "$firstLetter > $match" }
     * </pre>
     *
     * @param self    a String
     * @param pattern the compiled regex Pattern
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex Pattern within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static <T> List<T> findAll(String self, Pattern pattern, Closure<T> closure) {
        Matcher matcher = pattern.matcher(self);
        return DefaultGroovyMethods.collect(matcher, closure);
    }

    /**
     * Returns a (possibly empty) list of all occurrences of a regular expression (in String format) found within a String.
     * <p>
     * For example, if the regex doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(/(\w*) Fish/)
     * </pre>
     * Any regular expression matches are returned in a list, and all regex capture groupings are ignored, only the full match is returned:
     * <pre>
     * def expected = ["One Fish", "Two Fish", "Red Fish", "Blue Fish"]
     * assert expected == "One Fish, Two Fish, Red Fish, Blue Fish".findAll(/(\w*) Fish/)
     * </pre>
     * If you need to work with capture groups, then use the closure version
     * of this method or use Groovy's matcher operators or use <tt>eachMatch</tt>.
     *
     * @param self  a String
     * @param regex the capturing regex String
     * @return a List containing all full matches of the regex within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static List<String> findAll(String self, String regex) {
        return findAll(self, Pattern.compile(regex));
    }

    /**
     * Finds all occurrences of a regular expression string within a String.   Any matches are passed to the specified closure.  The closure
     * is expected to have the full match in the first parameter.  If there are any capture groups, they will be placed in subsequent parameters.
     * <p>
     * If there are no matches, the closure will not be called, and an empty List will be returned.
     * <p>
     * For example, if the regex doesn't match, it returns an empty list:
     * <pre>
     * assert [] == "foo".findAll(/(\w*) Fish/) { match, firstWord -> return firstWord }
     * </pre>
     * Any regular expression matches are passed to the closure, if there are no capture groups, there will be one parameter for the match:
     * <pre>
     * assert ["couldn't", "wouldn't"] == "I could not, would not, with a fox.".findAll(/.ould/) { match -> "${match}n't"}
     * </pre>
     * If there are capture groups, the first parameter will be the match followed by one parameter for each capture group:
     * <pre>
     * def orig = "There's a Wocket in my Pocket"
     * assert ["W > Wocket", "P > Pocket"] == orig.findAll(/(.)ocket/) { match, firstLetter -> "$firstLetter > $match" }
     * </pre>
     *
     * @param self    a String
     * @param regex   the capturing regex String
     * @param closure will be passed the full match plus each of the capturing groups
     * @return a List containing all full matches of the regex within the string, an empty list will be returned if there are no matches
     * @since 1.6.1
     */
    public static <T> List<T> findAll(String self, String regex, Closure<T> closure) {
        return findAll(self, Pattern.compile(regex), closure);
    }

    // TODO expose this for stream based scenarios?
    private static int findMinimumLeadingSpaces(String line, int count) {
        int length = line.length();
        int index = 0;
        while (index < length && index < count && Character.isWhitespace(line.charAt(index))) index++;
        return index;
    }

    /**
     * Select a List of characters from a CharSequence using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a CharSequence
     * @param indices a Collection of indices
     * @return a CharSequence consisting of the characters at the given indices
     * @since 1.0
     */
    public static CharSequence getAt(CharSequence self, Collection indices) {
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
     * Support the range subscript operator for CharSequence or StringBuffer with EmptyRange
     *
     * @param text  a CharSequence
     * @param range an EmptyRange
     * @return the subsequence CharSequence
     * @since 1.5.0
     */
    public static CharSequence getAt(CharSequence text, EmptyRange range) {
        return "";
    }

    /**
     * Support the subscript operator for CharSequence.
     *
     * @param text  a CharSequence
     * @param index the index of the Character to get
     * @return the Character at the given index
     * @since 1.0
     */
    public static CharSequence getAt(CharSequence text, int index) {
        index = normaliseIndex(index, text.length());
        return text.subSequence(index, index + 1);
    }

    /**
     * Support the range subscript operator for CharSequence or StringBuffer with IntRange
     *
     * @param text  a CharSequence
     * @param range an IntRange
     * @return the subsequence CharSequence
     * @since 1.0
     */
    public static CharSequence getAt(CharSequence text, IntRange range) {
        return getAt(text, (Range) range);
    }

    /**
     * Support the range subscript operator for CharSequence
     *
     * @param text  a CharSequence
     * @param range a Range
     * @return the subsequence CharSequence
     * @since 1.0
     */
    public static CharSequence getAt(CharSequence text, Range range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), text.length());
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), text.length());

        boolean reverse = range.isReverse();
        // If this is a backwards range, reverse the arguments to substring.
        if (from > to) {
            int tmp = from;
            from = to;
            to = tmp;
            reverse = !reverse;
        }

        CharSequence sequence = text.subSequence(from, to + 1);
        return reverse ? reverse((String) sequence) : sequence;
    }

    /**
     * Select a List of values from a Matcher using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a Matcher
     * @param indices a Collection of indices
     * @return a String of the values at the given indices
     * @since 1.6.0
     */
    public static List getAt(Matcher self, Collection indices) {
        List result = new ArrayList();
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
     * Support the subscript operator, e.g.&#160;matcher[index], for a regex Matcher.
     * <p>
     * For an example using no group match,
     * <pre>
     *    def p = /ab[d|f]/
     *    def m = "abcabdabeabf" =~ p
     *    assert 2 == m.count
     *    assert 2 == m.size() // synonym for m.getCount()
     *    assert ! m.hasGroup()
     *    assert 0 == m.groupCount()
     *    def matches = ["abd", "abf"]
     *    for (i in 0..&lt;m.count) {
     *    &#160;&#160;assert m[i] == matches[i]
     *    }
     * </pre>
     * <p>
     * For an example using group matches,
     * <pre>
     *    def p = /(?:ab([c|d|e|f]))/
     *    def m = "abcabdabeabf" =~ p
     *    assert 4 == m.count
     *    assert m.hasGroup()
     *    assert 1 == m.groupCount()
     *    def matches = [["abc", "c"], ["abd", "d"], ["abe", "e"], ["abf", "f"]]
     *    for (i in 0..&lt;m.count) {
     *    &#160;&#160;assert m[i] == matches[i]
     *    }
     * </pre>
     * <p>
     * For another example using group matches,
     * <pre>
     *    def m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/
     *    assert 3 == m.count
     *    assert m.hasGroup()
     *    assert 1 == m.groupCount()
     *    def matches = [["abd", "d"], ["abxyz", "xyz"], ["abx", "x"]]
     *    for (i in 0..&lt;m.count) {
     *    &#160;&#160;assert m[i] == matches[i]
     *    }
     * </pre>
     *
     * @param matcher a Matcher
     * @param idx     an index
     * @return object a matched String if no groups matched, list of matched groups otherwise.
     * @since 1.0
     */
    public static Object getAt(Matcher matcher, int idx) {
        try {
            int count = getCount(matcher);
            if (idx < -count || idx >= count) {
                throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + idx + ")");
            }
            idx = normaliseIndex(idx, count);

            Iterator iter = iterator(matcher);
            Object result = null;
            for (int i = 0; i <= idx; i++) {
                result = iter.next();
            }
            return result;
        }
        catch (IllegalStateException ex) {
            return null;
        }
    }

    /**
     * Given a matcher that matches a string against a pattern,
     * this method returns true when the string matches the pattern or if a longer string, could match the pattern.
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
     * @param matcher the Matcher
     * @return true if more input to the String could make the matcher match the associated pattern, false otherwise.
     *
     * @since 2.0.0
     */
    public static boolean matchesPartially(Matcher matcher) {
        return matcher.matches() || matcher.hitEnd();
    }

    /**
     * Select a List of characters from a String using a Collection
     * to identify the indices to be selected.
     *
     * @param self    a String
     * @param indices a Collection of indices
     * @return a String consisting of the characters at the given indices
     * @since 1.0
     */
    public static String getAt(String self, Collection indices) {
        return (String) getAt((CharSequence) self, indices);
    }

    /**
     * Support the range subscript operator for String with EmptyRange
     *
     * @param text  a String
     * @param range an EmptyRange
     * @return the resulting String
     * @since 1.5.0
     */
    public static String getAt(String text, EmptyRange range) {
        return "";
    }

    /**
     * Support the subscript operator for String.
     *
     * @param text  a String
     * @param index the index of the Character to get
     * @return the Character at the given index
     * @since 1.0
     */
    public static String getAt(String text, int index) {
        index = normaliseIndex(index, text.length());
        return text.substring(index, index + 1);
    }

    /**
     * Support the range subscript operator for String with IntRange
     *
     * @param text  a String
     * @param range an IntRange
     * @return the resulting String
     * @since 1.0
     */
    public static String getAt(String text, IntRange range) {
        return getAt(text, (Range) range);
    }

    /**
     * Support the range subscript operator for String
     *
     * @param text  a String
     * @param range a Range
     * @return a substring corresponding to the Range
     * @since 1.0
     */
    public static String getAt(String text, Range range) {
        int from = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getFrom()), text.length());
        int to = normaliseIndex(DefaultTypeTransformation.intUnbox(range.getTo()), text.length());

        // If this is a backwards range, reverse the arguments to substring.
        boolean reverse = range.isReverse();
        if (from > to) {
            int tmp = to;
            to = from;
            from = tmp;
            reverse = !reverse;
        }

        String answer = text.substring(from, to + 1);
        if (reverse) {
            answer = reverse(answer);
        }
        return answer;
    }

    /**
     * Converts the given CharSequence into an array of characters.
     *
     * @param self a CharSequence
     * @return an array of characters
     * @see #getChars(String)
     * @since 1.8.2
     */
    public static char[] getChars(CharSequence self) {
        return getChars(self.toString());
    }

    /**
     * Converts the given String into an array of characters.
     * Alias for toCharArray.
     *
     * @param self a String
     * @return an array of characters
     * @see String#toCharArray()
     * @since 1.6.0
     */
    public static char[] getChars(String self) {
        return self.toCharArray();
    }

    /**
     * Find the number of Strings matched to the given Matcher.
     *
     * @param matcher a Matcher
     * @return int  the number of Strings matched to the given matcher.
     * @since 1.0
     */
    public static int getCount(Matcher matcher) {
        int counter = 0;
        matcher.reset();
        while (matcher.find()) {
            counter++;
        }
        return counter;
    }

    private static String getPadding(String padding, int length) {
        if (padding.length() < length) {
            return multiply(padding, length / padding.length() + 1).substring(0, length);
        } else {
            return padding.substring(0, length);
        }
    }

    /**
     * Get a replacement corresponding to the matched pattern for {@link org.codehaus.groovy.runtime.StringGroovyMethods#replaceAll(String, java.util.regex.Pattern, groovy.lang.Closure)}.
     * The closure take parameter:
     * <ul>
     * <li>Whole of match if the pattern include no capturing group</li>
     * <li>Object[] of capturing groups if the closure takes Object[] as parameter</li>
     * <li>List of capturing groups</li>
     * </ul>
     *
     * @param    matcher the matcher object used for matching
     * @param    closure specified with replaceAll() to get replacement
     * @return   replacement correspond replacement for a match
     */
    private static String getReplacement(Matcher matcher, Closure closure) {
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
     * Check whether a Matcher contains a group or not.
     *
     * @param matcher a Matcher
     * @return boolean  <code>true</code> if matcher contains at least one group.
     * @since 1.0
     */
    public static boolean hasGroup(Matcher matcher) {
        return matcher.groupCount() > 0;
    }

    /**
     * True if a CharSequence only contains whitespace characters.
     *
     * @param self The CharSequence to check the characters in
     * @return true If all characters are whitespace characters
     * @see #isAllWhitespace(String)
     * @since 1.8.2
     */
    public static boolean isAllWhitespace(CharSequence self) {
        return isAllWhitespace(self.toString());
    }

    /**
     * True if a String only contains whitespace characters.
     *
     * @param self The String to check the characters in
     * @return true If all characters are whitespace characters
     * @see Character#isWhitespace(char)
     * @since 1.6
     */
    public static boolean isAllWhitespace(String self) {
        for (int i = 0; i < self.length(); i++) {
            if (!Character.isWhitespace(self.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Determine if a CharSequence can be parsed as a BigDecimal.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isBigDecimal(String)
     * @since 1.8.2
     */
    public static boolean isBigDecimal(CharSequence self) {
        return isBigDecimal(self.toString());
    }

    /**
     * Determine if a String can be parsed into a BigDecimal.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
     */
    public static boolean isBigDecimal(String self) {
        try {
            new BigDecimal(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a CharSequence can be parsed as a BigInteger.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isBigInteger(String)
     * @since 1.8.2
     */
    public static boolean isBigInteger(CharSequence self) {
        return isBigInteger(self.toString());
    }

    /**
     * Determine if a String can be parsed into a BigInteger.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
     */
    public static boolean isBigInteger(String self) {
        try {
            new BigInteger(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * 'Case' implementation for a CharSequence, which simply calls the equivalent method for String.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     * @since 1.8.2
     */
    public static boolean isCase(CharSequence caseValue, Object switchValue) {
        return isCase(caseValue.toString(), switchValue);
    }

    /**
     * 'Case' implementation for a GString, which simply calls the equivalent method for String.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     * @since 1.6.0
     */
    public static boolean isCase(GString caseValue, Object switchValue) {
        return isCase(caseValue.toString(), switchValue);
    }

    /**
     * 'Case' implementation for the {@link java.util.regex.Pattern} class, which allows
     * testing a String against a number of regular expressions.
     * For example:
     * <pre>switch( str ) {
     *   case ~/one/ :
     *     // the regex 'one' matches the value of str
     * }
     * </pre>
     * Note that this returns true for the case where both the pattern and
     * the 'switch' values are <code>null</code>.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue is deemed to match the caseValue
     * @since 1.0
     */
    public static boolean isCase(Pattern caseValue, Object switchValue) {
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
     * 'Case' implementation for a String, which uses String#equals(Object)
     * in order to allow Strings to be used in switch statements.
     * For example:
     * <pre>switch( str ) {
     *   case 'one' :
     *   // etc...
     * }</pre>
     * Note that this returns <code>true</code> for the case where both the
     * 'switch' and 'case' operand is <code>null</code>.
     *
     * @param caseValue   the case value
     * @param switchValue the switch value
     * @return true if the switchValue's toString() equals the caseValue
     * @since 1.0
     */
    public static boolean isCase(String caseValue, Object switchValue) {
        if (switchValue == null) {
            return caseValue == null;
        }
        return caseValue.equals(switchValue.toString());
    }

    /**
     * Determine if a CharSequence can be parsed as a Double.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isDouble(String)
     * @since 1.8.2
     */
    public static boolean isDouble(CharSequence self) {
        return isDouble(self.toString());
    }

    /**
     * Determine if a String can be parsed into a Double.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
     */
    public static boolean isDouble(String self) {
        try {
            Double.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a CharSequence can be parsed as a Float.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isFloat(String)
     * @since 1.8.2
     */
    public static boolean isFloat(CharSequence self) {
        return isFloat(self.toString());
    }

    /**
     * Determine if a String can be parsed into a Float.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
     */
    public static boolean isFloat(String self) {
        try {
            Float.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a CharSequence can be parsed as an Integer.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isInteger(String)
     * @since 1.8.2
     */
    public static boolean isInteger(CharSequence self) {
        return isInteger(self.toString());
    }

    /**
     * Determine if a String can be parsed into an Integer.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
     */
    public static boolean isInteger(String self) {
        try {
            Integer.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a CharSequence can be parsed as a Long.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isLong(String)
     * @since 1.8.2
     */
    public static boolean isLong(CharSequence self) {
        return isLong(self.toString());
    }

    /**
     * Determine if a String can be parsed into a Long.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @since 1.5.0
     */
    public static boolean isLong(String self) {
        try {
            Long.valueOf(self.trim());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Determine if a CharSequence can be parsed as a Number.
     * Synonym for 'isBigDecimal()'.
     *
     * @param self a CharSequence
     * @return true if the CharSequence can be parsed
     * @see #isNumber(String)
     * @since 1.8.2
     */
    public static boolean isNumber(CharSequence self) {
        return isNumber(self.toString());
    }

    /**
     * Determine if a String can be parsed into a Number.
     * Synonym for 'isBigDecimal()'.
     *
     * @param self a String
     * @return true if the string can be parsed
     * @see #isBigDecimal(String)
     * @since 1.5.0
     */
    public static boolean isNumber(String self) {
        return isBigDecimal(self);
    }

    /**
     * Returns an {@link java.util.Iterator} which traverses each match.
     *
     * @param matcher a Matcher object
     * @return an Iterator for a Matcher
     * @see java.util.regex.Matcher#group()
     * @since 1.0
     */
    public static Iterator iterator(final Matcher matcher) {
        matcher.reset();
        return new Iterator() {
            private boolean found /* = false */;
            private boolean done /* = false */;

            public boolean hasNext() {
                if (done) {
                    return false;
                }
                if (!found) {
                    found = matcher.find();
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

                if (hasGroup(matcher)) {
                    // are we using groups?
                    // yes, so return the specified group as list
                    List<String> list = new ArrayList<String>(matcher.groupCount());
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                       list.add(matcher.group(i));
                    }
                    return list;
                } else {
                    // not using groups, so return the nth
                    // occurrence of the pattern
                    return matcher.group();
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
     * @since 1.8.2
     */
    public static StringBuilder leftShift(CharSequence self, Object value) {
        return new StringBuilder(self).append(value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a String.
     *
     * @param self  a String
     * @param value an Object
     * @return a StringBuffer built from this string
     * @since 1.0
     */
    public static StringBuffer leftShift(String self, Object value) {
        return new StringBuffer(self).append(value);
    }

    /**
     * Overloads the left shift operator to provide an easy way to append multiple
     * objects as string representations to a StringBuffer.
     *
     * @param self  a StringBuffer
     * @param value a value to append
     * @return the StringBuffer on which this operation was invoked
     * @since 1.0
     */
    public static StringBuffer leftShift(StringBuffer self, Object value) {
        self.append(value);
        return self;
    }

    /**
     * Overloads the left shift operator to provide syntactic sugar for appending to a StringBuilder.
     *
     * @param self  a StringBuilder
     * @param value an Object
     * @return the original StringBuilder
     * @since 1.8.2
     */
    public static StringBuilder leftShift(StringBuilder self, Object value) {
        self.append(value);
        return self;
    }

    /**
     * Tells whether or not a CharSequence matches the given
     * compiled regular expression Pattern.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @return  true if the CharSequence matches
     * @see String#matches(String)
     * @since 1.8.2
     */
    public static boolean matches(CharSequence self, Pattern pattern) {
        return pattern.matcher(self).matches();
    }

    /**
     * Tells whether or not self matches the given
     * compiled regular expression Pattern.
     *
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @return  true if the string matches
     * @see String#matches(String)
     * @since 1.6.1
     */
    public static boolean matches(String self, Pattern pattern) {
        return pattern.matcher(self).matches();
    }

    /**
     * Remove a part of a CharSequence by replacing the first occurrence
     * of target within self with '' and returns the result.
     *
     * @param self   a CharSequence
     * @param target an object representing the part to remove
     * @return a CharSequence minus the part to be removed
     * @see #minus(String, Object)
     * @since 1.8.2
     */
    public static CharSequence minus(CharSequence self, Object target) {
        return minus(self.toString(), target);
    }

    /**
     * Remove a part of a String. This replaces the first occurrence
     * of target within self with '' and returns the result. If
     * target is a regex Pattern, the first occurrence of that
     * pattern will be removed (using regex matching), otherwise
     * the first occurrence of target.toString() will be removed.
     *
     * @param self   a String
     * @param target an object representing the part to remove
     * @return a String minus the part to be removed
     * @since 1.0
     */
    public static String minus(String self, Object target) {
        if (target instanceof Pattern) {
            return ((Pattern)target).matcher(self).replaceFirst("");
        }
        String text = DefaultGroovyMethods.toString(target);
        int index = self.indexOf(text);
        if (index == -1) return self;
        int end = index + text.length();
        if (self.length() > end) {
            return self.substring(0, index) + self.substring(end);
        }
        return self.substring(0, index);
    }

    /**
     * Repeat a CharSequence a certain number of times.
     *
     * @param self   a CharSequence to be repeated
     * @param factor the number of times the CharSequence should be repeated
     * @return a CharSequence composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
     * @since 1.8.2
     */
    public static CharSequence multiply(CharSequence self, Number factor) {
        return multiply(self.toString(), factor);
    }

    /**
     * Repeat a String a certain number of times.
     *
     * @param self   a String to be repeated
     * @param factor the number of times the String should be repeated
     * @return a String composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
     * @since 1.0
     */
    public static String multiply(String self, Number factor) {
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
     * This method is called by the ++ operator for the class CharSequence.
     *
     * @param self a CharSequence
     * @return an incremented CharSequence
     * @see #next(String)
     * @since 1.8.2
     */
    public static CharSequence next(CharSequence self) {
        return next(self.toString());
    }

    /**
     * This method is called by the ++ operator for the class String.
     * It increments the last character in the given string. If the
     * character in the string is Character.MAX_VALUE a Character.MIN_VALUE
     * will be appended. The empty string is incremented to a string
     * consisting of the character Character.MIN_VALUE.
     *
     * @param self a String
     * @return an incremented String
     * @since 1.0
     */
    public static String next(String self) {
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
     * Return a CharSequence with linefeeds and carriage returns normalized to linefeeds.
     *
     * @param self a CharSequence object
     * @return the normalized CharSequence
     * @see #normalize(String)
     * @since 1.8.2
     */
    public static CharSequence normalize(final CharSequence self) {
        return normalize(self.toString());
    }

    /**
     * Return a String with linefeeds and carriage returns normalized to linefeeds.
     *
     * @param self a String object
     * @return the normalized string
     * @since 1.6.0
     */
    public static String normalize(final String self) {
        int nx = self.indexOf('\r');

        if (nx < 0) {
            return self;
        }

        final int len = self.length();
        final StringBuilder sb = new StringBuilder(len);

        int i = 0;

        do {
            sb.append(self, i, nx);
            sb.append('\n');

            if ((i = nx + 1) >= len) break;

            if (self.charAt(i) == '\n') {
                // skip the LF in CR LF
                if (++i >= len) break;
            }

            nx = self.indexOf('\r', i);
        } while (nx > 0);

        sb.append(self, i, len);

        return sb.toString();
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the left as many times as needed.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @return the CharSequence padded to the left
     * @see #padLeft(CharSequence, Number, CharSequence)
     * @since 1.8.2
     */
    public static CharSequence padLeft(CharSequence self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding CharSequence as many times as needed to the left.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence padded to the left
     * @see #padLeft(String, Number, String)
     * @since 1.8.2
     */
    public static CharSequence padLeft(CharSequence self, Number numberOfChars, CharSequence padding) {
        return padLeft(self.toString(), numberOfChars, padding.toString());
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the left as many times as needed.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
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
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the String padded to the left
     * @see #padLeft(String, Number, String)
     * @since 1.0
     */
    public static String padLeft(String self, Number numberOfChars) {
        return padLeft(self, numberOfChars, " ");
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding String as many times as needed to the left.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
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
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @param padding       the characters used for padding
     * @return the String padded to the left
     * @since 1.0
     */
    public static String padLeft(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        } else {
            return getPadding(padding, numChars - self.length()) + self;
        }
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the right as many times as needed.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the CharSequence padded to the right
     * @see #padRight(String, Number)
     * @since 1.8.2
     */
    public static CharSequence padRight(CharSequence self, Number numberOfChars) {
        return padRight(self.toString(), numberOfChars);
    }

    /**
     * Pad a CharSequence to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding CharSequence as many times as needed to the right.
     *
     * @param self          a CharSequence object
     * @param numberOfChars the total minimum number of characters of the resulting CharSequence
     * @param padding       the characters used for padding
     * @return the CharSequence padded to the right
     * @see #padRight(String, Number, String)
     * @since 1.8.2
     */
    public static CharSequence padRight(CharSequence self, Number numberOfChars, CharSequence padding) {
        return padRight(self.toString(), numberOfChars, padding.toString());
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt> by adding the space character to the right as many times as needed.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
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
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @return the String padded to the right
     * @since 1.0
     */
    public static String padRight(String self, Number numberOfChars) {
        return padRight(self, numberOfChars, " ");
    }

    /**
     * Pad a String to a minimum length specified by <tt>numberOfChars</tt>, adding the supplied padding String as many times as needed to the right.
     *
     * If the String is already the same size or bigger than the target <tt>numberOfChars</tt>, then the original String is returned. An example:
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
     * @param self          a String object
     * @param numberOfChars the total minimum number of characters of the resulting string
     * @param padding       the characters used for padding
     * @return the String padded to the right
     * @since 1.0
     */
    public static String padRight(String self, Number numberOfChars, String padding) {
        int numChars = numberOfChars.intValue();
        if (numChars <= self.length()) {
            return self;
        } else {
            return self + getPadding(padding, numChars - self.length());
        }
    }

    /**
     * Appends the String representation of the given operand to this string.
     *
     * @param left  a CharSequence
     * @param value any Object
     * @return the new CharSequence with the object appended
     * @since 1.8.2
     */
    public static CharSequence plus(CharSequence left, Object value) {
        return left + DefaultGroovyMethods.toString(value);
    }

    /**
     * Appends a String to the string representation of this number.
     *
     * @param value a Number
     * @param right a String
     * @return a String
     * @since 1.0
     */
    public static String plus(Number value, String right) {
        return DefaultGroovyMethods.toString(value) + right;
    }

    /**
     * Appends the String representation of the given operand to this string.
     *
     * @param left  a String
     * @param value any Object
     * @return the new string with the object appended
     * @since 1.0
     */
    public static String plus(String left, Object value) {
        return left + DefaultGroovyMethods.toString(value);
    }

    /**
     * Appends a String to this StringBuffer.
     *
     * @param left  a StringBuffer
     * @param value a String
     * @return a String
     * @since 1.0
     */
    public static String plus(StringBuffer left, String value) {
        return left + value;
    }

    /**
     * This method is called by the -- operator for the class CharSequence.
     *
     * @param self a CharSequence
     * @return a CharSequence with a decremented digit at the end
     * @see #previous(String)
     * @since 1.8.2
     */
    public static CharSequence previous(CharSequence self) {
        return previous(self.toString());
    }

    /**
     * This method is called by the -- operator for the class String.
     * It decrements the last character in the given string. If the
     * character in the string is Character.MIN_VALUE it will be deleted.
     * The empty string can't be decremented.
     *
     * @param self a String
     * @return a String with a decremented digit at the end
     * @since 1.0
     */
    public static String previous(String self) {
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
     * Support the range subscript operator for StringBuffer.
     *
     * @param self  a StringBuffer
     * @param range a Range
     * @param value the object that's toString() will be inserted
     * @since 1.0
     */
    public static void putAt(StringBuffer self, EmptyRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Support the range subscript operator for StringBuffer.  Index values are
     * treated as characters within the buffer.
     *
     * @param self  a StringBuffer
     * @param range a Range
     * @param value the object that's toString() will be inserted
     * @since 1.0
     */
    public static void putAt(StringBuffer self, IntRange range, Object value) {
        RangeInfo info = subListBorders(self.length(), range);
        self.replace(info.from, info.to, value.toString());
    }

    /**
     * Return the lines of a CharSequence as a List of CharSequence.
     *
     * @param self a CharSequence object
     * @return a list of lines
     * @throws java.io.IOException if an error occurs
     * @since 1.8.2
     */
    public static List<CharSequence> readLines(CharSequence self) throws IOException {
        return new ArrayList<CharSequence>(readLines(self.toString()));
    }

    /**
     * Return the lines of a String as a List of Strings.
     *
     * @param self a String object
     * @return a list of lines
     * @throws java.io.IOException if an error occurs
     * @since 1.5.5
     */
    public static List<String> readLines(String self) throws IOException {
        return IOGroovyMethods.readLines(new StringReader(self));
    }

    /**
     * Replaces each substring of this CharSequence that matches the given
     * regular expression with the given replacement.
     *
     * @param self        a CharSequence
     * @param regex       the capturing regex
     * @param replacement the capturing regex
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#replaceAll(String, String)
     * @since 1.8.2
     */
    public static CharSequence replaceAll(final CharSequence self, final CharSequence regex, final CharSequence replacement) {
        return self.toString().replaceAll(regex.toString(), replacement.toString());
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure on that text.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex
     * @param closure the closure to apply on each captured group
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @since 1.8.2
     * @see #replaceAll(String, java.util.regex.Pattern, groovy.lang.Closure)
     */
    public static CharSequence replaceAll(final CharSequence self, final CharSequence regex, final Closure closure) {
        return replaceAll(self.toString(), Pattern.compile(regex.toString()), closure);
    }

    /**
     * Replaces all substrings of a CharSequence that match the given
     * compiled regular expression with the given replacement.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the CharSequence of interest is to be matched
     * @param   replacement the CharSequence to be substituted for the first match
     * @return  The resulting <tt>CharSequence</tt>
     * @see #replaceAll(String, java.util.regex.Pattern, String)
     * @since 1.8.2
     */
    public static CharSequence replaceAll(CharSequence self, Pattern pattern, CharSequence replacement) {
        return pattern.matcher(self).replaceAll(replacement.toString());
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure call on that text.
     *
     * @param self    a CharSequence
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on each captured group
     * @return a CharSequence with replaced content
     * @since 1.8.2
     * @see #replaceAll(String, java.util.regex.Pattern, groovy.lang.Closure)
     */
    public static String replaceAll(final CharSequence self, final Pattern pattern, final Closure closure) {
        return replaceAll(self.toString(), pattern, closure);
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure call on that text.
     * <p>
     * For examples,
     * <pre>
     *     assert "hellO wOrld" == "hello world".replaceAll(~"(o)") { it[0].toUpperCase() }
     *
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll(~"(([fF][oO]{2})[bB]ar)", { it[0].toUpperCase() })
     *
     *     Here,
     *          it[0] is the global string of the matched group
     *          it[1] is the first string in the matched group
     *          it[2] is the second string in the matched group
     *
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll(~"(([fF][oO]{2})[bB]ar)", { Object[] it -> it[0].toUpperCase() })
     *
     *     Here,
     *          it[0] is the global string of the matched group
     *          it[1] is the first string in the matched group
     *          it[2] is the second string in the matched group
     *
     *     assert "FOO-FOO-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { x, y, z -> z.toUpperCase() })
     *
     *     Here,
     *          x is the global string of the matched group
     *          y is the first string in the matched group
     *          z is the second string in the matched group
     * </pre>
     * Note that unlike String.replaceAll(String regex, String replacement), where the replacement string
     * treats '$' and '\' specially (for group substitution), the result of the closure is converted to a string
     * and that value is used literally for the replacement.
     *
     * @param self    a String
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on each captured group
     * @return a String with replaced content
     * @since 1.6.8
     * @see java.util.regex.Matcher#quoteReplacement(String)
     */
    public static String replaceAll(final String self, final Pattern pattern, final Closure closure) {
        final Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(self.length() + 16);
            do {
                String replacement = getReplacement(matcher, closure);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } while (matcher.find());
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    /**
     * Replaces all substrings of a String that match the given
     * compiled regular expression with the given replacement.
     * <p>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the
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
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @param   replacement the string to be substituted for the first match
     * @return  The resulting <tt>String</tt>
     * @see String#replaceAll(String, String)
     * @since 1.6.1
     */
    public static String replaceAll(String self, Pattern pattern, String replacement) {
        return pattern.matcher(self).replaceAll(replacement);
    }

    /**
     * Replaces all occurrences of a captured group by the result of a closure on that text.
     * <p>
     * For examples,
     * <pre>
     *     assert "hellO wOrld" == "hello world".replaceAll("(o)") { it[0].toUpperCase() }
     *
     *     assert "FOOBAR-FOOBAR-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { Object[] it -> it[0].toUpperCase() })
     *
     *     Here,
     *          it[0] is the global string of the matched group
     *          it[1] is the first string in the matched group
     *          it[2] is the second string in the matched group
     *
     *     assert "FOO-FOO-" == "foobar-FooBar-".replaceAll("(([fF][oO]{2})[bB]ar)", { x, y, z -> z.toUpperCase() })
     *
     *     Here,
     *          x is the global string of the matched group
     *          y is the first string in the matched group
     *          z is the second string in the matched group
     * </pre>
     * Note that unlike String.replaceAll(String regex, String replacement), where the replacement string
     * treats '$' and '\' specially (for group substitution), the result of the closure is converted to a string
     * and that value is used literally for the replacement.
     *
     * @param self    a String
     * @param regex   the capturing regex
     * @param closure the closure to apply on each captured group
     * @return a String with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @since 1.0
     * @see java.util.regex.Matcher#quoteReplacement(String)
     * @see #replaceAll(String, java.util.regex.Pattern, groovy.lang.Closure)
     */
    public static String replaceAll(final String self, final String regex, final Closure closure) {
        return replaceAll(self, Pattern.compile(regex), closure);
    }

    /**
     * Replaces the first substring of this CharSequence that matches the given
     * regular expression with the given replacement.
     *
     * @param self        a CharSequence
     * @param regex       the capturing regex
     * @param replacement the capturing regex
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#replaceAll(String, String)
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final CharSequence regex, final CharSequence replacement) {
        return self.toString().replaceFirst(regex.toString(), replacement.toString());
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     *
     * @param self    a CharSequence
     * @param regex   the capturing regex
     * @param closure the closure to apply on the first captured group
     * @return a CharSequence with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see #replaceFirst(String, String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final CharSequence regex, final Closure closure) {
        return replaceFirst(self.toString(), regex.toString(), closure);
    }

    /**
     * Replaces the first substring of a CharSequence that matches the given
     * compiled regular expression with the given replacement.
     *
     * @param   self the CharSequence that is to be matched
     * @param   pattern the regex Pattern to which the CharSequence of interest is to be matched
     * @param   replacement the CharSequence to be substituted for the first match
     * @return  The resulting <tt>CharSequence</tt>
     * @see #replaceFirst(String, java.util.regex.Pattern, String)
     * @since 1.8.2
     */
    public static CharSequence replaceFirst(CharSequence self, Pattern pattern, CharSequence replacement) {
        return pattern.matcher(self).replaceFirst(replacement.toString());
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     *
     * @param self    a CharSequence
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on the first captured group
     * @return a CharSequence with replaced content
     * @see #replaceFirst(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static String replaceFirst(final CharSequence self, final Pattern pattern, final Closure closure) {
        return replaceFirst(self.toString(), pattern, closure);
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     * <p>
     * For example (with some replaceAll variants thrown in for comparison purposes),
     * <pre>
     * assert "hellO world" == "hello world".replaceFirst(~"(o)") { it[0].toUpperCase() } // first match
     * assert "hellO wOrld" == "hello world".replaceAll(~"(o)") { it[0].toUpperCase() }   // all matches
     *
     * assert '1-FISH, two fish' == "one fish, two fish".replaceFirst(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * assert '1-FISH, 2-FISH' == "one fish, two fish".replaceAll(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * </pre>
     *
     * @param self    a String
     * @param pattern the capturing regex Pattern
     * @param closure the closure to apply on the first captured group
     * @return a String with replaced content
     * @since 1.7.7
     * @see #replaceAll(String, java.util.regex.Pattern, groovy.lang.Closure)
     */
    public static String replaceFirst(final String self, final Pattern pattern, final Closure closure) {
        final Matcher matcher = pattern.matcher(self);
        if (matcher.find()) {
            final StringBuffer sb = new StringBuffer(self.length() + 16);
            String replacement = getReplacement(matcher, closure);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    /**
     * Replaces the first substring of a String that matches the given
     * compiled regular expression with the given replacement.
     * <p>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the
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
     * @param   self the string that is to be matched
     * @param   pattern the regex Pattern to which the string of interest is to be matched
     * @param   replacement the string to be substituted for the first match
     * @return  The resulting <tt>String</tt>
     * @see String#replaceFirst(String, String)
     * @since 1.6.1
     */
    public static String replaceFirst(String self, Pattern pattern, String replacement) {
        return pattern.matcher(self).replaceFirst(replacement);
    }

    /**
     * Replaces the first occurrence of a captured group by the result of a closure call on that text.
     * <p>
     * For example (with some replaceAll variants thrown in for comparison purposes),
     * <pre>
     * assert "hellO world" == "hello world".replaceFirst("(o)") { it[0].toUpperCase() } // first match
     * assert "hellO wOrld" == "hello world".replaceAll("(o)") { it[0].toUpperCase() }   // all matches
     *
     * assert '1-FISH, two fish' == "one fish, two fish".replaceFirst(/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * assert '1-FISH, 2-FISH' == "one fish, two fish".replaceAll(/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
     * </pre>
     *
     * @param self    a String
     * @param regex   the capturing regex
     * @param closure the closure to apply on the first captured group
     * @return a String with replaced content
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @since 1.7.7
     * @see java.util.regex.Matcher#quoteReplacement(String)
     * @see #replaceFirst(String, java.util.regex.Pattern, groovy.lang.Closure)
     */
    public static String replaceFirst(final String self, final String regex, final Closure closure) {
        return replaceFirst(self, Pattern.compile(regex), closure);
    }

    /**
     * Creates a new CharSequence which is the reverse (backwards) of this string
     *
     * @param self a CharSequence
     * @return a new CharSequence with all the characters reversed.
     * @see #reverse(String)
     * @since 1.8.2
     */
    public static CharSequence reverse(CharSequence self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * Creates a new string which is the reverse (backwards) of this string
     *
     * @param self a String
     * @return a new string with all the characters reversed.
     * @since 1.0
     * @see StringBuilder#reverse()
     */
    public static String reverse(String self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * Set the position of the given Matcher to the given index.
     *
     * @param matcher a Matcher
     * @param idx     the index number
     * @since 1.0
     */
    public static void setIndex(Matcher matcher, int idx) {
        int count = getCount(matcher);
        if (idx < -count || idx >= count) {
            throw new IndexOutOfBoundsException("index is out of range " + (-count) + ".." + (count - 1) + " (index = " + idx + ")");
        }
        if (idx == 0) {
            matcher.reset();
        } else if (idx > 0) {
            matcher.reset();
            for (int i = 0; i < idx; i++) {
                matcher.find();
            }
        } else if (idx < 0) {
            matcher.reset();
            idx += getCount(matcher);
            for (int i = 0; i < idx; i++) {
                matcher.find();
            }
        }
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>CharSequence</code>.
     *
     * @param text a CharSequence
     * @return the length of the CharSequence
     * @since 1.8.2
     */
    public static int size(CharSequence text) {
        return text.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>Matcher</code>.
     *
     * @param self a matcher object
     * @return the matcher's size (count)
     * @since 1.5.0
     */
    public static long size(Matcher self) {
        return getCount(self);
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>String</code>.
     *
     * @param text a String
     * @return the length of the String
     * @since 1.0
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>StringBuffer</code>.
     *
     * @param buffer a StringBuffer
     * @return the length of the StringBuffer
     * @since 1.0
     */
    public static int size(StringBuffer buffer) {
        return buffer.length();
    }

    /**
     * Convenience method to split a CharSequence (with whitespace as delimiter).
     * Similar to tokenize, but returns an Array of CharSequence instead of a List.
     *
     * @param self the CharSequence to split
     * @return CharSequence[] result of split
     * @see #split(String)
     * @since 1.8.2
     */
    public static CharSequence[] split(CharSequence self) {
        return split(self.toString());
    }

    /**
     * Convenience method to split a GString (with whitespace as delimiter).
     *
     * @param self the GString to split
     * @return String[] result of split
     * @see #split(String)
     * @since 1.6.1
     */
    public static String[] split(GString self) {
        return split(self.toString());
    }

    /**
     * Convenience method to split a string (with whitespace as delimiter)
     * Like tokenize, but returns an Array of Strings instead of a List
     *
     * @param self the string to split
     * @return String[] result of split
     * @since 1.5.0
     */
    public static String[] split(String self) {
        StringTokenizer st = new StringTokenizer(self);
        String[] strings = new String[st.countTokens()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = st.nextToken();
        }
        return strings;
    }

    /**
     * Iterates through the given CharSequence line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a CharSequence
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see #splitEachLine(String, String, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> T splitEachLine(CharSequence self, CharSequence regex, Closure<T> closure) throws IOException {
        return splitEachLine(self.toString(), regex.toString(), closure);
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
     * @throws java.io.IOException if an error occurs
     * @see #splitEachLine(String, java.util.regex.Pattern, groovy.lang.Closure)
     * @since 1.8.2
     */
    public static <T> T splitEachLine(CharSequence self, Pattern pattern, Closure<T> closure) throws IOException {
        return splitEachLine(self.toString(), pattern, closure);
    }

    /**
     * Iterates through the given String line by line, splitting each line using
     * the given separator Pattern.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a String
     * @param pattern the regular expression Pattern for the delimiter
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @see java.util.regex.Pattern#split(CharSequence)
     * @since 1.6.8
     */
    public static <T> T splitEachLine(String self, Pattern pattern, Closure<T> closure) throws IOException {
        final List<String> list = readLines(self);
        T result = null;
        for (String line : list) {
            List vals = Arrays.asList(pattern.split(line));
            result = closure.call(vals);
        }
        return result;
    }

    /**
     * Iterates through the given String line by line, splitting each line using
     * the given separator.  The list of tokens for each line is then passed to
     * the given closure.
     *
     * @param self    a String
     * @param regex   the delimiting regular expression
     * @param closure a closure
     * @return the last value returned by the closure
     * @throws java.io.IOException if an error occurs
     * @throws java.util.regex.PatternSyntaxException if the regular expression's syntax is invalid
     * @see String#split(String)
     * @since 1.5.5
     */
    public static <T> T splitEachLine(String self, String regex, Closure<T> closure) throws IOException {
        return splitEachLine(self, Pattern.compile(regex), closure);
    }

    /**
     * Strip leading spaces from every line in a CharSequence. The
     * line with the least number of leading spaces determines
     * the number to remove. Lines only containing whitespace are
     * ignored when calculating the number of leading spaces to strip.
     *
     * @param self     The CharSequence to strip the leading spaces from
     * @return the stripped CharSequence
     * @see #stripIndent(String)
     * @since 1.8.2
     */
    public static CharSequence stripIndent(CharSequence self) {
        return stripIndent(self.toString());
    }

    /**
     * Strip <tt>numChar</tt> leading characters from
     * every line in a CharSequence.
     *
     * @param self     The CharSequence to strip the characters from
     * @param numChars The number of characters to strip
     * @return the stripped CharSequence
     * @since 1.8.2
     */
    public static CharSequence stripIndent(CharSequence self, int numChars) {
        return stripIndent(self);
    }

    /**
     * Strip leading spaces from every line in a String. The
     * line with the least number of leading spaces determines
     * the number to remove. Lines only containing whitespace are
     * ignored when calculating the number of leading spaces to strip.
     * <pre class="groovyTestCase">
     * assert '  A\n B\nC' == '   A\n  B\n C'.stripIndent()
     * </pre>
     *
     * @param self     The String to strip the leading spaces from
     * @return the stripped String
     * @see #stripIndent(String, int)
     * @since 1.7.3
     */
    public static String stripIndent(String self) {
        if (self.length() == 0) return self;
        int runningCount = -1;
        try {
            for (String line : readLines(self)) {
                // don't take blank lines into account for calculating the indent
                if (isAllWhitespace(line)) continue;
                if (runningCount == -1) runningCount = line.length();
                runningCount = findMinimumLeadingSpaces(line, runningCount);
                if (runningCount == 0) break;
            }
        } catch (IOException e) {
            /* ignore */
        }
        return stripIndent(self, runningCount == -1 ? 0 : runningCount);
    }

    /**
     * Strip <tt>numChar</tt> leading characters from
     * every line in a String.
     * <pre class="groovyTestCase">
     * assert 'DEF\n456' == '''ABCDEF\n123456'''.stripIndent(3)
     * </pre>
     *
     * @param self     The String to strip the characters from
     * @param numChars The number of characters to strip
     * @return the stripped String
     * @since 1.7.3
     */
    public static String stripIndent(String self, int numChars) {
        if (self.length() == 0 || numChars <= 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                // normalize an empty or whitespace line to \n
                // or strip the indent for lines containing non-space characters
                if (!isAllWhitespace(line)) {
                    builder.append(stripIndentFromLine(line, numChars));
                }
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    // TODO expose this for stream based stripping?
    private static String stripIndentFromLine(String line, int numChars) {
        int length = line.length();
        return numChars <= length ? line.substring(numChars) : "";
    }

    /**
     * Strip leading whitespace/control characters followed by '|' from
     * every line in a CharSequence.
     *
     * @param self The CharSequence to strip the margin from
     * @return the stripped CharSequence
     * @see #stripMargin(CharSequence, char)
     * @since 1.8.2
     */
    public static CharSequence stripMargin(CharSequence self) {
        return stripMargin(self, '|');
    }

    /**
     * Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a String.
     *
     * @param self       The CharSequence to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped CharSequence
     * @see #stripMargin(String, char)
     * @since 1.8.2
     */
    public static CharSequence stripMargin(CharSequence self, char marginChar) {
        return stripMargin(self.toString(), marginChar);
    }

    /**
     * Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a CharSequence.
     *
     * @param self       The CharSequence to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped CharSequence
     * @see #stripMargin(String, String)
     * @since 1.8.2
     */
    public static String stripMargin(CharSequence self, CharSequence marginChar) {
        return stripMargin(self.toString(), marginChar.toString());
    }

    /**
     * Strip leading whitespace/control characters followed by '|' from
     * every line in a String.
     * <pre class="groovyTestCase">
     * assert 'ABC\n123\n456' == '''ABC
     *                             |123
     *                             |456'''.stripMargin()
     * </pre>
     *
     * @param self The String to strip the margin from
     * @return the stripped String
     * @see #stripMargin(String, char)
     * @since 1.7.3
     */
    public static String stripMargin(String self) {
        return stripMargin(self, '|');
    }

    /**
     * Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a String.
     * <pre class="groovyTestCase">
     * assert 'ABC\n123\n456' == '''ABC
     *                             *123
     *                             *456'''.stripMargin('*')
     * </pre>
     *
     * @param self       The String to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped String
     * @since 1.7.3
     */
    public static String stripMargin(String self, char marginChar) {
        if (self.length() == 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                builder.append(stripMarginFromLine(line, marginChar));
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * Strip leading whitespace/control characters followed by <tt>marginChar</tt> from
     * every line in a String.
     *
     * @param self       The String to strip the margin from
     * @param marginChar Any character that serves as margin delimiter
     * @return the stripped String
     * @see #stripMargin(String, char)
     * @since 1.7.3
     */
    public static String stripMargin(String self, String marginChar) {
        if (marginChar == null || marginChar.length() == 0) return stripMargin(self, '|');
        // TODO IllegalArgumentException for marginChar.length() > 1 ? Or support String as marker?
        return stripMargin(self, marginChar.charAt(0));
    }

    // TODO expose this for stream based stripping?
    private static String stripMarginFromLine(String line, char marginChar) {
        int length = line.length();
        int index = 0;
        while (index < length && line.charAt(index) <= ' ') index++;
        return (index < length && line.charAt(index) == marginChar) ? line.substring(index + 1) : line;
    }

    /**
     * Returns the first <code>num</code> elements from this CharSequence.
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.take( 0 ) == ''
     * assert text.take( 2 ) == 'Gr'
     * assert text.take( 7 ) == 'Groovy'
     * </pre>
     *
     * @param self the original CharSequence
     * @param num  the number of chars to take from this CharSequence
     * @return a CharSequence consisting of the first <code>num</code> chars,
     *         or else the whole CharSequence if it has less then <code>num</code> elements.
     * @since 1.8.1
     */
    public static CharSequence take(CharSequence self, int num) {
        if (num < 0) {
            return self.subSequence(0, 0);
        }
        if (self.length() <= num) {
            return self;
        }
        return self.subSequence(0, num);
    }

    /**
     * Returns the longest prefix of this CharSequence where each
     * element passed to the given closure evaluates to true.
     * <p>
     * <pre class="groovyTestCase">
     * def text = "Groovy"
     * assert text.takeWhile{ it < 'A' } == ''
     * assert text.takeWhile{ it < 'Z' } == 'G'
     * assert text.takeWhile{ it != 'v' } == 'Groo'
     * assert text.takeWhile{ it < 'z' } == 'Groovy'
     * </pre>
     *
     * @param self      the original CharSequence
     * @param condition the closure that must evaluate to true to continue taking elements
     * @return a prefix of elements in the CharSequence where each
     *         element passed to the given closure evaluates to true
     * @since 2.0.0
     */
    public static CharSequence takeWhile(CharSequence self, Closure condition) {
        int num = 0;
        BooleanClosureWrapper bcw = new BooleanClosureWrapper(condition);
        while (num < self.length()) {
            char value = self.charAt(num);
            if (bcw.call(value)) {
                num += 1;
            } else {
                break;
            }
        }
        return take(self, num);
    }

    /**
     * Parse a CharSequence into a BigDecimal
     *
     * @param self a CharSequence
     * @return a BigDecimal
     * @see #toBigDecimal(String)
     * @since 1.8.2
     */
    public static BigDecimal toBigDecimal(CharSequence self) {
        return toBigDecimal(self.toString());
    }

    /**
     * Parse a String into a BigDecimal
     *
     * @param self a String
     * @return a BigDecimal
     * @since 1.0
     */
    public static BigDecimal toBigDecimal(String self) {
        return new BigDecimal(self.trim());
    }

    /**
     * Parse a CharSequence into a BigInteger
     *
     * @param self a CharSequence
     * @return a BigInteger
     * @see #toBigInteger(String)
     * @since 1.8.2
     */
    public static BigInteger toBigInteger(CharSequence self) {
        return toBigInteger(self.toString());
    }

    /**
     * Parse a String into a BigInteger
     *
     * @param self a String
     * @return a BigInteger
     * @since 1.0
     */
    public static BigInteger toBigInteger(String self) {
        return new BigInteger(self.trim());
    }

    /**
     * Converts the given string into a Boolean object.
     * If the trimmed string is "true", "y" or "1" (ignoring case)
     * then the result is true otherwise it is false.
     *
     * @param self a String
     * @return The Boolean value
     * @since 1.0
     */
    public static Boolean toBoolean(String self) {
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
     * @since 1.0
     */
    public static Character toCharacter(String self) {
        return self.charAt(0);
    }

    /**
     * Parse a CharSequence into a Double
     *
     * @param self a CharSequence
     * @return a Double
     * @see #toDouble(String)
     * @since 1.8.2
     */
    public static Double toDouble(CharSequence self) {
        return toDouble(self.toString());
    }

    /**
     * Parse a String into a Double
     *
     * @param self a String
     * @return a Double
     * @since 1.0
     */
    public static Double toDouble(String self) {
        return Double.valueOf(self.trim());
    }

    /**
     * Parse a CharSequence into a Float
     *
     * @param self a CharSequence
     * @return a Float
     * @see #toFloat(String)
     * @since 1.8.2
     */
    public static Float toFloat(CharSequence self) {
        return toFloat(self.toString());
    }

    /**
     * Parse a String into a Float
     *
     * @param self a String
     * @return a Float
     * @since 1.0
     */
    public static Float toFloat(String self) {
        return Float.valueOf(self.trim());
    }

    /**
     * Parse a CharSequence into an Integer
     *
     * @param self a CharSequence
     * @return an Integer
     * @see #toInteger(String)
     * @since 1.8.2
     */
    public static Integer toInteger(CharSequence self) {
        return toInteger(self.toString());
    }

    /**
     * Parse a String into an Integer
     *
     * @param self a String
     * @return an Integer
     * @since 1.0
     */
    public static Integer toInteger(String self) {
        return Integer.valueOf(self.trim());
    }

    /**
     * Tokenize a CharSequence (with a whitespace as the delimiter).
     *
     * @param self a CharSequence
     * @return a List of tokens
     * @see #tokenize(String)
     * @since 1.8.2
     */
    public static List<CharSequence> tokenize(CharSequence self) {
        return new ArrayList<CharSequence>(tokenize(self.toString()));
    }

    /**
     * Tokenize a CharSequence based on the given character delimiter.
     *
     * @param self  a CharSequence
     * @param token the delimiter
     * @return a List of tokens
     * @see #tokenize(String, Character)
     * @since 1.8.2
     */
    public static List<CharSequence> tokenize(CharSequence self, Character token) {
        return tokenize(self, token.toString());
    }

    /**
     * Tokenize a CharSequence based on the given CharSequence delimiter.
     *
     * @param self  a CharSequence
     * @param token the delimiter
     * @return a List of tokens
     * @see #tokenize(String, String)
     * @since 1.8.2
     */
    public static List<CharSequence> tokenize(CharSequence self, CharSequence token) {
        return new ArrayList<CharSequence>(tokenize(self.toString(), token.toString()));
    }

    /**
     * Tokenize a String (with a whitespace as the delimiter).
     *
     * @param self a String
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(String)
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<String> tokenize(String self) {
        return InvokerHelper.asList(new StringTokenizer(self));
    }

    /**
     * Tokenize a String based on the given character delimiter.
     * For example:
     * <pre class="groovyTestCase">
     * char pathSep = ':'
     * assert "/tmp:/usr".tokenize(pathSep) == ["/tmp", "/usr"]
     * </pre>
     *
     * @param self  a String
     * @param token the delimiter
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(String, String)
     * @since 1.7.2
     */
    public static List<String> tokenize(String self, Character token) {
        return tokenize(self, token.toString());
    }

    /**
     * Tokenize a String based on the given string delimiter.
     *
     * @param self  a String
     * @param token the delimiter
     * @return a List of tokens
     * @see java.util.StringTokenizer#StringTokenizer(String, String)
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static List<String> tokenize(String self, String token) {
        return InvokerHelper.asList(new StringTokenizer(self, token));
    }

    /**
     * Converts the given CharSequence into a List of CharSequence of one character.
     *
     * @param self a CharSequence
     * @return a List of characters (a 1-character CharSequence)
     * @see #toSet(String)
     * @since 1.8.2
     */
    public static List<CharSequence> toList(CharSequence self) {
        return new ArrayList<CharSequence>(toList(self.toString()));
    }

    /**
     * Converts the given String into a List of strings of one character.
     *
     * @param self a String
     * @return a List of characters (a 1-character String)
     * @since 1.0
     */
    public static List<String> toList(String self) {
        int size = self.length();
        List<String> answer = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            answer.add(self.substring(i, i + 1));
        }
        return answer;
    }

    /**
     * Parse a CharSequence into a Long
     *
     * @param self a CharSequence
     * @return a Long
     * @see #toLong(String)
     * @since 1.8.2
     */
    public static Long toLong(CharSequence self) {
        return toLong(self.toString());
    }

    /**
     * Parse a String into a Long
     *
     * @param self a String
     * @return a Long
     * @since 1.0
     */
    public static Long toLong(String self) {
        return Long.valueOf(self.trim());
    }

    /**
     * Converts the given CharSequence into a Set of unique CharSequence of one character.
     *
     * @param self a CharSequence
     * @return a Set of unique character CharSequence (each a 1-character CharSequence)
     * @see #toSet(String)
     * @since 1.8.2
     */
    public static Set<CharSequence> toSet(CharSequence self) {
        return new HashSet<CharSequence>(toList(self));
    }

    /**
     * Converts the given String into a Set of unique strings of one character.
     * <p>
     * Example usage:
     * <pre class="groovyTestCase">
     * assert 'groovy'.toSet() == ['v', 'g', 'r', 'o', 'y'] as Set
     * assert "abc".toSet().iterator()[0] instanceof String
     * </pre>
     *
     * @param self a String
     * @return a Set of unique character Strings (each a 1-character String)
     * @since 1.8.0
     */
    public static Set<String> toSet(String self) {
        return new HashSet<String>(toList(self));
    }

    /**
     * Parse a CharSequence into a Short
     *
     * @param self a CharSequence
     * @return a Short
     * @see #toShort(String)
     * @since 1.8.2
     */
    public static Short toShort(CharSequence self) {
        return toShort(self.toString());
    }

    /**
     * Parse a String into a Short
     *
     * @param self a String
     * @return a Short
     * @since 1.5.7
     */
    public static Short toShort(String self) {
        return Short.valueOf(self.trim());
    }

    /**
     * Translates a string by replacing characters from the sourceSet with characters from replacementSet.
     *
     * @param   self the CharSequence that is to be translated
     * @param   sourceSet the set of characters to translate from
     * @param   replacementSet the set of replacement characters
     * @return  The resulting translated <tt>CharSequence</tt>
     * @see #tr(String, String, String)
     * @since 1.8.2
     */
    public static CharSequence tr(final CharSequence self, CharSequence sourceSet, CharSequence replacementSet) throws ClassNotFoundException {
        return tr(self.toString(), sourceSet.toString(), replacementSet.toString());
    }

    /**
     * Translates a string by replacing characters from the sourceSet with characters from replacementSet.
     * If the first character from sourceSet appears in the string, it will be replaced with the first character from replacementSet.
     * If the second character from sourceSet appears in the string, it will be replaced with the second character from replacementSet.
     * and so on for all provided replacement characters.
     * <p>
     * Here is an example which converts the vowels in a word from lower to uppercase:
     * <pre>
     * assert 'hello'.tr('aeiou', 'AEIOU') == 'hEllO'
     * </pre>
     * A character range using regex-style syntax can also be used, e.g. here is an example which converts a word from lower to uppercase:
     * <pre>
     * assert 'hello'.tr('a-z', 'A-Z') == 'HELLO'
     * </pre>
     * Hyphens at the start or end of sourceSet or replacementSet are treated as normal hyphens and are not
     * considered to be part of a range specification. Similarly, a hyphen immediately after an earlier range
     * is treated as a normal hyphen. So, '-x', 'x-' have no ranges while 'a-c-e' has the range 'a-c' plus
     * the '-' character plus the 'e' character.
     * <p>
     * Unlike the unix tr command, Groovy's tr command supports reverse ranges, e.g.:
     * <pre>
     * assert 'hello'.tr('z-a', 'Z-A') == 'HELLO'
     * </pre>
     * If replacementSet is smaller than sourceSet, then the last character from replacementSet is used as the replacement for all remaining source characters as shown here:
     * <pre>
     * assert 'Hello World!'.tr('a-z', 'A') == 'HAAAA WAAAA!'
     * </pre>
     * If sourceSet contains repeated characters, the last specified replacement is used as shown here:
     * <pre>
     * assert 'Hello World!'.tr('lloo', '1234') == 'He224 W4r2d!'
     * </pre>
     * The functionality provided by tr can be achieved using regular expressions but tr provides a much more compact
     * notation and efficient implementation for certain scenarios.
     *
     * @param   self the string that is to be translated
     * @param   sourceSet the set of characters to translate from
     * @param   replacementSet the set of replacement characters
     * @return  The resulting translated <tt>String</tt>
     * @see org.codehaus.groovy.util.StringUtil#tr(String, String, String)
     * @since 1.7.3
     */
    public static String tr(final String self, String sourceSet, String replacementSet) throws ClassNotFoundException {
        return (String) InvokerHelper.invokeStaticMethod("org.codehaus.groovy.util.StringUtil", "tr", new Object[]{self, sourceSet, replacementSet});
    }

    /**
     * Replaces sequences of whitespaces with tabs using tabStops of size 8.
     *
     * @param self A CharSequence to unexpand
     * @return The unexpanded CharSequence
     * @see #unexpand(String)
     * @since 1.8.2
     */
    public static CharSequence unexpand(CharSequence self) {
        return unexpand(self.toString());
    }

    /**
     * Replaces sequences of whitespaces with tabs.
     *
     * @param self A CharSequence to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded CharSequence
     * @see #unexpand(String, int)
     * @since 1.8.2
     */
    public static CharSequence unexpand(CharSequence self, int tabStop) {
        return unexpand(self.toString(), tabStop);
    }

    /**
     * Replaces sequences of whitespaces with tabs using tabStops of size 8.
     *
     * @param self A String to unexpand
     * @return The unexpanded String
     * @since 1.7.3
     * @see #unexpand(String, int)
     */
    public static String unexpand(String self) {
        return unexpand(self, 8);
    }

    /**
     * Replaces sequences of whitespaces with tabs.
     *
     * @param self A String to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded String
     * @since 1.7.3
     */
    public static String unexpand(String self, int tabStop) {
        if (self.length() == 0) return self;
        try {
            StringBuilder builder = new StringBuilder();
            for (String line : readLines(self)) {
                builder.append(unexpandLine(line, tabStop));
                builder.append("\n");
            }
            // remove the normalized ending line ending if it was not present
            if (!self.endsWith("\n")) {
                builder.deleteCharAt(builder.length() - 1);
            }
            return builder.toString();
        } catch (IOException e) {
            /* ignore */
        }
        return self;
    }

    /**
     * Replaces sequences of whitespaces with tabs within a line.
     *
     * @param self A line to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded CharSequence
     * @see #unexpandLine(String, int)
     * @since 1.8.2
     */
    public static CharSequence unexpandLine(CharSequence self, int tabStop) {
        return unexpandLine(self.toString(), tabStop);
    }

    /**
     * Replaces sequences of whitespaces with tabs within a line.
     *
     * @param self A line to unexpand
     * @param tabStop The number of spaces a tab represents
     * @return The unexpanded String
     * @since 1.7.3
     */
    public static String unexpandLine(String self, int tabStop) {
        StringBuilder builder = new StringBuilder(self);
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

}
