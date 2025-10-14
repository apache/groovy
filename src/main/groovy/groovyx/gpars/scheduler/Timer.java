// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2013  The original author or authors
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

package groovyx.gpars.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * A shared timer to run timeouts for various GPars services
 */
public final class Timer {
    /**
     * The actual scheduler instance
     */
    public static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(5, new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable, "GPars timer scheduler");
            thread.setDaemon(true);
            return thread;
        }
    });

}
