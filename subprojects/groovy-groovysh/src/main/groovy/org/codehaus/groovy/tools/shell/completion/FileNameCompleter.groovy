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
package org.codehaus.groovy.tools.shell.completion

import groovy.transform.PackageScope
import jline.internal.Configuration

import static jline.internal.Preconditions.checkNotNull

/**
 * A file name completer takes the buffer and issues a list of
 * potential completions.
 * <p/>
 * This completer tries to behave as similar as possible to
 * <i>bash</i>'s file name completion (using GNU readline)
 * with the following exceptions:
 * <p/>
 * <ul>
 * <li>Candidates that are directories will end with "File.separator"</li>
 * <li>Wildcard regular expressions are not evaluated or replaced</li>
 * <li>The "~" character can be used to represent the user's home directory.
 * It cannot fully complete to other users' homes in all operating systems, since java does
 * not provide any way of determining that easily, but it will attempt a simplistic approach.</li>
 * </ul>
 *
 * @since 2.3
 */
@Deprecated
class FileNameCompleter extends jline.console.completer.FileNameCompleter {
    private static final boolean OS_IS_WINDOWS = Configuration.isWindows()
    private final GroovyShell gs = new GroovyShell()

    FileNameCompleter(boolean printSpaceAfterFullCompletion = true, boolean escapeBackslash = false,
                      boolean escapeSpaces = true) {
        this.printSpaceAfterFullCompletion = printSpaceAfterFullCompletion
        this.escapeBackslash = escapeBackslash
        if (OS_IS_WINDOWS) separator = escapeBackslash ? "\\\\" : "\\"
        this.escapeSpaces = escapeSpaces
    }

    private static boolean isWindowsSubsystemForLinux() {
        System.getProperty("os.name").contains('Linux') && System.getProperty('os.version').contains('Microsoft')
    }

    /**
     * True for say, a command-line arg, false for instance inside a String.
     */
    boolean printSpaceAfterFullCompletion

    /**
     * If the filename will be placed inside a single/double quoted String we must escape backslash when on e.g. Windows.
     */
    boolean escapeBackslash

    /**
     * Set false if e.g. the filename will be inside a String. Should not be true if quoteFilenamesWithSpaces is true.
     */
    boolean escapeSpaces

    private String separator

    @Override
    int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
        checkNotNull(candidates)

        buffer = buffer ?: ""
        String translated = buffer
        int adjustment = 0
        if (escapeBackslash) {
            translated = gs.evaluate("'$translated'")
            adjustment = buffer.size() - translated.size()
        }

        // Special character: ~ maps to the user's home directory in most OSs
        if (translated.startsWith("~")) {
            File homeDir = getUserHome()
            if ((OS_IS_WINDOWS || isWindowsSubsystemForLinux()) && (translated.equals("~" + separator()) || translated.equals("~/"))) {
                // for windows ~ isn't recognized at the file system level so replace
                def adjustSize = translated.size()
                String result
                String temp = (homeDir.path + translated.substring(separator().size())).toString().replace('"', '\\"').replace('\'', '\\\'')
                if (escapeBackslash) {
                    temp = temp.replace('\\', '\\\\')
                }
                result = escapeSpaces ? temp.replace(' ', '\\ ') : temp
                candidates << result
                return cursor - adjustSize - adjustment
            } else if (translated.startsWith("~/")) {
                translated = homeDir.path + translated.substring(2)
            } else {
                translated = homeDir.parentFile.absolutePath + separator() + translated.substring(1)
            }
        } else if (!(new File(translated).canonicalFile.exists()) && !(new File(translated).canonicalFile.parentFile?.exists())) {
            String cwd = getUserDir().absolutePath
            translated = cwd + separator() + translated
        }

        File file = new File(translated)
        final File dir

        if ((OS_IS_WINDOWS && translated.endsWith(separator())) || translated.endsWith('/')) {
            dir = file
        } else {
            dir = file.parentFile
        }

        File[] entries = (dir == null) ? new File[0] : dir.listFiles()

        return matchFiles(buffer, translated, entries, candidates)
    }

    private static String canonicalForm(String raw) {
        String result = raw.replace('\\', '/')
        OS_IS_WINDOWS ? result.toLowerCase() : result
    }

    protected int matchFiles(final String buffer, final String translated, final File[] files,
                             final List<CharSequence> candidates) {
        if (files == null) return -1
        for (File file : files) {
            if (canonicalForm(file.getAbsolutePath()).startsWith(canonicalForm(translated))) {
                CharSequence name = file.name
                String renderedName = render(name).toString()
                if (file.isDirectory()) {
                    renderedName += separator
                } else {
                    if (printSpaceAfterFullCompletion) {
                        renderedName += ' '
                    }
                }
                candidates.add(renderedName)
            }
        }

        int index = -1
        int sizeAdjust = 0
        if (separator) {
            index = buffer.lastIndexOf(separator)
            sizeAdjust = separator.size()
        }
        int slashIndex = buffer.lastIndexOf('/')
        if (slashIndex >= 0 && slashIndex > index) {
            index = slashIndex
            sizeAdjust = 1
        }
        return index + sizeAdjust
    }

    @PackageScope CharSequence render(CharSequence name) {
        String temp = name.toString().replace('"', '\\"').replace('\'', '\\\'')
        if (escapeBackslash) {
            temp = temp.replace('\\', '\\\\')
        }
        escapeSpaces ? temp.replace(' ', '\\ ') : temp
    }
}
