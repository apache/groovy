// Copyright (c) 2001 The Wilson Partnership.
// All Rights Reserved.
// @(#)MinMLHTTPServer.java, 0.3, 14th October 2001
// Author: John Wilson - tug@wilson.co.uk

package uk.co.wilson.net.http;

/*
Copyright (c) 2001 John Wilson (tug@wilson.co.uk).
All rights reserved.
Redistribution and use in source and binary forms,
with or without modification, are permitted provided
that the following conditions are met:

Redistributions of source code must retain the above
copyright notice, this list of conditions and the
following disclaimer.

Redistributions in binary form must reproduce the
above copyright notice, this list of conditions and
the following disclaimer in the documentation and/or
other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY JOHN WILSON ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JOHN WILSON
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE
*/

import uk.co.wilson.net.MinMLSocketServer;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.StringTokenizer;

public abstract class MinMLHTTPServer extends MinMLSocketServer {
  public MinMLHTTPServer(final ServerSocket serverSocket,
                         final int minWorkers,
                         final int maxWorkers,
                         final int maxKeepAlives,
                         final int workerIdleLife,
                         final int socketReadTimeout)
  {
    super(serverSocket, minWorkers, maxWorkers, workerIdleLife);

    this.maxKeepAlives = maxKeepAlives;
    this.socketReadTimeout = socketReadTimeout;
  }

  private synchronized boolean startKeepAlive() {
   if (this.keepAliveCount < this.maxKeepAlives) {
      MinMLHTTPServer.this.keepAliveCount++;

      return true;
    }

    return false;
  }

  private synchronized void endKeepAlive() {
    this.keepAliveCount--;
  }

  private static class LimitedInputStream extends InputStream {
    public LimitedInputStream(final InputStream in, final int contentLength) {
      this.in = in;
      this.contentLength = contentLength;
    }

    public int available() throws IOException {
      return Math.min(this.in.available(), this.contentLength);
    }

    public void close() throws IOException {
      //
      // Don't close the input stream as there is more data
      // but skip past any unread data in this section
      //

      skip(this.contentLength);
    }

    public int read() throws IOException {
      if (this.contentLength == 0) return -1;

      this.contentLength--;

      return this.in.read();
    }

    public int read(final byte[] buffer) throws IOException {
      return read(buffer, 0, buffer.length);
     }

    public int read(final byte[] buffer, final int offset, int length) throws IOException {
      if (this.contentLength == 0) return -1;

      length = this.in.read(buffer, offset, Math.min(length, this.contentLength));

      if (length != -1) this.contentLength -= length;

      return length;
    }

    public long skip(long count) throws IOException {
      count = Math.min(count, this.contentLength);

      this.contentLength -= count;

      return this.in.skip(count);
     }

     private int contentLength;
     private final InputStream in;
  }

  protected abstract class HTTPWorker extends Worker {
    protected final void process(final Socket socket) throws Exception {
      try {
        socket.setSoTimeout(MinMLHTTPServer.this.socketReadTimeout);

        final InputStream in = new BufferedInputStream(socket.getInputStream());
        final OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        int contentLength;

        do {
          contentLength = -1;

          while (readLine(in) != -1 && this.count == 0);  // skip any leading blank lines
          final StringTokenizer toks = new StringTokenizer(new String(this.buf, 0, this.count));
          final String method = toks.nextToken();
          final String uri = toks.nextToken();
          final String version = toks.hasMoreTokens() ? toks.nextToken() : "";

          while (readLine(in) != -1 && this.count != 0) {
          final String option = new String(this.buf, 0, this.count).trim().toLowerCase();

            if (option.startsWith("connection:")) {
              if (option.endsWith("keep-alive")) {
                if (!this.keepAlive)
                  this.keepAlive = MinMLHTTPServer.this.startKeepAlive();
              } else if (this.keepAlive) {
                  MinMLHTTPServer.this.endKeepAlive();
                  this.keepAlive = false;
              }
            } else if (option.startsWith("content-length:")) {
              contentLength = Integer.parseInt(option.substring(15).trim());
              //
              // This can throw NumberFormatException
              // In which case we will abort the transaction
              //
            }
          }

          if (contentLength == -1) {
            processMethod(in, out, method, uri, version);
          } else {
          final InputStream limitedIn = new LimitedInputStream(in, contentLength);

            processMethod(limitedIn, out, method, uri, version);

            limitedIn.close();  // skips unread bytes
          }

          out.flush();

        } while(contentLength != -1 && this.keepAlive);
      }
      finally {
        if (this.keepAlive == true) {
          MinMLHTTPServer.this.endKeepAlive();
          this.keepAlive = false;
        }
      }
    }

    protected void processMethod(final InputStream in,
                                 final OutputStream out,
                                 final String method,
                                 final String uri,
                                 final String version)
    throws Exception
    {
      if (method.equalsIgnoreCase("GET"))
        processGet(in, out, uri, version);
      else if (method.equalsIgnoreCase("HEAD"))
        processHead(in, out, uri, version);
      else if (method.equalsIgnoreCase("POST"))
        processPost(in, out, uri, version);
      else if (method.equalsIgnoreCase("PUT"))
        processPut(in, out, uri, version);
      else
        processOther(in, out, method, uri, version);
    }

    protected void processGet(final InputStream in,
                              final OutputStream out,
                              final String uri,
                              final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(get);
      out.write(errorMessage2);
    }

    protected void processHead(final InputStream in,
                               final OutputStream out,
                               final String uri,
                               final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(head);
      out.write(errorMessage2);
    }

    protected void processPost(final InputStream in,
                               final OutputStream out,
                               final String uri,
                               final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(post);
      out.write(errorMessage2);
    }

    protected void processPut(final InputStream in,
                              final OutputStream out,
                              final String uri,
                              final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(put);
      out.write(errorMessage2);
    }

    protected void processOther(final InputStream in,
                                final OutputStream out,
                                final String method,
                                final String uri,
                                final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(method.getBytes());
      out.write(errorMessage2);
    }

    protected void writeKeepAlive(final OutputStream res) throws IOException {
      res.write(this.keepAlive ? keepConnection : closeConnection);
    }

    private int readLine(final InputStream in) throws IOException {
    int nextByte;

      this.count = 0;

      while (!((nextByte = in.read()) == '\r' && (nextByte = in.read()) =='\n') && nextByte != -1) {
      	this.buf[this.count] = (byte)nextByte;

        if (this.count != this.buf.length - 1) this.count++;   // sort of crude should probably handle long lines better
      }

      return nextByte;
    }

    private final byte[] buf = new byte[256];
    private int count = 0;
    private boolean keepAlive = false;
  }

  private int keepAliveCount = 0;
  protected final int maxKeepAlives;
  protected final int socketReadTimeout;

  protected static final byte[] okMessage = (" 200 OK \r\n"
                                             + "Server: uk.co.wilson.net.http.HTTPServer\r\n").getBytes();

  protected static final byte[] endOfLine = "\r\n".getBytes();
  protected static final byte[] get = "GET".getBytes();
  protected static final byte[] head = "HEAD".getBytes();
  protected static final byte[] post = "POST".getBytes();
  protected static final byte[] put = "PUT".getBytes();
  protected static final byte[] errorMessage1 = (" 400 Bad Request\r\n"
                                                 + "Server: uk.co.wilson.net.http.HTTPServer\r\n\r\n"
                                                 + "Method ").getBytes();

  protected static final byte[] errorMessage2 = " not implemented\r\n".getBytes();
  protected static final byte[] keepConnection = "Connection: Keep-Alive\r\n".getBytes();
  protected static final byte[] closeConnection = "Connection: Close\r\n".getBytes();
}
