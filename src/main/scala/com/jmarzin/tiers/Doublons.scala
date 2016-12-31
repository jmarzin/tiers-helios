package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 30/12/2016.
  */
object Doublons {

  def paquetParCode(vect: Vector[Item], dist: Vector[(String,String,Integer)]): Vector[(String,Vector[(String,Integer)])] = {
    var i = 0
    return for(v <- vect) yield {
      i += 1
      if (i%10 == 0) println(i)
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
    print("Chargement des tiers : ")
    val vecteur = Base.litTiers
    println(vecteur.vect.length)
    val res = vecteur.calculDistance
    val resFinal = paquetParCode(vecteur.vect, res)
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
    AppTiers.finNormale = true
  }
}
