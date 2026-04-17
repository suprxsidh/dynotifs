package com.suprasidh.dynotifs.ui.onboarding

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.data.model.CalibrationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dataStore = remember { com.suprasidh.dynotifs.data.datastore.DynotifsDataStore(context) }

    var offsetX by remember { mutableFloatStateOf(0.5f) }
    var offsetY by remember { mutableFloatStateOf(0.02f) }
    var width by remember { mutableFloatStateOf(0.30f) }
    var height by remember { mutableFloatStateOf(0.05f) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "Calibrate Island Position",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Adjust the sliders to position where you want the Dynamic Island to appear on your screen.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Horizontal Position: ${(offsetX * 100).toInt()}%")
                Slider(
                    value = offsetX,
                    onValueChange = { offsetX = it },
                    valueRange = 0f..1f
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Vertical Position: ${(offsetY * 100).toInt()}%")
                Slider(
                    value = offsetY,
                    onValueChange = { offsetY = it },
                    valueRange = 0f..0.2f
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Width: ${(width * 100).toInt()}%")
                Slider(
                    value = width,
                    onValueChange = { width = it },
                    valueRange = 0.2f..0.5f
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Height: ${(height * 100).toInt()}%")
                Slider(
                    value = height,
                    onValueChange = { height = it },
                    valueRange = 0.03f..0.1f
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val calibration = CalibrationData(
                            offsetXPercent = offsetX,
                            offsetYPercent = offsetY,
                            widthPercent = width,
                            heightPercent = height
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStore.updateCalibration(calibration)
                        }
                        onCalibrationComplete()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Calibration")
                }
            }

            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val metrics = Point()
            display.getRealSize(metrics)

            val previewX = (metrics.x * offsetX - 90).toInt()
            val previewY = (metrics.y * offsetY + 200).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(previewX, previewY) }
                    .size((metrics.x * width).toInt().coerceAtLeast(60), (metrics.y * height).toInt().coerceAtLeast(30))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}