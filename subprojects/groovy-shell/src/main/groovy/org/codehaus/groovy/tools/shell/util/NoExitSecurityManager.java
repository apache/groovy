/*
 * Copyright (C) 2006-2007 the original author or authors.
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

package org.codehaus.groovy.tools.shell.util;

import java.security.Permission;

/**
 * Custom security manager to {@link System#exit} (and related) from being used.
 *
 * @version $Rev$ $Date$
 */
public class NoExitSecurityManager
    extends SecurityManager
{
    private final SecurityManager parent;

    public NoExitSecurityManager(final SecurityManager parent) {
        assert parent != null;

        this.parent = parent;
    }

    public NoExitSecurityManager() {
        this(System.getSecurityManager());
    }

    public void checkPermission(final Permission perm) {
        if (parent != null) {
            parent.checkPermission(perm);
        }
    }

    /**
     * Always throws {@link SecurityException}.
     */
    public void checkExit(final int code) {
        throw new SecurityException("Use of System.exit() is forbidden!");
    }

    /*
    public void checkPermission(final Permission perm) {
        assert perm != null;

        if (perm.getName().equals("exitVM")) {
            System.out.println("exitVM");
        }
    }
    */
}
