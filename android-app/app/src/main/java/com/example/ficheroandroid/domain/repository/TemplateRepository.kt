package com.example.ficheroandroid.domain.repository

import com.example.ficheroandroid.domain.model.LabelTemplate

interface TemplateRepository {
    fun saveTemplate(template: LabelTemplate)
    fun loadTemplates(): List<LabelTemplate>
    fun exportTemplateJson(template: LabelTemplate): String
    fun importTemplateJson(raw: String): LabelTemplate?
}

