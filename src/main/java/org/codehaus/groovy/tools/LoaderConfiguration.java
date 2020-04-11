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
package org.codehaus.groovy.tools;

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used to configure a RootLoader from a stream or by using
 * its methods.
 * <p>
 * The stream can be for example a FileInputStream from a file with
 * the following format:
 * <p>
 * <pre>
 * # comment
 * main is classname
 * load path
 * load file
 * load pathWith${property}
 * load pathWith!{required.property}
 * load path/*.jar
 * load path/&#42;&#42;/&#42;.jar
 * </pre>
 * <ul>
 * <li>All lines starting with "#" are ignored.</li>
 * <li>The "main is" part may only be once in the file. The String
 * afterwards is the name of a class with a main method. </li>
 * <li>The "load" command will add the given file or path to the
 * classpath in this configuration object. If the path does not
 * exist, the path will be ignored.
 * </li>
 * <li>properties referenced using !{x} are required.</li>
 * <li>properties referenced using ${x} are not required. If the
 * property does not exist the whole load instruction line will
 * be ignored.</li>
 * <li>* is used to match zero or more characters in a file.</li>
 * <li>** is used to match zero or more directories.</li>
 * <li>Loading paths with <code>load ./*.jar</code> or <code>load *.jar</code> are not supported.</li>
 * </ul>
 * <p>
 * Defining the main class is required unless setRequireMain(boolean) is
 * called with false, before reading the configuration.
 * You can use the wildcard "*" to filter the path, but only for files, not
 * directories. To match directories use "**". The ${propertyname} is replaced by the value of the system's
 * property name. You can use user.home here for example. If the property does
 * not exist, an empty string will be used. If the path or file after the load
 * command does not exist, the path will be ignored.
 *
 * @see RootLoader
 */
public class LoaderConfiguration {

    private static final String MAIN_PREFIX = "main is", LOAD_PREFIX = "load", GRAB_PREFIX = "grab", PROP_PREFIX = "property", CONFIGSCRIPT_PREFIX = "configscript";
    private final List<URL> classPath = new ArrayList<URL>();
    private String main;
    private boolean requireMain;
    private static final char WILDCARD = '*';
    private static final String ALL_WILDCARD = "" + WILDCARD + WILDCARD;
    private static final String MATCH_FILE_NAME = "\\\\E[^/]+?\\\\Q";
    private static final String MATCH_ALL = "\\\\E.+?\\\\Q";
    private final List<String> grabList = new ArrayList<String>();
    private final List<String> configScripts = new ArrayList<String>();

    /**
     * creates a new loader configuration
     */
    public LoaderConfiguration() {
        this.requireMain = true;
    }

    /**
     * configures this loader with a stream
     *
     * @param is stream used to read the configuration
     * @throws IOException if reading or parsing the contents of the stream fails
     */
    public void configure(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            int lineNumber = 0;

            while (true) {
                String line = reader.readLine();
                if (line == null) break;

                line = line.trim();
                lineNumber++;

                if (line.startsWith("#") || line.length() == 0) continue;

                if (line.startsWith(LOAD_PREFIX)) {
                    String loadPath = line.substring(LOAD_PREFIX.length()).trim();
                    loadPath = assignProperties(loadPath);
                    loadFilteredPath(loadPath);
                } else if (line.startsWith(GRAB_PREFIX)) {
                    String grabParams = line.substring(GRAB_PREFIX.length()).trim();
                    grabList.add(assignProperties(grabParams));
                } else if (line.startsWith(MAIN_PREFIX)) {
                    if (main != null)
                        throw new IOException("duplicate definition of main in line " + lineNumber + " : " + line);
                    main = line.substring(MAIN_PREFIX.length()).trim();
                } else if (line.startsWith(PROP_PREFIX)) {
                    String params = line.substring(PROP_PREFIX.length()).trim();
                    String key = SystemUtil.setSystemPropertyFrom(params);
                    System.setProperty(key, assignProperties(System.getProperty(key)));
                } else if (line.startsWith(CONFIGSCRIPT_PREFIX)) {
                    String script = line.substring(CONFIGSCRIPT_PREFIX.length()).trim();
                    configScripts.add(assignProperties(script));
                } else {
                    throw new IOException("unexpected line in " + lineNumber + " : " + line);
                }
            }
        }

        if (requireMain && main == null) throw new IOException("missing main class definition in config file");
        if (!configScripts.isEmpty()) {
            System.setProperty("groovy.starter.configscripts", DefaultGroovyMethods.join((Iterable)configScripts, ","));
        }
    }

    /*
    * Expands the properties inside the given string to it's values.
    */
    private static String assignProperties(String str) {
        int propertyIndexStart = 0, propertyIndexEnd = 0;
        boolean requireProperty;
        StringBuilder result = new StringBuilder();

        while (propertyIndexStart < str.length()) {
            {
                int i1 = str.indexOf("${", propertyIndexStart);
                int i2 = str.indexOf("!{", propertyIndexStart);
                if (i1 == -1) {
                    propertyIndexStart = i2;
                } else if (i2 == -1) {
                    propertyIndexStart = i1;
                } else {
                    propertyIndexStart = Math.min(i1, i2);
                }
                requireProperty = propertyIndexStart == i2;
            }
            if (propertyIndexStart == -1) break;
            result.append(str, propertyIndexEnd, propertyIndexStart);

            propertyIndexEnd = str.indexOf('}', propertyIndexStart);
            if (propertyIndexEnd == -1) break;

            String propertyKey = str.substring(propertyIndexStart + 2, propertyIndexEnd);
            String propertyValue = System.getProperty(propertyKey);
            // assume properties contain paths
            if (propertyValue == null) {
                if (requireProperty) {
                    throw new IllegalArgumentException("Variable " + propertyKey + " in groovy-starter.conf references a non-existent System property! Try passing the property to the VM using -D" + propertyKey + "=myValue in JAVA_OPTS");
                } else {
                    return null;
                }
            }
            propertyValue = getSlashyPath(propertyValue);
            propertyValue = correctDoubleSlash(propertyValue, propertyIndexEnd, str);
            result.append(propertyValue);

            propertyIndexEnd++;
            propertyIndexStart = propertyIndexEnd;
        }

        if (propertyIndexStart == -1 || propertyIndexStart >= str.length()) {
            result.append(str, propertyIndexEnd, str.length());
        } else if (propertyIndexEnd == -1) {
            result.append(str, propertyIndexStart, str.length());
        }

        return result.toString();
    }


    private static String correctDoubleSlash(String propertyValue, int propertyIndexEnd, String str) {
        int index = propertyIndexEnd + 1;
        if (index < str.length() && str.charAt(index) == '/' &&
                propertyValue.endsWith("/") &&
                propertyValue.length() > 0) {
            propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
        }
        return propertyValue;
    }

    /*
     * Load a possibly filtered path. Filters are defined
     * by using the * wildcard like in any shell.
     */
    private void loadFilteredPath(String filter) {
        if (filter == null) return;
        filter = getSlashyPath(filter);
        int starIndex = filter.indexOf(WILDCARD);
        if (starIndex == -1) {
            addFile(new File(filter));
            return;
        }
        boolean recursive = filter.contains(ALL_WILDCARD);

        if (filter.lastIndexOf('/') < starIndex) {
            starIndex = filter.lastIndexOf('/') + 1;
        }
        String startDir = filter.substring(0, starIndex - 1);
        File root = new File(startDir);

        filter = Pattern.quote(filter);
        filter = filter.replaceAll("\\" + WILDCARD + "\\" + WILDCARD, MATCH_ALL);
        filter = filter.replaceAll("\\" + WILDCARD, MATCH_FILE_NAME);
        Pattern pattern = Pattern.compile(filter);

        final File[] files = root.listFiles();
        if (files != null) {
            findMatchingFiles(files, pattern, recursive);
        }
    }

    private void findMatchingFiles(File[] files, Pattern pattern, boolean recursive) {
        for (File file : files) {
            String fileString = getSlashyPath(file.getPath());
            Matcher m = pattern.matcher(fileString);
            if (m.matches() && file.isFile()) {
                addFile(file);
            }
            if (file.isDirectory() && recursive) {
                final File[] dirFiles = file.listFiles();
                if (dirFiles != null) {
                    findMatchingFiles(dirFiles, pattern, true);
                }
            }
        }
    }

    // change path representation to something more system independent.
    // This solution is based on an absolute path
    private static String getSlashyPath(final String path) {
        String changedPath = path;
        if (File.separatorChar != '/')
            changedPath = changedPath.replace(File.separatorChar, '/');

        return changedPath;
    }

    /**
     * Adds a file to the classpath if it exists.
     *
     * @param file the file to add
     */
    public void addFile(File file) {
        if (file != null && file.exists()) {
            try {
                classPath.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                throw new AssertionError("converting an existing file to an url should have never thrown an exception!");
            }
        }
    }

    /**
     * Adds a file to the classpath if it exists.
     *
     * @param filename the name of the file to add
     */
    public void addFile(String filename) {
        if (filename != null) addFile(new File(filename));
    }

    /**
     * Adds a classpath to this configuration. It expects a string with
     * multiple paths, separated by the system dependent path separator.
     * Expands wildcards, e.g. dir/* into all the jars in dir.
     *
     * @param path the path as a path separator delimited string
     * @see java.io.File#pathSeparator
     */
    public void addClassPath(String path) {
        String[] paths = path.split(File.pathSeparator);
        for (String cpPath : paths) {
            // Check to support wild card classpath
            if (cpPath.endsWith("*")) {
                File dir = new File(cpPath.substring(0, cpPath.length() - 1));
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".jar")) addFile(file);
                    }
                }
            } else {
                addFile(new File(cpPath));
            }
        }
    }

    /**
     * The classpath as URL[] from this configuration.
     * This can be used to construct a class loader.
     *
     * @return the classpath
     * @see java.net.URLClassLoader
     */
    public URL[] getClassPathUrls() {
        return classPath.toArray(new URL[0]);
    }

    /**
     * The extra grab configuration.
     *
     * @return the list of grab urls
     */
    public List<String> getGrabUrls() {
        return grabList;
    }

    /**
     * Returns the name of the main class for this configuration.
     *
     * @return the name of the main class or null if not defined
     */
    public String getMainClass() {
        return main;
    }

    /**
     * Sets the main class. If there is already a main class
     * it is overwritten. Calling {@link #configure(InputStream)}
     * after calling this method does not require a main class
     * definition inside the stream.
     *
     * @param classname the name to become the main class
     */
    public void setMainClass(String classname) {
        main = classname;
        requireMain = false;
    }

    /**
     * Determines if a main class is required when calling.
     *
     * @param requireMain set to false if no main class is required
     * @see #configure(InputStream)
     */
    public void setRequireMain(boolean requireMain) {
        this.requireMain = requireMain;
    }

}
