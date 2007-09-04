/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package org.codehaus.groovy.tools;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A TestSuite which will run a Groovy unit test case inside any Java IDE
 * either as a unit test case or as an application.
 * <p/>
 * You can specify the GroovyUnitTest to run by running this class as an appplication
 * and specifying the script to run on the command line.
 * <p/>
 * <code>
 * java groovy.util.GroovyTestSuite src/test/Foo.groovy
 * </code>
 * <p/>
 * Or to run the test suite as a unit test suite in an IDE you can use
 * the 'test' system property to define the test script to run.
 * e.g. pass this into the JVM when the unit test plugin runs...
 * <p/>
 * <code>
 * -Dtest=src/test/Foo.groovy
 * </code>
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class FindAllTestsSuite extends TestSuite {

    protected static final String testDirectory = "target/test-classes";

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        FindAllTestsSuite suite = new FindAllTestsSuite();
        try {
            suite.loadTestSuite();
        } catch (Exception e) {
            throw new RuntimeException("Could not create the test suite: " + e, e);
        }
        return suite;
    }

    public void loadTestSuite() throws Exception {
        recurseDirectory(new File(testDirectory));
    }

    protected void recurseDirectory(File dir) throws Exception {
        File[] files = dir.listFiles();
        List traverseList = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                traverseList.add(file);
            } else {
                String name = file.getName();
                if (name.endsWith("Test.class") || name.endsWith("Bug.class")) {
                    addTest(file);
                }
            }
        }
        for (Iterator iter = traverseList.iterator(); iter.hasNext();) {
            recurseDirectory((File) iter.next());
        }
    }

    protected void addTest(File file) throws Exception {
        String name = file.getPath();

        name = name.substring(testDirectory.length() + 1, name.length() - ".class".length());
        name = name.replace(File.separatorChar, '.');

        //System.out.println("Found: " + name);
        Class type = loadClass(name);
        addTestSuite(type);
    }

    protected Class loadClass(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return getClass().getClassLoader().loadClass(name);
            } catch (ClassNotFoundException e1) {
                return Class.forName(name);
            }
        }
    }
}
