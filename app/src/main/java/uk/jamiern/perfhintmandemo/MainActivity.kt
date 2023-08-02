package uk.jamiern.perfhintmandemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.jamiern.perfhintmandemo.ui.theme.PerfHintManDemoTheme

class MainActivity : ComponentActivity() {

    private lateinit var geckoThread: GeckoThread

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOGTAG, "MainActivity.onCreate()")
        super.onCreate(savedInstanceState)
        setContent {
            PerfHintManDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        geckoThread = GeckoThread("GeckoThread", this)
        geckoThread.start()
    }

    companion object {
        private const val LOGTAG = "MainActivity"
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PerfHintManDemoTheme {
        Greeting("Android")
    }
}