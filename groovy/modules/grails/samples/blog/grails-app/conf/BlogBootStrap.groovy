class BlogBootStrap {

     @Property Closure init = { servletContext ->
     	
     	println "Loading Blog data"
     					
		def entry = new Entry(title:"Test Entry",date:new Date())
		entry.body = "This is a test entry in this demo blog"							
				

		entry.save() 

		def comment = new Comment()
		comment.entry = entry
		comment.authorName = "Fred Flintstone"
		comment.authorEmail = "fred@blogs.com"
		comment.authorBlogURL = "http://www.blogs.com/fred"
		comment.body = "This is my comment!"

		comment.save()
  
     }
     @Property Closure destroy = {
     }
} 