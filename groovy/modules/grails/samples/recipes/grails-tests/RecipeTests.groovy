import com.recipes.Recipe

class RecipeTests extends GroovyTestCase {

	void testDynamicMethods() {
		def r = new Recipe(title:"Chicken Tikka", description:"Yummy Indian Dish")
		r.save()
		
		r = Recipe.findByTitle("Chicken Tikka")
		assert r != null
	}
}
