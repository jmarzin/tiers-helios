package test.scala.com.jmarzin.tiers

import main.scala.com.jmarzin.tiers.{Colloc, Helios, SessionEnCours, Titre}
import org.scalatest._

/**
  * Created by jmarzin-cp on 26/12/2016.
  */
class HeliosTest$Test extends FunSpec with ShouldMatchers with BeforeAndAfter {

  val colloc = new Colloc
  val titre = new Titre
  titre.colloc = colloc

  before {
    Helios.listeTest = List((1, List(1, 2, 3)), (2, List(4, 5, 6)))
  }

  describe("Si la liste de test est vide") {
    it("la fonction enleveCollocTest ne fait rien") {
      Helios.listeTest = List()
      Helios.enleveCollocTest
      Helios.listeTest should be(List())
    }
    it("la fonction enleveTitreTest ne fait rien") {
      Helios.listeTest = List()
      Helios.enleveTitreTest
      Helios.listeTest should be(List())
    }
  }

  describe("Si la liste de test n'est pas vide") {
    it("la fonction enleveCollocTest enlève la 1ère collectivité") {
      Helios.enleveCollocTest
      Helios.listeTest should be(List((2, List(4, 5, 6))))
    }
    it("la fonction enleveTitreTest ne fait rien si la liste des titres est vide") {
      Helios.listeTest = (0, List()) :: Helios.listeTest
      Helios.enleveTitreTest
      Helios.listeTest should be(List((0, List()), (1, List(1, 2, 3)), (2, List(4, 5, 6))))
    }
    it("la fonction enleveTitreTest enlève le 1er titre s'il existe") {
      Helios.enleveTitreTest
      Helios.listeTest should be(List((1, List(2, 3)), (2, List(4, 5, 6))))
    }
    it("la fonction collocSuivante enlève la 1ere colloc si le code colloc existe") {
      titre.colloc.code = "qqchose"
      Helios.collocSuivante(titre)
      Helios.listeTest should be(List((2, List(4, 5, 6))))
    }
    it("la fonction collocSuivante n'enlève pas la 1ère colloc si le code est vide") {
      titre.colloc.code = ""
      Helios.collocSuivante(titre)
      Helios.listeTest should be(List((1, List(1, 2, 3)), (2, List(4, 5, 6))))
    }
    it("la fonction collocSuivante initie le code colloc et derniereColloc") {
      titre.colloc.code = ""
      Helios.collocSuivante(titre)
      titre.colloc.code should be ("1")
      titre.colloc.derniereColloc should be (false)
      Helios.collocSuivante(titre)
      titre.colloc.code should be ("2")
      titre.colloc.derniereColloc should be (true)
    }
    it("la fonction premierTitreColloc initie le code titre et l'indicateur d'existence de titre s'il y a un titre") {
      titre.rangDansLaPage = 7
      Helios.premierTitreColloc(titre)
      titre.code should be ("1")
      titre.colloc.pasDeTitre should be (false)
      titre.rangDansLaPage should be (0)
    }
    it("la fonction premierTitreColloc initie le code titre et l'indicateur d'existence de titre s'il n'y en a pas") {
      titre.rangDansLaPage = 7
      Helios.listeTest = List((1,List()))
      Helios.premierTitreColloc(titre)
      titre.colloc.pasDeTitre should be (true)
      titre.rangDansLaPage should be (-1)
    }
    it("la fonction premierTitreColloc ne retire pas le titre") {
      Helios.premierTitreColloc(titre)
      Helios.listeTest should be (List((1, List(1,2, 3)), (2, List(4, 5, 6))))
    }
    it("la fonction titreSuivant initie le numéro du titre") {
      titre.code = ""
      Helios.titreSuivantColloc(titre)
      titre.code should be ("1")
    }
    it("la fonction titreSuivant initie le rang du titre") {
      titre.rangDansLaPage = 7
      Helios.titreSuivantColloc(titre)
      titre.rangDansLaPage should be (8)
    }
    it("la fonction setDernierTitre initie l'indicateur de dernier titre") {
      titre.dernierTitreDeLaColloc = false
      Helios.setDernierTitre(titre)
      titre.dernierTitreDeLaColloc should be (false)
      Helios.setDernierTitre(titre)
      titre.dernierTitreDeLaColloc should be (false)
      Helios.setDernierTitre(titre)
      titre.dernierTitreDeLaColloc should be (true)
    }
    it("la fonction setDernierTitre retire le titre") {
      Helios.setDernierTitre(titre)
      Helios.listeTest should be ((List((1, List(2, 3)), (2, List(4, 5, 6)))))
    }
  }
}