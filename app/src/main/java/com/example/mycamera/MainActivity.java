package com.example.mycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,SurfaceHolder.Callback{

    private SurfaceView msv;//相机视频浏览
    private ImageView miv;//相片
    private SurfaceHolder msh;
    private ImageView shutter;//快照按钮
    private Camera mc=null;//相机
    private boolean mpr;//运行相机浏览
    private static final int MENU_START=1;
    private static final int MENU_SENSOR=2;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        msv= (SurfaceView) findViewById(R.id.camera);
        miv= (ImageView) findViewById(R.id.tupian);
        shutter= (ImageView) findViewById(R.id.shutter);
        //设置快照按钮事件
        shutter.setOnClickListener(this);
        miv.setVisibility(View.GONE);
        msh=msv.getHolder();
        //设置SurfaceHolder回调事件
        msh.addCallback(this);
        msh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setCameraParams();
    }

    private void setCameraParams() {
        if (mc!=null){
            return;
        }
        //创建相机，打开相机
        mc=Camera.open();
        //设置相机参数
        Camera.Parameters params=mc.getParameters();
        //拍照自动对焦
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //设置预览帧速率
        params.setPreviewFrameRate(3);
        //设置预览格式
        params.setPictureFormat(PixelFormat.YCbCr_420_SP);
        //设置图片质量百分比
        params.set("图片质量：",85);
        //获取相机支持图片分辨率
        List<Camera.Size> list=params.getSupportedPictureSizes();
        Camera.Size size=list.get(0);
        int w=size.width;
        int h=size.height;
        //设置图片大小
        params.setPictureSize(w,h);
        //设置自动闪光灯
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mpr){
            mc.stopPreview();
        }
        //启动相机
        try {
            mc.setPreviewDisplay(holder);
            mc.startPreview();
            mpr=true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mc!=null){
            //停止相机预览
            mc.stopPreview();
            mpr=false;
            //回收相机
            mc.release();
            mc=null;
        }
    }

    @Override
    public void onClick(View v) {
        //判断是否可以进行拍照
        if (!mpr){
            shutter.setEnabled(false);
            mc.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    //mc.takePicture();
                }
            });
        }
        //相机图片拍照时回调函数
        Camera.PictureCallback mpc= new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (data!=null){
                    saveShow(data);
                }
            }
        };
        //快照回调函数
        Camera.ShutterCallback msc= new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                System.out.println("回调函数。。。");
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_START,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==MENU_START){
            //重启相机拍照
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if (item.getItemId()==MENU_SENSOR){
            Intent intent=new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveShow(byte[] data) {
        try {
            String imageId=System.currentTimeMillis()+"";
            String imagePath= Environment
                    .getExternalStorageDirectory().getPath()
                    +"/myCamera";
            File file=new File(imagePath);
            if (!file.exists()){
                file.mkdirs();
            }
            imagePath="/"+imageId+".jpg";
            file=new File(imagePath);
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album=new AlbumActivity();
            bitmap=album.loadImage(imagePath);
            miv.setImageBitmap(bitmap);
            miv.setVisibility(View.GONE);
            //停止相机浏览
            if (mpr){
                mc.stopPreview();
                mpr=false;
            }
            shutter.setEnabled(true);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
