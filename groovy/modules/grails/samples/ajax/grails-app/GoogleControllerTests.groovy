import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class GoogleControllerTests extends GroovyTestCase {
   @Property gc = new GoogleController();

   void testSearch() {
		def request = new MockHttpServletRequest();
		def response = new MockHttpServletResponse();

		request.addParameter("q","java")
		gc.search(request,response)
   }
}