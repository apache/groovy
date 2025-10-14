// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
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
package groovyx.gpars.actor.remote;

final class RemoteActorsUrlUtils {
    private RemoteActorsUrlUtils() {
    }

    /**
     * Checks if specified name is valid to publish some actor under it.
     * Valid name cannot contain "/"
     * @param name the name of an actor
     * @return true if name is valid; false otherwise
     */
    static boolean isValidActorName(String name) {
        return name != null && !name.contains("/");
    }

    /**
     * Gets actor name from specified actor url
     * @param actorUrl the actor url
     * @return name of an actor taken from specified actor url
     */
    static String getActorName(String actorUrl) {
        int splitPos = calculateSplitPosition(actorUrl);
        return actorUrl.substring(splitPos+1);
    }

    /**
     * Get actor group name from specified actor url
     *
     */
    static String getGroupName(String actorUrl) {
        int splitPos = calculateSplitPosition(actorUrl);
        return splitPos >= 0 ? actorUrl.substring(0, splitPos) : "";
    }

    private static int calculateSplitPosition(String actorUrl) {
        return actorUrl.lastIndexOf('/');
    }
}
