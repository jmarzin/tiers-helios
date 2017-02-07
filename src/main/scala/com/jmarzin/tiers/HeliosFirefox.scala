package main.scala.com.jmarzin.tiers

import java.util._
import java.util.concurrent.TimeUnit
import javax.swing.{JOptionPane, JPasswordField, JTextField}

import org.openqa.selenium.{By, TimeoutException, WebDriverException, WebElement}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}

/**
  * Created by jmarzin-cp on 28/12/2016.
  */
trait HeliosFirefox extends FirefoxDriver{

  //var nbAppelsCollocSuivante = 0
  //var nbAppelsPremierePieceColloc = 0
  //var nbAppelsTitreSuivantColloc = 0
  //var listeTest: List[(Int ,List[Int])] = List()

  def attends(): Unit = {
    for(_ <- 1 to 100){
      if(executeScript("return document.readyState") == "complete")return
      Thread.sleep(100)
    }
    throw new TimeoutException("trop d'attente", new Throwable)
  }

  implicit class clickEtAttend(webelement: WebElement) {
    def clickw() : Unit = {
      webelement.click()
      attends()
    }
  }

  def init() : Unit = {
    manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
    get("http://ulysse.dgfip")
    attends() //Thread.sleep(500)
    var utilisateur = ""
    var password = ""
    if (!AppTiers.developpement) {
      val nom = new JTextField()
      var okCxl = JOptionPane.showConfirmDialog(null, nom, "Votre nom d'utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
      if (okCxl == JOptionPane.OK_OPTION) {
        utilisateur = nom.getText
      } else {
        throw new IllegalArgumentException
      }
      val pf = new JPasswordField()
      okCxl = JOptionPane.showConfirmDialog(null, pf, "Votre mot de passe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
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
    attends()
    val identifiant = findElements(By.id("identifiant"))
    if (!identifiant.isEmpty) {
      identifiant.get(0).sendKeys(utilisateur)
      val pwd = findElement(By.id("secret_tmp"))
      pwd.sendKeys(password)
      findElement(By.className("valid")).clickw()
    }
    //findElement(By.className("nom_appli"))
    if (!getPageSource.contains("http://helios.appli.impots")) {
      throw new WebDriverException()
    }
    get("http://helios.appli.impots")
  }

  def init(typePiece: Symbol) : Unit = {
    switchTo().defaultContent
    new WebDriverWait(this, 10)
        .until(ExpectedConditions.presenceOfElementLocated(By.name("frmWork")))
    attends() //findElement(By.tagName("FRAME"))
    switchTo().frame("frmMenu")
    attends() //findElementByTagName("LINK")
    if(typePiece == 'titre)
      executeScript("m.go('172')")
    else
      executeScript("m.go('180')")
    attends()
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
      findElementByLinkText("valeurs").click()
      while (getWindowHandles.size != 2) {
        Thread.sleep(100)
      }
      val handles = getWindowHandles.toArray
      switchTo.window(handles(handles.length - 1).toString)
      attends()
      for(_ <- 1 to piece.colloc.rangPage){
        findElementByXPath("//input[@value='>>']").clickw()
        //findElementByXPath("//tr[@idx]//a")
      }
    }
    //Thread.sleep(500)
    val colbuds = findElementsByXPath("//tr[@idx]//a")
    piece.colloc.code = colbuds.get(piece.colloc.rangDansLaPage).getText
    AppTiers.collocEnCours = piece.colloc.code
    AppTiers.rangPieceCollocEnCours = 0
    if (piece.colloc.rangDansLaPage == colbuds.size - 1) {
      if (getPageSource.contains("value=\"&gt;&gt;\"")) {
        findElementByXPath("//input[@value='>>']").clickw()
        //findElementByXPath("//tr[@idx]//a")
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
    switchTo.window(handles(handles.length - 1).toString)
    switchTo().frame("frmWork")
    findElementByClassName("inputreset").clickw()
    //findElementByClassName("inputreset")
    findElementById("_1").clear()
    findElementById("_1").sendKeys(piece.colloc.code)
    if(typePiece == 'titre) {
      findElementById("_3").clear()
      val now = Calendar.getInstance()
      val dateFormatee = now.get(Calendar.DAY_OF_MONTH).toString +
        "/" + (now.get(Calendar.MONTH) + 1).toString +
        "/" + now.get(Calendar.YEAR).toString
      findElementById("_6").sendKeys(dateFormatee)
      val elementCache = findElementByName("RCE_CT_F07_CriteresRecherche_CAF.#piecesNonSoldees")
      if (elementCache.getAttribute("value") == "false") {
        findElementByName("RCE_CT_F07_CriteresRecherche_CAF.#piecesNonSoldees-checkbox").click()
      }
      findElementByClassName("inputvalider").clickw()
      //findElementByClassName("inputvalider")
      if (getPageSource.contains("total  0 titre")) {
        piece.code = ""
        piece.rangDansLaPage = -1
        piece.colloc.pasDePiece = true
      } else {
        val res = findElementsByClassName("soustitre2")
        for (i <- 0 until res.size) {
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
        if (piece.colloc.code == SessionEnCours.collocEnCoursTitres && SessionEnCours.pageTitres != "1"){
//TODO saut de page pour les titres
        }
        piece.rangDansLaPage = 0
        litCodePiece(typePiece, piece)
        piece.colloc.pasDePiece = false
      }
    } else {
      findElementByClassName("box").click()
      findElementByClassName("inputvalider").clickw()
      //findElementByClassName("inputvalider")
      if (getPageSource.contains("total 0 article")) {
        piece.code = ""
        piece.rangDansLaPage = -1
        piece.colloc.pasDePiece = true
      } else {
        val res = findElementsByClassName("soustitre2")
        for (i <- 0 until res.size) {
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
        if (piece.colloc.code == SessionEnCours.collocEnCoursArticles && SessionEnCours.pageArticles != "1"){
          val numPage = findElementByClassName("accesdirect")
          numPage.clear()
          numPage.sendKeys(SessionEnCours.pageArticles)
          findElementByXPath("//img[@src='/JFW/images/aller.gif']").clickw()
        }
        piece.rangDansLaPage = 0
        litCodePiece(typePiece, piece)
        piece.colloc.pasDePiece = false
      }
    }
    piece
  }

  def setDernierePiece(typePiece: Symbol, piece: Piece) : Piece = {

    attends() //findElementByClassName("inputreset")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    piece.dernierePieceDeLaColloc = false
    if (piece.rangDansLaPage == boutonsDetail.size - 1 && !getPageSource.contains("value=\"&gt;&gt;\"")) {
      piece.dernierePieceDeLaColloc = true
    }
    piece
  }

  def litCodePiece(typePiece: Symbol, piece : Piece) : Unit = {

    if (typePiece == 'titre) {
      val codeTitre = findElementsByXPath("//tr[@idx]/td[2]").get(piece.rangDansLaPage).getText
      piece.etat = findElementsByXPath("//tr[@idx]/td[@class='tdliste '][1]/following-sibling::TD[1]").get(piece.rangDansLaPage).getText
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
        piece.page = findElementByClassName("accesdirect").getAttribute("value")
      } catch {
        case _: org.openqa.selenium.NoSuchElementException =>
      }
      manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
      piece.code = ("TI" + piece.annee + "-" + piece.bordereau + "-" + codeTitre).replaceAll(" ", "")
    } else {
      val codeArticle = findElementsByXPath("//tr[@idx]/td[@class='tdliste '][1]").get(piece.rangDansLaPage).getText
      piece.etat = findElementsByXPath("//tr[@idx]/td[@class='tdliste '][1]/following-sibling::TD[1]").get(piece.rangDansLaPage).getText
      manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS)
      try {
        var anneeRole = findElementByXPath("//TR[TD='"+codeArticle+"']/preceding-sibling::TR[TH][1]").getText
        anneeRole = anneeRole.replaceFirst("Référence Rôle : ", "").replaceFirst("Exercice", "").trim
        piece.annee = anneeRole.split(" ")(2)
        piece.bordereau = anneeRole.split(" ")(0)
        piece.page = findElementByClassName("accesdirect").getAttribute("value")
      } catch {
        case _: org.openqa.selenium.NoSuchElementException =>
      }
      manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS)
      piece.code = ("AR"+ piece.annee + "-" + piece.bordereau + "-" + codeArticle).replaceAll(" ", "")
    }
  }

  def litDebiteur(piece : Piece): Unit = {
    attends() //findElementByXPath("//input[@value='Retour']")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail']")
    piece.debiteur.identifiant = findElementByXPath("//input[@value='Détail']/parent::*/preceding-sibling::td[1]").
      getText.replaceAll("'","").trim
    boutonsDetail.get(1).clickw()
    //findElementByXPath("//input[@value='Retour']")
    piece.debiteur.categorie = findElementByXPath("//td[label='Catégorie']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.natureJuridique = findElementByXPath("//td[label='Nature juridique']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.immatriculation = findElementByXPath("//td[label='Immatriculation']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.statutDgfip = findElementByXPath("//td[label='Statut DGFIP']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.civilite = findElementByXPath("//td[label='Civilité']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.nomRs = findElementByXPath("//td[label='Nom/RS']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.prenom = findElementByXPath("//td[label='Prénom']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    if (piece.debiteur.prenom.equals(".")) {
      piece.debiteur.prenom = ""
    }
    piece.debiteur.complement = findElementByXPath("//td[label='Complément']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.residentEnFrance = findElementByXPath("//td[label='Résident en France']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.dateDeNaissance = findElementByXPath("//td[label='Date de naissance']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.telDomicile = findElementByXPath("//td[label='Tél. Domicile']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.telPortable = findElementByXPath("//td[label='Tél. Portable']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.emel = findElementByXPath("//td[label='E-mail']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.compteParDefaut = findElementByXPath("//tr[td='Référence du compte par défaut']/following-sibling::tr[1]/td").
      getText.replaceAll("'"," ").trim
    piece.debiteur.codeActualite = findElementByXPath("//td[label='Code Actualité']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.dateMiseAJour = findElementByXPath("//td[label='Date de  mise à jour']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.nfp = findElementByXPath("//td[label='NFP']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.dateStatutDgfip = findElementByXPath("//td[label='Date Statut DGFIP']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.adresse.adressePrincipale = findElementByXPath("//td[label='Adresse Principale']").
      getText.replaceFirst("Adresse Principale","").replaceAll("'"," ").trim
    piece.debiteur.adresse.npai = findElementByXPath("//td[label='NPAI']").
      getText.replaceFirst("NPAI","").replaceAll("'"," ").trim
    piece.debiteur.adresse.dateMiseAJour = findElementByXPath("//td[label='Date mise à jour']").
      getText.replaceFirst("Date mise à jour","").replaceAll("'"," ").trim
    piece.debiteur.adresse.origineMiseAJour = findElementByXPath("//td[label='Orig. mise à jour']").
      getText.replaceFirst("Orig. mise à jour","").replaceAll("'"," ").trim
    piece.debiteur.adresse.complementAdresse = findElementByXPath("//td[label='Cplt. Adresse (Bât, Rés...)']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.adresse.numeroEtVoie = findElementByXPath("//td[label='Adresse (N° et voie)']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.adresse.localite = findElementByXPath("//td[label='Localité (Lieu-dit, BP...)']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.adresse.cpVille = findElementByXPath("//td[label='CP-Ville']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.adresse.pays = findElementByXPath("//td[label='Pays']/following-sibling::td[1]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.nomRsEmployeur = findElementByXPath("//tr[td='Employeur par défaut']/following-sibling::tr[1]/td[2]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.cpVilleEmployeur = findElementByXPath("//tr[td='Employeur par défaut']/following-sibling::tr[2]/td[2]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.nomRsCaf = findElementByXPath("//tr[td=\"Caisse d'Allocations familiales \"]/following-sibling::tr[1]/td[2]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.cpVilleCaf = findElementByXPath("//tr[td=\"Caisse d'Allocations familiales \"]/following-sibling::tr[2]/td[2]").
      getText.replaceAll("'"," ").trim
    piece.debiteur.numAllocataireCaf = findElementByXPath("//tr[td=\"Caisse d'Allocations familiales \"]/following-sibling::tr[3]/td[2]").
      getText.replaceAll("'"," ").trim
    if (getPageSource.contains("title=\"tiers consolidé\"")) {
      piece.debiteur.dateConsolidation = findElementByCssSelector("nobr").
        getText.replaceFirst("Date de consolidation :","").replaceAll("'"," ").trim
      piece.debiteur.consolide = "Oui"
      findElementByLinkText("Tiers Consolidé").clickw()
      val codesTiersConsolides = findElementsByXPath("//td[@title='nom/rs complet']/preceding-sibling::td[1]")
      val retour = findElementByXPath("//input[@value='Retour']")
      var liste = ""
      for (a <- 0 until codesTiersConsolides.size) {
        liste += codesTiersConsolides.get(a).getText + ";"
      }
      piece.debiteur.listeConsolidation = liste.substring(0, liste.length() - 1)
      val tableau = piece.debiteur.listeConsolidation.split(";").sorted.toList
      var nouvelleListe = ""
      tableau.foreach(nouvelleListe += _ + ";")
      piece.debiteur.listeConsolidation = nouvelleListe.substring(0, nouvelleListe.length()-1)
      retour.clickw()
    } else {
      piece.debiteur.dateConsolidation = ""
      piece.debiteur.consolide = "Non"
      piece.debiteur.listeConsolidation = piece.debiteur.identifiant
    }
    findElementByXPath("//input[@value='Retour']").clickw()
    findElementByXPath("//input[@value='Retour']").clickw()
  }

  def litPiece(typePiece: Symbol, piece : Piece) : Piece = {

    findElementByClassName("inputreset")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    boutonsDetail.get(piece.rangDansLaPage).clickw()
    //var page = getPageSource
    if(typePiece == 'titre) {
      //Bordereau Titres
      piece.dateEmission = findElementByXPath("//table[@class='tablezonesaisie']/descendant::tr[10]/td[2]").
        getText.replaceAll("'", " ").trim
      piece.resteARecouvrerPrincipal = findElementByXPath("//td[@width='135px']").
        getText.replaceAll("[.,€]", "").toInt
      piece.resteARecouvrerFrais = findElementByXPath("//td[@width='120px']").
        getText.replaceAll("[.,€]", "").toInt
      piece.datePrescription = findElementByXPath("//td[@width='186px']/following-sibling::td[1]").getText
    } else {
      //Rôle Article
      piece.dateEmission = findElementByXPath("//td[@class='soustitre2'][1]").
        getText.replaceAll(".*mis le  ", "").trim
      piece.resteARecouvrerPrincipal = findElementByXPath("//td[@width='185px']").
        getText.replaceAll("[.,€]", "").toInt
      piece.resteARecouvrerFrais = findElementByXPath("//td[@width='185px']/following-sibling::td[2]").
        getText.replaceAll("[.,€]", "").toInt
      piece.datePrescription = findElementByXPath("//td[@width='169px']/following-sibling::td[1]").getText
    }
    if (getPageSource.contains("Tiers débiteur")) {
      findElementByLinkText("Tiers débiteur").clickw()
      litDebiteur(piece)
      if(typePiece == 'titre) findElementByXPath("//input[@value='Retour']").clickw()
    } else {
      //TODO traiter le cas où le lien Tiers débiteur n'existe pas
      findElementByXPath("//input[@value='Retour']").clickw()
    }
    //findElementByXPath("//input[@value='Détail'][@name='OID']")
    print("\r\nTitre "+ piece.code)
    if (piece.dernierePieceDeLaColloc) {
      print(" dernier")
    }
    piece
  }

  def pieceSuivanteColloc(typePiece: Symbol, piece : Piece) : Piece = {
    //nbAppelsTitreSuivantColloc += 1
    piece.rangDansLaPage += 1

    findElementByXPath("//input[@value='Détail'][@name='OID']")
    val boutonsDetail = findElementsByXPath("//input[@value='Détail'][@name='OID']")
    if (piece.rangDansLaPage > boutonsDetail.size - 1) {
      if (getPageSource.contains("value=\"&gt;&gt;\"")) {
        findElementByXPath("//input[@value='>>']").clickw()
        //findElementByXPath("//input[@value='Détail']")
        piece.rangDansLaPage = 0
      }
    }
    litCodePiece(typePiece, piece)
    AppTiers.rangPieceCollocEnCours += 1
    piece
  }

  def pageAccueil() : Unit = {
    if(getWindowHandles.size == 2){
      findElementById("annuler").click()
      while (getWindowHandles.size == 2) {
        Thread.sleep(100)
      }
      val handles = getWindowHandles.toArray
      switchTo.window(handles(handles.length - 1).toString)
      switchTo().frame("frmWork")
    }
    findElementByXPath("//input[@value='Quitter']").clickw()
  }
}
