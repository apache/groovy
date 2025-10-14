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

package groovyx.gpars;

import java.util.List;

/**
 * This class wraps multiple exception, which occurred in concurrently run code inside one of the <i>GParsExecutorsPoolUtil</i> methods.
 *
 * @author Vaclav Pech
 *         Date: Nov 17, 2008
 */
public final class AsyncException extends RuntimeException {
    private static final long serialVersionUID = 1573135643731810717L;
    private final List<Throwable> concurrentExceptions;

    @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter"})
    public AsyncException(final String message, final List<Throwable> concurrentExceptions) {
        super(message);
        this.concurrentExceptions = concurrentExceptions;
    }

    public List<Throwable> getConcurrentExceptions() {
        return concurrentExceptions;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ' ' + buildMessage();
    }

    @Override
    public String toString() {
        return buildMessage();
    }

    @SuppressWarnings({"StringBufferWithoutInitialCapacity"})
    private String buildMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AsyncException");
        sb.append("{concurrentExceptions=").append("[\n");
        for (final Throwable concurrentException : concurrentExceptions) {
            sb.append(concurrentException.toString()).append('\n');
        }
        sb.append("]}");
        return sb.toString();
    }
}
