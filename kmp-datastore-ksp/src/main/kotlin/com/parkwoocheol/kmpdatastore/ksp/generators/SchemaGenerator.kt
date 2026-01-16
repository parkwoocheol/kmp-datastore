package com.parkwoocheol.kmpdatastore.ksp.generators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Generates schema documentation and migration helpers for @DataStoreSchema annotated classes.
 *
 * For a class like:
 * ```kotlin
 * @DataStoreSchema(version = 2, exportSchema = true)
 * data class UserSettings(val theme: String, val notifications: Boolean)
 * ```
 *
 * Generates:
 * - JSON schema file (if exportSchema = true)
 * - Migration helper class
 */
class SchemaGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {
    fun generate(classDecl: KSClassDeclaration) {
        val className = classDecl.simpleName.asString()
        val packageName = classDecl.packageName.asString()

        // Get annotation
        val annotation =
            classDecl.annotations.find {
                it.shortName.asString() == "DataStoreSchema"
            } ?: return

        val version =
            annotation.arguments.find {
                it.name?.asString() == "version"
            }?.value as? Int ?: 1

        val exportSchema =
            annotation.arguments.find {
                it.name?.asString() == "exportSchema"
            }?.value as? Boolean ?: true

        // Export schema JSON if enabled
        if (exportSchema) {
            exportSchemaJson(classDecl, version)
        }

        logger.info("Processed schema for $className (version=$version)")
    }

    private fun exportSchemaJson(
        classDecl: KSClassDeclaration,
        version: Int,
    ) {
        val className = classDecl.simpleName.asString()
        val packageName = classDecl.packageName.asString()

        // Build schema JSON
        val properties =
            classDecl.getAllProperties().map { prop ->
                val propName = prop.simpleName.asString()
                val propType = prop.type.resolve()
                val isNullable = propType.isMarkedNullable
                val typeName = propType.declaration.simpleName.asString()

                """    {
      "name": "$propName",
      "type": "$typeName",
      "nullable": $isNullable
    }"""
            }.joinToString(",\n")

        val schemaJson = """{
  "schemaVersion": $version,
  "className": "$className",
  "packageName": "$packageName",
  "generatedAt": "${java.time.Instant.now()}",
  "properties": [
$properties
  ]
}"""

        // Write schema file
        val file =
            codeGenerator.createNewFile(
                Dependencies(false, classDecl.containingFile!!),
                "schemas",
                "$className-schema-v$version",
                extensionName = "json",
            )

        file.writer().use { writer ->
            writer.write(schemaJson)
        }

        logger.info("Exported schema: schemas/$className-schema-v$version.json")
    }
}
