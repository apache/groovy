// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.activeobject;

import groovyx.gpars.group.PGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps string identifiers to instances of PGroup.
 * Active objects may refer to their desired groups by the identifiers and they will look the groups up in the registry.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"Singleton"})
public final class ActiveObjectRegistry {
    private static final ActiveObjectRegistry ourInstance = new ActiveObjectRegistry();

    public static ActiveObjectRegistry getInstance() {
        return ourInstance;
    }

    private final Map<String, PGroup> registry = new ConcurrentHashMap<String, PGroup>();

    private ActiveObjectRegistry() { }

    public PGroup findGroupById(final String groupId) {
        return registry.get(groupId);
    }

    public void register(final String groupId, final PGroup group) {
        registry.put(groupId, group);
    }
}
