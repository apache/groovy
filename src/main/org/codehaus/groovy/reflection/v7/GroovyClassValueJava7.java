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
package org.codehaus.groovy.reflection.v7;

import org.codehaus.groovy.reflection.GroovyClassValue;

/** GroovyClassValue implementaion that simply delegates to Java 7's java.lang.ClassValue
 * @see java.lang.ClassValue
 *
 * @param <T>
*/
public class GroovyClassValueJava7<T> extends ClassValue<T> implements GroovyClassValue<T> {
   private final ComputeValue<T> computeValue;
   public GroovyClassValueJava7(ComputeValue<T> computeValue){
      this.computeValue = computeValue;
   }
   @Override
   protected T computeValue(Class<?> type) {
      return computeValue.computeValue(type);
   }
}
