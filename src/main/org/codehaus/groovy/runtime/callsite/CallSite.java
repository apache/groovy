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
package org.codehaus.groovy.runtime.callsite;

/**
 * Base class for all call sites
 *
 * @author Alex Tkachman
 */
public abstract class CallSite {
    protected final String name;
    static final CallSite DUMMY = new DummyCallSite();

    public CallSite(String name) {
        this.name = name;
    }

    /**
     * Call method 'name' of receiver with given arguments
     *
     * @param receiver receiver
     * @param args arguments
     * @return result of invocation
     */
    public abstract Object call(Object receiver, Object [] args);

    /**
     * Check if receiver/arguments are "exactly the same" as when this site was created.
     *
     * Exact meaning of "exactly the same" depends on type of the site.
     * For example, for GroovyInterceptable it is enough to check that receiver is GroovyInterceptable
     * but for site with meta method we need to be sure that classes of arguments are exactly the same
     * in the strongest possible meaning.
     *
     * @param receiver receiver
     * @param args arguments
     * @return if receiver/arguments are valid for this site
     */
    public abstract boolean accept(Object receiver, Object[] args);

    /**
     * Call site which never accept any receiver/arguments.
     * We use it as initial value for any call site.
     * It allow us to avoid additional null check on each call
     */
    static class DummyCallSite extends CallSite {
        public DummyCallSite() {
            super("");
        }

        public Object call(Object receiver, Object[] args) {
            return null;
        }

        public boolean accept(Object receiver, Object[] args) {
            return false;
        }
    }
}
