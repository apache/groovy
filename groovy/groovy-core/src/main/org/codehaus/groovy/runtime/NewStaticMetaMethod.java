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

import groovy.lang.MetaMethod;

import java.lang.reflect.Modifier;

/**
 * A MetaMethod implementation where the underlying method is really a static
 * helper method on some class but it appears to be an instance method on a class.
 * 
 * This implementation is used to add new methods to the JDK writing them as normal
 * static methods with the first parameter being the class on which the method is added.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */

public class NewStaticMetaMethod extends MetaMethod {

    private static final Class[] EMPTY_TYPE_ARRAY = {};
    
    private MetaMethod metaMethod;
    private Class[] logicalParameterTypes;

    public NewStaticMetaMethod(MetaMethod metaMethod) {
        super(metaMethod);
        this.metaMethod = metaMethod;
        Class[] realParameterTypes = metaMethod.getParameterTypes();
        int size = realParameterTypes.length;
        if (size <= 1) {
            logicalParameterTypes = EMPTY_TYPE_ARRAY;
        }
        else {
            logicalParameterTypes = new Class[--size];
            System.arraycopy(realParameterTypes, 1, logicalParameterTypes, 0, size);
        }
    }

    public Class getDeclaringClass() {
        return getBytecodeParameterTypes()[0];
    }

    public boolean isStatic() {
        return false;
    }

    public int getModifiers() {
        // lets clear the static bit
        return super.getModifiers() ^ Modifier.STATIC;
    }

    public Class[] getParameterTypes() {
        return logicalParameterTypes;
    }

    public Class[] getBytecodeParameterTypes() {
        return super.getParameterTypes();
    }

    public Object invoke(Object object, Object[] arguments) throws Exception {
        // we need to cheat using the type
        int size = arguments.length;
        Object[] newArguments = new Object[size + 1];
        newArguments[0] = object;
        System.arraycopy(arguments, 0, newArguments, 1, size);
        return metaMethod.invoke(null, newArguments);
    }
}
