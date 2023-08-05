package io.github.gasbarrel.localization

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.ServiceType
import com.freya02.botcommands.api.localization.LocalizationMap
import com.freya02.botcommands.api.localization.LocalizationMapRequest
import com.freya02.botcommands.api.localization.readers.LocalizationMapReader
import com.typesafe.config.ConfigFactory
import java.io.InputStreamReader

@BService
@ServiceType(LocalizationMapReader::class)
class HoconLocalizationMapReader(private val context: BContext) : LocalizationMapReader {
    override fun readLocalizationMap(request: LocalizationMapRequest): LocalizationMap? {
        val inputStream = HoconLocalizationMapReader::class.java.getResourceAsStream("/bc_localization/${request.bundleName}.conf")
            ?: return null
        val config = InputStreamReader(inputStream).use { ConfigFactory.parseReader(it) }

        return HoconLocalizationMap(context, request.requestedLocale, config)
    }
}
