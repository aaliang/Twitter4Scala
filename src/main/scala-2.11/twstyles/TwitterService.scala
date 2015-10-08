package twstyles

import com.typesafe.config.ConfigFactory
import twitter4j.conf.{Configuration,ConfigurationBuilder}
import twitter4j._
import scala.collection.JavaConversions._

/**
 * A wrapper over twitter4j.Twitter The goal is not to make every method stream friendly. Data will be fetched
 * only when it is needed, honoring the semantics of a lazy list. Methods will be added as needed.
 *
 * Of course, you can still call every method in the {@link twitter.4j.Twitter} via the {@link TwitterService#twitter}
 * member.
 */
class TwitterService (config: Configuration) {

  val twitter = new TwitterFactory(config).getInstance

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

  def search(str: String): Stream[Status] = queryToStream(twitter search new Query(str))

  def homeTimeLine: Stream[Status] = pageableToStream(twitter.getHomeTimeline)

  def favorites: Stream[Status] = pageableToStream(twitter.getFavorites)

  def favoritesForUser(username: String): Stream[Status] = pageableToStream(twitter.getFavorites(username, _:Paging))

  def userTimeLine: Stream[Status] = pageableToStream(twitter.getUserTimeline)

  def sentDirectMessages: Stream[DirectMessage] = pageableToStream(twitter.getSentDirectMessages)

  def myRetweets: Stream[Status] = pageableToStream(twitter.getRetweetsOfMe)

  def timelineForUser(username: String): Stream[Status] = pageableToStream(twitter.getUserTimeline(username, _:Paging))


  /**
   * Transforms any function that returns a QueryResult to a Stream of Statuses
   *
   * @param queryGetter
   * @return
   */
  protected def queryToStream (queryGetter: => QueryResult): Stream[Status] = {
    val queryResult = queryGetter
    getQueryableStream(queryResult.getTweets.toList, queryResult)
  }

  /**
   * Transforms any function that is pageable and returns a ResponseList[Status] to a Stream of Statuses
   *
   * @param getterFn any pageable function that retrieve statuses
   * @param pageSize the maximum page size that the underlying REST calls should batch, defaulted to 200
   * @param pageStart the page number to start at, defaulted to 1
   * @precondition 0 < pageSize <= 1000
   * @return
   */
  protected def pageableToStream[A] (getterFn: (Paging) => ResponseList[A], pageSize:Int = 200, pageStart:Int = 1): Stream[A] = {
    val page = new Paging(pageStart, pageSize)
    val initResultList = getterFn(page).toList //TODO: should probably just use toStream tbh
    getPageableStream(initResultList, page, getterFn)
  }

  /**
   * Constructs a Stream built via {@link QueryResult}s
   * @param remainingTweets the remaining tweets that have already been fetched somehow
   * @param currQueryResult
   * @return
   */
  protected def getQueryableStream (remainingTweets:List[Status], currQueryResult: QueryResult): Stream[Status] = {
    //N.b. the #:: / Stream.cons operator/function does not require tailrecursion to guard against a max recursion
    //error - the second argument is a thunk stored lazily
    remainingTweets match {
      case Nil =>
        currQueryResult.nextQueryResult match {
          case None => Stream.Empty
          case Some(results:QueryResult) =>
            results.getTweets.toList match {
              case Nil => Stream.Empty
              case a => a.head #:: getQueryableStream(a.tail, results)
            }
        }
      case _ => remainingTweets.head #:: getQueryableStream(remainingTweets.tail, currQueryResult)
    }
  }

  protected def getPageableStream[A] (remainingTweets:List[A], page:Paging,
                                  nextResultGetter: (Paging) => ResponseList[A]): Stream[A] = {
    remainingTweets match {
      case Nil =>
        page.setPage(page.getCount)
        nextResultGetter(page).toList match {
          case Nil => Stream.Empty
          case a =>
            val aList = a.toList
            aList.head #:: getPageableStream(aList.tail, page, nextResultGetter)
        }
      case _ => remainingTweets.head #:: getPageableStream(remainingTweets.tail, page, nextResultGetter)
    }
  }
}

object TwitterService {

  def apply(config:Configuration) = {
    new TwitterService(config)
  }

  /**
   * Gets the default twitter configuration from your resources/application.conf
   * @return
   */
  def getDefaultConfig ():Configuration = {
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

    configBuilder.build
  }
}