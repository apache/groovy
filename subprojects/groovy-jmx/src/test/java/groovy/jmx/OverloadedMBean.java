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
package groovy.jmx;

import java.net.URI;

public interface OverloadedMBean {

    class BaseNode {
    }

    class ChildNode extends BaseNode {
    }

    String redeploy(String url);

    String redeploy(URI uri);

    String redeploy(BaseNode info);

    String pair(String text, int count);

    String pair(int count, String text);

    String num(short value);

    String num(int value);

    String num(long value);

    String num(float value);

    String num(double value);

    String num(String value);

    String id(long value);

    String id(long[] values);

    String id(long value, int depth);

    String id(long[] values, int depth);

    String label(String value);

    String label(URI uri);

    String seq(String value);

    String seq(CharSequence value);

    String real(double value);

    String real(String value);

    String box(Integer value);

    String box(int value);

    String flag(Boolean value);

    String flag(boolean value);

    String shape(String value);

    String shape(Object value);

    String refs(String[] value);

    String refs(Object[] value);

    String bytes(byte[] value);

    String bytes(int[] value);

    String only(int value);

    String child(BaseNode value);

    String child(ChildNode value);

    String wrap(Object value);

    String wrap(int value);

    String code(char value);

    String code(short value);

    String code(int value);

    String bit(boolean value);

    String bit(int value);

    String tiny(byte value);

    String tiny(short value);

    String tiny(int value);

    String mix(String text, double value);

    String mix(URI uri, String text);

    String ping();

    String echo(String value);
}
