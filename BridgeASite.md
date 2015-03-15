

# Introduction #

You are reading this page because you are ready to bridge a site. You are assumed that you have some idea about how it works, and have set up both SiteBridgeServer and SiteBridgeClient successfully. If not, read [the concepts](http://code.google.com/p/sitebridgeserver/wiki/AllAboutSiteBridge) and [setup instructions](http://code.google.com/p/sitebridgeserver/wiki/BuildAndInstallation) first.

If you don't like reading documents, you can also write the SiteBridgeClient without providing any parameters. The program will show the usage as shown below.

```
Main Usage:
   [--restricted] <serverURL> <endpointURL> [<transformation script>*]
Alternative:
   warmup <serverURL>
   report [<# requests to go back - default all requests>]
```

# Let's Warm It Up #

As mentioned in the concepts page, GAE scales your application dynamically. This is good for normal sites but very bad for SiteBridge because of the increase latency when new server process is spawn up. We want to warm up the site (kick off some number of servers and keep them alive) before actually using it. To do this, just execute the following command:

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar warmup "http://<your-sitebridge-server-url>"
```

You should be seeing an output like this.

```
0    [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Kick off server #0
3486 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Kick off server #1
4553 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Keep server warm
20528 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Keep server warm
...
```


You should leave this running all the time while you are bridging a site. Also, it's strongly recommended to stop it as soon as you are no longer bridging anything. Otherwise you will be spending your free daily quota unnecessarily.

# Bridge A Site #

Once your GAE app is warmed up, you are ready to bridge a site. To do that, it's as simple as running the following command:


```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar "http://<your-sitebridge-server-url>" "http://<target-site-url>" BasicTransformer.groovy
```

What you are doing here is that you want to bridge your SiteBridgeServer to the target site using the given basic request/response transformer. If things run as expected, you'll see the following output.

```
0    [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Bridge  - Reset server succeeded
392  [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Bridge  - Query server found no pending request
1188 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Bridge  - Query server found no pending request
...
```

Once you are seeing that, your bridge site is ready. You can now test it by opening up a browser and connect to `http://<your-sitebridge-server-url>`. Note that if your actual site has additional paths like /aaa/bbb, you can just pass it like that to your bridge site as if it knows about it (e.g. `http://<your-sitebridge-server-url>/aaa/bbb`.

That's it. If you don't have the site that you want to bridge up yet, you might want to try bridging to a public site first (make sure to use the site that's not graphic heavy). For example, you can bridge the mobile CNN site with this command.

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar "http://<your-sitebridge-server-url>" "http://m.cnn.com" BasicTransformer.groovy
```

Another interesting one is mobile Wikipedia site.

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar "http://<your-sitebridge-server-url>" "http://en.m.wikipedia.org" BasicTransformer.groovy
```

If you bridge either sites above, it's strongly recommended to visit `http://<your-sitebridge-server-url>` with a mobile device.

# Bridge An HTTPS Site #

You can bridge an HTTPS site in the same manner as you would with the normal site. Just change the protocol in the URL you pass to SiteBridgeClient program. The BasicTranformer.groovy will automatically handle the cookie's domain and secure flag such that, if your GAE site is not using HTTPS, it will strip off the secure flag and the domain. This way the secure cookie will still be sent back by the browser. **It's however strongly recommended that you put your GAE bridge site on HTTPS as well if you are bridging a secure target site. This will help keep the information passed between SiteBridgeClient and SiteBridgeServer secure. Any sensitive information will not be sniffable by bad people.** In fact, since GAE already provide HTTPS for free already, there is no point for not using if your target site is using HTTPS.

You can also bridge an HTTPS site whose certificate is self-signed. These sites are usually your internal sites or your development sites. To do that, you need to tell SiteBridgeClient not verify hostname and whatnots otherwise things will blow up. SiteBridgeClient already comes with another transformer called NoCertCheckTransformer.groovy which help suppressing the check. To use it, just add it into the command. Remember that SiteBridgeClient support a **chain** of transformers. So you can theoretically have as many transformers as you like. This is an example.

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar "http://<your-sitebridge-server-url>" "http*s*://<your-self-signed-sites" BasicTransformer.groovy NoCertCheckTransformer.groovy
```

# Restricted Bridge #

Sometimes you want to expose your target site only to certain people. This might be because the content is sensitive and/or you don't want any free standers to get involved while the bridge is active. SiteBridge supports this by providing --restricted flag to turn on restriction. When on, no one can get into your site without knowing the bridge passcode. Only you and your interested parties know the bridge passcode so they can access.

To start a restricted bridge, issue the following command.

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar --restricted "http://<your-sitebridge-server-url>" "http://<target-site-url>" BasicTransformer.groovy
```

Notice the first line of the output.

```
Bridging...
0    [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Bridge  - Reset server succeeded bridgePasscode = 2599728067476682
621  [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Bridge  - Query server found no pending request
1504 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Bridge  - Query server found no pending request
...
```

The first line of the log is the bridge passcode that needs to be passed in the first request from whoever want to access the bridge. This passcode is generated from the server and it changes **every time** a bridge is established. This means that if you restart a bridge, you'll need to use a new passcode.

Passing the bridge passcode is pretty easy. If your first request is a GET, just putting **bridgePasscode** parameter right in the URL. If it's a POST, you can also embedded the parameter right in the POST body as well. If that's inconvenient or impossible, you can just make GET request to something (even non-existing) with the parameter first, then proceed with your usual POST request.

The underlying mechanism for restriction is cookie. Once the right passcode is passed, the server will generate a permit cookie. As long as that permit cookie is passed back and forth in every request to the bridge, the access is granted. Note that this SiteBridge cookie is **never** seen by the target site. Same thing with the bridgePasscode parameter as well. When the actual request hits the target site, it's as if this parameter is never passed at all.

# Write A Transformer #

Usually the two transformers given with the SiteBridgeClient are sufficient to bridge basic to medium-fancy sites. However, they may or may not necessarily work well with all sites. When things don't work, most likely the transformers you use must be modified or a new one must be added. The recommended approach is, if BasicTransformer.groovy already works correctly on your side but it misses something, you should create a new transformer file and do just what's missing. Then you can pass this new transformer into the chain. If however, BasicTransformer.groovy is doing things wrong genernally, just fix it and submit the changes back to the project.

The anatomy of a transformer is very simple. Basically it's a Groovy script with two required closure defined at top level: onRequest and onResponse. These two closures are called before the request is sent to the target site and before the response is sent back to the server. onRequest closure has access to a Map-like object called "request", while onResponse closure has access to another Map-like object called "response". These are their properties.

**request**
| **Property** | **Description** | **Type** |
|:-------------|:----------------|:---------|
| method | HTTP method | String |
| query | The query string portion that appears in the URL. | Map-like object whose value could either be a string or List-like object containing strings. |
| params | The parameters passed in POST body. Note that parameters passed in the URL do not appear here. | Map-like object whose value could either be a string or List-like object containing strings. |
| headers | HTTP headers | Map-like object whose value could either be a string or List-like object containing strings. |
| bodyBytes | Bytes array of request body | byte[.md](.md) |

**response**
| **Property** | **Description** | **Type** |
|:-------------|:----------------|:---------|
| status | Numeric status of the response | int |
| headers | HTTP headers | Map-like object whose value could either be a string or List-like object containing strings. |
| bodyBytes | Bytes array of response body | byte[.md](.md) |

You are free to modify any of these properties in your transformer. Of course, power comes with responsibility. So make very sure that you know what you are doing.

Also, a good way to start learning how to write your own transformer is to understanding how BasicTransformer.groovy works.

# Generate A Report #

Once you got everything working and you start interacting with the bridge site, behind the scene, SiteBridgeClient tracks everything that happens and save off the information into a directory called `data` under your current location. The files are just JSON text files, each represents request/response pair. If you savvy enough, you can just open up these files directly to look for the information you need. However, if you would like a friendly HTML report, you can do that by generating it with the following comand.

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar report
```

The command will generate an HTML report under `data/html`. Just open up the index.html there.

Sometimes you have a long series of interaction with the bridge site and you just want to generate the report for only the last N request/response pairs, you can pass a number to it as shown below. For example, the following command generate the report only for the last 10 interactions.

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar report 10
```

# It Simply Doesn't Work #

You've tried everything you could and you believe you did everything already but you still cannot bridge the target site. There are two possibilities. One is that there is a bug in either SiteBridgeClient or SiteBridgeServer, or there is a bug in the transformers you are using. Usually the latter is more likely (not intend to imply that the SiteBridge components are rock solid!). Therefore, the best way to start troubleshooting problems is to look into the transformers. A simple way to do that is to put print statement to print out information like headers and whatnots.

Also, the generated HTML report is very helpful. Many times you will see something wrong there (e.g. wrong header values, wrong cookie attributes).

# Things To Keep In Mind #

Although SiteBridge enables the bridge site to look and behave identical to the target site, it can only do that to the limit/restrictions of the technology upon which it's built allows. That means there are still some differences between the bridge site and your target site and sometimes these differences matter to you. For example, even though your target always send content encoded in gzip format, the bridge site may or may not send the same content in gzip format. GAE disallows the following HTTP headers and it takes control of if/when they can be set.

  * Content-Encoding
  * Content-Length
  * Date
  * Server
  * Transfer-Encoding

These are just some examples of the differences you could see. So don't be surprised when you run into one.