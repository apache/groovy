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

int eval(Expr expr) {
    int result = switch(expr) {
        case IntExpr(i) -> i
        case NegExpr(n) -> -eval(n)
        case AddExpr(left, right) -> eval(left) + eval(right)
        case MulExpr(left, right) -> eval(left) * eval(right)
        default -> throw new IllegalStateException()
    }
    return result
}

// -2 * 5 + 4
assert -6 == eval(new AddExpr(
        left: new MulExpr(
                left: new NegExpr(n: new IntExpr(i: 2)),
                right: new IntExpr(i: 5)
        ),
        right: new IntExpr(i: 4)
))
