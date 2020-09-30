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
package org.codehaus.groovy.reflection.stdclasses;

import groovy.lang.GString;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.ReflectionCache;

public class StringCachedClass extends CachedClass {
    private static final Class STRING_CLASS = String.class;
    private static final Class GSTRING_CLASS = GString.class;

    public StringCachedClass(ClassInfo classInfo) {
        super(STRING_CLASS, classInfo);
    }

    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return argument instanceof String;
    }

    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return  classToTransformFrom == null
              || classToTransformFrom == STRING_CLASS
              || ReflectionCache.isAssignableFrom(GSTRING_CLASS,classToTransformFrom);
    }

    @Override
    public Object coerceArgument(Object argument) {
        return argument instanceof GString ? argument.toString() : argument;
    }
}
