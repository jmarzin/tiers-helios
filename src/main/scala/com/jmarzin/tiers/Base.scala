package main.scala.com.jmarzin.tiers

import java.sql._

import main.scala.com.jmarzin.tiers
import org.openqa.selenium.SessionNotCreatedException
import specs2.text

import scalaz.std.effect.sql.statement
import util.control.Breaks._

/**
  * Created by jmarzin-cp on 24/12/2016.
  */

object Base {
  val connexion = DriverManager.getConnection("jdbc:sqlite:C:/tiers/restes.db")
  val ordreSql = connexion.createStatement

  def init(repertoire: String) : Unit = {
    SessionEnCours.collocsTraiteesTitres = List()
    SessionEnCours.collocEnCoursTitres =""
    SessionEnCours.titresTraites = List()
    SessionEnCours.collocsTraiteesArticles = List()
    SessionEnCours.collocEnCoursArticles =""
    SessionEnCours.articlesTraites = List()
    Class.forName("org.sqlite.JDBC")
    var rs = ordreSql.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='debiteur';")
    if (!rs.next) {
      ordreSql.executeUpdate(
        "create table debiteur " +
          "(identifiant text primary key, " + "listeConsolidation text, " + "consolide text, "       +
          "dateConsolidation text, "        + "categorie text, "          + "natureJuridique text, " +
          "immatriculation text, "          + "statutDgfip text, "        + "dateStatutDgfip text, " +
          "civilite text, "                 + "nomRs text, "              + "prenom text, "          +
          "complement text, "               + "residentEnFrance text, "   + "dateDeNaissance text, " +
          "telDomicile text, "              + "telPortable text, "        + "emel text, "            +
          "compteParDefaut, text"           + "codeActualite text, "      + "dateMiseAJour text, "   +
          "nfp text, "                      + "nomRsEmployeur text, "     + "cpVilleEmployeur text, "+
          "nomRsCaf text, "                 + "cpVilleCaf text, "         + "numAllocataireCaf text, "+
          "refAdresse integer references adresse (rowid))")
      ordreSql.executeUpdate(
        "create table titre " +
          "(codeColloc text, "              + "code text, "         + "resteARecouvrerPrincipal integer, " +
          "resteARecouvrerFrais integer, "  + "dateEmission text, " + "datePrescription text, refDebiteur text references tiers (identifiant))")
      ordreSql.executeUpdate(
        "create unique index codeCollIdTitre on titre (codeColloc, code)")
      ordreSql.executeUpdate(
        "create table adresse " +
          "(adressePrincipale text, " + "npai text, "               + "dateMiseAJour text, " +
          "origineMiseAJour text, "   + "complementAdresse text, "  + "numeroEtVoie text, " +
          "localite text, "           + "cpVille text, "            + "pays text)")
      ordreSql.executeUpdate(
        "create table session " +
          "(collocsTraiteesTitres text, collocTitresEnCours text, titresTraites text, " +
          "collocsTraiteesArticles text, collocArticlesEnCours text, articlesTraites text)")
    }
    rs = ordreSql.executeQuery("SELECT * FROM session;")
    if (!rs.next) {
      ordreSql.executeUpdate("insert into session values('','','','','','');")
      SessionEnCours.collocsTraiteesTitres = List()
      SessionEnCours.collocEnCoursTitres = ""
      SessionEnCours.titresTraites = List()
      SessionEnCours.collocsTraiteesArticles = List()
      SessionEnCours.collocEnCoursArticles = ""
      SessionEnCours.articlesTraites = List()
    } else {
      var liste = rs.getString(1)
      SessionEnCours.collocsTraiteesTitres = liste.substring(5,liste.length-1).split(",").
        toList.filter(p => p != "" && p != " ").map(_.trim)
      SessionEnCours.collocEnCoursTitres = rs.getString(2)
      liste = rs.getString(3)
      SessionEnCours.titresTraites = liste.substring(5,liste.length-1).split(",").
        toList.filter(p => p != "" && p != " ").map(_.trim)
      liste = rs.getString(4)
      SessionEnCours.collocsTraiteesArticles = liste.substring(5,liste.length-1).split(",").
        toList.filter(p => p != "" && p != " ").map(_.trim)
      SessionEnCours.collocEnCoursArticles = rs.getString(5)
      liste = rs.getString(6)
      SessionEnCours.articlesTraites = liste.substring(5,liste.length-1).split(",").
        toList.filter(p => p != "" && p != " ").map(_.trim)
    }
  }

  def vide: Unit = {
    SessionEnCours.raz
    ordreSql.execute("DELETE FROM debiteur;")
    ordreSql.execute("DELETE FROM titre;")
    ordreSql.execute("DELETE FROM adresse;")
    ordreSql.execute("DELETE FROM session;")
  }

  def sauveSession = {
    ordreSql.executeUpdate("DELETE FROM session")
    val chaine = "insert into session values('" +
      SessionEnCours.collocsTraiteesTitres.toString + "','" +
      SessionEnCours.collocEnCoursTitres + "','" +
      SessionEnCours.titresTraites.toString + "','" +
      SessionEnCours.collocsTraiteesArticles.toString + "','" +
      SessionEnCours.collocEnCoursArticles + "','" +
      SessionEnCours.articlesTraites.toString + "');"
    ordreSql.executeUpdate(chaine)
  }

  def sauve(res: Vector[Triplet]) : Unit = {
    ordreSql.executeUpdate(
      "create table if not exists doublon" +
        "(id1 text, " + "id2 text, " + "distance int, " + "statut text)")
    ordreSql.executeUpdate(
      "create unique index if not exists id1Id2 on doublon (id1, id2)")
    AppTiers.nbDoublonsEcrits = 0
    AppTiers.nbDoublonsAEcrire = res.size
    for(triplet <- res) {
      if (Thread.interrupted) return
      val rs = ordreSql.executeQuery("SELECT * FROM doublon WHERE id1='" +
        triplet.code1 + "' and id2='" + triplet.code2 + "';")
      if (!rs.next) {
        ordreSql.executeUpdate("insert into doublon values('" +
        triplet.code1 + "','" + triplet.code2 + "'," + triplet.dist + ",'');")
      }
      AppTiers.nbDoublonsEcrits += 1
    }
  }
  def sauve(piece : Piece) : Unit = {
    if(piece.resteARecouvrerFrais == 0 && piece.resteARecouvrerPrincipal ==0) {
      return
    }
    var chaine = ""
    val rs = ordreSql.executeQuery("SELECT rowid FROM debiteur WHERE identifiant ='" +
      piece.debiteur.identifiant + "';")
    if (!rs.next) {
      chaine = "insert into adresse values('" +
        piece.debiteur.adresse.adressePrincipale + "','" + piece.debiteur.adresse.npai + "','" +
        piece.debiteur.adresse.dateMiseAJour + "','" + piece.debiteur.adresse.origineMiseAJour + "','" +
        piece.debiteur.adresse.complementAdresse + "','" + piece.debiteur.adresse.numeroEtVoie + "','" +
        piece.debiteur.adresse.localite + "','" + piece.debiteur.adresse.cpVille + "','" +
        piece.debiteur.adresse.pays + "');"
      print ("\r\n" + chaine)
      ordreSql.executeUpdate(chaine)
      var id = ordreSql.executeQuery("SELECT last_insert_rowid();;").getInt(1)
      chaine = "insert into debiteur values('" +
        piece.debiteur.identifiant + "','" + piece.debiteur.listeConsolidation + "','" +
        piece.debiteur.consolide + "','" + piece.debiteur.dateConsolidation + "','" +
        piece.debiteur.categorie + "','" + piece.debiteur.natureJuridique + "','" +
        piece.debiteur.immatriculation + "','" + piece.debiteur.statutDgfip + "','" +
        piece.debiteur.dateStatutDgfip + "','" + piece.debiteur.civilite + "','" +
        piece.debiteur.nomRs + "','" + piece.debiteur.prenom + "','" +
        piece.debiteur.complement + "','" + piece.debiteur.residentEnFrance + "','" +
        piece.debiteur.dateDeNaissance + "','" + piece.debiteur.telDomicile + "','" +
        piece.debiteur.telPortable + "','" + piece.debiteur.emel + "','" +
        piece.debiteur.compteParDefaut + "','" + piece.debiteur.codeActualite + "','" +
        piece.debiteur.dateMiseAJour + "','" + piece.debiteur.nfp + "','" +
        piece.debiteur.nomRsEmployeur + "','" + piece.debiteur.cpVilleEmployeur + "','" +
        piece.debiteur.nomRsCaf + "','" + piece.debiteur.cpVilleCaf + "','" +
        piece.debiteur.numAllocataireCaf + "'," +
        id + ");"
      print ("\r\n" + chaine)
      ordreSql.executeUpdate(chaine)
    }
    chaine = "insert into titre values('" +
      piece.colloc.code               + "','" + piece.code                      + "'," +
      piece.resteARecouvrerPrincipal  + ","   + piece.resteARecouvrerFrais      + ",'"  +
      piece.dateEmission              + "','" + piece.datePrescription          + "','"  +
      piece.debiteur.identifiant      + "');"
    print ("\r\n" + chaine)
    ordreSql.executeUpdate(chaine)
  }

  def litTiers: Vecteur = {
    val chaine = "select t1.listeConsolidation, t1.nomRS, t1.prenom, t2.cpVille, t2.numeroEtVoie, "+
                "t2.complementAdresse, t1.compteParDefaut from debiteur t1, adresse t2 where t1.refAdresse = t2.rowid and " +
                "t1.listeConsolidation in (select distinct listeConsolidation from debiteur);"
    val rs = ordreSql.executeQuery(chaine)
    var vect = Vector[Item]()
    var item = Item("","")
    breakable {
      AppTiers.nbDebiteursLus = 0
      while (rs.next) {
        if (Thread.interrupted) return Vecteur(vect)
        item = Item(rs.getString(1), rs.getString(2) + " " + rs.getString(3) +
          rs.getString(4) + rs.getString(5) + rs.getString(6) + rs.getString(7))
        AppTiers.nbDebiteursLus += 1
        if (item.code != "" && item.texte != "") vect = vect :+ item
      }
    }
    return Vecteur(vect)
  }

  def pieceConnue(titre: Piece): Boolean = {
    val rs = ordreSql.executeQuery("select * from titre where codeColloc = '" +
    titre.colloc.code + "' and code = '" + titre.code +"';")
    return rs.next()
  }

  def ferme: Unit = {
    connexion.close
  }
}
