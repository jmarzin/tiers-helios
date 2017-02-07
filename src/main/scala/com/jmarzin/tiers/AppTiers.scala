package main.scala.com.jmarzin.tiers

import java.awt.{Color, Point}
import java.io.File
import javax.swing.{JOptionPane, JTextField}

import scala.swing.BorderPanel.Position._
import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, BoxPanel, Button, ButtonGroup, Dimension, Label, MainFrame, Orientation, ProgressBar, RadioButton, SimpleSwingApplication}

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object AppTiers extends SimpleSwingApplication {

  var poste = ""
  var threadHelios : Thread = _
  var threadDoublons : Thread = _
  val developpement = false
  var finNormale = false
  var stop = false
  var plante = false
  var nbPiecesTraites = 0
  var nbDebiteursLus: Int = -1
  var nbDebiteurDistances: Int = -1
  var nbDebiteurDoublons: Int = -1
  var nbDoublonsEcrits: Int = -1
  var nbDoublonsAEcrire: Int = -1
  var nbPiecesCollocEnCours = 0
  var rangPieceCollocEnCours = 0
  var collocEnCours = ""
  var typePiece = 'titre

  def afficheAvancementHelios(x: Any): String = x match {
    case 0 => " "
    case 1 => "1 pièce traitée"
    case y: Int => y.toString + " " + "pièces traitées"
  }

  def afficheAvancementDoublons : (String,Integer) = {
    if (nbDoublonsEcrits >= 0) {
      ("" + nbDoublonsEcrits + "/" + nbDoublonsAEcrire + " propositions de doublons sauvegardées",
        nbDoublonsEcrits*100/nbDoublonsAEcrire)
    } else if (nbDebiteurDoublons >= 0) {
      ("propositions construites pour " + nbDebiteurDoublons + "/" + nbDebiteursLus + " débiteurs",
        nbDebiteurDoublons*100/nbDebiteursLus)
    } else if (nbDebiteurDistances >= 0) {
      ("distances calculées pour " + nbDebiteurDistances + "/" + nbDebiteursLus + " débiteurs",
        nbDebiteurDistances*100/nbDebiteursLus)
    } else if (nbDebiteursLus >= 0) {
      ("" + nbDebiteursLus + " débiteurs lus",0)
    } else {
      (" ",0)
    }
  }

  val champPoste = new JTextField()
  champPoste.hasFocus()
  val okCxl: Int = JOptionPane.showConfirmDialog(null, champPoste, "Nom du poste", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
  if (okCxl == JOptionPane.OK_OPTION) {
    poste = champPoste.getText.replaceAll(" ","")
  } else {
    throw new IllegalArgumentException
  }

  def top = new MainFrame {

    private def prevenirUneFois() = {
      if (bouton.text != "Quitter") java.awt.Toolkit.getDefaultToolkit.beep()
    }


    Base.init("C:/tiers/restes"+"_"+poste)

    title = "Récupération des RAR"
    location = new Point(1000,0)
    preferredSize = new Dimension(320,172)

    val mutex = new ButtonGroup
    val reprendre = new RadioButton("Reprendre la session précédente")
    val recommencer = new RadioButton("Tout récupérer")
    val chercherDoublons = new RadioButton("Chercher les doublons")
    val radios = List(reprendre, recommencer,chercherDoublons)
    mutex.buttons ++= radios
    if (SessionEnCours.?) {
      if (SessionEnCours.titresTraites == List() && SessionEnCours.articlesTraites == List()) {
        recommencer.enabled = false
        mutex.select(chercherDoublons)
      } else {
        mutex.select(reprendre)
      }
    } else {
      mutex.select(recommencer)
      reprendre.enabled = false
      chercherDoublons.enabled = false
    }
    val options = new BoxPanel(Orientation.Vertical) {
      contents ++= radios
    }

    val bouton = new Button {
      text = "Lancer"
    }

    val message = new Label{
      text = " "
      foreground = Color.red
    }

    val progression = new ProgressBar {
      min = 0
      max = 100
      value = 0
      label = " "
      labelPainted = true
    }

    val bas = new BoxPanel(Orientation.Vertical) {
      contents ++= List(message,progression)
    }

    contents = new BorderPanel {
      layout(options) = North
      layout(bouton) = Center
      layout(bas) = South
    }

    listenTo(bouton)
    listenTo()

    Timer(1000) {
      if (threadHelios == null) {
        if (!finNormale && !stop) {
          val pair = afficheAvancementDoublons
          message.text = pair._1
          progression.value = pair._2
        } else {
          var libelle = ""
          if (finNormale) {
            progression.value = 0
            libelle = "terminée"
          } else {
            stop = false
            threadDoublons.interrupt()
            libelle = "arrêtée"
          }
          prevenirUneFois()
          message.text = "Recherche des doublons " + libelle
          bouton.text = "Quitter"
        }
      } else if (threadHelios.isAlive) {
        if(typePiece == 'titre)
          progression.label = SessionEnCours.collocEnCoursTitres
        else
          progression.label = SessionEnCours.collocEnCoursArticles
        progression.labelPainted = true
        if (nbPiecesCollocEnCours == 0) {
          progression.value = 0
        } else {
          progression.value = rangPieceCollocEnCours*100/nbPiecesCollocEnCours
        }
        message.text = afficheAvancementHelios(nbPiecesTraites)
      } else {
        var libelle = ""
        if (finNormale) libelle = "terminé"
        else if (stop) libelle = "arrêté"
        else if(plante) libelle = "planté"
        prevenirUneFois()
        message.text = "Hélios " + libelle + " après " + afficheAvancementHelios(nbPiecesTraites)
        Helios.close()
        bouton.text = "Quitter"
      }
    }

    reactions += {
      case ButtonClicked(component) if component == bouton =>
        if (bouton.text.equals("Lancer")) {
          bouton.text = "Arrêter"
          reprendre.enabled = false
          recommencer.enabled = false
          chercherDoublons.enabled = false
          if(chercherDoublons.selected) {
            threadDoublons = new Thread {
              override def run(): Unit = {
                Doublons.cherche()
              }
            }
            threadDoublons.start()
          } else {
            if (recommencer.selected) {
              Base.vide()
              Fichier.init("c:\\tiers\\titres_" + poste + ".csv", ajout = false)
            } else {
              Fichier.init("c:\\tiers\\titres_" + poste + ".csv", ajout =true)
            }
            System.setProperty("webdriver.firefox.bin", "D:\\firefox31\\firefox.exe")
            Helios.init()
            threadHelios = new Thread {
              override def run() {
                //try {
                  typePiece = 'article
                  Helios.parcours(typePiece)
                  typePiece = 'titre
                  Helios.parcours(typePiece)
                  Helios.close()
                //} catch {
                //  case e: Exception =>
                //    plante = true
                //    finNormale = false
                //    println("Erreur " + e + e.getStackTrace.toString)
                //}
              }
            }
            threadHelios.start()
          }
        } else if (bouton.text.equals("Arrêter")) {
          stop = true
        } else if (bouton.text.equals("Quitter")) {
          Base.connexion.close()
          sys.exit(0)
        }
    }
  }
}