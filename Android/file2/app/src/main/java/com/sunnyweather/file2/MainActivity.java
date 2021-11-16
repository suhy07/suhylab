package com.sunnyweather.file2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.app.Activity;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WifiManager wifiManager;
    private EditText etUrl;
    private Button btUrl;
    private Button btOpen;
    private String fileUrl;
    private Button btsend;
    private ServerSocket  server;
    private File file;
    private String file1;
    private String file_accept;
    private String ip="192.168.0.102";
    private String filePath;
    private String fileName;
    private Button bt_accept;

    final int port = 30000;
    public static final String TAG = "SendSocket";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*输入路径*/
        etUrl = (EditText) findViewById(R.id.et_url);
        /*自己选择路径*/
        btUrl = (Button) findViewById(R.id.bt_url);
        /*打开文件*/
        btOpen = (Button) findViewById(R.id.bt_open);
        btsend = (Button) findViewById(R.id.bt_send);
        bt_accept = (Button) findViewById(R.id.bt_accept);
        btsend.setOnClickListener(this);
        bt_accept.setOnClickListener(this);
        btUrl.setOnClickListener(this);
        btOpen.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            /*选择文件路径*/
            case R.id.bt_url:
                if (isGrantExternalRW(MainActivity.this)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");   //打开文件类型   Excel文档
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(MainActivity.this, "请检查是否开启读写权限", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.bt_send:
                final String path = fileUrl;
                final String fileName = "文件.xls";
                final String ipAddress = ip;
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        file1=SendFile(fileName, path, ipAddress, port);
                        Log.d("TAG",file1);

                    }
                });
                sendThread.start();
                Toast toast1 = Toast.makeText(MainActivity.this, file1+"", Toast.LENGTH_SHORT);
                toast1.show();
                break;
            case R.id.bt_accept:
                // 服务器端用于监听Socket的线程
                // 服务器端用于监听Socket的线程
                Thread listener = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        int port = 10000;
                        Log.d("TTAA","AAAA1111");
                        try {
                            server = new ServerSocket();
                            Log.d("TTAA","AAAA");
                            server.setReuseAddress(true);
                            server.bind(new InetSocketAddress(10000));
                            if (server != null) {

                                Log.d("TTAA","AAA123244A");
                                while (true) {
                                    file_accept= ReceiveFile();
                                    Log.e("TTAA",file_accept+"+");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                listener.start();

                Toast toast2 = Toast.makeText(this, file_accept, Toast.LENGTH_SHORT);
                toast2.show();
                break;
            /*打开文件*/
            case R.id.bt_open:
                if (fileUrl!= null) {
                    try {
                        File file = new File(fileUrl);
                        Intent intent2 = new Intent("android.intent.action.VIEW");
                        intent2.addCategory("android.intent.category.DEFAULT");
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromFile(file);
                        //application/msword
                        //application/vnd.ms-excel
                        if (fileUrl.contains(".docx")){
                            intent2.setDataAndType(uri, "application/msword");
                        }else if (fileUrl.contains(".xlsx")){
                            intent2.setDataAndType(uri, "application/vnd.ms-excel");
                        }else {
                            intent2.setDataAndType(uri, "text/plain");
                        }
                        startActivity(intent2);
                    } catch (Exception e) {
                        //没有安装第三方的软件会提示
                        Toast toast = Toast.makeText(this, "没有找到打开该文件的应用程序", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    // Intent intent2 = new Intent();
                    // intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //  intent2.setAction(Intent.ACTION_VIEW);
                    //  intent2.setDataAndType((Uri) etUrl.getText(), "text/plain");
                    //      startActivity(intent2);
                } else {
                    Toast.makeText(this, "请选择或输入文件路径", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }

    }public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }
    // 文件接收方法
    public String ReceiveFile() {
        try {
            // 接收文件名

            Log.e("TTAA","AAAA");
            Socket name = server.accept();
            Log.e("TTAA","aaaaAAAA");
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();

            // 接收文件数据
            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();
            File dir = new File("/sdcard/MyMusic"); // 创建文件的存储路径
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String savePath = "/sdcard/MyMusic/" + fileName; // 定义完整的存储路径
            FileOutputStream file = new FileOutputStream(savePath, false);
            byte[] buffer = new byte[1024];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1) {
                file.write(buffer, 0, size);
            }
            file.close();
            dataStream.close();
            data.close();
            return fileName + " 接收完成";
        } catch (Exception e) {
            return "接收错误:\n" + e.getMessage();
        }
    }

    /*接收到刚才选择的文件路径*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。

        if(!uri.getPath().equals(filePath)){//判断是否第二次选择文件
            file=null;
        }

        //获取到选中文件的路径
        filePath = uri.getPath();

        //判断是否是外部打开
        if(filePath.contains("external")){
            isExternal(uri);
        }
        //获取的是否是真实路径
        if(file==null){
            isWhetherTruePath(uri);
        }
        //如果前面都获取不到文件，则自己拼接路径
        if(file==null){
            splicingPath(uri);
        }
        Log.i("hxl", "路径转化成的file========="+file);
        fileUrl=file.toString();
        etUrl.setText(fileUrl);

    }
    /**
     * 拿到文件外部路径，通过外部路径遍历出真实路径
     * @param uri
     */
    private void isExternal(Uri uri){
        Log.i("hxl", "获取文件的路径filePath========="+filePath);
        Log.i("hxl", "===调用外部遍历出路径方法===");
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor actualimagecursor = this.managedQuery(uri,proj,null,null,null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        file = new File(img_path);
//        Log.i("hxl", "file========="+file);
        filePath=file.getAbsolutePath();
        if(!filePath.endsWith(".xls")||!filePath.endsWith(".xlsx")){
            Toast.makeText(MainActivity.this, "您选中的文件不是Excel文档", Toast.LENGTH_LONG).show();
            filePath=null;
            return;
        }

    }
    /**
     * 判断打开文件的是那种类型
     * @param uri
     */
    private void isWhetherTruePath(Uri uri){
        try {
            Log.i("hxl", "获取文件的路径filePath========="+filePath);
            if (filePath != null) {
                if (filePath.endsWith(".xls")||filePath.endsWith(".xlsx")) {
                    if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                        filePath = getPath(this, uri);
                        Log.i("hxl", "===调用第三方应用打开===");
                        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        file = new File(filePath);
                    }
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                        Log.i("hxl", "===调用4.4以后系统方法===");
                        filePath = getRealPathFromURI(uri);
                        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        file = new File(filePath);
                    } else {//4.4以下系统调用方法
                        filePath = getRealPathFromURI(uri);
                        Log.i("hxl", "===调用4.4以下系统方法===");
                        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        file = new File(filePath);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "您选中的文件格式不是Excel文档", Toast.LENGTH_LONG).show();
                }
//                Log.i("hxl", "file========="+file);
            }else{

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }



    //获取文件的真实路径
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }


    /**
     * 如果前面两种都获取不到文件
     * 则使用此种方法拼接路径
     * 此方法在Andorid7.0系统中可用
     */
    private void splicingPath(Uri uri){
        Log.i("hxl", "获取文件的路径filePath========="+filePath);
        if(filePath.endsWith(".xls")||filePath.endsWith(".xlsx")){
            Log.i("hxl", "===调用拼接路径方法===");
            String string =uri.toString();
            String a[]=new String[2];
            //判断文件是否在sd卡中
            if (string.indexOf(String.valueOf(Environment.getExternalStorageDirectory()))!=-1){
                //对Uri进行切割
                a = string.split(String.valueOf(Environment.getExternalStorageDirectory()));
                //获取到file
                file = new File(Environment.getExternalStorageDirectory(),a[1]);
            }else if(string.indexOf(String.valueOf(Environment.getDataDirectory()))!=-1) { //判断文件是否在手机内存中
                //对Uri进行切割
                a = string.split(String.valueOf(Environment.getDataDirectory()));
                //获取到file
                file = new File(Environment.getDataDirectory(), a[1]);
            }
//            fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
//            Log.i("hxl", "file========="+file);
        }else{
            Toast.makeText(MainActivity.this, "您选中的文件不是Excel文档", Toast.LENGTH_LONG).show();
        }
    }



    public String SendFile(String fileName, String path, String ipAddress, int port) {

        try {

            Log.d("TAG","ASS1");
            Socket name = new Socket();
            name.connect(new InetSocketAddress(ipAddress,port),10000);
            Log.d("TAG","ASS2");
            OutputStream outputName = name.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
            BufferedWriter bwName = new BufferedWriter(outputWriter);
            bwName.write(fileName);
            bwName.close();
            outputWriter.close();
            outputName.close();
            name.close();

            Log.d("TAG","ASS3");

            Socket data = new Socket();
            InetSocketAddress inetSocketAddress1 = new InetSocketAddress(ipAddress,port);
            data.connect(inetSocketAddress1);
            Log.d("TAG","ASS5");
            OutputStream outputData = data.getOutputStream();
            Log.d("TAG","ASS6");
            FileInputStream fileInput = new FileInputStream(path);
            Log.d("TAG","ASS7");
            int size = -1;
            byte[] buffer = new byte[1024];
            while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
                outputData.write(buffer, 0, size);
            }
            Log.d("TAG","ASS8");
            outputData.close();
            fileInput.close();
            data.close();

            Log.d("TAG","ASS4");
            return fileName + " 发送完成";
        } catch (Exception e) {
            return "发送错误:\n" + e.getMessage();
        }
    }



}