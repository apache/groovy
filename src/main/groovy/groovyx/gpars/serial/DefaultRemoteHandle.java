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

import groovyx.gpars.remote.RemoteHost;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * @author Alex Tkachman
 */
public class DefaultRemoteHandle extends RemoteHandle {
    private static final long serialVersionUID = 3543416239144672233L;

    private final Class<?> klazz;

    public DefaultRemoteHandle(final UUID id, final UUID hostId, final Class<?> klazz) {
        super(hostId, id);
        this.klazz = klazz;
    }

    @Override
    protected WithSerialId createObject(final SerialContext context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
      final Constructor<?> constructor = klazz.getConstructor(RemoteHost.class);
      return (WithSerialId) constructor.newInstance(context);
    }
}
