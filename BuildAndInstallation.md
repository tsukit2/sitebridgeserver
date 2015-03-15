

**If you haven't read [Introduction](http://code.google.com/p/sitebridgeserver/wiki/AllAboutSiteBridge) to understand SiteBridge, it's strongly recommended to do so first.**

# Overall #

As in the Introduction, you need to two components of SiteBridge to make the magic happen: SiteBridgeServer and [SiteBridgeClient](http://code.google.com/p/sitebridgeclient/). Once you got both, the idea is simply installing the server component to as many GAE sites of your own as you want, then you are good to make a bridge to the target site for each GAE site.

# Install SiteBridgeServer #

## Requirements For SiteBridgeServer ##
This is perhaps the easiest part but it a bit requires some involvement. First you need to download Google App Engine **Java SDK**. Sorry GAE doesn't have an option of building ready-to-deploy WAR and a tool that you can do the deployment without having to mess around with the SDK. So go an download/install that. Second you need to have Groovy 1.7.5+. Note that this is not strictly needed because you can go with the pre-built already-ready-to-deploy WAR file in the download section. If you choose to build it yourself, go install Groovy system then jump to Build SiteBridgeServer section. If you go with the pre-built WAR file, just jump to Deploy SiteBridgeServer section directly.

## Build SiteBridgeServer ##

Once you get all the source files of SiteBridgeServer, all you need to do is to go in there and execute the following command to compile class.

```
groovy build.groovy
```

Assuming everything goes well, this command will generate a bunch of classes in war/WEB-INF/classes. This is it for building.

## Deploy SiteBridgeServer ##

Either you build the code yourself or you use the almost-ready-to-deploy war file, you will need to deploy the app to **your own** GAE site. If you haven't created a new GAE application under your account, do so now. See GAE's document on how to create one - very simple.

Once the GAE site/app is created, you are ready to deploy. Before you do just that, edit the following: WEB-INF/appengine-web.xml such that the name in **`<application>`** tag matches with your GAE site/app. **You need to do this first otherwise you'll have a hard time deploying it.** The deployment must go to your very own GAE site/app.

Once editing of WEB-INF/appengine-web.xml is done, execute GAE's command to deploy it. Usually this is the command to do so.

```
appcfg.sh update war
```

Check out any error. You shouldn't be getting one. Once that's you are ready to smoke test the deployment.

## Smoketest SiteBridgeServer ##

Once you are through with the deployment and everything, you can test if the system is properly install and ready to run by opening the browser and type in the following address.

> `http://<your-sitebridge-server-url>/bridgeconsole/status`

> e.g.

> `http://myfoobarsitebridge.appspot.com/bridgeconsole/status`


Note that your GAE site can be https as well. Modify the address above to match with what you actually have.

Once you enter the address, you should be getting a page saying:

```
system is up 
```

If so, you are done with server side.

# Install SiteBridgeClient #

## Requirements For SiteBridgeClient ##

If you just want to use SiteBridgeClient, all you need is a Java Runtime Environment 6 (though older versions like 5 is fine, it's strongly recommend to go with the latest version you can afford). All you need to do is to download the pre-build [SiteBridgeClient jar](http://code.google.com/p/sitebridgeclient/downloads/list) file. If you however want to build the client code, you will need Java JDK 6+ and Maven 2. Jump directly to Start Bridging section if you already downloaded the client jar. Otherwise, continue to Build SiteBridgeClient section.

## Build SiteBridgeClient ##

SiteBridgeClient is written in Groovy and is built with Maven. Theoretically speaking, once you download the [source code](http://code.google.com/p/sitebridgeclient/source/checkout) of the client, you should be able to build it by just executing the following command:

```
mvn package
```

Assuming everything goes fine, you'll find the ready-to-run jar file in `target` directory.

## Smoketest SiteBridgeClient ##

Either you've built the client or download the ready-to-run jar file, you are ready to smoke test with SiteBridgeServer that you just deployed. Just make you sure are using command line. If you build the client yourself, the way to execute it is to be in the project folder and execute this command

```
java -jar target/sitebridgeclient-1.0-jar-with-dependencies.jar warmup http://<your-sitebridge-server-url>"
```

And if you download the pre-built jar, you execute it with this command

```
java -jar sitebridgeclient-1.0-jar-with-dependencies.jar warmup "http://<your-sitebridge-server-url>"
```

Wait a little bit, and you should be seeing output like this. And if you indeed see it, this means you are done setting up everything. **Make sure to press Ctrl-C to stop the program. Do not leave it running otherwise you will be spending your free daily quota unnecessarily.**

```
0    [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Kick off server #0
3486 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Kick off server #1
4553 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Keep server warm
20528 [pool-1-thread-1] INFO  com.eddy.sitebridgeclient.Controller  - Keep server warm
...
```

If you are behind a firewall, you can specify proxy server via the `http.proxyHost` and `http.proxyPort` system properties. Just pass them via -D parameter.