package com.example.lucon.sockettest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    public final int SERVER = 0;
    public final int CLIENT = 1;
    private ServerSocket serverSocket;
    private Socket connectSocket, socket;
    private ExecutorService mThreadPool;
    private int identity; // 标识当前用户在当前会话中的身份类别

    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    String response;
    OutputStream os;

    private Button btnListen, btnConnect, btnDisconnect, btnSend;
    private TextView receiveMsg;
    private EditText sendMsg, serverIP, serverPort, localPort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnListen = findViewById(R.id.listen);
        btnConnect = findViewById(R.id.connect);
        btnDisconnect = findViewById(R.id.disconnect);
        btnSend = findViewById(R.id.send);
        sendMsg = findViewById(R.id.send_message);
        receiveMsg = findViewById(R.id.receive_message);




        serverIP = findViewById(R.id.server_ip);
        serverPort = findViewById(R.id.server_port);
        localPort = findViewById(R.id.local_port);

        mThreadPool = Executors.newCachedThreadPool();

        btnListen.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            // 点击listen按钮，当前用户作为服务器端
            case R.id.listen:
                identity = SERVER;
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            String port = localPort.getText().toString();
                            if(TextUtils.isEmpty(port)){
                                Toast.makeText(ChatActivity.this, "本地监听端口号为空！", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int portNum = Integer.parseInt(port);
                            serverSocket = new ServerSocket(portNum);
                            connectSocket = serverSocket.accept();

                            is = connectSocket.getInputStream();
                            isr = new InputStreamReader(is);
                            br = new BufferedReader(isr);
                            os = connectSocket.getOutputStream();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try {
                                if(br != null){
                                    response = br.readLine();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            receiveMsg.setText(response);
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            // 当前用户作为客户机端
            case R.id.connect:
                identity = CLIENT;
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            String ip = serverIP.getText().toString();
                            String port = serverPort.getText().toString();
                            if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChatActivity.this, "IP地址和端口号不能为空！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }
                            int portNum = Integer.parseInt(port);
                            socket = new Socket(ip, portNum);

                            is = socket.getInputStream();
                            isr = new InputStreamReader(is);
                            br = new BufferedReader(isr);
                            os = socket.getOutputStream();

                            System.out.println("client is connected ? " + socket.isConnected());
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            try {
                                if(br != null){
                                    response = br.readLine();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            receiveMsg.setText(response);
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            case R.id.send:
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            os.write((sendMsg.getText().toString() + "\n").getBytes("utf-8"));
                            os.flush();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sendMsg.setText("");
                                }
                            });
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case R.id.disconnect:
                try{
                    os.close();
                    br.close();
                    is.close();
                    if(identity == SERVER){
                        connectSocket.close();
                        serverSocket.close();
                    }else if(identity == CLIENT){
                        socket.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

}
