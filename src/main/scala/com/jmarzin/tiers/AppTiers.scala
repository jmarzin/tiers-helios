package main.scala.com.jmarzin.tiers

import java.awt.{Color, Point}

import specs2.text
import sun.awt.resources.awt

import scala.swing.BorderPanel.Position._
import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, BoxPanel, Button, ButtonGroup, Dimension, Label, MainFrame, Orientation, ProgressBar, RadioButton, SimpleSwingApplication}
import scalaz.Digit.{_1, _2}

/**
  * Created by jmarzin-cp on 24/12/2016.
  */
object AppTiers extends SimpleSwingApplication {

  var threadHelios : Thread = null
  var threadDoublons : Thread = null
  var developpement = false
  var finNormale = false
  var stop = false
  var plante = false
  var nbTitresTraites = 0
  var nbDebiteursLus = -1
  var nbDebiteurDistances = -1
  var nbDebiteurDoublons = -1
  var nbDoublonsEcrits = -1
  var nbDoublonsAEcrire = -1
  var nbTitresCollocEnCours = 0
  var rangTitreCollocEnCours = 0
  var collocEnCours = ""

  def afficheAvancementHelios(x: Any): String = x match {
    case 0 => " "
    case 1 => "1 titre traité"
    case y: Int => y.toString + " titres traités"
  }

  def afficheAvancementDoublons : (String,Integer) = {
    if (nbDoublonsEcrits >= 0) {
      return ("" + nbDoublonsEcrits + "/" + nbDoublonsAEcrire + " propositions de doublons sauvegardées",
        nbDoublonsEcrits*100/nbDoublonsAEcrire)
    } else if (nbDebiteurDoublons >= 0) {
      return ("propositions construites pour " + nbDebiteurDoublons + "/" + nbDebiteursLus + " débiteurs",
        nbDebiteurDoublons*100/nbDebiteursLus)
    } else if (nbDebiteurDistances >= 0) {
      return ("distances calculées pour " + nbDebiteurDistances + "/" + nbDebiteursLus + " débiteurs",
        nbDebiteurDistances*100/nbDebiteursLus)
    } else if (nbDebiteursLus >= 0) {
      return ("" + nbDebiteursLus + " débiteurs lus",0)
    } else {
      return (" ",0)
    }
  }

  def top = new MainFrame {

    private def prevenirUneFois = {
      if (bouton.text != "Quitter") java.awt.Toolkit.getDefaultToolkit().beep
    }

    Base.init("C:\\tiers\\restes")

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
      if (SessionEnCours.titresTraites == List()) {
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
        if (finNormale) {
          progression.value = 0
          prevenirUneFois
          message.text = "Recherche des doublons terminée"
          bouton.text = "Quitter"
        } else if (stop) {
          threadDoublons.interrupt
          prevenirUneFois
          message.text = "Recherche des doublons arrêtée"
          bouton.text = "Quitter"
          stop = false
        } else {
          val pair = afficheAvancementDoublons
          message.text = pair._1
          progression.value = pair._2
        }
      } else if (threadHelios.isAlive) {
        progression.label = SessionEnCours.collocEnCours
        progression.labelPainted = true
        if (nbTitresCollocEnCours == 0) {
          progression.value = 0
        } else {
          progression.value = rangTitreCollocEnCours*100/nbTitresCollocEnCours
        }
        message.text = afficheAvancementHelios(nbTitresTraites)
      } else if (finNormale) {
        prevenirUneFois
        message.text = "Hélios terminé après " + afficheAvancementHelios(nbTitresTraites)
        bouton.text = "Quitter"
      } else if (stop) {
        prevenirUneFois
        message.text = "Hélios arrêté après " + afficheAvancementHelios(nbTitresTraites)
        bouton.text = "Quitter"
      } else if (plante) {
        prevenirUneFois
        message.text = "Hélios planté après "+ afficheAvancementHelios(nbTitresTraites)
        Helios.close
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
              override def run: Unit = {
                Doublons.cherche
              }
            }
            threadDoublons.start
          } else {
            if (recommencer.selected) {
              Base.vide
              Fichier.init(false)
            } else {
              Fichier.init(true)
            }
            Helios.init
            threadHelios = new Thread {
              override def run {
//                try {
                  Helios.parcours
//                } catch {
//                  case e: Exception => {
//                    plante = true
//                    finNormale = false
//                    println("Erreur " + e + e.getStackTrace.toString)
//                  }
//                }
              }
            }
            threadHelios.start
          }
        } else if (bouton.text.equals("Arrêter")) {
          stop = true
        } else if (bouton.text.equals("Quitter")) {
          Base.ferme
          sys.exit(0)
        }
    }
  }
}