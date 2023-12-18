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
package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

final class Groovy11250 {

    @Test
    void testMultipleInstanceOfPropertyAccess() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [memStub: true]
            targetDirectory = File.createTempDir()
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Property.java')
            a.write '''
                public class Property {
                    private String targetName;
                    private String propertyName;
                    public String getName() { return propertyName; }
                    public String getTargetName() { return targetName; }
                    public void setTargetName(String targetName) { this.targetName = targetName; }
                    public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
                }
            '''

            def b = new File(parentDir, 'PersistentProperty.java')
            b.write '''
                public interface PersistentProperty<T extends Property> {
                    String getName();
                }
            '''

            def c = new File(parentDir, 'AbstractPersistentProperty.java')
            c.write '''
                public abstract class AbstractPersistentProperty<T extends Property> implements PersistentProperty<T> {
                    protected final String name;
                    public AbstractPersistentProperty(String name) {
                        this.name = name;
                    }
                    @Override
                    public String getName() {
                        return name;
                    }
                }
            '''

            def d = new File(parentDir, 'Association.java')
            d.write '''
                public abstract class Association<T extends Property> extends AbstractPersistentProperty<T> {
                    public Association(String name) {
                        super(name);
                    }
                }
            '''

            def e = new File(parentDir, 'Main.groovy')
            e.write '''import groovy.transform.CompileStatic
                @CompileStatic
                class Attribute extends Property {
                }
                @CompileStatic
                abstract class ToOne<T extends Property> extends Association<T> {
                    ToOne(String name) { super(name) }
                }
                @CompileStatic
                abstract class ToMany<T extends Property> extends Association<T> {
                    ToMany(String name) { super(name) }
                }

                @CompileStatic
                abstract class OneToOne<T extends Property> extends ToOne<T> {
                    OneToOne(String name) { super(name) }
                }
                @CompileStatic
                abstract class OneToMany<T extends Property> extends ToMany<T> {
                    OneToMany(String name) { super(name) }
                }
                @CompileStatic
                abstract class ManyToMany<T extends Property> extends ToMany<T> {
                    ManyToMany(String name) { super(name) }
                }

                @CompileStatic
                static main(args) {
                    def oneToOne = new OneToOne<Attribute>('foo') {}
                    def oneToMany = new OneToMany<Attribute>('bar') {}
                    for (association in [oneToOne, oneToMany]) {
                        if (association instanceof ToOne) {
                            def propertyName = association.name
                            println "to-one -> $propertyName"
                        } else if (association instanceof OneToMany || association instanceof ManyToMany) {
                            def associationName = association.getName()
                            def propertyName = association.name
                            println "to-many -> $propertyName"
                        }
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d, e)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
