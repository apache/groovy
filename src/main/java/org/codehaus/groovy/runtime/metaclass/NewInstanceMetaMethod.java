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
package org.codehaus.groovy.runtime.metaclass;

import org.codehaus.groovy.reflection.CachedMethod;

import java.lang.reflect.Modifier;

/**
 * A MetaMethod implementation where the underlying method is really a static
 * helper method on some class but it appears to be an instance method on a class.
 *
 * This implementation is used to add new methods to the JDK writing them as normal
 * static methods with the first parameter being the class on which the method is added.
 */
public class NewInstanceMetaMethod extends NewMetaMethod {


    public NewInstanceMetaMethod(CachedMethod method) {
        super(method);
    }

    public boolean isStatic() {
        return false;
    }

    public int getModifiers() {
        // lets clear the static bit
        return Modifier.PUBLIC;
    }

    public Object invoke(Object object, Object[] arguments)  {
        // we need to cheat using the type
        int size = arguments.length;
        Object[] newArguments = new Object[size + 1];
        newArguments[0] = object;
        System.arraycopy(arguments, 0, newArguments, 1, size);
        return super.invoke(null, newArguments);
    }
}
