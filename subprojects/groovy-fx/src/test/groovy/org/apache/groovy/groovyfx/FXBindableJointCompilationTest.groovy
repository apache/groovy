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
package org.apache.groovy.groovyfx

import javafx.beans.property.StringProperty
import org.apache.groovy.groovyfx.beans.FXBindable
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Java code compiled jointly with an {@code @FXBindable} class sees the
 * generated {@code nameProperty()} accessors through the stub, which is what
 * the stubber exists for. Needs only {@code javafx.base} and a JDK.
 */
final class FXBindableJointCompilationTest {

    @TempDir
    File workDir

    @Test
    void javaCallerSeesTheGeneratedAccessorsThroughTheStub() {
        File person = source('foo/Person.groovy', '''
            package foo

            import org.apache.groovy.groovyfx.beans.FXBindable

            @FXBindable
            class Person {
                String name
                int age
            }
        ''')
        File user = source('foo/JavaUser.java', '''
            package foo;

            import javafx.beans.property.IntegerProperty;
            import javafx.beans.property.StringProperty;

            public class JavaUser {
                public static String rename(String to) {
                    Person person = new Person();
                    StringProperty name = person.nameProperty();
                    IntegerProperty age = person.ageProperty();
                    name.set(to);
                    age.set(36);
                    return person.getName() + " " + person.getAge();
                }
            }
        ''')
        File stubs = new File(workDir, 'stubs')
        File classes = new File(workDir, 'classes')
        def config = new CompilerConfiguration(targetDirectory: classes, jointCompilationOptions: [stubDir: stubs, keepStubs: true])
        // javac has to see the annotation's module and javafx.base; the Groovy runtime is added for it
        config.classpathList = [FXBindable, StringProperty].collect { new File(it.protectionDomain.codeSource.location.toURI()).path }

        def unit = new JavaAwareCompilationUnit(config, new GroovyClassLoader(getClass().classLoader))
        unit.addSources(person, user)
        unit.compile()

        String stub = new File(stubs, 'foo/Person.java').text
        assert stub =~ /javafx\.beans\.property\.StringProperty\s+nameProperty\(\s*\)/
        assert stub =~ /javafx\.beans\.property\.IntegerProperty\s+ageProperty\(\s*\)/

        def loader = new URLClassLoader([classes.toURI().toURL()] as URL[], getClass().classLoader)
        assert loader.loadClass('foo.JavaUser').rename('Grace') == 'Grace 36'
    }

    private File source(String path, String text) {
        File file = new File(workDir, "src/$path")
        file.parentFile.mkdirs()
        file.text = text.stripIndent()
        file
    }
}
