// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2013  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.activeobject;


import groovy.transform.Memoized;
import groovyx.gpars.MessagingRunnable;
import groovyx.gpars.actor.AbstractLoopingActor;
import groovyx.gpars.actor.Actors;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.DataflowWriteChannel;
import groovyx.gpars.dataflow.Promise;
import groovyx.gpars.group.PGroup;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * Backs active objects and invokes all object's active methods.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"CallToStringEquals"})
public final class InternalActor extends AbstractLoopingActor {
    public static final String METHOD_NAME_PREFIX = "activeObject_";
    public static final String PREPROCESS_METHOD_NAME = "preProcessReturnValue";
    public static final String RECOVERY_METHOD_NAME = "recoverFromException";

    private static final long serialVersionUID = 6700367864074699984L;
    private static final Object[] NO_ARGS = {};

    /**
     * Just like DynamicDispatchActor, except that the actual method dispatch is static through the closure passed to the initialize() method.
     */
    public InternalActor() {
        initialize(new MessagingRunnable() {
            @Override
            protected void doRun(final Object argument) {
                InternalActor.this.onMessage((Object[]) argument);
            }
        });
    }

    public static InternalActor create(final Object groupId) {
        final PGroup group;

        group = "".equals(groupId) ? Actors.defaultActorPGroup : ActiveObjectRegistry.getInstance().findGroupById((String) groupId);

        if (group == null)
            throw new IllegalArgumentException("Cannot find a PGroup "
                    + groupId
                    + " in the ActiveObjectRegistry. Please make sure you register the group prior to instantiating ActiveObjects.");

        final InternalActor internalActor = new InternalActor();

        internalActor.setParallelGroup(group);
        internalActor.silentStart();

        return internalActor;
    }

    @SuppressWarnings({"unchecked"})
    private static Object handleCurrentMessage(final Object msg) {
        try {
            final Object[] params;
            params = msg instanceof Collection ? ((Collection<Object>) msg).toArray(new Object[((Collection<Object>) msg).size()]) : (Object[]) msg;

            final Object target = params[0];
            final String methodName = (String) params[1];
            final Object[] args = params.length > 2 ? Arrays.copyOfRange(params, 2, params.length) : NO_ARGS;

            return InvokerHelper.invokeMethod(target, METHOD_NAME_PREFIX + methodName, args);
        } catch (final Throwable all) {
            return all;
        }
    }

    /**
     * Handles incoming messages
     *
     * @param msg The message representing the requested method call
     */
    @SuppressWarnings({"unchecked", "MethodMayBeStatic"})
    public void onMessage(final Object[] msg) {
        final DataflowWriteChannel<Object> promise = (DataflowWriteChannel<Object>) msg[1];
        promise.leftShift(handleCurrentMessage(msg[0]));
    }

    /**
     * A DataflowVariable is passed to the actor, which will bind it once the result is known.
     *
     * @param args The method parameters
     * @return A Promise for the real result
     */
    DataflowVariable<Object> submit(final Object... args) {
        final DataflowVariable<Object> result = new DataflowVariable<Object>();

        if (this.currentThread == Thread.currentThread())
            result.leftShift(handleCurrentMessage(args));
        else
            send(new Object[]{args, result});

        return result;
    }

    /**
     * A DataflowVariable is passed to the actor, which will bind it once the result is known.
     * The method blocks waiting for the Promise to hold a value. The value is then returned back to the caller.
     *
     * @param args The method parameters
     * @return The result of the internal method as returned when run in the internal actor's context
     * @throws InterruptedException If the current thread gets interrupted while waiting for the internal actor to respond
     * @throws Throwable            If the target method invoked asynchronously throws an exception.
     */
    @SuppressWarnings({"ProhibitedExceptionDeclared"})
    Object submitAndWait(final Object... args) throws Throwable {
        if (this.currentThread == Thread.currentThread())
            return handleCurrentMessage(args);
        else {
            final Promise<Object> result = new DataflowVariable<Object>();
            send(new Object[]{args, result});
            return result.get();
        }
    }

    /**
     */
    private DataflowVariable<Object> preProcessReturnValue(final DataflowReadChannel<Object> actualResult, final Object actorHolder, final String executedMethodName) {
        final DataflowVariable<Object> resultProxy = new DataflowVariable<>();

        actualResult.getValAsync(new MessageStream() {
            @Override
            public MessageStream send(Object message) {
                if (message instanceof Exception) {
                    // Notify actor's holder class about error and ask for new value

                    final Class<?> holderClass = actorHolder.getClass();

                    // Find a suitable callback method
                    final Method recoveryMethod = getMethod(holderClass);

                    if (recoveryMethod != null) {
                        try {
                            final Object newVal = recoveryMethod.invoke(actorHolder, executedMethodName, message);
                            if (!recoveryMethod.getReturnType().equals(Void.class)) // If we can have such new value returned
                                message = newVal;
                        } catch (final Exception ignored) {
                        }
                    }
                }

                resultProxy.bind(message); // Bind the object with a (possible new) value

                return this;
            }

            @Memoized
            private Method getMethod(final Class<?> holderClass) {
                Method recoveryMethod = null;
                Class<?> currentClass = holderClass;

                try {
                    recoveryMethod = getMethodHelper(currentClass);
                } catch (final NoSuchMethodException e) {
                    while ((currentClass = currentClass.getSuperclass()) != null) {
                        try {
                            recoveryMethod = getMethodHelper(currentClass);
                        } catch (final NoSuchMethodException ignored) {
                        }
                    }
                }

                if (recoveryMethod != null)
                    recoveryMethod.setAccessible(true);

                return recoveryMethod;
            }

            private Method getMethodHelper(final Class<?> holderClass) throws NoSuchMethodException {
                return holderClass.getDeclaredMethod(RECOVERY_METHOD_NAME, String.class, Exception.class);
            }
        });

        return resultProxy; // Return a new synchronization object
    }
}