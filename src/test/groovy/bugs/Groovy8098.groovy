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
package bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

/**
 * GROOVY-8098: a Groovy class that extends a Java generic repository
 * and implements the specialized interface must dispatch call(Integer)
 * and call(String) like the equivalent Java subclass.
 */
final class Groovy8098 {

    @Test
    void testOverloadDispatchOnGroovySubclassOfGenericJavaRepo() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [
                stubDir: File.createTempDir(),
                keepStubs: true
            ]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'BaseRepository.java').write '''
                import java.io.Serializable;
                public interface BaseRepository<T, S extends Serializable> {
                    void call(T t);
                    void call(S s);
                }
            '''
            new File(parentDir, 'BaseSpecializedRepo.java').write '''
                public interface BaseSpecializedRepo extends BaseRepository<Integer, String> {
                }
            '''
            new File(parentDir, 'InMemoryBaseRepo.java').write '''
                import java.io.Serializable;
                import java.util.List;
                public class InMemoryBaseRepo<T, S extends Serializable> implements BaseRepository<T, S> {
                    public final List<String> log;
                    public InMemoryBaseRepo(List<String> log) { this.log = log; }
                    public void call(T t) { log.add("T"); }
                    public void call(S s) { log.add("S"); }
                }
            '''
            new File(parentDir, 'InMemorySpecializedJavaRepo.java').write '''
                import java.util.List;
                public class InMemorySpecializedJavaRepo extends InMemoryBaseRepo<Integer, String> implements BaseSpecializedRepo {
                    public InMemorySpecializedJavaRepo(List<String> log) { super(log); }
                }
            '''
            new File(parentDir, 'InMemorySpecializedGroovyRepo.groovy').write '''
                import java.util.List
                class InMemorySpecializedGroovyRepo extends InMemoryBaseRepo<Integer, String> implements BaseSpecializedRepo {
                    InMemorySpecializedGroovyRepo(List log) { super(log) }
                }
            '''
            new File(parentDir, 'AppRunner.java').write '''
                import java.util.ArrayList;
                import java.util.List;
                public class AppRunner {
                    public static List<String> runJava() {
                        List<String> log = new ArrayList<String>();
                        BaseSpecializedRepo repo = new InMemorySpecializedJavaRepo(log);
                        repo.call(Integer.valueOf(0));
                        repo.call(new String("empty"));
                        return log;
                    }
                    public static List<String> runGroovy() {
                        List<String> log = new ArrayList<String>();
                        BaseSpecializedRepo repo = new InMemorySpecializedGroovyRepo(log);
                        repo.call(Integer.valueOf(0));
                        repo.call(new String("empty"));
                        return log;
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(parentDir.listFiles())
            cu.compile()

            def runner = loader.loadClass('AppRunner')
            assert runner.runJava() == ['T', 'S']
            assert runner.runGroovy() == ['T', 'S']
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
