/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org.
 * 
 * 4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 * 
 * 5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package groovy.servlet;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * A common ground dealing with the http servlet API wrinkles.
 *
 * @author Christian Stein
 */
public abstract class AbstractHttpServlet extends HttpServlet {

  /**
   * Content type of the HTTP response.
   */
  public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

  /*
   * Servlet API include key names.
   */
  public static final String INC_PATH_INFO = "javax.servlet.include.path_info";
  // public static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";
  public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";

  /**
   * Returns the include-aware uri of the script or template file.
   * 
   * @param request
   *  the http request to analyze
   * @return the include-aware uri either parsed from request attributes or
   *  hints provided by the servlet container
   */
  protected String getScriptUri(HttpServletRequest request) {
    //
    // NOTE: This piece of code is heavily inspired by Apaches Jasper2!
    // 
    // http://cvs.apache.org/viewcvs.cgi/jakarta-tomcat-jasper/jasper2/ \
    //        src/share/org/apache/jasper/servlet/JspServlet.java?view=markup
    //
    // Why doesn't it use request.getRequestURI() or INC_REQUEST_URI?
    //

    String uri = null;
    String info = null;

    //
    // Check to see if the requested script/template source file has been the
    // target of a RequestDispatcher.include().
    //
    uri = (String) request.getAttribute(INC_SERVLET_PATH);
    if (uri != null) {
      //
      // Requested script/template file has been target of 
      // RequestDispatcher.include(). Its path is assembled from the relevant
      // javax.servlet.include.* request attributes and returned!
      //
      info = (String) request.getAttribute(INC_PATH_INFO);
      if (info != null) {
        uri += info;
      }
      return uri;
    }

    //
    // Requested script/template file has not been the target of a 
    // RequestDispatcher.include(). Reconstruct its path from the request's
    // getServletPath() and getPathInfo() results.
    //
    uri = request.getServletPath();
    info = request.getPathInfo();
    if (info != null) {
      uri += info;
    }

    return uri;
  }

  /**
   * Parses the http request for the real script or template source file.
   * 
   * @param request
   *  the http request to analyze
   * @param context
   *  the context of this servlet used to get the real path string
   * @return a file object using an absolute file path name
   */
  protected File getScriptUriAsFile(HttpServletRequest request,
      ServletContext context) {
    String uri = getScriptUri(request);
    String real = context.getRealPath(uri);
    File file = new File(real).getAbsoluteFile();

    // log("\tInclude-aware URI: " + uri);
    // log("\tContext real path: " + real); // context.getRealPath(uri)
    // log("\t             File: " + file);
    // log("\t      File exists? " + file.exists());
    // log("\t    File can read? " + file.canRead());
    // log("\t      ServletPath: " + request.getServletPath());
    // log("\t         PathInfo: " + request.getPathInfo()); 
    // log("\t       RequestURI: " + request.getRequestURI());
    // log("\t      QueryString: " + request.getQueryString());

    // //log("\t  Request Params: ");
    // //Enumeration e = request.getParameterNames();
    // //while (e.hasMoreElements()) {
    // //  String name = (String) e.nextElement();
    // //  log("\t\t " + name + " = " + request.getParameter(name));
    // //}   

    return file;
  }

}
