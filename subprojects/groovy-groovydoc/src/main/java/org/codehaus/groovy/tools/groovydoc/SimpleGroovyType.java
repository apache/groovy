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

import org.codehaus.groovy.groovydoc.GroovyType;

public class SimpleGroovyType implements GroovyType {
    private final String typeName;

    public SimpleGroovyType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String typeName() {
        return typeName;
    }

    @Override
    public boolean isPrimitive() {
        return false; // TODO
    }

    @Override
    public String qualifiedTypeName() {
        return typeName.startsWith("DefaultPackage.") ? typeName.substring("DefaultPackage.".length()) : typeName;
    }

    @Override
    public String simpleTypeName() {
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot < 0) return typeName;
        return typeName.substring(lastDot + 1);
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
