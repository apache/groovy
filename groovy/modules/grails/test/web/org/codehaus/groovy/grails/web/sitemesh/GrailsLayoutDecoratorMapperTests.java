package org.codehaus.groovy.grails.web.sitemesh;

import java.io.StringReader;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.parser.FastPageParser;

public class GrailsLayoutDecoratorMapperTests extends TestCase {

	/*
	 * Test method for 'org.codehaus.groovy.grails.web.sitemesh.GrailsLayoutDecoratorMapper.getDecorator(HttpServletRequest, Page)'
	 */
	public void testGetDecoratorHttpServletRequestPage() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "orders/list");
		GrailsLayoutDecoratorMapper m = new GrailsLayoutDecoratorMapper();
		FastPageParser parser = new FastPageParser();
		String html = "<html><head><title>Test title</title><meta name=\"layout\" content=\"test\"></meta></head><body>here is the body</body></html>";
		
		
		Page page = parser.parse( new StringReader(html) ); 
		Decorator d = m.getDecorator(request, page);
		
		assertEquals("/WEB-INF/jsp/layouts/test.jsp", d.getPage());
		assertEquals("test", d.getName());
		
	}

}
