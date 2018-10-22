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
package org.codehaus.groovy.ast.decompiled;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structures holding class info to enable lazy loading
 */
public class ClassStub extends MemberStub {
    final String className;
    final int accessModifiers;
    final String signature;
    final String superName;
    final String[] interfaceNames;
    List<MethodStub> methods;
    List<FieldStub> fields;

    // Used to store the real access modifiers for inner classes
    int innerClassModifiers = -1;

    public ClassStub(String className, int accessModifiers, String signature, String superName, String[] interfaceNames) {
        this.className = className;
        this.accessModifiers = accessModifiers;
        this.signature = signature;
        this.superName = superName;
        this.interfaceNames = interfaceNames;
    }
}

class MemberStub {
    List<AnnotationStub> annotations = null;

    AnnotationStub addAnnotation(String desc) {
        AnnotationStub stub = new AnnotationStub(desc);
        if (annotations == null) annotations = new ArrayList<AnnotationStub>(1);
        annotations.add(stub);
        return stub;
    }
}

class MethodStub extends MemberStub {
    final String methodName;
    final int accessModifiers;
    final String desc;
    final String signature;
    final String[] exceptions;
    Map<Integer, List<AnnotationStub>> parameterAnnotations;
    Object annotationDefault;

    public MethodStub(String methodName, int accessModifiers, String desc, String signature, String[] exceptions) {
        this.methodName = methodName;
        this.accessModifiers = accessModifiers;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
    }
}

class FieldStub extends MemberStub {
    final String fieldName;
    final int accessModifiers;
    final String desc;
    final String signature;
    final Object value;

    public FieldStub(String fieldName, int accessModifiers, String desc, String signature) {
        this(fieldName, accessModifiers, desc, signature, null);
    }

    public FieldStub(String fieldName, int accessModifiers, String desc, String signature, Object value) {
        this.fieldName = fieldName;
        this.accessModifiers = accessModifiers;
        this.desc = desc;
        this.signature = signature;
        this.value = value;
    }
}

class AnnotationStub {
    final String className;
    final Map<String, Object> members = new LinkedHashMap<String, Object>();

    public AnnotationStub(String className) {
        this.className = className;
    }
}

class TypeWrapper {
    final String desc;

    public TypeWrapper(String desc) {
        this.desc = desc;
    }
}

class EnumConstantWrapper {
    final String enumDesc;
    final String constant;

    public EnumConstantWrapper(String enumDesc, String constant) {
        this.enumDesc = enumDesc;
        this.constant = constant;
    }
}