package com.wunder.rental.ui.map


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ncorti.slidetoact.SlideToActView
import com.wunder.rental.R
import com.wunder.rental.databinding.FragmentMapViewBinding
import com.wunder.rental.model.CarDetail
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class MapViewFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private lateinit var mLocationCallback: LocationCallback
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private var _binding: FragmentMapViewBinding? = null
    private val viewModel: MapViewViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapViewBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        initializeLocationCallBack()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).apply {
            this.getMapAsync(this@MapViewFragment)
        }
        bottomSheetBehavior = BottomSheetBehavior.from(binding.mapBottomBar.bottomSheet)
        checkForPermissionsAndUpdateMap()
        observeViewModel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        viewModel.fetchCars()
        mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        mMap?.setOnMarkerClickListener {
            viewModel.fetchCarDetail(it.tag as Int)
            false
        }
        if (checkLocationPermission() && isGpsProviderEnable()) {
            updateMap()
        }
    }

    private fun observeViewModel() {
        viewModel.carList.observe(viewLifecycleOwner, {
            for (car in it) {
                addMarker(car)
            }
        })
        viewModel.carDetails.observe(viewLifecycleOwner, {
            mMap?.clear()
            addMarker(it)
            updateBottomSheet(it)
        })
        viewModel.isBooked.observe(viewLifecycleOwner, {
            if (it) {
                updateSwipeButtonToLock()
            }
        })
        viewModel.error.observe(viewLifecycleOwner, {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })
    }

    private fun addMarker(carDetail: CarDetail) {
        carDetail.lat?.let { it1 -> carDetail.lon?.let { it2 -> LatLng(it1, it2) } }?.let { it2 ->
            MarkerOptions().position(it2).title(carDetail.title)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        getBitmapFromView(
                            getCustomMarkerView(
                                carDetail
                            )
                        )
                    )
                )
        }?.let { it3 ->
            mMap?.addMarker(
                it3
            )?.setTag(carDetail.carId)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateMap() {
        if (isGpsProviderEnable() && checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                mLocationCallback,
                Looper.getMainLooper()
            )
            mMap?.isMyLocationEnabled = true
        }
    }

    private fun updateBottomSheet(carDetail: CarDetail) {
        binding.mapBottomBar.bottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.mapBottomBar.tvBattery.text = carDetail.fuelLevel?.toString().plus("%")
        binding.mapBottomBar.tvParkingPrice.text = carDetail.pricingParking
        binding.mapBottomBar.tvAddress.text =
            resources.getString(
                R.string.address,
                carDetail.address,
                carDetail.city,
                carDetail.zipCode
            )
        Glide.with(requireContext())
            .load(carDetail.vehicleTypeImageUrl)
            .circleCrop()
            .into(binding.mapBottomBar.ivVehicle)
        binding.mapBottomBar.btSlide.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    viewModel.requestQuickRent(carDetail.carId.toString())
                }
            }
    }

    private fun updateSwipeButtonToLock() {
        binding.mapBottomBar.btSlide.resetSlider()
        binding.mapBottomBar.btSlide.isReversed = true
        binding.mapBottomBar.btSlide.isLocked = true
        binding.mapBottomBar.btSlide.text = getString(R.string.swipe_left_to_lock)
    }

    private fun getCustomMarkerView(carDetail: CarDetail): View {
        val customMarkerView =
            (requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.map_marker,
                null
            )
        customMarkerView.findViewById<AppCompatTextView>(R.id.tvFuelIndicator).text =
            carDetail.fuelLevel.toString().plus("%")
        return customMarkerView
    }

    private fun initializeLocationCallBack() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude
                        ), 16f
                    )
                )
            }
        }
    }

    private fun checkForPermissionsAndUpdateMap() {
        if (!checkLocationPermission()) {
            locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    updateMap()
                }
            }
            requestLocationPermission(locationPermissionRequest)
        }
        if (!isGpsProviderEnable()) {
            askForGPSAccess()
        }
    }

    override fun onLocationChanged(location: Location) {
        updateMap()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}