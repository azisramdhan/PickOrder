package me.alfatih.pickorder.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.alfatih.pickorder.R
import me.alfatih.pickorder.view.maps.MapsActivity
import org.jetbrains.anko.startActivityForResult

class MainActivity : AppCompatActivity() {

    // https://developers.google.com/places/web-service/autocomplete#place_types

    companion object {
        const val ADDRESS_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       /* et_location.setOnTouchListener { _, _ ->
            startActivityForResult<MapsActivity>(ADDRESS_REQUEST_CODE)
            true
        }*/

        et_location.setOnClickListener {
            startActivityForResult<MapsActivity>(ADDRESS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ADDRESS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getStringExtra("result")
                et_location.setText(result)
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

}
