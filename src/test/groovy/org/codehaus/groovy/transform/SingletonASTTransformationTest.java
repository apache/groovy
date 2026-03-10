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
package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for SingletonASTTransformation.
 */
public class SingletonASTTransformationTest {

    @Test
    public void testVisit_Contract() {
        try {
            ASTNode[] badInput = new ASTNode[]{
                    new ConstantExpression("sample"),
                    EmptyExpression.INSTANCE
            };
            new SingletonASTTransformation().visit(badInput, null);
            fail("Contract Failure");
        } catch (Error e) {
            assertTrue(e.getMessage().contains("ConstantExpression"), "Bad error msg: " + e.getMessage());
            assertTrue(e.getMessage().contains("EmptyExpression"), "Bad error msg: " + e.getMessage());
            assertTrue(e.getMessage().contains("expecting [AnnotationNode, AnnotatedNode]"), "Bad error msg: " + e.getMessage());
        }
    }

}