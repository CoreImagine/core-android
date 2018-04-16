package com.coreix.coreix0404

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.MenuView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter

import kotlinx.android.synthetic.main.activity_list_online.*
import android.util.Log
import android.view.*
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.LocationSettingsRequest.Builder
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.*
import kotlinx.android.synthetic.main.user_layout.view.*
import javax.security.auth.callback.Callback


class ListOnline : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    // Firebase variables
    var adapter : FirebaseRecyclerAdapter<User,ListOnlineViewHolder> ?= null
    lateinit var locationRef : DatabaseReference
    lateinit var onlineRef : DatabaseReference
    lateinit var currentUserRef : DatabaseReference
    lateinit var counterRef : DatabaseReference
    lateinit var listOnline : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager
    lateinit var mLocationRequest : LocationRequest
    lateinit var googleApi : GoogleApiAvailability
    lateinit var mGoogleApiClient : GoogleApiClient
    lateinit var mLastLocation : Location
    lateinit var builder : LocationSettingsRequest.Builder
    lateinit var mLocationSettingsRequest : LocationSettingsRequest
    lateinit var mSettingsClient: SettingsClient
    private lateinit var mFusedLocation : FusedLocationProviderClient
    private val MY_PERMISSION_REQUEST_CODE = 7171
    private val PLAY_SERVICES_REQUEST = 7172
    private val UpdateInterval :Long = 3000
    private val FastInterval :Long  = 2000
    private val Distance :Float = 10f

    override fun onStart() {
        super.onStart()
//        if(checkPlayServices()) mGoogleApiClient.connect()
        adapter?.startListening()
        if(mGoogleApiClient != null) mGoogleApiClient.connect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_online)
        listOnline = findViewById(R.id.recycleView)
        listOnline.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        listOnline.layoutManager = layoutManager
        toolbar.title = "Coreix User"
        setSupportActionBar(toolbar)
        //Firebase
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline")
        currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline")
                .child(FirebaseAuth.getInstance().currentUser!!.uid) // create new child in lastOnline with key is uid
        locationRef = FirebaseDatabase.getInstance().getReference("Locations")

        // Location Request
//        createLocationRequest()

        //Location
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("[Main]", "Not get granted info")
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    ),MY_PERMISSION_REQUEST_CODE)
        }
        else
        {
            if(checkPlayServices()){
                Log.e("[Main]","Building Connection")
                buildGoogleApiClient()
                Log.e("[Main]","Location request")
                createLocationRequest()
                Log.e("[Main]","Display")
                displayLocation()
            }
        }

        //Firebase
        setupSystem()

        // After setup system, we load all user from counterRef and display on RecyclerView
        updateList()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Open Google Map", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        run@when(requestCode){
            MY_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient()
                        createLocationRequest()
                        displayLocation()
                    }
                }
                return@run
            }
        }
    }


    private fun checkPlayServices(): Boolean{
        Log.e("[Check]","called")
        googleApi = GoogleApiAvailability.getInstance()
        val resultCode = googleApi.isGooglePlayServicesAvailable(this)
        if(resultCode != ConnectionResult.SUCCESS) {
            Log.e("[Check]","not connected")
            if (googleApi.isUserResolvableError(resultCode))
            {
                googleApi.getErrorDialog(this,resultCode, PLAY_SERVICES_REQUEST).show()
            }
            else
            {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        Log.e("[Check]","connected")
        return true
    }

    private fun buildGoogleApiClient(){
        configGoogleApiClient()
        Log.e("[Connection]","Ready to connect...")
        mGoogleApiClient.connect()
        Log.e("[Connection]","Finish Connected")
    }

    private fun configGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        Log.e("[init]","Google Api Client finished")
    }

    private fun createLocationRequest(){
        mLocationRequest = LocationRequest().apply {
            interval = UpdateInterval
            fastestInterval = FastInterval
            smallestDisplacement = Distance
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun displayLocation(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("[Display]","Return statement")
            return
        }
        Log.e("[Display]", "get Last location")
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
//        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
//        if(mFusedLocation.locationAvailability != null){
//            //update firebase
//            mLastLocation != mFusedLocation.lastLocation
//            locationRef.child(FirebaseAuth.getInstance().currentUser!!.uid).
//                    setValue(Tracking(FirebaseAuth.getInstance().currentUser?.email.toString(),
//                    FirebaseAuth.getInstance().currentUser!!.uid,
//                            mLastLocation.latitude as String, mLastLocation.longitude as String))
//            Log.e("[Display]","The Location object is created")
//            Log.e("[Display]","Firebase Updated")
//        }
//        else {
//            Toast.makeText(this,"Couldn't get the location", Toast.LENGTH_SHORT).show()
//        }
        mFusedLocation.lastLocation.addOnSuccessListener(this, { location ->
            if(location != null){
                mLastLocation = location
                locationRef.child(FirebaseAuth.getInstance().currentUser!!.uid).
                    setValue(Tracking(FirebaseAuth.getInstance().currentUser?.email.toString(),
                    FirebaseAuth.getInstance().currentUser!!.uid,
                            mLastLocation.latitude.toString(), mLastLocation.longitude.toString()))
                Log.e("[Display]","The Location object is created")
                Log.e("[Display]","Firebase Updated")
            }
            else
            {
                Toast.makeText(this,"Couldn't get the location", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        displayLocation()
    }

    override fun onConnected(p1: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        startLocationUpdates()
        displayLocation()
    }
//    deprecated function, this function is replaced by displayLocation update
//    private fun startLocationUpdates_deprecated(){
//        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            Log.e("[LocationU]","Return statement")
//            return
//        }
//        Log.e("[LocationU]","Location Updated")
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this as com.google.android.gms.location.LocationListener)
//    }
    private fun startLocationUpdates() {
        // mLocationRequest is the location request
        // Create LocationSettingsRequest object using location request
        builder = LocationSettingsRequest.Builder().apply {
            this.addLocationRequest(mLocationRequest)
            mLocationSettingsRequest = this.build()
        }
        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        //reference here https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
        var mSC = LocationServices.getSettingsClient(this)
        mSC.checkLocationSettings(mLocationSettingsRequest)
        mSettingsClient = mSC
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        var mLocationCallback:LocationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult : LocationResult) {
                // do work here
                onLocationChanged(locationResult.lastLocation)
            }
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }else{
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper())
        }
    }

    private fun updateList(){
        var userOptions = FirebaseRecyclerOptions.Builder<User>()
        adapter = object : FirebaseRecyclerAdapter<User, ListOnlineViewHolder>(userOptions
                .setQuery(counterRef,User::class.java).build()){
            override fun onBindViewHolder(holder: ListOnlineViewHolder, position: Int, model: User) {
                if(model.getEmail().equals(FirebaseAuth.getInstance().currentUser!!.email)){
                    holder.txtEmail.text = model.getEmail() + "(me)"
                }else {
                    holder.txtEmail.text = model.getEmail()
                }
                holder?.itemView.setOnClickListener{
                    Toast.makeText(baseContext,itemCount.toString(),Toast.LENGTH_LONG).show()
                    if(model.getEmail() != FirebaseAuth.getInstance().currentUser!!.email)
                    {
                        Toast.makeText(baseContext,model.getEmail()+" is on the map",Toast.LENGTH_LONG).show()
                         val map = Intent(baseContext, MapTracking::class.java)
                         map.putExtra("email",model.getEmail())
                         map.putExtra("latitude",mLastLocation.latitude)
                         map.putExtra("longitude",mLastLocation.longitude)
                         startActivity(map)
                    }
                }
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOnlineViewHolder {
                return ListOnlineViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.user_layout, parent, false))
            }
        }
        adapter!!.notifyDataSetChanged()
        listOnline.adapter = adapter
    }

    override fun onStop() {
        if(mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
        adapter?.stopListening()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        if(mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.e("[Susp]","Connection Suspended")
        mGoogleApiClient.connect()
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    private fun setupSystem(){
        onlineRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.getValue(Boolean::class.java)!!) {
                        currentUserRef.onDisconnect().removeValue()
                        // set user in list
                        counterRef.child(FirebaseAuth.getInstance().currentUser?.uid)
                                .setValue(User(FirebaseAuth.getInstance().currentUser?.email.toString(),"Online"))
                        adapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        counterRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot!!.children.forEach { postSnapshot ->
                    val user = postSnapshot.getValue(User::class.java)
                    Log.d("LOG", "" + user!!.getEmail() + " is " + user.getStatus())
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?):Boolean {
        run@ when (item!!.itemId) {
            R.id.action_join -> {
                Log.e("[Debug]","Action join")
                counterRef.child(FirebaseAuth.getInstance().currentUser?.uid)
                        .setValue(User(FirebaseAuth.getInstance().currentUser?.email.toString(), "Online"))
                return@run
            }
            R.id.action_logout -> {
                Log.e("[Debug]","Action logout")
                currentUserRef.removeValue()
                return@run
            }
            else -> {
                super.onOptionsItemSelected(item)
                return@run
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult){
        val errorCode = connectionResult.errorCode
        // Device doesn't install Google Play Service
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show()
        }
    }
}
