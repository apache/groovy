/*
 * Copyright (c) 2002-2021, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.apache.groovy.groovysh.jline;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageHelper {
    private enum ClassesToScan {
        ALL,
        PACKAGE_ALL,
        PACKAGE_CLASS
    }

    private enum ClassOutput {
        NAME,
        CLASS,
        MIXED
    }

    /**
     * Private helper method
     *
     * @param directory   The directory to start with
     * @param packageName The package name to search for. Will be needed for getting the Class object.
     * @param classes     if a file isn't loaded but still is in the directory
     * @param scan        determinate which classes will be added
     */
    private static void checkDirectory(
            File directory,
            String packageName,
            List<Object> classes,
            ClassesToScan scan,
            ClassOutput outType,
            Function<String, Class<?>> classResolver) {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files != null ? files : new String[0]) {
                if (file.endsWith(".class")) {
                    String className = packageName + '.' + file.substring(0, file.length() - 6);
                    if (outType != ClassOutput.CLASS) {
                        classes.add(className);
                    } else {
                        addClass(className, classes, classResolver);
                    }
                } else if (scan == ClassesToScan.ALL && (tmpDirectory = new File(directory, file)).isDirectory()) {
                    checkDirectory(
                            tmpDirectory,
                            packageName + "." + file,
                            classes,
                            ClassesToScan.ALL,
                            ClassOutput.NAME,
                            classResolver);
                }
            }
        }
    }

    /**
     * Private helper method.
     *
     * @param jarFile     the jar file
     * @param packageName the package name to search for
     * @param classes     the current ArrayList of all classes. This method will simply add new classes.
     * @param scan        determinate which classes will be added
     * @throws IOException if it can't correctly read from the jar file.
     */
    private static void checkJarFile(
            final JarFile jarFile,
            String packageName,
            List<Object> classes,
            ClassesToScan scan,
            ClassOutput outType,
            Function<String, Class<?>> classResolver)
            throws IOException {
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null); ) {
            name = jarEntry.getName();
            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');
                if (scan != ClassesToScan.ALL) {
                    String namepckg = name.substring(0, name.lastIndexOf("."));
                    if (packageName.equals(namepckg)
                            && ((scan == ClassesToScan.PACKAGE_CLASS && !name.contains("$"))
                                    || scan == ClassesToScan.PACKAGE_ALL)) {
                        if (outType == ClassOutput.CLASS) {
                            addClass(name, classes, classResolver);
                        } else {
                            classes.add(name);
                        }
                    }
                } else if (name.contains(packageName)) {
                    if (outType == ClassOutput.CLASS
                            || (outType == ClassOutput.MIXED
                                    && Character.isUpperCase(name.charAt(packageName.length() + 1)))) {
                        addClass(name, classes, classResolver);
                    } else {
                        classes.add(name);
                    }
                }
            }
        }
    }

    private static void addClass(String className, List<Object> classes, Function<String, Class<?>> classResolver) {
        if (classResolver != null) {
            Class<?> clazz = classResolver.apply(className);
            if (clazz != null) {
                classes.add(clazz);
            }
        } else {
            classes.add(className);
        }
    }

    private static Class<?> classResolver(String name) {
        Class<?> out = null;
        try {
            out = Class.forName(name);
        } catch (Exception | Error ignore) {

        }
        return out;
    }

    private static class PackageNameParser {
        private final String packageName;
        private final ClassesToScan classesToScan;
        private final ClassOutput outType;

        public PackageNameParser(String packageName) {
            if (packageName.endsWith(".*")) {
                classesToScan = ClassesToScan.PACKAGE_CLASS;
                outType = ClassOutput.CLASS;
                this.packageName = packageName.substring(0, packageName.length() - 2);
            } else if (packageName.endsWith(".**")) {
                this.packageName = packageName.substring(0, packageName.length() - 3);
                classesToScan = ClassesToScan.PACKAGE_ALL;
                outType = ClassOutput.CLASS;
            } else {
                classesToScan = ClassesToScan.ALL;
                this.packageName = packageName;
                outType = ClassOutput.MIXED;
            }
        }

        public String packageName() {
            return packageName;
        }

        public ClassesToScan classesToScan() {
            return classesToScan;
        }

        public ClassOutput outType() {
            return outType;
        }
    }

    private static Enumeration<URL> toEnumeration(final URL[] urls) {
        return (new Enumeration<URL>() {
            final int size = urls.length;

            int cursor;

            public boolean hasMoreElements() {
                return (cursor < size);
            }

            public URL nextElement() {
                return urls[cursor++];
            }
        });
    }

    private static Enumeration<URL> getResources(final ClassLoader classLoader, String packageName)
            throws ClassNotFoundException {
        try {
            return classLoader.getResources(packageName.replace('.', '/'));
        } catch (final NullPointerException ex) {
            throw new ClassNotFoundException(
                    packageName + " does not appear to be a valid package (Null pointer exception)", ex);
        } catch (final IOException ioex) {
            throw new ClassNotFoundException(
                    "IOException was thrown when trying to get all resources for " + packageName, ioex);
        }
    }

    /**
     * Attempts to list all the class names in the specified package as determined
     * by the Groovy class loader classpath
     *
     * @param packageName the package name to search
     * @param classLoader class loader
     * @return a list of class names that exist within that package
     */
    @SuppressWarnings("unchecked")
    public static List<String> getClassNamesForPackage(String packageName, ClassLoader classLoader) {
        try {
            PackageNameParser pnp = new PackageNameParser(packageName);
            Enumeration<URL> resources = getResources(classLoader, pnp.packageName());
            return (List<String>) (Object)
                    getClassesForPackage(pnp.packageName(), resources, pnp.classesToScan(), ClassOutput.NAME, null);
        } catch (Exception ignore) {
        }
        return new ArrayList<>();
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the Groovy class loader classpath
     *
     * @param packageName   the package name to search
     * @param classLoader   Groovy class loader
     * @param classResolver resolve class from class name
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException if something went wrong
     */
    public static List<Object> getClassesForPackage(
            String packageName, GroovyClassLoader classLoader, Function<String, Class<?>> classResolver)
            throws ClassNotFoundException {
        PackageNameParser pnp = new PackageNameParser(packageName);
        Enumeration<URL> resources = toEnumeration(classLoader.getURLs());
        return getClassesForPackage(pnp.packageName(), resources, pnp.classesToScan(), pnp.outType(), classResolver);
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @param packageName the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException if something went wrong
     */
    public static List<Object> getClassesForPackage(String packageName) throws ClassNotFoundException {
        final ClassLoader cld = Thread.currentThread().getContextClassLoader();
        if (cld == null) {
            throw new ClassNotFoundException("Can't get class loader.");
        }
        PackageNameParser pnp = new PackageNameParser(packageName);
        Enumeration<URL> resources = getResources(cld, pnp.packageName());
        return getClassesForPackage(
                pnp.packageName(), resources, pnp.classesToScan(), pnp.outType(), PackageHelper::classResolver);
    }

    private static List<Object> getClassesForPackage(
            String packageName,
            final Enumeration<URL> resources,
            ClassesToScan scan,
            ClassOutput outType,
            Function<String, Class<?>> classResolver)
            throws ClassNotFoundException {
        List<Object> classes = new ArrayList<>();
        URLConnection connection;

        for (URL url; resources.hasMoreElements() && ((url = resources.nextElement()) != null); ) {
            try {
                connection = url.openConnection();

                if (connection instanceof JarURLConnection) {
                    checkJarFile(
                            ((JarURLConnection) connection).getJarFile(),
                            packageName,
                            classes,
                            scan,
                            outType,
                            classResolver);
                } else if (connection
                        .getClass()
                        .getCanonicalName()
                        .equals("sun.net.www.protocol.file.FileURLConnection")) {
                    try {
                        File file = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
                        if (file.exists()) {
                            if (file.isDirectory()) {
                                checkDirectory(file, packageName, classes, scan, outType, classResolver);
                            } else if (file.getName().endsWith(".jar")) {
                                checkJarFile(new JarFile(file), packageName, classes, scan, outType, classResolver);
                            }
                        }
                    } catch (final UnsupportedEncodingException ex) {
                        throw new ClassNotFoundException(
                                packageName + " does not appear to be a valid package (Unsupported encoding)", ex);
                    }
                } else {
                    throw new ClassNotFoundException(
                            packageName + " (" + url.getPath() + ") does not appear to be a valid package");
                }
            } catch (final IOException ioex) {
                throw new ClassNotFoundException(
                        "IOException was thrown when trying to get all resources for " + packageName, ioex);
            }
        }
        return classes;
    }
}
