package com.walter.fireapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.javafaker.Faker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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

@Composable
fun RegForm(
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val db = Firebase.database.getReference("people")
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current


    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val faker = Faker()

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineLocationGranted || coarseLocationGranted) {
                fetchCurrentLocation(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    onSuccess = { lat, lon ->
                        latitude = lat
                        longitude = lon
                        isFetchingLocation = false
                    },
                    onFailure = {
                        isFetchingLocation = false
                        Toast.makeText(context, "Failed to fetch location", Toast.LENGTH_SHORT)
                            .show()
                    })
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

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

        if (isFetchingLocation) {
            Text("Fetching location...", color = MaterialTheme.colorScheme.primary)
        } else if (latitude != null && longitude != null) {
            Text("Location: Lat=${"%.4f".format(latitude)}, Lon=${"%.4f".format(longitude)}")
        } else if (hasLocationPermissions(context) && !isGpsEnabled(context)) {
            Text(
                "GPS is disabled. Enable it for location.",
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) {
                Text("Enable GPS")
            }
        } else if (!hasLocationPermissions(context)) {
            Text(
                "Location permission needed for coordinates.",
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Text("Grant Location Permission")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {

                if (!hasLocationPermissions(context)) {
                    Toast.makeText(
                        context,
                        "Please grant location permission first.",
                        Toast.LENGTH_LONG
                    ).show()
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                    return@OutlinedButton
                }

                if (!isGpsEnabled(context)) {
                    Toast.makeText(
                        context,
                        "Please enable GPS to save location.",
                        Toast.LENGTH_LONG
                    ).show()
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    return@OutlinedButton
                }

                if (name.isNotBlank() && email.isNotBlank() && dob.isNotBlank()) {
                    val student = Student(name, email, dob, latitude, longitude)
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


// Helper function to check permissions
fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// Helper function to check if GPS is enabled
fun isGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}


// Helper function to fetch current location
@SuppressLint("MissingPermission") // Checked by calling function
fun fetchCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onFailure: (String) -> Unit
) {
    if (!isGpsEnabled(context)) {
        onFailure("GPS is disabled.")
        // Optionally prompt user to enable GPS here
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        return
    }

    val cancellationTokenSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY, // Or PRIORITY_BALANCED_POWER_ACCURACY
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onSuccess(location.latitude, location.longitude)
        } else {
            onFailure("Unable to get current location (null).")
        }
    }.addOnFailureListener { exception ->
        onFailure("Location fetch exception: ${exception.message}")
    }
}


data class Student(
    val name: String = "",
    val email: String = "",
    val dob: String = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0
) {
    constructor() : this("", "", "", 0.0, 0.0)
}











