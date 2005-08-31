package test

class OrderService {
	@Property transactional = true
		
	def findAllOrders() {
		println(getClass().getClassLoader().loadClass("test.OrderEntry"))
		//return OrderEntry.findAll()
		//return [ new OrderEntry() ]
		OrderEntry.testMethod()
		return [ OrderEntry.class ]
	}
	
	def addOrder(orderEntry) {
		orderEntry.save()
	}
}