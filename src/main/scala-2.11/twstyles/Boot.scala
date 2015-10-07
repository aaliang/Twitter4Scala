package twstyles

//nothing to see here
object Boot {
  def main(args: Array[String]): Unit = {
    val tw = getTwitterServiceInstance
  }

  def getTwitterServiceInstance(): TwitterService = {
    new TwitterService(TwitterService.getDefaultConfig)
  }
}