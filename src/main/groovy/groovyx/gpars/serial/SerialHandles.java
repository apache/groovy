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

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Alex Tkachman
 */
public abstract class SerialHandles {
    /**
     * Unique id of the provider
     */
    protected final UUID id = UUID.randomUUID();

    /**
     * Table of local objects serialized out to remote nodes
     */
    private final HashMap<UUID, SerialHandle> localHandles = new HashMap<UUID, SerialHandle>();

    /**
     * Getter for provider id
     *
     * @return unique id
     */
    public UUID getId() {
        return id;
    }

    public void add(final SerialHandle handle) {
        localHandles.put(handle.getSerialId(), handle);
    }

    public void remove(final SerialHandle handle) {
        localHandles.remove(handle.getSerialId());
    }

    public SerialHandle get(final UUID id) {
        return localHandles.get(id);
    }

    public void finalizeHandle(final SerialHandle handle) {
        localHandles.remove(handle.getSerialId());
    }

    public abstract SerialContext getSerialHost(UUID hostId, Object attachment);
}
