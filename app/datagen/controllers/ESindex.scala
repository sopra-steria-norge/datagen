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
import play.api.libs.iteratee.Concurrent
import org.elasticsearch.node.NodeBuilder
import org.elasticsearch.node.Node
import org.elasticsearch.client.Requests.indexRequest
import java.util.Locale
import play.libs.Akka
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

object ESindex extends Controller{
implicit val dataPointWrites = Json.writes[DataPoint]
  val conf = ConfigFactory.load("application.conf")
  val random = new Random()
	
  var measurementFrequencyMin = 15
  var sources = Seq[MeasurementSource]()
  var currentTs = DateTime.now()
  var subSets = IndexedSeq[Seq[MeasurementSource]]()
  
	implicit val rds = (
    (__ \ 'measurementFrequencyMin).read[Int] and
    (__ \ 'startDate).read[String] and
    (__ \ 'councilFilter).read[String] and
    (__ \ 'clusterName).read[String] and
    (__ \ 'indexName).read[String] and
    (__ \ 'parallel).read[Int]
  ) tupled
  
  var ESjava = IndexedSeq[ESjava]()
  
	def init = Action(parse.json) { (request =>
    request.body.validate[(Int, String, String, String, String, Int)].map{ 
        case (measurementFrequencyMinArg, dateString, councilFilterArg, clusterName, indexName, parallelArg) => {
          println("init start")
          ESjava = for(i <- 1 to parallelArg) yield {new ESjava()}
          ESjava.map(_.init(clusterName, indexName))
          measurementFrequencyMin = measurementFrequencyMinArg
        	currentTs = DateTime.parse(dateString)
        	sources = Generator.createSources(conf, councilFilterArg)
        	random.setSeed(0)
        	val perSource = sources.size / parallelArg + 1
        	subSets = for(i <- 0 to (parallelArg-1)) yield {
        	  val from = i*perSource
        	  val to  = Math.min(perSource*(i+1)-1,sources.size-1)
        	  println("subset "+i+": "+from+":"+to)
        	  sources.slice(from, to)        		
        	}
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
    
    currentTs = currentTs.plusMinutes(measurementFrequencyMin)
    
    for(i <- 0 to subSets.length-1){
    	Akka.system.scheduler.scheduleOnce(1 seconds) {sender(subSets(i), ESjava(i), ts)	}
    }
    
    Ok("all good")
  }
  
  private def sender(sourceList:Seq[MeasurementSource], es:ESjava, ts:String):Unit = {
    var j = 0
    sourceList.foreach(source => {
      var sb = new java.lang.StringBuilder
    	source.addTo(currentTs, random, measurementFrequencyMin)
     	j = j+1     	
      source.getMeasure(ts, sb)
      es.addToBatch(sb);
    	if(j % 10000 == 0){
    	  println("progress "+j)
    	  es.sendBatch()
    	}
    })
    if(j%10000 != 0) es.sendBatch()
  }
  
//    def pullChunkOld = Action {
//    println("current time " + currentTs)
//    val ts = currentTs.toString()
//    
//    currentTs = currentTs.plusMinutes(measurementFrequencyMin)
//    
//    var j = 0
//    sources.foreach(source => {
//      var sb = new java.lang.StringBuilder
//    	source.addTo(currentTs, random, measurementFrequencyMin)
//     	j = j+1     	
//      source.getMeasure(ts, sb)
//      ESjava.addToBatch(sb);
//    	if(j % 10000 == 0){
//    	  println("progress "+j)
//    	  ESjava.sendBatch()
//    	}
//    })
//    if(j%10000 != 0) ESjava.sendBatch()
//    Ok("all good")
//  }
}