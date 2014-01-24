package datagen.writer

import com.mongodb.casbah.MongoClient
import com.mongodb.WriteConcern
import java.util.ArrayList
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import datagen.generator.MeasurementSource
import com.mongodb.casbah.MongoClientURI

class MongoDBWriter(uri:String, dbName:String, collectionName:String, batchSize: Integer) extends BatchedWriter{
	//val uri = MongoClientURI("mongodb://localhost:27017,localhost:27018,localhost:27019/")
  val MCuri = MongoClientURI(uri)
	val client = MongoClient(MCuri)
	println("Created new MongoClient")
	client.writeConcern = WriteConcern.MAJORITY
	val db = client.getDB(dbName)
	val collection = db.getCollection(collectionName)
	collection.ensureIndex(MongoDBObject("ts" -> 1))
	var objectList = new ArrayList[DBObject](batchSize)	
	
	def write(key:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) = {
	  val obj = MongoDBObject(
	      "_id" -> key.+(timeStamp),
	      "ts" -> timeStamp,
	      "cumkwh" -> cumkwh,
	      "kw" -> kw)
	  objectList.add(obj)
	}
	
  def sendBatch() = {
    val result = collection.insert(objectList, WriteConcern.MAJORITY)
    val error = result.getLastError()
    error.throwOnError()
    objectList = new ArrayList[DBObject](batchSize) 
  }
  
  def close = client.close
}

