package ru.barabo.scanner.service

import ru.barabo.afina.AfinaOrm
import ru.barabo.db.service.StoreFilterService
import ru.barabo.scanner.entity.PasportType

object PasportTypeService :  StoreFilterService<PasportType>(AfinaOrm, PasportType::class.java)