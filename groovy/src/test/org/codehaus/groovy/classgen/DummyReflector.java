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

package org.codehaus.groovy.classgen;

import org.codehaus.groovy.runtime.Reflector;
import org.codehaus.groovy.reflection.CachedMethod;

/**
 * This is a scratch class used to experiment with ASM to see what kind of
 * stuff is output for normal Java code
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class DummyReflector extends Reflector {

    public DummyReflector() {
    }

    /*
    public Object invoke(MetaMethod method, Object object, Object[] arguments) {
        switch (method.getMethodIndex()) {
            case 1 :
                return InvokerHelper.toObject(object.hashCode());
            case 2 :
                return object.toString();
            case 3 :
                return InvokerHelper.toObject(object.equals(arguments[0]));
            case 4 :
                return new ObjectRange((Comparable) arguments[0], (Comparable) arguments[1]);
            case 5 :
                return ((String) object).toCharArray();
            case 7 :
                return new Character("hello".charAt(2));
            case 8 :
                return null;
            default :
                return noSuchMethod(method, object, arguments);
        }
    }
    */

    public Object invoke(CachedMethod method, Object object, Object[] arguments) {
/*
        switch (method.getMethodIndex()) {
            case 1:
                return ((String) object).toCharArray();
            case 2:
                return new Boolean(((List) object).contains(arguments[0]));
            default:
                return noSuchMethod(method, object, arguments);
        }
*/
        return null;
    }

    public Object invokeConstructorAt(Object at, Object constructor, Object[] arguments) {
        return null; // noSuchMethod(method, object, arguments);
    }

    public Object invokeConstructorOf(Object constructor, Object[] arguments) {
        return null; // noSuchMethod(method, object, arguments);
    }

    char[] blah() {
        return "foo".toCharArray();
    }

}
