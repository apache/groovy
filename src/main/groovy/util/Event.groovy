package groovy.util

/**
 * This clas provides a simple event management for Groovy.
 *
 * You can add a callback to be executed when an event spawns by simply using the << operator and remove it using the >> operator
 */
public class Event
{
	private Set<Closure> subscribers = []

	/**
	 * Adds a new callback to be executed when a event spawns
	 * @param callback Callback to execute
	 */
	void leftShift(Closure callback){
		this.subscribers.add(callback)
	}

	/**
	 * Removes a callcack registered to the event
	 * @param callback Callback to be removed
	 */
	void rightShift(Closure callback){
		this.subscribers.remove(callback)
	}

	/**
	 * Fires the event
	 * @param args Arguments to be passed to the callback
	 */
	void call(Object... args){
		subscribers*.call(args)
	}
}