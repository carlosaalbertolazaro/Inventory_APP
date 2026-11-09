package com.example.appcrud.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.model.EquipoEstado
import com.example.appcrud.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState = viewModel.uiState.collectAsState().value

    var showForm by remember { mutableStateOf(false) }
    var equipoEditando by remember { mutableStateOf<Equipo?>(null) }

    var numeroSerie by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var fechaAdquisicion by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf(EquipoEstado.DISPONIBLE) }
    var numeroInventario by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }

    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }

    fun limpiarFormulario() {
        numeroSerie = ""
        marca = ""
        modelo = ""
        fechaAdquisicion = ""
        estadoSeleccionado = EquipoEstado.DISPONIBLE
        numeroInventario = ""
        ubicacion = ""
        equipoEditando = null
    }

    fun onEditarClick(equipo: Equipo) {
        equipoEditando = equipo
        numeroSerie = equipo.numeroSerie
        marca = equipo.marca
        modelo = equipo.modelo
        fechaAdquisicion = equipo.fechaAdquisicion
        estadoSeleccionado = equipo.estado
        numeroInventario = equipo.numeroInventario.toString()
        ubicacion = equipo.ubicacion
        showForm = true
    }

    fun onEliminarClick(equipo: Equipo) {
        equipoAEliminar = equipo
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Equipos de cómputo",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                limpiarFormulario()
                showForm = true
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Agregar equipo")
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }
            uiState.error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.listarEquipos() }) {
                        Text("Reintentar")
                    }
                }
            }
            uiState.equipos.isEmpty() -> {
                Text(text = "No hay equipos registrados", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items = uiState.equipos, key = { it.id }) { equipo ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = equipo.numeroSerie, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "${equipo.marca} ${equipo.modelo}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = equipo.numeroInventario.toString(), style = MaterialTheme.typography.bodySmall)
                                }
                                Column {
                                    Button(onClick = { onEditarClick(equipo) }) {
                                        Text("Editar")
                                    }
                                    Button(onClick = { onEliminarClick(equipo) }) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = { Text(if (equipoEditando == null) "Nuevo equipo" else "Editar equipo") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = numeroSerie,
                            onValueChange = { numeroSerie = it },
                            label = { Text("Número de serie") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = marca,
                            onValueChange = { marca = it },
                            label = { Text("Marca") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = modelo,
                            onValueChange = { modelo = it },
                            label = { Text("Modelo") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = fechaAdquisicion,
                            onValueChange = { fechaAdquisicion = it },
                            label = { Text("Fecha adquisición") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = numeroInventario,
                            onValueChange = { numeroInventario = it },
                            label = { Text("Número de inventario") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = ubicacion,
                            onValueChange = { ubicacion = it },
                            label = { Text("Ubicación") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val equipoDesdeFormulario = Equipo(
                            id = equipoEditando?.id ?: "",
                            numeroSerie = numeroSerie,
                            marca = marca,
                            modelo = modelo,
                            fechaAdquisicion = fechaAdquisicion,
                            estado = estadoSeleccionado,
                            numeroInventario = numeroInventario.toIntOrNull() ?: 0,
                            ubicacion = ubicacion
                        )
                        if (equipoEditando == null) {
                            viewModel.crearEquipo(equipoDesdeFormulario)
                        } else {
                            viewModel.actualizarEquipo(equipoEditando!!.id, equipoDesdeFormulario)
                        }
                        showForm = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    if (equipoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de eliminar ${equipoAEliminar!!.numeroSerie}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.eliminarEquipo(equipoAEliminar!!.id)
                    equipoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}