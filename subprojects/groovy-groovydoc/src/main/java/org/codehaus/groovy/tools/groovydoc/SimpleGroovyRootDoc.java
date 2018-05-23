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

    private final Map<String, GroovyPackageDoc> packageDocs;
    private List<GroovyPackageDoc> packageDocValues = null;
    private final Map<String, GroovyClassDoc> classDocs;
    private final Map<String, String> equivalentPackageImports;
    private List<GroovyClassDoc> classDocValues = null;
    private final Map<String, GroovyClassDoc> cachedResolvedClasses = new HashMap<String, GroovyClassDoc>();

    private String description = "";

    public SimpleGroovyRootDoc(String name) {
        super(name);
        packageDocs = new LinkedHashMap<String, GroovyPackageDoc>();
        classDocs = new LinkedHashMap<String, GroovyClassDoc>();
        equivalentPackageImports = new HashMap<String, String>();
    }

    public GroovyClassDoc classNamed(GroovyClassDoc groovyClassDoc, String name) {
        GroovyClassDoc doc = classDocs.get(name);
        if (doc != null) {
            return doc;
        }
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
            classDocValues = new ArrayList<GroovyClassDoc>(classDocs.values());
            Collections.sort(classDocValues);
        }
        return classDocValues.toArray(new GroovyClassDoc[0]);
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
            packageDocValues = new ArrayList<GroovyPackageDoc>(packageDocs.values());
            Collections.sort(packageDocValues);
        }
        return packageDocValues.toArray(new GroovyPackageDoc[0]);
    }

    public Map<String, GroovyClassDoc> getVisibleClasses(List importedClassesAndPackages) {
        Map<String, GroovyClassDoc> visibleClasses = new LinkedHashMap<String, GroovyClassDoc>();
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

}
