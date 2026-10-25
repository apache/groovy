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
package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-12426: @Delegate of an interface whose methods use a static nested
 * class of a joint-compiled Java source must generate those methods, and must
 * not break other classes implementing the interface directly.
 */
final class Groovy12426 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'Payload.java': '''
                public class Payload {
                    public static abstract class Result {
                        public abstract String text();
                    }
                    public static class Args {
                        public String name = "args";
                    }
                }
            ''',
            'Renderer.groovy': '''
                interface Renderer {
                    Payload.Result render(Map arguments)
                    String describe(Payload.Args args)
                }
            ''',
            'DelegatingRenderer.groovy': '''
                class DelegatingRenderer {
                    @Delegate final Renderer renderer
                    DelegatingRenderer(Renderer renderer) { this.renderer = renderer }
                }
            ''',
            'ForwardingRenderer.groovy': '''
                class ForwardingRenderer {
                    @Delegate final Renderer renderer
                    ForwardingRenderer(Renderer renderer) { this.renderer = renderer }
                    Payload.Result render(Map arguments) { renderer.render(arguments) }
                }
            ''',
            'DirectRenderer.groovy': '''
                class DirectRenderer implements Renderer {
                    Payload.Result render(Map arguments) {
                        new Payload.Result() { String text() { arguments.text } }
                    }
                    String describe(Payload.Args args) { args.name }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('DelegatingRenderer')
        assert stub =~ /Payload\.Result\s+render\(java\.util\.Map/
        assert stub =~ /describe\(Payload\.Args/

        def direct = loader.loadClass('DirectRenderer').getDeclaredConstructor().newInstance()
        def args = loader.loadClass('Payload$Args').getDeclaredConstructor().newInstance()
        for (name in ['DelegatingRenderer', 'ForwardingRenderer']) {
            def renderer = loader.loadClass(name).getDeclaredConstructor(loader.loadClass('Renderer')).newInstance(direct)
            assert renderer.render(text: 'hello').text() == 'hello'
            assert renderer.describe(args) == 'args'
        }
    }
}
