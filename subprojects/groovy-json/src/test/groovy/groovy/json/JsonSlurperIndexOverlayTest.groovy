package groovy.json



/**
 * Created by Richard on 2/2/14.
 */
class JsonSlurperIndexOverlayTest extends JsonSlurperTest {

    void setUp() {
        parser = new JsonSlurper().setType(JsonParserType.INDEX_OVERLAY)
    }
}
