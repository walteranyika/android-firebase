package com.walter.fireapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.walter.fireapp.ui.theme.FireAppTheme

class UsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FireAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShowUsers(innerPadding)
                }
            }
        }
    }
}

@Preview
@Composable
fun ShowUsers(innerPadding: PaddingValues = PaddingValues(16.dp)) {
    var usersList by remember { mutableStateOf<List<Student>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var selectedUser by remember { mutableStateOf<Student?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    val db = Firebase.database.getReference("people")

    LaunchedEffect(Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<Student>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Student::class.java)
                    user?.let { users.add(it) }
                }
                usersList = users
                isLoading = false
                errorMessage = ""
            }

            override fun onCancelled(error: DatabaseError) {
                errorMessage = "Failed to load ${error.message}"
                isLoading = false
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != "" -> {
                Text(errorMessage)
            }

            usersList.isEmpty() -> {
                Text("No users found")
            }
            //ssh-keygen -t ed25519 -C "walteranyika@gmail.com"

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(usersList) { user ->
                        Card(onClick = {
                            selectedUser = user
                            showDialog = true
                        }, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    user.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(
                                        0xFF3F51B5
                                    )
                                )
                                Text(user.email, fontSize = 12.sp)
                                Text(user.dob, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedUser != null) {
        UserDetailsDialog(student = selectedUser!!, onDismiss = {
            showDialog = false
            selectedUser = null
        })
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsDialog(student: Student, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("User Details") }, text = {
        Column {
            Text("Name: ${student.name}")
            Text("Email: ${student.email}")
            Text("DOB: ${student.dob}")
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Ok") } })
}