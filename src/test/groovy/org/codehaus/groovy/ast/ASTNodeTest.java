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

import junit.framework.TestCase;

import java.util.Map;

public class ASTNodeTest extends TestCase {

    ASTNode astNode = new ASTNode();

    public void testHashCode() {
        int hashcode = astNode.hashCode();

        astNode.setLineNumber(astNode.getLineNumber() + 10);
        assertEquals("hashCode is consistent", hashcode, astNode.hashCode());
    }

    // GROOVY-12744
    public void testMetaDataSnapshotsAndCopiesSeeLaterChanges() {
        astNode.putNodeMetaData("a", 1);
        assertEquals(Map.of("a", 1), astNode.getNodeMetaData());
        new ASTNode().copyNodeMetaData(astNode);

        astNode.putNodeMetaData("a", 2);
        astNode.putNodeMetaData("b", 3);
        assertEquals(Map.of("a", 2, "b", 3), astNode.getNodeMetaData());
        ASTNode copy = new ASTNode();
        copy.copyNodeMetaData(astNode);
        assertEquals(Map.of("a", 2, "b", 3), copy.getNodeMetaData());

        astNode.putNodeMetaData("c", 4);
        astNode.putNodeMetaData("d", 5);
        astNode.removeNodeMetaData("a");
        assertEquals(Map.of("b", 3, "c", 4, "d", 5), astNode.getNodeMetaData());
        copy = new ASTNode();
        copy.copyNodeMetaData(astNode);
        assertEquals(Map.of("b", 3, "c", 4, "d", 5), copy.getNodeMetaData());
    }
}
