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
package org.codehaus.groovy.tools.shell.util;

import org.codehaus.groovy.tools.shell.IO;

import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Container for shell preferences.
 */
public class Preferences {
    private static final java.util.prefs.Preferences STORE = java.util.prefs.Preferences.userRoot().node("/org/codehaus/groovy/tools/shell");

    /**
     * Current shell verbosity preference.
     */
    public static IO.Verbosity verbosity;

    /**
     * Preference key storing the selected verbosity level.
     */
    public static final String VERBOSITY_KEY = "verbosity";
    /**
     * Preference key controlling whether the last result is shown.
     */
    public static final String SHOW_LAST_RESULT_KEY = "show-last-result";
    /**
     * Preference key controlling stack-trace sanitization.
     */
    public static final String SANITIZE_STACK_TRACE_KEY = "sanitize-stack-trace";
    /**
     * Preference key storing the preferred editor command.
     */
    public static final String EDITOR_KEY = "editor";
    /**
     * Preference key storing the parser flavor.
     */
    public static final String PARSER_FLAVOR_KEY = "parser-flavor";

    /**
     * Parser flavor value selecting rigid parsing.
     */
    public static final String PARSER_RIGID = "rigid";
    /**
     * Parser flavor value selecting relaxed parsing.
     */
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
            /**
             * Updates cached verbosity when the stored preference changes.
             *
             * @param event the preference change event
             */
            @Override
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

    /**
     * Returns whether the shell should display the last evaluation result.
     *
     * @return {@code true} if the last result should be shown
     */
    public static boolean getShowLastResult() {
        return STORE.getBoolean(SHOW_LAST_RESULT_KEY, true);
    }

    /**
     * Returns whether stack traces should be sanitized before display.
     *
     * @return {@code true} if stack traces should be sanitized
     */
    public static boolean getSanitizeStackTrace() {
        return STORE.getBoolean(SANITIZE_STACK_TRACE_KEY, true);
    }

    /**
     * Returns the configured editor command.
     *
     * @return the configured editor command, or the {@code EDITOR}
     * environment variable if none is stored
     */
    public static String getEditor() {
        return STORE.get(EDITOR_KEY, System.getenv("EDITOR"));
    }

    /**
     * Returns the configured parser flavor.
     *
     * @return the parser flavor name
     */
    public static String getParserFlavor() {
        return STORE.get(PARSER_FLAVOR_KEY, PARSER_RIGID);
    }

    //
    // Store Access
    //

    /**
     * Returns all stored preference keys.
     *
     * @return the stored preference keys
     * @throws BackingStoreException if the backing store cannot be queried
     */
    public static String[] keys() throws BackingStoreException {
        return STORE.keys();
    }

    /**
     * Returns a preference value with a fallback.
     *
     * @param name the preference key
     * @param defaultValue the fallback value to return when the key is absent
     * @return the stored or fallback value
     */
    public static String get(final String name, final String defaultValue) {
        return STORE.get(name, defaultValue);
    }

    /**
     * Returns a preference value or {@code null} when absent.
     *
     * @param name the preference key
     * @return the stored value, or {@code null} if absent
     */
    public static String get(final String name) {
        return get(name, null);
    }

    /**
     * Stores a preference value.
     *
     * @param name the preference key
     * @param value the value to store
     */
    public static void put(final String name, final String value) {
        STORE.put(name, value);
    }

    /**
     * Clears all stored shell preferences.
     *
     * @throws BackingStoreException if the backing store cannot be updated
     */
    public static void clear() throws BackingStoreException {
        STORE.clear();
    }

    /**
     * Registers a listener for preference changes.
     *
     * @param listener the listener to register
     */
    public static void addChangeListener(final PreferenceChangeListener listener) {
        STORE.addPreferenceChangeListener(listener);
    }
}
