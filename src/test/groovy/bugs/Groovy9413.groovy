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

final class Groovy9413 {
// .\gradlew --no-daemon --max-workers 2 :test --tests groovy.bugs.Groovy9413 --debug-jvm
    @Test
    void testInterfaceGenerics() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                import java.util.Map;

                interface APSBus {
                  void send(String target, Map<String, Object> message, APSHandler<APSResult<?>> resultHandler);
                }

                interface APSBusRouter {
                  boolean send(String target, Map<String, Object> message, APSHandler<APSResult<?>> resultHandler);
                }

                interface APSHandler<T> {
                  void handle(T value);
                }

                interface APSResult<T> {
                }

                class APSServiceTracker<Service> {
                  void withService(WithService<Service> withService, Object... args) throws Exception {
                  }
                }

                interface WithService<Service> {
                  void withService(Service service, Object... args) throws Exception;
                }
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
                @groovy.transform.CompileStatic
                class One9413 implements APSBus {

                  private APSServiceTracker<APSBusRouter> busRouterTracker

                  @Override
                  void send(String target, Map<String, Object> message, APSHandler<APSResult<?>> resultHandler) {
                    busRouterTracker.withService() { APSBusRouter busRouter ->
                      busRouter.send(target, message, resultHandler)
                    }
                  }
                }

                @groovy.transform.CompileStatic
                class Two9413 implements APSBusRouter {
                  @Override
                  boolean send(String target, Map<String, Object> payload, APSHandler<APSResult<?>> resultHandler) {
                  }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
