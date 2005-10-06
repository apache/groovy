import com.google.soap.search.*;

class GoogleController {
	
	@Property String defaultClosure = "search"
	
	@Property Closure search = {
		request,response ->
		def q = request["q"][0];
		def google = new GoogleSearch();
		google.key = "/ndZrntQFHIOTUjtA2bCOE5ulMxHovBj";
		google.queryString = q;
		google.maxResults = 10;

		println "Querying Google with string = ${q}"		
		def result = google.doSearch();
		
		response.contentType = "text/xml"
		new grails.util.OpenRicoBuilder(response).ajax {
			object(id:"googleAutoComplete") {
				for (re in result.resultElements) {
					div(class:"autoCompleteResult", re.URL)
				}
			}
		}
		return null;
	}
}
