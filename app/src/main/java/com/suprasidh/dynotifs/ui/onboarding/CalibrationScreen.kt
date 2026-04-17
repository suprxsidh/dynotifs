package com.suprasidh.dynotifs.ui.onboarding

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.data.model.CalibrationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CalibrationScreen(
    onCalibrationComplete: () -> Unit
) {
    val context = LocalContext.current
    var offsetX by remember { mutableFloatStateOf(0.5f) }
    var offsetY by remember { mutableFloatStateOf(0.02f) }
    var width by remember { mutableFloatStateOf(0.30f) }
    var height by remember { mutableFloatStateOf(0.05f) }

    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = wm.defaultDisplay
    val metrics = Point()
    display.getRealSize(metrics)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                Text("Calibrate Island Position", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text("Adjust where you want the Dynamic Island to appear.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))

                Text("Horizontal: ${(offsetX * 100).toInt()}%")
                Slider(value = offsetX, onValueChange = { offsetX = it }, valueRange = 0f..1f)
                Spacer(Modifier.height(16.dp))

                Text("Vertical: ${(offsetY * 100).toInt()}%")
                Slider(value = offsetY, onValueChange = { offsetY = it }, valueRange = 0f..0.2f)
                Spacer(Modifier.height(16.dp))

                Text("Width: ${(width * 100).toInt()}%")
                Slider(value = width, onValueChange = { width = it }, valueRange = 0.2f..0.5f)
                Spacer(Modifier.height(16.dp))

                Text("Height: ${(height * 100).toInt()}%")
                Slider(value = height, onValueChange = { height = it }, valueRange = 0.03f..0.1f)

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        val calibration = CalibrationData(offsetX, offsetY, width, height)
                        CoroutineScope(Dispatchers.IO).launch {
                            DynotifsDataStore(context).updateCalibration(calibration)
                        }
                        onCalibrationComplete()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Apply Calibration") }
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(((metrics.x * offsetX) - 90).toInt(), ((metrics.y * offsetY) + 200).toInt()) }
                    .size((metrics.x * width).coerceAtLeast(60).dp, (metrics.y * height).coerceAtLeast(30).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
            )
        }
    }
}