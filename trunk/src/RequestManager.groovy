import java.util.concurrent.CountDownLatch
import groovyx.gaelyk.logging.GroovyLogger
import com.google.appengine.api.memcache.MemcacheService
import net.sf.json.*

public class RequestManager {
   private static log = new GroovyLogger(RequestManager.class.name)

   private memcache

   public RequestManager(memcache) {
      this.memcache = memcache
   }
   
   public processRequest(request) {
      // queue it up first
      long index = memcache.increment('lastRequestIndex', 1)
      log.info("index = $index")
      memcache['request' + index] = [requestIndex:index, requestDetails:request]

      // now lock on it and wait for it until someone satisfy it
      log.info("request queue...waiting")
      def response = waitFor(index)

      // once you reach here, the request has been satisfied
      return response
   }

   private waitFor(index) {
      def response = memcache['response' + index]
      log.info("waiting for: " + index)
      while (!response) {
         Thread.currentThread().sleep(1000)
         response = memcache['response' + index]
         log.info("waiting for: " + index)
      }
      memcache.deleteAll(['request' + index, 'response' + index])
      return response
   }

   public getNextPendingRequest() {
      def lastRequestIndex = memcache['lastRequestIndex']
      def lastServeIndex = memcache['lastServeIndex']
      log.info("lastRequestIndex = ${lastRequestIndex}, lastServeIndex = ${lastServeIndex}")


      // has pending request if
      if (lastRequestIndex != null && lastServeIndex != null && lastRequestIndex > lastServeIndex) {
         def index = memcache.increment('lastServeIndex', 1)
         log.info("found request ${index}")
         return memcache['request' + index]
      }

      // reach here, there is nothing
      return null
   }

   public satisfyRequest(response) {
      log.info("response ${response.responseIndex}")
      memcache['response' + response.responseIndex] = convertToMapAndArray(response)
   }

   // utility method to convert json object to map and array
   private convertToMapAndArray(jsonObj) {
      switch(jsonObj) {
         case List: 
            return jsonObj.inject([]) { l, elem -> l << convertToMapAndArray(elem); l }
         case Map:  
            return jsonObj.inject([:]) { m, entry -> m[entry.key] = convertToMapAndArray(entry.value); m }
         case JSONNull:
            return null
         default:   
            return jsonObj
      }
   }
}

