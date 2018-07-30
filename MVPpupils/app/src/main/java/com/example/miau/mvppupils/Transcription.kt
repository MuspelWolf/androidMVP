package com.example.miau.mvppupils

import android.content.Intent
import java.lang.*
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.miau.mvppupils.R.layout.activity_transcription
import kotlinx.android.synthetic.main.activity_transcription.*
import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.client.WebSocketClient
import java.nio.ByteBuffer
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake
import java.lang.Long.parseLong
import kotlin.math.*
import android.widget.TextView
import android.widget.Toast
import android.text.method.ScrollingMovementMethod
import java.util.*
import android.R.string.cancel
import java.nio.channels.ConnectionPendingException
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.exceptions.WebsocketNotConnectedException


class Transcription : AppCompatActivity() {
    var onrepeat=false
    var open = false
    var onclose = false
    lateinit var SERVER_URL:String
    lateinit var mclient:ChatClient
    var oldtext=""
    var newtext=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transcription)
        profText.setMovementMethod(ScrollingMovementMethod())
        SERVER_URL = getIP()
        mclient= ChatClient(URI(SERVER_URL), Draft_6455(), emptyMap(), 1000)
        mclient.connect()

    }
    override fun onBackPressed() {
        super.onBackPressed()
        mclient.close()
    }


    fun getIP(): String {
            val bundle = intent.extras
            var hex1 = bundle.getString("hex1")
            var hex2 = bundle.getString("hex2")
            var SERVER_URL = ""

            var toInt: Long
            var toInt2: Long

            toInt2 = parseLong(hex2, 16)
            toInt = parseLong(hex1, 16)

            hex1 = toInt.toString()
            hex2 = toInt2.toString()

            SERVER_URL = "ws://192.168.$hex1.$hex2:8080"
            return SERVER_URL
        }


        inner class ChatClient(url: URI, draft: Draft, httpHeaders: Map<String, String>, Timeout: Int) : WebSocketClient(url, draft, httpHeaders, Timeout) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                open=true
                Log.e("Open: ", "new connection opened")
            }


            override fun onMessage(message: String) {
                runOnUiThread { profText.text = "$newtext $message"
                                oldtext="$newtext $message"
                }
                Log.e("---------- Mensaje:", message)

            }

            override fun onMessage(message: ByteBuffer) {
                if (Arrays.toString(message.array()) == "[0, 0, 1, 1]") {
                    runOnUiThread {
                        onrepeat=true
                        val builder = AlertDialog.Builder(this@Transcription)
                        builder.setMessage("Sesion Finalizada por su instructor")
                        builder.setPositiveButton("OK") { _, _ ->
                        }
                        builder.setNegativeButton("Salir") { _, _ ->
                            finish()
                        }
                        builder.show()

                    }
                    //val intent = Intent(Transcription(), PinActivity::class.java)
                    //startActivity(intent)

                    //profText.text="${profText.text} \n ByteBuffer recibido"
                }
                else if (Arrays.toString(message.array()) == "[1, 1, 0, 0]") {
                    newtext=oldtext
                    }


                Log.e("Bytes: ", onclose.toString())
            }


            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.e("Close: ", "closed with exit code $code additional info: $reason")
                runOnUiThread {
                    val builder = AlertDialog.Builder(this@Transcription)
                    if(!onrepeat) {
                        if (open)
                            builder.setMessage("Sesion Finalizada por su instructor")
                        else
                            builder.setMessage("No se encuentra ninguna conexión")

                        builder.setPositiveButton("OK") { _, _ ->

                        }
                        builder.setNegativeButton("Salir") { _, _ ->
                            finish()
                        }
                        builder.show()

                    }

                }

            }

            override fun onError(ex: Exception) {
                Log.e("Error: ", "an error occurred:$ex")

            }


        }


    }



