package com.sunnyweather.file2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    TextView tv_LocalIP,tv_State,tv_FileName;
    EditText et_IP;
    Button btn_OpenFile,btn_SendFile;
    String TAG="AAA$$$";
    private ServerSocket server;
    private int port=25569;
    int fileIntentCode=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.d(TAG,"hello");
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                )
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "即将申请的权限是程序必须依赖的权限", "我已明白");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            // TODO: 2020/10/14 权限申请成功
                        } else {
                            Toast.makeText(MainActivity2.this, "您拒绝了如下权限：" + deniedList, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        tv_LocalIP=findViewById(R.id.tv_LocalIP);
        tv_FileName=findViewById(R.id.tv_FileName);
        tv_State=findViewById(R.id.tv_State);
        et_IP=findViewById(R.id.et_IP);
        btn_OpenFile=findViewById(R.id.btn_OpenFile);
        btn_SendFile=findViewById(R.id.btn_SendFile);

        tv_LocalIP.setText(IPUtil.GetLocalIP(this));
        btn_OpenFile.setOnClickListener(v->OpenFile());
        btn_SendFile.setOnClickListener(v->FileSend(filename,path,et_IP.getText().toString(),port));
        InitServer();
    }

    private void OpenFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,fileIntentCode);
    }
    String filename,path,ip;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程
            path=FileUtils.getPath(this,uri);
            tv_FileName.setText(uri.getPath());
            Toast.makeText(MainActivity2.this, path, Toast.LENGTH_SHORT).show();
        }
    }
    public void FileSend(String fileName, String path, String ipAddress, int port){
        Log.d(TAG,"开始连接Server"+ipAddress+":"+port);
        Socket socket = new Socket();
        new Thread(){
            @Override
            public void run(){
                try {
                    //设置socket，并进行连接connect
                    socket.connect(new InetSocketAddress(ipAddress, port), 5000);
                    Log.d(TAG,"开始SendFile");
                    int bufferSize = 8192;
                    byte[] buf = new byte[bufferSize];//数据存储
                    // 选择进行传输的文件
                    File file = new File(path + fileName);
                    System.out.println("文件长度:" + (int) file.length());
                    DataInputStream input = new DataInputStream(new FileInputStream(path + fileName));
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());//将socket设置为数据的传输出口
                    DataInputStream getAck = new DataInputStream(socket.getInputStream());//设置socket数据的来源
                    //将文件名传输过去
                    output.writeUTF(file.getName());
                    output.flush();
                    //将文件长度传输过去
                    output.writeLong((long) file.length());
                    output.flush();
                    int readSize = 0;
                    while(true)
                    {
                        if(input != null)
                        {
                            readSize = input.read(buf);
                        }
                        if(readSize == -1)
                            break;
                        output.write(buf, 0, readSize);
                        if(!getAck.readUTF().equals("OK"))
                        {
                            System.out.println("服务器"+ ipAddress + ":" + port + "失去连接！");
                            break;
                        }
                    }
                    output.flush();// 注意关闭socket链接，不然客户端会等待server的数据过来，// 直到socket超时，导致数据不完整。
                    input.close();
                    output.close();
                    socket.close();
                    getAck.close();
                    System.out.println("文件传输完成");
                    Log.d(TAG, fileName + " 发送完成");
                } catch (Exception e) {
                    Log.d(TAG,"发送错误:\n" + e.getMessage());
                }
            }

        }.start();
    }
    public void InitServer(){
        new Thread(){
            @Override
            public void run() {
                try {
                    server = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (server != null) {
                    while (true) {
                        String path="/";
                        Log.d(TAG,"Server启动port:"+port);
                        ReceiveFile(path);
                    }
                }
            }
        }.start();
    }
    public String ReceiveFile(String path) {
        try {
            Log.d(TAG,"Server wait for accept");
            Socket socket = server.accept();
            Log.d(TAG,"客户端"+ socket.getInetAddress() +"已连接");
            int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];//数据存储
            long donelen = 0;//传输完成的数据长度
            long filelen = 0;//文件长度
            //将socket数据作为数据输入流
            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            //以客户端的IP地址作为存储路径
            String fileDir = path + "\\" + socket.getInetAddress().toString().substring(1, socket.getInetAddress().toString().length());;
            File file = new File(fileDir);
            //判断文件夹是否存在，不存在则创建
            if(!file.exists())
            {
                file.mkdir();
            }

            String fileName = input.readUTF();//读取文件名

            //设置文件路径
            String filePath = fileDir + "\\" + fileName;


            file = new File(filePath);

            if(!file.exists())
            {
                file.createNewFile();
            }

            DataOutputStream fileOut = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));


            filelen = input.readLong();//读取文件长度

            System.out.println("文件的长度为:" + filelen + "\n");
            System.out.println("开始接收文件!" + "\n");
            DataOutputStream ack = new DataOutputStream(socket.getOutputStream());

            while (true) {
                int read = 0;
                if (input != null) {
                    read = input.read(buf);
                    ack.writeUTF("OK");//结束到数据以后给client一个回复
                }

                if (read == -1) {
                    break;
                }
                donelen += read;
                // 下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比
                System.out.println("文件接收了" + (donelen * 100 / filelen)
                        + "%\n");
                fileOut.write(buf, 0, read);
            }

            if(donelen == filelen)
                System.out.println("接收完成，文件存为" + file + "\n");
            else
            {
                System.out.printf("IP:%s发来的%s传输过程中失去连接\n",socket.getInetAddress(),fileName);
                file.delete();
            }
            ack.close();
            input.close();
            fileOut.close();
            return fileDir+"接受完成";
        } catch (Exception e) {
            return "接收错误:\n" + e.getMessage();
        }
    }
}