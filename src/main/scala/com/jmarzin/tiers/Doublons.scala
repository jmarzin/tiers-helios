package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 30/12/2016.
  */
object Doublons {

  def paquetParCode(vect: Vector[Item], dist: Vector[(String,String,Integer)]): Vector[(String,Vector[(String,Integer)])] = {
    AppTiers.nbDebiteurDoublons = 0
    return for(v <- vect) yield {
      AppTiers.nbDebiteurDoublons += 1
      if (Thread.interrupted) return Vector(("",Vector(("",0))))
      val resfiltre = dist.filter(p => p._1 == v.code || p._2 == v.code)
      val restrie = resfiltre.sortWith(_._3 < _._3)
      val restronque = restrie.slice(0,10)
      (v.code, restronque.map(p => {
        if(p._1 == v.code) {
          (p._2, p._3)
        }else {
          (p._1, p._3)
        }}))
    }
  }

  def cherche: Unit = {
    if (Thread.interrupted()) return
    val vecteur = Base.litTiers
    if (Thread.interrupted()) return
    val res = vecteur.calculDistance
    if (Thread.interrupted()) return
    val resFinal = paquetParCode(vecteur.vect, res)
    if (Thread.interrupted()) return
    val resFinalFlat = (for {
      v <- resFinal
      w <- v._2
    } yield {
      if (v._1.length < w._1.length) {
        Triplet(v._1, w._1, w._2)
      } else if (v._1.length == w._1.length && v._1 < w._1) {
        Triplet(v._1, w._1, w._2)
      } else {
        Triplet(w._1, v._1, w._2)
      }
    }).distinct.sorted
    if (Thread.interrupted) return
    Base.sauve(resFinalFlat)
    if (Thread.interrupted) {
      return
    } else {
      AppTiers.finNormale = true
      return
    }
  }
}
