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

import java.util.List;

/**
 * Represents one or more arguments being passed into a method by name
 */
public class NamedArgumentListExpression extends MapExpression {

    public NamedArgumentListExpression() {
    }
    
    public NamedArgumentListExpression(List<MapEntryExpression> mapEntryExpressions) {
        super(mapEntryExpressions);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new NamedArgumentListExpression(
            transformExpressions(getMapEntryExpressions(), transformer, MapEntryExpression.class)); 
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;        
    }
    
}
