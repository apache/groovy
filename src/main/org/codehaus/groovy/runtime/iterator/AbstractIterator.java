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

import groovy.lang.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Abstract implementation of Iterator.
 *
 * Implementation of abstract method doCheckNext should fill hasNext and lastAquired fields.
 */
public abstract class AbstractIterator extends GroovyObjectSupport implements Iterator {
    private boolean checked;
    protected boolean hasNext;
    protected Object lastAcquired;
    protected LocalVars vars;

    public AbstractIterator(Map vars) {
        if (vars != null)
            for (Iterator it = vars.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                String key = e.getKey().toString();
                MetaProperty metaProperty = getMetaClass().getMetaProperty(key);
                if (metaProperty != null) {
                    metaProperty.setProperty(this, e.getValue());
                } else
                    defineLocal(key, e.getValue());
            }
    }

    public AbstractIterator() {
    }

    public final boolean hasNext() {
        checkNext();
        return hasNext;
    }

    public final Object next() {
        checkNext();
        if (hasNext) {
            checked = false;
            Object ret = lastAcquired;
            lastAcquired = null;
            return ret;
        } else {
            throw new NoSuchElementException();
        }
    }

    protected final void checkNext() {
        if (!checked) {
            checked = true;
            doCheckNext();
        }
    }

    public final void remove() {
        throw new UnsupportedOperationException();
    }

    protected static Object callClosure(Closure closure, Object param) {
        return param instanceof Object[] ? closure.call((Object[]) param) : closure.call(param);
    }

    protected final Closure adoptClosure(Closure closure) {
        if (closure == null)
            return null;

        Closure cloned = (Closure) closure.clone();
        cloned.setDelegate(this);
        cloned.setResolveStrategy(Closure.DELEGATE_FIRST);
        return cloned;
    }

    protected abstract void doCheckNext();

    public Object getProperty(String property) {
        Map vars = findVar(property);
        if (vars != null)
            return vars.get(property);

        return super.getProperty(property);
    }

    public void setProperty(String property, Object newValue) {
        Map vars = findVar(property);
        if (vars != null)
            vars.put(property, newValue);
        else
            super.setProperty(property, newValue);
    }

    protected Map findVar(String property) {
        if (vars != null && vars.containsKey(property))
            return vars;
        return null;
    }

    public void defineLocal(String name, Object initialValue) {
        if (vars == null) {
            vars = new LocalVars();
        }
        vars.define(name, initialValue);
    }

    protected Object prepareParams(Iterator delegate, int paramCount) {
        Object params = delegate.next();
        if (paramCount > 1) {
            if (paramCount == 2 && params instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) params;
                return new Object[]{entry.getKey(), entry.getValue()};
            }

            Object p[] = new Object[paramCount];
            p[0] = params;
            for (int i = 1; i != paramCount; ++i)
                p[i] = delegate.next();
            params = p;
        }
        return params;
    }

    static class BreakIterationException extends GroovyRuntimeException {
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    static class ContinueIterationException extends GroovyRuntimeException {
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public void breakIteration() {
        throw new BreakIterationException();
    }

    public void continueIteration() {
        throw new ContinueIterationException();
    }

    protected static class LocalVars extends HashMap {
        public void define(String property, Object initialValue) {
            if (!containsKey(property)) {
                put(property, initialValue);
            }
        }

        public Object getProperty(String property) {
            if (!containsKey(property)) {
                throw new MissingPropertyException(property, getClass());
            }

            return get(property);
        }

        public void setProperty(String property, Object newValue) {
            if (!containsKey(property)) {
                throw new MissingPropertyException(property, getClass());
            }

            put(property, newValue);
        }
    }
}
