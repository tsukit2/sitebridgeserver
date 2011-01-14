// feel f
all '/bridgeconsole',                     forward:'/bridgeconsole.groovy?action=index'
all '/bridgeconsole/@action',             forward:'/bridgeconsole.groovy?action=@action'

// do not change these lines unless you know what you are doing. These lines set up path
// forwarding
all '/',                                  forward:'/sitebridge.groovy'
('a'..'z').each { p ->
   def path = "/@p${('a'..'a').join('/@p')}"
   all path,                              forward:"/sitebridge.groovy?pathInfo=${path}".toString()
}
