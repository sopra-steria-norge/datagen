package datagen.writer

trait Writer{
  def write(key:String, council:String, timeStamp:String, cumkwh:Double, kw:Double) : Unit
  def close : Unit
}

trait BatchedWriter extends Writer{
  def sendBatch() : Unit
}