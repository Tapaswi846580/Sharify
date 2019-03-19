package com.tapaswi.test;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SecondActivity extends AppCompatActivity {
    String who = "";
    EditText txtMessage;

    private ChatArrayAdapter chatArrayAdapter;
    ImageButton btnSend;
    private ListView listView;
    private boolean side;

    Socket socket;
    ServerSocket serverSocket;

    private InputStream inputStream;
    static private OutputStream outputStream;
    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        txtMessage = (EditText) findViewById(R.id.txtMessage);

        listView = (ListView) findViewById(R.id.msgview);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);

        Intent intent = getIntent();
        who = intent.getStringExtra("who");
        if(who.equals("server")){
            //Do server coding here
            serverClass = new ServerClass();
            serverClass.start();
        }else if(who.equals("client")){
            //Do client coding here
            InetAddress groupOwnerAddress = (InetAddress) intent.getSerializableExtra("InetAddress");
            clientClass = new ClientClass(groupOwnerAddress);
            clientClass.start();
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String msg = txtMessage.getText().toString();
                txtMessage.setText("");
                if(msg.length() != 0) {
                    sendChatMessageLeft(msg);
                    new Abc(msg.getBytes()).start();
                }
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessageRight(String msg) {
        side = false;
        chatArrayAdapter.add(new ChatMessage(side, msg));
        return true;
    }

    private boolean  sendChatMessageLeft(String msg) {
        side = true;
        chatArrayAdapter.add(new ChatMessage(side, msg));
        return true;
    }


    @Override
    public void onBackPressed() {
        MainActivity.manager.removeGroup(MainActivity.channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                try {
//                    inputStream.close();
//                    outputStream.close();
                    socket.close();
                    socket = null;
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(SecondActivity.this,MainActivity.class));
            }

            @Override
            public void onFailure(int i) {
                try {
//                    inputStream.close();
//                    outputStream.close();
                    socket.close();
                    socket = null;
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(SecondActivity.this,MainActivity.class));
            }
        });

    }

    public class ServerClass extends Thread{
//        Socket socket;
//        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(2222);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                Thread t = new Thread(sendReceive);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive implements Runnable {
        //private  Socket socket;

        public SendReceive(Socket skt) {
            socket = skt;
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch  (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    sendChatMessageRight(tempMsg);
                    break;
            }
            return true;
        }
    });

    public class ClientClass extends Thread{
        Socket skt;
        String hostAdd;
        private volatile boolean running = true;
        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            skt = new Socket();
        }

        @Override
        public void run() {

            try {
                skt.connect(new InetSocketAddress(hostAdd, 2222), 5000);
                sendReceive = new SendReceive(skt);
                Thread t = new Thread(sendReceive);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    static class Abc extends Thread{
        byte[] bytes;

        public Abc(byte[] bytes){
            this.bytes = bytes;
        }

        @Override
        public void run() {
            try {
                outputStream.write(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
