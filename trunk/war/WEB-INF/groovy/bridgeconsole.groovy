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

def timeit(label, closure) {
   def startTime = System.currentTimeMillis()
   def ret = closure()
   log.info("${label}: ${System.currentTimeMillis() - startTime} ms")
   return ret
}

warmup = {
   def data = (1..100).collect { new Date() }
   def sysprop = MiscUtility.convertToMapAndArray(JSONArray.fromObject(data))
   def deflate = MiscUtility.deflateObjectToByteArray(sysprop)
   def inflate = MiscUtility.inflateByteArrayToObj(deflate)

   headers.contentType = 'text/json'
   println JSONArray.fromObject(inflate).toString()
}

query = {
   def manager = new RequestManager(memcache)
   def requests = timeit("getNextPendingRequests") { manager.getNextPendingRequests() }

   headers.contentType = 'text/json'

   timeit("Serialize JSON") {
      println JSONObject.fromObject(
         [payload:MiscUtility.deflateObjectToByteArray(
            JSONArray.fromObject(requests).toString())]).toString()
   }
}

satisfy = {
   def body = request.reader.text
   //log.info("satisfying ${body}")
   def responses = timeit("Deserialize input") { JSONArray.fromObject(
      MiscUtility.inflateByteArrayToObj(
         MiscUtility.convertToMapAndArray(JSONObject.fromObject(body).payload) as byte[]))
   }

   // satisfy all requests
   def manager = new RequestManager(memcache)
   timeit("Satisfy request") { manager.satisfyRequests(responses) }

   headers.contentType = 'text/json'
   println JSONObject.fromObject([satisfied:true]).toString()
}

"${params.action}"()

