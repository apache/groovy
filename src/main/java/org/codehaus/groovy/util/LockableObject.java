/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.util;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A bit simplified lock designed to be inherited by.
 */
public class LockableObject extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 2284470475073785118L;
    transient Thread owner;

    @Override
    protected final boolean isHeldExclusively() {
        return getState() != 0 && owner == Thread.currentThread();
    }

    public final void lock() {
        if (compareAndSetState(0, 1))
            owner = Thread.currentThread();
        else
            acquire(1);
    }

    public final void unlock() {
        release(1);
    }

    @Override
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (compareAndSetState(0, acquires)) {
                owner = current;
                return true;
            }
        }
        else if (current == owner) {
            setState(c+ acquires);
            return true;
        }
        return false;
    }

    @Override
    protected final boolean tryRelease(int releases) {
        int c = getState() - releases;
        if (Thread.currentThread() != owner)
            throw new IllegalMonitorStateException();
        boolean free = false;
        if (c == 0) {
            free = true;
            owner = null;
        }
        setState(c);
        return free;
    }
}
