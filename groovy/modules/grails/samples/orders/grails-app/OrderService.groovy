class OrderService {
	@Property transactional = true
		
	def findAllOrders() {
		return Order.findAll()
	}
	
	def addOrder(order) {
		order.save()
	}
}