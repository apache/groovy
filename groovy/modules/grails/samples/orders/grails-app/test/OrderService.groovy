package test

class OrderService {
	@Property transactional = true
		
	def findAllOrders() {
		return OrderEntry.findAll()
	}
	
	def addOrder(orderEntry) {
		orderEntry.save()
	}
}