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
package groovy.ant

class Groovy9352Test extends AntTestCase {
    void test() {
        doInTmpDir { ant, baseDir ->
            baseDir.src {
                p1 {
                    'Consumer.groovy'('''
                        package p1
                        import groovy.transform.CompileStatic
                        import p2.Producer
                        
                        @CompileStatic
                        class Consumer {
                           void doSomething(Producer producer) {
                              // this shouldn't trigger a compile error
                              producer.foo()
                           }
                        
                        }
                    ''')
                }
                p2 {
                    'Producer.java'('''
                        package p2;
                        import java.util.List;
                        import java.util.ArrayList;
                        
                        public class Producer {
                           public void foo() {}
                        
                           // the following members are private, they shouldn't leak into the public API
                           private Gson gson;
                           private Gson gson2 = new SubGson();
                           private List<Gson> gsonList;
                           @GsonAnnotation
                           private List<? extends Gson> gsonList2 = new ArrayList<SubGson>();
                           
                           @GsonAnnotation
                           private Producer(Gson p) {}
                           private Producer(int p) throws Gson {}
                           private Producer() { gson = new Gson(); }
                           
                           @GsonAnnotation
                           private void bar(Gson p) {}
                           private Gson bar() { return null;}
                           private void bar(int p) throws Gson {}
                           private Object bar(float p) { return new Gson(); }
                        }
                    ''')
                    'Gson.java'('''
                        package p2;
                        class Gson extends Exception {
                        }
                    ''')
                    'SubGson.java'('''
                        package p2;
                        class SubGson extends Gson {
                        }
                    ''')
                    'GsonAnnotation.java'('''
                        package p2;
                        import java.lang.annotation.ElementType;
                        import java.lang.annotation.Retention;
                        import java.lang.annotation.RetentionPolicy;
                        import java.lang.annotation.Target;
                        
                        @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
                        @Retention(RetentionPolicy.RUNTIME)
                        @interface GsonAnnotation {
                        }
                    ''')
                }
            }

            ant.mkdir(dir: 'build')
            ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc')

            // 1) compile the Java source code only
            ant.groovyc(srcdir: 'src', destdir: 'build', includes: 'p2/*' /*, keepStubs: true*/) {
                javac()
            }

            // 2) delete `Gson`, `SubGson`, `GsonAnnotation` related files:
            // "Gson.java", "Gson.class", "SubGson.java", "SubGson.class", "GsonAnnotation.java", "GsonAnnotation.class"
            assert new File(ant.project.baseDir,"src/p2/Gson.java").delete()
            assert new File(ant.project.baseDir,"build/p2/Gson.class").delete()
            assert new File(ant.project.baseDir,"src/p2/SubGson.java").delete()
            assert new File(ant.project.baseDir,"build/p2/SubGson.class").delete()
            assert new File(ant.project.baseDir,"src/p2/GsonAnnotation.java").delete()
            assert new File(ant.project.baseDir,"build/p2/GsonAnnotation.class").delete()

            // 3) compile the Groovy source code
            ant.groovyc(srcdir: 'src', destdir: 'build', includes: 'p1/*'){
                classpath { pathelement(path: 'build') }
            }
        }
    }
}
