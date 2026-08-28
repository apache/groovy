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
package bugs;

import java.util.ArrayList;

/**
 * GROOVY-12314: a Java collection type with public fields named for the
 * {@code size}/{@code length} property conventions; property access must
 * resolve to the fields, not be rewritten to {@code Collection#size()}.
 */
public class Groovy12314Support extends ArrayList<Object> {
    public int size = 42;
    public int length = 99;
}
