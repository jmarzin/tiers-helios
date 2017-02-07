package test.scala.com.jmarzin.tiers

import main.scala.com.jmarzin.tiers.{Doublons, Item, Vecteur}
import org.apache.commons.text.LevenshteinDistance
import org.scalatest.{BeforeAndAfter, FunSpec, ShouldMatchers}

import scalaz.Digit.{_1, _2}

/**
  * Created by jmarzin-cp on 28/12/2016.
  */
class Distance$Test extends FunSpec with ShouldMatchers with BeforeAndAfter {
  val vect1 = Vecteur( Vector(
    Item("1","1"), Item("2","12"), Item("3","123"), Item("4","1234"), Item("5","12345"), Item("6","123456"),
    Item("7","1234567"), Item("8","12345678"), Item("9","123456789"), Item("10","1234567890"),
    Item("11","1234567890A"), Item("12","1234567890AB")))
  val vect2 = Vecteur( Vector(
    Item("1","1"), Item("2","12"), Item("3","123"), Item("4","1234"), Item("5","12345"), Item("6","123456"),
    Item("7","1234567"), Item("8","12345678"), Item("9","123456789"), Item("10","1234567890"),
    Item("11","1234567890A"), Item("12","1234567890AB"), Item("13","1234567890ABC")))

  describe("Le résultat du calcul") {
    it ("est un vecteur d'un taille  = size*size/2") {
      vect1.calculDistance.size should be ((vect1.vect.size * (vect1.vect.size - 1))/2)
      vect2.calculDistance.size should be ((vect2.vect.size * (vect2.vect.size - 1))/2)
    }
    it("prend beaucoup de temps pour un grand tableau") {
      val vect3 = Vecteur(vect1.vect ++ vect1.vect ++ vect1.vect ++ vect1.vect ++ vect1.vect ++ vect1.vect)
      val vect4 = Vecteur(vect3.vect ++ vect3.vect ++ vect3.vect ++ vect3.vect ++ vect3.vect ++ vect3.vect)
      val vect5 = Vecteur(vect4.vect ++ vect4.vect ++ vect4.vect ++ vect4.vect)
      vect5.length should be (1728)
      val res = vect5.calculDistance
      res.length should be (1492128)
    }
    it ("on construit une table par code avec les 10 meilleurs scores et les codes correspondants"){
      val res = vect1.calculDistance
      val table = Doublons.paquetParCode(vect1.vect, res)
      table.length should be (12)
    }
  }
}
