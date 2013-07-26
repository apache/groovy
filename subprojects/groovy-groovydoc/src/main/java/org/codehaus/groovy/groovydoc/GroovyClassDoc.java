/*
 * Copyright 2003-2013 the original author or authors.
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
package org.codehaus.groovy.groovydoc;

public interface GroovyClassDoc extends GroovyType, GroovyProgramElementDoc {
    GroovyConstructorDoc[] constructors();

    GroovyConstructorDoc[] constructors(boolean filter);

    boolean definesSerializableFields();

    GroovyFieldDoc[] enumConstants();

    GroovyFieldDoc[] fields();

    GroovyFieldDoc[] properties();

    GroovyFieldDoc[] fields(boolean filter);

    GroovyClassDoc findClass(String className);

    GroovyClassDoc[] importedClasses();

    GroovyPackageDoc[] importedPackages();

    GroovyClassDoc[] innerClasses();

    GroovyClassDoc[] innerClasses(boolean filter);

    GroovyClassDoc[] interfaces();

    GroovyType[] interfaceTypes();

    boolean isAbstract();

    boolean isExternalizable();

    boolean isSerializable();

    GroovyMethodDoc[] methods();

    GroovyMethodDoc[] methods(boolean filter);

    GroovyFieldDoc[] serializableFields();

    GroovyMethodDoc[] serializationMethods();

    boolean subclassOf(GroovyClassDoc gcd);

    GroovyClassDoc superclass();

    GroovyType superclassType();
//    GroovyTypeVariable[] typeParameters(); // not supported in groovy
//    GroovyParamTag[] typeParamTags(); // not supported in groovy


    String getFullPathName(); // not in Java Doclet API

    String getRelativeRootPath(); // not in Java Doclet API
}
