package com.vsm.mymapstest.ui.maps

import android.Manifest
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.vsm.mymapstest.R
import com.vsm.mymapstest.utils.Permissions


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    private val TAG = this::class.java.simpleName

    //HUAWEI map
    private var hMap: HuaweiMap? = null
    private var mMapView: MapView? = null
    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private lateinit var rootView: View
    private var common = Permissions()
    private lateinit var mContex: Context
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var mLocationRequest: LocationRequest
    private var mLocationCallback: LocationCallback? = null


    private val RUNTIME_PERMISSIONS = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET,
        "android.permission.ACCESS_BACKGROUND_LOCATION"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_first, container, false)
        //location
        //create a fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
//create a settingsClient
        settingsClient = LocationServices.getSettingsClient(activity)
        mLocationRequest = LocationRequest()
// set the interval for location updates, in milliseconds.
// set the interval for location updates, in milliseconds.
        mLocationRequest.interval = 10000
// set the priority of the request
// set the priority of the request
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        Log.i(TAG, "onCreate:hzj")
        //check permissions
        if (!common.hasPermissions(context!!, RUNTIME_PERMISSIONS)) {
            activity?.let { ActivityCompat.requestPermissions(it, RUNTIME_PERMISSIONS, 100) }
        }

        //get mapview instance
        //get mapview instance
        mMapView = rootView.findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView!!.onCreate(mapViewBundle)
        //get map instance
        mMapView!!.getMapAsync(this);

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* view.findViewById<Button>(R.id.button_first).setOnClickListener {
             findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
         }*/
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContex = context
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSSION  failed");
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mMapView!!.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        mMapView?.onStart();
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop();
    }

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdatesWithCallback()
        mMapView?.onDestroy()

    }

    override fun onPause() {
        mMapView?.onPause();
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
        //checkPermission()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory();
    }

    override fun onMapReady(p0: HuaweiMap?) {
        //get map instance in a callback method
        Log.i(TAG, "onMapReady()")
        hMap = p0
        hMap?.setMyLocationEnabled(true);// Enable the my-location overlay.
        hMap?.getUiSettings()?.setMyLocationButtonEnabled(true);// Enable the my-location icon.
        getLocation()

    }

    private fun getLocation() {
        requestLocationUpdatesWithCallback()
        Log.i(TAG, "getLocation()")
        if (null == mLocationCallback) {
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult != null) {
                        val locations =
                            locationResult.locations
                        if (!locations.isEmpty()) {
                            for (location in locations) {
                                Log.i(
                                    TAG,
                                    "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.longitude
                                            + "," + location.latitude + "," + location.accuracy
                                )
                                //move map camera
                                val zoom = 11.0f
                                val latLng1 = LatLng(location.latitude, location.longitude)
                                val cameraUpdate5 =
                                    CameraUpdateFactory.newLatLngZoom(latLng1, zoom);
                                hMap?.animateCamera(cameraUpdate5)
                            }
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (locationAvailability != null) {
                        val flag = locationAvailability.isLocationAvailable
                        Log.i(TAG, "onLocationAvailability isLocationAvailable:$flag")
                    }
                }
            }
        }
    }

    private fun requestLocationUpdatesWithCallback() {
        try {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()
            // check devices settings before request location updates.
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    Log.i(TAG, "check location settings success")
                    // request location updates
                    fusedLocationProviderClient
                        .requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.getMainLooper()
                        )
                        .addOnSuccessListener {
                            Log.i(
                                TAG,
                                "requestLocationUpdatesWithCallback onSuccess"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                TAG,
                                "requestLocationUpdatesWithCallback onFailure:" + e.message
                            )
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "checkLocationSetting onFailure:" + e.message)
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                activity,
                                0
                            )
                        } catch (sie: SendIntentException) {
                            Log.e(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:" + e.message)
        }
    }

    /**
     * remove the request with callback
     */
    private fun removeLocationUpdatesWithCallback() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                .addOnSuccessListener {
                    Log.i(
                        TAG,
                        "removeLocationUpdatesWithCallback onSuccess"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(
                        TAG,
                        "removeLocationUpdatesWithCallback onFailure:" + e.message
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.message)
        }
    }

}
