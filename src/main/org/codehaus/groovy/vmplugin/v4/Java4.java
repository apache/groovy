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
package org.codehaus.groovy.vmplugin.v4;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.control.CompilationUnit;

import java.lang.reflect.Method;

/**
 * java 4 based functions
 * @author Jochen Theodorou
 *
 */
public class Java4 implements VMPlugin {
    private static Class[] EMPTY_CLASS_ARRAY = new Class[0];
    
    public void setAdditionalClassInformation(ClassNode c) {
        return;        
    }
    
    public Class[] getPluginDefaultGroovyMethods() {
        return EMPTY_CLASS_ARRAY;
    }

    public void addPhaseOperations(CompilationUnit unit) {
        // add nothing
    }

    public void setMethodDefaultValue(MethodNode mn, Method m) {
        // do nothing
    }

    public void setAnnotationMetaData(ClassNode cn) {
        // do nothing
    }

    public void configureAnnotation(AnnotationNode an) {
        // do nothing        
    }
}
