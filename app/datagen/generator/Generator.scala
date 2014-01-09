package datagen.generator
import com.typesafe.config.Config
import scala.collection.JavaConverters._

object Generator {
  
  def createSources(conf: Config, filterString:String): Seq[MeasurementSource] = {
    val councils = conf.getConfigList("councils").asScala.toList
    val filteredCouncils = if("".equals(filterString)){
      println("not filtering sources")
      councils
    }else{
    	val matches = filterString.split(",").toSet
    	println("filtering sources")
    	councils.filter(x=> matches.contains(x.getString("id")))
    }
    val sources = for{council <- filteredCouncils} yield  {
  		fetchSources(council.getString("id"), council.getInt("size"))
    	}
    sources.flatten
  }

  def fetchSources(council:String, count:Int) =
    count match { 
    	case x if x > 100000 =>
    	  List(
    	    fetchSource(council, "largeBusiness", count, 0.0001),
    	    fetchSource(council, "smallBusiness", count, 0.001),
	        fetchSource(council, "workingHousehold", count, 0.5),
	        fetchSource(council, "retiredHousehold", count, 0.5)
	        ).flatten
    	case x if x > 10000 =>
    	  List(
    	      fetchSource(council, "smallBusiness", count, 0.001),
	        fetchSource(council, "workingHousehold", count, 0.4),
	        fetchSource(council, "retiredHousehold", count, 0.6)
	        ).flatten
    	case _ =>
    	  List(
	        fetchSource(council, "workingHousehold", count, 0.2),
	        fetchSource(council, "retiredHousehold", count, 0.8)
	        ).flatten
  	}
    
    
  def fetchSource(council:String, name:String, size:Int, frequency:Double): Seq[MeasurementSource] =
    for(i <- 1 to (frequency * size).round.toInt)
    yield MeasurementSource(name, council, i)
}