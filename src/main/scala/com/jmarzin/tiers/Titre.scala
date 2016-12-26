package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
class Titre {
  var dernierTitreDeLaColloc = true
  var colloc : Colloc = new Colloc
  var code : String = ""
  def Titre(): Titre = {
    return this
  }
}
