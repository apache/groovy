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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.WriteAbortedException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * @author Alex Tkachman
 */
public abstract class RemoteHandle implements Serializable {
    private static final long serialVersionUID = 1L ;
    protected final UUID serialId;
    protected final UUID hostId;

    public RemoteHandle(final UUID hostId, final UUID id) {
        this.hostId = hostId;
        serialId = id;
    }

    @SuppressWarnings({"CatchGenericClass", "UnusedDeclaration", "OverlyBroadCatchBlock"})
    protected final Object readResolve() throws ObjectStreamException {
        final SerialContext context = SerialContext.get();
        final SerialHandle serialHandle = context.get(serialId);

        WithSerialId obj;
        if (serialHandle == null || (obj = serialHandle.get()) == null) {
            try {
                obj = createObject(context);
                obj.serialHandle = SerialHandle.create(obj, serialId);
            } catch (Exception t) {
                throw new WriteAbortedException(t.getMessage(), t);
            }
        }
        return obj;
    }

    protected abstract WithSerialId createObject(SerialContext context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException;
}
