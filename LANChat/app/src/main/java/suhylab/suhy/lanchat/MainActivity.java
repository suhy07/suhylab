package suhylab.suhy.lanchat;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tv_localIP;
    EditText et,et_ip;
    Button button;
    int port=5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                            Toast.makeText(MainActivity.this, "您拒绝了如下权限：" + deniedList, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        tv_localIP=findViewById(R.id.tv_localIP);
        et=findViewById(R.id.et);
        et_ip=findViewById(R.id.et_ip);
        button=findViewById(R.id.button);

        tv_localIP.setText(IPUtil.GetLocalIP(this));
        button.setOnClickListener(v->SentMessage());
        InitServer();
    }
    public void SentMessage(){
        String ip=et_ip.getText().toString();
        Log.d("tag",ip);
        Socket socket=new Socket();
        new Thread(){
            @Override
            public void run(){
                try {
                    socket.connect(new InetSocketAddress(ip, port), 5000);
                    OutputStream outputStream=socket.getOutputStream();
                    String str=et.getText().toString();
                    str=str.replace("#","\r\n");
                    outputStream.write(str.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    outputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void InitServer(){
        new Thread(){
            ServerSocket serverSocket;
            Socket socket;
            @Override
            public void run(){
                try {
                    serverSocket=new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while(true){
                    InputStream inputStream=null;
                    OutputStream outputStream=null;
                    try {
                        socket=serverSocket.accept();
                        Log.d("TAG","accept");
                        inputStream=socket.getInputStream();
                        String str=IPUtil.GetStringFromInputStream(inputStream);
                        str=str.replace("#","\n");
                        Log.d("TAG",str);
                        et.setText(str);
                        inputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}