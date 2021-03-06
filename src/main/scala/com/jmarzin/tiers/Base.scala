package main.scala.com.jmarzin.tiers

import java.sql._

import scala.collection.mutable.ArrayBuffer
import util.control.Breaks._

/**
  * Created by jmarzin-cp on 24/12/2016.
  */

object Base {
  var connexion: Connection = _
  var ordreSql: Statement = _

  def init(base: String) : Unit = {
    connexion = DriverManager.getConnection("jdbc:sqlite:"+base+".db")
    ordreSql = connexion.createStatement
    SessionEnCours.collocsTraiteesTitres = List()
    SessionEnCours.collocEnCoursTitres =""
    SessionEnCours.pageTitres = ""
    SessionEnCours.titresTraites = List()
    SessionEnCours.collocsTraiteesArticles = List()
    SessionEnCours.collocEnCoursArticles =""
    SessionEnCours.pageArticles = ""
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
          "collocsTraiteesArticles text, collocArticlesEnCours text, articlesTraites text, " +
          "pageTitres text, pageArticles text)")
    }
    rs = ordreSql.executeQuery("SELECT * FROM session;")
    if (!rs.next) {
      ordreSql.executeUpdate("insert into session values('','','','','','','','');")
      SessionEnCours.collocsTraiteesTitres = List()
      SessionEnCours.collocEnCoursTitres = ""
      SessionEnCours.pageTitres = ""
      SessionEnCours.titresTraites = List()
      SessionEnCours.collocsTraiteesArticles = List()
      SessionEnCours.collocEnCoursArticles = ""
      SessionEnCours.pageArticles = ""
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
      SessionEnCours.pageTitres = rs.getString(7)
      SessionEnCours.pageArticles = rs.getString(8)
    }
  }

  def vide(): Unit = {
    SessionEnCours.raz()
    ordreSql.execute("DELETE FROM debiteur;")
    ordreSql.execute("DELETE FROM titre;")
    ordreSql.execute("DELETE FROM adresse;")
    ordreSql.execute("DELETE FROM session;")
  }

  def sauveSession(): Unit = {
    ordreSql.executeUpdate("DELETE FROM session")
    val chaine = "insert into session values('" +
      SessionEnCours.collocsTraiteesTitres.toString + "','" +
      SessionEnCours.collocEnCoursTitres + "','" +
      SessionEnCours.titresTraites.toString + "','" +
      SessionEnCours.collocsTraiteesArticles.toString + "','" +
      SessionEnCours.collocEnCoursArticles + "','" +
      SessionEnCours.articlesTraites.toString + "','" +
      SessionEnCours.pageTitres + "','" +
      SessionEnCours.pageArticles + "');"
    ordreSql.executeUpdate(chaine)
  }

  def sauve(res: Vector[(Integer, Integer, Integer)]) : Unit = {
    ordreSql.executeUpdate(
      "create table if not exists doublon" +
        "(rowId1 int, " + "rowId2 int, " + "distance int, " + "statut text)")
    ordreSql.executeUpdate(
      "create unique index if not exists id1Id2 on doublon (rowId1, rowId2)")
    AppTiers.nbDoublonsEcrits = 0
    AppTiers.nbDoublonsAEcrire = res.size
    for(triplet <- res) {
      if (Thread.interrupted) return
      val rs = ordreSql.executeQuery("SELECT * FROM doublon WHERE rowId1='" +
        triplet._1 + "' and rowId2='" + triplet._2 + "';")
      if (!rs.next) {
        ordreSql.executeUpdate("insert into doublon values('" +
        triplet._1 + "','" + triplet._2 + "'," + triplet._3 + ",'');")
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
      val id = ordreSql.executeQuery("SELECT last_insert_rowid();;").getInt(1)
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
    val chaine = "select t1.rowId, t1.nomRS, t1.prenom, t2.cpVille, t2.numeroEtVoie, "+
                "t2.complementAdresse, t1.compteParDefaut from debiteur t1, adresse t2 where t1.refAdresse = t2.rowid and " +
                "t1.listeConsolidation in (select distinct listeConsolidation from debiteur);"
    val rs = ordreSql.executeQuery(chaine)
    var vect = Vector[Item]()
    var item = Item(0,"")
    breakable {
      AppTiers.nbDebiteursLus = 0
      while (rs.next) {
        if (Thread.interrupted) return Vecteur(vect)
        item = Item(rs.getInt(1), rs.getString(2) + " " + rs.getString(3) +
          rs.getString(4) + rs.getString(5) + rs.getString(6) + rs.getString(7))
        AppTiers.nbDebiteursLus += 1
        if (item.rowId != 0 && item.texte != "") vect = vect :+ item
      }
    }
    Vecteur(vect)
  }

  def pieceConnue(piece: Piece): Boolean = {
    val rs = ordreSql.executeQuery("select * from titre where codeColloc = '" +
    piece.colloc.code + "' and code = '" + piece.code +"';")
    rs.next()
  }
}
