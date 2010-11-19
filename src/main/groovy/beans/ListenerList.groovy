package groovy.beans

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass


/**
 * This annotation adds Java-style listener support to a class based on the annotated Collection-property.
 * 
 * Given the following example:
 *
 * <pre>
 * interface TestListener {
 *     void eventOccurred(TestEvent event)
 * }
 * 
 * class TestEvent {
 *     def source
 *     String message
 * 
 *     TestEvent(def source, String message) {
 *         this.source = source
 *         this.message = message
 *     }
 * }
 * 
 * class TestClass {
 *     &#064;ListenerList
 *     List<TestListener> listeners
 * }
 * </pre>
 *
 * It adds the following methods:
 *
 * <pre>
 * synchronized void addTestListener(TestListener listener)
 * synchronized void removeTestListener(TestListener listener)
 * synchronized TestListener[] getTestListeners()
 * void fireEventOccurred(TestEvent event)
 * void fireEventOccurred(Object source, String message)
 * </pre>
 *
 * The annotation can get the following parameters:
 *
 * listener    = type of the Listener instance.
 *               Default:
 *                 If the annotated collection has a single, concrete (not wildcarded) generic subtype this subtype is
 *                 used as listener type.
 *                 Otherwise java.util.Map is used.
 * event       = type of the Event to fire
 *               Default:
 *                 If the listener type has a single abstract method with a single parameter, this is used as event type
 *                 Otherwise java.lang.Object is used.
 * fire        = One or many names of event-methods to fire
 *               Format: <suffix for fire-method>[-><event-method to be called>]?
 *                 e.g. fire="eventOccurred" or fire="eventOccurred2->eventOccurred"
 *                 If the second part is omitted, the first part is used for both.
 *               Default:
 *                 All abstract methods with a single parameter derived from type of event
 * name        = name as suffix for creating the add, remove and get methods
 *               Default:
 *                 Name of the listener type
 * synchronize = Whether or not the methods created should be synchronized at the method level. 
 *               Default:
 *                 false
 * </pre>
 *
 * <p>You can add the same annotation to the class itself as well, but need to specify at least listener and event.
 * Then it autocreates a private variable called <tt><name>List</tt> (starting lowercase) and uses this to store the listeners.
 * If there already exists a compatible variable named this way, it will be used instead.
 * You can add multiple annotations to the class.
 * 
 * <p>More info on fire-method creation:
 * For each entry in the given or default created list a fire-method name will be created: <tt>fire${suffix.capitalize()}</tt>.
 * For each fire-method name a set of fire-methods will be created with the event type as single parameter and all the
 * parameter lists from the declared constructors of the event type.
 *
 * @see ListenerListASTTransformation
 *
 * @author Alexander Klein
 * @author Hamlet D'Arcy
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.FIELD, ElementType.TYPE])
@GroovyASTTransformationClass('groovy.beans.ListenerListASTTransformation')
@interface ListenerList {
    Class listener() default Void.class
    Class event() default Void.class
    String[] fire() default []
    String name() default ""
    boolean synchronize() default false
}
