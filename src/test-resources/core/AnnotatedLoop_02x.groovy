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
import groovy.transform.ASTTest
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement

import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS

// --- annotated for-in loop: verify annotation is attached ---
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof ForStatement
    def annos = node.statementAnnotations
    assert annos.size() == 1
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
})
for (int i in [1, 2, 3]) {
    assert i > 0
}

// --- annotated classic for loop: verify annotation is attached ---
int sum = 0
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof ForStatement
    def annos = node.statementAnnotations
    assert annos.size() == 1
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
})
for (int i = 0; i < 5; i++) {
    sum += i
}
assert sum == 10

// --- annotated while loop: verify annotation is attached ---
int count = 0
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof WhileStatement
    def annos = node.statementAnnotations
    assert annos.size() == 1
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
})
while (count < 3) {
    count++
}
assert count == 3

// --- annotated do-while loop: verify annotation is attached ---
int x = 0
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof DoWhileStatement
    def annos = node.statementAnnotations
    assert annos.size() == 1
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
})
do {
    x++
} while (x < 4)
assert x == 4

// --- multi-annotation on for loop ---
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof ForStatement
    def annos = node.statementAnnotations
    assert annos.size() == 2
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
    assert annos[1].classNode.name == 'java.lang.SuppressWarnings'
})
@SuppressWarnings('unused')
for (String s in ['a', 'b']) {
    assert s.length() == 1
}

// --- multi-annotation on while loop (multi-line) ---
int y = 0
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof WhileStatement
    def annos = node.statementAnnotations
    assert annos.size() == 2
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
    assert annos[1].classNode.name == 'java.lang.SuppressWarnings'
})
@SuppressWarnings('unused')
while (y < 2) {
    y++
}
assert y == 2

// --- multi-annotation on do-while loop ---
int z = 0
@ASTTest(phase=SEMANTIC_ANALYSIS, value={
    assert node instanceof DoWhileStatement
    def annos = node.statementAnnotations
    assert annos.size() == 2
    assert annos[0].classNode.name == 'groovy.transform.ASTTest'
    assert annos[1].classNode.name == 'java.lang.SuppressWarnings'
})
@SuppressWarnings('unused')
do {
    z++
} while (z < 3)
assert z == 3
