package main.scala.com.jmarzin.tiers

import java.io.{File, FileOutputStream, PrintWriter}

/**
  * Created by jmarzin-cp on 28/12/2016.
  */
object Fichier {
  var pw:PrintWriter = _
  def init(ajout:Boolean) : Unit = {
    pw = new PrintWriter(new FileOutputStream(new File("C:\\tiers\\titres.csv"),ajout))
    if (!ajout) {
      pw.println(
        "CCode|TDateEmission|TIdentifiant|TResteARecouvrerPrincipal|TResteARecouvrerFrais|" +
          "DAdresse|DAdressePrincipale|DCategorie|DCivilite|DCodeActualite|DComplement|" +
          "DComplementAdresse|DCompteParDefaut|DCpVille|DDateConsolidation|DDateDeNaissance|" +
          "DDateMiseAJour|DDateMiseAJourAdresse|DDateStatutDgfip|DEmel|DIdendifiant|DImmatriculation|" +
          "DLocalite|DNatureJuridique|DNfp|DNomRs|DNpai|DOrigineMiseAJour|DPays|DPrenom|" +
          "DResidentEnFrance|DStatutDgfip|DTelDomicile|DTelPortable|DConsolidé|DListeConsolidation|" +
          "DnomRsEmployeur|DcpVilleEmployeur|DnomRsCaf|DcpVilleCaf|DnumAllocataireCaf\r\n"
      )
      pw.flush()
    }
  }
  def sauve(piece: Piece): Unit = {
    pw.println(
      ("%s" + "|%s"*40 +"\r\n").
          format(piece.colloc.code, piece.dateEmission, piece.code,
            piece.resteARecouvrerPrincipal, piece.resteARecouvrerFrais,
            piece.debiteur.adresse.numeroEtVoie,
            piece.debiteur.adresse.adressePrincipale, piece.debiteur.categorie,
            piece.debiteur.civilite, piece.debiteur.codeActualite,
            piece.debiteur.complement, piece.debiteur.adresse.complementAdresse,
            piece.debiteur.compteParDefaut, piece.debiteur.adresse.cpVille,
            piece.debiteur.dateConsolidation, piece.debiteur.dateDeNaissance,
            piece.debiteur.dateMiseAJour, piece.debiteur.adresse.dateMiseAJour,
            piece.debiteur.dateStatutDgfip, piece.debiteur.emel,
            piece.debiteur.identifiant, piece.debiteur.immatriculation,
            piece.debiteur.adresse.localite, piece.debiteur.natureJuridique,
            piece.debiteur.nfp, piece.debiteur.nomRs, piece.debiteur.adresse.npai,
            piece.debiteur.adresse.origineMiseAJour, piece.debiteur.adresse.pays,
            piece.debiteur.prenom, piece.debiteur.residentEnFrance,
            piece.debiteur.statutDgfip, piece.debiteur.telDomicile,
            piece.debiteur.telPortable, piece.debiteur.consolide,
            piece.debiteur.listeConsolidation, piece.debiteur.nomRsEmployeur,
            piece.debiteur.cpVilleEmployeur, piece.debiteur.nomRsCaf,
            piece.debiteur.cpVilleCaf, piece.debiteur.numAllocataireCaf)
    )
    pw.flush()
  }
  def close(): Unit = {
    pw.close()
  }
}
