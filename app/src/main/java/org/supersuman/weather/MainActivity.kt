package org.supersuman.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var celsiusTextView : MaterialTextView
    private lateinit var placeTextView : MaterialTextView
    private lateinit var progressIndicator : CircularProgressIndicator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        modifyViews()

        if (!isPermissionPresent()){
            requestMyPermission{
                accessLocation {
                    requestApi(it)
                }
            }
        } else{
            accessLocation {
                requestApi(it)
            }
        }
    }

    private fun initViews() {
        celsiusTextView = findViewById(R.id.textView2)
        progressIndicator = findViewById(R.id.progressBar)
        placeTextView = findViewById(R.id.textView3)
    }

    private fun modifyViews() {
        progressIndicator.isIndeterminate = true
        celsiusTextView.visibility = View.INVISIBLE
        placeTextView.visibility = View.INVISIBLE
    }

    private fun requestApi(it: Location) = coroutineScope.launch {
        delay(500)
        val response = khttp.get(
            "https://api.openweathermap.org/data/2.5/weather?lat=${it.latitude}&lon=${it.longitude}&appid=${
                getString(R.string.weather_api_key)
            }"
        )
        if (response.statusCode < 400) {
            println("Updating Information Successfully ")
            updateWeatherInfo(response.text)
        }
    }


    private fun updateWeatherInfo(jsonString: String) = coroutineScope.launch(Dispatchers.Main) {
        val json = JSONObject(jsonString)
        val temp = json.getJSONObject("main").getDouble("temp") -273.15
        val temperature = String.format("%.1f", temp) + "° Celsius"
        val name = json.getString("name")

        celsiusTextView.text = temperature
        placeTextView.text = name
        progressIndicator.isIndeterminate = false
        celsiusTextView.visibility = View.VISIBLE
        placeTextView.visibility = View.VISIBLE
    }

    private fun isPermissionPresent(): Boolean {
        val permission1 = Manifest.permission.ACCESS_FINE_LOCATION
        val permission2 = Manifest.permission.ACCESS_COARSE_LOCATION
        val res1 = this.checkCallingOrSelfPermission(permission1)
        val res2 = this.checkCallingOrSelfPermission(permission2)
        return res1 == PackageManager.PERMISSION_GRANTED && res2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMyPermission(function: () -> Unit) = coroutineScope.launch {
        val result = Peko.requestPermissionsAsync(this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        if (result is PermissionResult.Granted){
            function()
        }
        else{
            Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun accessLocation(function: (it: Location) -> Unit){
        val mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.lastLocation
            .addOnSuccessListener {
                function(it)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}