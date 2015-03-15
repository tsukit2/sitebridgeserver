SiteBridge is a site bridging system that allows developers to connect his/her locally developed app to an externally accessible site. It's aimed to get rid of the pain of the fact that you are still internally developing an app but want to test how it looks and feels when externally accessible. An obvious example of this is a web app for mobile devices. Usually these devices are on a different network than that used in development. So you won't be able to test it with real devices unless you have a test site, or you have control over everything including providing WIFI network that you mobile devices can get on. However, so many people do not have this luxury especially those working in enterprise environment. This is a pain and even you have a test site it's often still very awkward to incorporate it into the rapid development cycle. Sometimes you just want a quick test to see how things go such as troubleshooting.

This is a quick introduction video.

<a href='http://www.youtube.com/watch?feature=player_embedded&v=UxhLthKSr_E' target='_blank'><img src='http://img.youtube.com/vi/UxhLthKSr_E/0.jpg' width='425' height=344 /></a>

The way SiteBridgeServer works is very simple conceptually. Clients connect to this GAE app and it will queue up the clients' requests to be satisfied by the web app being locally developed. Then on the development machine, you run SiteBridgeClient to pipe requests being queued to the actual web app back and forth. Finally the response is sent back to this GAE app which is in turn returned to the client.

In addition to the above, SiteBridgeClient also sports tracking where you can see look through the traffic between client and the actual server. This is very useful in troubleshooting a lot of things.

The server-side component is written as a GAE app using Groovy and Gaelyk. The client-site component is a normal jar-based application, written with HTTPBuilder and JSON-lib.

Please see [Wiki section](http://code.google.com/p/sitebridgeserver/w/list) for details.

For all questions/inquiries, please send them to user@groovy.codehaus.org.