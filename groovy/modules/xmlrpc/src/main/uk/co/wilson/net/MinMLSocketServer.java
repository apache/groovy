/*
 * Copyright 2001, 2005 John G. Wilson
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


package uk.co.wilson.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public abstract class MinMLSocketServer extends MinMLThreadPool {
  public MinMLSocketServer(final ServerSocket serverSocket,
                           final int minWorkers,
                           final int maxWorkers,
                           final int workerIdleLife)
  {
    super(minWorkers, maxWorkers, workerIdleLife);
    this.serverSocket = serverSocket;
  }

  public synchronized void shutDown() throws IOException {
    this.serverSocket.close();
  }

  public int getPortNumber() {
    return this.serverSocket.getLocalPort();
  }

  protected void setTimeout(final int timeout) {
    try {
      this.serverSocket.setSoTimeout(timeout);
    }
    catch (final SocketException e) {
    }
  }
  protected abstract class ServerSocketWorker extends Worker {
      protected Object getResource() throws IOException {
          return MinMLSocketServer.this.serverSocket.accept();
      }
      
      protected void dispose(Object resource) throws IOException {
          ((Socket)resource).close();
      }
  }

  private final ServerSocket serverSocket;
}
