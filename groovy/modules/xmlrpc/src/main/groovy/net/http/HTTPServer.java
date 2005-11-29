/*
 * Copyright 2005 John G. Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package groovy.net.http;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

import uk.co.wilson.net.MinMLSocketServer;
import uk.co.wilson.net.http.MinMLHTTPServer;

/**
 * @author John Wilson
 *
 */

public class HTTPServer extends GroovyObjectSupport {
  protected static final byte[] userAgent = "User-Agent: Groovy Simple Web Server\r\n".getBytes();
  
  private final int minWorkers;
  private final int maxWorkers;
  private final int maxKeepAlives;
  private final int workerIdleLife;
  private final int socketReadTimeout;
  private MinMLSocketServer server = null;
  private Closure getClosure = null;
  private Closure headClosure = null;
  private Closure postClosure = null;
  private Closure putClosure = null;
  
  public HTTPServer(final int minWorkers, final int maxWorkers, final int maxKeepAlives, final int workerIdleLife, final int socketReadTimeout) {
    this.minWorkers = minWorkers;
    this.maxWorkers = maxWorkers;
    this.maxKeepAlives = maxKeepAlives;
    this.workerIdleLife = workerIdleLife;
    this.socketReadTimeout = socketReadTimeout;
  }
  
  public HTTPServer() {
    this(2, 10, 8, 60000, 60000);
  }

  /* (non-Javadoc)
   * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
   */
  public void setProperty(final String property, final Object newValue) {
    if ("get".equalsIgnoreCase(property) && newValue instanceof Closure) {
      this.getClosure = (Closure)((Closure)newValue).clone();
    } else if ("head".equalsIgnoreCase(property) && newValue instanceof Closure) {
      this.headClosure = (Closure)((Closure)newValue).clone();
    } else if ("post".equalsIgnoreCase(property) && newValue instanceof Closure) {
      this.postClosure = (Closure)((Closure)newValue).clone();
    } else if ("put".equalsIgnoreCase(property) && newValue instanceof Closure) {
      this.putClosure = (Closure)((Closure)newValue).clone();
    } else {
      super.setProperty(property, newValue);
    }
  }

  /**
   * @param serverSocket
   */
  public void startServer(final ServerSocket serverSocket) throws IOException {
      if (this.server != null) stopServer();
      
      final MinMLHTTPServer server = new MinMLHTTPServer(serverSocket,
                                                         this.minWorkers, 
                                                         this.maxWorkers, 
                                                         this.maxKeepAlives, 
                                                         this.workerIdleLife, 
                                                         this.socketReadTimeout) {


        /* (non-Javadoc)
         * @see uk.co.wilson.net.MinMLThreadPool#makeNewWorker()
         */
        protected Worker makeNewWorker() {
          return new HTTPWorker() {
            protected void processGet(final InputStream in,
                                      final OutputStream out,
                                      final String uri,
                                      final String version)
                                        throws Exception
            {             
              out.write(version.getBytes());
              out.write(okMessage);
              out.write(userAgent);
              out.write(host);
              
              if (HTTPServer.this.getClosure != null) {
                HTTPServer.this.getClosure.call(new Object[]{in, out, uri, version});
              }
            }
            
            protected void processHead(final InputStream in,
                                       final OutputStream out,
                                       final String uri,
                                       final String version)
                                         throws Exception
            {
              out.write(version.getBytes());
              out.write(okMessage);
              out.write(userAgent);
              out.write(host);
              
              if (HTTPServer.this.headClosure != null) {
                HTTPServer.this.headClosure.call(new Object[]{in, out, uri, version});
              }
            }
            
            protected void processPost(final InputStream in,
                                       final OutputStream out,
                                       final String uri,
                                       final String version)
                                         throws Exception
            {
              out.write(version.getBytes());
              out.write(okMessage);
              out.write(userAgent);
              out.write(host);
              
              if (HTTPServer.this.postClosure != null) {
                HTTPServer.this.postClosure.call(new Object[]{in, out, uri, version});
              }
            }
            
            protected void processPut(final InputStream in,
                                      final OutputStream out,
                                      final String uri,
                                      final String version)
                                        throws Exception
            {
              out.write(version.getBytes());
              out.write(okMessage);
              out.write(userAgent);
              out.write(host);
              
              if (HTTPServer.this.putClosure != null) {
                HTTPServer.this.putClosure.call(new Object[]{in, out, uri, version});
              }
            }
          };
        }
      };
      
      this.server = server;
      
      
      final Thread startingThread = new Thread() {
        public void run() {
          server.start();
        }
      };
      
      startingThread.setDaemon(false);
      startingThread.setName("HTTP Server main thread");
      startingThread.start();
    }

  /**
   * Starts the server shutdown process
   * This will return before the server has shut down completely
   * Full shutdown may take some time
   * 
   * @throws IOException
   */
  public void stopServer() throws IOException {
    this.server.shutDown();
  }
}
