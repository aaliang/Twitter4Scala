package twstyles

import com.typesafe.config.ConfigFactory
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Query, QueryResult, TwitterFactory}

/**
 * A wrapper over 
 */
class TwitterService {

  implicit class QueryResultImproved(qr: QueryResult) {

    /***
     * @return an Option'ed result containing the queryResult of this.nextQuery() if it is
     * searched again.
     */
    def nextQueryResult(): Option[QueryResult] = {
      qr.nextQuery() match {
        case null => None
        case query => Some(twitter.search(query))
      }
    }
  }

  val (conf) = ConfigFactory.load()

  val (consumerKey, consumerSecret, accessToken, accessTokenSecret) = (
    conf.getString("twitter.consumerKey"),
    conf.getString("twitter.consumerSecret"),
    conf.getString("twitter.accessToken"),
    conf.getString("twitter.accessTokenSecret")
    )

  val (configBuilder) = new ConfigurationBuilder()

  configBuilder.setOAuthConsumerKey(consumerKey)
  configBuilder.setOAuthConsumerSecret(consumerSecret)
  configBuilder.setOAuthAccessToken(accessToken)
  configBuilder.setOAuthAccessTokenSecret(accessTokenSecret)

  val twitter = new TwitterFactory(configBuilder.build()).getInstance

  def search(str:String): QueryResult = {
    twitter.search(new Query(str))
  }

  private def shouldFetchMore(queryResult: QueryResult): Boolean = {
  }

}


