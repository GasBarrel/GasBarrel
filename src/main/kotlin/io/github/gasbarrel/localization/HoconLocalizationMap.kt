package io.github.gasbarrel.localization

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.localization.DefaultLocalizationTemplate
import com.freya02.botcommands.api.localization.LocalizationMap
import com.freya02.botcommands.api.localization.LocalizationTemplate
import com.typesafe.config.Config
import java.util.*

class HoconLocalizationMap(
    private val context: BContext,
    override val effectiveLocale: Locale,
    private val config: Config
) : LocalizationMap {
    override val keys: Set<String>
        get() = config.entrySet().mapTo(hashSetOf()) { it.key }

    override fun get(path: String): LocalizationTemplate? = when {
        config.hasPathOrNull(path) && !config.getIsNull(path) -> DefaultLocalizationTemplate(context, config.getString(path), effectiveLocale)
        else -> null
    }
}
