package main.scala.com.jmarzin.tiers

/**
  * Created by jmarzin-cp on 24/12/2016.
  */

object Base {
  def init(repertoire: String) : Unit = {
    SessionEnCours.collocsTraitees = List()
    SessionEnCours.collocEnCours =""
    SessionEnCours.titresTraites = List()
  }
  def vide: Unit = {
    SessionEnCours.raz
  }

  def sauve(titre : Titre) : Unit = {

  }
}
