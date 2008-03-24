/*
 * Copyright 2003-2007 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

public class SimpleGroovyRootDoc extends SimpleGroovyDoc implements GroovyRootDoc {
	private Map packageDocs;
	private Map classDocs;

	public SimpleGroovyRootDoc(String name) {
		super(name);
		packageDocs = new HashMap();
		classDocs = new HashMap();
	}

	// todo - take account of package names !
	public GroovyClassDoc classNamed(String name) {
		Iterator itr = classDocs.keySet().iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			int lastSlashIdx = key.lastIndexOf('/');
			if (lastSlashIdx > 0) {
				String shortKey = key.substring(lastSlashIdx + 1);
				if (shortKey.equals(name)) {
					return (GroovyClassDoc) classDocs.get(key);
				}
			}
		}		
		return null;
	}
    public GroovyClassDoc[] classes() {
		List classDocValues = new ArrayList(classDocs.values());
		Collections.sort(classDocValues); // todo - performance / maybe move into a sortMe() method
		return (GroovyClassDoc[]) classDocValues.toArray(new GroovyClassDoc[classDocValues.size()]);		
	}
	public String[][] options() {/*todo*/return null;}
	public GroovyPackageDoc packageNamed(String packageName) {
		return (GroovyPackageDoc) packageDocs.get(packageName);
	}
	public void putAllClasses(Map classes) {
		classDocs.putAll(classes);
	}
	public void put(String packageName, GroovyPackageDoc packageDoc) {
		packageDocs.put(packageName, packageDoc);
	}
	
	public GroovyClassDoc[] specifiedClasses() {/*todo*/return null;}
	public GroovyPackageDoc[] specifiedPackages() {
		List packageDocValues = new ArrayList(packageDocs.values());
		Collections.sort(packageDocValues);
		return (GroovyPackageDoc[]) packageDocValues.toArray(new GroovyPackageDoc[packageDocValues.size()]);
	}

    public Map getVisibleClasses(List importedClassesAndPackages) {
        Map visibleClasses = new HashMap();
        Iterator itr = classDocs.keySet().iterator();
        while (itr.hasNext()) {
            String fullClassName = (String) itr.next();
            String equivalentPackageImport = fullClassName.replaceAll("[^/]+$","*");
            if (importedClassesAndPackages.contains(fullClassName) ||
                    importedClassesAndPackages.contains(equivalentPackageImport)) {
                GroovyClassDoc classDoc = (GroovyClassDoc) classDocs.get(fullClassName);
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
		Iterator itr = classDocs.values().iterator();
		while (itr.hasNext()) {
			SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) itr.next();
			classDoc.resolve(this);
		}
		
	}


}

