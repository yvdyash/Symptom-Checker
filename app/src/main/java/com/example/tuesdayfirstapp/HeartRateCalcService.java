package com.example.tuesdayfirstapp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class HeartRateCalcService extends Service {

    private Bundle b = new Bundle();
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private int windows = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.gc();
        Toast.makeText(this, "Processing video...", Toast.LENGTH_LONG).show();
        Log.i("log", "Heart Rate service started");

        //Multithreading for frame extracting from 9 5-second windows of heart rate video
        HeartRateWindowSplitting runnable = new HeartRateWindowSplitting();
        Thread thread = new Thread(runnable);
        thread.start();

        return START_STICKY;
    }

    /**
     * Runnable for multithreading of processing frames function
     */
    public class HeartRateWindowSplitting implements Runnable {
        @Override
        public void run() {
            System.gc();

            b.putIntegerArrayList("heartData", calc());

            stopSelf();
        }
    }

    private int getAverageColor(Bitmap bitmap){

        long redBucket = 0, pixelCount = 0;

        // avg using every 4th pixel value
        for (int y = 0; y < bitmap.getHeight(); y+=4) {
            for (int x = 0; x < bitmap.getWidth(); x+=4) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(c);
//                Log.i("log", "redBucket: "+ Color.red(c));
            }
        }
        Log.i("log", " "+(int) (redBucket/pixelCount));
        return (int)(redBucket / pixelCount);

//        Log.i("log", " "+(int) Color.red(bitmap.getPixel(89, 97)));
//        return Color.red(bitmap.getPixel(89, 97));
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("log", "Heart Service stopping");
                Intent intent = new Intent("broadcastingHeartData");
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(HeartRateCalcService.this).sendBroadcast(intent);
                b.clear();
                System.gc();
            }
        });

        thread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private ArrayList<Integer> getFrames(){
        Bitmap bitmap = null;
        MediaPlayer mp = null;
        try {

            File mediaFile = new File(getExternalFilesDir(null), "myvideo.mp4");
            Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", mediaFile);
            mp = MediaPlayer.create(getBaseContext(), fileUri);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mediaFile.getAbsolutePath());
            ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
            ArrayList<Integer> avgColorArr = new ArrayList<>();

            String METADATA_KEY_DURATION = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int totalDuration = (int) Long.parseLong(METADATA_KEY_DURATION);

            Log.i("log", "totalDuration in microseconds: "+ totalDuration);
            for(int i = 0;i<totalDuration*1000;i+=1000000) {
                Bitmap bm = retriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
                bitmapArrayList.add(bm);
                avgColorArr.add(getAverageColor(bm));
            }

            return avgColorArr;

        } catch(Exception e) {
            Log.e("FrameError",e.toString());
            System.out.println(e.toString());
        }
        finally {
            mp.release();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public ArrayList<Integer> calc() {

        ArrayList<Integer> rednessData = new ArrayList<>();
        try {
            rednessData = getFrames();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rednessData;
    }


}
