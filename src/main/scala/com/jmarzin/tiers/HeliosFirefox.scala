package main.scala.com.jmarzin.tiers

import java.util.{Calendar, Date}
import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane.showMessageDialog
import javax.swing.{JOptionPane, JPasswordField, JTextField}
import org.openqa.selenium.{By, WebDriverException, WebElement}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import unfiltered.response.Date

/**
  * Created by jmarzin-cp on 28/12/2016.
  */
trait HeliosFirefox extends FirefoxDriver{

  //var nbAppelsCollocSuivante = 0
  //var nbAppelsPremierePieceColloc = 0
  //var nbAppelsTitreSuivantColloc = 0
  //var listeTest: List[(Int ,List[Int])] = List()

  def init : Unit = {
    manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
    get("http://ulysse.dgfip")
    Thread.sleep(500)
    var utilisateur = ""
    var password = ""
    if (!AppTiers.developpement) {
      var nom = new JTextField()
      var okCxl = JOptionPane.showConfirmDialog(null, nom, "Votre nom d'utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (okCxl == JOptionPane.OK_OPTION) {
        utilisateur = nom.getText
      } else {
        throw new IllegalArgumentException
      }
      var pf = new JPasswordField()
      okCxl = JOptionPane.showConfirmDialog(null, pf, "Votre mot de passe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (okCxl == JOptionPane.OK_OPTION) {
        password = new String(pf.getPassword)
      } else {
        throw new IllegalArgumentException
      }
    } else {
      utilisateur = "jacques.marzin"
      password = ""
    }
    get("http://portailapplicatif.appli.impots")
    val handles = getWindowHandles.toArray
    switchTo.window(handles(handles.length - 1).toString)
    val identifiant = findElements(By.id("identifiant"))
    if (!identifiant.isEmpty()) {
      identifiant.get(0).sendKeys(utilisateur)
      val pwd = findElement(By.id("secret_tmp"))
      pwd.sendKeys(password)
      findElement(By.className("valid")).click()
    }
    findElement(By.className("nom_appli"))
    if (!getPageSource.contains("http://helios.appli.impots")) {
      throw new WebDriverException()
    }
    get("http://helios.appli.impots")
  }

  def init(typePiece: Symbol) : Unit = {
    switchTo().defaultContent
    val myDynamicElement = new WebDriverWait(this, 10)
        .until(ExpectedConditions.presenceOfElementLocated(By.name("frmWork")))
    findElement(By.tagName("FRAME"))
    switchTo().frame("frmMenu")
    findElementByTagName("LINK")
    if(typePiece == 'titre)
      executeScript("m.go('172')")
    else
      executeScript("m.go('180')")
  }

  def collocSuivante(piece : Piece) :Piece = {
    //nbAppelsCollocSuivante += 1
    if (piece.colloc.code == "") {
      piece.colloc.rangDansLaPage = 0
      piece.colloc.rangPage = 0
    } else {
      piece.colloc.rangDansLaPage += 1
    }
    piece.code = ""

    if (getWindowHandles.size == 1) {
      switchTo().defaultContent
      switchTo().frame("frmWork")
      findElementByLinkText("valeurs").click
      while (getWindowHandles.size != 2) {
        Thread.sleep(100)
      }
      val handles = getWindowHandles.toArray
      switchTo.window((handles(handles.length - 1).toString))
      for( a <- 1 to piece.colloc.rangPage){
        findElementByXPath("//input[@value='>>']").click
        findElementByXPath("//tr[@idx]//a")
      }
    }
    val colbuds = findElementsByXPath("//tr[@idx]//a")
    piece.colloc.code = colbuds.get(piece.colloc.rangDansLaPage).getText
    AppTiers.collocEnCours = piece.colloc.code
    AppTiers.rangPieceCollocEnCours = 0
    if (piece.colloc.rangDansLaPage == colbuds.size - 1) {
      if (getPageSource.contains("value=\"&gt;&gt;\"")) {
        findElementByXPath("//input[@value='>>']").click
        findElementByXPath("//tr[@idx]//a")
        piece.colloc.rangPage += 1
        piece.colloc.rangDansLaPage = -1
      } else {
        piece.colloc.derniereColloc = true
      }
    } else {
      piece.colloc.derniereColloc = false
    }
    if (piece.colloc.derniereColloc) {
      print("\r\nDernière colloc " + piece.colloc.code)
    } else {
      print("\r\ncolloc " + piece.colloc.code)
    }
    piece
  }

  def premierePieceColloc(typePiece: Symbol, piece : Piece) :Piece = {
    //nbAppelsPremierePieceColloc += 1

    findElementById("annuler").click
    while (getWindowHandles.size == 2) {
      Thread.sleep(100)
    }
    val handles = getWindowHandles.toArray
    switchTo.window((handles(handles.length - 1).toString))
    switchTo().frame("frmWork")
    findElementByClassName("inputreset").click
    findElementByClassName("inputreset")
    findElementById("_1").clear
    findElementById("_1").sendKeys(piece.colloc.code)
    if(typePiece == 'titre) {
      findElementById("_3").clear
      val now = Calendar.getInstance()
      val dateFormatee = now.get(Calendar.DAY_OF_MONTH).toString +
        "/" + (now.get(Calendar.MONTH) + 1).toString +
        "/" + now.get(Calendar.YEAR).toString
      findElementById("_6").sendKeys(dateFormatee)
      val elementCache = findElementByName("RCE_CT_F07_CriteresRecherche_CAF.#piecesNonSoldees")
      if (elementCache.getAttribute("value") == "false") {
        findElementByName("RCE_CT_F07_CriteresRecherche_CAF.#piecesNonSoldees-checkbox").click
      }
      findElementByClassName("inputvalider").click
      findElementByClassName("inputvalider")
      if (getPageSource().contains("total  0 titre")) {
        piece.code = ""
        piece.rangDansLaPage = -1
        piece.colloc.pasDePiece = true
      } else {
        val res = findElementsByClassName("soustitre2")
        for (i <- 0 to res.size - 1) {
          var texte = res.get(i).getText
          if (texte.contains("Liste des titres (total")) {
            texte = texte.replaceFirst("L.*\\(total", "")
            texte = texte.replaceFirst("titres\\)", "").trim
            AppTiers.rangPieceCollocEnCours = 0
            if (texte.matches("\\d*")) {
              AppTiers.nbPiecesCollocEnCours = texte.toInt
              AppTiers.collocEnCours = piece.colloc.code
            }
            else {
              AppTiers.nbPiecesCollocEnCours = 0
            }
          }
        }
        piece.rangDansLaPage = 0
        litCodePiece(typePiece, piece)
        piece.colloc.pasDePiece = false
      }
    } else {
      findElementByClassName("box").click
      findElementByClassName("inputvalider").click
      findElementByClassName("inputvalider")
      if (getPageSource().contains("total 0 article")) {
        piece.code = ""
        piece.rangDansLaPage = -1
        piece.colloc.pasDePiece = true
      } else {
        val res = findElementsByClassName("soustitre2")
        for (i <- 0 to res.size - 1) {
          var texte = res.get(i).getText
          if (texte.contains("Liste (total")) {
            texte = texte.replaceFirst("L.*\\(total", "")
            texte = texte.replaceFirst("articles\\)", "").trim
            AppTiers.rangPieceCollocEnCours = 0
            if (texte.matches("\\d*")) {
              AppTiers.nbPiecesCollocEnCours = texte.toInt
              AppTiers.collocEnCours = piece.colloc.code
            }
            else {
              AppTiers.nbPiecesCollocEnCours = 0
            }
          }
        }
        piece.rangDansLaPage = 0
        litCodePiece(typePiece, piece)
        piece.colloc.pasDePiece = false
      }
    }
    piece
  }

  def setDernierTitre(typePiece: Symbol, piece: Piece) : Piece = {

    findElementByClassName("inputreset")
    if (typePiece == 'titre) {
      val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
      piece.dernierePieceDeLaColloc = false
      if (piece.rangDansLaPage == boutonsDetail.size - 1 && !getPageSource().contains("value=\"&gt;&gt;\"")) {
        piece.dernierePieceDeLaColloc = true
      }
    } else {
      val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
      piece.dernierePieceDeLaColloc = false
      if (piece.rangDansLaPage == boutonsDetail.size - 1 && !getPageSource().contains("value=\"&gt;&gt;\"")) {
        piece.dernierePieceDeLaColloc = true
      }
    }
    piece
  }

  def litCodePiece(typePiece: Symbol, piece : Piece) : Unit = {

    if (typePiece == 'titre) {
      val codeTitre = findElementsByXPath("//tr[@idx]/td[2]").get(piece.rangDansLaPage).getText
      manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS)
      try {
        var anneeBordereau = findElementByXPath("//TR[TD='\n\t\t\t\t\t\t\t\t\t" +
          codeTitre.split(" - ")(0) +
          "\n\t\t\t\t\t\t\t\t\t-\n\t\t\t\t\t\t\t\t\t" +
          codeTitre.split(" - ")(1) +
          "\n\t\t\t\t\t\t\t\t']/preceding-sibling::TR[TH][1]").getText
        anneeBordereau = anneeBordereau.replaceFirst("N°Bordereau : ", "").replaceFirst("Exercice ", "").replaceAll(" ", "")
        piece.annee = anneeBordereau.split("-")(1)
        piece.bordereau = anneeBordereau.split("-")(0)
      } catch {
        case e: org.openqa.selenium.NoSuchElementException =>
      }
      manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
      piece.code = ("TI" + piece.annee + "-" + piece.bordereau + "-" + codeTitre).replaceAll(" ", "")
    } else {
      val codeArticle = findElementsByXPath("//tr[@idx]/td[@class='tdliste '][1]").get(piece.rangDansLaPage).getText
      manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS)
      try {
        var anneeRole = findElementByXPath("//TR[TD='"+codeArticle+"']/preceding-sibling::TR[TH][1]").getText
        anneeRole = anneeRole.replaceFirst("Référence Rôle : ", "").replaceFirst("Exercice", "").trim
        piece.annee = anneeRole.split(" ")(2)
        piece.bordereau = anneeRole.split(" ")(0)
      } catch {
        case e: org.openqa.selenium.NoSuchElementException =>
      }
      manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
      piece.code = ("AR"+ piece.annee + "-" + piece.bordereau + "-" + codeArticle).replaceAll(" ", "")
    }
  }

  def litDebiteur(piece : Piece): Unit = {
    findElementByXPath("//input[@value='Retour']")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail']")
    var zone = findElementByXPath("//input[@value='Détail']/parent::*/preceding-sibling::td[1]")
    piece.debiteur.identifiant = zone.getText.replaceAll("'","").trim
    boutonsDetail.get(1).click
    findElementByXPath("//input[@value='Retour']")
    zone = findElementByXPath("//td[label='Catégorie']/following-sibling::td[1]")
    piece.debiteur.categorie = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Nature juridique']/following-sibling::td[1]")
    piece.debiteur.natureJuridique = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Immatriculation']/following-sibling::td[1]")
    piece.debiteur.immatriculation = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Statut DGFIP']/following-sibling::td[1]")
    piece.debiteur.statutDgfip = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Civilité']/following-sibling::td[1]")
    piece.debiteur.civilite = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Nom/RS']/following-sibling::td[1]")
    piece.debiteur.nomRs = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Prénom']/following-sibling::td[1]")
    piece.debiteur.prenom = zone.getText.replaceAll("'"," ").trim
    if (piece.debiteur.prenom.equals(".")) {
      piece.debiteur.prenom = ""
    }
    zone = findElementByXPath("//td[label='Complément']/following-sibling::td[1]")
    piece.debiteur.complement = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Résident en France']/following-sibling::td[1]")
    piece.debiteur.residentEnFrance = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date de naissance']/following-sibling::td[1]")
    piece.debiteur.dateDeNaissance = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Tél. Domicile']/following-sibling::td[1]")
    piece.debiteur.telDomicile = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Tél. Portable']/following-sibling::td[1]")
    piece.debiteur.telPortable = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='E-mail']/following-sibling::td[1]")
    piece.debiteur.emel = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td='Référence du compte par défaut']/following-sibling::tr[1]/td")
    piece.debiteur.compteParDefaut = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Code Actualité']/following-sibling::td[1]")
    piece.debiteur.codeActualite = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date de  mise à jour']/following-sibling::td[1]")
    piece.debiteur.dateMiseAJour = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='NFP']/following-sibling::td[1]")
    piece.debiteur.nfp = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date Statut DGFIP']/following-sibling::td[1]")
    piece.debiteur.dateStatutDgfip = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Adresse Principale']")
    piece.debiteur.adresse.adressePrincipale = zone.getText.replaceFirst("Adresse Principale","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='NPAI']")
    piece.debiteur.adresse.npai = zone.getText.replaceFirst("NPAI","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Date mise à jour']")
    piece.debiteur.adresse.dateMiseAJour = zone.getText.replaceFirst("Date mise à jour","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Orig. mise à jour']")
    piece.debiteur.adresse.origineMiseAJour = zone.getText.replaceFirst("Orig. mise à jour","").replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Cplt. Adresse (Bât, Rés...)']/following-sibling::td[1]")
    piece.debiteur.adresse.complementAdresse = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Adresse (N° et voie)']/following-sibling::td[1]")
    piece.debiteur.adresse.numeroEtVoie = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Localité (Lieu-dit, BP...)']/following-sibling::td[1]")
    piece.debiteur.adresse.localite = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='CP-Ville']/following-sibling::td[1]")
    piece.debiteur.adresse.cpVille = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//td[label='Pays']/following-sibling::td[1]")
    piece.debiteur.adresse.pays = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td='Employeur par défaut']/following-sibling::tr[1]/td[2]")
    piece.debiteur.nomRsEmployeur = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td='Employeur par défaut']/following-sibling::tr[2]/td[2]")
    piece.debiteur.cpVilleEmployeur = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td=\"Caisse d'Allocations familiales \"]/following-sibling::tr[1]/td[2]")
    piece.debiteur.nomRsCaf = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td=\"Caisse d'Allocations familiales \"]/following-sibling::tr[2]/td[2]")
    piece.debiteur.cpVilleCaf = zone.getText.replaceAll("'"," ").trim
    zone = findElementByXPath("//tr[td=\"Caisse d'Allocations familiales \"]/following-sibling::tr[3]/td[2]")
    piece.debiteur.numAllocataireCaf = zone.getText.replaceAll("'"," ").trim
    if (getPageSource().contains("title=\"tiers consolidé\"")) {
      zone = findElementByCssSelector("nobr")
      piece.debiteur.dateConsolidation = zone.getText.replaceFirst("Date de consolidation :","").replaceAll("'"," ").trim
      piece.debiteur.consolide = "Oui"
      findElementByLinkText("Tiers Consolidé").click
      var codesTiersConsolides = findElementsByXPath("//td[@title='nom/rs complet']/preceding-sibling::td[1]")
      val retour = findElementByXPath("//input[@value='Retour']")
      var liste = ""
      for (a <- 0 until codesTiersConsolides.size) {
        liste += codesTiersConsolides.get(a).getText + ";"
      }
      piece.debiteur.listeConsolidation = liste.substring(0, liste.length() - 1)
      var tableau = piece.debiteur.listeConsolidation.split(";").sorted.toList
      var nouvelleListe = ""
      tableau.foreach(nouvelleListe += _ + ";")
      piece.debiteur.listeConsolidation = nouvelleListe.substring(0, nouvelleListe.length()-1)
      retour.click
    } else {
      piece.debiteur.dateConsolidation = ""
      piece.debiteur.consolide = "Non"
      piece.debiteur.listeConsolidation = piece.debiteur.identifiant
    }
    findElementByXPath("//input[@value='Retour']").click
    findElementByXPath("//input[@value='Retour']").click
  }

  def litPiece(typePiece: Symbol, piece : Piece) : Piece = {

    findElementByClassName("inputreset")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    boutonsDetail.get(piece.rangDansLaPage).click
    var page = getPageSource
    if(typePiece == 'titre) {
      //Bordereau Titres
      var zone = findElementByXPath("//table[@class='tablezonesaisie']/descendant::tr[10]/td[2]")
      piece.dateEmission = zone.getText.replaceAll("'", " ").trim
      zone = findElementByXPath("//td[@width='135px']")
      piece.resteARecouvrerPrincipal = zone.getText.replaceAll("[.,€]", "").toInt
      zone = findElementByXPath("//td[@width='120px']")
      piece.resteARecouvrerFrais = zone.getText.replaceAll("[.,€]", "").toInt
      zone = findElementByXPath("//td[@width='186px']/following-sibling::td[1]")
      piece.datePrescription = zone.getText
    } else {
      //Rôle Article
      var zone = findElementByXPath("//td[@class='soustitre2'][1]")
      piece.dateEmission = zone.getText.replaceAll(".*mis le  ", "").trim
      zone = findElementByXPath("//td[@width='185px']")
      piece.resteARecouvrerPrincipal = zone.getText.replaceAll("[.,€]", "").toInt
      zone = findElementByXPath("//td[@width='185px']/following-sibling::td[2]")
      piece.resteARecouvrerFrais = zone.getText.replaceAll("[.,€]", "").toInt
      zone = findElementByXPath("//td[@width='169px']/following-sibling::td[1]")
      piece.datePrescription = zone.getText
    }
    if (getPageSource().contains("Tiers débiteur")) {
      findElementByLinkText("Tiers débiteur").click
      litDebiteur(piece)
      if(typePiece == 'titre) findElementByXPath("//input[@value='Retour']").click
    } else {
      //TODO traiter le cas où le lien Tiers débiteur n'existe pas
      findElementByXPath("//input[@value='Retour']").click
    }
    findElementByXPath("//input[@value='Détail'][@name='OID']")
    print("\r\nTitre "+ piece.code)
    if (piece.dernierePieceDeLaColloc) {
      print(" dernier")
    }
    piece
  }

  def titreSuivantColloc(typePiece: Symbol, piece : Piece) : Piece = {
    //nbAppelsTitreSuivantColloc += 1
    piece.rangDansLaPage += 1

    findElementByXPath("//input[@value='Détail'][@name='OID']")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    if (piece.rangDansLaPage > boutonsDetail.size - 1) {
      if (getPageSource.contains("value=\"&gt;&gt;\"")) {
        findElementByXPath("//input[@value='>>']").click
        findElementByXPath("//input[@value='Détail']")
        piece.rangDansLaPage = 0
      }
    }
    litCodePiece(typePiece, piece)
    AppTiers.rangPieceCollocEnCours += 1
    piece
  }

  def pageAccueil : Unit = {
    if(getWindowHandles.size == 2){
      findElementById("annuler").click
      while (getWindowHandles.size == 2) {
        Thread.sleep(100)
      }
      val handles = getWindowHandles.toArray
      switchTo.window(handles(handles.length - 1).toString)
      switchTo().frame("frmWork")
    }
    findElementByXPath("//input[@value='Quitter']").click
  }
}
