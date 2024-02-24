package com.enmanuelbergling.pathpower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.enmanuelbergling.pathpower.ui.canvas.AnimatedWavesPreview
import com.enmanuelbergling.pathpower.ui.list.LazyBeeLayoutPrev
import com.enmanuelbergling.pathpower.ui.list.LazyBeeList
import com.enmanuelbergling.pathpower.ui.list.LazyBeeListAdaptive
import com.enmanuelbergling.pathpower.ui.list.LazyBeeListFixed
import com.enmanuelbergling.pathpower.ui.theme.PathPowerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PathPowerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyBeeLayoutPrev()
                }
            }
        }
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
    PathPowerTheme {
        Greeting("Android")
    }
}