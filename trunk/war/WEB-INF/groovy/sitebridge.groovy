// create request object
def myrequest = [
   method:request.method,
   pathInfo:params.pathInfo,
   query:constructQueryMap(request.queryString),
   headers:request.headerNames.inject([:]) { m,n ->
      def values = request.getHeaders(n).inject([]) { l,v -> l << v; v }
      m[n] = values.size() == 1 ? values[0] : values
      return m
   },
   params:new HashMap(params),
   bodyBytes:request.inputStream.bytes
   ]

// then ask request manager to process request
// this call will block. 
def manager = new RequestManager(memcache)
def myresponse = manager.processRequest(myrequest)

// now relay the information to the original request
response.status = myresponse.responseDetails.status
myresponse.responseDetails.headers?.each  { k,v ->
   if (k != 'Content-Encoding') {
      if (v instanceof List) {
         v.each { response.setHeader(k,it) }
      } else {
         response.setHeader(k,v)
      }
   }
}
//log.info(new String(myresponse.responseDetails.bodyBytes as byte[]))
response.outputStream << (myresponse.responseDetails.bodyBytes as byte[])

def constructQueryMap(queryStr) {
   if (queryStr) {
      return queryStr.split('&').inject([:]) { m,v -> 
         def s = v.split('=')
         if (s[0] != 'pathInfo') {
            m[s[0]] = s.size() == 2 ? s[1] : ''
         }
         return m
      }
   } else {
      return null
   }
}

