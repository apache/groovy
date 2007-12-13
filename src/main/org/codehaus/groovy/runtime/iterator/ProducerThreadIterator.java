/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.runtime.iterator;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingPropertyException;

import java.util.Map;

public class ProducerThreadIterator extends BlockingQueuedIterator {

    private final Closure myClosure;
    protected boolean repeatable;

    public ProducerThreadIterator(Closure closure, Map vars) {
        super(vars);
        this.myClosure = adoptClosure(closure);

        for (int i = 0; i != poolSize; ++i) {
            WorkerThread thread = new WorkerThread();
            attachProducer(thread);
        }
    }

    public void defineLocal(String name, Object initialValue) {
        Thread thread = Thread.currentThread();
        if (thread instanceof WorkerThread) {
            ((WorkerThread) thread).localVars.define(name, initialValue);
        } else
            super.defineLocal(name, initialValue);
    }

    public int getThreadIndexInPool() {
        Thread thread = Thread.currentThread();
        if (thread instanceof WorkerThread)
            return ((WorkerThread) thread).threadIndex;
        return -1;
    }

    public Object getProperty(String property) {
        try {
            return super.getProperty(property);
        }
        catch (MissingPropertyException e) {
            Thread thread = Thread.currentThread();
            if (!(thread instanceof WorkerThread))
                throw e;
            return ((WorkerThread) thread).localVars.getProperty(property);
        }
    }

    public void setProperty(String property, Object newValue) {
        try {
            super.setProperty(property, newValue);
        }
        catch (MissingPropertyException e) {
            Thread thread = Thread.currentThread();
            if (!(thread instanceof WorkerThread))
                throw e;
            ((WorkerThread) thread).localVars.setProperty(property, newValue);
        }
    }

    private class WorkerThread extends BlockingQueuedIterator.WorkerThread {
        public WorkerThread() {
        }

        public void run() {
            try {
                do {
                    try {
                        callClosure(myClosure, null);
                    }
                    catch (GroovyRuntimeException g) {
                        GroovyRuntimeException orig = g;
                        while (g.getCause() != null && g.getCause() != g && g.getCause() instanceof GroovyRuntimeException)
                            g = (GroovyRuntimeException) g.getCause();

                        if (g instanceof BreakIterationException) {
                            break;
                        }
                        if (g instanceof ContinueIterationException) {
                            continue;
                        }
                        put(orig);
                        break;
                    }
                    catch (Throwable t) {
                        put(t);
                    }
                }
                while (repeatable);
            }
            finally {
                detachProducer();
            }
        }
    }
}
