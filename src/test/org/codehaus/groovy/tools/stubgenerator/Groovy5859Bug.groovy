/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.stubgenerator

class Groovy5859Bug extends StringSourcesStubTestCase {
    @Override
    Map<String, String> provideSources() {
        ['TaggedsMap.groovy': '''class TaggedsMap extends TreeMap {

    TaggedsMap() { super() }

    TaggedsMap(Comparator comparator) { super(comparator)}

    TaggedsMap(Map m) {
        super()
        putAll( m)
    }

    TaggedsMap(SortedMap m) {
        super()
        putAll (m)
    }
}''',
        'Blah.java': '''public class Blah { TaggedsMap map; }''']
    }

    @Override
    void verifyStubs() {
        def stubSource = stubJavaSourceFor('TaggedsMap')
        assert stubSource.contains('super ((java.util.SortedMap)null);')
    }
}
