import java.util.concurrent.CountDownLatch
import groovyx.gaelyk.logging.GroovyLogger
import com.google.appengine.api.memcache.MemcacheService

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
      memcache['request' + index] = request

      // now lock on it and wait for it until someone satisfy it
      log.info("request queue...waiting")
      def response = waitFor(index)

      // once you reach here, the request has been satisfied
      return response
   }

   private waitFor(index) {
      def response = memcache['response' + index]
      log.info("waiting: " + response)
      while (!response) {
         Thread.currentThread().sleep(3000)
         response = memcache['response' + index]
         log.info("waiting: " + response)
      }
      memcache.deleteAll(['request' + index, 'response' + index])
      return response
   }

   public getNextPendingRequestHandle() {
      log.info("about to get next request")
      def lastRequestIndex = memcache['lastRequestIndex']
      def lastServeIndex = memcache['lastServeIndex']
      if (lastRequestIndex > lastServeIndex) {
         log.info("get into the queue now")
         return memcache.increment('lastServeIndex', 1)
      }

      // reach here, there is nothing
      return null
   }

   public satisfyRequestForKey(index,response) {
      log.info("request = " + memcache['request' + index])
      memcache['response' + index] = response
   }

}

