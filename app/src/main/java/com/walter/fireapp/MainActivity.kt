package com.walter.fireapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.javafaker.Faker
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.walter.fireapp.ui.theme.FireAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FireAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegForm(innerPadding)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegForm(innerPadding: PaddingValues = PaddingValues(0.dp)) {
    val db = Firebase.database.getReference("people")
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val faker = Faker()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    )
    {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth") },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && dob.isNotBlank()) {
                    val student = Student(name, email, dob)
                    isLoading = true
                    db.push().setValue(student).addOnSuccessListener {
                        isLoading = false
                        name = ""
                        email = ""
                        dob = ""
                        Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val time = faker.date().birthday().time
                    val date = Date(time)
                    val formatter = SimpleDateFormat("d-M-y", Locale.getDefault())

                    name = faker.name().fullName()
                    email = faker.internet().emailAddress()
                    dob = formatter.format(date)
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                isLoading -> Text("Saving ....")
                else -> Text("Register")
            }
        }

        Button(
            onClick = {
                val intent = Intent(context, UsersActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(1.dp)
        ) { Text("Show Data") }
    }
}

data class Student(val name: String = "", val email: String = "", val dob: String = "")












