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

package groovyx.gpars.util;

import groovy.lang.Closure;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extends ReentrantReadWriteLock with handy withReadLock(Closure) and withWriteLock(Closure) methods to safely lock
 * and unlock the lock for the passed-in closure.
 * Use:
 * def extendedLock = new ExtendedRWLock()
 * extendedLock.withReadLock() {*      //read lock locked here
 * }*
 *
 * @author Vaclav Pech
 *         Date: Feb 18, 2009
 */
public class EnhancedRWLock extends ReentrantReadWriteLock {
    private static final long serialVersionUID = 4598708242656870566L;

    public EnhancedRWLock() {
        super();
    }

    public EnhancedRWLock(final boolean fair) {
        super(fair);
    }

    /**
     * Performs the passed-in closure with the read lock locked and unlocks the read lock automatically
     * after the closure finishes.
     *
     * @param cl The closure to perform with the read lock held
     */
    public void withReadLock(final Closure cl) {
        readLock().lock();
        try {
            cl.call();
        } finally {
            readLock().unlock();
        }
    }

    /**
     * Performs the passed-in closure with the write lock locked and unlocks the write lock automatically
     * after the closure finishes.
     *
     * @param cl The closure to perform with the write lock held
     */
    public void withWriteLock(final Closure cl) {
        writeLock().lock();
        try {
            cl.call();
        } finally {
            writeLock().unlock();
        }
    }
}
