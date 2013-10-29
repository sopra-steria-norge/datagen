package generator

case class DataPoint(
    stationId: String,
    council: String,
    timeStamp: String,
    var cumkwh: Double,
    var kwh: Double) {
  if(kwh < 0) {kwh = 0}
  if(cumkwh < 0) {cumkwh = 0}
  
  def writeToJson(writer:java.io.StringWriter) = 
    DataPoint.writeToJson(writer, stationId, council, timeStamp, cumkwh, kwh)
}

object DataPoint{
  //simple serializer that stores in a StringWriter
  def writeToJson(writer:java.io.StringWriter,
      stationId:String, council:String, timeStamp:String, cumkwh:Double, kwh:Double) :Unit = {
    writer.append("{\"stationId\":")
    writer.append(stationId)
    writer.append(",\"council\":\"")
    writer.append(council)
    writer.append(",\"timeStamp\":\"")
    writer.append(timeStamp)
    writer.append("\",\"kwh\":")
    writer.append("%.5f".format(kwh))
    writer.append(",\"cumkwh\":")
    writer.append("%.5f".format(cumkwh))
    writer.append("}")
  }
}
