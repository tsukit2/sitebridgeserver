

# What The Heck Is It? #

Welcome to SiteBridge! SiteBridge allows you to bridge a Google App Engine (GAE) site to a target site. With proper transformers written (default/basic/generic version is provided) the GAE bridge site looks and behaves identically to the target site. The target site is usually inaccessible directly from the rest of the world (e.g. behind firewall). This allows one to temporarily expose it so testing and/or showcasing it are possible.

# Why Bother? #

SiteBridge was initially and purposely created to remove the pain of its author, Ed Tretriluxana, that'd been irritating him when developing web-based and server-backed mobile applications. Why? His development environment is behind a firewall, a usual setup for almost all enterprise development. He very often needs to test/troubleshoot the apps on these mobile devices. Unfortunately these devices do not and cannot see his development or internal sites because they are not on the same network. For security reason, the company doesn't provide any WIFI network either. So the possibility of these devices can get on the same network in order to be able to see his server-side systems is gone. Thus, all he'd been ending up doing in the past was to use simulators or browser plugins to imitate these devices. However, they are and never will be the same. He wants to temporarily make the site accessible to devices.

# What Can I Do With It? #

If you are like Ed, you will appreciate the existence of this project without much explanation. Anyhow, these are some scenarios that you might want to use it for, not necessarily relevant to mobile application development.

  * Ed's situation. See "Why Bother?".
  * You are working for a client. You want to showcase what you have been developing for them. However, you don't have a site set up yet so that they can access it (e.g. the site doesn't exist yet in case of a brand new site, or it's being occupied by a production version in case of you are making changes to it).
  * Customers are reporting problems with your production site. You don't find much on the logs how the problems occur. You want to be able to see how the information flow in and out of your site when they are using.
  * You want to observe how a certain site works.
  * You want to create a fake site so that you can steal people's confidential information <- **PLEASE DO NOT DO THIS! IT'S ILLEGAL**.
  * ...

# How Does It Work? #

SiteBridge works very simply. Basically there are two components: server and client. The server is a GAE application. You create one for yourself then deploy SiteBrigeServer into it. For client, the SiteBridgeClient is a command-line Java app (written in Groovy). When run, the client connects to the server querying for any pending requests. If so, the original requests are obtained, transformed, and relayed to the target site. Once the responses are received, the client transforms and passes them back to the server. Then the server relays them back to the original client. The final effect is the bridge site behave identically as the target site.

# What Transformers? #

When you bridge a site, you cannot just present the content of one via the other and everything will be just fine. Cookies, links, headers, javascripts and whatnots can be site specific. For example, a cookie can be specified for the original site's domain. If passed as is, the browser will never send it back when connecting to the bridge site.

A transformer is a groovy script that you can specify when running the client. Transforms will be presented with every request and response the client see. They can observe and manipulate HTTP elements (e.g. headers, body) before they are sent to GAE server app. This way any site being bridged can be theoretically re-presented seamlessly through the bridge GAE site.

For convenience, a basic/default is provided which you can use as it or enhance it. Also, since SiteBridgeClient support a chain of multiple transformers, you write your own and add it to the chain.

# Should/Can I Use It? #

SiteBridge is not for everyone - period. All GAE apps must follow a rule that all requests must be fulfilled within a strict 30 seconds or less timeframe. That means web sites with heavy graphics will likely have problems because graphics take time to hop around and we got many hops happenning between server and client. Note that this is a deliberate design choice (not an overlook) so that client can be used even in the most restricted environment.

Given above, this explains why SiteBridge excels with mobile sites (usually these sites are very light) and regular sites with moderate graphic contents. The best way to find out if you can use it is to try it.

# Isn't It Warm Enough? #

GAE apps are known to have an issue with latency when new server processes are created dynamically as the load is increasing. SiteBridgeServer is no exception and this creates problem because the latency is counted toward the total time a request can wait. Without doing anything, you can experience often timeout on requests.

In order to circumvent this, SiteBridge has a process (coordinated by both server and client) to keep the server warm. Client makes a bunch of connections back to the server to do some foobar computation. This causes GAE to spawn off more server instances. Client keeps doing this periodically to keep these server alive. So when the actual requests coming in, GAE got enough server processes to serve the request.

# One Last Thing!?! #

SiteBridgeClient tracks all information it receives and sends. This allows it to offer, with no extra charge, an extensive reports on what's going on when you interact with SiteBridge. The report has information like what requests were made and what responses were returned. You can examine headers and actual contents. This is available for both browser-vs-SiteBridgeServer and SiteBridgeClient-vs-targetsite interactions. You can generate an HTML report simply by running the client.