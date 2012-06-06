package groovy.servlet;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class GroovyServletTest extends MockObjectTestCase {

    private ServletConfig config;
    private ServletContext context;
    private GroovyServlet servlet;

    protected void setUp() throws Exception {
        super.setUp();
        //this.config = (ServletConfig) mock(ServletConfig.class).proxy();
        //this.context = (ServletContext) mock(ServletContext.class).proxy();
        this.servlet = new GroovyServlet();
        //servlet.init(config);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRequestGetCommandOK() {
        Mock requestMock = mock(HttpServletRequest.class);
        requestMock.expects(once()).method("getParameter").with(eq("command")).will(returnValue("SELECT..."));
        HttpServletRequest request = (HttpServletRequest) requestMock.proxy();
        String command = request.getParameter("command");
        assertEquals("SELECT...", command);
    }

    //    public void testService() {
    //        Mock requestMock = mock(HttpServletRequest.class);
    //        Mock responseMock = mock(HttpServletResponse.class);
    //        
    //        HttpServletRequest request = (HttpServletRequest) requestMock.proxy();
    //        HttpServletResponse response = (HttpServletResponse) responseMock.proxy();
    //        try {
    //            servlet.service(request, response);
    //        } catch (Throwable t) {
    //            t.printStackTrace();
    //            fail(t.getMessage());
    //        }
    //    }

}
