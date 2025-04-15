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
package bugs;

import org.objectweb.asm.Opcodes;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.transform.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class G3839Transform3 implements ASTTransformation, Opcodes{

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode classNode = (ClassNode) nodes[1];
        classNode.addField(new FieldNode("f3", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, classNode, null));
    }

}
