class OrderServiceTests extends GroovyTestCase {
   @Property OrderService orderService

   void testLoadAllOrders() {
		assert orderService != null
   }
}