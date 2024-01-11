package controllers
import play.api.mvc._
import javax.inject.Inject
import play.api.Configuration
import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Call
import com.twilio.twiml
import com.twilio.twiml.voice
import com.twilio.twiml.TwiMLException
import com.twilio.twiml.VoiceResponse
import com.twilio.twiml.voice.Say
import java.nio.file.{Files, Paths}
import play.libs
import com.twilio.rest.insights.v1
import java.net.URL
import java.net.URI
import com.twilio.rest.api.v2010.account
import com.twilio.http.HttpMethod
import com.twilio.rest.api.v2010.account.Call
import com.twilio.`type`.Twiml
import com.twilio.exception.TwilioException
import com.twilio.rest.api.v2010.account.Call
import com.twilio.twiml.TwiML
import java.net.URLEncoder
import models.Utilizator
import scala.annotation.meta.param
import java.io
import scala.io.Source
class TwilioController @Inject()(cc: ControllerComponents, config: Configuration) extends AbstractController(cc) {

  // Initialize Twilio with account SID and auth token
  Twilio.init(config.get[String]("twilio.accountSid"), config.get[String]("twilio.authToken"))

  def listaUtilizator(): List[Utilizator] = {
  List(
    new Utilizator("Persoana Fizica", "5001102440031", "Ionut", "Plesescu", 612.25, "+40765843324"),
    new Utilizator("Persoana Juridica", "33250089", "Codezilla", "SRL", 23123213.2, "+40765843324")
  )
}


 
  def PressOne(): Action[AnyContent] = Action { implicit request =>
  val toPhoneNumber = new PhoneNumber("+40765843324")
  val fromPhoneNumber = new PhoneNumber(config.get[String]("twilio.phoneNumber"))

  var utilizator = listaUtilizator()

  val call = account.Call.creator(toPhoneNumber, fromPhoneNumber, new URI("https://84b9-194-102-113-105.ngrok-free.app/CallFirst"))
    .setMethod(HttpMethod.GET)
    .create()

  Ok("Gata")
}

def CallFirst(): Action[AnyContent] = Action { implicit request =>
  val twiMLString =
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Response>
       | <Say language='ro-RO' >
       |            Apasa 1 pentru a continua in limba romana
       | </Say>
       |  <Gather input="dtmf" timeout="3" method="GET" action="/Prezentare">
       |       
       |    </Gather>
       |    <Say language='ro-RO'>Nu am primit nicio indicatie. Din pacate avem implementata doar limba romana. La revedere</Say>
       |  
       |</Response>""".stripMargin

  Ok(twiMLString).as("text/xml")
}


  def Prezentare(): Action[AnyContent] = Action { implicit request =>
    val enteredDigits = request.getQueryString("Digits").getOrElse("")

    val twiMLString = enteredDigits match {
      case "1" =>
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Say language='ro-RO'>Ați selectat limba română. Sunteți 1. persoană fizică sau 2. persoană juridică?</Say>
           |  <Gather input="dtmf" timeout="3" numDigits="1" method='GET' action='/CallSecond'>
           |    
           |  </Gather>
           |  
           |</Response>""".stripMargin

      case _ =>
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Say language='ro-RO' >Opțiune invalidă. Vă rugăm să alegeți din nou.</Say>
           |  <Gather input="dtmf" timeout="3" numDigits="1">
           |    <Say language='ro-RO' >Bine ai venit la ING. Te rog introdu 1 pentru limba română.</Say>
           |  </Gather>
           |  <Redirect method="GET">/Prezentare</Redirect>
           |</Response>""".stripMargin
    }

    Ok(twiMLString).as("text/xml")
  }

  def CallSecond(): Action[AnyContent] = Action { implicit request =>
    val selectedOption = request.getQueryString("Digits").getOrElse("")
    var utilizator = listaUtilizator()
    
    val twiMLString = selectedOption match {
      case "1" =>
  s"""<?xml version="1.0" encoding="UTF-8"?>
     |<Response>
     |  <Say language='ro-RO'>Ați ales 1. Vă rugăm să confirmați CNP-ul.</Say>
     |  <Gather input="dtmf" timeout="6" numDigits="13" method='GET' action="/handleInput">
     |  </Gather>
     |
     |</Response>""".stripMargin

      case "2" =>
  s"""<?xml version="1.0" encoding="UTF-8"?>
     |<Response>
     |  <Say language='ro-RO'>Ați ales 2. Vă rugăm să confirmați CUI-ul firmei.</Say>
     |  <Gather input="dtmf" timeout="6" numDigits="8" method='GET' action="/handleCUI">
     |  </Gather>
     
     |</Response>""".stripMargin

      case _ =>
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Say language='ro-RO' >Opțiune invalidă. Vă rugăm să alegeți din nou.</Say>
           |  <Gather numDigits="1" method="GET" action="/CallSecond">
           |  </Gather>
           |</Response>""".stripMargin
    }

    Ok(twiMLString).as("text/xml")
  }
def handleInput(): Action[AnyContent] = Action { implicit request =>
  // Assuming listaUtilizator() returns a List[Utilizator]
  val utilizatori = listaUtilizator()

  // Retrieve the entered digits from the DTMF input
  val userEnteredCNPOption = request.getQueryString("Digits")

  val responseXml = userEnteredCNPOption.map { userEnteredCNP =>
    val isCNPValid = utilizatori.exists(_.cnp == userEnteredCNP)
    println(isCNPValid)
    if (isCNPValid) {
      val redirectUrl = s"/Detalii?userCNP=$userEnteredCNP"
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |  <Say language='ro-RO'>Ai confirmat cu succes</Say>
         |  <Redirect method="GET">$redirectUrl</Redirect>
         |</Response>""".stripMargin
    } else {
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |  <Say language='ro-RO'>CNP Incorect</Say>
         |</Response>""".stripMargin
    }
  }.getOrElse {
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Response>
       |  <Say language='ro-RO'>CNP Incorect</Say>
       |</Response>""".stripMargin
  }

  Ok(responseXml).as("text/xml")
}


def handleCUI():Action[AnyContent] = Action { implicit request =>
val utilizator = listaUtilizator()
val userEnteredCNPOption = request.getQueryString("Digits")
val responseXml = userEnteredCNPOption.map { userEnteredCNP =>
    val isCNPValid = utilizator.exists(_.cnp == userEnteredCNP)
    println(isCNPValid)
    if (isCNPValid) {
      val redirectUrl = s"/Detalii?userCNP=$userEnteredCNP"
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |  <Say language='ro-RO'>Ai confirmat cu succes CUI</Say>
         |  <Redirect method="GET">$redirectUrl</Redirect>
         |</Response>""".stripMargin
    } else {
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |  <Say language='ro-RO'>CUI Incorect</Say>
         |</Response>""".stripMargin
    }
  }.getOrElse {
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Response>
       |  <Say language='ro-RO'>CNP Incorect</Say>
       |</Response>""".stripMargin
  }

  Ok(responseXml).as("text/xml")

}
def Detalii(): Action[AnyContent] = Action { implicit request =>
  val userCNPOption: Option[String] = request.getQueryString("userCNP")
  println(s"userCNPOption: $userCNPOption")

  val responseXml = userCNPOption.flatMap { userCNP =>
    val userList = listaUtilizator()
    println(s"User list: $userList")

    // Find the user in the list using the findByCNP method
    val userOption = userList.find(_.findByCNP(userCNP).isDefined)

    userOption.map { user =>
      println(s"Found user: $user")
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |  <Say language='ro-RO'>Datele tale la momentul actual sunt: CNP/CUI $userCNP Sold ${user.soldActual} lei, Nume ${user.nume} Prenume ${user.prenume}</Say>
         |  <Redirect method="GET">/Incheiere</Redirect>
         |</Response>""".stripMargin
    }
  }.getOrElse {
    println("User not found")
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Response>
       |  <Say language='ro-RO'>CNP indisponibil</Say>
       |</Response>""".stripMargin
  }

  Ok(responseXml).as("text/xml")
}
def Incheiere():Action[AnyContent] = Action { implicit request =>

  var responseXml =
  s"""<?xml version="1.0" encoding="UTF-8"?>
     |<Response>
     |  <Say language='ro-RO'>Te mai putem ajuta cu altceva?</Say>
     |  <Gather timeout="10" numDigits="1" method="GET" action="/handleCallCenter">
     |    <Say language='ro-RO'>Daca doresti sa suni la CallCenter, apasa 1</Say>
     |  </Gather>
     |</Response>""".stripMargin

Ok(responseXml).as("text/xml")
}
def handleCallCenter(): Action[AnyContent] = Action { implicit request =>
  val userPressedOption = request.getQueryString("Digits")

  val responseXml = userPressedOption match {
    case Some(digits) if digits == "1" =>
      // Logic to initiate a call to the CallCenter
      s"""<?xml version="1.0" encoding="UTF-8"?>
   |<Response>
   |  <Say language='ro-RO'>Initiem apelul catre CallCenter. Multumim pentru apasare!</Say>
   |  <Say language='ro-RO'>Toti operatorii nostri sunt ocupati. Ramai conectat pentru preluarea apelului. Iti vom pune niste bancuri, zambeste.</Say>
   |  <Dial callerId="+40765843324">
   |    <Number>+40765843324</Number>
   |  </Dial>
   |<Play>https://84b9-194-102-113-105.ngrok-free.app/bancuri</Play>
   |  
   |  <Say>Adio. </Say>
   |</Response>""".stripMargin


          
    case _ =>
      // Handle other scenarios or invalid input
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |  <Say language='ro-RO'>Optiune invalida. </Say>
         |</Response>""".stripMargin
  }

  Ok(responseXml).as("text/xml")
}
def bancuri(): Action[AnyContent] = Action { implicit request =>
val absolutePathBancuri = "C:\\Users\\ionut.plesescu\\Documents\\Scala\\Scala\\voiceapp\\app\\wwwroot\\bancuri.mp3"
val file = new java.io.File(absolutePathBancuri)



  if (file.exists()) {
    val mp3Bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath))

    Ok(mp3Bytes).as("audio/mpeg")
  } else {
    NotFound(s"MP3 file not found at $absolutePathBancuri")
  }
}


}