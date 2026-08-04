package no.digdir.fdk.parserservice.model

enum class LanguageCodes(
    val code: String,
) {
    NORWEGIAN_BOKMAL("nb"),
    NORWEGIAN_NYNORSK("nn"),
    ENGLISH("en"),
    NORWEGIAN("no"),
    NONE(""),
    ;

    companion object {
        private val byCode = entries.filter { it != NONE }.associateBy { it.code }

        fun fromCode(code: String?): LanguageCodes? =
            when {
                code == null || code.isEmpty() -> NONE
                else -> byCode[code]
            }
    }
}
