/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.reflection

System.err.println "WARNING: this class is deprecated and will be removed in Groovy 1.9"

def types = [
        "boolean",
        "char",
        "byte",
        "short",
        "int",
        "long",
        "float",
        "double",
        "Object"
]

types.each { arg1 ->
    println "public Object invoke(Object receiver, $arg1 arg1) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1)); }"
    types.each { arg2 ->
        println "public Object invoke(Object receiver, $arg1 arg1, $arg2 arg2) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2)); }"
        types.each { arg3 ->
            println "public Object invoke(Object receiver, $arg1 arg1, $arg2 arg2, $arg3 arg3) throws Throwable { return invoke(receiver,ArrayUtil.createArray(arg1,arg2,arg3)); }"
        }
    }
}
