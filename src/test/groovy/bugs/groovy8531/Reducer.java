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
package groovy.bugs.groovy8531;

interface Reducable {
    class InterfaceContext {}
}

class BaseReducer {
    public abstract class PublicBaseContext {}
    protected abstract class ProtectedBaseContext {}
    /*package*/ abstract class PackagePrivateBaseContext {}

    public static abstract class PublicStaticBaseContext {}
    protected static abstract class ProtectedStaticBaseContext {}
    /*package*/ static abstract class PackagePrivateStaticBaseContext {}

    private abstract class PrivateBaseContext {}
}

public class Reducer extends BaseReducer implements Reducable {
    public abstract class PublicContext {}
    protected abstract class ProtectedContext {}
    /*package*/ abstract class PackagePrivateContext {}

    public static abstract class PublicStaticContext {}
    protected static abstract class ProtectedStaticContext {}
    /*package*/ static abstract class PackagePrivateStaticContext {}

    private abstract class PrivateContext {}

    public enum Type { DYNAMIC, STATIC }
}
