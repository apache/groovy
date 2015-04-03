/**
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
package org.codehaus.groovy.tools.shell.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.PreferenceChangeEvent;

import org.codehaus.groovy.tools.shell.IO;

/**
 * Container for shell preferences.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Preferences
{
    private static final java.util.prefs.Preferences STORE = java.util.prefs.Preferences.userRoot().node("/org/codehaus/groovy/tools/shell");

    public static IO.Verbosity verbosity;

    public static final String VERBOSITY_KEY = "verbosity";
    public static final String SHOW_LAST_RESULT_KEY = "show-last-result";
    public static final String SANITIZE_STACK_TRACE_KEY = "sanitize-stack-trace";
    public static final String EDITOR_KEY = "editor";
    public static final String PARSER_FLAVOR_KEY = "parser-flavor";

    public static final String PARSER_RIGID = "rigid";
    public static final String PARSER_RELAXED = "relaxed";


    static {
        String tmp = STORE.get(VERBOSITY_KEY, IO.Verbosity.INFO.name);
        try {
            verbosity = IO.Verbosity.forName(tmp);
        }
        catch (IllegalArgumentException e) {
            verbosity = IO.Verbosity.INFO;
            STORE.remove(VERBOSITY_KEY);
        }

        addChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(final PreferenceChangeEvent event) {
                if (event.getKey().equals(VERBOSITY_KEY)) {
                    String name = event.getNewValue();

                    if (name == null) {
                        name = IO.Verbosity.INFO.name;
                    }

                    try {
                        verbosity = IO.Verbosity.forName(name);
                    }
                    catch (Exception e) {
                        event.getNode().put(event.getKey(), verbosity.name);
                    }
                }
            }
        });
    }

    public static boolean getShowLastResult() {
        return STORE.getBoolean(SHOW_LAST_RESULT_KEY, true);
    }

    public static boolean getSanitizeStackTrace() {
        return STORE.getBoolean(SANITIZE_STACK_TRACE_KEY, true);
    }

    public static String getEditor() {
        return STORE.get(EDITOR_KEY, System.getenv("EDITOR"));
    }

    public static String getParserFlavor() {
        return STORE.get(PARSER_FLAVOR_KEY, PARSER_RIGID);
    }

    //
    // Store Access
    //
    
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
