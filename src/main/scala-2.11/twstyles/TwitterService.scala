package twstyles

import twitter4j.conf.{Configuration}
import twitter4j._
import scala.collection.JavaConversions._

/**
 * A wrapper over twitter4j.Twitter The goal is not to make every method Scala friendly. Methods will be added as needed.
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

  def searchStream(str: String): Stream[Status] = queryToStream(twitter search new Query(str))

  def homeTimeLineStream: Stream[Status] = pageableToStream(twitter.getHomeTimeline _)

  protected def queryToStream (queryGetter: => QueryResult): Stream[Status] = {
    val queryResult = queryGetter
    getQueryableStream(queryResult.getTweets.toList, queryResult)
  }

  protected def pageableToStream (getterFn: (Paging) => ResponseList[Status], pageSize:Int = 100, pageStart:Int = 1): Stream[Status] = {
    val page = new Paging(pageStart, pageStart)
    val initResultList = getterFn(page).toList //TODO: should probably just use toStream tbh
    getPageableStream(initResultList, page, getterFn)
  }

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

  protected def getPageableStream (remainingTweets:List[Status], page:Paging,
                                  nextResultGetter: (Paging) => ResponseList[Status]): Stream[Status] = {
    remainingTweets match {
      case Nil =>
        page.setPage(page.getCount+1)
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