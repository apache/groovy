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
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an inner class defined as helper for an interface
 */
public class InterfaceHelperClassNode extends InnerClassNode {

    private List callSites = new ArrayList();
    
    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superClass the base class name - use "java.lang.Object" if no direct base class
     * @param callSites list of callsites used in the interface
     */
    public InterfaceHelperClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass, List<String> callSites) {
        super(outerClass, name, modifiers, superClass, ClassHelper.EMPTY_TYPE_ARRAY, MixinNode.EMPTY_ARRAY);
        setCallSites(callSites);
    }
    
    public void setCallSites(List<String> cs) {
        callSites = (cs != null) ? cs : new ArrayList<String>();
    }
    
    public List<String> getCallSites() {
        return callSites;
    }
}
