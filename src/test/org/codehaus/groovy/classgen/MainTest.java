/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyShell;

import java.io.File;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MainTest extends TestSupport {

    public void testMainMethod() throws Exception {
        GroovyShell shell = new GroovyShell();
        shell.run(new File("src/test/groovy/SampleMain.groovy"), new String[]{"A", "B", "C"});
    }
}
