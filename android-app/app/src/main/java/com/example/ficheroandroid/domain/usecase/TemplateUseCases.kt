package com.example.ficheroandroid.domain.usecase

import com.example.ficheroandroid.domain.model.LabelTemplate
import com.example.ficheroandroid.domain.repository.TemplateRepository

class SaveTemplateUseCase(
    private val templateRepository: TemplateRepository,
) {
    operator fun invoke(template: LabelTemplate) = templateRepository.saveTemplate(template)
}

class LoadLastTemplateUseCase(
    private val templateRepository: TemplateRepository,
) {
    operator fun invoke(): LabelTemplate? = templateRepository.loadTemplates().firstOrNull()
}

class LoadTemplatesUseCase(
    private val templateRepository: TemplateRepository,
) {
    operator fun invoke(): List<LabelTemplate> = templateRepository.loadTemplates()
}

class ExportTemplateJsonUseCase(
    private val templateRepository: TemplateRepository,
) {
    operator fun invoke(template: LabelTemplate): String = templateRepository.exportTemplateJson(template)
}

class ImportTemplateJsonUseCase(
    private val templateRepository: TemplateRepository,
) {
    operator fun invoke(raw: String): LabelTemplate? = templateRepository.importTemplateJson(raw)
}

