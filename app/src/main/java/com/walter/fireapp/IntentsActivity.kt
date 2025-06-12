package com.walter.fireapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walter.fireapp.ui.theme.FireAppTheme

class IntentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FireAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                  MyButtons(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun MyButtons(modifier: Modifier){

    Column(modifier = modifier.padding(horizontal = 12.dp)) {
         Button(onClick = {}) { Text("Call Someone") }
         Button(onClick = {}) { Text("Send Sms") }

    }
}