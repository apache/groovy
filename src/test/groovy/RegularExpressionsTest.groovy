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
 	
 	void testFindRegex() {
 	
 		assert "cheese" =~ "cheese"
 		
 		regex = "cheese"
 		string = "cheese"
 		assert string =~ regex
 		
 		i = 0
 		m = "cheesecheese" =~ "cheese"
 		
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
 	
 	property i
 	
 	void testRegexEach() {
 		i = 0
 		("cheesecheese" =~ "cheese").each {value | println(value); i = i + 1}
 		assert i == 2

 		i = 0
 		("cheesecheese" =~ "ee+").each { println(it); i = i + 1}
 		assert i == 2
 	}
 	
 	void testPatterns() {
 		pattern = ~"foo"
 		assert pattern instanceof Pattern
 		assert pattern.matcher("foo").matches()
 		assert !pattern.matcher("bar").matches()
 		
 		pattern = ~<<<EOS
foo
EOS
 		assert pattern.matcher("foo").matches()
 		assert !pattern.matcher("bar").matches()
 		
 		assert "foofoofoo" =~ ~"foo"
 	}
 	
 	void testMatcher() {
 	    matcher = "cheese-cheese" =~ "cheese"
 	    answer = matcher.replaceAll("edam")
 	    assert answer == 'edam-edam'
 	    
 	    cheese = ("cheese cheese!" =~ "cheese").replaceFirst("nice")
 	    assert cheese == "nice cheese!"
 	}
 	
 	void testSubscript() {
 	    a = "cheesecheese"
 	    b = a =~ "e+"
 	    value = b[2]
 	    assert value == "ee"
 	    
 	    value = b[0, 2]
 	    
 	    assert value == "eeee"
 	    
 	    value = b[0, 1..3]
 	    
 	    assert value == "eeeee"
 	}
 }