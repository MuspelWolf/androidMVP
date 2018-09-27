package com.example.miau.mvp30

import android.app.Dialog
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_access.*
import kotlinx.android.synthetic.main.activity_transcription.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.lang.Long
import java.net.URI
import java.nio.ByteBuffer
import java.util.*

class Access : AppCompatActivity(),ConnectivityReceiver.ConnectivityReceiverListener  {

    var hex1 = ""
    var hex2 = ""
    var onrepeat = false
    var open = false
    var onclose = false
    var oldtext = ""
    var newtext = ""
    lateinit var mclient: ChatClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_access)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        registerReceiver(ConnectivityReceiver(), //wifi
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))


        profPin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                b2.isEnabled = profPin.text.toString().length >= 4
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        b2.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(b2.getWindowToken(), 0)
            if (profPin.text.toString().length == 4) {
                var isHex = true //para saber si nos han introducido texto hexadecimal
                val regex = "^[0-9a-fA-F]+$".toRegex() //expresion regular para comprobar que nos han introducido un hexadecimal

                isHex = regex.matches(profPin.text.toString()) //averiguamos si el pin es hexadecimal

                if (isHex) { // si hexadecimal=true
                    mclient = ChatClient(URI(getIP()), Draft_6455(), emptyMap(), 100000)
                    profPin.setText("")
                    mclient.connect()
                    deviceOnline.text = "No se encuentra ninguna conexión con este PIN"
                    deviceOnline.visibility = View.VISIBLE
                } else { // si hexadecimal=false
                    deviceOnline.setText("El PIN introducido es incorrecto")
                    deviceOnline.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (open)
            mclient.close()
    }


    fun getIP(): String {
        hex1 = profPin.text.substring(0, 2)
        hex2 = profPin.text.substring(2, 4)
        var SERVER_URL = ""

        var toInt = Long.parseLong(hex1, 16).toString()
        var toInt2 = Long.parseLong(hex2, 16).toString()

        SERVER_URL = "ws://192.168.$toInt.$toInt2:8080"
        return SERVER_URL

    }


    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    private fun showMessage(isConnected: Boolean) {
        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (!isConnected || !mWifi.isConnected) {
            deviceOnline.text = ""
            wifiname.text = "No hay conexión WIFI"
            profPin.setEnabled(false)
            b2.isEnabled = false
        } else {
            deviceOnline.text = ""
            wifiname.text = mWifi.extraInfo.replace("\"", "") //obtenemos nombre del wifi sin comillas
            wifiname.visibility = View.VISIBLE
            profPin.setEnabled(true)
            b2.isEnabled = true
        }


    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
    }

    inner class ChatClient(url: URI, draft: Draft, httpHeaders: Map<String, String>, Timeout: Int) : WebSocketClient(url, draft, httpHeaders, Timeout) {

        val subFragment = SubsFragment()

        fun setURI(urin: URI) {
            this.uri = urin
        }

        override fun onOpen(handshakedata: ServerHandshake?) {
            runOnUiThread {
                val fragmentManager = getSupportFragmentManager()
                val transaction = fragmentManager.beginTransaction()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.add(android.R.id.content, subFragment).commit()
            }
            open = true
            Log.e("Open: ", "new connection opened")
        }


        override fun onMessage(message: String) {
            runOnUiThread {
                subFragment.escribirSubs(message, newtext)
                oldtext = "$newtext $message"
            }
            Log.e("---------- Mensaje:", message)

        }

        override fun onMessage(message: ByteBuffer) {
            if (Arrays.toString(message.array()) == "[0, 0, 1, 1]") {
                runOnUiThread {
                    onrepeat = true
                    val builder = AlertDialog.Builder(this@Access)
                    builder.setMessage("Sesion Finalizada por su instructor")
                    builder.setNegativeButton("Ok") { _, _ ->
                        finish()
                    }
                    builder.show()

                }
            } else if (Arrays.toString(message.array()) == "[1, 1, 0, 0]") {
                //todo check when text ends and update in the dialog
                newtext = oldtext
            }


            Log.e("Bytes: ", onclose.toString())
        }


        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            open = false
            Log.e("Close: ", "closed with exit code $code additional info: $reason")
        }


        override fun onError(ex: Exception) {
            open = false
            Log.e("Error: ", "an error occurred:$ex")

        }


    }

    class SubsFragment : DialogFragment() {
        private var rootView: View? = null


        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

            rootView = inflater.inflate(R.layout.activity_transcription, container, false)
            return rootView
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return super.onCreateDialog(savedInstanceState)
        }

        override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
            menu!!.clear()
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            val id = item!!.itemId

            if (id == android.R.id.home) {
                // handle close button click here
                dismiss()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        fun escribirSubs(message: String, newtext: String){
            profText.text = "$newtext $message"
        }

        companion object {

            private val TAG = "Access"
        }
    }
}
