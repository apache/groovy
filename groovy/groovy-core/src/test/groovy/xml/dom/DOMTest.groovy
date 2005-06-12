package groovy.xml.dom;

import java.io.*;
import groovy.xml.*;
import org.codehaus.groovy.sandbox.markup.*;
import org.codehaus.groovy.tools.xml.*;

class DOMTest extends GroovyTestCase {

  def benchmark = false;

  /*
  void testDOMParser() {
    def xml = new StringReader("<html><head><title class='mytitle'>Test</title></head><body><p class='mystyle'>This is a test.</p></body></html>");
    def doc = DOMBuilder.parse(xml);
    def html = doc.documentElement;
    
    if (!benchmark) { assertCorrect html }
  }

  void testDOMBuilder() {
    def html = DOMBuilder.newInstance().
        
    html {
      head {
		title (class:"mytitle", "Test")
      }
      body {
		p (class:"mystyle", "This is a test.")
      }
    }

    if (!benchmark) { assertCorrect html }
  }
 */
 
  void testStreamingDOMBuilder() {

    def doc = new StreamingDOMBuilder().bind {
      html {
        head {
		  title (class:"mytitle", "Test")
        }
        body {
		  p (class:"mystyle", "This is a test.")
        }
      }
	}()

    if (!benchmark) { assertCorrect doc.documentElement }
  }
  void assertCorrect(html) {
    use (groovy.xml.dom.DOMCategory) {
	  assert html.head.title.textContent == "Test";
    	  assert html.body.p.textContent == "This is a test.";
      assert html.find { it.tagName == "body" }.tagName == "body";
      assert html.getElementsByTagName("*").findAll { if (it["@class"] != "") { return it } }.size() == 2;
    }
    shouldFail (MissingPropertyException) { html.head };
  }
  
  static void main(args) {
  	// Relative results:
  	// Test       5th May 2004
  	// Parser:    1.0
  	// Builder:   1.05
  	// Streaming: 0.77
  	def x = args.size() == 0 ? 1000 : Integer.parseInt(args[0]);
  	def mydomtest = new DOMTest();
  	def standard = 0;
  	mydomtest.benchmark = true;
  	[mydomtest.testDOMParser, mydomtest.testDOMBuilder, mydomtest.testStreamingDOMBuilder].each {
  		// Run the method once to fill any caches and to load classes
  		it();
	  	def start = System.currentTimeMillis();
  		for (i in 1..x) {
  			it();
	  	}
  		def elapsed = System.currentTimeMillis() - start;
  		def result = i * 1000 / elapsed;

		def standard = (standard == 0 ? result : standard);
		def factor = result/standard;
  		
	  	println "${it.method}: ${factor}x (${result} trees/s)";
	}
  }
}
