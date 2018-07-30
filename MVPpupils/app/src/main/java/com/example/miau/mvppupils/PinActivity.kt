package com.example.miau.mvppupils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_pin.*
import java.lang.Long.parseLong
import android.net.NetworkInfo
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import com.example.miau.mvppupils.R.id.deviceOnline

class PinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        profPin.setText("")
        deviceOnline.text = ""

        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (!mWifi.isConnected()) { //si no estamos conectados a una red wifi
            deviceOnline.setText("El dispositivo no esta conectado a ninguna red WIFI")
            deviceOnline.visibility = View.VISIBLE
            profPin.setEnabled(false)
            b2.visibility = View.INVISIBLE
        } else {
            //si estamos conectados a una red wifi
            wifiname.text = mWifi.extraInfo.replace("\"", "") //obtenemos nombre del wifi sin comillas

            var hex1 = ""
            var hex2 = ""

            var isHex = true //para saber si nos han introducido texto hexadecimal
            val regex = "^[0-9a-fA-F]+$".toRegex() //expresion regular para comprobar que nos han introducido un hexadecimal

            b2.setOnClickListener {
                if (profPin.text.toString().length == 4) { //si nos han introducido 4 digitos

                    isHex = regex.matches(profPin.text.toString()) //averiguamos si el pin es hexadecimal

                    if (isHex) { // si hexadecimal=true
                        hex1 = profPin.text.substring(0, 2)
                        hex2 = profPin.text.substring(2, 4)
                        val intent = Intent(this, Transcription::class.java)
                        intent.putExtra("hex1", hex1)
                        intent.putExtra("hex2", hex2)
                        startActivity(intent)
                        deviceOnline.text = ""
                        profPin.setText("")
                    } else { // si hexadecimal=false
                        deviceOnline.setText("de la A a la F retrasao")
                        deviceOnline.visibility = View.VISIBLE
                    }

                } else { //si nos han introducido menos de 4 dígitos
                    deviceOnline.setText("Introduzca los 4 dígitos")
                    deviceOnline.visibility = View.VISIBLE
                }

            }

            //listener del editText profPin
            /*profPin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (profPin.text.toString() == "MIAU") {
                    val intent = Intent(applicationContext, Transcription::class.java)
                    startActivity(intent)
                }
            }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })*/
        }
    }

    //si el dispositivo esta conectado por el medio que sea
    /*private fun isDeviceOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo.isConnected
        } else false
    }*/

}




