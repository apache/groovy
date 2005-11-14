class BlogBootStrap {

     @Property Closure init = { servletContext ->
		def user = new User()
		user.firstName = "Joe"
		user.lastName = "Blogs"
		user.login = "jblogs"
		user.password = "me"
		user.email = "joe.blogs@blogs.com"
		

		
		def blog = new Blog()
		blog.owner = user
		blog.name = "Joe's Blog"
		user.blog = blog
		
		def entry = new Entry()
		entry.title = "Test Entry"
		entry.date = new Date()
		entry.body = "This is a test entry in this demo blog"
		blog.entries.add(entry)
		
		
		def comment = new Comment()
		comment.entry = entry
		comment.authorName = "Fred Flintstone"
		comment.authorEmail = "fred@blogs.com"
		comment.authorBlogURL = "http://www.blogs.com/fred"
		comment.body = "This is my comment!"		
		entry.comments.add(comment)
		
		user.save()     
     }
     @Property Closure destroy = {
     }
} 