package org.supersuman.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isPermissionPresent()){
            requestMyPermission{
                accessLocation {
                    requestApi(it)
                }
            }
        }
    }

    private fun requestApi(it: Location) = coroutineScope.launch {
        val response = khttp.get(
            "https://api.openweathermap.org/data/2.5/weather?lat=${it.latitude}&lon=${it.longitude}&appid=${
                getString(R.string.weather_api_key)
            }"
        )
        if (response.statusCode < 400) {
            updateWeatherInfo(response.text)
        }
    }

    private fun isPermissionPresent(): Boolean {
        val permission1 = Manifest.permission.ACCESS_FINE_LOCATION
        val permission2 = Manifest.permission.ACCESS_COARSE_LOCATION
        val res1 = this.checkCallingOrSelfPermission(permission1)
        val res2 = this.checkCallingOrSelfPermission(permission2)
        return res1 == PackageManager.PERMISSION_GRANTED && res2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMyPermission(function: () -> Unit) = coroutineScope.launch{
        val result = Peko.requestPermissionsAsync(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
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