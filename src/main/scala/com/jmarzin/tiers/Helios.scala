package main.scala.com.jmarzin.tiers

import javax.swing.JOptionPane._

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object Helios {

  var nbTitresTraites = 0

  def init : Unit = {
    showMessageDialog(null, "Connectez-vous sur Hélios\net ne touchez plus à rien !");
  }

  def collocSuivante(titre : Titre) :Titre = {
    return titre
  }

  def premierTitreColloc(titre : Titre) :Titre = {
    return titre
  }

  def titreSuivantColloc(titre : Titre) : Titre = {
    return titre
  }

  def litTitre(titre : Titre) : Titre = {
    return titre
  }

  def parcours : Unit = {
    var titreEnCours = new Titre
    titreEnCours.colloc = new Colloc
    titreEnCours.dernierTitreDeLaColloc = true
    titreEnCours.colloc.derniereColloc = false
    do {
      if (titreEnCours.dernierTitreDeLaColloc) {
        titreEnCours = collocSuivante(titreEnCours)
        if (SessionEnCours.collocDejaTraitee(titreEnCours.colloc)) {
          titreEnCours.dernierTitreDeLaColloc = true
        } else {
          titreEnCours = premierTitreColloc(titreEnCours)
          SessionEnCours.memoriseColloc(titreEnCours.colloc)
        }
      } else {
        titreEnCours = titreSuivantColloc(titreEnCours)
        if (SessionEnCours.titreDejaTraite(titreEnCours)) {
          titreEnCours.dernierTitreDeLaColloc = true
        } else {
          titreEnCours = litTitre(titreEnCours)
          SessionEnCours.memoriseTitre(titreEnCours)
          Base.sauve(titreEnCours)
        }
      }
    } while (!titreEnCours.dernierTitreDeLaColloc || !titreEnCours.colloc.derniereColloc)
    //TODO compléter l'algorithme de parcours

  }
}
