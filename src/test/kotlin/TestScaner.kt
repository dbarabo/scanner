import org.apache.log4j.Logger
import org.junit.Test

class TestScaner {

    private val logger = Logger.getLogger(TestScaner::class.simpleName)!!


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
        logger.error("c=Ь CODE=${'Ь'.toInt()} isLetter=${'Ь'.isLetter()}")
    }



}