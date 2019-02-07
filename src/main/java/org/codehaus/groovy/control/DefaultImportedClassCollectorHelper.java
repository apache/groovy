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
package org.codehaus.groovy.control;

import groovy.lang.GroovySystem;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Represents the helper for DefaultImportedClassCollector
 *
 * Note: `DefaultImportedClassCollector` relies on the 3rd-party lib ClassGraph, as a result, it is not always able to be loaded,
 *        so the class is necessary to keep common info and provide util methods
 *
 * @since 3.0.0
 */
public class DefaultImportedClassCollectorHelper {
    public static final String[] DEFAULT_IMPORTS = { "java.lang.", "java.util.", "java.io.", "java.net.", "groovy.lang.", "groovy.util." };
    private static final String DEFAULTIMPORTEDCLASSCOLLECTOR_CLASSNAME = "org.codehaus.groovy.control.DefaultImportedClassCollector";
    private static final Logger LOGGER = Logger.getLogger(DefaultImportedClassCollectorHelper.class.getName());

    public static Map<String, Set<String>> getClassNameToPackageMap() {
        Map<String, Set<String>> result = null;
        File classesInfoFile = LazyInfoHolder.CLASSES_INFO_FILE;
        boolean exists = classesInfoFile.exists();
        boolean same = false;
        boolean errOccurred = false;
        String javaVersion = LazyInfoHolder.JAVA_VERSION;
        String groovyVersion = LazyInfoHolder.GROOVY_VERSION;

        if (exists) {
            try (ObjectInputStream ois =
                         new ObjectInputStream(
                                 new GZIPInputStream(
                                         new BufferedInputStream(
                                                 new FileInputStream(classesInfoFile))))) {
                DefaultImportedClassesInfo classesInfo = (DefaultImportedClassesInfo) ois.readObject();

                same = Objects.equals(javaVersion, classesInfo.getJavaVersion())
                        && Objects.equals(groovyVersion, classesInfo.getGroovyVersion())
                        && Arrays.equals(DEFAULT_IMPORTS, classesInfo.getPackageNames());

                result = classesInfo.getClassNameToPackageMap();
            } catch (Exception e) {
                LOGGER.warning("Failed to read " + classesInfoFile.getAbsolutePath() + ".\n" + DefaultGroovyMethods.asString(e));
                errOccurred = true;
            }
        }

        if (null == result || !same) {
            result = doGetClassNameToPackageMap();
        }

        if ((null != result) && (!exists || !same)) {
            File cachesDir = classesInfoFile.getParentFile();
            if (!cachesDir.exists()) {
                cachesDir.mkdirs();
            }

            try (ObjectOutputStream oos =
                         new ObjectOutputStream(
                                 new GZIPOutputStream(
                                         new BufferedOutputStream(
                                                 new FileOutputStream(classesInfoFile))))) {
                DefaultImportedClassesInfo classesInfo = new DefaultImportedClassesInfo(javaVersion, groovyVersion, DEFAULT_IMPORTS, result);
                oos.writeObject(classesInfo);
            } catch (Exception e) {
                LOGGER.warning("Failed to generate " + classesInfoFile.getAbsolutePath() + ".\n" + DefaultGroovyMethods.asString(e));
                errOccurred = true;
            }
        }

        if (errOccurred) {
            classesInfoFile.delete();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Set<String>> doGetClassNameToPackageMap() {
        Map<String, Set<String>> result = null;
        try {
            Class defaultImportedClassCollectorClazz = Class.forName(DEFAULTIMPORTEDCLASSCOLLECTOR_CLASSNAME);

            Object defaultImportedClassCollector = defaultImportedClassCollectorClazz.getField("INSTANCE").get(null);
            result = (Map<String, Set<String>>) defaultImportedClassCollectorClazz.getDeclaredMethod("getClassNameToPackageMap").invoke(defaultImportedClassCollector);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOGGER.severe("Runtime dependency classgraph(io.github.classgraph:classgraph) not found.\n" + DefaultGroovyMethods.asString(e));
        } catch (Throwable t) {
            LOGGER.severe("Failed to collect default imported class info.\n" + DefaultGroovyMethods.asString(t));
        }

        return result;
    }

    private static class DefaultImportedClassesInfo implements Serializable {
        private static final long serialVersionUID = 9017949572933849622L;
        private String javaVersion;
        private String groovyVersion;
        private String[] packageNames;
        private Map<String, Set<String>> classNameToPackageMap;

        public DefaultImportedClassesInfo(String javaVersion, String groovyVersion, String[] packageNames, Map<String, Set<String>> classNameToPackageMap) {
            this.javaVersion = javaVersion;
            this.groovyVersion = groovyVersion;
            this.packageNames = packageNames;
            this.classNameToPackageMap = classNameToPackageMap;
        }

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }

        public String getJavaVersion() {
            return javaVersion;
        }

        public void setJavaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
        }

        public String getGroovyVersion() {
            return groovyVersion;
        }

        public void setGroovyVersion(String groovyVersion) {
            this.groovyVersion = groovyVersion;
        }

        public String[] getPackageNames() {
            return packageNames;
        }

        public void setPackageNames(String[] packageNames) {
            this.packageNames = packageNames;
        }

        public Map<String, Set<String>> getClassNameToPackageMap() {
            return classNameToPackageMap;
        }

        public void setClassNameToPackageMap(Map<String, Set<String>> classNameToPackageMap) {
            this.classNameToPackageMap = classNameToPackageMap;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefaultImportedClassesInfo)) return false;
            DefaultImportedClassesInfo that = (DefaultImportedClassesInfo) o;
            return Objects.equals(javaVersion, that.javaVersion) &&
                    Objects.equals(groovyVersion, that.groovyVersion) &&
                    Arrays.equals(packageNames, that.packageNames);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(javaVersion, groovyVersion);
            result = 31 * result + Arrays.hashCode(packageNames);
            return result;
        }

        @Override
        public String toString() {
            return "DefaultImportedClassesInfo{" +
                    "javaVersion='" + javaVersion + '\'' +
                    ", groovyVersion='" + groovyVersion + '\'' +
                    ", packageNames=" + Arrays.toString(packageNames) +
                    ", classNameToPackageMap=" + classNameToPackageMap +
                    '}';
        }
    }

    private static class LazyInfoHolder {
        private static final String SNAPSHOT = "SNAPSHOT";
        private static final File CLASSES_INFO_FILE = new File(new File(GroovySystem.getGroovyRoot(), "caches"), "default_imported_classes");
        private static final String JAVA_VERSION = System.getProperty("java.version");
        private static final String GROOVY_VERSION = getGroovyVersion();

        private static String getGroovyVersion() {
            String version = GroovySystem.getVersion();
            version = (null == version || version.trim().isEmpty()) ? SNAPSHOT : version;

            // if using SNAPSHOT version(e.g. 3.0.0-SNAPSHOT), cache for only 1 hour because default imported classes may change when developing
            return version.contains(SNAPSHOT) ? version + new SimpleDateFormat("-yyyyMMdd.HH").format(new Date()) : version;
        }
    }

    public static void main(String[] args) {
        getClassNameToPackageMap().entrySet().forEach(System.out::println);
    }
}
