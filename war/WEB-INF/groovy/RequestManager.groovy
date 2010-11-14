@Singleton
public class RequestManager {

   private pendingRequests = [:]
   private queuedRequests = new LinkedList()

   public processRequest(request) {
      // queue it up first
      def requestHandle = null
      synchronized(this) {
         requestHandle = [requestKey:System.nanoTime().toString(), request:request]
         queuedRequests << requestHandle
      }

      // now lock on it and wait for it until someone satisfy it
      synchronized(requestHandle) {
         requestHandle.wait()
      }

      // once you reach here, the request has been satisfied
      return requestHandle.response
   }

   public synchronized getNextPendingRequestHandle() {
      if (queuedRequests) {
         def requestHandle = queuedRequests.removeFirst()
         pendingRequests[requestHandle.requestKey] = requestHandle

         // return the handle
         return requestHandle
      } 

      // reach here, it's empty
      return null
   }


   public satisfyRequestForKey(key,response) {
      // remove from pending
      def requestHandle = null
      synchronized(this) {
         requestHandle = pendingRequests.remove(key);
      }

      // notify the waiter
      synchronized(requestHandle) {
         requestHandle.response = response
         requestHandle.notify()
      }
   }

}

