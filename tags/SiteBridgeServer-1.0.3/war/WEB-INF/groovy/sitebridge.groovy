import com.eddy.sitebridgeserver.*

try {
   // then ask bridge manager to process request
   // this call will block. 
   def manager = new BridgeManager(memcache)
   def myresponse = manager.processRequest(request)

   // now relay the information to the original request
   response.status = myresponse.responseDetails.status
   myresponse.responseDetails.headers?.each  { k,v ->
      if (v instanceof List) {
         v.each { response.addHeader(k,it) }
      } else {
         response.addHeader(k,v)
      }
   }

   // and relay the body bytes
   response.outputStream << 
      MiscUtility.convertIntegerListToByteArray(myresponse.responseDetails.bodyBytes)

} catch (IllegalAccessException ex) {
   response.status = 403
}

