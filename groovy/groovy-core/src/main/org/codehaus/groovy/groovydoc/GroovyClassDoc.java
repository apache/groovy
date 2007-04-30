/**
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
 **/
package org.codehaus.groovy.groovydoc;

public interface GroovyClassDoc extends GroovyType, GroovyProgramElementDoc{
	public GroovyConstructorDoc[] constructors();
	public GroovyConstructorDoc[] constructors(boolean filter);
	public boolean definesSerializableFields();
	public GroovyFieldDoc[] enumConstants();
	public GroovyFieldDoc[] fields();
	public GroovyFieldDoc[] fields(boolean filter);
	public GroovyClassDoc findClass(String className);
	public GroovyClassDoc[] importedClasses();
	public GroovyPackageDoc[] importedPackages();
	public GroovyClassDoc[] innerClasses(); // not supported in groovy
	public GroovyClassDoc[] innerClasses(boolean filter); // not supported in groovy
	public GroovyClassDoc[] interfaces();
	public GroovyType[] interfaceTypes();
	public boolean isAbstract();
	public boolean isExternalizable();
	public boolean isSerializable();
	public GroovyMethodDoc[] methods();
	public GroovyMethodDoc[] methods(boolean filter);
	public GroovyFieldDoc[] serializableFields();
	public GroovyMethodDoc[] serializationMethods();
	public boolean subclassOf(GroovyClassDoc gcd);
	public GroovyClassDoc superclass();
	public GroovyType superclassType();
//	public GroovyTypeVariable[] typeParameters(); // not supported in groovy
//	public GroovyParamTag[] typeParamTags(); // not supported in groovy


	public String getFullPathName(); // not in Java Doclet API
	public String getRelativeRootPath(); // not in Java Doclet API	
}
