/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

import org.codehaus.groovy.ast.expr.EmptyExpression; 
import org.codehaus.groovy.ast.expr.ConstantExpression; 
import org.codehaus.groovy.ast.ASTNode; 

/**
 * Unit test for SingletonASTTransformation.
 *
 * @author Hamlet D'Arcy
 */
public class SingletonASTTransformationTest {

    @Test
    public void testVisit_Contract() {
        try {
            ASTNode[] badInput = new ASTNode[]{
                new ConstantExpression("sample"), 
                new EmptyExpression() 
            }; 
            new SingletonASTTransformation().visit(badInput, null); 
            Assert.fail("Contract Failure"); 
        } catch (RuntimeException ex) {
            Assert.assertTrue("Bad error msg: " + ex.getMessage(), ex.getMessage().contains("wrong types")); 
            Assert.assertTrue("Bad error msg: " + ex.getMessage(), ex.getMessage().contains("ConstantExpression")); 
            Assert.assertTrue("Bad error msg: " + ex.getMessage(), ex.getMessage().contains("EmptyExpression")); 
            Assert.assertTrue("Bad error msg: " + ex.getMessage(), ex.getMessage().contains("Expected: AnnotationNode / AnnotatedNode")); 
        }
    }

}