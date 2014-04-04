/*
 * Copyright 2003-2014 the original author or authors.
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

package org.codehaus.groovy.transform

import gls.CompilableTestSupport

class SortableTransformTest extends CompilableTestSupport {
    void testSortableWithCustomOrdering() {
        assertScript '''
            import groovy.transform.Sortable
            def people = [
              [first: 'Bart', last: 'Simpson', bestFriend: 'Milhouse'],
              [first: 'Lisa', last: 'Simpson', bestFriend: 'Janey']
            ]

            @Sortable(includes='last,first') class PersonLF { String first, last, bestFriend }
            def peopleLF = people.collect{ new PersonLF(*:it) }
            assert peopleLF.sort()*.first == ['Bart', 'Lisa']

            @Sortable(includes='last,bestFriend') class PersonLB { String first, last, bestFriend }
            def peopleLB = people.collect{ new PersonLB(*:it) }
            assert peopleLB.sort()*.first == ['Lisa', 'Bart']
        '''
    }

    void testSortableWithSortableProperty() {
        assertScript '''
            import groovy.transform.*
            @Canonical @Sortable(includes=['first', 'favoriteEvent']) class Person {
              String first
              String last
              Event favoriteEvent
            }
            @Immutable @Sortable class Event {
              String description
              Date when
            }
            def now = new Date()
            def people = [
              new Person('Groovy', 'Hacker1', new Event('discover @Sortable', now)),
              new Person('Groovy', 'Hacker2', new Event('discover @Sortable', now+1)),
              new Person('Groovy', 'Hacker3', new Event('discover @Sortable', now-1))
            ]
            assert people.sort()*.last == ['Hacker3', 'Hacker1', 'Hacker2']
        '''
    }

    void testBadIncludesAndExcludes() {
        def message = shouldFail '''
            @groovy.transform.Sortable(includes='first', excludes='last') class Person {
              String first
              String last
              Integer born
            }
        '''
        assert message.contains("Error during @Sortable processing: Only one of 'includes' and 'excludes' should be supplied not both")
    }

    void testBadInclude() {
        def message = shouldFail '''
            @groovy.transform.Sortable(includes='first,middle') class Person {
              String first
              String last
              Integer born
            }
        '''
        assert message.contains("Error during @Sortable processing: tried to include unknown property 'middle'")
    }

    void testBadPropertyType() {
        def message = shouldFail '''
            @groovy.transform.Sortable class Person {
              String first
              String last
              List<String> appearances
            }
        '''
        assert message.contains("Error during @Sortable processing: property 'appearances' must be Comparable")
    }

    void testBadInterfaceUsage() {
        def message = shouldFail '''
            @groovy.transform.Sortable interface Foo { }
        '''
        assert message.contains("@Sortable cannot be applied to interface Foo")
    }
}
