package com.example.appcrud.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appcrud.domain.model.Equipo
import com.example.appcrud.domain.model.EquipoEstado
import com.example.appcrud.domain.validation.EquipoValidator
import com.example.appcrud.ui.viewmodel.HomeViewModel
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var equipoEditando by remember { mutableStateOf<Equipo?>(null) }
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }

    LaunchedEffect(Unit) {
        viewModel.listarEquipos()
    }

    LaunchedEffect(uiState.isSessionClosed) {
        if (uiState.isSessionClosed) onLogout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Equipos de cómputo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = viewModel::logout) {
                Text("Cerrar sesión")
            }
        }

        Button(
            onClick = {
                equipoEditando = null
                showForm = true
            },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Agregar equipo")
        }

        uiState.error?.let { error ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::listarEquipos) {
                    Text("Reintentar")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.equipos.isEmpty() && !uiState.isLoading -> {
                    Text("No hay equipos registrados")
                }
                uiState.equipos.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(items = uiState.equipos, key = { it.id }) { equipo ->
                            EquipoCard(
                                equipo = equipo,
                                onEdit = {
                                    equipoEditando = equipo
                                    showForm = true
                                },
                                onDelete = { equipoAEliminar = equipo }
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }

    if (showForm) {
        EquipoFormDialog(
            equipo = equipoEditando,
            isSaving = uiState.isLoading,
            onDismiss = { showForm = false },
            onSave = { equipo ->
                val equipoActual = equipoEditando
                if (equipoActual == null) {
                    viewModel.crearEquipo(equipo)
                } else {
                    viewModel.actualizarEquipo(equipoActual.id, equipo)
                }
                showForm = false
            }
        )
    }

    equipoAEliminar?.let { equipo ->
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Deseas eliminar ${equipo.numeroSerie}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarEquipo(equipo.id)
                        equipoAEliminar = null
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EquipoCard(
    equipo: Equipo,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = equipo.numeroSerie, style = MaterialTheme.typography.titleMedium)
            Text(text = "${equipo.marca} ${equipo.modelo}")
            Text(text = "Inventario: ${equipo.numeroInventario} • ${equipo.estado.toLabel()}")
            Text(text = "Adquisición: ${equipo.fechaAdquisicion}")
            Text(text = "Ubicación: ${equipo.ubicacion}")
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipoFormDialog(
    equipo: Equipo?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Equipo) -> Unit
) {
    var numeroSerie by remember { mutableStateOf(equipo?.numeroSerie.orEmpty()) }
    var marca by remember { mutableStateOf(equipo?.marca.orEmpty()) }
    var modelo by remember { mutableStateOf(equipo?.modelo.orEmpty()) }
    var fechaAdquisicion by remember { mutableStateOf(equipo?.fechaAdquisicion.orEmpty()) }
    var estadoSeleccionado by remember {
        mutableStateOf(equipo?.estado ?: EquipoEstado.DISPONIBLE)
    }
    var numeroInventario by remember {
        mutableStateOf(equipo?.numeroInventario?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var ubicacion by remember { mutableStateOf(equipo?.ubicacion.orEmpty()) }
    var estadoExpandido by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (equipo == null) "Nuevo equipo" else "Editar equipo") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it },
                    label = { Text("Número de serie") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it },
                    label = { Text("Modelo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fechaAdquisicion,
                    onValueChange = {
                        fechaAdquisicion = it
                        formError = null
                    },
                    label = { Text("Fecha de adquisición") },
                    supportingText = { Text("Toca el calendario o escribe: AAAA-MM-DD") },
                    trailingIcon = {
                        IconButton(onClick = { mostrarCalendario = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = numeroInventario,
                    onValueChange = { numeroInventario = it },
                    label = { Text("Número de inventario") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedButton(
                        onClick = { estadoExpandido = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Estado: ${estadoSeleccionado.toLabel()}")
                    }
                    DropdownMenu(
                        expanded = estadoExpandido,
                        onDismissRequest = { estadoExpandido = false }
                    ) {
                        EquipoEstado.entries.forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado.toLabel()) },
                                onClick = {
                                    estadoSeleccionado = estado
                                    estadoExpandido = false
                                }
                            )
                        }
                    }
                }
                formError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validationError = EquipoValidator.validate(
                        numeroSerie = numeroSerie,
                        marca = marca,
                        modelo = modelo,
                        fechaAdquisicion = fechaAdquisicion,
                        numeroInventario = numeroInventario,
                        ubicacion = ubicacion
                    )
                    if (validationError != null) {
                        formError = validationError
                    } else {
                        onSave(
                            Equipo(
                                id = equipo?.id.orEmpty(),
                                numeroSerie = numeroSerie.trim(),
                                marca = marca.trim(),
                                modelo = modelo.trim(),
                                fechaAdquisicion = fechaAdquisicion.trim(),
                                estado = estadoSeleccionado,
                                numeroInventario = numeroInventario.toInt(),
                                ubicacion = ubicacion.trim()
                            )
                        )
                    }
                },
                enabled = !isSaving
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )

    if (mostrarCalendario) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                            fechaAdquisicion = Instant.ofEpochMilli(selectedDateMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                            formError = null
                        }
                        mostrarCalendario = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun EquipoEstado.toLabel(): String {
    return when (this) {
        EquipoEstado.DISPONIBLE -> "Disponible"
        EquipoEstado.EN_USO -> "En uso"
        EquipoEstado.MANTENIMIENTO -> "Mantenimiento"
        EquipoEstado.BAJA -> "Baja"
    }
}
