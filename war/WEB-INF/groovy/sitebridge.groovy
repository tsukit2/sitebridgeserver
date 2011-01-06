
// create request object
def queryMap = constructQueryMap(request.queryString)
def paramsMap = constructParamsMap(queryMap, params)
def myrequest = [
   method:request.method,
   pathInfo:params.pathInfo,
   query:queryMap,
   headers:request.headerNames.inject([:]) { m,n ->
      def values = request.getHeaders(n).inject([]) { l,v -> l << v; v }
      m[n] = values.size() == 1 ? values[0] : values
      return m
   },
   params:paramsMap,
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
         v.each { response.addHeader(k,it); System.out.println "${k} = ${it}" }
      } else {
         System.out.println "${k} = ${v}"
         response.addHeader(k,v)
      }
   }
}
//log.info(new String(myresponse.responseDetails.bodyBytes as byte[]))
response.outputStream << 
   MiscUtility.convertIntegerListToByteArray(myresponse.responseDetails.bodyBytes)

def constructQueryMap(queryStr) {
   if (queryStr) {
      return queryStr.split('&').inject([:]) { m,v -> 
         def s = v.split('=')
         if (s[0] != 'pathInfo') {
            m[s[0]] = s.size() == 2 ? URLDecoder.decode(s[1], 'utf8') : ''
         }
         return m
      }
   } else {
      return null
   }
}

def constructParamsMap(queryMap, params) {
   def paramsMap = new HashMap(params)
   queryMap?.keySet().each { paramsMap.remove(it) }
   paramsMap.remove('pathInfo')
   return paramsMap ?: null
}


