package groovy.lang

public class OrderService {

	@Property Order otherOrder
	def getOrder() {
		return new Order()
	}
}