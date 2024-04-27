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
package org.apache.groovy.groovysh.commands

import jline.console.completer.Completer
import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Groovysh

/**
 * The 'doc' command.
 *
 * @since 2.2.0
 */
class DocCommand extends CommandSupport {

    public static final String COMMAND_NAME = ':doc'

    private static final String ENV_BROWSER = 'BROWSER'
    private static final String ENV_BROWSER_GROOVYSH = 'GROOVYSH_BROWSER'
    private static final List<String> PRIMITIVES = ['boolean', 'byte', 'short', 'char', 'int', 'long', 'float', 'double']

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
        if (args?.size() == 2) {
            doc(args[1], args[0])
            return
        }
        fail(messages.format('error.unexpected_args', args ? args.join(' ') : 'no arguments'))
    }

    void doc(String className, String module = null) {
        def normalizedClassName = normalizeClassName(className)
        def normalizedModule = normalizeClassName(module ?: '')
        def urls = urlsFor(normalizedClassName, normalizedModule)
        if (urls.empty) {
            fail("Documentation for \"${normalizedClassName}\" could not be found.")
        }

        // Print the URLs.
        // It is useful especially when the browsing fails.
        urls.each { url -> io.out.println url }

        browse(urls)
    }

    protected String normalizeClassName(String className) {
        className.replace('"', '').replace("'", '').replace('[', '%5B').replace(']', '%5D')
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

    protected List urlsFor(String className, String module = '') {
        String groovyVersion = GroovySystem.getVersion()
        String path = className.replace('.', '/') + '.html'

        def url
        def urls = []
        if (!module && className.matches(/^(?:org\.(?:apache|codehaus)\.)?groovy\..+/)) {
            if (sendHEADRequest(url = new URL("https://docs.groovy-lang.org/$groovyVersion/html/gapi/$path"), path)) {
                urls << url
            }
        }
        // Don't specify package names to not depend on a specific version of Java SE.
        // Java SE includes non-java(x) packages such as org.omg.*, org.w3c.*, org.xml.* for now
        // and new packages might be added in the future.
        if (sendHEADRequest(url = new URL("https://docs.oracle.com/${versionPrefix(module)}/$path"), path) ||
            sendHEADRequest(url = new URL("https://download.java.net/java/early_access/${versionPrefix(module, true)}/$path"), path)) {
            urls << url
        } else if (!module) {
            // if no module specified, fall back to JDK8 if java.base url wasn't found
            if (sendHEADRequest(url = new URL("https://docs.oracle.com/javase/8/docs/api/$path"), path)) {
                urls << url
            }
        }
        // make accessing enhancements for e.g. int[] or double[][] easier
        if (PRIMITIVES.any{path.startsWith(it) }) {
            path = "primitives-and-primitive-arrays/$path"
        }

        if (sendHEADRequest(url = new URL("https://docs.groovy-lang.org/$groovyVersion/html/groovy-jdk/$path"), path)) {
            urls << url
        }

        urls
    }

    private static versionPrefix(String module, boolean ea = false) {
        String javaVersion = System.getProperty('java.version')
        if (javaVersion.startsWith('1.')) {
            'javase/' + javaVersion.split(/\./)[1] + '/docs/api'
        } else {
            // java 9 and above
            def mod = module ?: 'java.base'
            def ver = javaVersion.replaceAll(/-.*/, '').split(/\./)[0]
            "${(ea ? 'jdk' : 'en/java/javase/')}$ver/docs/api/$mod"
        }
    }

    protected boolean sendHEADRequest(URL url, String path = null) {
        IOException ioe
        // try at most 3 times
        for (int i = 0; i < 3; i++) {
            try {
                return doSendHEADRequest(url, path)
            } catch (SocketTimeoutException e) {
                ioe = e
            } catch (IOException e) {
                ioe = e
                break
            }
        }

        io.out.println "Sending a HEAD request to $url failed (${ioe}). Please check your network settings."
        // allow timeout to fail since this will happen if we check e.g. for an early access URL for a release JDK version
        if (ioe !instanceof SocketTimeoutException) fail "Unable to get URLs for documentation."

        return false
    }

    private boolean doSendHEADRequest(URL url, String path = null) {
        HttpURLConnection conn = null
        try {
            conn = (HttpURLConnection) url.openConnection()
            conn.setInstanceFollowRedirects(true)
            conn.setConnectTimeout(TIMEOUT_CONN)
            conn.setReadTimeout(TIMEOUT_READ)
            conn.setRequestMethod('HEAD')

            // if not found, redirects to search page, which we don't count as successful
            // if no path given (legacy calls from third parties), treat all redirects as suspicious
            String  connectionURL = conn.getURL().toString()
            boolean successfulRedirect = path ? connectionURL.endsWith(path) : connectionURL.equals(url.toString())

            return (conn.getResponseCode() == HttpURLConnection.HTTP_OK) && (conn.getContentLength() > 0) && successfulRedirect
        } finally {
            conn?.disconnect()
        }
    }
}
