/*
 * Adapted from JLine which has the following license
 *
 *  Copyright (c) 2002-2012, the original author or authors.
 *  This software is distributable under the BSD license. See the terms of the
 *  BSD license in the documentation provided with this software.
 *
 *    http://www.opensource.org/licenses/bsd-license.php
 *
 * Subsequent modifications by the Groovy community have been done under the Apache License v2:
 *
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
package org.codehaus.groovy.tools.shell.completion

import jline.console.completer.Completer
import jline.internal.Configuration

import static jline.internal.Preconditions.checkNotNull

/**
 * PATCHED copy from jline 2.12, with
 * https://github.com/jline/jline2/issues/90 (no trailing blank)
 * NOTE: we hope to work with the jline project to have this functionality
 * absorbed into a future jline release and then remove this file, so keep
 * that in mind if you are thinking of changing this file.
 *
 * A file name completer takes the buffer and issues a list of
 * potential completions.
 * <p/>
 * This completer tries to behave as similar as possible to
 * <i>bash</i>'s file name completion (using GNU readline)
 * with the following exceptions:
 * <p/>
 * <ul>
 * <li>Candidates that are directories will end with "/"</li>
 * <li>Wildcard regular expressions are not evaluated or replaced</li>
 * <li>The "~" character can be used to represent the user's home,
 * but it cannot complete to other users' homes, since java does
 * not provide any way of determining that easily</li>
 * </ul>
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public class FileNameCompleter
implements Completer
{
    // TODO: Handle files with spaces in them

    private static final boolean OS_IS_WINDOWS;

    private final boolean blankSuffix = true;

    private final handleLeadingHyphen = false;

    static {
        String os = Configuration.getOsName();
        OS_IS_WINDOWS = os.contains("windows");
    }

    public FileNameCompleter() {
    }

    public FileNameCompleter(boolean blankSuffix) {
        this.blankSuffix = blankSuffix;
    }


    public FileNameCompleter(boolean blankSuffix, boolean handleLeadingHyphen) {
        this(blankSuffix)
        this.handleLeadingHyphen = handleLeadingHyphen
    }

    public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
        // buffer can be null
        checkNotNull(candidates);
        String hyphenChar = null;

        if (buffer == null) {
            buffer = "";
        }

        if (OS_IS_WINDOWS) {
            buffer = buffer.replace('/', '\\');
        }

        String translated = buffer;
        if (handleLeadingHyphen && (translated.startsWith('\'') || translated.startsWith('"'))) {
            hyphenChar = translated[0];
            translated = translated.substring(1);
        }

        File homeDir = getUserHome();

        // Special character: ~ maps to the user's home directory
        if (translated.startsWith("~" + separator())) {
            translated = homeDir.getPath() + translated.substring(1);
        }
        else if (translated.startsWith("~")) {
            translated = homeDir.getParentFile().getAbsolutePath();
        }
        else if (!(new File(translated).isAbsolute())) {
            String cwd = getUserDir().getAbsolutePath();
            translated = cwd + separator() + translated;
        }

        File file = new File(translated);
        final File dir;

        if (translated.endsWith(separator())) {
            dir = file;
        }
        else {
            dir = file.getParentFile();
        }

        File[] entries = (dir == null) ? new File[0] : dir.listFiles();

        return matchFiles(buffer, translated, entries, candidates, hyphenChar);
    }

    protected String separator() {
        return File.separator;
    }

    protected File getUserHome() {
        return Configuration.getUserHome();
    }

    protected File getUserDir() {
        return new File(".");
    }

    protected int matchFiles(final String buffer, final String translated, final File[] files, final List<CharSequence> candidates, hyphenChar) {
        if (files == null) {
            return -1;
        }

        int matches = 0;

        // first pass: just count the matches
        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                matches++;
            }
        }
        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                CharSequence name = file.getName()
                if (matches == 1) {
                    if (file.isDirectory()) {
                        name += separator();
                    } else {
                        if (blankSuffix && !hyphenChar) {
                            name += ' ';
                        }
                    }
                }
                candidates.add(render(name, hyphenChar).toString());
            }
        }

        final int index = buffer.lastIndexOf(separator());

        return index + separator().length();
    }

    protected CharSequence render(final CharSequence name, final String hyphenChar) {
        if (hyphenChar != null) {
            return escapedNameInHyphens(name, hyphenChar);
        }
        if (name.contains(' ')) {
            return escapedNameInHyphens(name, '\'');
        }
        return name;
    }

    private String escapedNameInHyphens(String name, String hyphen) {
        // need to escape every instance of chartoEscape, and every instance of the escape char backslash
        return hyphen + name.replace('\\', '\\\\').replace(hyphen, '\\' + hyphen) + hyphen
    }
}
