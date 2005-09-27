class OrderServiceTests extends GroovyTestCase {
   @Property OrderService orderService = new OrderService()

   void testLoadAllOrders() {
		assert orderService != null   
		OrderEntry oe = new OrderEntry();
		oe.creationDate = new java.util.Date();		
		orderService.addOrder(oe);
   		assert orderService.findAllOrders().size() == 1
   }
}