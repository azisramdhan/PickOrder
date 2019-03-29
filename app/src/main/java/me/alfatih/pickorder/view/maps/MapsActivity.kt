package me.alfatih.pickorder.view.maps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import me.alfatih.pickorder.R
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener, MapsView,
    PlaceSelectionListener {
    private var mapsPresenter:MapsPresenter? = null
    private var address:String = ""

    private lateinit var mMap: GoogleMap
    lateinit var mapFragment:SupportMapFragment
    lateinit var mLocationRequest: LocationRequest
    private var mCurrentLocationMarker: Marker? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var mLastLocation:Location

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val locationList = locationResult!!.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                mLastLocation = location

                if (mCurrentLocationMarker != null) {
                    mCurrentLocationMarker!!.remove()
                }

                val latLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title(getString(R.string.loading))
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                markerOptions.draggable(true)
                mCurrentLocationMarker = mMap.addMarker(markerOptions)
                mMap.setOnMarkerDragListener(this@MapsActivity)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                mCurrentLocationMarker?.showInfoWindow()
                mCurrentLocationMarker?.let {
                    mapsPresenter?.getAddress(it)
                }

            }

        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapsPresenter = MapsPresenter(this, this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // https://stackoverflow.com/questions/54965020/apiexception-9003-places-api-access-not-configured
        // Initialize Places.

        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        var client = Places.createClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME))
        autocompleteFragment.setOnPlaceSelectedListener(this)

    }

    override fun onPlaceSelected(p0: Place) {
        //mCurrentLocationMarker?.position = p0.latLng
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p0.latLng, 16f))

    }

    override fun onError(p0: Status) {
    }

    override fun onPause() {
        super.onPause()
        if(mFusedLocationProviderClient != null){
            mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 120000
        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("ANDROID N", "check permission - granted")
                mFusedLocationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = true
            } else {
                Log.d("ANDROID_N", "check permission - not granted")
                checkLocationPermission()
            }
        } else {
            mFusedLocationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            mMap.isMyLocationEnabled = true
        }

        mMap.setOnMyLocationButtonClickListener {
                mFusedLocationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = true
            true
        }
        mMap.setOnInfoWindowClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("result", address)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        mFusedLocationProviderClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        Log.d("ANDROID_N", "Check location permission")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("ANDROID_N", "Check location permission granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("ANDROID_N", "should request permission rationale")
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        ActivityCompat.requestPermissions(
                            this@MapsActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                Log.d("ANDROID_N", "Check location permission - not granted")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun showData(result: String) {
        mCurrentLocationMarker!!.title = result
        mCurrentLocationMarker?.showInfoWindow()
        address = result
        // toast(result)
    }

    override fun showLoading() {
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        val position = p0!!.position

        Log.d(
            javaClass.simpleName,
            String.format(
                "Dragging to %f:%f", position.latitude,
                position.longitude
            )
        )
        mCurrentLocationMarker?.let {
            mapsPresenter?.getAddress(it)
        }
    }

    override fun onMarkerDragStart(p0: Marker?) {
        val position = p0!!.position

        Log.d(
            javaClass.simpleName,
            String.format(
                "Dragging to %f:%f", position.latitude,
                position.longitude
            )
        )    }

    override fun onMarkerDrag(p0: Marker?) {
        val position = p0!!.position

        Log.d(
            javaClass.simpleName,
            String.format(
                "Dragging to %f:%f", position.latitude,
                position.longitude
            )
        )
    }
}
