package com.example.ficheroandroid.data.templates

import android.content.Context
import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.domain.model.LabelContentType
import com.example.ficheroandroid.domain.model.LabelTemplate
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import org.json.JSONArray
import org.json.JSONObject

class LocalTemplateDataSource(context: Context) {
    private val prefs = context.getSharedPreferences("label_templates", Context.MODE_PRIVATE)
    private val keyTemplates = "items"

    fun saveTemplate(template: LabelTemplate) {
        val current = loadTemplates().toMutableList()
        current.add(0, template)
        val arr = JSONArray()
        current.take(20).forEach { t ->
            arr.put(templateToJson(t))
        }
        prefs.edit().putString(keyTemplates, arr.toString()).apply()
    }

    fun loadTemplates(): List<LabelTemplate> {
        val raw = prefs.getString(keyTemplates, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(jsonToTemplate(o, i))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun exportTemplateJson(template: LabelTemplate): String = templateToJson(template).toString()

    fun importTemplateJson(raw: String): LabelTemplate? {
        return runCatching {
            jsonToTemplate(JSONObject(raw), 0)
        }.getOrNull()
    }

    private fun templateToJson(t: LabelTemplate): JSONObject {
        return JSONObject()
            .put("title", t.title)
            .put("text", t.text)
            .put("fontSizePx", t.fontSizePx)
            .put("labelWidthPx", t.labelWidthPx)
            .put("labelHeightPx", t.labelHeightPx)
            .put("density", t.density)
            .put("copies", t.copies)
            .put("contentType", t.contentType.name)
            .put("csvEnabled", t.csvEnabled)
            .put("csvText", t.csvText)
            .put("objects", JSONArray().apply {
                t.objects.forEach { o ->
                    put(
                        JSONObject()
                            .put("id", o.id)
                            .put("type", o.type.name)
                            .put("text", o.text)
                            .put("x", o.x)
                            .put("y", o.y)
                            .put("width", o.width)
                            .put("height", o.height)
                            .put("fontSizePx", o.fontSizePx)
                            .put("strokeWidth", o.strokeWidth)
                            .put("imageBase64", o.imageBase64),
                    )
                }
            })
            .put("postProcess", t.postProcess.name)
            .put("threshold", t.threshold)
            .put("invert", t.invert)
            .put("labelType", t.labelType)
            .put("offsetX", t.offsetX)
            .put("offsetY", t.offsetY)
            .put("offsetType", t.offsetType.name)
    }

    private fun jsonToTemplate(o: JSONObject, index: Int): LabelTemplate {
        return LabelTemplate(
            title = o.optString("title", "Template ${index + 1}"),
            text = o.optString("text", ""),
            fontSizePx = o.optDouble("fontSizePx", 28.0).toFloat(),
            labelWidthPx = o.optInt("labelWidthPx", 240),
            labelHeightPx = o.optInt("labelHeightPx", 120),
            density = o.optInt("density", 2),
            copies = o.optInt("copies", 1),
            contentType = runCatching {
                LabelContentType.valueOf(o.optString("contentType", "TEXT"))
            }.getOrDefault(LabelContentType.TEXT),
            objects = parseObjects(o.optJSONArray("objects")),
            csvEnabled = o.optBoolean("csvEnabled", false),
            csvText = o.optString("csvText", ""),
            postProcess = runCatching {
                PostProcessType.valueOf(o.optString("postProcess", "THRESHOLD"))
            }.getOrDefault(PostProcessType.THRESHOLD),
            threshold = o.optInt("threshold", 140),
            invert = o.optBoolean("invert", false),
            labelType = o.optInt("labelType", 1),
            offsetX = o.optInt("offsetX", 0),
            offsetY = o.optInt("offsetY", 0),
            offsetType = runCatching {
                OffsetType.valueOf(o.optString("offsetType", "INNER"))
            }.getOrDefault(OffsetType.INNER),
        )
    }

    private fun parseObjects(arr: JSONArray?): List<DesignObject> {
        if (arr == null) return emptyList()
        return buildList {
            for (j in 0 until arr.length()) {
                val ob = arr.getJSONObject(j)
                add(
                    DesignObject(
                        id = ob.optLong("id", System.currentTimeMillis() + j),
                        type = runCatching {
                            DesignObjectType.valueOf(ob.optString("type", "TEXT"))
                        }.getOrDefault(DesignObjectType.TEXT),
                        text = ob.optString("text", ""),
                        x = ob.optInt("x", 8),
                        y = ob.optInt("y", 8),
                        width = ob.optInt("width", 70),
                        height = ob.optInt("height", 24),
                        fontSizePx = ob.optDouble("fontSizePx", 18.0).toFloat(),
                        strokeWidth = ob.optInt("strokeWidth", 2),
                        imageBase64 = ob.optString("imageBase64", "").ifBlank { null },
                    ),
                )
            }
        }
    }
}

