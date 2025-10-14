// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

/**
 * Provides logical grouping for actors, agents and dataflow tasks and operators. Each group has an underlying thread pool, which will perform actions
 * on behalf of the users belonging to the group. Actors created through the DefaultPGroup.actor() method
 * will automatically belong to the group through which they were created, just like agents created through the agent() or fairAgent() methods
 * or dataflow tasks and operators created through the task() or operator() methods.
 * Uses a pool of non-daemon threads.
 * The DefaultPGroup class implements the Pool interface through @Delegate.
 *
 * @author Vaclav Pech
 *         Date: Jun 17, 2009
 */
public final class NonDaemonPGroup extends PGroup {

    /**
     * Creates a group for actors, agents, tasks and operators. The actors will share a common non-daemon thread pool.
     */
    public NonDaemonPGroup() {
        super(GParsConfig.getPoolFactory() == null ? new DefaultPool(false) : GParsConfig.getPoolFactory().createPool(false));
    }

    /**
     * Creates a group for actors, agents, tasks and operators. The actors will share a common non-daemon thread pool.
     *
     * @param poolSize The initial size of the underlying thread pool
     */
    public NonDaemonPGroup(final int poolSize) {
        super(GParsConfig.getPoolFactory() == null ? new DefaultPool(false, poolSize) : GParsConfig.getPoolFactory().createPool(false, poolSize));
    }
}
