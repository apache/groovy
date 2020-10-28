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
package org.apache.groovy.ginq.dsl;

import org.apache.groovy.ginq.dsl.expression.AbstractGinqExpression;
import org.apache.groovy.ginq.dsl.expression.FromExpression;
import org.apache.groovy.ginq.dsl.expression.GinqExpression;
import org.apache.groovy.ginq.dsl.expression.GroupExpression;
import org.apache.groovy.ginq.dsl.expression.HavingExpression;
import org.apache.groovy.ginq.dsl.expression.JoinExpression;
import org.apache.groovy.ginq.dsl.expression.LimitExpression;
import org.apache.groovy.ginq.dsl.expression.OnExpression;
import org.apache.groovy.ginq.dsl.expression.OrderExpression;
import org.apache.groovy.ginq.dsl.expression.SelectExpression;
import org.apache.groovy.ginq.dsl.expression.WhereExpression;

/**
 * Represents the visitor for AST of GINQ
 *
 * @param <R> the type of visit result
 * @since 4.0.0
 */
public interface GinqAstVisitor<R> {
    R visitGinqExpression(GinqExpression ginqExpression);
    R visitFromExpression(FromExpression fromExpression);
    R visitJoinExpression(JoinExpression joinExpression);
    R visitOnExpression(OnExpression onExpression);
    R visitWhereExpression(WhereExpression whereExpression);
    R visitGroupExpression(GroupExpression groupExpression);
    R visitHavingExpression(HavingExpression havingExpression);
    R visitOrderExpression(OrderExpression orderExpression);
    R visitLimitExpression(LimitExpression limitExpression);
    R visitSelectExpression(SelectExpression selectExpression);
    R visit(AbstractGinqExpression expression);
}
