package controllers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Random
import scala.util.Success
import org.joda.time.DateTime
import com.typesafe.config.ConfigFactory
import generator.Generator
import generator.MeasurementSource
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.libs.json.__
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Controller
import play.libs.Akka
import generator.DataPoint

object JsonPushParallel extends Controller {
  
  implicit val dataPointWrites = Json.writes[DataPoint]
  
  val random = new Random()
  val conf = ConfigFactory.load("application.conf")
  
  var intervalCount = 5
  var measurementFrequencyMin = 15
  var startTime = DateTime.parse("2013-01-01")
  var sendDelaySec = 5
  var dataPerCall = 100
  var url = "http://localhost:9000/receivePush"
  var sources = Seq[MeasurementSource]()
  
  implicit val rds = (
    (__ \ 'intervalCount).read[Int] and
    (__ \ 'measurementFrequencyMin).read[Int] and
    (__ \ 'sendDelaySec).read[Int] and
    (__ \ 'startDate).read[String] and
    (__ \ 'url).read[String] and
    (__ \ 'councilFilter).read[String] and
    (__ \ 'parallel).read[Int] and
    (__ \ 'dataPerCall).read[Int]
  ) tupled
  
    def start = Action(parse.json) { (request =>
    request.body.validate[(Int, Int ,Int, String, String, String, Int, Int)].map{ 
        case (intervalCountArg, measurementFrequencyMinArg, sendDelaySecArg, dateString, urlArg, councilFilterArg, parallelArg, dataPerCallArg) => {
          intervalCount = intervalCountArg
          measurementFrequencyMin = measurementFrequencyMinArg
          sendDelaySec = sendDelaySecArg
        	startTime = DateTime.parse(dateString)
        	url = urlArg
        	dataPerCall = dataPerCallArg
        	sources = Generator.createSources(conf, councilFilterArg)
        	random.setSeed(0)
        	val perSource = sources.size / parallelArg + 1
        	val subSets = for(i <- 0 to (parallelArg-1)) yield {
        	  val from = i*perSource
        	  val to  = Math.min(perSource*(i+1)-1,sources.size-1)
        	  println("subset "+i+": "+from+":"+to)
        	  sources.slice(from, to)        		
        	}
          println("scheduling threads")
          subSets.map(sender(_, intervalCount, startTime,0))
          Ok("started with "+sources.size+" sources")
        }
      }.recoverTotal{(
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))) 
    })
  }
  
  private def sender(sourceList:Seq[MeasurementSource], remaining:Int, now:DateTime, delaySec:Int):Unit= remaining match {
    case 0 => println("done")
    case _ =>      
    	Akka.system.scheduler.scheduleOnce(delaySec seconds) {
    	  sendBatch(sourceList, now).onComplete({ 
    	     
    	    case Success(y) => {
    	    	println("scheduled next ")
    	    	val ts = now.plusMinutes(measurementFrequencyMin)
    	    	sender(sourceList, remaining -1, ts, sendDelaySec)
    	    }
    	    case Failure(y) => println("Failure, so stopping next batch: "+y)
    	  })
    	}
  }
  
  private def sendBatch(sourceList:Seq[MeasurementSource], ts: DateTime) = {
    println("sending batch, new time " + ts)
    val measures = sourceList.map(x=> {
    	x.addTo(ts, random, measurementFrequencyMin)
      x.getMeasure(ts)
    })
    
    val subSets = for(i <- 0 to measures.size/dataPerCall) yield {
        	  val from = i*dataPerCall
        	  val to  = Math.min(dataPerCall*(i+1)-1,measures.size-1)
        	  println("subset "+i+": "+from+":"+to)
        	  measures.slice(from, to)
   	}
    
    serialiseFutures(subSets)({datapoints =>
      {
        println("sending chunk ")
        WS.url(url).post(Json.toJson(datapoints)).filter({
          case a if (a.status==200) => true
          case b => {
            println("Failure response: "+b.status)
            false
          }
        })        
      }
    })
  }
  
  //executes a function on a list of items in sequence
  def serialiseFutures[A, B](l: Iterable[A])(fn: A ⇒ Future[B])
  	(implicit ec: ExecutionContext): Future[List[B]] =
  		l.foldLeft(Future(List.empty[B])) {
  			(previousFuture, next) ⇒
  				for {
  					previousResults ← previousFuture
  					next ← fn(next)
  				} yield previousResults :+ next
  		}
}