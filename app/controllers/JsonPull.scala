package controllers

import java.io.StringWriter
import scala.util.Random
import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import generator.Generator
import generator.MeasurementSource
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.__
import play.api.mvc.Action
import play.api.mvc.Controller
import generator.DataPoint
object JsonPull extends Controller{

  val conf = ConfigFactory.load("application.conf")
  val random = new Random()
	
  var measurementFrequencyMin = 15
  var sources = Seq[MeasurementSource]()
  var currentTs = DateTime.now()
  
	implicit val rds = (
    (__ \ 'measurementFrequencyMin).read[Int] and
    (__ \ 'startDate).read[String] and
    (__ \ 'councilFilter).read[String]
  ) tupled
  
	def init = Action(parse.json) { (request =>
    request.body.validate[(Int, String, String)].map{ 
        case (measurementFrequencyMinArg, dateString, councilFilterArg) => {
          measurementFrequencyMin = measurementFrequencyMinArg
        	currentTs = DateTime.parse(dateString)
        	sources = Generator.createSources(conf, councilFilterArg)
        	random.setSeed(0)
        	println("pull init complete")
          Ok("init complete with "+sources.size+" sources")
        }
      }.recoverTotal{(
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))) 
    })
	}

  def pullChunk = Action {
    println("current time " + currentTs)
    val ts = currentTs.toString()
    val writer = new StringWriter
    var i = 0
    val measures = sources.map(source => {
      source.addTo(currentTs, random, measurementFrequencyMin)
      i = i+1
    	if(i %10000 == 0) println(i)
      source.getMeasure(ts, writer)      
    })
    writer.append("]")
    println("data")
    val data = writer.toString()
    writer.close()
    
    println("sending data")
    currentTs = currentTs.plusMinutes(measurementFrequencyMin)
    Ok(data)
  }
}