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
   def requests = []
   def pendingRequest = null
   while (pendingRequest = manager.getNextPendingRequest()) {
      requests << pendingRequest
   }

   headers.contentType = 'text/json'
   println JSONObject.fromObject(
      [payload:MiscUtility.deflateObjectToByteArray(
         JSONArray.fromObject(requests).toString())]).toString()
}

satisfy = {
   def body = request.reader.text
   //log.info("satisfying ${body}")
   def responses = JSONArray.fromObject(
      MiscUtility.inflateByteArrayToObj(
         MiscUtility.convertToMapAndArray(JSONObject.fromObject(body).payload) as byte[]))

   // for each response, satisfy it
   def manager = new RequestManager(memcache)
   responses.each { 
      log.info("satisfying request = ${it.responseIndex}")
      manager.satisfyRequest(it)
   }

   headers.contentType = 'text/json'
   println JSONObject.fromObject([satisfied:true]).toString()
}

"${params.action}"()

