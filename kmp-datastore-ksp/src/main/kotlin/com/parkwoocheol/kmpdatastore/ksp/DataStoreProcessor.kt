package com.parkwoocheol.kmpdatastore.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.parkwoocheol.kmpdatastore.ksp.generators.QueryBuilderGenerator
import com.parkwoocheol.kmpdatastore.ksp.generators.SchemaGenerator
import com.parkwoocheol.kmpdatastore.ksp.generators.ValidationGenerator

/**
 * KSP processor for DataStore annotations.
 *
 * Processes the following annotations:
 * - @DataStoreIndex: Generates type-safe query builders
 * - @DataStoreSchema: Generates schema JSON and migration helpers
 * - Validation annotations (@Min, @Max, @Pattern, @NotBlank): Generates validators
 */
class DataStoreProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    private val queryBuilderGenerator = QueryBuilderGenerator(codeGenerator, logger)
    private val schemaGenerator = SchemaGenerator(codeGenerator, logger)
    private val validationGenerator = ValidationGenerator(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferredSymbols = mutableListOf<KSAnnotated>()

        // Process @DataStoreIndex for query builder generation
        val indexSymbols =
            resolver.getSymbolsWithAnnotation(
                "com.parkwoocheol.kmpdatastore.annotations.DataStoreIndex",
            )
        indexSymbols.filterIsInstance<KSClassDeclaration>().forEach { classDecl ->
            if (classDecl.validate()) {
                logger.info("Processing @DataStoreIndex: ${classDecl.simpleName.asString()}")
                queryBuilderGenerator.generate(classDecl)
            } else {
                deferredSymbols.add(classDecl)
            }
        }

        // Process @DataStoreSchema for schema generation
        val schemaSymbols =
            resolver.getSymbolsWithAnnotation(
                "com.parkwoocheol.kmpdatastore.annotations.DataStoreSchema",
            )
        schemaSymbols.filterIsInstance<KSClassDeclaration>().forEach { classDecl ->
            if (classDecl.validate()) {
                logger.info("Processing @DataStoreSchema: ${classDecl.simpleName.asString()}")
                schemaGenerator.generate(classDecl)
            } else {
                deferredSymbols.add(classDecl)
            }
        }

        // Process validation annotations
        val validationAnnotations =
            listOf(
                "com.parkwoocheol.kmpdatastore.annotations.validation.Min",
                "com.parkwoocheol.kmpdatastore.annotations.validation.Max",
                "com.parkwoocheol.kmpdatastore.annotations.validation.Pattern",
                "com.parkwoocheol.kmpdatastore.annotations.validation.NotBlank",
            )

        val classesWithValidation = mutableSetOf<KSClassDeclaration>()
        validationAnnotations.forEach { annotationName ->
            resolver.getSymbolsWithAnnotation(annotationName)
                .mapNotNull { it.parent as? KSClassDeclaration }
                .forEach { classesWithValidation.add(it) }
        }

        classesWithValidation.forEach { classDecl ->
            if (classDecl.validate()) {
                logger.info("Processing validation for: ${classDecl.simpleName.asString()}")
                validationGenerator.generate(classDecl)
            } else {
                deferredSymbols.add(classDecl)
            }
        }

        return deferredSymbols
    }
}
