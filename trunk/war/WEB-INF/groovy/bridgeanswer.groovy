log.info("params.responseText = ${params.responseText}")
RequestManager.instance.satisfyRequestForKey(params.key, params.responseText)

forward 'bridgeanswer.gtpl'

