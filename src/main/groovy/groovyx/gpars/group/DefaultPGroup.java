// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2011, 2013  The original author or authors
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

package groovyx.gpars.group;

import groovyx.gpars.GParsConfig;
import groovyx.gpars.scheduler.DefaultPool;
import groovyx.gpars.scheduler.Pool;

/**
 * Provides logical grouping for actors, agents and dataflow tasks and operators. Each group has an
 * underlying thread pool, which will perform actions on behalf of the users belonging to the group. {@code
 * Actors} created through the {@code DefaultPGroup.actor} method will automatically belong to the group
 * through which they were created, just like agents created through the {@code agent} or {@code fairAgent}
 * methods or dataflow tasks and operators created through the {@code task} or {@code operator} methods.
 * Uses a pool of non-daemon threads.  The {@code DefaultPGroup} class implements the {@code Pool} interface
 * through {@code @Delegate}.
 * <pre>
 * def group = new DefaultPGroup()
 * group.resize 1
 * def actor = group.actor {
 *   react { message -&gt;
 *     println message
 *   }
 * }.start()
 * actor.send 'Hi!'
 * . . .
 * group.shutdown()
 * </pre>
 * <p>
 * Otherwise, if constructing {@code Actors} directly through their constructors, the {@code
 * AbstractPooledActor.parallelGroup} property, which defaults to the {@code Actors.defaultActorPGroup}, can
 * be set before the actor is started.
 * </p>
 * <pre>
 * def group = new DefaultPGroup()
 * def actor = new MyActor()
 * actor.parallelGroup = group
 * actor.start()
 * . . .
 * group.shutdown()
 * </pre>
 *
 * @author Vaclav Pech
 */
public final class DefaultPGroup extends PGroup {

    /**
     * Creates a group for actors, agents, tasks and operators. The actors will share the supplied thread
     * pool.
     *
     * @param threadPool The thread pool to use for the group
     */
    public DefaultPGroup(final Pool threadPool) {
        super(threadPool);
    }

    /**
     * Creates a group for actors, agents, tasks and operators. The actors will share a common daemon thread pool.
     */
    public DefaultPGroup() {
        super(GParsConfig.getPoolFactory() == null ? new DefaultPool(true) : GParsConfig.getPoolFactory().createPool(true));
    }

    /**
     * Creates a group for actors, agents, tasks and operators. The actors will share a common daemon thread pool.
     *
     * @param poolSize The initial size of the underlying thread pool
     */
    public DefaultPGroup(final int poolSize) {
        super(GParsConfig.getPoolFactory() == null ? new DefaultPool(true, poolSize) : GParsConfig.getPoolFactory().createPool(true, poolSize));
    }
}
