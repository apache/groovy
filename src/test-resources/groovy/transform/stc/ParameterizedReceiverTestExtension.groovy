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
import org.codehaus.groovy.ast.GenericsType

import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics

// resolves every dynamic variable dynamically with type Optional<String> and
// silences all property missing errors
unresolvedVariable { var ->
    if (isDynamic(var)) {
        makeDynamic(var, makeClassSafeWithGenerics(make(Optional), new GenericsType(STRING_TYPE)))
        handled = true
    }
}
unresolvedProperty { pexp ->
    handled = true
}
