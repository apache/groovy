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
package org.codehaus.groovy.tools.stubgenerator

/**
 * A subclass of <code>StubTestCase</code> for simple cases
 * when you don't want to create a directory with full blown samples,
 * you can use it by overriding <code>provideSources()</code>
 * to provide code sources as strings. 
 */
abstract class StringSourcesStubTestCase extends StubTestCase {

    /**
     * All sub classes should implement this method to provide the sources to be jointly compiled
     * in the form of Strings.
     * <p>
     * Example of mapping:
     * <pre><code>
     *  ['com/foo/Bar.groovy': '''
     *      package com.foo
     *      class Bar {}
     *  ''']
     * </code></pre>
     *
     * @return a mapping of source file names and their source code
     */
    abstract Map<String, String> provideSources()

    /**
     * Create a temporary directory to store the provided sources.
     *
     * @return a temporary directory
     */
    protected File sourcesRootPath() {
        def path = createTempDirectory()

        if (debug) println ">>> sources root path: ${path}"

        def sources = provideSources()
        sources.each { String relativeFilePath, String sourceCode ->
            
            createNecessaryPackageDirs(path, relativeFilePath)
            
            def sourceFile = new File(path, relativeFilePath)

            if (debug) println " -> ${sourceFile}"

            sourceFile << sourceCode
        }

        return path
    }
}

