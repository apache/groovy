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
 * https://github.com/jline/jline2/pull/204
 *
 * NOTE: we hope to work with the jline project to have this functionality
 * absorbed into a future jline release and then remove this file, so keep
 * that in mind if you are thinking of changing this file.
 */

 /**
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
    private static final boolean OS_IS_WINDOWS;

    private boolean printSpaceAfterFullCompletion = true;

    public boolean getPrintSpaceAfterFullCompletion() {
        return printSpaceAfterFullCompletion;
    }

    public void setPrintSpaceAfterFullCompletion(boolean printSpaceAfterFullCompletion) {
        this.printSpaceAfterFullCompletion = printSpaceAfterFullCompletion;
    }

    static {
        String os = Configuration.getOsName();
        OS_IS_WINDOWS = os.contains("windows");
    }

    public FileNameCompleter() {
    }

    public FileNameCompleter(boolean printSpaceAfterFullCompletion) {
        this.printSpaceAfterFullCompletion = printSpaceAfterFullCompletion;
    }


    public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
        // buffer can be null
        checkNotNull(candidates);

        if (buffer == null) {
            buffer = "";
        }

        if (OS_IS_WINDOWS) {
            buffer = buffer.replace('/', '\\');
        }

        String translated = buffer;

        // Special character: ~ maps to the user's home directory in most OSs
        if (!OS_IS_WINDOWS && translated.startsWith("~")) {
            File homeDir = getUserHome();
            if (translated.startsWith("~" + separator())) {
                translated = homeDir.getPath() + translated.substring(1);
            }
            else {
                translated = homeDir.getParentFile().getAbsolutePath();
            }
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

        return matchFiles(buffer, translated, entries, candidates);
    }

    protected static String separator() {
        return File.separator;
    }

    protected static File getUserHome() {
        return Configuration.getUserHome();
    }

    /*
     * non static for testing
     */
    protected File getUserDir() {
        return new File(".");
    }

    protected int matchFiles(final String buffer, final String translated, final File[] files, final List<CharSequence> candidates) {
        if (files == null) {
            return -1;
        }

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                CharSequence name = file.getName();
                String renderedName = render(name).toString();
                if (file.isDirectory()) {
                    renderedName += separator();
                } else {
                    if (printSpaceAfterFullCompletion) {
                        renderedName += ' '
                    }
                }

                candidates.add(renderedName);
            }
        }

        final int index = buffer.lastIndexOf(separator());

        return index + separator().length();
    }

    /**
     * @param name
     * @param hyphenChar force hyphenation with this if not null
     * @return name in hyphens if it contains a blank
     */
    protected static CharSequence render(final CharSequence name) {
        return escapedName(name);
    }

    /**
     *
     * @return name in hyphens Strings with hyphens and backslashes escaped
     */
    private static String escapedName(final CharSequence name) {
        // Escape blanks, hyphens and escape characters
        return name.toString().replace('\\', '\\\\').replace('"', '\\"').replace('\'', '\\\'').replace(' ', '\\ ')
    }
}
