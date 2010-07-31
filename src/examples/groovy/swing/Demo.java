/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.swing;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyClassLoader;
import groovy.util.GroovyTestCase;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Demo extends GroovyTestCase {
    ClassLoader parentLoader = getClass().getClassLoader();
    protected GroovyClassLoader loader =
        (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new GroovyClassLoader(parentLoader);
            }
        });

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        GroovyObject object = demo.compile("src/examples/groovy/swing/SwingDemo.groovy");
        object.invokeMethod("run", null);
    }

    protected GroovyObject compile(String fileName) throws Exception {
        Class groovyClass = loader.parseClass(new GroovyCodeSource(new File(fileName)));
        GroovyObject object = (GroovyObject) groovyClass.newInstance();
        assertTrue(object != null);
        return object;
    }
}
