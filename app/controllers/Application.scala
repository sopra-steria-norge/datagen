package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import generator.DataPoint
import scala.util.Random
import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import generator.Generator
import play.api.libs.json.JsArray
import play.api.libs.ws.WS

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  
}