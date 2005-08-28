package test

class OrderService {
	@Property transactional = true
		
	def findAllOrders() {
		println(getClass().getClassLoader().loadClass("test.OrderEntry"))
		return OrderEntry.findAll()
	}
	
	def addOrder(orderEntry) {
		orderEntry.save()
	}
}