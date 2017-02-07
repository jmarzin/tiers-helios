package test.scala.com.jmarzin.tiers

import org.scalatest.{BeforeAndAfter, FunSpec, ShouldMatchers}

import scala.io.BufferedSource

/**
  * Created by jmarzin-cp on 30/12/2016.
  */
class Bordereau$Test extends FunSpec with ShouldMatchers with BeforeAndAfter {
  val source: BufferedSource = scala.io.Source.fromFile("D:\\Bureau\\jmarzin-cp\\Mes Documents\\tiers\\bordereau.txt")
  val lines: String = try source.mkString finally source.close()
  lines should be("")
}
