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

import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.RemoteHost;
import org.codehaus.groovy.util.ManagedReference;
import org.codehaus.groovy.util.ReferenceBundle;
import org.codehaus.groovy.util.ReferenceManager;
import org.codehaus.groovy.util.ReferenceType;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Weak reference to object, which was serialized to remote hosts.
 * Also keep ids of all hosts, where the object was serialized.
 * <p>
 * While subscribed by remote nodes it keeps object alive by holding strong reference (anchor) to it
 * </p>
 *
 * @author Alex Tkachman
 */
public class SerialHandle extends ManagedReference<WithSerialId> {

    private static final ReferenceQueue<WithSerialId> queue = new ReferenceQueue<WithSerialId>();

    private static final ReferenceManager manager = ReferenceManager.createThreadedManager(queue);

    private static final ReferenceBundle bundle = new ReferenceBundle(manager, ReferenceType.WEAK);

    /**
     * serial id of the object
     */
    protected final UUID serialId;

    /**
     * local host
     */
    protected final SerialContext context;

    /**
     * remote hosts subscribed to this objects
     */
    private volatile Object subscribers;

    @SuppressWarnings("unused")
    private volatile WithSerialId anchor; //  TODO:  Eclipse requires this to be tagged as unused.

    /**
     * Construct handle for object with given id to it
     *
     * @param value The value to associate with the id
     * @param id    The id, if null a new id will be generated
     */
    private SerialHandle(final WithSerialId value, final UUID id) {
        super(bundle, value);

        context = SerialContext.get();
        if (id == null) {
            serialId = UUID.randomUUID();
        } else {
            serialId = id;
        }
        context.add(this);
    }

    /**
     * Serial id of the object
     *
     * @return The serial id
     */
    public UUID getSerialId() {
        return serialId;
    }

    @Override
    public void finalizeReference() {
        context.finalizeHandle(this);
        super.finalizeReference();
    }

    /**
     * Getter for subscribers
     *
     * @return The current subscribers
     */
    public Object getSubscribers() {
        return subscribers;
    }

    /**
     * Subscribes host as interested in the object
     *
     * @param context The subscription context to use
     */
    @SuppressWarnings({"SynchronizedMethod"})
    public synchronized void subscribe(final SerialContext context) {
        if (subscribers == null) {
            subscribers = context;
        } else {
            if (subscribers instanceof SerialContext) {
                if (subscribers != context) {
                    final Collection<SerialContext> list = new ArrayList<SerialContext>(2);
                    list.add((SerialContext) subscribers);
                    list.add(context);
                    subscribers = list;
                }
            } else {
                @SuppressWarnings({"unchecked"})
                final Collection<SerialContext> list = (Collection<SerialContext>) subscribers;
                for (final SerialContext remoteHost : list) {
                    if (remoteHost == context) {
                        return;
                    }
                }
                list.add(context);
            }
        }

        anchor = get();
    }

    @SuppressWarnings({"SynchronizedMethod"})
    public synchronized void unsubscribe(final SerialContext context) {
        if (subscribers != null) {
            if (subscribers instanceof SerialContext) {
                if (subscribers == context) {
                    subscribers = null;
                }
            } else {
                @SuppressWarnings({"unchecked"})
                final List<SerialContext> list = (List<SerialContext>) subscribers;
                list.remove(context);
                if (list.size() == 1) {
                    subscribers = list.get(0);
                }
            }
        }

        if (subscribers == null) {
            anchor = null;
        }
    }

    public static SerialHandle create(final WithSerialId obj, final UUID id) {
        if (id == null) {
            return new LocalSerialHandle(obj, UUID.randomUUID());
        } else {
            return new RemoteSerialHandle(obj, id);
        }
    }


    private static class LocalSerialHandle extends SerialHandle {
        private LocalSerialHandle(final WithSerialId obj, final UUID uuid) {
            super(obj, uuid);
        }
    }

    private static class RemoteSerialHandle extends SerialHandle {
        private RemoteSerialHandle(final WithSerialId obj, final UUID uuid) {
            super(obj, uuid);
        }

        @Override
        public void finalizeReference() {
            super.finalizeReference();
            context.write(new ReleaseHandle(serialId));
        }

        public static class ReleaseHandle extends SerialMsg {
            private static final long serialVersionUID = -951052191389868427L;
            private final UUID serialId;

            public ReleaseHandle(final UUID serialId) {
                this.serialId = serialId;
            }

            @Override
            public void execute(final RemoteConnection conn) {
                final RemoteHost remoteHost = conn.getHost();
                final SerialHandle handle = remoteHost.get(serialId);
                if (handle instanceof LocalSerialHandle) {
                    handle.unsubscribe(remoteHost);
                }
            }
        }
    }
}
