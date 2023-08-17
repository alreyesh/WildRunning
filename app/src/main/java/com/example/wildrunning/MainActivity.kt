package com.example.wildrunning

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.wildrunning.LoginActivity.Companion.useremail
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var  drawer: DrawerLayout
    private lateinit var  navigationview : NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         initToolbar()
        initNavigationView()
    }
    fun callSignOff(view: View) {
        signOut()
    }
    private fun initToolbar(){

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toobar_main)
        setSupportActionBar(toolbar)
         drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer,R.string.bar_title,R.string.navigation_drawer_close)

        drawer.addDrawerListener(toggle)
        toggle.syncState()
        toggle.isDrawerIndicatorEnabled
  supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }
    private fun initNavigationView(){
        navigationview   = findViewById( R.id.nav_view)
        navigationview.setNavigationItemSelectedListener(this)
        var headerView: View = LayoutInflater.from(this ).inflate(R.layout.nav_header_main, navigationview,false)
        // Para refrescar el header
        navigationview.removeHeaderView(headerView)
        navigationview.addHeaderView(headerView)
        var tvUser: TextView = headerView.findViewById(R.id.tvUser)
        tvUser.text = useremail

    }
    private fun signOut(){
        useremail=""
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginActivity::class.java))
    }

    override fun onSupportNavigateUp(): Boolean {
        // Para abrir con el icono de la hamburguesa
        drawer.openDrawer(navigationview,true)
        return true
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
          when(item.itemId){
      //   android.R.id.home ->    drawer.openDrawer(Gravity.LEFT)
            R.id.nav_item_record -> callRecordActivity()
            R.id.nav_item_signout -> signOut()
        }
        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    private fun callRecordActivity(){
        val intent = Intent(this,RecordActivity::class.java)
        startActivity(intent)
    }

}
