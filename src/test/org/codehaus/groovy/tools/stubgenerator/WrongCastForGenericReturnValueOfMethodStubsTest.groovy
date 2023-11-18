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
 * GROOVY-5630: stub generator inserted wrong cast for generic method return
 *
 * (also covers GROOVY-5439)
 */
final class WrongCastForGenericReturnValueOfMethodStubsTest extends StringSourcesStubTestCase  {

    Map<String, String> provideSources() {
        [
            'Task.java': '''
                public class Task {
                }
            ''',

            'Schedule.groovy': '''
                class Schedule<T extends ScheduleItem> extends HashSet<T> {
                    T getCurrentItem() {
                    }
                }
            ''',

            'ScheduleItem.java': '''
                public class ScheduleItem {
                }
            ''',

            'Utility.groovy': '''
                class Utility {
                    final Map<String, String> test = new HashMap<String, String>()
                    static <T extends Task> T createTask(Class<T> type) { }
                    public <T extends List> T foo() { null }
                }
            '''
        ]
    }

    void verifyStubs() {
        String stub = stubJavaSourceFor('Schedule')
        assert stub.contains("T getCurrentItem() { return null; }")

               stub = stubJavaSourceFor('Utility')
        assert stub.contains("static <T extends Task> T createTask(java.lang.Class<T> type) { return null; }")
        assert stub.contains("java.util.Map<java.lang.String, java.lang.String> getTest() { return null; }")
        assert stub.contains("<T extends java.util.List> T foo() { return null; }")

    }
}
