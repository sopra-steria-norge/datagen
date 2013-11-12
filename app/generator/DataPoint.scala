package generator

import java.util.Locale

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
}
