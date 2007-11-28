/*
 * $Id$
 *
 * Copyright (c) 2005 The Codehaus - http://groovy.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */


package org.codehaus.groovy.control;

import groovy.lang.GroovyClassLoader;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.util.Iterator;

public class CompilationUnitTest extends MockObjectTestCase {

    public void testAppendsTheClasspathOfTheCompilerConfigurationToCurrentClassLoaderWhenInstantiated() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setClasspath(System.getProperty("java.class.path"));
        // disabled until checked with fraz
        //new CompilationUnit(configuration, null, createGroovyClassLoaderWithExpectations(configuration));
    }

    private GroovyClassLoader createGroovyClassLoaderWithExpectations(CompilerConfiguration configuration) {
        Mock mockGroovyClassLoader = mock(GroovyClassLoader.class);
        for (Iterator iterator = configuration.getClasspath().iterator(); iterator.hasNext();) {
            mockGroovyClassLoader.expects(once()).method("addClasspath").with(eq(iterator.next()));
        }
        return (GroovyClassLoader) mockGroovyClassLoader.proxy();
    }
}
