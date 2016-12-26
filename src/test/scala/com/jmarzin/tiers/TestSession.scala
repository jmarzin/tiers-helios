package test.scala.com.jmarzin.tiers

import groovy.transform.Trait
import main.scala.com.jmarzin.tiers.{Colloc, SessionEnCours, Titre}
import org.scalatest.{BeforeAndAfter, FunSpec, ShouldMatchers}

/**
  * Created by jmarzin-cp on 25/12/2016.
  */
class TestSession extends FunSpec with ShouldMatchers with BeforeAndAfter {

  val colloc = new Colloc
  val titre = new Titre
  titre.colloc = colloc
  before {
    SessionEnCours.listeCodesCollocs = List("1","2")
    SessionEnCours.listeCodesTitres = List("1","2")
  }
  describe("Une colloc mémorisée") {
    it ("se met à la fin de la liste et initialise la liste des titres") {
      colloc.code = "3"
      SessionEnCours.memoriseColloc(colloc)
      SessionEnCours.listeCodesCollocs should be (List("1","2","3"))
      SessionEnCours.listeCodesTitres.isEmpty should be (true)
    }
    it ("ne peut pas être mémorisée une 2ème fois") {
      colloc.code = "1"
      SessionEnCours.memoriseColloc(colloc)
      SessionEnCours.listeCodesCollocs should be (List("1","2"))
    }
    it ("est déjà traitée si elle n'est pas la dernière") {
      colloc.code = "1"
      SessionEnCours.collocDejaTraitee(colloc) should be (true)
      colloc.code = "2"
      SessionEnCours.collocDejaTraitee(colloc) should be (false)
    }
  }
  describe("Un titre mémorisé") {
    it ("se met à la fin de la liste s'il est nouveau et appartient à la dernière colloc") {
      titre.colloc.code = "2"
      titre.code = "3"
      SessionEnCours.memoriseTitre(titre)
      SessionEnCours.listeCodesTitres should be (List("1","2","3"))
    }
    it ("ne peut pas être mémorisée une 2ème fois") {
      titre.colloc.code = "2"
      titre.code = "1"
      SessionEnCours.memoriseTitre(titre)
      SessionEnCours.listeCodesTitres should be (List("1","2"))
    }
    it ("memorise la collectivité et le titre si la collectivité n'est pas dans la liste") {
      titre.colloc.code = "3"
      titre.code = "4"
      SessionEnCours.memoriseTitre(titre)
      SessionEnCours.listeCodesCollocs should be (List("1","2","3"))
      SessionEnCours.listeCodesTitres should be (List("4"))
    }
    it ("est déjà traité s'il est de la dernière colloc et est dans la liste") {
      titre.colloc.code = "2"
      titre.code = "1"
      SessionEnCours.titreDejaTraite(titre) should be (true)
      titre.code = "3"
      SessionEnCours.titreDejaTraite(titre) should be (false)
    }
    it ("est déjà traité s'il est d'une collectivité traitée") {
      titre.colloc.code = "1"
      titre.code = "4"
      SessionEnCours.titreDejaTraite(titre) should be (true)
    }
  }
  describe("La remise à zéro") {
    it ("vide la liste des collocs et la liste des titres") {
      SessionEnCours.raz
      SessionEnCours.listeCodesCollocs should be (List())
      SessionEnCours.listeCodesTitres should be (List())
    }
  }
}
