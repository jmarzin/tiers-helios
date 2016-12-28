package test.scala.com.jmarzin.tiers

import main.scala.com.jmarzin.tiers.{Helios, SessionEnCours}
import org.scalacheck.Prop.True
import org.scalatest.{BeforeAndAfter, FunSpec, ShouldMatchers}

/**
  * Created by jmarzin-cp on 27/12/2016.
  */
class HeliosParcours$Test extends FunSpec with ShouldMatchers with BeforeAndAfter {

  before {
    SessionEnCours.collocEnCours = ""
    SessionEnCours.titresTraites = List()
    SessionEnCours.collocsTraitees = List()
    Helios.nbAppelsTitreSuivantColloc = 0
    Helios.nbAppelsPremierTitreColloc = 0
    Helios.nbAppelsCollocSuivante = 0
    Helios.nbTitresTraites = 0
  }

  describe("le parcours d'1 colloc sans titre") {
    it("ne traite rien") {
      Helios.listeTest = List((1, List()))
      Helios.parcours
      Helios.nbTitresTraites should be(0)
      Helios.nbAppelsCollocSuivante should be (1)
      Helios.nbAppelsPremierTitreColloc should be (1)
      Helios.nbAppelsTitreSuivantColloc should be (0)
      SessionEnCours.collocsTraitees.isEmpty should be(true)
      SessionEnCours.collocEnCours should be("1")
      SessionEnCours.titresTraites.isEmpty should be(true)
    }
  }
  describe("le parcours de 2 collocs sans titre") {
    it ("ne traite rien") {
      Helios.listeTest = List((1, List()), (2, List()))
      Helios.parcours
      Helios.nbTitresTraites should be(0)
      Helios.nbAppelsCollocSuivante should be (2)
      Helios.nbAppelsPremierTitreColloc should be (2)
      Helios.nbAppelsTitreSuivantColloc should be (0)
      SessionEnCours.collocsTraitees should be(List("1"))
      SessionEnCours.collocEnCours should be("2")
      SessionEnCours.titresTraites.isEmpty should be(true)
    }
  }

  describe("le parcours d'une liste avec un titre") {
    it("traite un titre et le mémorise") {
      Helios.listeTest = List((1, List(1)))
      Helios.parcours
      Helios.nbTitresTraites should be(1)
      Helios.nbAppelsCollocSuivante should be (1)
      Helios.nbAppelsPremierTitreColloc should be (1)
      Helios.nbAppelsTitreSuivantColloc should be (0)
      SessionEnCours.collocsTraitees.isEmpty should be(true)
      SessionEnCours.collocEnCours should be("1")
      SessionEnCours.titresTraites should be(List("1"))
    }
  }
  describe("le parcours d'une liste de deux titres") {
    it("traite 2 titres et les mémorise") {
      Helios.listeTest = List((1, List(1,2)))
      Helios.parcours
      Helios.nbTitresTraites should be(2)
      Helios.nbAppelsCollocSuivante should be (1)
      Helios.nbAppelsPremierTitreColloc should be (1)
      Helios.nbAppelsTitreSuivantColloc should be (1)
      SessionEnCours.collocsTraitees.isEmpty should be(true)
      SessionEnCours.collocEnCours should be("1")
      SessionEnCours.titresTraites should be(List("1","2"))
    }
  }
  describe("le parcours d'une liste de trois titres") {
    it("traite 2 titres et les mémorise") {
      Helios.listeTest = List((1, List(1,2,3)))
      Helios.parcours
      Helios.nbTitresTraites should be(3)
      Helios.nbAppelsCollocSuivante should be (1)
      Helios.nbAppelsPremierTitreColloc should be (1)
      Helios.nbAppelsTitreSuivantColloc should be (2)
      SessionEnCours.collocsTraitees.isEmpty should be(true)
      SessionEnCours.collocEnCours should be("1")
      SessionEnCours.titresTraites should be(List("1","2","3"))
    }
  }
  describe("le parcours d'une liste avec une colloc vide et une de deux titres") {
    it("traite 2 titres et les mémorise") {
      Helios.listeTest = List((1,List()),(2, List(1,2)))
      Helios.parcours
      Helios.nbAppelsCollocSuivante should be (2)
      Helios.nbAppelsPremierTitreColloc should be (2)
      Helios.nbAppelsTitreSuivantColloc should be (1)
      SessionEnCours.collocsTraitees should be(List("1"))
      SessionEnCours.collocEnCours should be("2")
      SessionEnCours.titresTraites should be(List("1","2"))
      Helios.nbTitresTraites should be(2)
    }
  }
  describe("le parcours d'une liste avec une colloc de deux titres et une vide") {
    it("traite 2 titres et les mémorise") {
      Helios.listeTest = List((1,List(1,2)),(2, List()))
      Helios.parcours
      Helios.nbAppelsCollocSuivante should be (2)
      Helios.nbAppelsPremierTitreColloc should be (2)
      Helios.nbAppelsTitreSuivantColloc should be (1)
      SessionEnCours.collocsTraitees should be(List("1"))
      SessionEnCours.collocEnCours should be("2")
      SessionEnCours.titresTraites should be(List())
      Helios.nbTitresTraites should be(2)
    }
  }
  describe("le parcours d'une liste avec une colloc de deux titres, un colloc vide et une de 1 titre") {
    it("traite 3 titres et les mémorsie") {
      Helios.listeTest = List((1,List(1,2)),(2,List()),(3,List(3)))
      Helios.parcours
      Helios.nbAppelsCollocSuivante should be (3)
      Helios.nbAppelsPremierTitreColloc should be (3)
      Helios.nbAppelsTitreSuivantColloc should be (1)
      SessionEnCours.collocsTraitees should be (List("1","2"))
      SessionEnCours.collocEnCours should be ("3")
      SessionEnCours.titresTraites should be (List("3"))
      Helios.nbTitresTraites should be (3)
    }
  }
  describe("le parcours d'une liste avec 1 colloc de 2 titres, 1 cvide, 1 de 3 titres et 1 de 1 titre") {
    it("traite 6 titres et les mémorsie") {
      Helios.listeTest = List((1,List(1,2)),(2,List()),(3,List(3,4,5)),(4,List(6)))
      Helios.parcours
      Helios.nbAppelsCollocSuivante should be (4)
      Helios.nbAppelsPremierTitreColloc should be (4)
      Helios.nbAppelsTitreSuivantColloc should be (3)
      SessionEnCours.collocsTraitees should be (List("1","2","3"))
      SessionEnCours.collocEnCours should be ("4")
      SessionEnCours.titresTraites should be (List("6"))
      Helios.nbTitresTraites should be (6)
    }
  }
  describe("le parcours saute les collectivités et les titres déjà traités") {
    it("traite 4 titres et les mémorise si 1ère collectivité") {
      Helios.listeTest = List((1,List(1,2)),(2,List()),(3,List(3,4,5)),(4,List(6)))
      SessionEnCours.collocsTraitees = List("1")
      SessionEnCours.collocEnCours = ""
      SessionEnCours.titresTraites = List()
      Helios.parcours
      Helios.nbAppelsCollocSuivante should be (4)
      Helios.nbAppelsPremierTitreColloc should be (3)
      Helios.nbAppelsTitreSuivantColloc should be (2)
      SessionEnCours.collocsTraitees should be (List("1","2","3"))
      SessionEnCours.collocEnCours should be ("4")
      SessionEnCours.titresTraites should be (List("6"))
      Helios.nbTitresTraites should be (4)
    }
    it("traite 2 titres et les mémorise si 1ere, 2ème collocs et 2 premiers titres de la 3ème") {
      Helios.listeTest = List((1,List(1,2)),(2,List()),(3,List(3,4,5)),(4,List(6)))
      SessionEnCours.collocsTraitees = List("1","2")
      SessionEnCours.collocEnCours = "3"
      SessionEnCours.titresTraites = List("3","4")
      Helios.parcours
      Helios.nbAppelsCollocSuivante should be (4)
      Helios.nbAppelsPremierTitreColloc should be (2)
      Helios.nbAppelsTitreSuivantColloc should be (2)
      SessionEnCours.collocsTraitees should be (List("1","2","3"))
      SessionEnCours.collocEnCours should be ("4")
      SessionEnCours.titresTraites should be (List("6"))
      Helios.nbTitresTraites should be (2)
    }

  }


}
