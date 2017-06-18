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
package groovy.transform

/**
 * Class annotation used to assist in the creation of {@code Externalizable} classes.
 * The {@code @AutoExternalize} annotation instructs the compiler to execute an
 * AST transformation which adds {@code writeExternal()} and {@code readExternal()} methods
 * to a class and adds {@code Externalizable} to the interfaces which the class implements.
 * The {@code writeExternal()} method writes each property (or field) for the class while the
 * {@code readExternal()} method will read each one back in the same order. Properties or fields
 * marked as {@code transient} are ignored.
 * <p>
 * Example usage:
 * <pre>
 * import groovy.transform.*
 * {@code @AutoExternalize}
 * class Person {
 *   String first, last
 *   List favItems
 *   Date since
 * }
 * </pre>
 * Which will create a class of the following form:
 * <pre>
 * class Person implements Externalizable {
 *   ...
 *   public void writeExternal(ObjectOutput out) throws IOException {
 *     out.writeObject(first)
 *     out.writeObject(last)
 *     out.writeObject(favItems)
 *     out.writeObject(since)
 *   }
 *
 *   public void readExternal(ObjectInput oin) {
 *     first = oin.readObject()
 *     last = oin.readObject()
 *     favItems = oin.readObject()
 *     since = oin.readObject()
 *   }
 *   ...
 * }
 * </pre>
 * <p>
 * The {@code @AutoExternalize} transform is implemented as a combination of the {@code @ExternalizeMethods} and {@code @ExternalizeVerifier} transforms.
 *
 * @since 1.8.0
 */
@AnnotationCollector([ExternalizeMethods, ExternalizeVerifier])
@interface AutoExternalize { }
