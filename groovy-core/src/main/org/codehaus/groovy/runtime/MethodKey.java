/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.codehaus.groovy.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract base class for a key used for comparators and Map keys to lookup a method by
 * name and parameter types
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class MethodKey {

    private int hash;
    private String name;
    private Class sender;
    
    public MethodKey(Class sender, String name) {
        this.sender = sender;
        this.name = name;
    }

    /**
     * Creates an immutable copy that we can cache. 
     */
    public MethodKey createCopy() {
        int size = getParameterCount();
        Class[] paramTypes = new Class[size];
        for (int i = 0; i < size; i++) {
            paramTypes[i] = getParameterType(i);
        }
        return new DefaultMethodKey(sender, name, paramTypes);
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        else if (hashCode() == that.hashCode() && that instanceof MethodKey) {
            return equals((MethodKey) that);
        }
        return false;
    }

    public boolean equals(MethodKey that) {
        int size = getParameterCount();
        if (sender!=that.sender) return false;
        if (name.equals(that.name) && size == that.getParameterCount()) {
            for (int i = 0; i < size; i++) {
                if (!getParameterType(i).equals(that.getParameterType(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (hash == 0) {
            hash = createHashCode();
            if (hash == 0) {
                hash = 0xcafebabe;
            }
        }
        return hash;
    }

    public String toString() {
        return super.toString() + "[name:" + name + "; params:" + getParamterTypes();
    }

    public String getName() {
        return name;
    }

    public List getParamterTypes() {
        int size = getParameterCount();
        if (size <= 0) {
            return Collections.EMPTY_LIST;
        }
        List params = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            params.add(getParameterType(i));
        }
        return params;
    }

    public abstract int getParameterCount();
    public abstract Class getParameterType(int index);

    protected int createHashCode() {
        int answer = name.hashCode();
        int size = getParameterCount();

        /** @todo we should use the real Josh Bloch algorithm here */

        // can't remember the exact Josh Bloch algorithm and I've not got the book handy
        // but its something like this IIRC
        for (int i = 0; i < size; i++) {
            answer *= 37;
            answer += 1 + getParameterType(i).hashCode();
        }
        answer *= 37;
        answer += 1 + sender.hashCode();
        return answer;
    }
}
