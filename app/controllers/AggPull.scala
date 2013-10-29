package controllers

import scala.util.Random

import org.joda.time.DateTime

import com.typesafe.config.ConfigFactory

import generator.Generator
import generator.JacksonWrapper
import generator.MeasurementSource
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.__
import play.api.mvc.Action
import play.api.mvc.Controller

object AggPull extends Controller{
	
  val conf = ConfigFactory.load("application.conf")
  val random = new Random()
	
	var sources = Seq[MeasurementSource]()
	
	var currentTs = DateTime.now()
  
  implicit val rds = (
    (__ \ 'startDate).read[String] and
    (__ \ 'councilFilter).read[String]
  ) tupled
  
  def init = Action(parse.json) { (request =>
    request.body.validate[(String, String)].map{ 
        case (dateString, councilFilterArg) => {
        	currentTs = DateTime.parse(dateString)
        	sources = Generator.createSources(conf, councilFilterArg)
        	random.setSeed(0)
        	println("agg pull init")
          Ok("init complete with "+sources.size+" sources")
        }
      }.recoverTotal{(
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))) 
    })
	}
	
	def pullDay = Action {
    println("current time " + currentTs)
    val measures = sources.map(source => {
      source.addToDay(currentTs, random)
      source.getMeasure(currentTs)
    })
    currentTs = currentTs.plusDays(1)
    Ok(JacksonWrapper.serialize(measures))
  }

  def pullMonth = Action {
    println("current time " + currentTs)
    val measures = sources.map(source => {
      source.addToMonth(currentTs, random)
      source.getMeasure(currentTs)
    })
    currentTs = currentTs.plusMonths(1)
    Ok(JacksonWrapper.serialize(measures))
  }
}