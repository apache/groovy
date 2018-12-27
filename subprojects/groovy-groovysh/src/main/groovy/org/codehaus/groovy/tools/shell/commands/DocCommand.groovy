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
package org.codehaus.groovy.tools.shell.commands

import jline.console.completer.Completer
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * The 'doc' command.
 *
 * @since 2.2.0
 */
class DocCommand extends CommandSupport {

    public static final String COMMAND_NAME = ':doc'

    private static final String ENV_BROWSER = 'BROWSER'
    private static final String ENV_BROWSER_GROOVYSH = 'GROOVYSH_BROWSER'

    private static final int TIMEOUT_CONN = 5 * 1000 // ms
    private static final int TIMEOUT_READ = 5 * 1000 // ms

    // indicates support for java.awt.Desktop#browse on the current platform
    private static boolean hasAWTDesktopPlatformSupport
    private static desktop

    /**
     * Check for java.awt.Desktop#browse platform support
     */
    static {
        try {
            def desktopClass = Class.forName('java.awt.Desktop')
            desktop = desktopClass.desktopSupported ? desktopClass.desktop : null

            hasAWTDesktopPlatformSupport =
                desktop != null &&
                        desktop.isSupported(desktopClass.declaredClasses.find { it.simpleName == 'Action' }.BROWSE)

        } catch (Exception e) {
            hasAWTDesktopPlatformSupport = false
            desktop = null
        }
    }

    DocCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':D')
    }

    @Override
    protected List<Completer> createCompleters() {
        return [new ImportCompleter(shell.packageHelper, shell.interp, false),
                null]
    }

    @Override
    Object execute(final List<String> args) {
        if (args?.size() == 1) {
            doc(args[0])
            return
        }
        fail(messages.format('error.unexpected_args', args ? args.join(' ') : 'no arguments'))
    }

    void doc(String className) {
        def normalizedClassName = normalizeClassName(className)
        def urls = urlsFor(normalizedClassName)
        if (urls.empty) {
            fail("Documentation for \"${normalizedClassName}\" could not be found.")
        }

        // Print the URLs.
        // It is useful especially when the browsing fails.
        urls.each { url -> io.out.println url }

        browse(urls)
    }

    protected String normalizeClassName(String className) {
        className.replaceAll('"', '').replaceAll("'", '')
    }

    protected void browse(List urls) {
        def browser = browserEnvironmentVariable

        // fallback to java.awt.Desktop in case of missing env variable(s)
        if (browser) {
            browseWithNativeBrowser(browser, urls)
        } else if (hasAWTDesktopPlatformSupport) {
            browseWithAWT(urls)
        } else {
            fail 'Browser could not be opened due to missing platform support for "java.awt.Desktop". Please set ' +
                 "a $ENV_BROWSER_GROOVYSH or $ENV_BROWSER environment variable referring to the browser binary to " +
                 'solve this issue.'
        }
    }

    protected String getBrowserEnvironmentVariable() {
        System.getenv(ENV_BROWSER_GROOVYSH) ?: System.getenv(ENV_BROWSER)
    }

    protected void browseWithAWT(List urls) {
        try {
            urls.each { url -> desktop.browse(url.toURI()) }
        } catch (Exception e) {
            fail "Browser could not be opened, an unexpected error occured (${e}). You can add a " +
                 "$ENV_BROWSER_GROOVYSH or $ENV_BROWSER environment variable to explicitly specify a browser binary."
        }
    }

    protected void browseWithNativeBrowser(String browser, List urls) {
        try {
            "$browser ${urls.join(' ')}".execute()
        } catch (Exception e) {
            // we could be here caused by a IOException, SecurityException or NP Exception
            fail "Browser could not be opened (${e}). Please check the $ENV_BROWSER_GROOVYSH or $ENV_BROWSER " +
                 "environment variable."
        }
    }

    protected List urlsFor(String className) {
        String groovyVersion = GroovySystem.version
        def path = className.replaceAll(/\./, '/') + '.html'

        def urls = []
        if (className.matches(/^(groovy|org\.codehaus\.groovy|)\..+/)) {
            def url = new URL("http://docs.groovy-lang.org/$groovyVersion/html/gapi/$path")
            if (sendHEADRequest(url)) {
                urls << url
            }
        } else {
            // Don't specify package names to not depend on a specific version of Java SE.
            // Java SE includes non-java(x) packages such as org.w3m.*, org.omg.*. org.xml.* for now
            // and new packages might be added in the future.
            def url = new URL("http://docs.oracle.com/javase/${simpleVersion()}/docs/api/$path")
            if (sendHEADRequest(url)) {
                urls << url
                url = new URL("http://docs.groovy-lang.org/$groovyVersion/html/groovy-jdk/$path")
                if (sendHEADRequest(url)) {
                    urls << url
                }
            }
        }

        urls
    }

    private static simpleVersion() {
        String javaVersion = System.getProperty('java.version')
        if (javaVersion.startsWith('1.')) {
            javaVersion.split(/\./)[1]
        } else {
            // java 9 and above
            javaVersion.replaceAll(/-.*/, '').split(/\./)[0]
        }
    }

    protected boolean sendHEADRequest(URL url) {
        try {
            HttpURLConnection conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = 'HEAD'
            conn.connectTimeout = TIMEOUT_CONN
            conn.readTimeout = TIMEOUT_READ
            conn.instanceFollowRedirects = true

            return conn.responseCode == 200

        } catch (IOException e) {
            fail "Sending a HEAD request to $url failed (${e}). Please check your network settings."
        }
    }

}

