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

import org.jivesoftware.smack.PacketCollector;

/**
 * @author John Wilson
 *
 */

public abstract class MinMLJabberPacketServer  extends MinMLThreadPool {
    public MinMLJabberPacketServer(final PacketCollector packetCollector,
                                   final int minWorkers,
                                   final int maxWorkers,
                                   final int workerIdleLife)
            {
              super(minWorkers, maxWorkers, workerIdleLife);
              this.packetCollector = packetCollector;
            }

    /* (non-Javadoc)
     * @see uk.co.wilson.net.MinMLThreadPool#shutDown()
     */
    public void shutDown() throws IOException {
      this.serverActive = false;
        this.packetCollector.cancel();
    }

    /* (non-Javadoc)
     * @see uk.co.wilson.net.MinMLThreadPool#setTimeout(int)
     */
    protected void setTimeout(int timeout) {
        this.timeout = timeout;
        
    }
    protected abstract class JabberPacketWorker extends Worker {
        protected Object getResource() throws IOException {
        final Object result = MinMLJabberPacketServer.this.packetCollector.nextResult(MinMLJabberPacketServer.this.timeout);
        
          if (result == null) throw new IOException("packet read timed out");
          
          return result;
        }
        
        protected void dispose(Object resource) throws IOException {
            // No action needed
        }
    }
    
    private final PacketCollector packetCollector;
    private long timeout = this.workerIdleLife;
}
