package groovy.servlet;

import javax.servlet.http.HttpServletRequest;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class GroovyServletTest extends MockObjectTestCase {

    public void testRequestGetCommandOK() {
        Mock requestMock = mock(HttpServletRequest.class);
        requestMock.expects(once()).method("getParameter").with(eq("command")).will(returnValue("SELECT..."));
        HttpServletRequest request = (HttpServletRequest) requestMock.proxy();
        String command = request.getParameter("command");
        assertEquals("SELECT...", command);
    }
}
