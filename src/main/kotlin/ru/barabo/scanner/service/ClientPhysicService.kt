package ru.barabo.scanner.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.ClientPhysic

object ClientPhysicService : StoreFilterService<ClientPhysic>(AfinaOrm, ClientPhysic::class.java)