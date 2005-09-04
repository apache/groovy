package test

import org.springframework.util.ClassLoaderUtils

class OrderService {
	@Property transactional = false
		
	def findAllOrders() {
		println(getClass().getClassLoader().loadClass("test.OrderEntry"))
		println(ClassLoaderUtils.showClassLoaderHierarchy(this, "Spring managed service"))	
		return test.OrderEntry.findAll()
	}
	
	def addOrder(orderEntry) {
		orderEntry.save()
	}
}