// GPars - Groovy Parallel Systems
//
// Copyright © 2008–2011  The original author or authors
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

package groovyx.gpars.util;

import groovyx.gpars.scheduler.Pool;

/**
 * Provides a couple of utility methods to pools and schedulers.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"AccessOfSystemProperties", "UtilityClass"})
public final class PoolUtils {
    private static final String GPARS_POOLSIZE = "gpars.poolsize";

    private PoolUtils() {}

    public static int retrieveDefaultPoolSize() {
        final String poolSizeValue = System.getProperty(PoolUtils.GPARS_POOLSIZE);
        if (poolSizeValue == null) return defaultPoolSize();
        try {
            return Integer.parseInt(poolSizeValue);
        } catch (NumberFormatException ignored) {
            return defaultPoolSize();
        }
    }

    private static int defaultPoolSize() {
        return Runtime.getRuntime().availableProcessors() + 1;
    }

    public static void checkValidPoolSize(final int poolSize) {
        if (poolSize <= 0) throw new IllegalStateException(Pool.POOL_SIZE_MUST_BE_A_POSITIVE_NUMBER);
    }
}
