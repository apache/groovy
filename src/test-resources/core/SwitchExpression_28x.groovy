import groovy.transform.CompileStatic

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

interface Expr {}
class IntExpr implements Expr {
    int i
}
class NegExpr implements Expr {
    Expr n
}
class AddExpr implements Expr {
    Expr left, right
}
class MulExpr implements Expr {
    Expr left, right
}

@CompileStatic
int evalStatic(Expr expr) {
    int result = switch(expr) {
        case IntExpr(i) -> i
        case NegExpr(n) -> -evalStatic(n)
        case AddExpr(left, right) -> evalStatic(left) + evalStatic(right)
        case MulExpr(left, right) -> evalStatic(left) * evalStatic(right)
        default -> throw new IllegalStateException()
    }
    return result
}

// -2 * 5 + 4
assert -6 == evalStatic(new AddExpr(
        left: new MulExpr(
                left: new NegExpr(n: new IntExpr(i: 2)),
                right: new IntExpr(i: 5)
        ),
        right: new IntExpr(i: 4)
))

int evalDynamic(Expr expr) {
    int result = switch(expr) {
        case IntExpr(i) -> i
        case NegExpr(n) -> -evalDynamic(n)
        case AddExpr(left, right) -> evalDynamic(left) + evalDynamic(right)
        case MulExpr(left, right) -> evalDynamic(left) * evalDynamic(right)
        default -> throw new IllegalStateException()
    }
    return result
}
// -2 * 5 + 4
assert -6 == evalDynamic(new AddExpr(
        left: new MulExpr(
                left: new NegExpr(n: new IntExpr(i: 2)),
                right: new IntExpr(i: 5)
        ),
        right: new IntExpr(i: 4)
))
