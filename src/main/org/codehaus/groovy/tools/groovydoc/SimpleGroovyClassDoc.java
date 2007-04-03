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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.codehaus.groovy.groovydoc.*;

public class SimpleGroovyClassDoc extends SimpleGroovyProgramElementDoc implements GroovyClassDoc {
	private List constructors;
	private List fields;
	private List methods;
	private String fullPathName;

	public SimpleGroovyClassDoc(String name) {
		super(name);
		constructors = new ArrayList();
		fields = new ArrayList();
		methods = new ArrayList();
	}

	/**
	 * returns a sorted array of constructors
	 */
	public GroovyConstructorDoc[] constructors() {
		Collections.sort(constructors); // todo - performance / maybe move into a sortMe() method
		return (GroovyConstructorDoc[]) constructors.toArray(new GroovyConstructorDoc[constructors.size()]);
	}
	public boolean add(GroovyConstructorDoc constructor) {
		return constructors.add(constructor);
	}

	/**
	 * returns a sorted array of fields
	 */
	public GroovyFieldDoc[] fields() {
		Collections.sort(fields); // todo - performance / maybe move into a sortMe() method
		return (GroovyFieldDoc[]) fields.toArray(new GroovyFieldDoc[fields.size()]);
	}
	public boolean add(GroovyFieldDoc field) {
		return fields.add(field);
	}

	/**
	 * returns a sorted array of methods
	 */
	public GroovyMethodDoc[] methods() {
		Collections.sort(methods); // todo - performance / maybe move into a sortMe() method
		return (GroovyMethodDoc[]) methods.toArray(new GroovyMethodDoc[methods.size()]);
	}
	public boolean add(GroovyMethodDoc method) {
		return methods.add(method);
	}
		
	public String getFullPathName() {
		return fullPathName;
	}
	public void setFullPathName(String fullPathName) {
		this.fullPathName = fullPathName;
	}
	
	public String getRelativeRootPath() {
		StringTokenizer tokenizer = new StringTokenizer(fullPathName, "/"); // todo windows??
		StringBuffer sb = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			tokenizer.nextToken();
			sb.append("../");
		}
		return sb.toString();
	}	
	
	
	// methods from GroovyClassDoc
	
	public GroovyConstructorDoc[] constructors(boolean filter) {/*todo*/return null;}
	public boolean definesSerializableFields() {/*todo*/return false;}
	public GroovyFieldDoc[] enumConstants() {/*todo*/return null;}
	public GroovyFieldDoc[] fields(boolean filter) {/*todo*/return null;}
	public GroovyClassDoc findClass(String className) {/*todo*/return null;}
	public GroovyClassDoc[] importedClasses() {/*todo*/return null;}
	public GroovyPackageDoc[] importedPackages() {/*todo*/return null;}
	public GroovyClassDoc[] innerClasses() {/*todo*/return null;} // not supported in groovy
	public GroovyClassDoc[] innerClasses(boolean filter) {/*todo*/return null;} // not supported in groovy
	public GroovyClassDoc[] interfaces() {/*todo*/return null;}
	public GroovyType[] interfaceTypes() {/*todo*/return null;}
	public boolean isAbstract() {/*todo*/return false;}
	public boolean isExternalizable() {/*todo*/return false;}
	public boolean isSerializable() {/*todo*/return false;}
	public GroovyMethodDoc[] methods(boolean filter) {/*todo*/return null;}
	public GroovyFieldDoc[] serializableFields() {/*todo*/return null;}
	public GroovyMethodDoc[] serializationMethods() {/*todo*/return null;}
	public boolean subclassOf(GroovyClassDoc gcd) {/*todo*/return false;}
	public GroovyClassDoc superclass() {/*todo*/return null;}
	public GroovyType superclassType() {/*todo*/return null;}
//	public GroovyTypeVariable[] typeParameters() {/*todo*/return null;} // not supported in groovy
//	public GroovyParamTag[] typeParamTags() {/*todo*/return null;} // not supported in groovy

	
	
	// methods from GroovyType (todo: remove this horrible copy of SimpleGroovyType.java)

//	public GroovyAnnotationTypeDoc asAnnotationTypeDoc() {/*todo*/return null;}
	public GroovyClassDoc asClassDoc() {/*todo*/return null;}
//	public GroovyParameterizedType asParameterizedType() {/*todo*/return null;}
//	public GroovyTypeVariable asTypeVariable() {/*todo*/return null;}
//	public GroovyWildcardType asWildcardType() {/*todo*/return null;}
	public String dimension() {/*todo*/return null;}
	public boolean isPrimitive() {/*todo*/return false;}
	public String qualifiedTypeName() {/*todo*/return null;}
	public String simpleTypeName() {/*todo*/return null;}
	public String typeName() {/*todo*/return null;}

}
