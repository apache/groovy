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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a map expression [1 : 2, "a" : "b", x : y] which creates a mutable Map
 */
public class MapExpression extends Expression {
    private final List<MapEntryExpression> mapEntryExpressions;

    public MapExpression() {
        this(new ArrayList<MapEntryExpression>());
    }

    public MapExpression(List<MapEntryExpression> mapEntryExpressions) {
        this.mapEntryExpressions = mapEntryExpressions;
        //TODO: get the type's of the expressions to specify the
        // map type to Map<X> if possible.
        setType(ClassHelper.MAP_TYPE);
    }

    public void addMapEntryExpression(MapEntryExpression expression) {
        mapEntryExpressions.add(expression);
    }

    public List<MapEntryExpression> getMapEntryExpressions() {
        return mapEntryExpressions;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMapExpression(this);
    }

    public boolean isDynamic() {
        return false;
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new MapExpression(transformExpressions(getMapEntryExpressions(), transformer, MapEntryExpression.class));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String toString() {
        return super.toString() + mapEntryExpressions;
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("[");
        int size = mapEntryExpressions.size();
        MapEntryExpression mapEntryExpression = null;
        if (size > 0) {
            mapEntryExpression = mapEntryExpressions.get(0);
            sb.append(mapEntryExpression.getKeyExpression().getText()).append(":").append(mapEntryExpression.getValueExpression().getText());
            for (int i = 1; i < size; i++) {
                mapEntryExpression = mapEntryExpressions.get(i);
                sb.append(", ").append(mapEntryExpression.getKeyExpression().getText()).append(":").append(mapEntryExpression.getValueExpression().getText());
                if (sb.length() > 120 && i < size - 1) {
                    sb.append(", ... ");
                    break;
                }
            }
        } else {
            sb.append(":");
        }
        sb.append("]");
        return sb.toString();
    }

    public void addMapEntryExpression(Expression keyExpression, Expression valueExpression) {
        addMapEntryExpression(new MapEntryExpression(keyExpression, valueExpression));
    }

}
