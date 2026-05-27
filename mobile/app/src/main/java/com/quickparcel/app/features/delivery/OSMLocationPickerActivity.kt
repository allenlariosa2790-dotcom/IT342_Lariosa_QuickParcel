package com.quickparcel.app.features.delivery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.quickparcel.app.R
import com.quickparcel.app.databinding.ActivityOsmLocationPickerBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class OSMLocationPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOsmLocationPickerBinding
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var selectedGeoPoint: GeoPoint? = null
    private var selectedAddress: String = ""
    private var isPickup = true
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        binding = ActivityOsmLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isPickup = intent.getBooleanExtra("is_pickup", true)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isPickup) "Select Pickup Location" else "Select Dropoff Location"

        setupMap()
        setupListeners()
        checkLocationPermission()
    }

    private fun setupMap() {
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // CRITICAL: Enable multi-touch controls for zooming and panning
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)

        // Enable scrolling/panning
        mapView.isHorizontalMapRepetitionEnabled = false
        mapView.isVerticalMapRepetitionEnabled = false

        // Set zoom limits
        mapView.minZoomLevel = 5.0
        mapView.maxZoomLevel = 18.0

        // Set zoom to center of Philippines (Manila)
        val center = GeoPoint(14.5995, 120.9842)
        mapView.controller.setZoom(12.0)
        mapView.controller.setCenter(center)

        // Add click listener to map - FIXED to not interfere with panning
        var touchStartX = 0f
        var touchStartY = 0f
        var hasMoved = false

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStartX = event.x
                    touchStartY = event.y
                    hasMoved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(event.x - touchStartX) > 10 || Math.abs(event.y - touchStartY) > 10) {
                        hasMoved = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // Only add marker if user didn't pan/zoom
                    if (!hasMoved) {
                        val projection = mapView.projection
                        val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        addMarker(geoPoint)
                        reverseGeocode(geoPoint)
                    }
                }
            }
            false  // Return false to allow map to continue processing touch events
        }
    }

    private fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            if (selectedGeoPoint != null && selectedAddress.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    putExtra("address", selectedAddress)
                    putExtra("latitude", selectedGeoPoint!!.latitude)
                    putExtra("longitude", selectedGeoPoint!!.longitude)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please tap on the map to select a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarker(geoPoint: GeoPoint) {
        mapView.overlays.removeAll { it is Marker }

        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = if (isPickup) "Pickup Location" else "Dropoff Location"
        mapView.overlays.add(marker)
        mapView.invalidate()

        selectedGeoPoint = geoPoint
        binding.tvCoordinates.text = "Lat: ${String.format("%.6f", geoPoint.latitude)}, Lon: ${String.format("%.6f", geoPoint.longitude)}"
    }

    private fun reverseGeocode(geoPoint: GeoPoint) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressLine = StringBuilder()

                val featureName = address.featureName ?: ""
                val thoroughfare = address.thoroughfare ?: ""
                val subLocality = address.subLocality ?: ""
                val locality = address.locality ?: ""
                val adminArea = address.adminArea ?: ""
                val countryName = address.countryName ?: ""

                if (featureName.isNotEmpty()) addressLine.append(featureName).append(", ")
                if (thoroughfare.isNotEmpty()) addressLine.append(thoroughfare).append(", ")
                if (subLocality.isNotEmpty()) addressLine.append(subLocality).append(", ")
                if (locality.isNotEmpty()) addressLine.append(locality).append(", ")
                if (adminArea.isNotEmpty()) addressLine.append(adminArea).append(", ")
                if (countryName.isNotEmpty()) addressLine.append(countryName)

                selectedAddress = addressLine.toString().trimEnd(',', ' ')
                binding.tvAddress.text = selectedAddress
            } else {
                selectedAddress = "${String.format("%.6f", geoPoint.latitude)}, ${String.format("%.6f", geoPoint.longitude)}"
                binding.tvAddress.text = selectedAddress
            }
        } catch (e: Exception) {
            e.printStackTrace()
            selectedAddress = "${String.format("%.6f", geoPoint.latitude)}, ${String.format("%.6f", geoPoint.longitude)}"
            binding.tvAddress.text = selectedAddress
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            setupMyLocation()
        }
    }

    private fun setupMyLocation() {
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        mapView.overlays.add(myLocationOverlay)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied. You can still tap on the map to select location.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}