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
package org.codehaus.groovy.ast.expr;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a map expression [1 : 2, "a" : "b", x : y] which creates a mutable Map
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
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

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMapExpression(this);
    }

    public boolean isDynamic() {
        return false;
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new MapExpression(transformExpressions(getMapEntryExpressions(), transformer, MapEntryExpression.class));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    public String toString() {
        return super.toString() + mapEntryExpressions;
    }

    public String getText() {
        StringBuffer sb = new StringBuffer(32);
        sb.append("[");
        int size = mapEntryExpressions.size();
        MapEntryExpression mapEntryExpression = null;
        if (size > 0) {
            mapEntryExpression = mapEntryExpressions.get(0);
            sb.append(mapEntryExpression.getKeyExpression().getText() + ":" + mapEntryExpression.getValueExpression().getText());
            for (int i = 1; i < size; i++) {
                mapEntryExpression = mapEntryExpressions.get(i);
                sb.append(", " + mapEntryExpression.getKeyExpression().getText() + ":" + mapEntryExpression.getValueExpression().getText());
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
