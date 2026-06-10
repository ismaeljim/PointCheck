package com.pointcheck.core.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointcheck.features.profile.presentation.DayConfig

/**
 * Barra superior de la aplicación (TopAppBar).
 * 
 * Configurada con el estilo visual de Material3, soporta navegación hacia atrás
 * y acciones personalizadas en el extremo derecho.
 *
 * @param title Título a mostrar en la barra.
 * @param onBack Acción opcional para el botón de retroceso. Si es null, no se muestra el botón.
 * @param actions Composable con las acciones adicionales (iconos de acción).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Campo de selección estilizado tipo tarjeta (Card).
 * 
 * Utilizado para flujos donde el usuario debe elegir una opción de una lista (ej: categorías, servicios).
 * Presenta un diseño de objetivo táctil amplio optimizado para dispositivos móviles.
 *
 * @param label Etiqueta descriptiva sobre el campo.
 * @param value Texto que representa la opción seleccionada actualmente.
 * @param icon Icono descriptivo a la izquierda.
 * @param onClick Acción a ejecutar al presionar el campo.
 * @param modifier Modificador de Compose para personalización de layout.
 * @param enabled Define si el campo está activo para interacción.
 * @param isError Indica si el campo debe mostrar un estado de error visual.
 */
@Composable
fun AppSelectorField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedCard(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder(enabled).run {
                if (isError) copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)) else this
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Fila de configuración para un día de la semana.
 * 
 * Permite activar/desactivar el día y seleccionar el rango horario (inicio/fin)
 * mediante un selector nativo (TimePickerDialog).
 *
 * @param dayName Nombre del día (ej: "Lunes").
 * @param config Configuración actual del día (estado activo y horas).
 * @param onConfigChange Callback para notificar cambios en la configuración.
 * @param enabled Define si la fila es interactuable.
 */
@Composable
fun DayScheduleRow(
    dayName: String,
    config: DayConfig,
    onConfigChange: (DayConfig) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = config.isActive,
                    onCheckedChange = { if (enabled) onConfigChange(config.copy(isActive = it)) },
                    enabled = enabled
                )
                Text(dayName, style = MaterialTheme.typography.bodyMedium)
            }

            if (config.isActive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = {
                            if (enabled) {
                                val time = config.start.split(":")
                                android.app.TimePickerDialog(context, { _, h, m ->
                                    onConfigChange(config.copy(start = "%02d:%02d".format(h, m)))
                                }, time[0].toInt(), time[1].toInt(), true).show()
                            }
                        },
                        enabled = enabled
                    ) {
                        Text(config.start)
                    }
                    Text("-", style = MaterialTheme.typography.bodyMedium)
                    TextButton(
                        onClick = {
                            if (enabled) {
                                val time = config.end.split(":")
                                android.app.TimePickerDialog(context, { _, h, m ->
                                    onConfigChange(config.copy(end = "%02d:%02d".format(h, m)))
                                }, time[0].toInt(), time[1].toInt(), true).show()
                            }
                        },
                        enabled = enabled
                    ) {
                        Text(config.end)
                    }
                }
            } else {
                Text(
                    "No disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * Botón principal de la aplicación con soporte para estado de carga.
 *
 * @param text Texto a mostrar en el botón.
 * @param onClick Acción al presionar.
 * @param modifier Modificador para el layout.
 * @param enabled Estado de habilitación.
 * @param isLoading Si es true, muestra un indicador de carga en lugar del texto.
 * @param containerColor Color de fondo del botón.
 * @param contentColor Color del contenido (texto/icono).
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Botón secundario con estilo Outlined.
 */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(16.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Campo de texto estándar configurado con el estilo de la aplicación.
 *
 * @param value Valor actual del texto.
 * @param onValueChange Callback para cambios en el texto.
 * @param label Etiqueta flotante.
 * @param leadingIcon Icono opcional al inicio.
 * @param trailingIcon Composable opcional al final (ej: botón para ver contraseña).
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    maxLines: Int = if (minLines > 1) Int.MAX_VALUE else 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.primary) } },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        minLines = minLines,
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
