import org.junit.Test
import org.slf4j.LoggerFactory
import ru.barabo.scanner.service.dateIssuedPassport
import ru.barabo.scanner.service.docNumber
import ru.barabo.scanner.service.trim32

class TestScaner {

    private val logger = LoggerFactory.getLogger(TestScaner::class.java)!!


    @Test
    fun testFioPattern() {
        val fio = "НЕГАНОВА! ММАРГАРИТА АНАТОЛЬЕВНА"

        //if(isNotFioPattern(fio.trim().uppercase(Locale.getDefault() ))) throw Exception("Неправильно написана 'фамилия имя отчество'")
    }

    //@Test
    fun testsummaryTrim() {

        val fields =
            "П;;02.06.2023;10702020;Верховых Лидия Ивановна;250602061030;21;662727;0501;16.01.2002;7730176610;773001001;024501901;03100643000000019502;643;3;9070;832065.51;15311009000011000110"
                .split(';')

        logger.error(fields[0].trim().trim32())
    }

    //@Test
    fun testgetFormatDoc() {

        val map = mapOf("DOC_NUMBER" to "")

        logger.error("docNumber=${map.docNumber()}")
    }


    //@Test
    fun testTimestamp() {

        val map = mapOf("PAYER_DOC_ISSUED" to "17.04.2019")

        val time = map.dateIssuedPassport()

        logger.error("time=$time")
    }
    //@Test
    fun testsummaryAmountAndKbk() {

        val fields =
"ПО;0000773;02.06.2023;10716050;КАЛЕННИКОВ ВЛАДИСЛАВ АЛЕКСАНДРОВИЧ;250600781096;RU01001;656682;0518;17.04.2019;7730176610;773001001;024501901;03100643000000019502;643;2;9070;3100.00;15311009000011000110;9070;450079.86;15311009000011000110"
    .split(';')

        val countPay = fields[15].trim().toInt()

        val (amountSum, kbk) = ru.barabo.scanner.service.summaryAmountAndKbk(fields, countPay, startIndex = 16)

        logger.error("countPay=$countPay")

        logger.error("amountSum=$amountSum")

        logger.error("amountSum.toString()=${amountSum.toString()}")

        logger.error("kbk=$kbk")

        logger.error("fields=$fields")
    }



    //@Test
    fun testSplit() {
        val str = "S000012ђЖнmM=ООО УК \"ХАСАН СЕРВИС ДВ\"╨e\u000EsodOQAґc=40702810050000001970LBekNaсu=ДАЛЬНЕВОСТОЧНЫЙ БАНК ПАО \"СБЕРБАНК РОССИИ\" Г.ХАБАРОВСК||НC=040813608L_oЌre♦AЙc=30101810600000000608њPayeeINN=2531005882Catйgory=УСЛУГИ ЖКХ\\fersAcН=00005155\u0004ll\u000FtName=КОРЧАГИН|firs\u0010Name=ПАВЕЛ|middleNa}e=АНАТОЛЬЕВИЧњpaЙerAddress=ПГТ. СЛАВЯНКА, УЛ. МОЛОДЕЖНАЯ, ДОМ № 14, КВ. 42|Sym=2068♦"

        logger.error(str)

        val tags =   str.split("=")
        tags.forEach{
            logger.error(it)
        }
    }

    //@Test
    fun testWhenAny() {
        val a: Int? = 1
        val b: Int? = 1
        val c: Int? = null

        logger.error(
            when {
                (a == null).apply { logger.error("a == null") } -> "LASTNAME"
                (b == null).apply { logger.error("b == null") } -> "FIRSTNAME"
                (c == null).apply { logger.error("c == null") } -> "MIDDLENAME"
                else -> "IGNORE"
            })
    }


    //@Test
    fun testCyrrilic() {
//        for(c in ' '..'ё' ) {
//            logger.error("c=$c CODE=${c.toInt()} isLetter=${c.isLetter()}")
//        }
//
//        logger.error("c=ё CODE=${'ё'.toInt()} isLetter=${'ё'.isLetter()}")
        //logger.error("c=Ь CODE=${'Ь'.toInt()} isLetter=${'Ь'.isLetter()}")
    }

}