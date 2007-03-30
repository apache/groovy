/*
 *
 * Copyright 2007 Jeremy Rayner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.groovy.tools.groovydoc;

import java.util.HashMap;
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
	
	public GroovyClassDoc classNamed(String arg0) {/*todo*/return null;}
	public GroovyClassDoc[] classes() {
		return (GroovyClassDoc[]) classDocs.values().toArray(new GroovyClassDoc[classDocs.values().size()]);		
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
		// todo - array sorted by name
		return (GroovyPackageDoc[]) packageDocs.values().toArray(new GroovyPackageDoc[packageDocs.values().size()]);
	}

	
	
// GroovyDocErrorReporter interface
	public void printError(String arg0) {/*todo*/}
//	public void printError(GroovySourcePosition arg0, String arg1) {/*todo*/}
	public void printNotice(String arg0) {/*todo*/}
//	public void printNotice(GroovySourcePosition arg0, String arg1) {/*todo*/}
	public void printWarning(String arg0) {/*todo*/}
//	public void printWarning(GroovySourcePosition arg0, String arg1) {/*todo*/}


}

