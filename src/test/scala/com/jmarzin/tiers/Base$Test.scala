package test.scala.com.jmarzin.tiers

import java.io.File

import com.google.common.io.Files
import main.scala.com.jmarzin.tiers.{Base, Debiteur, SessionEnCours, Piece}
import org.scalatest.{BeforeAndAfter, FunSpec, ShouldMatchers}

import scala.reflect.io.Path


/**
  * Created by jmarzin-cp on 28/12/2016.
  */
class Base$Test extends FunSpec with ShouldMatchers with BeforeAndAfter {

  describe("Une base initialisée contient tables") {
    Base.init("C:\\tiers\\restes")
    val rs = Base.ordreSql.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table';")
    rs.getInt(1) should be (4)
  }
  describe("Une base vidée est vide !") {
    Base.vide()
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM debiteur;").getInt(1) should be (0)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM titre;").getInt(1) should be (0)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM adresse;").getInt(1) should be (0)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM session;").getInt(1) should be (0)
  }
  describe("Un titre sauvé a son débiteur dans la base") {
    Base.init("C:\\tiers\\restes")
    val titre = new Piece
    titre.debiteur = new Debiteur
    titre.debiteur.identifiant = "débiteur 1"
    titre.debiteur.nomRs = "Marzin"
    titre.debiteur.prenom = "Jacques"
    titre.code = "titre 1"
    titre.colloc.code = "colloc 1"
    titre.resteARecouvrerFrais = 82100
    titre.resteARecouvrerPrincipal = 1221500
    titre.debiteur.adresse.cpVille = "63960 VEYRE MONTON"
    SessionEnCours.collocsTraiteesTitres = List("1","2")
    SessionEnCours.collocEnCoursTitres = "3"
    SessionEnCours.titresTraites = List("1","2","3","4","5","6")
    Base.sauve(titre)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM debiteur;").getInt(1) should be (1)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM titre;").getInt(1) should be (1)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM adresse;").getInt(1) should be (1)
    Base.ordreSql.executeQuery("SELECT COUNT(*) FROM session;").getInt(1) should be (1)
  }
}
