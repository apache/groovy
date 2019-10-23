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
package groovy.bugs.groovy9236

import groovy.test.GroovyTestCase
import org.apache.groovy.util.ScriptRunner

class Groovy9236Bug extends GroovyTestCase {
    /* groovy-3.0.0-beta-3 will try to guess and load the following classes:
Script1$_p_lambda1BeanInfo
Script1$_p_lambda1Customizer
Script1BeanInfo
Script1Customizer
groovy$transform$CompileStatic
groovy.lang.GroovyObject$Collectors
groovy.lang.GroovyObject$CompileStatic
groovy.lang.GroovyObject$groovy$transform$CompileStatic
groovy.lang.GroovyObject$java$util$stream$Collectors
groovy.lang.GroovyObject$java$util$stream$Stream
groovy.lang.groovy$transform$CompileStatic
groovy.lang.java$util$stream$Collectors
groovy.lang.java$util$stream$Stream
groovy.transform$CompileStatic
groovy.util.groovy$transform$CompileStatic
groovy.util.java$util$stream$Collectors
groovy.util.java$util$stream$Stream
java$util$stream$Collectors
java$util$stream$Stream
java.io.groovy$transform$CompileStatic
java.io.java$util$stream$Collectors
java.io.java$util$stream$Stream
java.lang.groovy$transform$CompileStatic
java.lang.java$util$stream$Collectors
java.lang.java$util$stream$Stream
java.net.groovy$transform$CompileStatic
java.net.java$util$stream$Collectors
java.net.java$util$stream$Stream
java.util$stream$Collectors
java.util$stream$Stream
java.util.groovy$transform$CompileStatic
java.util.java$util$stream$Collectors
java.util.java$util$stream$Stream
java.util.stream$Collectors
java.util.stream$Stream
     */
    void testAvoidUnnecessaryResolving() {
        def cl = new Groovy9236ClassLoader()
        def gs = new GroovyShell(cl)
        gs.evaluate('''
                import groovy.transform.CompileStatic
                import java.util.stream.Collectors
                import java.util.stream.Stream
                
                @CompileStatic
                void p() {
                    assert [2, 3, 4] == [1, 2, 3].stream().map(e -> e.plus 1).collect(Collectors.toList())
                }
                
                p()
        ''')

        final classNamesShouldAvoidToGuess = ['java.lang.java$util$stream$Collectors',
                                              'java.util.java$util$stream$Collectors',
                                              'java.io.java$util$stream$Collectors',
                                              'java.net.java$util$stream$Collectors',
                                              'groovy.lang.java$util$stream$Collectors',
                                              'groovy.util.java$util$stream$Collectors',
                                              'java$util$stream$Collectors',
                                              'java.util$stream$Collectors',
                                              'java.util.stream$Collectors',
                                              'java.lang.groovy$transform$CompileStatic',
                                              'java.util.groovy$transform$CompileStatic',
                                              'java.io.groovy$transform$CompileStatic',
                                              'java.net.groovy$transform$CompileStatic',
                                              'groovy.lang.groovy$transform$CompileStatic',
                                              'groovy.util.groovy$transform$CompileStatic',
                                              'groovy$transform$CompileStatic',
                                              'groovy.transform$CompileStatic',
                                              'java.lang.java$util$stream$Stream',
                                              'java.util.java$util$stream$Stream',
                                              'java.io.java$util$stream$Stream',
                                              'java.net.java$util$stream$Stream',
                                              'groovy.lang.java$util$stream$Stream',
                                              'groovy.util.java$util$stream$Stream',
                                              'java$util$stream$Stream',
                                              'java.util$stream$Stream',
                                              'java.util.stream$Stream']

        assert cl.guessedClassNameList.every(n -> !classNamesShouldAvoidToGuess.contains(n))
    }

    void testResolvingPrecedence() {
        ScriptRunner.runScript('/groovy/bugs/groovy9236/Main.groovy')
    }

    private static class Groovy9236ClassLoader extends GroovyClassLoader {
        def guessedClassNameList = []

        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            guessedClassNameList << name
            super.findClass(name)
        }
    }
}
