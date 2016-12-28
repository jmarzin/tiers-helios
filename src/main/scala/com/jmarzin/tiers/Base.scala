package main.scala.com.jmarzin.tiers

import java.sql._

import specs2.text

import scalaz.std.effect.sql.statement


/**
  * Created by jmarzin-cp on 24/12/2016.
  */

object Base {
  val connexion = DriverManager.getConnection("jdbc:sqlite:C:/tiers/restes.db")
  val ordreSql = connexion.createStatement

  def init(repertoire: String) : Unit = {
    SessionEnCours.collocsTraitees = List()
    SessionEnCours.collocEnCours =""
    SessionEnCours.titresTraites = List()
    Class.forName("org.sqlite.JDBC")
    val rs = ordreSql.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='debiteur';")
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
          "nfp text, "                      + "refAdresse integer references adresse (rowid))")
      ordreSql.executeUpdate(
        "create table titre " +
          "(codeColloc text, "              + "code text, "         + "resteARecouvrerPrincipal integer, " +
          "resteARecouvrerFrais integer, "  + "dateEmission text, " + "refDebiteur text references tiers (identifiant))")
      ordreSql.executeUpdate(
        "create unique index codeCollIdTitre on titre (codeColloc, code)")
      ordreSql.executeUpdate(
        "create table adresse " +
          "(adressePrincipale text, " + "npai text, "               + "dateMiseAJour text, " +
          "origineMiseAJour text, "   + "complementAdresse text, "  + "numeroEtVoie text, " +
          "localite text, "           + "cpVille text, "            + "pays text)")
      ordreSql.executeUpdate(
        "create table session (collocsTraitees text, collocEnCours, titresTraites text)");
    }
    //TODO lire la session en cours  : liste.substring(5,liste.length-1).split(",").toList
  }

  def vide: Unit = {
    SessionEnCours.raz
    ordreSql.execute("DELETE FROM debiteur;")
    ordreSql.execute("DELETE FROM titre;")
    ordreSql.execute("DELETE FROM adresse;")
    ordreSql.execute("DELETE FROM session;")
  }

  def sauve(titre : Titre) : Unit = {
    var chaine = "insert into adresse values('" +
      titre.debiteur.adresse.adressePrincipale  + "','" + titre.debiteur.adresse.npai             + "','" +
      titre.debiteur.adresse.dateMiseAJour      + "','" + titre.debiteur.adresse.origineMiseAJour + "','" +
      titre.debiteur.adresse.complementAdresse  + "','" + titre.debiteur.adresse.numeroEtVoie     + "','" +
      titre.debiteur.adresse.localite           + "','" + titre.debiteur.adresse.cpVille          + "','" +
      titre.debiteur.adresse.pays               + "');"
    ordreSql.executeUpdate(chaine)
    var id = ordreSql.executeQuery("SELECT last_insert_rowid();;").getInt(1)
    chaine = "insert into debiteur values('" +
      titre.debiteur.identifiant      + "','" + titre.debiteur.listeConsolidation  + "','" +
      titre.debiteur.consolide        + "','"   + titre.debiteur.dateConsolidation + "','" +
      titre.debiteur.categorie        + "','" + titre.debiteur.natureJuridique     + "','" +
      titre.debiteur.immatriculation  + "','" + titre.debiteur.statutDgfip         + "','" +
      titre.debiteur.dateStatutDgfip  + "','" + titre.debiteur.civilite            + "','" +
      titre.debiteur.nomRs            + "','" + titre.debiteur.prenom              + "','" +
      titre.debiteur.complement       + "','" + titre.debiteur.residentEnFrance    + "','" +
      titre.debiteur.dateDeNaissance  + "','" + titre.debiteur.telDomicile         + "','" +
      titre.debiteur.telPortable      + "','" + titre.debiteur.emel                + "','" +
      titre.debiteur.compteParDefaut  + "','" + titre.debiteur.codeActualite       + "','" +
      titre.debiteur.dateMiseAJour    + "','" + titre.debiteur.nfp                 + "'," +
      id                              + ");"
    //      "refAdresse integer references adresse (rowid))");
    ordreSql.executeUpdate(chaine)
    chaine = "insert into titre values('" +
      titre.colloc.code               + "','" + titre.code                      + "'," +
      titre.resteARecouvrerPrincipal  + ","   + titre.resteARecouvrerFrais      + ",'"  +
      titre.dateEmission              + "','" + titre.debiteur.identifiant      + "');"
    ordreSql.executeUpdate(chaine)
    chaine = "insert into session values('" +
      SessionEnCours.collocsTraitees.toString + "','" + SessionEnCours.collocEnCours + "','" +
      SessionEnCours.titresTraites.toString   + "');"
    ordreSql.executeUpdate(chaine)
  }

  def ferme: Unit = {
    connexion.close
  }
}
