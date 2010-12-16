// create request object
def myrequest = [
   method:request.method,
   path:request.pathInfo,
   contextPath:request.contextPath,
   queryStr:request.queryString,
   headers:new HashMap(headers),
   params:new HashMap(params),
   bodyBytes:request.inputStream.bytes
   ]

// then ask request manager to process request
// this call will block. 
def manager = new RequestManager(memcache)
def response = manager.processRequest(myrequest)

request.response = response
forward '/view/sitebridge/index.gtpl'
