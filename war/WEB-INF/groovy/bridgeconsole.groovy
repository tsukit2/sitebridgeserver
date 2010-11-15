
index = {
   println "this is index"

}

reset = {
   // clear all memcache
   memcache.clearAll()
   memcache['lastRequestIndex'] = 0
   memcache['lastServeIndex'] = 0

   println "reset successfully"
}

status = {
   println "system is up"
}

query = {
   def manager = new RequestManager(memcache)
   def requestIndex = manager.getNextPendingRequestHandle()
   log.info("query index: ${requestIndex}")

   request.requestIndex = requestIndex
   forward '/view/bridgeconsole/query.gtpl'
}

satisfy = {
   log.info("params.responseText = ${params.responseText}")
   def manager = new RequestManager(memcache)
   manager.satisfyRequestForKey(params.index, params.responseText)

   forward '/view/bridgeconsole/satisfy.gtpl'
}

"${params.action}"()
//forward 'bridgeconsole.gtpl'
