package com.example.mycamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.Vector;

public class AlbumActivity extends AppCompatActivity {

    private ViewFlipper vFlipper;
    private Bitmap[] mBitmap;//图片储存列表
    private long startTime=0;
    private SensorManager sManager;//重力感应硬件控制器
    private SensorEventListener sel;//重力感应监听

    //加载相册
    public String[] loadAlbum(){
        String pathName=android.os.Environment.getExternalStorageDirectory().getPath()+
                "/mycamera";
        //创建文件
        File file=new File(pathName);
        Vector<Bitmap> fileName=new Vector<Bitmap>();
        if (file.exists()&&file.isDirectory()){
            String[] str=file.list();
            for(String s:str){
                if(new File(pathName+"/"+s).isFile()){
                    //fileName.addElement(loadAlbum(loadImage(pathName)+"/"+s));
                }
            }
            mBitmap=fileName.toArray(new Bitmap[]{});
        }
        return null;
    }

    public Bitmap loadImage(String pathName) {
        //读取相片，并对图片进行缩小
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        //此时返回的bitmap为空
        Bitmap bitmap=BitmapFactory.decodeFile(pathName,options);
        //获取屏幕的宽度
        WindowManager manager=getWindowManager();
        Display display=manager.getDefaultDisplay();

        //假设希望Bitmap的显示宽度为手机屏幕的宽度
        int screenWidth=display.getWidth();
        //int screenHeight=display.getHeigth();
        //计算Bitmap的高度比变化数值
        options.inSampleSize=options.outWidth/screenWidth;
        //将inJustDecodeBounds设置为false，以便于可以解码为Bitmap文件
        options.inJustDecodeBounds=false;
        //读取相片Bitmap
        bitmap=BitmapFactory.decodeFile(pathName,options);
        return  bitmap;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        vFlipper= (ViewFlipper) this.findViewById(R.id.vf);
        loadAlbum();
        if (mBitmap==null){
            Toast.makeText(this,"相册无图片",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else{
            for (int i=0;i<=mBitmap.length-1;i++){
                vFlipper.addView(addImage(mBitmap[i]));
            }
        }
        sManager= (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor sensor= sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //添加重力传感器监听
        sel=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x=event.values[SensorManager.DATA_X];
                if (x>10&&System.currentTimeMillis()>startTime+1000){
                    //记录甩动的开始时间
                    startTime=System.currentTimeMillis();
                    vFlipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this
                    ,R.anim.push_right_in));
                    vFlipper.setOutAnimation(AnimationUtils.loadAnimation(AlbumActivity.this
                    ,R.anim.push_right_out));
                    vFlipper.showPrevious();
                }else if(x<-10&&System.currentTimeMillis()>startTime+1000){
                    startTime=System.currentTimeMillis();
                    vFlipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this
                    ,R.anim.puah_left_in));
                    vFlipper.setOutAnimation(AnimationUtils.loadAnimation(AlbumActivity.this
                    ,R.anim.push_left_out));
                    vFlipper.showNext();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        //注册监听，SENSOR_DELAY_GAME是检测的精确度
        sManager.registerListener(sel,sensor,SensorManager.SENSOR_DELAY_GAME);
    }

    private View addImage(Bitmap bitmap) {
        ImageView iv=new ImageView(this);
        iv.setImageBitmap(bitmap);
        return iv;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注册重力传感器监听
        sManager.unregisterListener(sel);
    }
}
