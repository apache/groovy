package groovy.util

import junit.framework.TestCase


class EventTest extends TestCase
{
	Event testEvent
	boolean passed1
	boolean passed2
	Closure onEvent1 = { passed1 = true }
	Closure onEvent2 = { passed2 = true }

	@Override
	public void setUp() throws Exception
	{
		testEvent = new Event()
		passed1 = false
		passed2 = false
	}

	public void testShouldHaveCalledClosure1() throws Exception
	{
		assertEquals(passed1, false)
		testEvent << onEvent1
		testEvent()
		assertEquals(passed1, true)
	}

	public void testShouldHaveCalledBothClosures() throws Exception
	{
		assertEquals(passed1, false)
		assertEquals(passed2, false)
		testEvent << onEvent1
		testEvent << onEvent2
		testEvent()
		assertEquals(passed1, true)
		assertEquals(passed2, true)
	}

	public void testShouldNotCallClosureAfterRemove() throws Exception
	{
		assertEquals(passed1, false)
		testEvent << onEvent1
		testEvent()
		assertEquals(passed1, true)
		passed1 = false
		assertEquals(passed1, false)
		testEvent >> onEvent1
		testEvent()
		assertEquals(passed1, false)
	}
}
