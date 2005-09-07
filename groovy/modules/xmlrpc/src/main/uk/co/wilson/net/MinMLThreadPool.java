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

package uk.co.wilson.net;

import java.io.IOException;
import java.io.InterruptedIOException;

public abstract class MinMLThreadPool {
    public MinMLThreadPool(final int minWorkers,
                           final int maxWorkers,
                           final int workerIdleLife)
    {
        this.minWorkers = Math.max(minWorkers, 1);
        this.maxWorkers = Math.max(this.minWorkers, maxWorkers);
        this.workerIdleLife = workerIdleLife;
    }

    public void start() {
      getNewWorker().run();
    }

    public abstract void shutDown() throws IOException;

    protected abstract Worker makeNewWorker();
    
    protected abstract void setTimeout(int timeout);

    private Worker getNewWorker() {
      if (debug) System.out.println("Starting new thread: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);
      
      if (this.liveWorkerCount++ == this.minWorkers)
        setTimeout(this.workerIdleLife);

      return makeNewWorker();
    }

    private synchronized void startWork() {
      if (debug) System.out.println("Thread starting work: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);
     if (++this.workingWorkerCount == this.liveWorkerCount && this.liveWorkerCount < this.maxWorkers) {
      final Thread workerThread = new Thread(getNewWorker());
      
        workerThread.setDaemon(false);
        workerThread.setName("Thread Pool worker thread");
        workerThread.start();
      }

      if (debug) System.out.println("Thread started work: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);
    }

    private synchronized void endWork() {
      this.workingWorkerCount--;
      
      if (debug) System.out.println("Thread ending work: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);
    }

    private synchronized boolean workerMustDie() {
      if (debug) System.out.println("Thread timing out socket read: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);
      
      if (this.liveWorkerCount > this.minWorkers && this.liveWorkerCount != this.workingWorkerCount + 1) {
        if (debug) System.out.println("Thread commits suicide");
        
        workerDies();

        return true;
      }

      return false;
    }

    private synchronized void workerDies() {
      if (--this.liveWorkerCount == this.minWorkers) {
        setTimeout(0);
      }
      
      if (debug) System.out.println("Thread dying: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);
      
      if (this.liveWorkerCount < this.minWorkers) { 
        new Thread(getNewWorker()).start();
        
        if (debug) System.out.println("Creating replacement thread: liveWorkerCount = " + this.liveWorkerCount + " workingWorkerCount = " + this.workingWorkerCount);        
      }
    }

    protected abstract class Worker implements Runnable {
      public final void run() {
        try {
          while (true) {
          final Object resource;

            try {
              resource = getResource();

              try {
                try {
                  MinMLThreadPool.this.startWork();

                  Thread.yield(); // let a blocked worker thread do an accept()

                  process(resource);
                }
                catch (final Exception e) {
                  processingException(e);
                }
              }
              finally {
                try {
                  dispose(resource);
                }
                catch (final IOException e) {
                  if (debug) {
                    System.out.println("Exception thrown when closing socket: " + e.toString());
                    e.printStackTrace();
                  }
                }
                finally {
                    MinMLThreadPool.this.endWork();
                }
              }
            }
            catch (final InterruptedIOException e) {
              if (MinMLThreadPool.this.workerMustDie()) return;
            }
          }
        }
        catch (final Exception e) {
          operatingException(e);
                  
          if (debug) {
            System.out.println("Thread dying due to Exception: " + e.toString());
            e.printStackTrace();
          }
        
          MinMLThreadPool.this.workerDies();
        }
      }

      protected void processingException(final Exception e) {
      }

      protected void operatingException(final Exception e) {
      }

      protected abstract Object getResource() throws Exception;
      protected abstract void dispose(Object resource) throws Exception;
      protected abstract void process(Object resource) throws Exception;
    }
    
    protected final int minWorkers;
    protected final int maxWorkers;
    protected final int workerIdleLife;
    private int liveWorkerCount = 0;
    private int workingWorkerCount = 0;

    protected static final boolean debug = false;
}
