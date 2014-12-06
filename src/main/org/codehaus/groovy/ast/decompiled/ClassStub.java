/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.ast.decompiled;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Gromov
 */
public class ClassStub {
    final String className;
    final int accessModifiers;
    final String superName;
    final String[] interfaceNames;
    final List<MethodStub> methods = new ArrayList<MethodStub>();
    final List<FieldStub> fields = new ArrayList<FieldStub>();

    public ClassStub(String className, int accessModifiers, String superName, String[] interfaceNames) {
        this.className = className;
        this.accessModifiers = accessModifiers;
        this.superName = superName;
        this.interfaceNames = interfaceNames;
    }
}

class MethodStub {
    final String methodName;
    final int accessModifiers;
    final String desc;
    final String signature;
    final String[] exceptions;

    public MethodStub(String methodName, int accessModifiers, String desc, String signature, String[] exceptions) {
        this.methodName = methodName;
        this.accessModifiers = accessModifiers;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
    }
}

class FieldStub {
    final String fieldName;
    final int accessModifiers;
    final String desc;
    final String signature;

    public FieldStub(String fieldName, int accessModifiers, String desc, String signature) {
        this.fieldName = fieldName;
        this.accessModifiers = accessModifiers;
        this.desc = desc;
        this.signature = signature;
    }
}