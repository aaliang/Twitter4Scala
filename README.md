###TwStyles
####soon to be renamed to something cooler

Java libraries can be awkward to work with in Scala sometimes.
Twitter4J is no different!
This turns some of Twitter4J calls to Twitter's REST APIs into infinite (well in theory) lazy lists!
(immutable Streams)

####example usage:

```
import twstyles.TwitterService //yeah the naming isn't great right now :/

val defaultConfig = TwitterService.getDefaultConfig

//make an instance
val twitter = TwitterService(defaultConfig)

//construct a stream for "hello" as a query
val searchStream = twitter.search("hello")

//get 500 tweets from the hello stream
val fiveHundoHellos = searchStream.take(500)

//print the first 500!
fiveHundoHellos.foreach(x => println(x.getText))
```

Note:
1) does not yet handle rate limit exceeded
2) everything is done synchronously for now (most likely will stay synchronous)
3) perhaps some buffering is needed, conditional upon load