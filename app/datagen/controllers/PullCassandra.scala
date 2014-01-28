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
import datagen.writer.CassandraWriter
import datagen.writer.BatchedWriter

object PullCassandra extends Pull{
  implicit val rds = (
    (__ \ 'measurementFrequencyMin).formatNullable[Int] and
    (__ \ 'batchSize).formatNullable[Int] and
	  (__ \ 'startDate).formatNullable[String] and
    (__ \ 'councilFilter).formatNullable[String] and
    (__ \ 'parallel).formatNullable[Int] and
    (__ \ 'uri).formatNullable[String] and
    (__ \ 'resetDB).formatNullable[Boolean] and
    (__ \ 'keyspace).formatNullable[String]
  ) tupled
  
  var uri = ""
  var resetDB = false
  var keyspace = "" 
  
  def init = Action(parse.json) { (request =>
    request.body.validate[(Option[Int], Option[Int], Option[String], Option[String],Option[Int], Option[String], Option[Boolean], Option[String])].map{ 
          case (measurementFrequencyMinArg, batchSizeArg, dateString, councilFilterArg, parallelArg, uriArg, resetDBArg, keyspaceArg) => {
            println("init start")
            //uri = uriArg.getOrElse("mongodb://localhost:27017/?replicaSet=rep")
            resetDB = resetDBArg.getOrElse(false)
            keyspace = keyspaceArg.getOrElse("POC")
            initArgs(parallelArg, batchSizeArg, measurementFrequencyMinArg, dateString, councilFilterArg)            
          }
        }.recoverTotal{(
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))) 
    })
	}
  
  def createWriters = 
    for(i <- 1 to parallel) yield {new CassandraWriter(resetDB, keyspace).asInstanceOf[BatchedWriter]}
}