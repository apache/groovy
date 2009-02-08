/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleGroovyRootDoc extends SimpleGroovyDoc implements GroovyRootDoc {
    private Map<String, GroovyPackageDoc> packageDocs;
    private Map<String, GroovyClassDoc> classDocs;
    private String description = "";

    public SimpleGroovyRootDoc(String name) {
        super(name);
        packageDocs = new HashMap<String, GroovyPackageDoc>();
        classDocs = new HashMap<String, GroovyClassDoc>();
    }

    // todo - take better account of package names !
    public GroovyClassDoc classNamed(String name) {
        for (String key : classDocs.keySet()) {
            if (key.equals(name)) return classDocs.get(key);
            int lastSlashIdx = key.lastIndexOf('/');
            if (lastSlashIdx > 0) {
                String shortKey = key.substring(lastSlashIdx + 1);
                if (shortKey.equals(name)) {
                    return classDocs.get(key);
                }
            }
        }
        return null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public GroovyClassDoc[] classes() {
        List<GroovyClassDoc> classDocValues = new ArrayList<GroovyClassDoc>(classDocs.values());
        Collections.sort(classDocValues); // todo - performance / maybe move into a sortMe() method
        return classDocValues.toArray(new GroovyClassDoc[classDocValues.size()]);
    }

    public String[][] options() {/*todo*/
        return null;
    }

    public GroovyPackageDoc packageNamed(String packageName) {
        return packageDocs.get(packageName);
    }

    public void putAllClasses(Map<String, GroovyClassDoc> classes) {
        classDocs.putAll(classes);
    }

    public void put(String packageName, GroovyPackageDoc packageDoc) {
        packageDocs.put(packageName, packageDoc);
    }

    public GroovyClassDoc[] specifiedClasses() {/*todo*/
        return null;
    }

    public GroovyPackageDoc[] specifiedPackages() {
        List<GroovyPackageDoc> packageDocValues = new ArrayList<GroovyPackageDoc>(packageDocs.values());
        Collections.sort(packageDocValues);
        return packageDocValues.toArray(new GroovyPackageDoc[packageDocValues.size()]);
    }

    public Map<String, GroovyClassDoc> getVisibleClasses(List importedClassesAndPackages) {
        Map<String, GroovyClassDoc> visibleClasses = new HashMap<String, GroovyClassDoc>();
        for (String fullClassName : classDocs.keySet()) {
            String equivalentPackageImport = fullClassName.replaceAll("[^/]+$", "*");
            if (importedClassesAndPackages.contains(fullClassName) ||
                    importedClassesAndPackages.contains(equivalentPackageImport)) {
                GroovyClassDoc classDoc = classDocs.get(fullClassName);
                visibleClasses.put(classDoc.name(), classDoc);
            }
        }
        return visibleClasses;
    }

    // GroovyDocErrorReporter interface
    public void printError(String arg0) {/*todo*/}

    //	public void printError(GroovySourcePosition arg0, String arg1) {/*todo*/}
    public void printNotice(String arg0) {/*todo*/}

    //	public void printNotice(GroovySourcePosition arg0, String arg1) {/*todo*/}
    public void printWarning(String arg0) {/*todo*/}
//	public void printWarning(GroovySourcePosition arg0, String arg1) {/*todo*/}

    public void resolve() {
        //resolve class names at the end of adding all files to the tree
        for (GroovyClassDoc groovyClassDoc : classDocs.values()) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) groovyClassDoc;
            classDoc.resolve(this);
        }

    }

}
