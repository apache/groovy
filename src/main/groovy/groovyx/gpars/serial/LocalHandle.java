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

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Alex Tkachman
 */
public final class LocalHandle implements Serializable {
    private static final long serialVersionUID = -8206894167996286304L;
    private final UUID id;

    public LocalHandle(final UUID id) {
        this.id = id;
    }

    @SuppressWarnings({"UnusedDeclaration", "ProtectedMemberInFinalClass"})
    protected Object readResolve() {
        return SerialContext.get().get(id).get();
    }

    public UUID getId() {
        return id;
    }
}
