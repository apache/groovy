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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SimpleGroovyRootDoc extends SimpleGroovyDoc implements GroovyRootDoc {
    private final static Pattern EQUIVALENT_PACKAGE_IMPORT = Pattern.compile("[^/]+$");
    private static final GroovyClassDoc[] EMPTY_GROOVYCLASSDOC_ARRAY = new GroovyClassDoc[0];
    private static final GroovyPackageDoc[] EMPTY_GROOVYPACKAGEDOC_ARRAY = new GroovyPackageDoc[0];

    private final Map<String, GroovyPackageDoc> packageDocs;
    private List<GroovyPackageDoc> packageDocValues = null;
    private final Map<String, GroovyClassDoc> classDocs;
    private final Map<String, String> equivalentPackageImports;
    private List<GroovyClassDoc> classDocValues = null;
    private final Map<String, GroovyClassDoc> cachedResolvedClasses = new HashMap<>();
    private final ClassNamedCache classNamedCache;

    private String description = "";

    public SimpleGroovyRootDoc(String name) {
        super(name);
        packageDocs = new LinkedHashMap<>();
        classDocs = new LinkedHashMap<>();
        equivalentPackageImports = new HashMap<>();
        classNamedCache = new ClassNamedCache(classDocs);
    }

    public GroovyClassDoc classNamed(GroovyClassDoc groovyClassDoc, String name) {
        GroovyClassDoc doc = classDocs.get(name);
        if (doc != null) {
            return doc;
        }
        return classNamedCache.search(groovyClassDoc, name);
    }

    public GroovyClassDoc classNamedExact(String name) {
        return classDocs.get(name);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public String summary() {
        return SimpleGroovyDoc.calculateFirstSentence(description);
    }

    public GroovyClassDoc[] classes() {
        if (classDocValues == null) {
            classDocValues = new ArrayList<>(classDocs.values());
            Collections.sort(classDocValues);
        }
        return classDocValues.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    public String[][] options() {/*todo*/
        return null;
    }

    public GroovyPackageDoc packageNamed(String packageName) {
        return packageDocs.get(packageName);
    }

    public void putAllClasses(Map<String, GroovyClassDoc> classes) {
        classDocs.putAll(classes);
        classDocValues = null;
    }

    public void put(String packageName, GroovyPackageDoc packageDoc) {
        packageDocs.put(packageName, packageDoc);
        packageDocValues = null;
    }

    public GroovyClassDoc[] specifiedClasses() {/*todo*/
        return null;
    }

    public GroovyPackageDoc[] specifiedPackages() {
        if (packageDocValues == null) {
            packageDocValues = new ArrayList<>(packageDocs.values());
            Collections.sort(packageDocValues);
        }
        return packageDocValues.toArray(EMPTY_GROOVYPACKAGEDOC_ARRAY);
    }

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

    @Override
    public Map<String, GroovyClassDoc> getResolvedClasses() {
        return cachedResolvedClasses;
    }

    // GroovyDocErrorReporter interface
    public void printError(String arg0) {/*todo*/}

    //    public void printError(GroovySourcePosition arg0, String arg1) {/*todo*/}
    public void printNotice(String arg0) {/*todo*/}

    //    public void printNotice(GroovySourcePosition arg0, String arg1) {/*todo*/}
    public void printWarning(String arg0) {/*todo*/}
//    public void printWarning(GroovySourcePosition arg0, String arg1) {/*todo*/}

    public void resolve() {
        //resolve class names at the end of adding all files to the tree
        for (GroovyClassDoc groovyClassDoc : classDocs.values()) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) groovyClassDoc;
            classDoc.resolve(this);
        }

    }

    private static class ClassNamedCache {
        private final Map<String, GroovyClassDoc> classDocs;
        private final Map<Entry, GroovyClassDoc> store = new HashMap<>();

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
                        GroovyClassDoc value = entry.getValue();
                        return value;
                    }
                }
            }
            return null;
        }

        private static class Entry {
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

                if (groovyClass != null ? !groovyClass.equals(entry.groovyClass) : entry.groovyClass != null)
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
