package uk.jamiern.perfhintmandemo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import uk.jamiern.perfhintmandemo.ui.theme.PerfHintManDemoTheme
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    companion object {
        private const val LOGTAG = "MainActivity"
        var result by mutableStateOf("")
    }

    private lateinit var geckoThread: GeckoThread

    private var phmEnabled = false
    private val workloadValues = arrayOf(1500, 3000, 5000)
    private val workloadLabels = arrayOf("Small", "Medium", "Large")
    private var selectedWorkload = 0
    private val budgetValues = arrayOf(5.milliseconds, 10.milliseconds, 15.milliseconds)
    private val budgetLabels = arrayOf("5ms", "10ms", "15ms")
    private var selectedBudget = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOGTAG, "MainActivity.onCreate()")
        super.onCreate(savedInstanceState)
        setContent {
            PerfHintManDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainUi()
                }
            }
        }

        geckoThread = GeckoThread("GeckoThread", this)
        geckoThread.start()
        updateSettings()
    }

    private fun updateSettings() {
        val handler = Handler(geckoThread.looper)
        handler.post {
            geckoThread.updateSettings(
                this, workloadValues[selectedWorkload], phmEnabled, budgetValues[selectedBudget]
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainUi() {
        var workloadExpanded by remember {
            mutableStateOf(false)
        }
        var enabledChecked by remember {
            mutableStateOf(phmEnabled)
        }
        var budgetExpanded by remember {
            mutableStateOf(false)
        }

        Column {
            Row {
                Text(text = "Workload")
                ExposedDropdownMenuBox(expanded = workloadExpanded,
                    onExpandedChange = { workloadExpanded = !workloadExpanded }) {
                    TextField(
                        value = workloadLabels[selectedWorkload],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { TrailingIcon(expanded = workloadExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = workloadExpanded,
                        onDismissRequest = { workloadExpanded = false }) {
                        workloadLabels.forEachIndexed { i, label ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedWorkload = i
                                workloadExpanded = false
                                updateSettings()
                            })
                        }
                    }
                }
            }
            Row {
                Text(text = "Enable PerformanceHintManager")

                Switch(checked = enabledChecked, onCheckedChange = {
                    enabledChecked = it
                    phmEnabled = enabledChecked
                    updateSettings()
                })
            }
            Row {
                Text(text = "Budget")
                ExposedDropdownMenuBox(expanded = budgetExpanded,
                    onExpandedChange = { budgetExpanded = !budgetExpanded }) {
                    TextField(value = budgetLabels[selectedBudget],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { TrailingIcon(expanded = budgetExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = budgetExpanded,
                        onDismissRequest = { budgetExpanded = false }) {
                        budgetLabels.forEachIndexed { i, label ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                selectedBudget = i
                                budgetExpanded = false
                                updateSettings()
                            })
                        }
                    }
                }
            }
            Text(text = "Average frame duration: $result")
        }
    }
}
