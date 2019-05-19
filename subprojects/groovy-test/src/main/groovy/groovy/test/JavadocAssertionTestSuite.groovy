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
package groovy.test

import junit.framework.Test
import junit.framework.TestSuite
import junit.textui.TestRunner

/**
 * <code>JavadocAssertionTestSuite</code> will dynamically create test cases from Groovy assertions placed within
 * Javadoc comments. Assertions should be placed within an html tag with a <code>class="groovyTestCase"</code>
 * attribute assignment. Example:
 * <pre>&lt;pre class="groovyTestCase"&gt; assert "example".size() == 7 &lt;/pre&gt;</pre>
 *
 * Source files will be read from the directory specified by the <code>javadocAssertion.src.dir</code>
 * system property, including all files matching <code>javadocAssertion.src.pattern</code> and
 * excluding files matching the <code>javadocAssertion.src.excludesPattern</code>. 
 *
 * By default all <code>.java</code> and <code>.groovy</code> source files from <code>./src</code> will
 * be scanned for assertions.
 *
 * You can also run this class as an application from the command line (assumes Groovy, JUnit and Ant
 * are on the classpath). As an example:
 * <p>
 * <code>
 * java groovy.test.JavadocAssertionTestSuite src/main
 * </code>
 *
 * <b>Note: this class requires the Ant module at runtime.</b>
 */
class JavadocAssertionTestSuite extends TestSuite {
    /** The System Property to set as base directory for collection of Classes.
     * The pattern will be used as an Ant fileset include basedir.
     * Key is "javadocAssertion.src.dir".
     * Defaults to the <code>./src</code> directory
     */
    public static final String SYSPROP_SRC_DIR = "javadocAssertion.src.dir";

    /** The System Property to set as the filename pattern for collection of Classes.
     * The pattern will be used as Regular Expression pattern applied with the find
     * operator against each candidate file.path.
     * Key is "javadocAssertion.src.pattern".
     * Defaults to including all <code>.java</code> and <code>.groovy</code> files.
     */
    public static final String SYSPROP_SRC_PATTERN = "javadocAssertion.src.pattern";
    
    /** The System Property to set as a filename excludes pattern for collection of Classes.
     * When non-empty, the pattern will be used as Regular Expression pattern applied with the
     * find operator against each candidate file.path.
     * Key is "javadocAssertion.src.excludesPattern".
     * Default value is "".
     */
    public static final String SYSPROP_SRC_EXCLUDES_PATTERN = "javadocAssertion.src.excludesPattern";
    
    private static final JavadocAssertionTestBuilder testBuilder = new JavadocAssertionTestBuilder()
    private static final IFileNameFinder finder = Class.forName('groovy.ant.FileNameFinder',true,this.classLoader).newInstance()
    
    static Test suite() {
        String basedir = System.getProperty(SYSPROP_SRC_DIR, "./src/")
        return suite(basedir)
    }
    
    static Test suite(String basedir) {
        String includePattern = System.getProperty(SYSPROP_SRC_PATTERN, "**/*.java,**/*.groovy")
        return suite(basedir, includePattern)
    }
    
    static Test suite(String basedir, String includePattern) {
        String excludePattern = System.getProperty(SYSPROP_SRC_EXCLUDES_PATTERN, "")
        return suite(basedir, includePattern, excludePattern)
    }
    
    static Test suite(String basedir, String includePattern, String excludePattern) {
        assert new File(basedir).exists()
        
        TestSuite suite = new JavadocAssertionTestSuite()

        Collection filenames = finder.getFileNames([dir:basedir, includes:includePattern, excludes:excludePattern])
        filenames.each { filename ->
            String code = new File(filename).text
            Class test = testBuilder.buildTest(filename, code)
            if (test != null) {
                suite.addTestSuite(test)
            }
        }

        return suite
    }

    static void main(String[] args) {
        switch(args.length) {
            case 3:
                TestRunner.run(suite(args[0], args[1], args[2]))
                break
            case 2:
                TestRunner.run(suite(args[0], args[1]))
                break
            case 1:
                TestRunner.run(suite(args[0]))
                break
            default:
                TestRunner.run(suite())
        }
    }

}
