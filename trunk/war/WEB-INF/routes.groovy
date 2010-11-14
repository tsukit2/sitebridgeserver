// example routes
/*
get "/blog/@year/@month/@day/@title", forward: "/WEB-INF/groovy/blog.groovy?year=@year&month=@month&day=@day&title=@title"
get "/something", redirect: "/blog/2008/10/20/something", cache: 2.hours
get "/book/isbn/@isbn", forward: "/WEB-INF/groovy/book.groovy?isbn=@isbn", validate: { isbn ==~ /\d{9}(\d|X)/ }
*/

all '/bridgeconsole',         forward:'/bridgeconsole.groovy'
all '/bridgeconsole/**',      forward:'/bridgeconsole.groovy'
all '/bridgequery',           forward:'/bridgequery.groovy'
all '/bridgeanswer',          forward:'/bridgeanswer.groovy'
all '/**',                    forward:'/sitebridge.groovy'

// routes for the blobstore service example
/*
get "/upload",  forward: "/upload.gtpl"
get "/success", forward: "/success.gtpl"
get "/failure", forward: "/failure.gtpl"
*/
