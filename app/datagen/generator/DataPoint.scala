package datagen.generator

import java.util.Locale
import java.io.OutputStream

case class DataPoint(
    stationId: String,
    council: String,
    timeStamp: String,
    var cumkwh: Double,
    var kw: Double) {
  if(kw < 0) {kw = 0}
  if(cumkwh < 0) {cumkwh = 0}
  
  def writeToJson(writer:java.io.StringWriter) = 
    DataPoint.writeToJson(writer, stationId, council, timeStamp, cumkwh, kw)
}

object DataPoint{
  //simple serializer that stores in a StringWriter
  def writeToJson(writer:java.io.StringWriter,
      stationId:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) :Unit = {
    writer.append("{\"stationId\":\"")
    writer.append(stationId)
    writer.append("\",\"council\":\"")
    writer.append(council)
    writer.append("\",\"timeStamp\":\"")
    writer.append(timeStamp)
    writer.append("\",\"kw\":")
    writer.append("%f".formatLocal(Locale.ENGLISH, kw))
    writer.append(",\"cumkwh\":")
    writer.append("%f".formatLocal(Locale.ENGLISH, cumkwh))
    writer.append("}")
    
  }
  def writeToJson(sb:StringBuilder,
      stationId:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) :Unit = {
    sb.append("{\"stationId\":\"")
    sb.append(stationId)
    sb.append("\",\"council\":\"")
    sb.append(council)
    sb.append("\",\"timeStamp\":\"")
    sb.append(timeStamp)
    sb.append("\",\"kw\":")
    sb.append("%f".formatLocal(Locale.ENGLISH, kw))
    sb.append(",\"cumkwh\":")
    sb.append("%f".formatLocal(Locale.ENGLISH, cumkwh))
    sb.append("}")    
  }

  def writeToJson(sb:OutputStream,
      stationId:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) :Unit = {
    sb.write("{\"stationId\":\"".getBytes())
    sb.write(stationId.getBytes())
    sb.write("\",\"council\":\"".getBytes())
    sb.write(council.getBytes())
    sb.write("\",\"timeStamp\":\"".getBytes())
    sb.write(timeStamp.getBytes())
    sb.write("\",\"kw\":".getBytes())    
    sb.write("%f".formatLocal(Locale.ENGLISH, kw).getBytes())
    sb.write(",\"cumkwh\":".getBytes())
    sb.write("%f".formatLocal(Locale.ENGLISH, cumkwh).getBytes())
    sb.write('}')    
  }
  
}
object DataPointJava{
    def writeToJson(sb:java.lang.StringBuilder,
      stationId:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) :Unit = {
    sb.append("{\"stationId\":\"")
    sb.append(stationId)
    sb.append("\",\"council\":\"")
    sb.append(council)
    sb.append("\",\"timeStamp\":\"")
    sb.append(timeStamp)
    sb.append("\",\"kw\":")
    sb.append("%f".formatLocal(Locale.ENGLISH, kw))
    sb.append(",\"cumkwh\":")
    sb.append("%f".formatLocal(Locale.ENGLISH, cumkwh))
    sb.append("}")    
  }
}