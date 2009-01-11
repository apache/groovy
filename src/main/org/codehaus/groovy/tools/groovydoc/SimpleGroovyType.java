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

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyType;

public class SimpleGroovyType implements GroovyType {
    private String typeName;

    public SimpleGroovyType(String typeName) {
        this.typeName = typeName;
    }

    public String typeName() {
        return typeName;
    }

    public boolean isPrimitive() {
        return false; // TODO
    }

    public String qualifiedTypeName() {
        return typeName;
    }

    public String simpleTypeName() {
        return typeName;
    }

//    public GroovyAnnotationTypeDoc asAnnotationTypeDoc() {/*todo*/
//        return null;
//    }
//
//    public GroovyClassDoc asClassDoc() {/*todo*/
//        return null;
//    }
//
//    public GroovyParameterizedType asParameterizedType() {/*todo*/
//        return null;
//    }
//
//    public GroovyTypeVariable asTypeVariable() {/*todo*/
//        return null;
//    }
//
//    public GroovyWildcardType asWildcardType() {/*todo*/
//        return null;
//    }
//
//    public String dimension() {/*todo*/
//        return null;
//    }

}
