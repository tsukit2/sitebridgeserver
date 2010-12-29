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
      memcache['request' + index] = MiscUtility.deflateObjectToByteArray(
         [requestIndex:index, requestDetails:request])

      // now lock on it and wait for it until someone satisfy it
      log.info("request queue...waiting")
      def response = waitFor(index, request)

      // once you reach here, the request has been satisfied
      return response
   }

   private waitFor(index, request) {
      def response = memcache['response' + index]
      log.info("waiting for: ${index} - ${request.pathInfo}")
      //def startTime = System.currentTimeMillis()
      // only wait for 15 seconds
      while (!response /*&& System.currentTimeMillis() - startTime < 30000*/) {
         Thread.currentThread().sleep(500)
         response = memcache['response' + index]
         log.info("waiting for: " + index)
      }

      // if reach here, still no response, throw exception
      if (!response) {
         throw new RuntimeException('Too long to wait for response')
      }

      response = MiscUtility.inflateByteArrayToObj(response)
      resembleBodyBytesIfTooLarge(response)
      memcache.deleteAll(['request' + index, 'response' + index])
      return response
   }

   public getNextPendingRequests() {
      def lastRequestIndex = memcache['lastRequestIndex']
      def lastServeIndex = memcache['lastServeIndex']
      log.info("lastRequestIndex = ${lastRequestIndex}, lastServeIndex = ${lastServeIndex}")

      // has pending request if
      if (lastRequestIndex != null && lastServeIndex != null && lastRequestIndex > lastServeIndex) {
         def names = ((lastServeIndex+1)..lastRequestIndex).collect { index -> 'request' + index }
         //println "**** ${names}"
         def requests = memcache.getAll(names).values()
         memcache.increment('lastServeIndex', lastRequestIndex - lastServeIndex)
         return requests.collect { MiscUtility.inflateByteArrayToObj(it) }
      }

      // reach here, there is nothing
      return []
   }

   public satisfyRequests(responses) {
      /*
      def responseMap = responses.inject([:]) { map, response ->
         log.info("response ${response.responseIndex}")
         response = MiscUtility.convertToMapAndArray(response)
         breakBodyBytesIfTooLarge(response)
         map['response' + response.responseIndex] = MiscUtility.deflateObjectToByteArray(response)
         return map
      }
      memcache.putAll(responseMap)
      */

      responses.each { response ->
         response = MiscUtility.convertToMapAndArray(response)
         breakBodyBytesIfTooLarge(response)
         memcache['response' + response.responseIndex] = MiscUtility.deflateObjectToByteArray(response)
      }
   }

   private breakBodyBytesIfTooLarge(response) {
      // break the body bytes if it's too larget
      def bodyBytes = response.responseDetails.bodyBytes
      def chuckSize = 7680
      if (bodyBytes.size() > chuckSize) {
         // break into chucks
         def newBodyBytes = []
         bodyBytes.eachWithIndex { v,i -> 
            if (i % chuckSize == 0) {
               newBodyBytes << [v] 
            } else {
               newBodyBytes.last() << v 
            }
         }
         //println "**** ${newBodyBytes.size()}"

         // then replace each chuck with name reference to memcache value
         def chunks = [:]
         newBodyBytes.size().times { index ->
            def name = "response${response.responseIndex}-bodyByte${index}".toString()
            chunks[name] = MiscUtility.deflateObjectToByteArray(newBodyBytes[index])
            newBodyBytes[index] = name
         }
         memcache.putAll(chunks)

         // finally replace the body bytes with the new one
         response.responseDetails.bodyBytes = newBodyBytes
         //println(response.responseDetails.bodyBytes)
      }
   }

   private resembleBodyBytesIfTooLarge(response) {
      def bodyBytes = response.responseDetails.bodyBytes
      if (bodyBytes.size() && bodyBytes.first() instanceof String) {
         // resemble the bytes together again and remove cache
         def chunks = memcache.getAll(bodyBytes)
         memcache.deleteAll(bodyBytes)
         def newBodyBytes = []
         bodyBytes.each { name -> 
            newBodyBytes.addAll(MiscUtility.inflateByteArrayToObj(chunks[name])) 
         }

         // finally replace the body bytes with the new one
         response.responseDetails.bodyBytes = newBodyBytes
         //println("**** ${response.responseDetails.bodyBytes.size()}")
      }

      /*
      // decompress the body bytes no matter what
      if (response.responseDetails.bodyBytes) {
         response.responseDetails.bodyBytes = new GZIPInputStream(
            new ByteArrayInputStream(response.responseDetails.bodyBytes as byte[])).bytes
      }
      */
   }
}

