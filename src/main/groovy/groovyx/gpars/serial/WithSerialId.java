// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2010, 2013  The original author or authors
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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Base class for objects which can be exposed to remote nodes via serialization.
 * <p>
 * Main concept is following:
 * </p>
 * <ul>
 *   <li>each object belongs to some LocalHost</li>
 *   <li>every object can be exposed to any {@link groovyx.gpars.remote.RemoteHost}</li>
 *   <li>on remote host object is represented by proxy usually called remote object</li>
 *   <li>for serialization we use writeReplace method, which creates special handle to be serialized instead of the object</li>
 *   <li>for deserialization handle's readResolve method creates remote object (proxy)</li>
 * </ul>
 * <p>
 * See {@link java.io.Serializable} for detailed description how methods writeReplace &amp; readResolve works.
 * </p>
 * <p>
 * It is very important to know that (de)serialization never happens by itself but
 * always happens in context of {@link groovyx.gpars.remote.RemoteHost} and (@link LocalHost}.
 * Such context is used for right resolution/transformation of objects
 * </p>
 *
 * @author Alex Tkachman
 */
public abstract class WithSerialId implements Serializable {
    private static final long serialVersionUID = 75514416530973469L;
    /**
     * See SerialHandle class for details
     */
    public volatile SerialHandle serialHandle;

    /**
     * Gets serial handle for the object
     * If needed new handle created and serialization host subscribed for the object handle
     *
     * @return serial handle for the object
     */
    public final SerialHandle getOrCreateSerialHandle() {
        if (serialHandle == null) {
            synchronized (this) {
                if (serialHandle == null) {
                    serialHandle = SerialHandle.create(this, null);
                }
            }
        }
        return serialHandle;
    }

    /**
     * Class of remote object to be created
     *
     * @return Throws UnsupportedOperationException
     */
    public <T extends RemoteSerialized> Class<T> getRemoteClass() {
        throw new UnsupportedOperationException();
    }

    /**
     * Replace object by handle for serialization
     *
     * @return handle to serialize
     * @throws ObjectStreamException If the object cannot be serialized
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected final Object writeReplace() throws ObjectStreamException {
        final SerialHandle handle = getOrCreateSerialHandle();
        if (this instanceof RemoteSerialized) {
            return new LocalHandle(handle.getSerialId());
        }

        final SerialContext host = SerialContext.get();
        handle.subscribe(host);
        return createRemoteHandle(handle, host);
    }

    protected RemoteHandle createRemoteHandle(final SerialHandle handle, final SerialContext host) {
        return new DefaultRemoteHandle(handle.getSerialId(), host.getHostId(), getRemoteClass());
    }
}
