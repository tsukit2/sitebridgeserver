log.info('before query')
def requestHandle = RequestManager.instance.getNextPendingRequestHandle()
log.info('after query')

request.requestHandle = requestHandle
forward 'bridgequery.gtpl'
