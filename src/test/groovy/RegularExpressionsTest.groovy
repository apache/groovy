/**
 * Tests the regular expression syntax.
 *
 * @author
 * @version
 */
 
 import java.util.regex.Pattern
 
 class RegularExpressionsTest extends GroovyTestCase {
 	
 	void testFindRegex() {
 	
 		assert "cheese" ~= "cheese"
 		
 		regex = "cheese"
 		string = "cheese"
 		assert string ~= regex
 		
 		i = 0
 		m = "cheesecheese" ~= "cheese"
 		while(m) { i = i + 1 }
 		assert i == 2
 		
 		i = 0
 		m = "cheesecheese" ~= "e+"
 		while(m) { i = i + 1 }
 		assert i == 4
 		
 		m.reset()
 		m.find()
 		m.find()
 		m.find()
 		assert m.group() == "ee"
 	}
 	
 	void testMatchRegex() {
 	
 		assert "cheese" ~== "cheese"
 		
 		assert !("cheesecheese" ~== "cheese")
 		
 	}
 	
 	property i
 	
 	void testRegexEach() {
 		i = 0
 		("cheesecheese" ~= "cheese").each({value | println(value) i = i + 1});
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
 		
 		assert "foofoofoo" ~= ~"foo"
 	}
 }