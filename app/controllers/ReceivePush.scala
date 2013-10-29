package controllers

import play.api._
import play.api.mvc._
import generator.DataPoint
import scala.util.Random
import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import generator.Generator
import play.api.libs.json._
import play.api.libs.functional.syntax._

object ReceivePush extends Controller {
  def data = Action { request =>
    println("RECEIVER: Got new batch of data to receivePush:")
    Thread.sleep(100)
    //request.body.asJson.map{x =>println(x)}
    println("RECEIVER: End batch of data")
    Ok("")
  }
}