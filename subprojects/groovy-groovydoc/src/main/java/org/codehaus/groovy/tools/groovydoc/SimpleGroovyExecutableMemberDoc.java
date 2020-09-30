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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyExecutableMemberDoc;
import org.codehaus.groovy.groovydoc.GroovyParameter;
import org.codehaus.groovy.groovydoc.GroovyType;

import java.util.ArrayList;
import java.util.List;

public class SimpleGroovyExecutableMemberDoc extends SimpleGroovyMemberDoc implements GroovyExecutableMemberDoc {
    private static final GroovyParameter[] EMPTY_GROOVYPARAMETER_ARRAY = new GroovyParameter[0];
    List parameters;
    
    public SimpleGroovyExecutableMemberDoc(String name, GroovyClassDoc belongsToClass) {
        super(name, belongsToClass);
        parameters = new ArrayList();
    }

    @Override
    public GroovyParameter[] parameters() {
        return (GroovyParameter[]) parameters.toArray(EMPTY_GROOVYPARAMETER_ARRAY);
    }

    public void add(GroovyParameter parameter) {
        parameters.add(parameter);
    }

    
    @Override
    public String flatSignature() {/*todo*/return null;}
    @Override
    public boolean isNative() {/*todo*/return false;}
    @Override
    public boolean isSynchronized() {/*todo*/return false;}
    @Override
    public boolean isVarArgs() {/*todo*/return false;}
//    public GroovyParamTag[] paramTags() {/*todo*/return null;}
    @Override
    public String signature() {/*todo*/return null;}
    @Override
    public GroovyClassDoc[] thrownExceptions() {/*todo*/return null;}
    @Override
    public GroovyType[] thrownExceptionTypes() {/*todo*/return null;}
//    public GroovyThrowsTag[] throwsTags() {/*todo*/return null;}
//    public GroovyTypeVariable[] typeParameters() {/*todo*/return null;}
//    public GroovyParamTag[] typeParamTags() {/*todo*/return null;}
}
