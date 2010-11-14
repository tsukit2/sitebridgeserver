// create request object
def myrequest = [
   method:request.method,
   headers:new HashMap(headers),
   params:new HashMap(params)
   ]

// then ask request manager to process request
// this call will block. 
def response = RequestManager.instance.processRequest(myrequest)


request.response = response
forward 'sitebridge.gtpl'
