package org.codehaus.groovy.util

class ManagedLinkedListTest extends GroovyTestCase{
  
  def list
  
  void setUp() {
    def manager = ReferenceManager.createIdlingManager(null)
    def bundle = new ReferenceBundle(manager, ReferenceType.HARD)
    list = new ManagedLinkedList(bundle)
  }
 
  void testElementAdd() {
    list.add(1)
    def i = 0
    list.each {
      assert it==1
      i++
    }
    assert i ==1 
  }
  
  void testEmptylist() {
    assert list.isEmpty()
  }
  
  void testRemoveinTheMiddle() {
    list.add(1)
    list.add(2)
    list.add(3)
    list.add(4)
    list.add(5)
    def iter = list.iterator()
    while (iter.hasNext()) {
      if (iter.next()==3) iter.remove()
    }
    def val = list.inject(0){value, it-> value+it}
    assert val == 12
  }
  
  void testAddRemove() {
    10.times {
       list.add(it)
       def iter = list.iterator()
       while (iter.hasNext()) {
         if (iter.next()==it) iter.remove()
       }
    }
    assert list.isEmpty()
  }  
}