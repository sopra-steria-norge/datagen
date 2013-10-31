package generator

import org.joda.time.DateTime
import scala.util.Random
import java.io.StringWriter

abstract class MeasurementSource(key:String, val council:String) {
  val randomness = 0.3
  var aggKwh = 0.0
  var kw = 0.0
  def getMeasure(ts:DateTime) =
    DataPoint(key, council, ts.toString(), aggKwh, kw)
  def getMeasure(ts:String, writer:StringWriter) = {
    DataPoint.writeToJson(writer, key, council, ts, aggKwh, kw)
  }
  protected def addTo(annualUsageRate:Double, factor:Double, random:Random, measurementFrequency:Int) = {
    val usage = factor * annualUsageRate /365.0 /(24*60/measurementFrequency)
    val r = randomness * random.nextGaussian() * usage
    //println("factor " + factor+"  r "+r+"  aggKw" + aggKw)
    kw = (usage +r) * (60/measurementFrequency)
    aggKwh += usage +r
  }
  protected def addToDay(annualUsageRate:Double, factor:Double, random:Random) = {
    val usage = factor * annualUsageRate / 365.0
    val r = randomness/10 * random.nextGaussian() * usage
    //println("factor " + factor+"  r "+r+"  aggKw" + aggKw)
    kw = (usage +r) / 24
    aggKwh += usage +r
  }
  protected def addToMonth(annualUsageRate:Double, factor:Double, random:Random, ts:DateTime) = {
    val usage = factor * annualUsageRate / 12
    val r = randomness/10 * random.nextGaussian() * usage
    //println("factor " + factor+"  r "+r+"  aggKw" + aggKw)
    kw = (usage +r) *12 /365 /24
    aggKwh += usage +r
  }
  def addTo(ts: DateTime, random:Random, measurementFrequency:Int) : Unit
  def addToDay(ts: DateTime, random:Random) = 
    addTo(ts, random, 24*60)
  def addToMonth(ts: DateTime, random:Random) : Unit
}

object MeasurementSource{
  def apply(s:String, council:String, id:Int) = s match {
  	case "smallBusiness" => SmallBusiness(council, id)
  	case "largeBusiness" => LargeBusiness(council, id)
    case "workingHousehold" => WorkingHousehold(council, id)
    case "retiredHousehold" => RetiredHousehold(council, id)
    case _ => throw new Exception("Unknown measurementsource "+s)
  }

  def sources = List("smallBusiness", "largeBusiness")
}

case class LargeBusiness(override val council:String, id:Int) extends Business(council, id, "LB"){
	def annualUsage = 1000000
}
case class SmallBusiness(override val council:String, id:Int) extends Business(council, id, "SB"){
	def annualUsage = 50000
}
abstract class Business(override val council:String, id:Int, typ:String) extends MeasurementSource(council+typ+id, council){
  def daily(ts:DateTime) = {
    val relativeUsage = ts.getHourOfDay match {
      case _ if Range(7,15).contains(ts.hourOfDay().get()) => 10000
      case _ => 1
    }
    relativeUsage / (8*10000+16)
  }
  def monthly(ts:DateTime) = {
    val relativeUsage = ts.getMonthOfYear() match {
      case 7 => 0
      case _ => 1
    }
    relativeUsage / 11.0 * 12
  }
  def annualUsage :Int
   // low or high usage business
  val generalType = (15 + (id % 7) - 3) / (15.0)
  def addTo(ts: DateTime, random:Random, measurementFrequency:Int){
    addTo(annualUsage, generalType*daily(ts)*monthly(ts), random, measurementFrequency)
  }
  def addToMonth(ts: DateTime, random:Random){
    addToMonth(annualUsage, generalType*monthly(ts), random, ts)
  }
}
case class WorkingHousehold(override val council:String, id:Int) extends Household(council, id, "WH")
{
  def daily(ts:DateTime) = {
    val relativeUsage = ts.getHourOfDay match {
      case _ if Range(0,5).contains(ts.hourOfDay().get()) => 10
      case 5 => 20
      case 6 => 60
      case 7 => 80
      case 8 => 90
      case _ if Range(9,14).contains(ts.hourOfDay().get()) => 20
      case 14 => 30
      case 15 => 40
      case 16 => 300
      case 17 => 350
      case 18 => 300
      case 19 => 200
      case 20 => 200
      case 21 => 150
      case 22 => 100
      case 23 => 60
    }
    relativeUsage / 2130.0 * 24
  }
  def annualUsage = 22000
}

case class RetiredHousehold(override val council:String, id:Int) extends Household(council, id, "RH")
{
  def daily(ts:DateTime) = {
    val relativeUsage = ts.getHourOfDay match {
      case _ if Range(0,5).contains(ts.hourOfDay().get()) => 10
      case 5 => 20
      case 6 => 20
      case 7 => 120
      case 8 => 150
      case 9 => 150
      case 10 => 100
      case 11 => 140
      case 12 => 140
      case 13 => 90
      case 14 => 90
      case 15 => 100
      case 16 => 170
      case 17 => 160
      case 18 => 150
      case 19 => 130
      case 20 => 100
      case 21 => 70
      case 22 => 40
      case 23 => 40
    }
    relativeUsage / 2030.0 * 24
  }
  def annualUsage = 8000
}
abstract class Household(override val council:String, id:Int, typ:String) extends MeasurementSource(council+typ+id, council){

  def daily(ts:DateTime) : Double
  
  def monthly(ts:DateTime) = {
    val relativeUsage = ts.getMonthOfYear() match {
      case 1 => 100
      case 2 => 100
      case 3 => 90
      case 4 => 70
      case 5 => 40
      case 6 => 20
      case 7 => 10
      case 8 => 10
      case 9 => 30
      case 10 => 70
      case 11 => 80
      case 12 => 95
    }
    relativeUsage / 715.0 * 12
  }
  
  def annualUsage : Int
  
  // low or high usage household?
  val generalType = (15 + (id % 7) - 3) / (15.0)  
  
  def addTo(ts: DateTime, random:Random, measurementFrequency:Int){
    addTo(annualUsage, generalType*daily(ts)*monthly(ts), random, measurementFrequency)
  }
  def addToMonth(ts: DateTime, random:Random){
    addToMonth(annualUsage, generalType*monthly(ts), random, ts)
  }
}