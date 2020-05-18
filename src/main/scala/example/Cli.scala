package example

import scalaj.http._

import util.chaining._
import Model._
import Model.OptionPickler._
import com.gu.spy._
import HttpOptions._
import System.getenv

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.util.Try

object Cli extends App with LazyLogging {
  val (organization, awsResourcesToCheck) = (args.toList match {
    case Nil => throw new RuntimeException("Please provide organization as first argument")
    case org :: Nil => org -> List("Lambda", "LoadBalancer")
    case org :: resources => org -> resources
  }) tap { v => logger.info(s"Checking $v for missing alarms...") }

  val searchQueryParam = """q=AWSTemplateFormatVersion+org:guardian+extension:yaml+extension:json"""

  val maybeGithubPersonalAccessToken = Option(getenv("token"))
  if (maybeGithubPersonalAccessToken.isDefined) logger.info("Personal access token detected. Increasing request rate.")

  object HttpWithLongTimeout
    extends BaseHttp(options = Seq(connTimeout(5000), readTimeout(5 * 60 * 1000)))

  def maybeAddAuthorization(req: HttpRequest): HttpRequest = {
    maybeGithubPersonalAccessToken
      .map(token => req.header("Authorization", s"token $token") )
      .getOrElse(req)
  }

  def getPage(page: Int): List[Item] = {
    HttpWithLongTimeout(s"""https://api.github.com/search/code?page=$page&per_page=100&$searchQueryParam""")
      .header("Content-Type", "application/json")
      .pipe { maybeAddAuthorization }
      .asString
      .body
      .pipe(read[Search](_))
      .items
  }

  def getFile(url: String): String = {
    Thread.sleep(maybeGithubPersonalAccessToken.fold(10000)(_ => 2000)) // prevent GitHub rate limiting
    HttpWithLongTimeout(url)
      .pipe { maybeAddAuthorization }
      .asString
      .body
  }

  def getAllPages(): List[Item] = {
    @tailrec def loop(page: Int, acc: List[Item]): List[Item] = {
      val next = getPage(page)
      if (next.nonEmpty) loop(page+1, acc ::: next)
      else acc
    }
    loop(page = 1, acc = Nil)
  }

  getAllPages()
    .tap      { v => logger.info(s"Checking ${v.length} CloudFormation files...") }
    .iterator
    .map      { _.html_url }
    .map      { case s"https://github.com/guardian/$repo/blob/$sha/$path" => s"https://raw.githubusercontent.com/guardian/$repo/master/$path" }
    .map      { url => url -> getFile(url) }
    .collect  { case (url, cloudformation) if !cloudformation.contains("Alarm") && awsResourcesToCheck.exists(cloudformation.contains) => url }
    .map      { case s"https://raw.githubusercontent.com/guardian/$repo/master/$path" => s"https://github.com/guardian/$repo/blob/master/$path"}
    .foreach  { logger.info(_) }
}

