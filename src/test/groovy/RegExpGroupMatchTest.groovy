package groovy

/**
 * Test for fixing the Jira issue GROOVY-1000
 *
 *    Fix an infinite loop when getting after group matching in regular expression.
 *    Graham Miller has given this idea.
 *
 * @author Pilho Kim
 * @version $Revision$
 */

import java.util.regex.Matcher
import java.util.regex.Pattern

class RegExpGroupMatchTest extends GroovyTestCase {

    void testFirst() {
        assert "cheesecheese" =~ "cheese"
        assert "cheesecheese" =~ /cheese/
        assert "cheese" == /cheese/   /*they are both string syntaxes*/
    }

    void testSecond() {
        // Lets create a regex Pattern
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
        // Lets do some replacement
        def cheese = ("cheesecheese" =~ /cheese/).replaceFirst("nice")
        assert cheese == "nicecheese"
    }

    void testFifth() {
        // Group demo
        def matcher = "\$abc." =~ "\\\$(.*)\\."
        matcher.matches();                   // must be invoked
        assert matcher.group(1) == "abc"     // is one, not zero
        // assert matcher[1] == "abc"     // This has worked only before jsr-03-release
        println (matcher[0])
        assert matcher[0] == ["\$abc.", "abc"]
        assert matcher[0][1] == "abc"
    }

    void testSixth() {
        // Group demo
        // Avoid having to double all the backslash escaping characters.
        def matcher = "\$abc." =~ /\$(.*)\./    // no need to double-escape!
        assert "\\\$(.*)\\." == /\$(.*)\./
        matcher.matches();                      // must be invoked
        assert matcher.group(1) == "abc"        // is one, not zero
        // assert matcher[1] == "abc"     // This has worked only before jsr-03-release
        println (matcher[0])
        assert matcher[0] == ["\$abc.", "abc"]
        assert matcher[0][1] == "abc"
    }

    // Test no group match.
    void testNoGroupMatcherAndGet() {
        def p = /ab[d|f]/
        def m = "abcabdabeabf" =~ p 

        for (i in 0..<m.count) { 
            println( "m.groupCount() = " + m.groupCount())
            println( "  " + i + ": " + m[i] )   // m[i] is a String
        }
    }

    // Test group matches.
    void testGroupMatcherAndGet() {
        def p = /(?:ab([c|d|e|f]))/
        def m = "abcabdabeabf" =~ p 

        for (i in 0..<m.count) { 
            println( "m.groupCount() = " + m.groupCount())
            println( "  " + i + ": " + m[i] )   // m[i] is a String
        }
    }

    // Test group matches.
    void testAnotherGroupMatcherAndGet() {
        def m = "abcabdabeabfabxyzabx" =~ /(?:ab([d|x-z]+))/

        m.count.times { 
            println( "m.groupCount() = " + m.groupCount())
            println( "  " + it + ": " + m[it] )   // m[it] is a String
        }
    }
}

