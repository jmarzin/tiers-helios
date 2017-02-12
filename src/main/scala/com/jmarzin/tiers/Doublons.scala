package main.scala.com.jmarzin.tiers

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jmarzin-cp on 30/12/2016.
  */
object Doublons {

  def paquetParCode(vect: Vector[Item], dist: Vector[(Integer,Integer,Integer)]): Unit = {
    for(v <- vect) {
      if (Thread.interrupted) return
      Future {
        val resfiltre = dist.filter(p => p._1 == v.rowId || p._2 == v.rowId)
        val restrie = resfiltre.sortWith(_._3 < _._3)
        val restronque = restrie.slice(0, 10).map(p => {
          if(p._1 < p._2) {
            (p._1, p._2, p._3)
          }else {
            (p._2, p._1, p._3)}})
        AppTiers.parallelePaquet.ajoute(restronque)
      }
    }
    while(AppTiers.parallelePaquet.nbDebiteurDoublons != vect.size) Thread.sleep(10)
  }

  def cherche(): Unit = {
    if (Thread.interrupted()) return
    val vecteur = Base.litTiers
    if (Thread.interrupted()) return
    val res = vecteur.calculDistance
    if (Thread.interrupted()) return
    paquetParCode(vecteur.vect, res)
    if (!Thread.interrupted) AppTiers.finNormale = true
  }
}
