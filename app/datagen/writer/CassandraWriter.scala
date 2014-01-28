package datagen.writer

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.ConsistencyLevel
import org.joda.time.DateTime
import com.datastax.driver.core.exceptions.WriteTimeoutException

class CassandraWriter(resetDB:Boolean, keyspaceName:String) extends BatchedWriter{
  val cluster = new Cluster.Builder().addContactPoints("localhost").build()
  
  val session = cluster.connect()
  
  val metadata = cluster.getMetadata()
  System.out.println(String.format("Connected to cluster '%s' on %s.", metadata.getClusterName(), metadata.getAllHosts()))
  if(resetDB){
	  val x = session.execute("DROP KEYSPACE IF EXISTS " + keyspaceName)
	  println("drop if exists got: "+x)
	  session.execute("CREATE KEYSPACE " + keyspaceName + " WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '3' }")
	  session.execute("use "+keyspaceName)
	  System.out.println("Keyspace " + keyspaceName + " created")
	  session.execute("CREATE TABLE stasjon_dag (\n" +
	                "\ttidsstempel timestamp,\n" +
	                "\tdag text,\n" +
	                "\tstasjon text,\n" +
	                "\tkommune text,\n" +
	                "\tkw double,\n" +
	                "\tcumkwh double,\n" +
	                "\tPRIMARY KEY((stasjon, dag), tidsstempel));")
	  println("create table done")
  }
  
  val insertStation = session.prepare("INSERT INTO "+keyspaceName+".stasjon_dag (tidsstempel, dag, stasjon, kommune, kw, cumkwh) VALUES (?, ?, ?, ?, ?, ?)");
  var batch = new BatchStatement()
  batch.setConsistencyLevel(ConsistencyLevel.ONE)
  println("batch statement ready")

	def write(key:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) = {
	  batch.add(insertStation.bind(new DateTime(timeStamp).toDate(), timeStamp.split("T")(0), key, council, kw.asInstanceOf[java.lang.Double], cumkwh.asInstanceOf[java.lang.Double]))
	  //println("added to batch")
	}
	
  def sendBatch() = {
    try{
    	session.execute(batch)
    }catch{
      case ex: WriteTimeoutException =>{
        //retry once
        session.execute(batch)
      } 
    }
    
     
     //println("sent batch")
     batch = new BatchStatement()
     batch.setConsistencyLevel(ConsistencyLevel.ONE)
  }
  
  def close = {
    
  }
}