import org.springframework.util.ClassLoaderUtils

class OrderService {
	@Property transactional = false
		
	def findAllOrders() {
		println(ClassLoaderUtils.showClassLoaderHierarchy(this, "Spring managed service"))
		return OrderEntry.findAll()
	}
	
	def addOrder(orderEntry) {
		orderEntry.save()
	}
}