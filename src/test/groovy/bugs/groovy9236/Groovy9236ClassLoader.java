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
package bugs.groovy9236;

import groovy.lang.GroovyClassLoader;

import java.util.ArrayList;
import java.util.List;

public class Groovy9236ClassLoader extends GroovyClassLoader {
    final List<String> guessedClassNameList = new ArrayList<>();

    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        guessedClassNameList.add(name);
        return super.findClass(name);
    }
}
