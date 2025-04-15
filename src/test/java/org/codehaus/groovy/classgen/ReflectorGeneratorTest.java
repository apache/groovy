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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.reflection.CachedMethod;

import java.lang.reflect.InvocationTargetException;

class A_GroovyReflector {
    static void doIt () {

        new A ().protectedMethod();

        try {
            CachedMethod m = CachedMethod.find(A.class.getDeclaredMethod("protectedMethod", new Class [0] ));
            Object[] arguments = new Object[0];
            m.setAccessible().invoke(new A(), arguments);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        }
    }

    static class A {
        protected void protectedMethod() {}
    }
}
