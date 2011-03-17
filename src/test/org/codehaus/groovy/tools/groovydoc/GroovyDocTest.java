/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.tools.groovydoc;

import org.apache.tools.ant.BuildFileTest;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * @author Andre Steingress
 */
public class GroovyDocTest extends BuildFileTest {

    private static final String SRC_TESTFILES = "src/test-resources/groovydoc/";

    private File tmpDir;

    public GroovyDocTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {

        configureProject(SRC_TESTFILES + "buildWithCustomGroovyDoc.xml");

        tmpDir = new File(getProject().getProperty("tmpdir"));
    }

    public void testCustomClassTemplate() throws Exception {
        executeTarget("doc");

        final File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles");

        final String[] list = testfilesPackageDir.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.equals("DocumentedClass.html");
            }
        });

        assertEquals(1, list.length);
        File documentedClassHtmlDoc = new File(testfilesPackageDir, list[0]);

        List<String> lines = DefaultGroovyMethods.readLines(documentedClassHtmlDoc);
        assertTrue(lines.contains("<title>DocumentedClass</title>"));
        assertTrue(lines.contains("This is a custom class template."));
    }
}
