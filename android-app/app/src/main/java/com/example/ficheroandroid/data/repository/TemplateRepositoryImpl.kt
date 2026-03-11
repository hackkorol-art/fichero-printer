package com.example.ficheroandroid.data.repository

import com.example.ficheroandroid.data.templates.LocalTemplateDataSource
import com.example.ficheroandroid.domain.model.LabelTemplate
import com.example.ficheroandroid.domain.repository.TemplateRepository

class TemplateRepositoryImpl(
    private val dataSource: LocalTemplateDataSource,
) : TemplateRepository {
    override fun saveTemplate(template: LabelTemplate) {
        dataSource.saveTemplate(template)
    }

    override fun loadTemplates(): List<LabelTemplate> {
        return dataSource.loadTemplates()
    }

    override fun exportTemplateJson(template: LabelTemplate): String {
        return dataSource.exportTemplateJson(template)
    }

    override fun importTemplateJson(raw: String): LabelTemplate? {
        return dataSource.importTemplateJson(raw)
    }
}

