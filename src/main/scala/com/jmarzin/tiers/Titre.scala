package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
class Titre {
  var dernierTitreDeLaColloc = false
  var colloc : Colloc = new Colloc
  var code = ""
  var resteARecouvrerPrincipal = 0
  var resteARecouvrerFrais = 0
  var dateEmission = ""
  var datePrescription = ""
  var debiteur = new Debiteur
  var rangDansLaPage = 0
  var annee = ""
  var bordereau = ""
//  def Titre(): Titre = {
//    return this
//  }
}
