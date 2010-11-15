// create request object
def myrequest = [
   method:request.method,
   headers:new HashMap(headers),
   params:new HashMap(params)
   ]

// then ask request manager to process request
// this call will block. 
def manager = new RequestManager(memcache)
def response = manager.processRequest(myrequest)

request.response = response
forward '/view/sitebridge/index.gtpl'
