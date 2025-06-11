package com.walter.fireapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    People()
                }
            }
        }
    }
}

@Composable
fun People(){
      var peoplesList by remember { mutableStateOf<List<Student>>(emptyList()) }
      var isLoading by remember { mutableStateOf(true) }
      val db = Firebase.database.getReference("people")
      LaunchedEffect(Unit) {
          db.addValueEventListener(object: ValueEventListener{
              override fun onDataChange(snapshot: DataSnapshot) {
                  val list = mutableListOf<Student>()
                  for(item in snapshot.children){
                      val student = item.getValue(Student::class.java)
                      list.add(student!!)
                  }
                  peoplesList = list
                  isLoading = false
              }

              override fun onCancelled(error: DatabaseError) {
                  isLoading=false
              }
          })
      }

      when{
          isLoading -> CircularProgressIndicator()
          peoplesList.isEmpty()-> Text("No data found")
          else->{
              LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
                  items(items = peoplesList, itemContent = {
                      Text(it.name, fontWeight = FontWeight.Bold, color = Color.Black)
                      Text(it.email)
                      Text(it.dob)
                      HorizontalDivider()
                  })
              }
          }
     }

}









