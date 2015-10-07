package twstyles

import com.typesafe.config.ConfigFactory
import twitter4j.conf.{Configuration, ConfigurationBuilder}
import twitter4j.{Query, QueryResult, TwitterFactory, Status}
import scala.annotation.tailrec
import scala.collection.JavaConversions._

sealed trait Depth
case object Infinite extends Depth
case class Finite(num: Int) extends Depth

/**
 * A wrapper over
 */
class TwitterService (config: Configuration) {

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

  val twitter = new TwitterFactory(config).getInstance

  def search(str:String): QueryResult = twitter.search(new Query(str))

  def searchStream(str:String): Stream[Status] = {
    val queryResult = search(str)
    val gt = queryResult.getTweets.toList

    gt match {
      case List() => Stream.Empty
      case a => getTweetStream(a, queryResult)
    }
  }

  //TODO: make me tailrec
  private def getTweetStream (remainingTweets:List[Status], currQueryResult: QueryResult): Stream[Status] = {
    if (remainingTweets.isEmpty) {
      currQueryResult.nextQueryResult match {
        case None => Stream.Empty
        case Some(results:QueryResult) =>
          results.getTweets.toList match {
            case List() => Stream.Empty
            case a => Stream.cons(a.head, getTweetStream(a.tail.toList, results))
          }
      }
    } else {
      val item = remainingTweets.head
      val tail = remainingTweets.tail
      Stream.cons(remainingTweets.head, getTweetStream(tail, currQueryResult))
    }
  }

}

