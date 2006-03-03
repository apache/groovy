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


package groovy.lang;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import junit.framework.TestCase;

public class GroovyClassLoaderTest extends TestCase {

    private final GroovyClassLoader classLoader = new GroovyClassLoader();

    private boolean contains(String[] paths, String eval) {
        try {
            eval = (new File(eval)).toURI().toURL().getFile();
        } catch (MalformedURLException e) {
            return false;
        }
        for (int i=0; i<paths.length; i++) {
            if(eval.equals(paths[i])) return true;
        }
        return false;
    }
    
    public void testAddsAClasspathEntryOnlyIfItHasNotAlreadyBeenAdded() {
        String newClasspathEntry = "/tmp";
        int initialNumberOfClasspathEntries = classLoader.getClassPath().length;

        classLoader.addClasspath(newClasspathEntry);
        assertEquals("number of classpath entries", initialNumberOfClasspathEntries + 1, classLoader.getClassPath().length);
        assertTrue("contains new classpath entry", contains(classLoader.getClassPath(),newClasspathEntry));

        classLoader.addClasspath(newClasspathEntry);
        assertEquals("number of classpath entries", initialNumberOfClasspathEntries + 1, classLoader.getClassPath().length);
        assertTrue("contains new classpath entry", contains(classLoader.getClassPath(),newClasspathEntry));
    }
}
