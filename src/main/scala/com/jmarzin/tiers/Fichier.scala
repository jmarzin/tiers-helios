package main.scala.com.jmarzin.tiers

import java.io.{File, FileOutputStream, PrintWriter}

/**
  * Created by jmarzin-cp on 28/12/2016.
  */
object Fichier {
  var pw:PrintWriter = null//new PrintWriter(new FileOutputStream(new File("C:\\tiers\\titres.csv"),true));
  def init(ajout:Boolean) : Unit = {
    pw = new PrintWriter(new FileOutputStream(new File("C:\\tiers\\titres.csv"),ajout))
    if (!ajout) {
      pw.println(
        "CCode|TDateEmission|TIdentifiant|TResteARecouvrerPrincipal|TResteARecouvrerFrais|" +
          "DAdresse|DAdressePrincipale|DCategorie|DCivilite|DCodeActualite|DComplement|" +
          "DComplementAdresse|DCompteParDefaut|DCpVille|DDateConsolidation|DDateDeNaissance|" +
          "DDateMiseAJour|DDateMiseAJourAdresse|DDateStatutDgfip|DEmel|DIdendifiant|DImmatriculation|" +
          "DLocalite|DNatureJuridique|DNfp|DNomRs|DNpai|DOrigineMiseAJour|DPays|DPrenom|" +
          "DResidentEnFrance|DStatutDgfip|DTelDomicile|DTelPortable|DConsolidé|DListeConsolidation\r\n"
      )
      pw.flush
    }
  }
  def sauve(titre: Titre): Unit = {
    pw.println(
        titre.colloc.code + "|" + titre.dateEmission + "|" +
        titre.code + "|" + titre.resteARecouvrerPrincipal + "|" +
        titre.resteARecouvrerFrais + "|" + titre.debiteur.adresse.numeroEtVoie + "|" +
        titre.debiteur.adresse.adressePrincipale + "|" + titre.debiteur.categorie + "|" +
        titre.debiteur.civilite + "|" + titre.debiteur.codeActualite + "|" +
        titre.debiteur.complement + "|" + titre.debiteur.adresse.complementAdresse + "|" +
        titre.debiteur.compteParDefaut + "|" + titre.debiteur.adresse.cpVille + "|" +
        titre.debiteur.dateConsolidation + "|" + titre.debiteur.dateDeNaissance + "|" +
        titre.debiteur.dateMiseAJour + "|" + titre.debiteur.adresse.dateMiseAJour + "|" +
        titre.debiteur.dateStatutDgfip + "|" + titre.debiteur.emel + "|" +
        titre.debiteur.identifiant + "|" + titre.debiteur.immatriculation + "|" +
        titre.debiteur.adresse.localite + "|" + titre.debiteur.natureJuridique + "|" +
        titre.debiteur.nfp + "|" + titre.debiteur.nomRs + "|" +
        titre.debiteur.adresse.npai + "|" + titre.debiteur.adresse.origineMiseAJour + "|" +
        titre.debiteur.adresse.pays + "|" + titre.debiteur.prenom + "|" +
        titre.debiteur.residentEnFrance + "|" + titre.debiteur.statutDgfip + "|" +
        titre.debiteur.telDomicile + "|" + titre.debiteur.telPortable + "|" +
        titre.debiteur.consolide + "|" + titre.debiteur.listeConsolidation + "\r\n"
    )
    pw.flush
  }
  def close: Unit = {
    pw.close
  }
}
