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
import datagen.mongodb.MongoDBWriter
import datagen.mongodb.BatchedWriter

object MongoPull extends Pull{
  implicit val rds = (
    (__ \ 'measurementFrequencyMin).formatNullable[Int] and
    (__ \ 'batchSize).formatNullable[Int] and
	  (__ \ 'startDate).formatNullable[String] and
    (__ \ 'councilFilter).formatNullable[String] and
    (__ \ 'parallel).formatNullable[Int] and
    (__ \ 'uri).formatNullable[String] and
    (__ \ 'database).formatNullable[String] and
    (__ \ 'collection).formatNullable[String]
  ) tupled
  
  var uri = ""
  var database = ""
  var collection = "" 
  
  def init = Action(parse.json) { (request =>
    request.body.validate[(Option[Int], Option[Int], Option[String], Option[String],Option[Int], Option[String], Option[String], Option[String])].map{ 
          case (measurementFrequencyMinArg, batchSizeArg, dateString, councilFilterArg, parallelArg, uriArg, databaseArg, collectionArg) => {
            println("init start")
            uri = uriArg.getOrElse("mongodb://localhost:27017/?replicaSet=rep")
            database = databaseArg.getOrElse("mydb")
            collection = collectionArg.getOrElse("myCol")
            initArgs(parallelArg, batchSizeArg, measurementFrequencyMinArg, dateString, councilFilterArg)            
            Ok("init complete with "+sources.size+" sources")
          }
        }.recoverTotal{(
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))) 
    })
	}
  
  def createSources = 
    for(i <- 1 to parallel) yield {new MongoDBWriter(uri, database, collection, batchSize).asInstanceOf[BatchedWriter]}
}
trait Pull extends Controller {
	implicit val dataPointWrites = Json.writes[DataPoint]
  val conf = ConfigFactory.load("application.conf")
  val random = new Random()
	
  var measurementFrequencyMin = 15
  var sources = Seq[MeasurementSource]()
  var currentTs = DateTime.now()
  var subSets = IndexedSeq[Seq[MeasurementSource]]()
  var parallel = 1
  var batchSize = 1
  var batchedWriter:IndexedSeq[BatchedWriter] = IndexedSeq[BatchedWriter]()
  
	protected def initArgs(parallelArg:Option[Int],batchSizeArgs:Option[Int],measurementFrequencyMinArg: Option[Int], dateString: Option[String], councilFilterArg: Option[String]) ={
  	parallel = parallelArg.getOrElse(1)
  	batchSize = batchSizeArgs.getOrElse(10000)
  	measurementFrequencyMin = measurementFrequencyMinArg.getOrElse(15)
  	if(dateString.isDefined)
  		currentTs = DateTime.parse(dateString.getOrElse("1900-01-01"))
	  sources = Generator.createSources(conf, councilFilterArg.getOrElse("1151"))
	  random.setSeed(0)
	  val perSource = sources.size / parallel + 1
	  subSets = for(i <- 0 to (parallel-1)) yield {
	    val from = i*perSource
	    val to  = Math.min(perSource*(i+1)-1,sources.size-1)
	    println("subset "+i+": "+from+":"+to)
	    sources.slice(from, to)        		
	  }
	}
	
	protected def createSources : IndexedSeq[BatchedWriter]

  def pullChunk = Action {
    val time = "current time " + currentTs
    println(time)
    pullStarted = DateTime.now()
    batchedWriter = createSources
    val ts = currentTs.toString()
    
    currentTs = currentTs.plusMinutes(measurementFrequencyMin)
    
    for(i <- 0 to subSets.length-1){
    	Akka.system.scheduler.scheduleOnce(0 seconds) {sender(subSets(i), batchedWriter(i), ts)	}
    }
    
    Ok("all good, "+time)
  }
  
  private def sender(sourceList:Seq[MeasurementSource], es:BatchedWriter, ts:String):Unit = {
    var j = 0
    sourceList.foreach(source => {
      var sb = new java.lang.StringBuilder
    	source.addTo(currentTs, random, measurementFrequencyMin)
     	j = j+1     	
      source.getMearsure(ts, es)
    	if(j % batchSize == 0){
    	  println("progress "+j)
    	  es.sendBatch()
    	}
    })
    if(j%batchSize != 0) es.sendBatch()
    es.close
    done
  }
  
  var doneCounter = 0
  var pullStarted = DateTime.now()
  private def done = {
  		synchronized({
  		  doneCounter += 1
  		  val elapsed = DateTime.now().getMillis() - pullStarted.getMillis()
  		  if(doneCounter % parallel == 0) {
  		    println("done "+ elapsed)
  		  }
  		})
  }
}