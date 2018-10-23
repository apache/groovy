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
package groovy

/**
 * Tests Groovy's regular expression syntax and DGM methods.
 *
 * @author Sam Pullara
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Pilho Kim
 * @author Graham Miller
 * @author Paul King
 * @author Ted Naleid
 */

import java.util.regex.Matcher
import java.util.regex.Pattern

class RegularExpressionsTest extends GroovyTestCase {

    void testMatchOperator() {
        assert "cheese" ==~ "cheese"
        assert !("cheesecheese" ==~ "cheese")
    }

    // The find operator is: =~
    void testFindOperator() {
        assert "cheese" =~ "cheese"

        def regex = "cheese"
        def string = "cheese"
        assert string =~ regex

        def i = 0
        def m = "cheesecheese" =~ "cheese"

        assert m instanceof Matcher

        while (m.find()) { i = i + 1 }
        assert i == 2

        i = 0
        m = "cheesecheese" =~ "e+"
        while (m.find()) { i = i + 1 }
        assert i == 4

        m.reset()
        m.find()
        m.find()
        m.find()
        assert m.group() == "ee"
    }
    
    // From the javadoc of the getAt() method     
    void testMatcherWithIntIndex() {
        def p = /ab[d|f]/
        def m = "abcabdabeabf" =~ p
        assert 2 == m.count
        assert 2 == m.size() // synonym for m.getCount()
        assert ! m.hasGroup()
        assert 0 == m.groupCount()
        def matches = ["abd", "abf"]
        for (i in 0 ..< m.count) {
            assert m[i] == matches[i]
        }

        p = /(?:ab([c|d|e|f]))/
        m = "abcabdabeabf" =~ p
        assert 4 == m.count
        assert m.hasGroup()
        assert 1 == m.groupCount()
        matches = [["abc", "c"], ["abd", "d"], ["abe", "e"], ["abf", "f"]]
        for (i in 0 ..< m.count) {
            assert m[i] == matches[i]
        }

        m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/
        assert 3 == m.count
        assert m.hasGroup()
        assert 1 == m.groupCount()
        matches = [["abd", "d"], ["abxyz", "xyz"], ["abx", "x"]]
        for (i in 0 ..< m.count) {
            assert m[i] == matches[i]
        }
    }

    void testMatcherWithIndexAndRanges() {
        def string = "cheesecheese"
        def matcher = string =~ "e+"

        assert "ee" == matcher[2]
        assert ["ee", "e"] == matcher[2..3] 
        assert ["ee", "ee"] == matcher[0, 2]
        assert ["ee", "e", "ee"] == matcher[0, 1..2]
        
        matcher = "cheese please" =~ /([^e]+)e+/
        assert ["se", "s"] == matcher[1]
        assert [["se", "s"], [" ple", " pl"]] == matcher[1, 2]
        assert [["se", "s"], [" ple", " pl"]] == matcher[1 .. 2]
        assert [["chee", "ch"], [" ple", " pl"], ["ase", "as"]] == matcher[0, 2..3]
        
        matcher = "cheese please" =~ /([^e]+)e+/
        shouldFail { matcher[0, [1, 2]] }
    }
    
    void testMatcherIterator() {
        def matcher = "cheesecheese" =~ "e+"
        def iter = matcher.iterator()
        assert iter instanceof Iterator
        assert iter.hasNext()
        assert "ee" == iter.next()
        assert iter.hasNext()
        assert "e" == iter.next()
        assert iter.hasNext()
        assert "ee" == iter.next()
        assert iter.hasNext()
        assert "e" == iter.next()
        assert ! iter.hasNext()
        shouldFail(NoSuchElementException.class, { iter.next() })

        matcher = "cheese please" =~ /([^e]+)e+/
        iter = matcher.iterator()
        assert iter instanceof Iterator
        assert iter.hasNext()
        assert ["chee", "ch"] == iter.next()
        assert iter.hasNext()
        assert ["se", "s"] == iter.next()
        assert iter.hasNext()
        assert [" ple", " pl"] == iter.next()
        assert iter.hasNext()
        assert ["ase", "as"] == iter.next()
        assert ! iter.hasNext()
        shouldFail(NoSuchElementException.class, { iter.next() })
        
        // collect() uses iterator
        matcher = "cheesecheese" =~ "e+"
        assert ["ee", "e", "ee", "e"] == matcher.collect { it }

        matcher = "cheese please" =~ /([^e]+)e+/
        assert [["chee", "ch"], ["se", "s"], [" ple", " pl"], ["ase", "as"]] == matcher.collect { it }
    }

    void testMatcherEach() {
        def count = 0
        def result = []
        ("cheesecheese" =~ "cheese").each {value -> result += value; count = count + 1}
        assert count == 2
        assert result == ['cheese', 'cheese']

        count = 0
        result = []
        ("cheesecheese" =~ "ee+").each { result += it; count = count + 1}
        assert count == 2
        assert result == ['ee', 'ee']

        def matcher = "cheese please" =~ /([^e]+)e+/
        def resultAll = []
        def resultGroup = []
        matcher.each { a, g ->
            resultAll << a
            resultGroup << g
        }
        assert ["chee", "se", " ple", "ase"] == resultAll
        assert ["ch", "s", " pl", "as"] == resultGroup
        
        matcher = "cheese please" =~ /([^e]+)e+/
        result = []
        matcher.each { result << it }
        assert [["chee", "ch"], ["se", "s"], [" ple", " pl"], ["ase", "as"]] == result
    }
        
    // Check consistency between each and collect
    void testMatcherEachVsCollect() {
        def matcher = "cheese cheese" =~ "e+"
        def result = []
        matcher.each { result << it }
        matcher.reset()
        assert result == matcher.collect { it }

        matcher = "cheese please" =~ /([^e]+)e+/
        result = []
        matcher.each { result << it }
        matcher.reset()
        assert result == matcher.collect { it }

        matcher = "cheese please" =~ /([^e]+)e+/
        result = []
        matcher.each { a, g -> result << a }
        matcher.reset()
        assert result == matcher.collect { a, g -> a }

        matcher = "cheese please" =~ /([^e]+)e+/
        result = []
        matcher.each { a, g -> result << g }
        matcher.reset()
        assert result == matcher.collect { a, g -> g }
    }

    void testSimplePattern() {
        def pattern = ~"foo"
        assert pattern instanceof Pattern
        assert pattern.matcher("foo").matches()
        assert !pattern.matcher("bar").matches()
    }

    void testMultiLinePattern() {
        def pattern = ~"""foo"""
        assert pattern instanceof Pattern
        assert pattern.matcher("foo").matches()
        assert !pattern.matcher("bar").matches()
    }

    void testPatternInAssertion() {
        assert "foofoofoo" =~ ~"foo"
    }


    void testMatcherWithReplace() {
        def matcher = "cheese-cheese" =~ "cheese"
        def answer = matcher.replaceAll("edam")
        assert answer == 'edam-edam'

        def cheese = ("cheese cheese!" =~ "cheese").replaceFirst("nice")
        assert cheese == "nice cheese!"
    }

    void testGetLastMatcher() {
        assert "cheese" ==~ "cheese"
        assert Matcher.getLastMatcher().matches()

        switch ("cheesefoo") {
            case ~"cheesecheese":
                assert false;
                break;
            case ~"(cheese)(foo)":
                def m = Matcher.getLastMatcher();
                assert m.group(0) == "cheesefoo"
                assert m.group(1) == "cheese"
                assert m.group(2) == "foo"
                assert m.groupCount() == 2
                break;
            default:
                assert false
        }
    }

    void testRyhmeMatchGina() {
        def myFairStringy = 'The rain in Spain stays mainly in the plain!'
        // words, that end with 'ain': \b\w*ain\b
        def BOUNDS = /\b/
        def rhyme = /$BOUNDS\w*ain$BOUNDS/
        def found = ''
        myFairStringy.eachMatch(rhyme) {match ->
            found += match + ' '
        }
        assert found == 'rain Spain plain '
        // a second way that is equivalent
        found = ''
        (myFairStringy =~ rhyme).each {match ->
            found += match + ' '
        }
        assert found == 'rain Spain plain '
    }

    void testEachMatchWithPattern() {
        def compiledPattern = ~/.at/
        def result = []
        "The cat sat on the hat".eachMatch(compiledPattern) { result << it }
        assert "cat sat hat" == result.join(" ")
    }

    void testPatternVersionsOfStringRegexMethods() {
        def compiledPattern = ~/.at/
        def s = "The cat sat on the hat"
        assert "bat".matches(compiledPattern)
        assert s.replaceFirst(compiledPattern, 'x') == "The x sat on the hat"
        assert s.replaceAll(compiledPattern, 'x') == "The x x on the x"
    }

    void testFindOperatorCollect() {
        def m = 'coffee' =~ /ee/
        def result = ''
        m.each { result += it }
        assert result == 'ee'
        result = ''
        m.each { result += it }
        assert result == 'ee'
        m.reset()
        result = ''
        m.each { result += it }
        assert result == 'ee'

        m = 'reek coffee' =~ /ee/
        assert m.collect { it }.join(',') == 'ee,ee'
        assert m.collect { it }.join(',') == 'ee,ee'
        m.reset()
        assert m.collect { it }.join(',') == 'ee,ee'
    }

    void testIteration() {
        def string = 'a:1 b:2 c:3'
        def result = []
        def letters = ''
        def numbers = ''
        string.eachMatch(/([a-z]):(\d)/) {full, group1, group2 ->
            result += full
            letters += group1
            numbers += group2
        }
        assert result == ['a:1', 'b:2', 'c:3']
        assert letters == 'abc'
        assert numbers == '123'

        result = []
        letters = ''
        numbers = ''
        def matcher = string =~ /([a-z]):(\d)/
        matcher.each {match ->
            result += match[0]
            letters += match[1]
            numbers += match[2]
        }
        assert result == ['a:1', 'b:2', 'c:3']
        assert letters == 'abc'
        assert numbers == '123'
    }

    void testFirst() {
        assert "cheesecheese" =~ "cheese"
        assert "cheesecheese" =~ /cheese/
        assert "cheese" == /cheese/   /*they are both string syntaxes*/
    }

    void testSecond() {
        // Let's create a regex Pattern
        def pattern = ~/foo/
        assert pattern instanceof Pattern
        assert pattern.matcher("foo").matches()
    }

    void testThird() {
        // Let's create a Matcher
        def matcher = "cheesecheese" =~ /cheese/
        assert matcher instanceof Matcher
        def answer = matcher.replaceAll("edam")
        assert answer == "edamedam"
    }

    void testFourth() {
        // Let's do some replacement
        def cheese = ("cheesecheese" =~ /cheese/).replaceFirst("nice")
        assert cheese == "nicecheese"
    }

    void testFifth() {
        def matcher = "\$abc." =~ "\\\$(.*)\\."
        matcher.matches();                   // must be invoked
        assert matcher.group(1) == "abc"     // is one, not zero
        assert matcher[0] == ["\$abc.", "abc"]
        assert matcher[0][1] == "abc"
    }

    void testSixth() {
        def matcher = "\$abc." =~ /\$(.*)\./    // no need to double-escape!
        assert "\\\$(.*)\\." == /\$(.*)\./
        matcher.matches();                      // must be invoked
        assert matcher.group(1) == "abc"        // is one, not zero
        assert matcher[0] == ["\$abc.", "abc"]
        assert matcher[0][1] == "abc"
    }

    void testReplaceWithClosure() {
        assert '1-FISH, two fish' == "one fish, two fish".replaceFirst(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
        assert '1-FISH, 2-FISH' == "one fish, two fish".replaceAll(~/([a-z]{3})\s([a-z]{4})/) { [one:1, two:2][it[1]] + '-' + it[2].toUpperCase() }
    }

    void testNoGroupMatcherAndGet() {
        def p = /ab[d|f]/
        def m = "abcabdabeabf" =~ p
        def result = []

        for (i in 0..<m.count) {
            assert m.groupCount() == 0 // no groups
            result += "$i:${m[i]}"
        }
        assert result == ['0:abd', '1:abf']
    }

    void testFindWithOneGroupAndGet() {
        def p = /(?:ab([c|d|e|f]))/
        def m = "abcabdabeabf" =~ p
        def result = []

        for (i in 0..<m.count) {
            assert m.groupCount() == 1
            result += "$i:${m[i]}"
        }
        assert result == ['0:[abc, c]', '1:[abd, d]', '2:[abe, e]', '3:[abf, f]']
    }

    void testAnotherOneGroupMatcherAndGet() {
        def m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/
        def result = []

        m.count.times {
            assert m.groupCount() == 1
            result += "$it:${m[it]}"
        }
        assert result == ['0:[abd, d]', '1:[abxyz, xyz]', '2:[abx, x]']
    }
    
    void replaceAllHelper(p, c) {
        assert 'x12345' == 'x123z45'.replaceAll(p, c)
        assert '12345' == '12345z'.replaceAll(p, c)
        assert '12345' == 'z12345z'.replaceAll(p, c)
        assert '12345' == 'z12z345z'.replaceAll(p, c)
        assert '12345' == 'zz12z3zz45z'.replaceAll(p, c)
        assert '12345' == 'z12z3zzz45z'.replaceAll(p, c)
        assert '12345' == '12345'.replaceAll(p, c)
        assert '$1$2345' == 'z$1$2345'.replaceAll(p, c)
        assert '1$2345$' == 'z1$2345$'.replaceAll(p, c)
        assert '2345\\' == '23z45zz\\'.replaceAll(p, c)
        assert 'x1\\2345' == 'x1\\23z4zz5'.replaceAll(p, c)
        assert '\\2345' == '\\23z45zzz'.replaceAll(p, c)
        assert '\\2345\\' == '\\23z45\\'.replaceAll(p, c)
        assert '\\23\\\\45\\' == '\\23\\\\45\\'.replaceAll(p, c)
        assert '$\\23\\$\\45\\' == '$\\23\\$\\45\\'.replaceAll(p, c)
    }
    
    void testReplaceAllClosure() {
        def p = /([^z]*)(z)/
        def c = { all, m, d -> m }
        replaceAllHelper(p, c)
    }

    void testReplaceAllClosureWithIt() {
        def p = /([^z]*)(z)/
        def c = { it[1] }
        replaceAllHelper(p, c)
    }

    void testReplaceAllClosureWithObjectArray() {
        def p = /([^z]*)(z)/
        def c = { Object[] a -> a[1] }
        replaceAllHelper(p, c)
    }

    void testFind() {
        def p = /.ar/
        assert null == 'foo foo baz'.find(p)
        assert 'bar' == 'foo bar baz'.find(p)
        assert 'car' == 'car'.find(p)
        
        def patternWithGroups = /(.)ar/
        assert null == ''.find(patternWithGroups)
        assert 'bar' == 'foo bar baz'.find(patternWithGroups)

        def compiledPattern = ~/(.)ar/
        assert null == ''.find(compiledPattern)
        assert 'bar' == 'foo bar baz'.find(compiledPattern)
    }

    void testFindClosureNoGroups() {
        def p = /.ar/
        def c = { match -> return "-$match-" }
        assert null == 'foo foo baz'.find(p, c)
        assert '-bar-' == 'foo bar baz'.find(p, c)
        assert '-car-' == 'car'.find(p, c)          

        def compiledPattern = ~p
        assert null == 'foo foo baz'.find(compiledPattern, c)
        assert '-bar-' == 'foo bar baz'.find(compiledPattern, c)
        assert '-car-' == 'car'.find(compiledPattern, c)
    }
    
    void testFindClosureWithGroups() {
        def AREA_CODE = /\d{3}/
        def EXCHANGE = /\d{3}/
        def STATION_NUMBER = /\d{4}/
        def phone = /($AREA_CODE)-($EXCHANGE)-($STATION_NUMBER)/

        def c = { match, areaCode, exchange, stationNumber -> 
            return "($areaCode) $exchange-$stationNumber" 
        }            
        
        assert null == 'foo'.find(phone) { match, areaCode, exchange, stationNumber -> return match } 
        assert "612-555-1212" == 'foo 612-555-1212 bar'.find(phone) { match, areaCode, exchange, stationNumber -> return match } 
        assert "(612) 555-1212" == 'foo 612-555-1212 bar'.find(phone, c) 

        def compiledPhonePattern = ~phone
        assert "(888) 555-1212" == "bar 888-555-1212 foo".find (compiledPhonePattern, c)

        def closureSingleVar = { matchArray -> 
            return "(${matchArray[1]}) ${matchArray[2]}-${matchArray[3]}" 
        }
    
        assert "(888) 555-1212" == "bar 888-555-1212 foo".find (compiledPhonePattern, closureSingleVar)
    }    

    void testFindAll() {
        def p = /.at/
        def compiledPattern = ~p
        assert [] == "".findAll(p)
        assert [] == "".findAll(compiledPattern)
    
        def orig = "The cat sat on the hat"
        assert ["cat", "sat", "hat"] == orig.findAll(p)
        assert ["cat", "sat", "hat"] == orig.findAll(compiledPattern)
        assert ["+cat", "+sat", "+hat"] == orig.findAll(p) { "+$it" }
        assert ["+cat", "+sat", "+hat"] == orig.findAll(compiledPattern) { "+$it" }
    }    

    void testFindAllWithGroups() {
        def p = /(.)a(.)/
        def compiledPattern = ~p
        def orig = "The cat sat on the hat"
        assert ["cat", "sat", "hat"] == orig.findAll(p)
        assert ["cat", "sat", "hat"] == orig.findAll(compiledPattern)
        
        def c = { match, firstLetter, lastLetter -> return  "$firstLetter+$match+$lastLetter" }
        assert ["c+cat+t", "s+sat+t", "h+hat+t"] == orig.findAll(p, c) 
        assert ["c+cat+t", "s+sat+t", "h+hat+t"] == orig.findAll(compiledPattern, c)

        def closureSingleVar = { matchArray -> return "${matchArray[1]}+${matchArray[0]}+${matchArray[2]}" }
        
        assert ["c+cat+t", "s+sat+t", "h+hat+t"] == orig.findAll(p, closureSingleVar)
        assert ["c+cat+t", "s+sat+t", "h+hat+t"] == orig.findAll(compiledPattern, closureSingleVar)
    }

    void testMatchesPartially() {

        def pattern = /\w+@\w+\.\w{2,}/

        def useCases = [
                "glaforge@gmail.com"    : true,
                "glaforge"              : true,
                "!!!!"                  : false,
                "glaforge@"             : true,
                "glaforge@@"            : false
        ]

        useCases.each { String email, boolean bool ->
            def matcher = email =~ pattern

            assert matcher.matchesPartially() == bool
        }
    }
}
