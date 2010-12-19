// example routes
/*
get "/blog/@year/@month/@day/@title", forward: "/WEB-INF/groovy/blog.groovy?year=@year&month=@month&day=@day&title=@title"
get "/something", redirect: "/blog/2008/10/20/something", cache: 2.hours
get "/book/isbn/@isbn", forward: "/WEB-INF/groovy/book.groovy?isbn=@isbn", validate: { isbn ==~ /\d{9}(\d|X)/ }
*/

all '/bridgeconsole',                     forward:'/bridgeconsole.groovy?action=index'
all '/bridgeconsole/@action',             forward:'/bridgeconsole.groovy?action=@action'
//all '/**',                                forward:'/sitebridge.groovy'
all '/@pa/@pb/@pc/@pd/@pe/@pf/@pg/@ph',   forward:'/sitebridge.groovy?pathInfo=/@pa/@pb/@pc/@pd/@pe/@pf/@pg/@ph'
all '/@pa/@pb/@pc/@pd/@pe/@pf/@pg',       forward:'/sitebridge.groovy?pathInfo=/@pa/@pb/@pc/@pd/@pe/@pf/@pg'
all '/@pa/@pb/@pc/@pd/@pe/@pf',           forward:'/sitebridge.groovy?pathInfo=/@pa/@pb/@pc/@pd/@pe/@pf'
all '/@pa/@pb/@pc/@pd/@pe',               forward:'/sitebridge.groovy?pathInfo=/@pa/@pb/@pc/@pd/@pe'
all '/@pa/@pb/@pc/@pd',                   forward:'/sitebridge.groovy?pathInfo=/@pa/@pb/@pc/@pd'
all '/@pa/@pb/@pc',                       forward:'/sitebridge.groovy?pathInfo=/@pa/@pb/@pc'
all '/@pa/@pb',                           forward:'/sitebridge.groovy?pathInfo=/@pa/@pb'
all '/@pa',                               forward:'/sitebridge.groovy?pathInfo=/@pa'
all '/',                                  forward:'/sitebridge.groovy'

// routes for the blobstore service example
/*
get "/upload",  forward: "/upload.gtpl"
get "/success", forward: "/success.gtpl"
get "/failure", forward: "/failure.gtpl"
*/
