package twstyles

import com.typesafe.config.ConfigFactory
import twitter4j.conf.ConfigurationBuilder

object Boot {
  def main(args: Array[String]): Unit = {
    val tw = getTwitterServiceInstance
  }

  def getTwitterServiceInstance(): TwitterService = {
    val conf = ConfigFactory.load()

    val (consumerKey, consumerSecret, accessToken, accessTokenSecret) = (
      conf.getString("twitter.consumerKey"),
      conf.getString("twitter.consumerSecret"),
      conf.getString("twitter.accessToken"),
      conf.getString("twitter.accessTokenSecret")
      )

    val configBuilder = new ConfigurationBuilder()

    configBuilder.setOAuthConsumerKey(consumerKey)
    configBuilder.setOAuthConsumerSecret(consumerSecret)
    configBuilder.setOAuthAccessToken(accessToken)
    configBuilder.setOAuthAccessTokenSecret(accessTokenSecret)

    new TwitterService(configBuilder.build)
  }
}