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

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.BackingStoreException;

/**
 * Container for shell preferences.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Preferences
{
    private static final java.util.prefs.Preferences STORE = java.util.prefs.Preferences.userRoot().node("/org/codehaus/groovy/tools/shell");

    public static boolean debug = STORE.getBoolean("debug", false);

    public static boolean verbose = STORE.getBoolean("verbose", false);

    public static boolean quiet = STORE.getBoolean("quiet", false);

    public static boolean showLastResult = STORE.getBoolean("show-last-result", true);

    public static boolean sanitizeStackTrace = STORE.getBoolean("sanitize-stack-trace", true);

    public static String[] keys() throws BackingStoreException {
        return STORE.keys();
    }

    public static String get(final String name, final String defaultValue) {
        return STORE.get(name, defaultValue);
    }

    public static String get(final String name) {
        return get(name, null);
    }

    public static void put(final String name, final String value) {
        STORE.put(name, value);
    }

    public static void clear() throws BackingStoreException {
        STORE.clear();
    }

    public static void addChangeListener(final PreferenceChangeListener listener) {
        STORE.addPreferenceChangeListener(listener);
    }
}
