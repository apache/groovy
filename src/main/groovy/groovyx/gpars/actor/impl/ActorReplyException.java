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

package groovyx.gpars.actor.impl;

import java.util.Collections;
import java.util.List;

/**
 * Indicates problems sending replies to actors. Holds a list of exceptions that occurred during reply dispatch.
 *
 * @author Vaclav Pech
 *         Date: Jun 11, 2009
 */
public final class ActorReplyException extends RuntimeException {
    private static final long serialVersionUID = -3877063222143535104L;
    private final List<Exception> issues;

    public ActorReplyException(final String message) {
        super(message);
        this.issues = Collections.emptyList();
    }

    public ActorReplyException(final String message, final List<Exception> issues) {
        super(message);
        this.issues = Collections.unmodifiableList(issues);
    }

    @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
    public List<Exception> getIssues() {
        return issues;
    }

    @Override public String toString() {
        return super.toString() + issues;
    }

    @SuppressWarnings({"CallToPrintStackTrace"})
    @Override public void printStackTrace() {
        super.printStackTrace();
        for (final Exception e : issues) e.printStackTrace();
    }
}
