// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.serial;

import java.util.UUID;

/**
 * @author Alex Tkachman
 */
public abstract class SerialContext {
    private static final ThreadLocal<SerialContext> threadContext = new ThreadLocal<SerialContext>();

    protected final SerialHandles localHost;

    protected final UUID hostId;

    public SerialContext(final SerialHandles localHost, final UUID hostId) {
        this.localHost = localHost;
        this.hostId = hostId;
    }

    public static SerialContext get() {
        return threadContext.get();
    }

    public UUID getHostId() {
        return hostId;
    }

    /**
     * Enter to the context
     */
    public final void enter() {
        if (threadContext.get() != null) {
            throw new IllegalStateException("Serialization context already defined");
        }

        threadContext.set(this);
    }

    /**
     * Leave this context
     */
    public final void leave() {
        if (threadContext.get() != this) {
            throw new IllegalStateException("Wrong serialization context");
        }

        threadContext.set(null);
    }

    public UUID getLocalHostId() {
        return localHost.getId();
    }

    public void add(final SerialHandle serialHandle) {
        localHost.add(serialHandle);
    }

    public void remove(final SerialHandle serialHandle) {
        localHost.remove(serialHandle);
    }

    public SerialHandle get(final UUID id) {
        return localHost.get(id);
    }

    public void finalizeHandle(final SerialHandle handle) {
        localHost.finalizeHandle(handle);
    }

    public SerialContext getSerialHost(final UUID hostId, final Object attachment) {
        return localHost.getSerialHost(hostId, attachment);
    }

    public abstract void write(SerialMsg msg);
}
