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
 * Captures the joint-compilation surface for {@code @NamedVariant}.
 *
 * <p>The stubber emits a placeholder Map-arg variant alongside the
 * user-declared method or constructor; the full transform replaces the
 * body with the real delegation logic at SEMANTIC_ANALYSIS.
 *
 * <p>Originally listed in Tier 3 because a target method "added by
 * another transform" wouldn't be visible at CONVERSION. In practice,
 * direct {@code @NamedVariant} sits on a hand-written method, where the
 * target is fully visible at CONVERSION; transforms compose this kind of
 * variant via {@code @NamedParam} on parameters, not by adding a
 * {@code @NamedVariant}-annotated method. This test exercises the
 * common direct-use case for both methods and constructors.
 */
final class NamedVariantJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Calc.groovy': '''
                package foo

                class Calc {
                    @groovy.transform.NamedVariant
                    static int makeSense(int dollars, int cents) {
                        100 * dollars + cents
                    }

                    @groovy.transform.NamedVariant
                    Calc(String label, int seed) {
                        this.label = label
                        this.seed = seed
                    }

                    String label
                    int seed
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.util.HashMap;
                import java.util.Map;

                public class JavaUser {
                    public static int viaMap() {
                        Map<String, Object> args = new HashMap<>();
                        args.put("dollars", 2);
                        args.put("cents", 50);
                        return Calc.makeSense(args);
                    }

                    public static Calc buildViaMap() {
                        Map<String, Object> args = new HashMap<>();
                        args.put("label", "alpha");
                        args.put("seed", 42);
                        return new Calc(args);
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: the Map-arg variant is declared for both method and constructor.
        String stub = stubJavaSourceFor('foo.Calc')
        assert stub =~ /static\s+int\s+makeSense\(\s*java\.util\.Map\s+\w+\s*\)/
        assert stub =~ /public\s+Calc\s*\(\s*java\.util\.Map\s+\w+\s*\)/

        // The original positional forms remain visible.
        assert stub =~ /static\s+int\s+makeSense\(\s*int\s+\w+\s*,\s*int\s+\w+\s*\)/
        assert stub =~ /public\s+Calc\s*\(\s*java\.lang\.String\s+\w+\s*,\s*int\s+\w+\s*\)/

        // Runtime view: real bodies installed and produce expected results.
        Class calcClass = loader.loadClass('foo.Calc')

        // Method via map.
        int result = (int) calcClass.getMethod('makeSense', Map).invoke(null, [dollars: 2, cents: 50])
        assert result == 250
        // Method via positional.
        int direct = (int) calcClass.getMethod('makeSense', int, int).invoke(null, 1, 25)
        assert direct == 125

        // Constructor via map.
        def viaMap = calcClass.getConstructor(Map).newInstance([label: 'alpha', seed: 42])
        assert viaMap.label == 'alpha'
        assert viaMap.seed == 42
        // Constructor via positional.
        def direct2 = calcClass.getConstructor(String, int).newInstance('beta', 7)
        assert direct2.label == 'beta'
        assert direct2.seed == 7
    }
}
