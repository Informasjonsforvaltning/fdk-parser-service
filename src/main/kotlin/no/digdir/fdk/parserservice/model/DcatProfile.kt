package no.digdir.fdk.parserservice.model

/**
 * Specification profiles a resource description can be in accordance with.
 */
enum class DcatProfile {
    DCAT_AP_NO,
    HVD_DCAT_AP_NO,
    MOBILITY_DCAT_AP,
    ;

    companion object {
        /**
         * Removes profiles that cannot describe the same resource at the same time.
         *
         * A description in accordance with mobilityDCAT-AP is not a description in accordance
         * with DCAT-AP-NO. HVD-DCAT-AP-NO supplements DCAT-AP-NO and is kept alongside it.
         *
         * @param profiles The profiles that apply to the resource
         * @return The profiles that can apply at the same time, in the order they were given
         */
        fun withoutConflicting(profiles: Collection<DcatProfile>): List<DcatProfile> = when {
            profiles.contains(MOBILITY_DCAT_AP) -> profiles.filterNot { it == DCAT_AP_NO }
            else -> profiles.toList()
        }
    }
}
