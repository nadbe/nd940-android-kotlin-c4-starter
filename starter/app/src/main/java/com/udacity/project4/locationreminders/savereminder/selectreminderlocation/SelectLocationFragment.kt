package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*

import org.koin.android.ext.android.inject
import java.util.*

private const val TAG = "SelectLocationFragment"


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<String>

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var map: GoogleMap

    private var currentMarker = MarkerOptions()
    private var currentPOI: PointOfInterest? = null

    private var permissionManager = PermissionManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        val mapFragment = childFragmentManager.findFragmentById(R.id.mapsFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveButton.setOnClickListener { onLocationSelected() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (!permission) {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }  else {
                enableCurrentLocation()
            }
        }
        requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun onLocationSelected() {
        if (currentMarker.position == null) {
            _viewModel.showSnackBarInt.value = R.string.err_select_location
        } else if (currentMarker.position != null) {
            _viewModel.longitude.value = currentMarker.position.longitude
            _viewModel.latitude.value = currentMarker.position.latitude
            _viewModel.reminderSelectedLocationStr.value = currentMarker.title
            _viewModel.selectedPOI.value = currentPOI
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapStyle(map)
        setPoiClick(map)
        setMapClick(map)

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun enableCurrentLocation() {
        if (permissionManager.isLocationPermissionGranted(requireContext())) {
            map.isMyLocationEnabled = true
            zoomToCurrentLocation()
            showInstructionDialog()
        }
    }


    @SuppressLint("MissingPermission")
    private fun zoomToCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener { location: Location? ->
            if (location != null) {
                var zoomLevel = 16f
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), zoomLevel)
                )
            }
        }
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_styled
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            currentPOI = poi
            currentMarker.position(poi.latLng)
            currentMarker.title(poi.name)
            val poiMarker = map.addMarker(currentMarker)
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { position ->
            currentMarker.position(LatLng(position.latitude, position.longitude))
            currentMarker.title(String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", position.latitude, position.longitude))
            val poiMarker = map.addMarker(currentMarker)
            poiMarker?.showInfoWindow()
        }
    }

    private fun showInstructionDialog() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it, R.style.AlertDialogCustom)
            builder.apply {
                setNeutralButton(
                    R.string.ok
                ) { dialog, id ->
                    dialog.cancel()
                }
                setTitle(R.string.select_poi)
            }.create()
        }
        alertDialog?.show()
    }

}
