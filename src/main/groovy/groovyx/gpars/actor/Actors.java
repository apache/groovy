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

package groovyx.gpars.actor;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovyx.gpars.GParsConfig;
import groovyx.gpars.group.DefaultPGroup;

/**
 * Provides handy helper methods to create pooled actors and customize the underlying thread pool.
 * Use static import to be able to call Actors methods without the need to prepend them with the Actors identifier.
 * <pre>
 * import static org.gpars.actor.Actors.actor
 *
 * Actors.defaultActorPGroup.resize 1
 *
 * def actor = actor {
 *     react {message -&gt;
 *         println message
 *     }
 *     // this line will never be reached
 * }.start()
 *
 * actor.send 'Hi!'
 * </pre>
 * <p>
 * All actors created through the Actors class will belong to the same default actor group and run
 * on daemon threads.
 * The DefaultPGroup class should be used when actors need to be grouped into multiple groups or when non-daemon
 * threads are to be used.
 * </p>
 *
 * @author Vaclav Pech, Alex Tkachman
 *         Date: Feb 18, 2009
 */
@SuppressWarnings({"UtilityClass", "AbstractClassWithoutAbstractMethods", "AbstractClassNeverImplemented", "ConstantDeclaredInAbstractClass"})
public abstract class Actors {

    /**
     * The default actor group to share by all actors created through the Actors class.
     */
    public static final DefaultPGroup defaultActorPGroup = new DefaultPGroup(GParsConfig.retrieveDefaultPool());

    /**
     * Creates a new instance of DefaultActor, using the passed-in closure as the body of the actor's act() method.
     * The created actor will be part of the default actor group.
     *
     * @param handler The body of the newly created actor's act method.
     * @return A newly created instance of the DefaultActor class
     */
    public static DefaultActor actor(@DelegatesTo(DefaultActor.class) final Runnable handler) {
        return defaultActorPGroup.actor(handler);
    }

    /**
     * Creates a new instance of BlockingActor, using the passed-in closure as the body of the actor's act() method.
     * The created actor will be part of the default actor group.
     *
     * @param handler The body of the newly created actor's act method.
     * @return A newly created instance of the BlockingActor class
     */
    public static BlockingActor blockingActor(@DelegatesTo(BlockingActor.class) final Runnable handler) {
        return defaultActorPGroup.blockingActor(handler);
    }

    /**
     * Creates a new instance of PooledActor, using the passed-in closure as the body of the actor's act() method.
     * The created actor will be part of the default actor group.
     * The actor will cooperate in thread sharing with other actors sharing the same thread pool in a fair manner.
     *
     * @param handler The body of the newly created actor's act method.
     * @return A newly created instance of the DefaultActor class
     */
    public static DefaultActor fairActor(@DelegatesTo(DefaultActor.class) final Runnable handler) {
        return defaultActorPGroup.fairActor(handler);
    }

    /**
     * Creates a reactor around the supplied code.
     * When a reactor receives a message, the supplied block of code is run with the message
     * as a parameter and the result of the code is send in reply.
     * The created actor will be part of the default actor group.
     *
     * @param code The code to invoke for each received message
     * @return A new instance of ReactiveEventBasedThread
     */
    public static Actor reactor(@DelegatesTo(Actor.class) final Closure code) {
        return defaultActorPGroup.reactor(code);
    }

    /**
     * Creates a reactor around the supplied code, which will cooperate in thread sharing with other actors in a fair manner.
     * When a reactor receives a message, the supplied block of code is run with the message
     * as a parameter and the result of the code is send in reply.
     * The created actor will be part of the default actor group.
     *
     * @param code The code to invoke for each received message
     * @return A new instance of ReactiveEventBasedThread
     */
    public static Actor fairReactor(@DelegatesTo(Actor.class) final Closure code) {
        return defaultActorPGroup.fairReactor(code);
    }

    /**
     * Creates an instance of DynamicDispatchActor.
     *
     * @param code The closure specifying individual message handlers.
     * @return A new started instance of a DynamicDispatchActor
     */
    public static Actor messageHandler(@DelegatesTo(Actor.class) final Closure code) {
        return defaultActorPGroup.messageHandler(code);
    }

    /**
     * Creates an instance of DynamicDispatchActor, which will cooperate in thread sharing with other actors in a fair manner.
     *
     * @param code The closure specifying individual message handlers.
     * @return A new started instance of a fair DynamicDispatchActor
     */
    public static Actor fairMessageHandler(@DelegatesTo(Actor.class) final Closure code) {
        return defaultActorPGroup.fairMessageHandler(code);
    }

    /**
     * Creates an instance of StaticDispatchActor.
     *
     * @param code The closure specifying the only statically dispatched message handler.
     * @return A new started instance of a StaticDispatchActor
     */
    public static Actor staticMessageHandler(@DelegatesTo(Actor.class) final Closure code) {
        return defaultActorPGroup.staticMessageHandler(code);
    }

    /**
     * Creates an instance of StaticDispatchActor, which will cooperate in thread sharing with other actors sharing the same thread pool.
     *
     * @param code The closure specifying the only statically dispatched message handler.
     * @return A new started instance of a fair StaticDispatchActor
     */
    public static Actor fairStaticMessageHandler(@DelegatesTo(Actor.class) final Closure code) {
        return defaultActorPGroup.fairStaticMessageHandler(code);
    }
}
