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

import org.codehaus.groovy.groovydoc.GroovyClassDoc;

import java.util.Map;

/**
 * Parses a single source unit into Groovydoc class documentation objects.
 */
public interface GroovyDocParserI {
    /**
     * Parses one source unit and returns the classes discovered in it.
     *
     * @param packagePath the package path associated with the source
     * @param file the source file name
     * @param src the source text to parse
     * @return a map of discovered class names to class documentation objects
     */
    Map<String, GroovyClassDoc> getClassDocsFromSingleSource(String packagePath, String file, String src);
}
