import net.sf.json.*
import net.sf.json.groovy.*;

index = {
   println "this is index"

}

reset = {
   // clear all memcache
   memcache.clearAll()
   memcache['lastRequestIndex'] = 0
   memcache['lastServeIndex'] = 0

   headers.contentType = 'text/json'
   println JSONObject.fromObject([status:true]).toString()
}

status = {
   println "system is up"
}

query = {
   def manager = new RequestManager(memcache)
   def pendingRequest = manager.getNextPendingRequest()
   log.info("query index: ${pendingRequest?.requestIndex}")

   headers.contentType = 'text/json'
   println JSONObject.fromObject([request:pendingRequest]).toString()
}

satisfy = {
   def body = request.reader.text
   //log.info("satisfying ${body}")
   def response = JSONObject.fromObject(body)
   log.info("satisfying request = ${response.responseIndex}")
   def manager = new RequestManager(memcache)
   manager.satisfyRequest(response)

   headers.contentType = 'text/json'
   println JSONObject.fromObject([satisfied:true]).toString()
}

"${params.action}"()

