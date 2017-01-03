package main.scala.com.jmarzin.tiers

import javax.swing.JOptionPane._

import org.openqa.selenium.firefox.FirefoxDriver

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object Helios extends HeliosFirefox {

  def parcours : Unit = {
    var titreEnCours = new Titre
    titreEnCours.colloc = new Colloc
    titreEnCours.dernierTitreDeLaColloc = true
    titreEnCours.colloc.derniereColloc = true
    do {
      if (titreEnCours.dernierTitreDeLaColloc) {
        SessionEnCours.memoriseColloc(titreEnCours.colloc)
        titreEnCours = collocSuivante(titreEnCours)
        if (SessionEnCours.collocDejaTraitee(titreEnCours.colloc)) {
          titreEnCours.colloc.pasDeTitre = true
        } else {
          SessionEnCours.memoriseTitre(titreEnCours)
          titreEnCours = premierTitreColloc(titreEnCours)
        }
        if (titreEnCours.colloc.pasDeTitre) {
          titreEnCours.dernierTitreDeLaColloc = true
        } else {
          titreEnCours.dernierTitreDeLaColloc = false
        }
      }
      if (!titreEnCours.colloc.pasDeTitre) {
        titreEnCours = setDernierTitre(titreEnCours)
        if (!SessionEnCours.titreDejaTraite(titreEnCours)) {
          titreEnCours = litTitre(titreEnCours)
          SessionEnCours.memoriseTitre(titreEnCours)
          Base.sauve(titreEnCours)
          Fichier.sauve(titreEnCours)
          AppTiers.nbTitresTraites += 1
        }
        if (!titreEnCours.dernierTitreDeLaColloc) {
          titreEnCours = titreSuivantColloc(titreEnCours)
        }
      }
    } while ((!titreEnCours.dernierTitreDeLaColloc || !titreEnCours.colloc.derniereColloc) && !AppTiers.stop)
    if (titreEnCours.dernierTitreDeLaColloc && titreEnCours.colloc.derniereColloc) {
      AppTiers.finNormale = true
      SessionEnCours.memoriseColloc(titreEnCours.colloc)
      Base.sauveSession
    }
    close
  }
}
