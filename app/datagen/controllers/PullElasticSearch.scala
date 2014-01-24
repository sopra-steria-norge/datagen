package datagen.controllers

import datagen.writer.BatchedWriter
import datagen.writer.ElasticSearchWriter
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.__
import play.api.mvc.Action

object PullElasticSearch extends Pull{
  implicit val rds = (
    (__ \ 'measurementFrequencyMin).formatNullable[Int] and
    (__ \ 'batchSize).formatNullable[Int] and
	  (__ \ 'startDate).formatNullable[String] and
    (__ \ 'councilFilter).formatNullable[String] and
    (__ \ 'parallel).formatNullable[Int] and
    (__ \ 'clusterName).formatNullable[String] and
    (__ \ 'multicast).formatNullable[Boolean] and
    (__ \ 'hosts).formatNullable[String] and
    (__ \ 'indexName).formatNullable[String]
  ) tupled
  
  var clusterName = ""
  var multicast = true
  var hosts = ""
  var indexName = ""  
  
  def init = Action(parse.json) { (request =>
    request.body.validate[(Option[Int], Option[Int], Option[String], Option[String], Option[Int], 
        Option[String], Option[Boolean], Option[String], Option[String])].map{ 
          case (measurementFrequencyMinArg, batchSizeArg, dateString, councilFilterArg, parallelArg, 
              clusterNameArg, multicastArg, hostsArg, indexNameArg) => {
            println("init start")
            clusterName = clusterNameArg.getOrElse("")
            indexName = indexNameArg.getOrElse("mydb")
            multicast = multicastArg.getOrElse(multicast)
            hosts = hostsArg.getOrElse("")
            initArgs(parallelArg, batchSizeArg, measurementFrequencyMinArg, dateString, councilFilterArg)            
          }
        }.recoverTotal{(
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))) 
    })
	}
  
  def createWriters = 
    for(i <- 1 to parallel) yield {new ElasticSearchWriter(clusterName, multicast, hosts, indexName, batchSize).asInstanceOf[BatchedWriter]}
}