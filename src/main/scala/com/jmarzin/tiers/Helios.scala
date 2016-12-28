package main.scala.com.jmarzin.tiers

import javax.swing.JOptionPane._

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object Helios {

  var listeTest: List[(Int ,List[Int])] = List()
  def enleveCollocTest :Unit = {
    if (!listeTest.isEmpty) {
      listeTest = listeTest.tail
    }
  }
  def enleveTitreTest :Unit = {
    if (listeTest.nonEmpty && listeTest.head._2.nonEmpty) {
        listeTest = (listeTest.head._1,listeTest.head._2.tail) :: listeTest.tail
    }
  }

  var nbTitresTraites = 0
  var nbAppelsCollocSuivante = 0
  var nbAppelsPremierTitreColloc = 0
  var nbAppelsTitreSuivantColloc = 0

  def init : Unit = {
    showMessageDialog(null, "Connectez-vous sur Hélios\net ne touchez plus à rien !");
  }

  def collocSuivante(titre : Titre) :Titre = {
    nbAppelsCollocSuivante += 1
    if (titre.colloc.code == "") {
      titre.colloc.rangDansLaPage = 0
    } else {
      titre.colloc.rangDansLaPage += 1
    }
    titre.code = ""
    if (listeTest.nonEmpty) {
      if (titre.colloc.code.nonEmpty) {
        enleveCollocTest
      }
      titre.colloc.code = listeTest.head._1.toString
      if (listeTest.size == 1) {
        titre.colloc.derniereColloc = true
      } else {
        titre.colloc.derniereColloc = false
      }
    }
    return titre
  }

  def premierTitreColloc(titre : Titre) :Titre = {
    nbAppelsPremierTitreColloc += 1
    if (listeTest.nonEmpty) {
      if (listeTest.head._2.nonEmpty) {
        titre.code = listeTest.head._2.head.toString
        titre.rangDansLaPage = 0
        titre.colloc.pasDeTitre = false
      } else {
        titre.code = ""
        titre.rangDansLaPage = -1
        titre.colloc.pasDeTitre = true
      }
    }
    return titre
  }

  def titreSuivantColloc(titre : Titre) : Titre = {
    nbAppelsTitreSuivantColloc += 1
    if (listeTest.nonEmpty) {
      titre.code = listeTest.head._2.head.toString
      titre.rangDansLaPage += 1
    }
    return titre
  }

  def setDernierTitre(titre: Titre) : Titre = {
    if (listeTest.nonEmpty && listeTest.head._2.size == 1) {
        titre.dernierTitreDeLaColloc = true
    }
    enleveTitreTest
    return titre
  }

  def litTitre(titre : Titre) : Titre = {
    return titre
  }

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
          nbTitresTraites += 1
        }
        if (!titreEnCours.dernierTitreDeLaColloc) {
          titreEnCours = titreSuivantColloc(titreEnCours)
        }
      }
    } while (!titreEnCours.dernierTitreDeLaColloc || !titreEnCours.colloc.derniereColloc)
  }
}
