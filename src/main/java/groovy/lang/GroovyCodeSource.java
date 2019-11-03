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
package groovy.lang;

import groovy.security.GroovyCodeSourcePermission;
import groovy.util.CharsetToolkit;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Objects;

/**
 * CodeSource wrapper class that allows specific security policies to be associated with a class
 * compiled from groovy source.
 */
public class GroovyCodeSource {

    /**
     * The codeSource to be given the generated class.  This can be used by policy file
     * grants to administer security.
     */
    private CodeSource codeSource;

    /**
     * The name given to the generated class
     */
    private String name;

    /**
     * The groovy source to be compiled and turned into a class
     */
    private String scriptText;

    /**
     * The certificates used to sign the items from the codesource
     */
    Certificate[] certs;

    private boolean cachable;

    private File file;

    private URL url;

    public GroovyCodeSource(String script, String name, String codeBase) {
        this.name = name;
        this.scriptText = script;
        this.codeSource = createCodeSource(codeBase);
        this.cachable = true;
    }

    /**
     * Construct a GroovyCodeSource for an inputStream of groovyCode that has an
     * unknown provenance -- meaning it didn't come from a File or a URL (e.g.&#160;a String).
     * The supplied codeBase will be used to construct a File URL that should match up
     * with a java Policy entry that determines the grants to be associated with the
     * class that will be built from the InputStream.
     * <p>
     * The permission groovy.security.GroovyCodeSourcePermission will be used to determine if the given codeBase
     * may be specified.  That is, the current Policy set must have a GroovyCodeSourcePermission that implies
     * the codeBase, or an exception will be thrown.  This is to prevent callers from hijacking
     * existing codeBase policy entries unless explicitly authorized by the user.
     */
    public GroovyCodeSource(Reader reader, String name, String codeBase) {
        this.name = name;
        this.codeSource = createCodeSource(codeBase);

        try {
            this.scriptText = IOGroovyMethods.getText(reader);
        } catch (IOException e) {
            throw new RuntimeException("Impossible to read the text content from that reader, for script: " + name + " with codeBase: " + codeBase, e);
        }
    }

    public GroovyCodeSource(final File infile, final String encoding) throws IOException {
        // avoid files which confuse us like ones with .. in path
        final File file = new File(infile.getCanonicalPath());
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString() + " (" + file.getAbsolutePath() + ")");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file.toString() + " (" + file.getAbsolutePath() + ") is a directory not a Groovy source file.");
        }
        try {
            if (!file.canRead())
                throw new RuntimeException(file.toString() + " can not be read. Check the read permission of the file \"" + file.toString() + "\" (" + file.getAbsolutePath() + ").");
        }
        catch (SecurityException e) {
            throw e;
        }

        this.file = file;
        this.cachable = true;
        //The calls below require access to user.dir - allow here since getName() and getCodeSource() are
        //package private and used only by the GroovyClassLoader.
        try {
            Object[] info = AccessController.doPrivileged((PrivilegedExceptionAction<Object[]>) () -> {
                // retrieve the content of the file using the provided encoding
                if (encoding != null) {
                    scriptText = ResourceGroovyMethods.getText(infile, encoding);
                } else {
                    scriptText = ResourceGroovyMethods.getText(infile);
                }

                Object[] info1 = new Object[2];
                URL url = file.toURI().toURL();
                info1[0] = url.toExternalForm();
                //toURI().toURL() will encode, but toURL() will not.
                info1[1] = new CodeSource(url, (Certificate[]) null);
                return info1;
            });

            this.name = (String) info[0];
            this.codeSource = (CodeSource) info[1];
        } catch (PrivilegedActionException pae) {
            Throwable cause = pae.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new RuntimeException("Could not construct CodeSource for file: " + file, cause);
        }
    }

    /**
     * @param infile the file to create a GroovyCodeSource for.
     * @throws IOException if an issue arises opening and reading the file.
     */
    public GroovyCodeSource(final File infile) throws IOException {
        this(infile, CharsetToolkit.getDefaultSystemCharset().name());
    }

    public GroovyCodeSource(URI uri) throws IOException {
        this(uri.toURL());
    }

    public GroovyCodeSource(URL url) {
        if (url == null) {
            throw new RuntimeException("Could not construct a GroovyCodeSource from a null URL");
        }
        this.url = url;
        // TODO: GROOVY-6561: GroovyMain got the name this way: script.substring(script.lastIndexOf("/") + 1)
        this.name = url.toExternalForm();
        this.codeSource = new CodeSource(url, (java.security.cert.Certificate[]) null);
        try {
            String contentEncoding = getContentEncoding(url);
            if (contentEncoding != null) {
                this.scriptText = ResourceGroovyMethods.getText(url, contentEncoding);
            } else {
                this.scriptText = ResourceGroovyMethods.getText(url); // falls-back on default encoding
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible to read the text content from " + name, e);
        }
    }

    /**
     * TODO(jwagenleitner): remove or fix in future release
     *
     * According to the spec getContentEncoding() returns the Content-Encoding
     * HTTP Header which typically carries values such as 'gzip' or 'deflate'
     * and is not the character set encoding. For compatibility in 2.4.x,
     * this behavior is retained but should be removed or fixed (parse
     * charset from Content-Type header) in future releases.
     *
     * see GROOVY-8056 and https://github.com/apache/groovy/pull/500
     */
    private static String getContentEncoding(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        String encoding = urlConnection.getContentEncoding();
        try {
            IOGroovyMethods.closeQuietly(urlConnection.getInputStream());
        } catch (IOException ignore) {
            // For compatibility, ignore exceptions from getInputStream() call
        }
        return encoding;
    }

    public CodeSource getCodeSource() {
        return codeSource;
    }

    public String getScriptText() {
        return scriptText;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public URL getURL() {
        return url;
    }

    public void setCachable(boolean b) {
        cachable = b;
    }

    public boolean isCachable() {
        return cachable;
    }

    private static CodeSource createCodeSource(final String codeBase) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new GroovyCodeSourcePermission(codeBase));
        }
        try {
            return new CodeSource(new URL("file", "", codeBase), (java.security.cert.Certificate[]) null);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("A CodeSource file URL cannot be constructed from the supplied codeBase: " + codeBase);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroovyCodeSource that = (GroovyCodeSource) o;
        return Objects.equals(codeSource, that.codeSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeSource);
    }
}
