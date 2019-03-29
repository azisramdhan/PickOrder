package me.alfatih.pickorder.view.maps

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.Marker
import me.alfatih.pickorder.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MapsPresenter(private val view: MapsView, private val context: Context){

    fun getAddress(marker: Marker){
        view.showLoading()
        doAsync {
            uiThread {
            }
        }
        var result = context.getString(R.string.loading)
        try {
            val geo = Geocoder(context, Locale.getDefault())
            val addresses = geo.getFromLocation(marker.position.latitude, marker.position.longitude, 1)
            if (addresses.isNotEmpty()) {
                if (addresses.size > 0) {
                    result = addresses[0].getAddressLine(0)
                    Log.d("result",addresses[0].getAddressLine(0))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // getFromLocation() may sometimes fail
        }
        view.showData(result)
    }
}
