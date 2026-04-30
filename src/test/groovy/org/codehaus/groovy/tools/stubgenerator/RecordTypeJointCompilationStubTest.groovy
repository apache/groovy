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
 * GROOVY-11974: native records in joint compilation must produce a stub
 * that javac compiles as a record, so that Java callers see the canonical
 * constructor and the {@code componentName()} accessors.
 */
class RecordTypeJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    protected void configure() {
        super.configure()
        config.targetBytecode = '17'
    }

    Map<String, String> provideSources() {
        [
                'JavaCaller.java': '''
                    public class JavaCaller {
                        public static int sumViaRecordAccessors() {
                            Point p = new Point(3, 4);
                            return p.x() + p.y();
                        }
                        public static String describe() {
                            return new Point(1, 2).getClass().isRecord()
                                ? "record" : "class";
                        }
                    }
                ''',
                'Point.groovy': '''
                    record Point(int x, int y) {}
                '''
        ]
    }

    void verifyStubs() {
        def stub = stubJavaSourceFor('Point')
        // header is rendered as record syntax, not class
        assert stub =~ /\brecord\s+Point\s*\(\s*int\s+x\s*,\s*int\s+y\s*\)/
        // no `class Point`, no `extends`
        assert !(stub =~ /\bclass\s+Point\b/)
        assert !(stub =~ /\bextends\s+java\.lang\.Object\b/)
        // no instance-field declarations
        assert !(stub =~ /(?m)^\s*(?:private|protected|public)?\s*int\s+x\s*;/)
    }
}
