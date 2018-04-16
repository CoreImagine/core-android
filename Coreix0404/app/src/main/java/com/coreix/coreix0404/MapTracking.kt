package com.coreix.coreix0404

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import java.lang.Math.sin
import java.text.DecimalFormat


class MapTracking : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var email : String
    private lateinit var locationsRef: DatabaseReference
    private var mlatitude : Double = 0.0
    private var mlongitude : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_tracking)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // reference to firebase location
        locationsRef = FirebaseDatabase.getInstance().getReference("Locations")

        //get intent data
        if(intent != null){
            email = intent.getStringExtra("email")
            mlatitude = intent.getDoubleExtra("latitude",0.0)
            mlongitude = intent.getDoubleExtra("longitude",0.0)
        }
        Log.e("[Load]","my location :  Lat "+ mlatitude + " Lng " + mlongitude)
        if(!TextUtils.isEmpty(email)){
            loadLocationForThisUser(email)
        }
    }

    private fun loadLocationForThisUser(email: String) {
        val user_location = locationsRef.orderByChild("email").equalTo(email)
//        Log.e("[Load]","Find the user! " + email)
        user_location.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                for (postSnapshot : DataSnapshot in dataSnapshot!!.children){
                    val tracking : Tracking = postSnapshot.getValue(Tracking::class.java)!!
                    val latitude = tracking.getLatitude().toDouble()
                    val longitude = tracking.getLatitude().toDouble()
//                    if(latitude == null || longitude == null){
//                        Log.e("[Load]", "What could possibility go wrong??")
//                    }else{
//                        Log.e("[Load]", "email" + tracking.getEmail() + " Lat "+ tracking.getLatitude()
//                                + " Lng " + tracking.getLongitude())
//                        Log.e("[Load]", "Gooooood")
                        val freindLocation = LatLng(tracking.getLatitude().toDouble(),
                                tracking.getLongitude().toDouble())

                        //creating location from user coordinates
                        val currentUser = Location("")
                        currentUser.latitude  = latitude
                        currentUser.longitude = longitude

                        //creating location from friend location
                        val friend = Location("")
                        friend.longitude  = tracking.getLongitude().toDouble()
                        friend.latitude = tracking.getLatitude().toDouble()

                        // clear old marker
                        mMap.clear()
                        //creating function to calculate the distance between two users
                        distance(currentUser, friend)

                        // Add friend on the map
                        mMap.addMarker(MarkerOptions()
                                .position(freindLocation)
                                .title(tracking!!.getEmail())
                                .snippet("Distance " + DecimalFormat("#.#").format(distance(currentUser, friend))+ " km")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude , longitude ), 12.0f))
//                    }
                }
                //create marker for current user
                val current = LatLng(mlatitude, mlongitude)
                mMap.addMarker(MarkerOptions().position(current).title(FirebaseAuth.getInstance().currentUser!!.email))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mlatitude , mlongitude), 14.0f))
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun distance(currentUser: Location, friend: Location): Double {
        val theta = currentUser.longitude - friend.longitude
        var dist: Double? = (sin(deg2rad(currentUser.latitude))
                * sin(deg2rad(friend.latitude))
                * Math.cos(deg2rad(currentUser.latitude))
                * Math.cos(deg2rad(friend.latitude))
                * Math.cos(deg2rad(theta)))
        dist = Math.acos(dist!!)
        dist = rad2deg(dist)
        dist *= 60.0 * 1.1515
        return dist
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private fun rad2deg(rad: Double?): Double {
        return rad!! * 180.0 / Math.PI
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
