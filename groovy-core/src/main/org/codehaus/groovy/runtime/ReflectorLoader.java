/*
 * $Id$
 * 
 * Copyright 2003 (C) Jochen Theodorou. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
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

import java.security.ProtectionDomain;
import java.util.HashMap;

/**
 * Reflector creation helper. This class is used to define the Refloctor classes.
 * For each ClassLoader such a Loader will be created by the MetaClass.
 * The only special about this loader is, that it knows the class Reflector, 
 * which is the base class of all runtime created Reflectors. 
 * 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * @version $Revision$
 */
public class ReflectorLoader extends ClassLoader {
    private HashMap loadedClasses = new HashMap();
    
    /**
     * returns the Reflector class.
     * 
     * @return the Reflector class if the name matches
     * @throws ClassNotFoundException if the name is not matching Reflector
     * @see Reflector
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        if (delegatationLoader==null) return super.loadClass(name);
        return delegatationLoader.loadClass(name);
    }
    
    /**
     * helper method to define Reflector classes
     * @param name of the Reflector
     * @param bytecode the bytecode
     * @param domain  the protection domain
     * @return the generated class
     */
    public Class defineClass(String name, byte[] bytecode, ProtectionDomain domain) {
        Class c = defineClass(name, bytecode, 0, bytecode.length, domain);
        synchronized(loadedClasses) { loadedClasses.put(name,c); }
        resolveClass(c);
        return c;
    }
    
    /**
     * creates a RelfectorLoader. 
     * @param parent the parent loader. This should never be null!
     */
    public ReflectorLoader(ClassLoader parent) {
        super(parent);
        delegatationLoader = getClass().getClassLoader();
    }
    
    public Class getLoadedClass(String name) {
        synchronized (loadedClasses) {
            return (Class)loadedClasses.get(name);
        }
    }
    
    private ClassLoader delegatationLoader; 
}
