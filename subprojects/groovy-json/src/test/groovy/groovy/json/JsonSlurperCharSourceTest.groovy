package groovy.json



/**
 * Created by Richard on 2/2/14.
 */
class JsonSlurperCharSourceTest extends JsonSlurperTest {

    void setUp() {
        parser = new JsonSlurper().setType(JsonParserType.CHARACTER_SOURCE)
    }
}
