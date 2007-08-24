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

package org.codehaus.groovy.tools.shell.util;

import jline.ANSIBuffer.ANSICodes;
import jline.Terminal;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides support for using ANSI color escape codes.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ANSI
{
    //
    // Detection/Enabled Muck
    //

    /**
     * Tries to detect if the current system supports ANSI.
     */
    private static boolean detect() {
        boolean enabled = Terminal.getTerminal().isANSISupported();

        if (!enabled) {
            String force = System.getProperty(ANSI.class.getName() + ".force", "false");
            enabled = Boolean.valueOf(force).booleanValue();
        }

        return enabled;
    }

    /** The detected ANSI support for the current system. */
    public static final boolean DETECTED = detect();

    /** Flag to enable or disable ANSI support at runtime. */
    public static boolean enabled = DETECTED;

    //
    // Codes
    //

    public static interface Codes
    {
        //
        // NOTE: Some fields duplicated from jline.ANSIBuffer.ANSICodes to change access modifiers
        //
        
        int OFF = 0;
        int BOLD = 1;
        int UNDERSCORE = 4;
        int BLINK = 5;
        int REVERSE = 7;
        int CONCEALED = 8;
        
        int FG_BLACK = 30;
        int FG_RED = 31;
        int FG_GREEN = 32;
        int FG_YELLOW = 33;
        int FG_BLUE = 34;
        int FG_MAGENTA = 35;
        int FG_CYAN = 36;
        int FG_WHITE = 37;

        int BLACK = FG_BLACK;
        int RED = FG_RED;
        int GREEN = FG_GREEN;
        int YELLOW = FG_YELLOW;
        int BLUE = FG_BLUE;
        int MAGENTA = FG_MAGENTA;
        int CYAN = FG_CYAN;
        int WHITE = FG_WHITE;
        
        int BG_BLACK = 40;
        int BG_RED = 41;
        int BG_GREEN = 42;
        int BG_YELLOW = 43;
        int BG_BLUE = 44;
        int BG_MAGENTA = 45;
        int BG_CYAN = 46;
        int BG_WHITE = 47;
    }

    /** A map of the field name to the field values for fast lookups. */
    private static final Map CODE_NAMES;

    static {
        // Initialize the map of ANSI code name to number values
        
        Field[] fields = Codes.class.getDeclaredFields();
        Map map = new HashMap();

        try {
            for (int i=0; i<fields.length; i++) {
                String name = fields[i].getName();
                Number value = (Number) fields[i].get(Codes.class);
                map.put(name, value);
            }
        }
        catch (IllegalAccessException e) {
            // This should never happen
            throw new Error(e);
        }

        CODE_NAMES = map;
    }

    /**
     * Returns the ANSI code for the given symbolic name.  Supported symbolic names are all defined as
     * fields in {@link Codes} where the case is not significant.
     */
    public static int codeFor(final String name) {
        assert name != null;

        // All names in the map are upper-case
        String tmp = name.toUpperCase();
        Number code = (Number) CODE_NAMES.get(tmp);
        
        if (code == null) {
            throw new IllegalArgumentException("Invalid ANSI code name: " + name);
        }

        return code.intValue();
    }

    //
    // Buffer
    //

    public static class Buffer
    {
        private final StringBuffer buff = new StringBuffer();

        public boolean autoClear = true;

        public String toString() {
            try {
                return buff.toString();
            }
            finally {
                if (autoClear) clear();
            }
        }

        public void clear() {
            buff.setLength(0);
        }

        public int size() {
            return buff.length();
        }

        public Buffer append(final String text) {
            buff.append(text);

            return this;
        }

        public Buffer append(final Object obj) {
            return append(String.valueOf(obj));
        }

        public Buffer attrib(final int code) {
            if (enabled) {
                buff.append(ANSICodes.attrib(code));
            }

            return this;
        }

        public Buffer attrib(final String text, final int code) {
            assert text != null;

            if (enabled) {
                buff.append(ANSICodes.attrib(code)).append(text).append(ANSICodes.attrib(Codes.OFF));
            }
            else {
                buff.append(text);
            }
            
            return this;
        }

        public Buffer attrib(final String text, final String codeName) {
            return attrib(text, codeFor(codeName));
        }
    }

    //
    // Renderer
    //

    public static class Renderer
    {
        public static final String BEGIN_TOKEN = "@|";

        private static final int BEGIN_TOKEN_SIZE = BEGIN_TOKEN.length();

        public static final String END_TOKEN = "|";

        private static final int END_TOKEN_SIZE = END_TOKEN.length();

        public static final String CODE_TEXT_SEPARATOR  = " ";

        public static final String CODE_LIST_SEPARATOR  = ",";

        private final Buffer buff = new Buffer();

        public String render(final String input) throws RenderException {
            assert input != null;

            // current, prefix and suffix positions
            int c = 0, p, s;

            while (c < input.length()) {
                p = input.indexOf(BEGIN_TOKEN, c);
                if (p < 0) { break; }

                s = input.indexOf(END_TOKEN, p + BEGIN_TOKEN_SIZE);
                if (s < 0) {
                    throw new RenderException("Missing '" + END_TOKEN + "': " + input);
                }

                String expr = input.substring(p + BEGIN_TOKEN_SIZE, s);

                buff.append(input.substring(c, p));

                evaluate(expr);

                c = s + END_TOKEN_SIZE;
            }

            buff.append(input.substring(c));

            return buff.toString();
        }

        private void evaluate(final String input) throws RenderException {
            assert input != null;

            int i = input.indexOf(CODE_TEXT_SEPARATOR);
            if (i < 0) {
                throw new RenderException("Missing ANSI code/text separator '" + CODE_TEXT_SEPARATOR + "': " + input);
            }

            String tmp = input.substring(0, i);
            String[] codes = tmp.split(CODE_LIST_SEPARATOR);
            String text = input.substring(i + 1, input.length());

            for (int j=0; j<codes.length; j++) {
                int code = codeFor(codes[j]);
                buff.attrib(code);
            }

            buff.append(text);

            buff.attrib(Codes.OFF);
        }

        //
        // RenderException
        //

        public static class RenderException
            extends RuntimeException
        {
            public RenderException(final String msg) {
                super(msg);
            }
        }

        //
        // Helpers
        //

        public static boolean test(final String text) {
            return text != null && text.indexOf(BEGIN_TOKEN) >= 0;
        }
    }

    //
    // RenderWriter
    //

    public static class RenderWriter
        extends PrintWriter
    {
        private final Renderer renderer = new Renderer();

        public RenderWriter(final OutputStream out) {
            super(out);
        }

        public RenderWriter(final OutputStream out, final boolean autoFlush) {
            super(out, autoFlush);
        }

        public RenderWriter(final Writer out) {
            super(out);
        }

        public RenderWriter(final Writer out, final boolean autoFlush) {
            super(out, autoFlush);
        }

        private String render(String text) {
            if (Renderer.test(text)) {
                text = renderer.render(text);
            }

            return text;
        }

        public void write(final String text) {
            super.write(render(text));
        }
    }
}
