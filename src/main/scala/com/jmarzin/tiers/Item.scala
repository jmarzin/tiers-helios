package main.scala.com.jmarzin.tiers

import org.apache.commons.text.LevenshteinDistance

/**
  * Created by jmarzin-cp on 30/12/2016.
  */
case class Item(code:String,texte:String)
case class Triplet(code1: String, code2: String, dist:Integer) extends Ordered[Triplet] {
  def compare(that: Triplet): Int = {
    if (this.code1.length < that.code1.length) {
      return -1
    } else if (this.code1.length > that.code1.length) {
      return 1
    } else if (this.code1 < that.code1) {
      return -1
    } else if (this.code1 > that.code1) {
      return 1
    } else if (this.dist < that.dist) {
      return -1
    } else if (this.dist > that.dist) {
      return 1
    } else if (this.code2.length < that.code2.length) {
      return -1
    } else if (this.code2.length > that.code2.length) {
      return 1
    } else if (this.code2 < that.code2) {
      return -1
    } else if (this.code2 > that.code2) {
      return 1
    } else {
      return 0
    }
  }
}
case class Vecteur(vect:Vector[Item]) {
  def length : Int = return vect.size
  def calculDistance : Vector[(String,String,Integer)] = {
    var compteDesI = -1
    AppTiers.nbDebiteurDistances = 0
    val x = new LevenshteinDistance()
    return (for {
      i <- 0 to vect.size - 2
      j <- i + 1 to vect.size - 1
    } yield {
      if (i != compteDesI) {
        compteDesI = i
        AppTiers.nbDebiteurDistances += 1
      }
      if (Thread.interrupted) return Vector(("","",0))
      (vect(i).code, vect(j).code, x.apply(vect(i).texte,vect(j).texte))
    }
      ).toVector
  }
}
