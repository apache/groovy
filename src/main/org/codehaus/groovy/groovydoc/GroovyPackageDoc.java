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
    GroovyClassDoc[] allClasses();
    GroovyClassDoc[] allClasses(boolean arg0);
//    GroovyAnnotationTypeDoc[] annotationTypes();
//    GroovyAnnotationDesc[] annotations();
    GroovyClassDoc[] enums();
    GroovyClassDoc[] errors();
    GroovyClassDoc[] exceptions();
    GroovyClassDoc findClass(String arg0);
    GroovyClassDoc[] interfaces();
    GroovyClassDoc[] ordinaryClasses();

    String summary();
    String description();
	
    String nameWithDots(); // not in JavaDoc API
    String getRelativeRootPath(); // not in JavaDoc API
}
