package com.eyyuperdogan.registerlocation.kotlinmaps.view.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.eyyuperdogan.registerlocation.kotlinmaps.R
import com.eyyuperdogan.registerlocation.kotlinmaps.databinding.ActivityMainBinding
import com.eyyuperdogan.registerlocation.kotlinmaps.databinding.ActivityMapsBinding
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.adapter.PlaceAdapter
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.model.Place
import com.eyyuperdogan.registerlocation.kotlinmaps.view.view.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

private lateinit var binding: ActivityMainBinding
private  var compositeDisposable= CompositeDisposable()
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var db=Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Plases").build()
        var plaseDao=db.placeDao()

        compositeDisposable.add(
            plaseDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )




    }
   private fun handleResponse(placeList: List<Place>){
      binding.recyclerView.layoutManager=LinearLayoutManager(this)
       val adapter=PlaceAdapter(placeList)
       binding.recyclerView.adapter=adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            val menuInflater = menuInflater
            menuInflater.inflate(R.menu.place_menu, menu)
            return super.onCreateOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.add_place) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("info","new")
                startActivity(intent)
            }
            return super.onOptionsItemSelected(item)
        }


}