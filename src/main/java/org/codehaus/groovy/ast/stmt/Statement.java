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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.ASTNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for any statement
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class Statement extends ASTNode {

    private List<String> statementLabels;

    public Statement() {
        statementLabels = null;
    }

    public List<String> getStatementLabels() {
        return statementLabels;
    }

    // TODO @Deprecated
    public String getStatementLabel() {
        // last label by default which is added first by APP
        return statementLabels == null ? null : statementLabels.get(0);
    }

    // TODO @Deprecated
    public void setStatementLabel(String label) {
        if (statementLabels == null) statementLabels = new LinkedList<>();
        statementLabels.add(label);
    }

    public void addStatementLabel(String label) {
        if (statementLabels == null) statementLabels = new LinkedList<>();
        statementLabels.add(label);
    }

    public boolean isEmpty() {
        return false;
    }

}
