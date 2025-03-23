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
package org.codehaus.groovy.vmplugin.v16;

import groovy.lang.GroovyRuntimeException;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.vmplugin.v10.Java10;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Java16 extends Java10 {

    {
        elementTypeToTarget.put(ElementType.RECORD_COMPONENT, AnnotationNode.RECORD_COMPONENT_TARGET);
    }

    @Override
    public int getVersion() {
        return 16;
    }

    @Override
    public Object getInvokeSpecialHandle(final Method method, final Object receiver) {
        try {
            final Class<?> receiverClass = receiver.getClass();
            // GROOVY-10145, GROOVY-10391: default interface method proxy
            if (method.isDefault() && Proxy.isProxyClass(receiverClass)) {
                return new ProxyDefaultMethodHandle((Proxy) receiver, method);
            }
            var  lookup = newLookup(receiverClass);
            if (!lookup.hasFullPrivilegeAccess()) {
                return lookup.unreflect(method).bindTo(receiver);
            }
            return lookup.unreflectSpecial(method, receiverClass).bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            return new GroovyRuntimeException(e);
        }
    }

    @Override
    public Object invokeHandle(final Object handle, final Object[] args) throws Throwable {
        if (handle instanceof ProxyDefaultMethodHandle) {
            return ((ProxyDefaultMethodHandle) handle).invokeWithArguments(args);
        }
        if (handle instanceof Throwable) throw (Throwable) handle;
        MethodHandle mh = (MethodHandle) handle;
        return mh.invokeWithArguments(args);
    }

    @Override
    protected void makeRecordComponents(final CompileUnit cu, final ClassNode cn, final Class<?> c) {
        if (c.isRecord()) cn.setRecordComponents(Arrays.stream(c.getRecordComponents()).map(rc -> {
            ClassNode type = makeClassNode(cu, rc.getGenericType(), rc.getType());
            type.addTypeAnnotations(Arrays.stream(rc.getAnnotatedType().getAnnotations()).map(this::toAnnotationNode).collect(Collectors.toList()));
            return new RecordComponentNode(cn, rc.getName(), type, Arrays.stream(rc.getAnnotations()).map(this::toAnnotationNode).collect(Collectors.toList()));
        }).collect(Collectors.toList()));
    }

    @Override
    @Incubating
    public List<String> getRecordComponentNames(Class<?> maybeRecord) {
        if (maybeRecord.isRecord()) {
            return Arrays.stream(maybeRecord.getRecordComponents()).map(RecordComponent::getName).toList();
        }
        return super.getRecordComponentNames(maybeRecord);
    }

    @Override
    @Incubating
    public List<String> getRecordComponentNames(Class<?> maybeRecord) {
        if (maybeRecord.isRecord()) {
            return Arrays.stream(maybeRecord.getRecordComponents()).map(RecordComponent::getName).toList();
        }
        return super.getRecordComponentNames(maybeRecord);
    }
}
