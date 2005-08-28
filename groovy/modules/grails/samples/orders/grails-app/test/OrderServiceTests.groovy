package test

class OrderServiceTests extends GroovyTestCase {
   @Property OrderService orderService

   void testLoadAllOrders() {
		assert orderService != null
		assert orderService.findAllOrders().size() == 0
   }
}