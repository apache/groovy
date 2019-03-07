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
package groovy.lang;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;

/**
 * An exception occurred if a dynamic method dispatch fails with an unknown class.
 * 
 * Note that the Missing*Exception classes were named for consistency and
 * to avoid conflicts with JDK exceptions of the same name.
 */
public class MissingClassException extends GroovyRuntimeException {

    private static final long serialVersionUID = -4059735896182191589L;
    private final String type;

    public MissingClassException(String type, ASTNode node, String message) {
        super("No such class: " + type + " " + message, node);
        this.type = type;
    }
    
    public MissingClassException(ClassNode type, String message){
        super("No such class: " + type.getName() + " " + message);
        this.type = type.getName();
    }

    /**
     * 
     * @return The type that could not be resolved
     */
    public String getType() {
        return type;
    }
}
