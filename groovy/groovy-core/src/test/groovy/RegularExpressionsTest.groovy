/**
 * Tests the regular expression syntax.
 *
 * @author Sam Pullara
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
 
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegularExpressionsTest extends GroovyTestCase {

     void testSubscript() {
         def a = "cheesecheese"
         def b = a =~ "e+"
         def value = b[2]
         assert value == "ee"

         value = b[0, 2]

         assert value == "eeee"

         value = b[0, 1..2]

         assert value == "eeeee"
     }

     void testFindRegex() {
         assert "cheese" =~ "cheese"

         def regex = "cheese"
         def string = "cheese"
         assert string =~ regex

         def i = 0
         def m = "cheesecheese" =~ "cheese"

         assert m instanceof Matcher

         while(m) { i = i + 1 }
         assert i == 2

         i = 0
         m = "cheesecheese" =~ "e+"
         while(m) { i = i + 1 }
         assert i == 4

         m.reset()
         m.find()
         m.find()
         m.find()
         assert m.group() == "ee"
     }

     void testMatchRegex() {
         assert "cheese" ==~ "cheese"

         assert !("cheesecheese" ==~ "cheese")

     }

     void testRegexEach() {
         def i = 0
         ("cheesecheese" =~ "cheese").each {value -> println(value); i = i + 1}
         assert i == 2

         i = 0
         ("cheesecheese" =~ "ee+").each { println(it); i = i + 1}
         assert i == 2
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


     void testMatcher() {
         def matcher = "cheese-cheese" =~ "cheese"
         def answer = matcher.replaceAll("edam")
         assert answer == 'edam-edam'

         def cheese = ("cheese cheese!" =~ "cheese").replaceFirst("nice")
         assert cheese == "nice cheese!"
     }

    void testGetLastMatcher() {
        assert "cheese" ==~ "cheese"
        assert Matcher.getLastMatcher().matches()

        switch("cheesefoo") {
            case ~"cheesecheese":
                assert false;
            case ~"(cheese)(foo)":
                m = Matcher.getLastMatcher();
                assert m.group(0) == "cheesefoo"
                assert m.group(1) == "cheese"
                assert m.group(2) == "foo"
                assert m.groupCount() == 2
                break;
            default:
                assert false
        }
    }
}