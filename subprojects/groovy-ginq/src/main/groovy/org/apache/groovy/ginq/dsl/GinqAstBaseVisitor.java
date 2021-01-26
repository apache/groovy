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
import org.apache.groovy.ginq.dsl.expression.ShutdownExpression;
import org.apache.groovy.ginq.dsl.expression.WhereExpression;
import org.codehaus.groovy.ast.CodeVisitorSupport;

/**
 * The default base visitor for GINQ AST
 *
 * @since 4.0.0
 */
public class GinqAstBaseVisitor extends CodeVisitorSupport implements GinqAstVisitor<Void> {
    @Override
    public Void visitGinqExpression(GinqExpression ginqExpression) {
        visit(ginqExpression.getFromExpression());

        for (JoinExpression joinExpression : ginqExpression.getJoinExpressionList()) {
            visit(joinExpression);
        }

        WhereExpression whereExpression = ginqExpression.getWhereExpression();
        if (null != whereExpression) {
            visit(whereExpression);
        }

        GroupExpression groupExpression = ginqExpression.getGroupExpression();
        if (null != groupExpression) {
            visit(groupExpression);
        }

        OrderExpression orderExpression = ginqExpression.getOrderExpression();
        if (null != orderExpression) {
            visit(orderExpression);
        }

        LimitExpression limitExpression = ginqExpression.getLimitExpression();
        if (null != limitExpression) {
            visit(limitExpression);
        }

        visit(ginqExpression.getSelectExpression());
        return null;
    }

    @Override
    public Void visitFromExpression(FromExpression fromExpression) {
        visit(fromExpression.getAliasExpr());
        visit(fromExpression.getDataSourceExpr());
        return null;
    }

    @Override
    public Void visitJoinExpression(JoinExpression joinExpression) {
        visit((joinExpression.getAliasExpr()));
        visit((joinExpression.getDataSourceExpr()));
        visit(joinExpression.getOnExpression());
        return null;
    }

    @Override
    public Void visitOnExpression(OnExpression onExpression) {
        visit(onExpression.getFilterExpr());
        return null;
    }

    @Override
    public Void visitWhereExpression(WhereExpression whereExpression) {
        visit(whereExpression.getFilterExpr());
        return null;
    }

    @Override
    public Void visitGroupExpression(GroupExpression groupExpression) {
        visit(groupExpression.getClassifierExpr());
        visit(groupExpression.getHavingExpression());
        return null;
    }

    @Override
    public Void visitHavingExpression(HavingExpression havingExpression) {
        visit(havingExpression.getFilterExpr());
        return null;
    }

    @Override
    public Void visitOrderExpression(OrderExpression orderExpression) {
        visit(orderExpression.getOrdersExpr());
        return null;
    }

    @Override
    public Void visitLimitExpression(LimitExpression limitExpression) {
        visit(limitExpression.getOffsetAndSizeExpr());
        return null;
    }

    @Override
    public Void visitSelectExpression(SelectExpression selectExpression) {
        visit(selectExpression.getProjectionExpr());
        return null;
    }

    @Override
    public Void visitShutdownExpression(ShutdownExpression shutdownExpression) {
        visit(shutdownExpression.getExpr());
        return null;
    }

    @Override
    public Void visit(AbstractGinqExpression expression) {
        if (null == expression) return null;
        return expression.accept(this);
    }
}
