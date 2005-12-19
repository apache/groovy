import com.google.soap.search.*;

class GoogleController {
	
	@Property String defaultClosure = "search"
	
	@Property Closure search = {
		def q = this.params["q"]
		def google = new GoogleSearch()
		google.key = "/ndZrntQFHIOTUjtA2bCOE5ulMxHovBj"
		google.queryString = q
		google.maxResults = 10;

		println "Querying Google with string = ${q}"		
		def result = google.doSearch();
		
		response.contentType = "text/xml"
		new grails.util.OpenRicoBuilder(this.response).ajax {
			element(id:"googleUrlResults") {
				for (re in result.resultElements) {
					div(class:"googleResult") {
						a(href:re.URL, re.title)
					}
				}
			}
		}
		return null
	}
	
	@Property Closure list = {
		println "hello!"
	}
}
