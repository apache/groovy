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
package org.codehaus.groovy.antlr.java;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter;

@Deprecated
public class Groovifier extends VisitorAdapter implements GroovyTokenTypes {
    private String currentClassName = "";
    private final boolean cleanRedundantPublic;

    public Groovifier(String[] tokenNames) {
        this(tokenNames, true);
    }

    public Groovifier(String[] tokenNames, boolean cleanRedundantPublic) {
        this.cleanRedundantPublic = cleanRedundantPublic;
    }

    public void visitClassDef(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            currentClassName = t.childOfType(GroovyTokenTypes.IDENT).getText();
        }
    }
    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            // only want to do this once per node...

            // remove 'public' when implied already if requested
            if (t.getType() == LITERAL_public && cleanRedundantPublic) {
                t.setType(EXPR);
            }

            // constructors are not distinguished from methods in java ast
            if (t.getType() == METHOD_DEF) {
                String methodName = t.childOfType(IDENT).getText();
                if (methodName != null && methodName.length() > 0) {
                    if (methodName.equals(currentClassName)) {
                        t.setType(CTOR_IDENT);
                    }
                }
            }


/*          if (t.getType() == MODIFIERS) {
                GroovySourceAST publicNode = t.childOfType(LITERAL_public);
                if (t.getNumberOfChildren() > 1 && publicNode != null) {
                    // has more than one modifier, and one of them is public

                    // delete 'public' node
                    publicNode.setType(EXPR); // near enough the same as delete for now...
                }
            }*/
            // ----
        }
    }
}
