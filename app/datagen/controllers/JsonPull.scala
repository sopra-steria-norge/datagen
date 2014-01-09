package datagen.controllers

import java.io.StringWriter
import scala.util.Random
import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import datagen.generator.MeasurementSource
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.__
import play.api.mvc.Action
import play.api.mvc.Controller
import datagen.generator.Generator
import datagen.generator.DataPoint
import play.api.libs.json.Json
import play.api.http.Writeable
import play.api.mvc.SimpleResult
import play.api.mvc.ResponseHeader
import play.api.libs.iteratee.Enumerator
import java.io.ByteArrayInputStream
import java.io.InputStream

object JsonPull extends Controller{
implicit val dataPointWrites = Json.writes[DataPoint]
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
  
  def pullChunkChunk = Action {
    println("current time " + currentTs)
    val ts = currentTs.toString()
    
    currentTs = currentTs.plusMinutes(measurementFrequencyMin)
    import scala.concurrent.ExecutionContext.Implicits.global
    val enu = Enumerator.outputStream{os=>
      var sb = new StringBuilder
      var j = 0
      os.write('[')
      sources.foreach(source => {
      	source.addTo(currentTs, random, measurementFrequencyMin)
      	j = j+1
      	if(j > 1) sb.append(',')
        source.getMeasure(ts, sb)
        if(j % 1000 == 0){
          os.write(sb.toString().getBytes())
          sb = new StringBuilder
        }
      })
      os.write(sb.toString().getBytes())
      os.write(']')
      os.close
    }
    Ok.chunked(enu >>> Enumerator.eof).withHeaders(
      "Content-Type"->"application/json"
    )  
  }
 
  def pullChunk = Action {
    println("current time " + currentTs)
    val ts = currentTs.toString()
    val writer = new StringWriter
    var i = 0
    writer.append("[")
    sources.foreach(source => {
      source.addTo(currentTs, random, measurementFrequencyMin)
      i = i+1
    	if(i > 1) writer.append(",")
      source.getMeasure(ts, writer)
    })
    
    writer.append("]")
    val data = writer.toString()
    writer.close()
    
    //println("sending data" + data)
    currentTs = currentTs.plusMinutes(measurementFrequencyMin)
    Ok(data).withHeaders(
        "Content-Type" -> "application/json")
  }
}