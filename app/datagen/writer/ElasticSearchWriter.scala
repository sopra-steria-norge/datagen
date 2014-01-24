package datagen.writer

import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.action.ListenableActionFuture
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.client.Client
import org.elasticsearch.node.Node
import org.elasticsearch.client.Requests.indexRequest
import datagen.generator.DataPointJava

class ElasticSearchWriter(clusterName:String, multicast:Boolean, hosts:String, indexName:String, batchSize: Integer) extends BatchedWriter{
	val esJava = new ElasticSearchWriterJava(clusterName, multicast, hosts, indexName)
	
  def write(key:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) = {
	  val sb = new java.lang.StringBuilder()
	  DataPointJava.writeToJson(sb, key, council, timeStamp, cumkwh, kw)
    esJava.addToBatch(sb)
	}
	
  def sendBatch() = {
    esJava.sendBatch()
  }
  
  def close = esJava.close
}

