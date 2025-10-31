package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.informationmodel.InformationModel

/**
 * Utility class for merging multiple InformationModel instances.
 *
 * This merger combines information models parsed by different parsers,
 * prioritizing values from the first (highest priority) parser while
 * falling back to values from lower-priority parsers when needed.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
object InformationModelMerger {

    /**
     * Merges a list of information models, prioritizing values from earlier items.
     *
     * The first information model in the list is used as the base, and values from
     * subsequent models are only used if the corresponding field in the base is null.
     *
     * @param models List of information models to merge, in priority order
     * @return The merged information model
     * @throws IllegalArgumentException if the models list is empty
     */
    fun merge(models: List<InformationModel>): InformationModel {
        require(models.isNotEmpty()) { "Cannot merge empty list of information models" }

        if (models.size == 1) {
            return models.first()
        }

        val base = models.first()
        val builder = InformationModel.newBuilder(base)

        // For each subsequent model, fill in null fields from the base
        for (model in models.drop(1)) {
            if (base.title == null) builder.setTitle(model.title)
            if (base.description == null) builder.setDescription(model.description)
            if (base.descriptionFormatted == null) builder.setDescriptionFormatted(model.descriptionFormatted)
            if (base.publisher == null) builder.setPublisher(model.publisher)
            if (base.identifier == null) builder.setIdentifier(model.identifier)
            if (base.dctType == null) builder.setDctType(model.dctType)
            if (base.conformsTo == null) builder.setConformsTo(model.conformsTo)
            if (base.license == null) builder.setLicense(model.license)
            if (base.informationModelIdentifier == null) builder.setInformationModelIdentifier(model.informationModelIdentifier)
            if (base.spatial == null) builder.setSpatial(model.spatial)
            if (base.temporal == null) builder.setTemporal(model.temporal)
            if (base.isPartOf == null) builder.setIsPartOf(model.isPartOf)
            if (base.hasPart == null) builder.setHasPart(model.hasPart)
            if (base.isReplacedBy == null) builder.setIsReplacedBy(model.isReplacedBy)
            if (base.isProfileOf == null) builder.setIsProfileOf(model.isProfileOf)
            if (base.replaces == null) builder.setReplaces(model.replaces)
            if (base.hasFormat == null) builder.setHasFormat(model.hasFormat)
            if (base.homepage == null) builder.setHomepage(model.homepage)
            if (base.status == null) builder.setStatus(model.status)
            if (base.versionInfo == null) builder.setVersionInfo(model.versionInfo)
            if (base.versionNotes == null) builder.setVersionNotes(model.versionNotes)
            if (base.subjects == null) builder.setSubjects(model.subjects)
            if (base.containsSubjects == null) builder.setContainsSubjects(model.containsSubjects)
            if (base.containsModelElements == null) builder.setContainsModelElements(model.containsModelElements)
            if (base.modelElements == null) builder.setModelElements(model.modelElements)
            if (base.modelProperties == null) builder.setModelProperties(model.modelProperties)
            if (base.themeUris == null) builder.setThemeUris(model.themeUris)
            if (base.theme == null) builder.setTheme(model.theme)
            if (base.losTheme == null) builder.setLosTheme(model.losTheme)
            if (base.eurovocThemes == null) builder.setEurovocThemes(model.eurovocThemes)
            if (base.keyword == null) builder.setKeyword(model.keyword)
            if (base.contactPoint == null) builder.setContactPoint(model.contactPoint)
            if (base.issued == null) builder.setIssued(model.issued)
            if (base.modified == null) builder.setModified(model.modified)
            if (base.language == null) builder.setLanguage(model.language)
        }

        return builder.build()
    }
}
