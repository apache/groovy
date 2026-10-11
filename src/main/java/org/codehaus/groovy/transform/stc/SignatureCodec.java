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
package org.codehaus.groovy.transform.stc;

import groovy.transform.TypeChecked;
import org.codehaus.groovy.ast.ClassNode;

/**
 * Encodes and decodes an inferred return type stored on
 * {@link TypeChecked.TypeCheckingInfo}.
 *
 * <p>The protocol version is that annotation's {@code version} member
 * (an {@code int}), not a field inside the encoded string. A codec
 * implements an explicit set of those ints. This release implements
 * only version 1. A new codec must still decode every older version
 * it claims to implement.
 *
 * <p>The other direction is not guesswork.
 * {@link StaticTypeCheckingVisitor.SignatureCodecFactory#getCodec}
 * returns {@code null} for every int it does not implement, including the
 * annotation default {@code 0}, any negative int, and any higher int.
 * A null codec means there is no inferred signature: the caller uses
 * the method's declared return type and does not read the bytes.
 * A release compiler refuses an experimental payload and refuses a
 * higher version it does not implement. It does not warn and then
 * parse. An experimental payload is an int that no release compiler's
 * {@code getCodec} contains; two experimental compilers are not
 * required to agree on it. A version int, once shipped in a release,
 * is append-only and is never reused for a different layout.
 *
 * <p>There is no major/minor pair and no experimental bit in the
 * class file. Do not add one in order to satisfy this rule. The
 * single {@code int} is the version.
 */
public interface SignatureCodec {
    String encode(ClassNode node);

    ClassNode decode(String signature);
}
