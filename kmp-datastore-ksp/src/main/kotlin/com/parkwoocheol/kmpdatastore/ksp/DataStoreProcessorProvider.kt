package com.parkwoocheol.kmpdatastore.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Provider for the DataStore KSP processor.
 *
 * This is the entry point for KSP to discover and instantiate the processor.
 * It is registered via META-INF/services.
 */
class DataStoreProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DataStoreProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
    }
}
