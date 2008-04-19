package groovy

/**
 * Tests the regular expression syntax.
 *
 * @author Sam Pullara
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Pilho Kim
 * @author Graham Miller
 * @author Paul King
 * @version $Revision$
 */

import java.util.regex.Matcher
import java.util.regex.Pattern

class RegularExpressionsTest extends GroovyTestCase {

    void testMatchOperator() {
        assert "cheese" ==~ "cheese"
        assert !("cheesecheese" ==~ "cheese")
    }

    void testFindOperator() {
        assert "cheese" =~ "cheese"

        def regex = "cheese"
        def string = "cheese"
        assert string =~ regex

        def i = 0
        def m = "cheesecheese" =~ "cheese"

        assert m instanceof Matcher

        while (m) { i = i + 1 }
        assert i == 2

        i = 0
        m = "cheesecheese" =~ "e+"
        while (m) { i = i + 1 }
        assert i == 4

        m.reset()
        m.find()
        m.find()
        m.find()
        assert m.group() == "ee"
    }

    void testFindOperatorWithIndexAndRanges() {
        def string = "cheesecheese"
        def matcher = string =~ "e+"

        def value = matcher[2]
        assert value == "ee"

        value = matcher[2..3]
        assert value == "eee"

        value = matcher[0, 2]
        assert value == "eeee"

        value = matcher[0, 1..2]
        assert value == "eeeee"
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
}