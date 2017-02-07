package main.scala.com.jmarzin.tiers


/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object Helios extends HeliosFirefox {

  def parcours(typePiece: Symbol) : Unit = {
    init(typePiece)
    var pieceEnCours: Piece = new Piece
    pieceEnCours.colloc = new Colloc
    pieceEnCours.dernierePieceDeLaColloc = true
    pieceEnCours.colloc.derniereColloc = true
    do {
      if (pieceEnCours.dernierePieceDeLaColloc) {
        SessionEnCours.memoriseColloc(typePiece, pieceEnCours.colloc)
        pieceEnCours = collocSuivante(pieceEnCours)
        if (SessionEnCours.collocDejaTraitee(typePiece, pieceEnCours.colloc)) {
          pieceEnCours.colloc.pasDePiece = true
        } else {
          SessionEnCours.memorisePiece(typePiece, pieceEnCours)
          pieceEnCours = premierePieceColloc(typePiece, pieceEnCours)
        }
        if (pieceEnCours.colloc.pasDePiece) {
          pieceEnCours.dernierePieceDeLaColloc = true
        } else {
          pieceEnCours.dernierePieceDeLaColloc = false
        }
      }
      if (!pieceEnCours.colloc.pasDePiece) {
        pieceEnCours = setDernierePiece(typePiece, pieceEnCours)
        if (!SessionEnCours.pieceDejaTraitee(typePiece, pieceEnCours) && !(pieceEnCours.etat == "ANNUL")) {
          pieceEnCours = litPiece(typePiece, pieceEnCours)
          SessionEnCours.memorisePiece(typePiece, pieceEnCours)
          Base.sauve(pieceEnCours)
          Fichier.sauve(pieceEnCours)
          AppTiers.nbPiecesTraites += 1
        }
        if (!pieceEnCours.dernierePieceDeLaColloc) {
          pieceEnCours = pieceSuivanteColloc(typePiece,pieceEnCours)
        }
      }
    } while ((!pieceEnCours.dernierePieceDeLaColloc || !pieceEnCours.colloc.derniereColloc) && !AppTiers.stop)
    if (pieceEnCours.dernierePieceDeLaColloc && pieceEnCours.colloc.derniereColloc) {
      AppTiers.finNormale = true
      SessionEnCours.memoriseColloc(typePiece, pieceEnCours.colloc)
      Base.sauveSession()
    }
    pageAccueil()
  }
}
