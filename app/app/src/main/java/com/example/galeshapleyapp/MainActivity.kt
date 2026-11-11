package com.example.galeshapleyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GaleShapleyScreen()
                }
            }
        }
    }
}

@Composable
fun GaleShapleyScreen() {
    val men = listOf("Héctor", "Sahid", "David", "Santiago")
    val women = listOf("Daniela", "Sara", "Alondra", "Ximena")

    // Preferencias configurables de los hombres
    var menPrefs by remember {
        mutableStateOf(
            mapOf(
                "Héctor" to listOf("Sara", "Daniela", "Alondra", "Ximena"),
                "Sahid" to listOf("Daniela", "Alondra", "Sara", "Ximena"),
                "David" to listOf("Alondra", "Ximena", "Daniela", "Sara"),
                "Santiago" to listOf("Ximena", "Sara", "Alondra", "Daniela")
            )
        )
    }

    // Preferencias configurables de las mujeres
    var womenPrefs by remember {
        mutableStateOf(
            mapOf(
                "Daniela" to listOf("Héctor", "Sahid", "David", "Santiago"),
                "Sara" to listOf("Sahid", "Héctor", "Santiago", "David"),
                "Alondra" to listOf("David", "Santiago", "Héctor", "Sahid"),
                "Ximena" to listOf("Santiago", "David", "Sahid", "Héctor")
            )
        )
    }

    var results by remember { mutableStateOf?>(null) }

    // Estados para controlar el asistente de configuración paso a paso
    var configuringPerson by remember { mutableStateOf<String?>(null) }
    var isConfiguringMan by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(0) }
    val tempSelectedOptions = remember { mutableStateOf(mutableListOf<String>()) }
    var availableOptions = remember { mutableStateOf(listOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gale-Shapley HSBM",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configura paso a paso las preferencias tanto de los hombres como de las mujeres.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // SECCIÓN DE HOMBRES
        Text(text = "Preferencias de los Hombres:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(men) { man ->
                val prefs = menPrefs[man] ?: emptyList()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Hombre: $man", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Preferencia: ${prefs.joinToString(" > ")}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                configuringPerson = man
                                isConfiguringMan = true
                                currentStep = 0
                                tempSelectedOptions.value = mutableListOf()
                                availableOptions.value = women
                            },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Elegir gustos de $man")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SECCIÓN DE MUJERES
        Text(text = "Preferencias de las Mujeres:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(women) { woman ->
                val prefs = womenPrefs[woman] ?: emptyList()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Mujer: $woman", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Preferencia: ${prefs.joinToString(" > ")}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                configuringPerson = woman
                                isConfiguringMan = false
                                currentStep = 0
                                tempSelectedOptions.value = mutableListOf()
                                availableOptions.value = men
                            },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Elegir gustos de $woman")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                results = runGaleShapley(men, women, menPrefs, womenPrefs)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Calcular Parejas Estables")
        }

        Spacer(modifier = Modifier.height(8.dp))

        results?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Resultado del Emparejamiento Estable:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    res.forEach { (man, woman) ->
                        Text(text = "• $man se empareja con $woman")
                    }
                }
            }
        }
    }

    // Diálogo interactivo paso a paso tanto para hombres como para mujeres
    configuringPerson?.let { person ->
        val positionNames = listOf(
            "1ª opción (la que/el que más le gusta)", 
            "2ª opción", 
            "3ª opción", 
            "4ª opción (la que/el que menos le gusta)"
        )
        
        AlertDialog(
            onDismissRequest = { configuringPerson = null },
            title = { Text(text = "Preferencias de $person") },
            text = {
                Column {
                    Text(text = "Selecciona su ${positionNames[currentStep]}:")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    availableOptions.value.forEach { option ->
                        Button(
                            onClick = {
                                tempSelectedOptions.value.add(option)
                                availableOptions.value = availableOptions.value.filter { it != option }
                                
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    if (isConfiguringMan) {
                                        val newMap = menPrefs.toMutableMap()
                                        newMap[person] = tempSelectedOptions.value.toList()
                                        menPrefs = newMap
                                    } else {
                                        val newMap = womenPrefs.toMutableMap()
                                        newMap[person] = tempSelectedOptions.value.toList()
                                        womenPrefs = newMap
                                    }
                                    configuringPerson = null
                                    results = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// Lógica del Algoritmo de Gale-Shapley
fun runGaleShapley(
    men: List<String>,
    women: List<String>,
    menPrefs: Map<String, List<String>>,
    womenPrefs: Map<String, List<String>>
): Map<String, String> {
    val engagedToWoman = mutableMapOf<String, String>()
    val engagedToMan = mutableMapOf<String, String>()
    val freeMen = mutableListOf<String>().apply { addAll(men) }
    val nextProposalIndex = mutableMapOf<String, Int>().apply {
        men.forEach { this[it] = 0 }
    }

    fun womanPrefersNewMan(woman: String, currentMan: String, newMan: String): Boolean {
        val prefs = womenPrefs[woman] ?: return false
        return prefs.indexOf(newMan) < prefs.indexOf(currentMan)
    }

    while (freeMen.isNotEmpty()) {
        val man = freeMen.removeAt(0)
        val mPrefs = menPrefs[man] ?: continue
        if (nextProposalIndex[man]!! >= mPrefs.size) continue
        
        val targetWoman = mPrefs[nextProposalIndex[man]!!]
        nextProposalIndex[man] = nextProposalIndex[man]!! + 1

        if (!engagedToMan.containsKey(targetWoman)) {
            engagedToMan[targetWoman] = man
            engagedToWoman[man] = targetWoman
        } else {
            val currentMan = engagedToMan[targetWoman]!
            if (womanPrefersNewMan(targetWoman, currentMan, man)) {
                engagedToMan[targetWoman] = man
                engagedToWoman.remove(currentMan)
                engagedToWoman[man] = targetWoman
                freeMen.add(currentMan)
            } else {
                freeMen.add(man)
            }
        }
    }
    return engagedToWoman
}
