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

import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import groovy.lang.Range;
import groovy.lang.Writable;
import groovy.transform.NamedParam;
import groovy.transform.NamedParams;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.max;
import static java.util.Arrays.stream;

/**
 * Formatting methods
 */
public class FormatHelper {

    private FormatHelper() {}

    private static final String SQ = "'";
    private static final String DQ = "\"";

    private static final Object[] EMPTY_ARGS = {};

    // heuristic size to pre-allocate stringbuffers for collections of items
    private static final int ITEM_ALLOCATE_SIZE = 5;

    public static final MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
    private static final String XMLUTIL_CLASS_FULL_NAME = "groovy.xml.XmlUtil";
    private static final String SERIALIZE_METHOD_NAME = "serialize";

    static final Set<String> DEFAULT_IMPORT_PKGS = new HashSet<>();
    static final Set<String> DEFAULT_IMPORT_CLASSES = new HashSet<>();

    static {
        for (String pkgName : ResolveVisitor.DEFAULT_IMPORTS) {
            FormatHelper.DEFAULT_IMPORT_PKGS.add(pkgName.substring(0, pkgName.length() - 1));
        }
        FormatHelper.DEFAULT_IMPORT_CLASSES.add("java.math.BigDecimal");
        FormatHelper.DEFAULT_IMPORT_CLASSES.add("java.math.BigInteger");
    }

    public static String toString(Object arguments) {
        return format(arguments, false, -1, false);
    }

    public static String inspect(Object self) {
        return format(self, true);
    }

    public static String format(Object arguments, boolean verbose) {
        return format(arguments, verbose, -1);
    }

    public static String format(Object arguments, boolean inspect, boolean escapeBackslashes) {
        return format(arguments, inspect, -1);
    }

    public static String format(Object arguments, boolean verbose, int maxSize) {
        return format(arguments, verbose, maxSize, false);
    }

    public static String format(Object arguments, boolean inspect, boolean escapeBackslashes, int maxSize) {
        return format(arguments, inspect, escapeBackslashes, maxSize, false);
    }

    /**
     * Output the {@code toString} for the argument(s) with various options to configure.
     * Configuration options:
     * <pre>
     * <dl>
     *     <dt>safe</dt><dd>provides protection if the {@code toString} throws an exception, in which case the exception is swallowed and a dumber default {@code toString} is used</dd>
     *     <dt>maxSize</dt><dd>will attempt to truncate the output to fit approx the maxSize number of characters, -1 means don't truncate</dd>
     *     <dt>inspect</dt><dd>if false, render a value by its {@code toString}, otherwise use its {@code inspect} value</dd>
     *     <dt>escapeBackSlashes</dt><dd>whether characters like tab, newline, etc. are converted to their escaped rendering ('\t', '\n', etc.)</dd>
     *     <dt>verbose</dt><dd>shorthand to turn on both {@code inspect} and {@code escapeBackslashes}</dd>
     * </dl>
     * </pre>
     *
     * @param options a map of configuration options
     * @param arguments the argument(s) to calculate the {@code toString} for
     * @return the string rendering of the argument(s)
     * @see DefaultGroovyMethods#inspect(Object)
     */
    public static String toString(@NamedParams({
            @NamedParam(value = "safe", type = Boolean.class),
            @NamedParam(value = "maxSize", type = Integer.class),
            @NamedParam(value = "verbose", type = Boolean.class),
            @NamedParam(value = "escapeBackslashes", type = Boolean.class),
            @NamedParam(value = "inspect", type = Boolean.class)
    }) Map<String, Object> options, Object arguments) {
        Object safe = options.get("safe");
        if (!(safe instanceof Boolean)) safe = false;
        Object maxSize = options.get("maxSize");
        if (!(maxSize instanceof Integer)) maxSize = -1;
        Object verbose = options.get("verbose");
        Object inspect = options.get("inspect");
        Object escapeBackslashes = options.get("escapeBackslashes");
        if (!(inspect instanceof Boolean)) inspect = false;
        if (!(escapeBackslashes instanceof Boolean)) escapeBackslashes = false;
        if (!(verbose instanceof Boolean)) verbose = false;
        if (Boolean.TRUE.equals(verbose)) {
            inspect = true;
            escapeBackslashes = true;
        }
        return format(arguments, (boolean) inspect, (boolean) escapeBackslashes, (int) maxSize, (boolean) safe);
    }

    public static String format(Object arguments, boolean verbose, int maxSize, boolean safe) {
        return format(arguments, verbose, verbose, maxSize, safe);
    }

    public static String format(Object arguments, boolean inspect, boolean escapeBackslashes, int maxSize, boolean safe) {
        if (arguments == null) {
            final NullObject nullObject = NullObject.getNullObject();
            return (String) nullObject.getMetaClass().invokeMethod(nullObject, "toString", EMPTY_ARGS);
        }
        if (arguments.getClass().isArray()) {
            if (arguments instanceof Object[]) {
                return toArrayString((Object[]) arguments, inspect, escapeBackslashes, maxSize, safe);
            }
            if (arguments instanceof char[]) {
                return new String((char[]) arguments);
            }
            // other primitives
            return formatCollection(DefaultTypeTransformation.arrayAsCollection(arguments), inspect, escapeBackslashes, maxSize, safe);
        }
        if (arguments instanceof Range) {
            Range range = (Range) arguments;
            try {
                if (inspect) {
                    return range.inspect();
                } else {
                    return range.toString();
                }
            } catch (RuntimeException ex) {
                if (!safe) throw ex;
                return handleFormattingException(arguments, ex);
            } catch (Exception ex) {
                if (!safe) throw new GroovyRuntimeException(ex);
                return handleFormattingException(arguments, ex);
            }
        }
        if (arguments instanceof Collection) {
            return formatCollection((Collection) arguments, inspect, escapeBackslashes, maxSize, safe);
        }
        if (arguments instanceof Map) {
            return formatMap((Map) arguments, inspect, escapeBackslashes, maxSize, safe);
        }
        if (arguments instanceof Element) {
            try {
                Method serialize = Class.forName(XMLUTIL_CLASS_FULL_NAME).getMethod(SERIALIZE_METHOD_NAME, Element.class);
                return (String) serialize.invoke(null, arguments);
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        if (arguments instanceof CharSequence) {
            String arg = escapeBackslashes ? escapeBackslashes(arguments.toString()) : arguments.toString();
            if (arguments instanceof String) {
                if (!inspect) return arg;
                return !escapeBackslashes && multiline(arg) ? "'''" + arg + "'''" : SQ + arg.replace(SQ, "\\'") + SQ;
            }
            if (!inspect) return arg;
            return !escapeBackslashes && multiline(arg) ? "\"\"\"" + arg + "\"\"\"" : DQ + arg.replace(DQ, "\\\"") + DQ;
        }
        try {
            // TODO: For GROOVY-2599 do we need something like below but it breaks other things
//            return (String) invokeMethod(arguments, "toString", EMPTY_ARGS);
            return arguments.toString();
        } catch (RuntimeException ex) {
            if (!safe) throw ex;
            return handleFormattingException(arguments, ex);
        } catch (Exception ex) {
            if (!safe) throw new GroovyRuntimeException(ex);
            return handleFormattingException(arguments, ex);
        }
    }

    private static boolean multiline(String s) {
        return s.contains("\n") || s.contains("\r");
    }

    public static String escapeBackslashes(String orig) {
        // must replace backslashes first, as the other replacements add backslashes not to be escaped
        return orig
                .replace("\\", "\\\\")           // backslash
                .replace("\n", "\\n")            // line feed
                .replace("\r", "\\r")            // carriage return
                .replace("\t", "\\t")            // tab
                .replace("\f", "\\f");           // form feed
    }

    private static String handleFormattingException(Object item, Exception ex) {

        String hash;
        try {
            hash = Integer.toHexString(item.hashCode());
        } catch (Exception ignored) {
            hash = "????";
        }
        return "<" + typeName(item) + "@" + hash + ">";
    }

    private static String formatMap(Map map, boolean inspect, boolean escapeBackslashes, int maxSize, boolean safe) {
        if (map.isEmpty()) {
            return "[:]";
        }
        StringBuilder buffer = new StringBuilder(ITEM_ALLOCATE_SIZE * map.size() * 2);
        buffer.append('[');
        boolean first = true;
        for (Object o : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            if (maxSize != -1 && buffer.length() > maxSize) {
                buffer.append("...");
                break;
            }
            Map.Entry entry = (Map.Entry) o;
            if (entry.getKey() == map) {
                buffer.append("(this Map)");
            } else {
                buffer.append(format(entry.getKey(), inspect, escapeBackslashes, sizeLeft(maxSize, buffer), safe));
            }
            buffer.append(":");
            if (entry.getValue() == map) {
                buffer.append("(this Map)");
            } else {
                buffer.append(format(entry.getValue(), inspect, escapeBackslashes, sizeLeft(maxSize, buffer), safe));
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    private static int sizeLeft(int maxSize, StringBuilder buffer) {
        return maxSize == -1 ? maxSize : max(0, maxSize - buffer.length());
    }

    private static String formatCollection(Collection collection, boolean inspect, boolean escapeBackslashes, int maxSize, boolean safe) {
        StringBuilder buffer = new StringBuilder(ITEM_ALLOCATE_SIZE * collection.size());
        buffer.append('[');
        boolean first = true;
        for (Object item : collection) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            if (maxSize != -1 && buffer.length() > maxSize) {
                buffer.append("...");
                break;
            }
            if (item == collection) {
                buffer.append("(this Collection)");
            } else {
                buffer.append(format(item, inspect, escapeBackslashes, sizeLeft(maxSize, buffer), safe));
            }
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * A helper method to format the arguments types as a comma-separated list.
     *
     * @param arguments the type to process
     * @return the string representation of the type
     */
    public static String toTypeString(Object[] arguments) {
        return toTypeString(arguments, -1);
    }

    /**
     * A helper method to format the arguments types as a comma-separated list.
     *
     * @param arguments the type to process
     * @param maxSize   stop after approximately this many characters and append '...', -1 means don't stop
     * @return the string representation of the type
     */
    public static String toTypeString(Object[] arguments, int maxSize) {
        if (arguments == null) {
            return "null";
        }
        if (arguments.length == 0) {
            return "";
        }
        if (maxSize < 0) {
            return stream(arguments)
                    .map(arg -> arg != null ? typeName(arg) : "null")
                    .collect(java.util.stream.Collectors.joining(", "));
        }

        var plainForm = new StringBuilder();
        var shortForm = new StringBuilder();
        for (int i = 0; i < arguments.length; i += 1) {
            String type = arguments[i] != null ? typeName(arguments[i]) : "null";

            if (plainForm.length() < maxSize) {
                if (i > 0) plainForm.append(", ");
                plainForm.append(type);
            } else if (plainForm.charAt(plainForm.length() - 1) != '.') {
                plainForm.append("...");
            }

            if (shortForm.length() < maxSize) {
                if (i > 0) shortForm.append(", ");
                String[] tokens = type.split("\\.");
                for (int j = 0; j < tokens.length - 1; j += 1) {
                    // GROOVY-11270: reduce "foo.bar.Baz" to "f.b.Baz"
                    shortForm.appendCodePoint(tokens[j].codePointAt(0)).append('.');
                }
                shortForm.append(tokens[tokens.length - 1]);
            } else {
                shortForm.append("...");
                break;
            }
        }

        return (plainForm.length() <= maxSize ? plainForm : shortForm).toString();
    }

    /**
     * Gets the type name
     *
     * @param argument the object to find the type for
     * @return the type name (slightly pretty format taking into account default imports)
     */
    static String typeName(Object argument) {
        Class<?> aClass = argument.getClass();
        String pkgName = aClass.getPackage() == null ? "" : aClass.getPackage().getName();
        boolean useShort = DEFAULT_IMPORT_PKGS.contains(pkgName) || DEFAULT_IMPORT_CLASSES.contains(aClass.getName());
        return useShort ? aClass.getSimpleName() : aClass.getName();
    }

    /**
     * A helper method to return the string representation of a map with bracket boundaries "[" and "]".
     *
     * @param arg the map to process
     * @return the string representation of the map
     */
    public static String toMapString(Map arg) {
        return toMapString(arg, -1);
    }

    /**
     * A helper method to return the string representation of a map with bracket boundaries "[" and "]".
     *
     * @param arg     the map to process
     * @param maxSize stop after approximately this many characters and append '...', -1 means don't stop
     * @return the string representation of the map
     */
    public static String toMapString(Map arg, int maxSize) {
        return formatMap(arg, false, false, maxSize, false);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg the collection to process
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg) {
        return toListString(arg, -1);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg     the collection to process
     * @param maxSize stop after approximately this many characters and append '...'
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg, int maxSize) {
        return toListString(arg, maxSize, false);
    }

    /**
     * A helper method to return the string representation of a list with bracket boundaries "[" and "]".
     *
     * @param arg     the collection to process
     * @param maxSize stop after approximately this many characters and append '...', -1 means don't stop
     * @param safe    whether to use a default object representation for any item in the collection if an exception occurs when generating its toString
     * @return the string representation of the collection
     */
    public static String toListString(Collection arg, int maxSize, boolean safe) {
        return formatCollection(arg, false, false, maxSize, safe);
    }

    /**
     * A helper method to return the string representation of an array of objects
     * with brace boundaries "[" and "]".
     *
     * @param arguments the array to process
     * @return the string representation of the array
     */
    public static String toArrayString(Object[] arguments) {
        return toArrayString(arguments, false, false, -1, false);
    }

    private static String toArrayString(Object[] array, boolean inspect, boolean escapeBackslashes, int maxSize, boolean safe) {
        if (array == null) {
            return "null";
        }
        boolean first = true;
        StringBuilder argBuf = new StringBuilder(array.length);
        argBuf.append('[');

        for (Object item : array) {
            if (first) {
                first = false;
            } else {
                argBuf.append(", ");
            }
            if (maxSize != -1 && argBuf.length() > maxSize) {
                argBuf.append("...");
                break;
            }
            if (item == array) {
                argBuf.append("(this array)");
            } else {
                argBuf.append(format(item, inspect, escapeBackslashes, sizeLeft(maxSize, argBuf), safe));
            }
        }
        argBuf.append(']');
        return argBuf.toString();
    }

    /**
     * A helper method to return the string representation of an array of objects
     * with brace boundaries "[" and "]".
     *
     * @param arguments the array to process
     * @param maxSize   stop after approximately this many characters and append '...'
     * @param safe      whether to use a default object representation for any item in the array if an exception occurs when generating its toString
     * @return the string representation of the array
     */
    public static String toArrayString(Object[] arguments, int maxSize, boolean safe) {
        return toArrayString(arguments, false, false, maxSize, safe);
    }

    /**
     * Writes an object to a Writer using Groovy's default representation for the object.
     */
    public static void write(Writer out, Object object) throws IOException {
        if (object instanceof String) {
            out.write((String) object);
        } else if (object instanceof Object[]) {
            out.write(toArrayString((Object[]) object));
        } else if (object instanceof Map) {
            out.write(toMapString((Map) object));
        } else if (object instanceof Collection) {
            out.write(toListString((Collection) object));
        } else if (object instanceof Writable) {
            Writable writable = (Writable) object;
            writable.writeTo(out);
        } else if (object instanceof InputStream || object instanceof Reader) {
            // Copy stream to stream
            Reader reader;
            if (object instanceof InputStream) {
                reader = new InputStreamReader((InputStream) object);
            } else {
                reader = (Reader) object;
            }

            try (Reader r = reader) {
                char[] chars = new char[8192];
                for (int i; (i = r.read(chars)) != -1; ) {
                    out.write(chars, 0, i);
                }
            }
        } else {
            out.write(toString(object));
        }
    }

    /**
     * Appends an object to an Appendable using Groovy's default representation for the object.
     */
    public static void append(Appendable out, Object object) throws IOException {
        if (object instanceof String) {
            out.append((String) object);
        } else if (object instanceof Object[]) {
            out.append(toArrayString((Object[]) object));
        } else if (object instanceof Map) {
            out.append(toMapString((Map) object));
        } else if (object instanceof Collection) {
            out.append(toListString((Collection) object));
        } else if (object instanceof Writable) {
            Writable writable = (Writable) object;
            Writer stringWriter = new StringBuilderWriter();
            writable.writeTo(stringWriter);
            out.append(stringWriter.toString());
        } else if (object instanceof InputStream || object instanceof Reader) {
            // Copy stream to stream
            try (Reader reader =
                         object instanceof InputStream
                                 ? new InputStreamReader((InputStream) object)
                                 : (Reader) object) {
                char[] chars = new char[8192];
                for (int i; (i = reader.read(chars)) != -1; ) {
                    for (int j = 0; j < i; j++) {
                        out.append(chars[j]);
                    }
                }
            }
        } else {
            out.append(toString(object));
        }
    }
}
