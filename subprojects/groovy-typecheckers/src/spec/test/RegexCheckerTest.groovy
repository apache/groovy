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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

class RegexCheckerTest {

    @Test
    void testIntro() {
        // tag::intro_example[]
        assert 'foo'.matches(/[Ff].{2}\b/)
        // end::intro_example[]
    }

    @Test
    void testCheckedIntro() {
        assertScript($/
        import groovy.transform.TypeChecked

        // tag::checked_example[]
        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        static main(args) {
            assert 'foo'.matches(/[Ff].{2}\b/)
        }
        // end::checked_example[]
        /$)
    }

    @Test
    void testIntroduction() {
        assertScript($/
        import groovy.transform.TypeChecked
        import java.util.regex.Pattern

        // tag::introduction_example[]
        static final String TWO_GROUPS_OF_THREE = /(...)(...)/  // <1>

        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        static main(args) {
            var m = 'foobar' =~ TWO_GROUPS_OF_THREE  //  <2>
            assert m.find()
            assert m.group(1) == 'foo'
            assert m.group(2) == 'bar'  // <3>

            var pets = ['cat', 'dog', 'goldfish']
            var shortNamed = ~/\w{3}/  // <4>
            assert pets.grep(shortNamed) == ['cat', 'dog']  // <5>

            assert Pattern.matches(/(\d{4})-(\d{1,2})-(\d{1,2})/, '2020-12-31')  // <6>
        }
        // end::introduction_example[]
        /$)
    }

    @Test
    void testGuardedRegex() {
        assertScript($/
        import groovy.transform.SafeRegex
        import groovy.transform.TypeChecked
        import groovy.util.regex.RegexGuard

        // tag::guarded_example[]
        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        @SafeRegex(millis = 200)
        static main(args) {
            var m = 'foobar' =~ /(...)(...)/     // guarded by @SafeRegex, still checked
            assert m[0][1] == 'foo'              // group inference still applies
            assert m[0][2] == 'bar'

            assert RegexGuard.matches(/(\d{4})-(\d{1,2})-(\d{1,2})/, '2020-12-31', 200)
        }
        // end::guarded_example[]
        /$)
    }

    @Test
    void testFindGroupsArity() {
        def err = shouldFail($/
        import groovy.transform.TypeChecked

        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        static main(args) {
            // tag::findgroups_arity[]
            def semver = /(\d+)\.(\d+)\.(\d+)(?:-(.+))?/

            def (all, major, minor, patch, qualifier) = '6.0.0-beta-2'.findGroups(semver)  // OK

            def (a, b, c, d, e, f) = '6.0.0-beta-2'.findGroups(semver)  // one variable too many
            // end::findgroups_arity[]
        }
        /$)
        def expectedError = '''\
        # tag::findgroups_arity_message[]
        [Static type checking] - Too many variables 6 for findGroups: a regex with 4 groups yields at most 5 elements (the full match plus each group)
        # end::findgroups_arity_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testInvalidGroupCount() {
        def err = shouldFail($/
        import groovy.transform.TypeChecked

        static final String TWO_GROUPS_OF_THREE = /(...)(...)/

        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        static main(args) {
            var m = 'foobar' =~ TWO_GROUPS_OF_THREE
            assert m.find()
            // tag::invalid_group_count[]
            assert m.group(3) == 'bar'
            // end::invalid_group_count[]
        }
        /$)
        def expectedError = '''\
        # tag::invalid_group_count_message[]
        [Static type checking] - Invalid group count 3 for regex with 2 groups
        # end::invalid_group_count_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testUnclosedCountedClosure() {
        def err = shouldFail($/
        import groovy.transform.TypeChecked

        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        static main(args) {
            var pets = ['cat', 'dog', 'goldfish']
            // tag::unclosed_counted_closure[]
            var shortNamed = ~/\w{3/
            // end::unclosed_counted_closure[]
        }
        /$)
        def expectedError = '''\
        # tag::unclosed_counted_closure_message[]
        [Static type checking] - Bad regex: Unclosed counted closure near index 4
        # end::unclosed_counted_closure_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testUnclosedGroup() {
        def err = shouldFail($/
        import groovy.transform.TypeChecked
        import java.util.regex.Pattern
import static groovy.test.GroovyAssert.*

        @TypeChecked(extensions='groovy.typecheckers.RegexChecker')
        static main(args) {
            // tag::unclosed_group[]
            assert Pattern.matches(/(\d{4})-(\d{1,2})-(\d{1,2}/, '2020-12-31')
            // end::unclosed_group[]
        }
        /$)
        def expectedError = '''\
        # tag::unclosed_group_message[]
        [Static type checking] - Bad regex: Unclosed group near index 26
        # end::unclosed_group_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

}
