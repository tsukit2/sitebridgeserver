package com.eddy.sitebridgeserver

import javax.servlet.http.*
import groovyx.gaelyk.logging.*
import com.google.appengine.api.memcache.*
import net.sf.json.*

/**
 * Provide the bridging service. This class works hand in hand with the client site of the sitebridge
 * to relay and receive request/response.
 */
public class BridgeManager {
   private static log = new GroovyLogger(this.name)
   private static long CHUNK_SIZE               = 900 * 1024  // make it 900K max

   private memcache

   /**
    * Constructor.
    *
    * @param memcache               MemcacheService to be used by this bridge.
    */
   public BridgeManager(MemcacheService memcache) {
      this.memcache = memcache
   }
   
   /**
    * Process the given request. This method relays the request to the client and waits until the correspoding
    * response is received.
    *
    * @param servletRequest         Servlet request.
    * 
    * @return Map representing the response.
    */
   Map processRequest(HttpServletRequest servletRequest) {
      // convert to request object that the system knows about first
      def request = constructRequestMapObject(servletRequest)

      // queue it up first
      long index = memcache.increment('lastRequestIndex', 1)
      log.info("index = $index")
      memcache['request' + index] = MiscUtility.deflateObjectToByteArray(
         [requestIndex:index, requestDetails:request])

      // now lock on it and wait for it until someone satisfy it
      log.info("request queue...waiting")
      def response = waitForResponse(index, request.pathInfo)

      // once you reach here, the request has been satisfied
      return response
   }

   /**
    * Utility method to construct request map object from the servlet request. This is the internal
    * request object that sitebridge knows about. We capture the logic of constructing it here.
    *
    * @param request                   Raw servlet request.
    *
    * @return Map representing the request object.
    */
   private Map constructRequestMapObject(HttpServletRequest request) {
      // extract various information from the servlet request and construct the request map
      def queryMap = constructQueryMap(request.queryString)
      def paramsMap = constructParamsMap(queryMap, request.parameterMap)
      return [
         method:request.method,
         pathInfo:request.getParameter('pathInfo'),
         query:queryMap,
         // here we turns the list of header to map of value, which could either be an atomic
         // or a list of values.
         headers:request.headerNames.inject([:]) { m,n ->
            def values = request.getHeaders(n).inject([]) { l,v -> l << v; v }
            m[n] = values.size() == 1 ? values[0] : values
            return m
         },
         params:paramsMap,
         bodyBytes:request.inputStream.bytes
      ]
   }

   /**
    * Utility method to constructy query map from the given query string.
    *
    * @queryStr                        Query string.
    *
    * @return Map containing the query, the value could be either an atomic or
    *       list of values.
    */
   private Map constructQueryMap(String queryStr) {
      if (queryStr) {
         return queryStr.split('&').inject([:]) { m,v -> 
            def s = v.split('=')
            if (s[0] != 'pathInfo') {
               def newval = s.size() == 2 ? URLDecoder.decode(s[1], 'utf8') : ''
               if (m.containsKey(s[0])) {
                  def val = m[s[0]]
                  m[s[0]] = val instanceof List ? val << newval : [val, newval]
               } else {
                  m[s[0]] = newval
               }
            }
            return m
         }
      } else {
         return null
      }
   }

   /**
    * Utility method to construct parameter maps. Note that query map and param map could be
    * largely overlapped. This is because of the HTTP quirk that, even in POST method, you could
    * pass parameters either via query string AND the body of the request. This will create
    * the map that are not overlap.
    *
    * @param queryMap                     Map representig the query string.
    * @param params                       The original parameter map from the servlet request.
    *
    * @return Map representing the params map.
    */
   private Map constructParamsMap(Map queryMap, Map params) {
      def paramsMap = new HashMap(params)
      queryMap?.keySet().each { paramsMap.remove(it) }
      paramsMap.remove('pathInfo')
      return paramsMap ?: null
   }

   /**
    * Utility method to wait until response becomes avaiable. The implementation relies on memcache
    * which could be set by the same or different server.
    *
    * @param index                  Index of the request.
    * @param pathInfo               Path of the request.
    *
    * @return Map representing response.
    * 
    */
   private Map waitForResponse(index, pathInfo) {
      // wait loop until response become available. If it doesn't become available, eventually app engine
      // will kill the thread running this loop
      def response = memcache['response' + index]
      log.info("waiting for: ${index} - ${pathInfo}")
      while (!response) {
         Thread.currentThread().sleep(500)
         response = memcache['response' + index]
         log.info("waiting for: " + index)
      }

      // reach here we get the response Here we massage and unpackage the response
      // and clean them all from the cache
      response = MiscUtility.inflateByteArrayToObj(response)
      assembleBodyBytesIfTooLarge(response)
      memcache.deleteAll(['request' + index, 'response' + index])
      return response
   }

   /**
    * Get the next pending request. Call this method the next pending requests. Once the requests
    * are obtained, they will be in wait states. Thus they must be satisifed by the clients.
    *
    * @param  limit                 Limit on how many requests the caller want to get back.
    *
    * @return List of Map each representing a request. This list could be empty.
    */
   List getNextPendingRequests(int limit) {
      // get the two critical indexes that we use to determine if there are pending requests
      // lastRequestIndex is the counter on how many requests arriving on the server
      // lastServeIndex is how many requests that the system has looked into, not necessarily satisfied
      def lastRequestIndex = memcache['lastRequestIndex']
      def lastServeIndex = memcache['lastServeIndex']
      log.info("lastRequestIndex = ${lastRequestIndex}, lastServeIndex = ${lastServeIndex}")

      // determine if we see any pending, if so, we get them all at once from memcache
      if (lastRequestIndex != null && lastServeIndex != null && lastRequestIndex > lastServeIndex) {
         def upperLimit = Math.min(lastServeIndex + limit, lastRequestIndex)
         def names = ((lastServeIndex+1)..upperLimit).collect { index -> 'request' + index }
         def requests = memcache.getAll(names).values()
         memcache.increment('lastServeIndex', upperLimit - lastServeIndex)
         return requests.collect { MiscUtility.inflateByteArrayToObj(it) }
      }

      // reach here, there is nothing
      return []
   }

   /**
    * Satisfy requests by the given set of responses. Call this method to make the responses available to
    * the requests that are in wait state.
    *
    * @param servletRequest         Servlet request.
    */
   void satisfyRequests(List responses) {
      /*
      // construct the responses from the servlet request
      def responses =  JSONArray.fromObject(
         MiscUtility.inflateByteArrayToObj(servletRequest.inputStream.bytes))
      */

      // note that we don't use the batch version of the memcache methods to satify multiple requests
      // at once The reason is that there could be multiple server waiting for different responses. If
      // we use the batch version, they all will wait longer as all resonses will be become available
      // at once but that means it takes more time to complete. We want to streamline the satisfaction
      // so what appengine won't kill the original client's request first
      responses.each { response ->
         response = MiscUtility.convertToMapAndArray(response)
         breakBodyBytesIfTooLarge(response)
         memcache['response' + response.responseIndex] = MiscUtility.deflateObjectToByteArray(response)
      }
   }

   /**
    * Utility method to help breaking up body bytes if it's too larget to fit memcache. This is to play
    * game with memcache's limit that the value cannot be bigger than 1MB.
    * 
    * @param response                  Map representing the response.
    */
   private breakBodyBytesIfTooLarge(Map response) {
      // break the body bytes if it's too larget
      def bodyBytes = response.responseDetails.bodyBytes
      if (bodyBytes.size() > CHUNK_SIZE) {
         // break into chucks
         def newBodyBytes = []
         bodyBytes.eachWithIndex { v,i -> 
            if (i % CHUNK_SIZE == 0) {
               newBodyBytes << [v] 
            } else {
               newBodyBytes.last() << v 
            }
         }

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
      }
   }

   /**
    * Utility method to assemble the body that was broken into smaller chunks. This works oppositely to
    * breakBodyBytesIfTooLarge method.
    *
    * @param response                  Map representing the response.
    */
   private assembleBodyBytesIfTooLarge(Map response) {
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
      }
   }

   void reset() {
      // clear all memcache
      memcache.clearAll()
      memcache['lastRequestIndex'] = 0
      memcache['lastServeIndex'] = 0
   }

   void warmup() {
      // just do something to incur some computation
      def data = (1..100).collect { new Date() }
      def sysprop = MiscUtility.convertToMapAndArray(JSONArray.fromObject(data))
      def deflate = MiscUtility.deflateObjectToByteArray(sysprop)
       MiscUtility.inflateByteArrayToObj(deflate)
   }

}

