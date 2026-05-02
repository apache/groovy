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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Default {@link GroovyRootDoc} implementation used to collect packages, classes, and lookup caches.
 */
public class SimpleGroovyRootDoc extends SimpleGroovyDoc implements GroovyRootDoc {
    private static final Pattern EQUIVALENT_PACKAGE_IMPORT = Pattern.compile("[^/]+$");
    private static final GroovyClassDoc[] EMPTY_GROOVYCLASSDOC_ARRAY = new GroovyClassDoc[0];
    private static final GroovyPackageDoc[] EMPTY_GROOVYPACKAGEDOC_ARRAY = new GroovyPackageDoc[0];

    private final Map<String, GroovyPackageDoc> packageDocs;
    private List<GroovyPackageDoc> packageDocValues = null;
    private final Map<String, GroovyClassDoc> classDocs;
    private final Map<String, String> equivalentPackageImports;
    private List<GroovyClassDoc> classDocValues = null;
    private final Map<String, GroovyClassDoc> cachedResolvedClasses = new LinkedHashMap<>();
    private final ClassNamedCache classNamedCache;

    private String description = "";
    /** GROOVY-11938: snippet-file resolution needs access to source dirs at render time. */
    private String[] sourcepaths = new String[0];

    /**
     * Creates a root documentation model with the supplied name.
     *
     * @param name the root document name
     */
    public SimpleGroovyRootDoc(String name) {
        super(name);
        packageDocs = new LinkedHashMap<>();
        classDocs = new LinkedHashMap<>();
        equivalentPackageImports = new LinkedHashMap<>();
        classNamedCache = new ClassNamedCache(classDocs);
    }

    /**
     * Returns the source paths used to build this root document.
     *
     * @return the configured source paths
     *
     * @since 6.0.0
     */
    public String[] getSourcepaths() {
        return sourcepaths;
    }

    /**
     * Stores the source paths used to build this root document.
     *
     * @param sourcepaths the source paths to retain
     *
     * @since 6.0.0
     */
    public void setSourcepaths(String[] sourcepaths) {
        this.sourcepaths = sourcepaths == null ? new String[0] : java.util.Arrays.copyOf(sourcepaths, sourcepaths.length);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc classNamed(GroovyClassDoc groovyClassDoc, String name) {
        GroovyClassDoc doc = classDocs.get(name);
        if (doc != null) {
            return doc;
        }
        return classNamedCache.search(groovyClassDoc, name);
    }

    /**
     * Resolves a class using its exact internal name.
     *
     * @param name the exact class name to resolve
     * @return the matching class documentation, or {@code null} if none exists
     */
    public GroovyClassDoc classNamedExact(String name) {
        return classDocs.get(name);
    }

    /**
     * Sets the root description text.
     *
     * @param description the root description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the root description text.
     *
     * @return the configured description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the first-sentence summary derived from {@link #description()}.
     *
     * @return the first-sentence summary
     */
    public String summary() {
        return SimpleGroovyDoc.calculateFirstSentence(description);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] classes() {
        if (classDocValues == null) {
            classDocValues = new ArrayList<>(classDocs.values());
            Collections.sort(classDocValues);
        }
        return classDocValues.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public String[][] options() {/*todo*/
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyPackageDoc packageNamed(String packageName) {
        return packageDocs.get(packageName);
    }

    /**
     * Adds all supplied classes to this root document.
     *
     * @param classes the classes to add
     */
    public void putAllClasses(Map<String, GroovyClassDoc> classes) {
        classDocs.putAll(classes);
        classDocValues = null;
    }

    /**
     * Adds a package to this root document.
     *
     * @param packageName the package name key
     * @param packageDoc the package documentation object
     */
    public void put(String packageName, GroovyPackageDoc packageDoc) {
        packageDocs.put(packageName, packageDoc);
        packageDocValues = null;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] specifiedClasses() {/*todo*/
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyPackageDoc[] specifiedPackages() {
        if (packageDocValues == null) {
            packageDocValues = new ArrayList<>(packageDocs.values());
            Collections.sort(packageDocValues);
        }
        return packageDocValues.toArray(EMPTY_GROOVYPACKAGEDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, GroovyClassDoc> getVisibleClasses(List importedClassesAndPackages) {
        Map<String, GroovyClassDoc> visibleClasses = new LinkedHashMap<>();
        for (Map.Entry<String, GroovyClassDoc> entry : classDocs.entrySet()) {
            String fullClassName = entry.getKey();
            String equivalentPackageImport = findEquivalentPackageImport(fullClassName);
            if (importedClassesAndPackages.contains(fullClassName) ||
                    importedClassesAndPackages.contains(equivalentPackageImport)) {
                GroovyClassDoc classDoc = entry.getValue();
                visibleClasses.put(classDoc.name(), classDoc);
            }
        }
        return visibleClasses;
    }

    private String findEquivalentPackageImport(String fullClassName) {
        String eq = equivalentPackageImports.get(fullClassName);
        if (eq == null) {
            eq = EQUIVALENT_PACKAGE_IMPORT.matcher(fullClassName).replaceAll("*");
            equivalentPackageImports.put(fullClassName, eq);
        }
        return eq;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, GroovyClassDoc> getResolvedClasses() {
        return cachedResolvedClasses;
    }

    // GroovyDocErrorReporter interface
    /** {@inheritDoc} */
    @Override
    public void printError(String arg0) {/*todo*/}

    //    public void printError(GroovySourcePosition arg0, String arg1) {/*todo*/}
    /** {@inheritDoc} */
    @Override
    public void printNotice(String arg0) {/*todo*/}

    //    public void printNotice(GroovySourcePosition arg0, String arg1) {/*todo*/}
    /** {@inheritDoc} */
    @Override
    public void printWarning(String arg0) {/*todo*/}
//    public void printWarning(GroovySourcePosition arg0, String arg1) {/*todo*/}

    /**
     * Resolves deferred type references across all collected classes.
     */
    public void resolve() {
        //resolve class names at the end of adding all files to the tree
        for (GroovyClassDoc groovyClassDoc : classDocs.values()) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) groovyClassDoc;
            classDoc.resolve(this);
        }

    }

    private static final class ClassNamedCache {
        private final Map<String, GroovyClassDoc> classDocs;
        private final Map<Entry, GroovyClassDoc> store = new LinkedHashMap<>();

        private ClassNamedCache(final Map<String, GroovyClassDoc> classDocs) {
            this.classDocs = classDocs;
        }

        public GroovyClassDoc search(GroovyClassDoc groovyClassDoc, String name) {
            Entry entry = new Entry(groovyClassDoc, name);
            GroovyClassDoc result = store.get(entry);
            if (result == null) {
                if (store.containsKey(entry)) {
                    return null;
                }
                result = performLookup(groovyClassDoc, name);
                store.put(entry, result);
            }
            return result;
        }

        private GroovyClassDoc performLookup(GroovyClassDoc groovyClassDoc, String name) {
            // look for full match or match excluding package
            String fullPathName = groovyClassDoc != null ? groovyClassDoc.getFullPathName() : null;
            boolean hasPackage = (fullPathName != null && fullPathName.lastIndexOf('/') > 0);
            if (hasPackage) {
                fullPathName = fullPathName.substring(0, fullPathName.lastIndexOf('/'));
            }

            for (Map.Entry<String, GroovyClassDoc> entry : classDocs.entrySet()) {
                String key = entry.getKey();
                int lastSlashIdx = key.lastIndexOf('/');
                if (lastSlashIdx > 0) {
                    String shortKey = key.substring(lastSlashIdx + 1);
                    if (shortKey.equals(name) && (!hasPackage || key.startsWith(fullPathName))) {
                        return entry.getValue();
                    }
                }
            }
            return null;
        }

        private static final class Entry {
            private final GroovyClassDoc groovyClass;
            private final String name;
            private final int hashCode;

            private Entry(final GroovyClassDoc groovyClass, final String name) {
                this.groovyClass = groovyClass;
                this.name = name;
                this.hashCode = computeHash();
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                final Entry entry = (Entry) o;

                if (!Objects.equals(groovyClass, entry.groovyClass))
                    return false;
                return name.equals(entry.name);
            }

            private int computeHash() {
                int result = groovyClass != null ? groovyClass.hashCode() : 0;
                result = 31 * result + name.hashCode();
                return result;
            }

            @Override
            public int hashCode() {
                return hashCode;
            }
        }
    }

}
