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
package groovy.bugs;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;

public class Groovy2365Bug extends Groovy2365Base {

    public void testDeadlock () {
        String path = createData();

        try {
            for (int i = 0; i != 100; ++i ) {
                    final GroovyClassLoader groovyLoader = new GroovyClassLoader ();
                    groovyLoader.addClasspath(path);

                    Class _script1Class = null;
                    try {
                        _script1Class = groovyLoader.loadClass("Script1", true, true);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    final Class script1Class = _script1Class;

                    // setup two threads to try a deadlock

                    // thread one: newInstance script foo
                    final boolean completed [] = new boolean[2] ;
                    Thread thread1 = new Thread(() -> {
                        try {
                            Script script = (Script) script1Class.getDeclaredConstructor().newInstance();
                            script.run();
                            completed [0] = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    Thread thread2 = new Thread(() -> {
                        try {
                            Class cls = groovyLoader.loadClass("Script2", true, true);
                            Script script = (Script) cls.getDeclaredConstructor().newInstance();
                            script.run();
                            completed [1] = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    // let's see if we get a deadlock
                    thread2.start();
                    thread1.start();

                try {
                    thread1.join(5000);
                    thread2.join(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                assertTrue("Potentially deadlock", completed[0] && completed[1]);
            }
        } finally {
            ResourceGroovyMethods.deleteDir(new File(path));
        }
    }
}
