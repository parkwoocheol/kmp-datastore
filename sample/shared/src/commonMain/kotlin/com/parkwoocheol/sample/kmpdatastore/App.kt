package com.parkwoocheol.sample.kmpdatastore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parkwoocheol.kmpdatastore.TypeSafeDataStore
import com.parkwoocheol.kmpdatastore.annotations.DataStoreIndex
import com.parkwoocheol.kmpdatastore.annotations.DataStoreKey
import com.parkwoocheol.kmpdatastore.annotations.DataStoreSchema
import com.parkwoocheol.kmpdatastore.annotations.RequiresSerializer
import com.parkwoocheol.kmpdatastore.annotations.SafeSerializable
import com.parkwoocheol.kmpdatastore.annotations.validation.Max
import com.parkwoocheol.kmpdatastore.annotations.validation.Min
import com.parkwoocheol.kmpdatastore.annotations.validation.NotBlank
import com.parkwoocheol.kmpdatastore.annotations.validation.Pattern
import com.parkwoocheol.kmpdatastore.query.filterByValue
import com.parkwoocheol.kmpdatastore.query.query
import com.parkwoocheol.kmpdatastore.query.queryValues
import com.parkwoocheol.kmpdatastore.query.valueContains
import com.parkwoocheol.kmpdatastore.serializers.KotlinxDataStoreSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Enhanced User data class with annotations for KSP code generation.
 *
 * - @SafeSerializable: Marks this class as safe for DataStore with version tracking
 * - @DataStoreIndex: Generates type-safe query builder (UserQueryBuilder)
 * - @DataStoreSchema: Generates schema JSON file
 * - Validation annotations: Generate UserValidator
 */
@SafeSerializable(version = 1, description = "User profile data")
@DataStoreSchema(version = 1)
@DataStoreIndex(properties = ["age", "email"])
@Serializable
data class User(
    @DataStoreKey("user_name", required = true)
    @NotBlank
    val name: String,

    @Min(0)
    @Max(150)
    val age: Int,

    @Pattern("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    val email: String = "",
)

@OptIn(RequiresSerializer::class)
@Composable
fun App() {
    val scope = rememberCoroutineScope()
    // In a real app, you would provide this via DI or as a singleton
    val dataStore = remember {
        TypeSafeDataStore(
            name = "sample_store",
            serializer = KotlinxDataStoreSerializer(),
        )
    }

    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var queryText by remember { mutableStateOf("a") }
    var queryKeys by remember { mutableStateOf<List<String>>(emptyList()) }
    var queryValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var adultKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var validationMessage by remember { mutableStateOf("") }

    val savedName by dataStore.getString("name").collectAsState(initial = "")
    val savedAge by dataStore.getInt("age").collectAsState(initial = 0)
    val savedUser by dataStore.get<User>("user").collectAsState(initial = null)

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("KMP DataStore Sample", style = MaterialTheme.typography.headlineMedium)

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                )

                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Age") },
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email") },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            val age = ageInput.toIntOrNull() ?: 0
                            val user = User(nameInput, age, emailInput)

                            // KSP-generated validation
                            val result = UserValidator.validate(user)
                            
                            if (result.isSuccess) {
                                dataStore.putString("name", nameInput)
                                dataStore.putInt("age", age)
                                dataStore.put("user", user)
                                validationMessage = "✓ Saved successfully!"
                            } else {
                                validationMessage = "✗ ${result.getErrorMessages().joinToString(", ")}"
                            }
                        }
                    }) {
                        Text("Save with Validation")
                    }

                    Button(onClick = {
                        scope.launch {
                            dataStore.clear()
                            validationMessage = ""
                        }
                    }) {
                        Text("Clear")
                    }
                }

                if (validationMessage.isNotEmpty()) {
                    Text(
                        text = validationMessage,
                        color = if (validationMessage.startsWith("✓")) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Saved Data:", style = MaterialTheme.typography.titleMedium)
                    Text("Name (Primitive): $savedName")
                    Text("Age (Primitive): $savedAge")
                    Text(
                        "User (Object): ${savedUser?.name ?: "N/A"}, " +
                            "${savedUser?.age ?: "N/A"}, ${savedUser?.email ?: "N/A"}",
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Query Extensions:", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Query Text (String contains)") },
                    )

                    Button(onClick = {
                        scope.launch {
                            queryKeys = dataStore.query()
                                .startsWith("n")
                                .executeKeys()
                                .first()

                            queryValues = dataStore.queryValues<String>()
                                .valueContains(queryText)
                                .executeMap()
                                .first()

                            adultKeys = dataStore.filterByValue<Int> { _, value ->
                                value >= 18
                            }.first()
                        }
                    }) {
                        Text("Run Query")
                    }

                    Text("Keys starting with 'n': ${queryKeys.joinToString()}")
                    Text("String values containing '$queryText': $queryValues")
                    Text("Adult keys (age >= 18): ${adultKeys.joinToString()}")
                }

                HorizontalDivider()

                // Annotation info section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Annotation Examples:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "User class is annotated with:\n" +
                            "• @SafeSerializable(version = 1)\n" +
                            "• @DataStoreIndex(properties = [\"age\", \"email\"])\n" +
                            "• Property validations: @Min, @Max, @NotBlank, @Pattern",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
