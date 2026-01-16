package com.parkwoocheol.kmpdatastore.annotations

/**
 * Marks a class for schema versioning and documentation export.
 *
 * When applied to a data class, the KSP processor will:
 * - Generate migration helper class for version upgrades
 * - Export schema to JSON for documentation (if enabled)
 * - Track schema changes across versions
 *
 * Example:
 * ```kotlin
 * @DataStoreSchema(version = 2, exportSchema = true)
 * @Serializable
 * data class UserSettings(
 *     val theme: String,
 *     val notifications: Boolean,
 *     val language: String  // Added in version 2
 * )
 * ```
 *
 * @property version Schema version number. Increment when making changes.
 * @property exportSchema Whether to export schema to JSON file.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DataStoreSchema(
    /**
     * Schema version number.
     * Increment this value when making any changes to the data structure.
     */
    val version: Int,
    /**
     * Whether to export the schema to a JSON file.
     * The file will be generated at: schemas/{ClassName}-schema-v{version}.json
     */
    val exportSchema: Boolean = true,
)
