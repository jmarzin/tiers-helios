package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
class Titre {
  var dernierTitreDeLaColloc = false
  var colloc : Colloc = new Colloc
  var code = ""
  var rangDansLaPage = 0
  def Titre(): Titre = {
    return this
  }
}
