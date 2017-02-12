package main.scala.com.jmarzin.tiers

import org.apache.commons.text.LevenshteinDistance

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jmarzin-cp on 30/12/2016.
  */
case class Item(rowId:Integer,texte:String)
case class Triplet(rowId1: Integer, rowId2: Integer, dist:Integer) extends Ordered[Triplet] {
  def compare(that: Triplet): Int = {
    if (this.rowId1 < that.rowId2) -1
    else if (this.rowId1 > that.rowId2) 1
    else 0
  }
}

case class Vecteur(vect:Vector[Item]) {
  def length : Int = vect.size
  def calculDistance : Vector[(Integer,Integer,Integer)] = {

    val x = new LevenshteinDistance()
    for(i<- 0 until vect.size - 1){
      if (Thread.interrupted) return Vector()
      Future {
        var vect2 = Vector[(Integer, Integer, Integer)]()
        for(j<- i + 1 until vect.size) {
          vect2 = vect2 :+ (vect(i).rowId, vect(j).rowId, x.apply(vect(i).texte,vect(j).texte).asInstanceOf[Integer])
        }
        AppTiers.paralleleDistance.ajoute(vect2)
      }
    }
    while(AppTiers.paralleleDistance.nbDebiteurDistances != vect.size - 1)
      Thread.sleep(10)
    AppTiers.paralleleDistance.tableau //sortWith((left, right) => left._1 < right._1 && left._2 < right._2).toVector
  }
}
