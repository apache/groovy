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
 * Test that fileorder doesn't impact whether GroovyObject appears in implements list.
 */
class Groovy7966Bug extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
                'Before.groovy' : '''
                  class Before extends AbstractThing {}
                ''',
                'AbstractThing.groovy' : '''
                  abstract class AbstractThing {}
                ''',
                'JavaThing.java' : '''
                  public class JavaThing {
                  }
                ''',
                'After.groovy' : '''
                  class After extends AbstractThing {}
                ''',
        ]
    }

    @Override
    protected List<File> collectSources(File path) {
        // parent method uses order returned by the file system - we want to maintain supplied order
        return provideSources().collect{ name, _ -> new File(path, name) }
    }

    @Override
    void verifyStubs() {
        // actually, we don't care about the stubs but let's verify the resulting class files
        def classLoader = new URLClassLoader([targetDir.toURI().toURL()] as URL[], loader)
        assert classLoader.loadClass('Before').interfaces.length == 0
        assert classLoader.loadClass('After').interfaces.length == 0
    }
}
