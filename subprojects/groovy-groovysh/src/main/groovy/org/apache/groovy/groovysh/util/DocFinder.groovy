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
package org.apache.groovy.groovysh.util

// TODO previous version had browseWithAWT as potential fallback if no native browser found.
// It also provided a list of URLs including GDK links. It would be nice to give those as options (or fire them all off).
class DocFinder extends HashMap<String, Object> {
    @Override
    Object get(Object key) {
        def groovyVersion = GroovySystem.getVersion()
        Class clazz = Eval.me("${key}")
        def name = clazz.name
        def path = name.replace('.', '/') + '.html'
        if (name.matches(/^(?:org\.(?:apache|codehaus)\.)?groovy\..+/)) {
            return "https://docs.groovy-lang.org/$groovyVersion/html/gapi/$path".toString()
        }
        def majorVersion = System.getProperty('java.version').tokenize('.')[0]
        def module = clazz.module?.name ?: 'java.base'
        "https://docs.oracle.com/en/java/javase/${majorVersion}/docs/api/${module}/${path}".toString()
    }
}
