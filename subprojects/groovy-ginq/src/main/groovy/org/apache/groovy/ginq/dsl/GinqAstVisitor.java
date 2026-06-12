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
import org.apache.groovy.ginq.dsl.expression.SetOperationExpression;
import org.apache.groovy.ginq.dsl.expression.ShutdownExpression;
import org.apache.groovy.ginq.dsl.expression.WhereExpression;

import java.util.Collections;
import java.util.Map;

/**
 * Represents the visitor for AST of GINQ
 *
 * @param <R> the type of visit result
 * @since 4.0.0
 */
public interface GinqAstVisitor<R> {
    /**
     * Visits a full GINQ expression.
     *
     * @param ginqExpression the expression to visit
     * @return the visit result
     */
    R visitGinqExpression(GinqExpression ginqExpression);
    /**
     * Visits a {@code from} clause.
     *
     * @param fromExpression the clause to visit
     * @return the visit result
     */
    R visitFromExpression(FromExpression fromExpression);
    /**
     * Visits a join clause.
     *
     * @param joinExpression the clause to visit
     * @return the visit result
     */
    R visitJoinExpression(JoinExpression joinExpression);
    /**
     * Visits an {@code on} clause.
     *
     * @param onExpression the clause to visit
     * @return the visit result
     */
    R visitOnExpression(OnExpression onExpression);
    /**
     * Visits a {@code where} clause.
     *
     * @param whereExpression the clause to visit
     * @return the visit result
     */
    R visitWhereExpression(WhereExpression whereExpression);
    /**
     * Visits a {@code groupby} clause.
     *
     * @param groupExpression the clause to visit
     * @return the visit result
     */
    R visitGroupExpression(GroupExpression groupExpression);
    /**
     * Visits a {@code having} clause.
     *
     * @param havingExpression the clause to visit
     * @return the visit result
     */
    R visitHavingExpression(HavingExpression havingExpression);
    /**
     * Visits an {@code orderby} clause.
     *
     * @param orderExpression the clause to visit
     * @return the visit result
     */
    R visitOrderExpression(OrderExpression orderExpression);
    /**
     * Visits a {@code limit} clause.
     *
     * @param limitExpression the clause to visit
     * @return the visit result
     */
    R visitLimitExpression(LimitExpression limitExpression);
    /**
     * Visits a {@code select} clause.
     *
     * @param selectExpression the clause to visit
     * @return the visit result
     */
    R visitSelectExpression(SelectExpression selectExpression);
    /**
     * Visits a set-operation expression.
     *
     * @param setOperationExpression the expression to visit
     * @return the visit result
     *
     * @since 6.0.0
     */
    R visitSetOperationExpression(SetOperationExpression setOperationExpression);
    /**
     * Visits a shutdown expression.
     *
     * @param shutdownExpression the expression to visit
     * @return the visit result
     */
    R visitShutdownExpression(ShutdownExpression shutdownExpression);
    /**
     * Visits an arbitrary GINQ expression.
     *
     * @param expression the expression to visit
     * @return the visit result
     */
    R visit(AbstractGinqExpression expression);
    /**
     * Updates the visitor configuration.
     *
     * @param configuration the configuration to apply
     */
    default void setConfiguration(Map<String, String> configuration) {}
    /**
     * Returns the visitor configuration.
     *
     * @return the current configuration
     */
    default Map<String, String> getConfiguration() { return Collections.emptyMap(); }
}
