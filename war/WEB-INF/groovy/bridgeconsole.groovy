import net.sf.json.*
import net.sf.json.groovy.*
import com.eddy.sitebridgeserver.*

index = {
   println "this is index"

}

reset = {
   // reset it
   new BridgeManager(memcache).reset()
   headers.contentType = 'text/json'
   println JSONObject.fromObject([status:true]).toString()
}

status = {
   println "system is up"
}

warmup = {
   new BridgeManager(memcache).warmup()
   headers.contentType = 'text/json'
   println JSONObject.fromObject([status:true]).toString()
}

query = {
   // query for requests
   def manager = new BridgeManager(memcache)
   def requests = manager.getNextPendingRequests(3)

   // respond to caller
   headers.contentType = 'application/octet-stream'
   sout << MiscUtility.deflateObjectToByteArray(JSONArray.fromObject(requests).toString())
}

satisfy = {
   // satisfy all requests
   def manager = new BridgeManager(memcache)
   manager.satisfyRequests(JSONArray.fromObject(MiscUtility.inflateByteArrayToObj(request.inputStream.bytes)))

   headers.contentType = 'text/json'
   println JSONObject.fromObject([satisfied:true]).toString()
}

"${params.action}"()

