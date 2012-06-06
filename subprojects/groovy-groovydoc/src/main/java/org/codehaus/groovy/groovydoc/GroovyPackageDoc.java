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

package org.codehaus.groovy.groovydoc;

public interface GroovyPackageDoc extends GroovyDoc {
    /**
     * All included classes and interfaces in this package.
     *
     * @return array of classes and interfaces found or empty array if none found
     */
    GroovyClassDoc[] allClasses();

    /**
     * All classes and interfaces in this package optionally
     * limited to just the included ones.
     *
     * @param filter Specifying true filters according to the specified access modifier option.
     * Specifying false includes all classes and interfaces regardless of access modifier option.
     * @return array of classes and interfaces found or empty array if none found
     */
    GroovyClassDoc[] allClasses(boolean filter);

//    GroovyAnnotationTypeDoc[] annotationTypes();

//    GroovyAnnotationDesc[] annotations();

    /**
     * Included enum types in this package.
     *
     * @return array of enum types found or empty array if none found
     */
    GroovyClassDoc[] enums();

    /**
     * Included errors in this package.
     *
     * @return array of errors found or empty array if none found
     */
    GroovyClassDoc[] errors();

    /**
     * Included exceptions in this package.
     *
     * @return array of exceptions found or empty array if none found
     */
    GroovyClassDoc[] exceptions();

    /**
     * Find a class or interface within this package.
     *
     * @param className the name of the class to find
     * @return ClassDoc of found class or interface, or null if not found
     */
    GroovyClassDoc findClass(String className);

    /**
     * Included interfaces in this package.
     *
     * @return array of interfaces found or empty array if none found
     */
    GroovyClassDoc[] interfaces();

    /**
     * Included ordinary classes in this package.
     *
     * @return array of ordinary classes (non-interface, non-enum, non-throwable classes) found or empty array if none found
     */
    GroovyClassDoc[] ordinaryClasses();

    /**
     * The one-sentence summary for the package derived from the beginning of the description.
     *
     * @return the summary
     */
    String summary();

    /**
     * Description of the package.
     *
     * @return the description
     */
    String description();

    String nameWithDots(); // not in JavaDoc API

    String getRelativeRootPath(); // not in JavaDoc API
}
