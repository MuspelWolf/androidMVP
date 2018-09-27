package com.example.miau.mvp30;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */

public class Server extends WebSocketServer {

    public static int clientCount = 0;


    public Server(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );

    }

    public Server(InetSocketAddress address) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        /*conn.send("Welcome to the server!"); *///This method sends a message to the new client
        /*broadcast( "new connection: " + handshake.getResourceDescriptor() );//This method sends a message to all clients connected*/
        Log.d(TAG, "new connection: " + handshake.getResourceDescriptor() );
        clientCount++;

        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
        Log.d( TAG, "Usuarios conectados:"+ clientCount );
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote)  {
        /*broadcast( conn + " has left the room!" );*/
        System.out.println( conn + " has left the room!" );
        clientCount --;
        Log.d( TAG, "Usuarios conectados:"+ clientCount );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        broadcast( message );
        System.out.println( conn + ": " + message );
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        broadcast( message.array() );
        System.out.println( conn + ": " + message );
    }


    public static void main( String[] args ) throws InterruptedException , IOException {
        WebSocketImpl.DEBUG = true;
        int port = 8080; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        Server s = new Server( port );
        s.start();
        System.out.println( "ChatServer started on port: " + s.getPort() );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;
            }
        }
    }
    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
    }
}
