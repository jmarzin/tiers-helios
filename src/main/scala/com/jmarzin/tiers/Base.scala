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
    SessionEnCours.collocsTraitees = List()
    SessionEnCours.collocEnCours =""
    SessionEnCours.titresTraites = List()
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
          "nfp text, "                      + "refAdresse integer references adresse (rowid))")
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
        "create table session (collocsTraitees text, collocEnCours, titresTraites text)");
    }
    rs = ordreSql.executeQuery("SELECT * FROM session;")
    if (!rs.next) {
      ordreSql.executeUpdate("insert into session values('','','');")
      SessionEnCours.collocsTraitees = List()
      SessionEnCours.collocEnCours = ""
      SessionEnCours.titresTraites = List()
    } else {
      var liste = rs.getString(1)
      SessionEnCours.collocsTraitees = liste.substring(5,liste.length-1).split(",").
        toList.filter(p => p != "" && p != " ").map(_.trim)
      SessionEnCours.collocEnCours = rs.getString(2)
      liste = rs.getString(3)
      SessionEnCours.titresTraites = liste.substring(5,liste.length-1).split(",").
        toList.filter(p => p != "" && p != " ").map(_.trim)
      println (SessionEnCours.titresTraites)
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
      SessionEnCours.collocsTraitees.toString + "','" + SessionEnCours.collocEnCours + "','" +
      SessionEnCours.titresTraites.toString + "');"
    print("\r\n" + chaine)
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
  def sauve(titre : Titre) : Unit = {

    var chaine = ""
    val rs = ordreSql.executeQuery("SELECT rowid FROM debiteur WHERE identifiant ='" +
      titre.debiteur.identifiant + "';")
    if (!rs.next) {
      chaine = "insert into adresse values('" +
        titre.debiteur.adresse.adressePrincipale + "','" + titre.debiteur.adresse.npai + "','" +
        titre.debiteur.adresse.dateMiseAJour + "','" + titre.debiteur.adresse.origineMiseAJour + "','" +
        titre.debiteur.adresse.complementAdresse + "','" + titre.debiteur.adresse.numeroEtVoie + "','" +
        titre.debiteur.adresse.localite + "','" + titre.debiteur.adresse.cpVille + "','" +
        titre.debiteur.adresse.pays + "');"
      print ("\r\n" + chaine)
      ordreSql.executeUpdate(chaine)
      var id = ordreSql.executeQuery("SELECT last_insert_rowid();;").getInt(1)
      chaine = "insert into debiteur values('" +
        titre.debiteur.identifiant + "','" + titre.debiteur.listeConsolidation + "','" +
        titre.debiteur.consolide + "','" + titre.debiteur.dateConsolidation + "','" +
        titre.debiteur.categorie + "','" + titre.debiteur.natureJuridique + "','" +
        titre.debiteur.immatriculation + "','" + titre.debiteur.statutDgfip + "','" +
        titre.debiteur.dateStatutDgfip + "','" + titre.debiteur.civilite + "','" +
        titre.debiteur.nomRs + "','" + titre.debiteur.prenom + "','" +
        titre.debiteur.complement + "','" + titre.debiteur.residentEnFrance + "','" +
        titre.debiteur.dateDeNaissance + "','" + titre.debiteur.telDomicile + "','" +
        titre.debiteur.telPortable + "','" + titre.debiteur.emel + "','" +
        titre.debiteur.compteParDefaut + "','" + titre.debiteur.codeActualite + "','" +
        titre.debiteur.dateMiseAJour + "','" + titre.debiteur.nfp + "'," +
        id + ");"
      print ("\r\n" + chaine)
      ordreSql.executeUpdate(chaine)
    }
    chaine = "insert into titre values('" +
      titre.colloc.code               + "','" + titre.code                      + "'," +
      titre.resteARecouvrerPrincipal  + ","   + titre.resteARecouvrerFrais      + ",'"  +
      titre.dateEmission              + "','" + titre.datePrescription          + "','"  +
      titre.debiteur.identifiant      + "');"
    print ("\r\n" + chaine)
    ordreSql.executeUpdate(chaine)

    sauveSession
  }

  def litTiers: Vecteur = {
    val chaine = "select t1.listeConsolidation, t1.nomRS, t1.prenom, t2.cpVille, t2.numeroEtVoie, "+
                "t2.complementAdresse from debiteur t1, adresse t2 where t1.refAdresse = t2.rowid and " +
                "t1.listeConsolidation in (select distinct listeConsolidation from debiteur);"
    val rs = ordreSql.executeQuery(chaine)
    var vect = Vector[Item]()
    var item = Item("","")
    breakable {
      AppTiers.nbDebiteursLus = 0
      while (rs.next) {
        if (Thread.interrupted) return Vecteur(vect)
        item = Item(rs.getString(1), rs.getString(2) + " " + rs.getString(3) +
          rs.getString(4) + rs.getString(5) + rs.getString(6))
        AppTiers.nbDebiteursLus += 1
        if (item.code != "" && item.texte != "") vect = vect :+ item
        //TODO supprimer la sortie de boucle après les tests
        //if (vect.size == 100) break
      }
    }
    return Vecteur(vect)
  }

  def titreConnu(titre: Titre): Boolean = {
    val rs = ordreSql.executeQuery("select * from titre where codeColloc = '" +
    titre.colloc.code + "' and code = '" + titre.code +"';")
    return rs.next()
  }

  def ferme: Unit = {
    connexion.close
  }
}
