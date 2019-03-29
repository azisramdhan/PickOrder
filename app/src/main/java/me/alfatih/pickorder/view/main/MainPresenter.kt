package me.alfatih.pickorder.view.main

import android.content.Context
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainPresenter(private val view: MainView,
                    private val context: Context){
    fun getAddress(input: String){
        view.showLoading()
        doAsync {
            uiThread {
            }
        }
    }
}
