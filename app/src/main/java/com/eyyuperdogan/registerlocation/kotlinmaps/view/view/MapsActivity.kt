package com.eyyuperdogan.registerlocation.kotlinmaps.view.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.eyyuperdogan.registerlocation.kotlinmaps.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.eyyuperdogan.registerlocation.kotlinmaps.databinding.ActivityMapsBinding
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.model.Place
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.roomdb.PlaceDao
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissonLauncher:ActivityResultLauncher<String>
    private  var selectedLatitude:Double?=null
    private var selectedLogitude:Double?=null
    private lateinit var db:PlaceDatabase
    private lateinit var placeDao: PlaceDao
    private lateinit var place: Place
    var compositeDisposable= CompositeDisposable()
    var placeFromMain:Place?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerLauncher()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        selectedLatitude=0.0
        selectedLogitude=0.0

        //veri tabanı oluşturuldu
        db=Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Plases").build()
        placeDao=db.placeDao()






    }



    override fun onMapReady(googleMap: GoogleMap) {

            mMap = googleMap
            mMap.setOnMapLongClickListener(this)//haritayla arasıdaki bağı kurmak
            //latitude=enlem , longitude=boylam
            //CASTİNG(location maneger olduğuna eminim)

            var info=intent.getStringExtra("info")
        if (info=="new") {
            binding.saveButton.isEnabled=false
            binding.saveButton.visibility=View.VISIBLE
            binding.deleteButton.visibility=View.GONE
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(p0: Location) {
                    var userLocation = LatLng(p0.latitude, p0.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    intent.putExtra("latitude", p0.longitude)
                    intent.putExtra("logtitude", p0.longitude)
                }
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin vermedikten sonra birdaha izin istiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    Snackbar.make(
                        binding.root,
                        "permisson needed for location",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give permisson") {
                        //request permisson(izin istemek)
                        permissonLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }.show()
                } else {
                    //request permisson(izin istemek)
                    permissonLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                //not granted(izin verilmedi)
            } else {
                //granted(izin verildi)
                //konum belirleme
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.FUSED_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )
                    mMap.isMyLocationEnabled = true
                }
            }
        }
        else{
            mMap.clear()
           placeFromMain=intent.getSerializableExtra("place") as Place
         //placeFromMain null değil ise
            placeFromMain?.let {
                var latLng=LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))

                binding.placeNameText.setText(it.name)
                binding.deleteButton.visibility=View.VISIBLE
                binding.saveButton.visibility=View.GONE

            }

        }




    }

    private fun registerLauncher(){
        permissonLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result->
            if (result)
            {
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                {
                    //granted(izin verildi)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10f, locationListener)
                    mMap.isMyLocationEnabled=true
                }
            }
            else
            {
                //not granted(izin verilmedi)
                Toast.makeText(applicationContext,"Permisson needed!",Toast.LENGTH_LONG).show()
            }

        }
    }


    //add marker ekleme
    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude=p0.latitude
        selectedLogitude=p0.longitude
        binding.saveButton.isEnabled=true
        
    }

    fun save(view: View){
        var name=binding.placeNameText.text.toString()
        if (name.isNotEmpty()) {
            place = Place(name, selectedLatitude!!, selectedLogitude!!)
            Toast.makeText(applicationContext, "kayıt başarılı", Toast.LENGTH_LONG).show()
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())//arkaplanda çalıştır
                    .observeOn(AndroidSchedulers.mainThread())//main threade gözlemle
                    .subscribe(this::hendleReponse)
            )
        }
        else
        {
            Toast.makeText(applicationContext, "lütfen bir yer giriniz!", Toast.LENGTH_LONG).show()

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun hendleReponse()
    {
     val intent=Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
    fun  delete(view: View){
        placeFromMain?.let {place->
            compositeDisposable.add(
                placeDao.delete(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::hendleReponse)
            )
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}