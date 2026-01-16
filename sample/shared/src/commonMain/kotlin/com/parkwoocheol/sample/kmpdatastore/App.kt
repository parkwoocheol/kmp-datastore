package com.parkwoocheol.sample.kmpdatastore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parkwoocheol.kmpdatastore.TypeSafeDataStore
import com.parkwoocheol.kmpdatastore.query.filterByValue
import com.parkwoocheol.kmpdatastore.query.query
import com.parkwoocheol.kmpdatastore.query.queryValues
import com.parkwoocheol.kmpdatastore.query.valueContains
import com.parkwoocheol.kmpdatastore.serializers.KotlinxDataStoreSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val age: Int)

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    // In a real app, you would provide this via DI or as a singleton
    val dataStore = remember { 
        TypeSafeDataStore(
            name = "sample_store", 
            serializer = KotlinxDataStoreSerializer() 
        ) 
    }

    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var queryText by remember { mutableStateOf("a") }
    var queryKeys by remember { mutableStateOf<List<String>>(emptyList()) }
    var queryValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var adultKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val savedName by dataStore.getString("name").collectAsState(initial = "")
    val savedAge by dataStore.getInt("age").collectAsState(initial = 0)
    val savedUser by dataStore.get<User>("user").collectAsState(initial = null)

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("KMP DataStore Sample", style = MaterialTheme.typography.headlineMedium)

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") }
                )

                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Age") }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        scope.launch {
                            dataStore.putString("name", nameInput)
                            val age = ageInput.toIntOrNull() ?: 0
                            dataStore.putInt("age", age)
                            dataStore.put("user", User(nameInput, age))
                        }
                    }) {
                        Text("Save")
                    }

                    Button(onClick = {
                        scope.launch {
                            dataStore.clear()
                        }
                    }) {
                        Text("Clear")
                    }
                }

                Divider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Saved Data:", style = MaterialTheme.typography.titleMedium)
                    Text("Name (Primitive): $savedName")
                    Text("Age (Primitive): $savedAge")
                    Text("User (Object): ${savedUser?.name ?: "N/A"}, ${savedUser?.age ?: "N/A"}")
                }

                Divider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Query Extensions:", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Query Text (String contains)") }
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
            }
        }
    }
}
