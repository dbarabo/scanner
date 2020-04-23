package ru.barabo.afina

import ru.barabo.db.DbConnection
import ru.barabo.db.DbSetting

object AfinaConnect: DbConnection(
        DbSetting("oracle.jdbc.driver.OracleDriver",
                "",
                "",
                "",
                "select 1 from dual") ) {


    //private const val AFINA_URL = "jdbc:oracle:thin:@192.168.0.43:1521:AFINA"
    //private const val TEST_URL = "jdbc:oracle:thin:@192.168.0.42:1521:AFINA"

    @JvmStatic
    fun init(url: String, user: String, password: String): Boolean {
        dbSetting.url = url
        dbSetting.user = user
        dbSetting.password = password

        return checkBase()
    }
}
