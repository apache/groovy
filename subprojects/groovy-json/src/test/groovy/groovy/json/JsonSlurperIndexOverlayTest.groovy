package groovy.json



/**
 * Created by Richard on 2/2/14.
 */
class JsonSlurperIndexOverlayTest {
    public JsonSlurperIndexOverlayTest () {
        parser = new JsonSlurper().setType( JsonParserType.INDEX_OVERLAY );
    }

}
