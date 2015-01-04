package com.pczhu.seniortest;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorListener, OnClickListener {
	
	private Sensor defaultSensor;
	private SensorManager sm;
	private float x, y, z, last_x, last_y, last_z;
	private long lastUpdate;
	private static final int SHAKE_THRESHOLD = 5000;
	private TextView tv;
	private int i = 1;
	private AudioManager audioService;
	private Vibrator vibrator;
	boolean isSingflag = false;
	private Button btn;
	private MediaPlayer mediaPlayer;
	private int currentPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 

		setContentView(R.layout.activity_main);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		defaultSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		tv = (TextView) findViewById(R.id.tv);
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		sm.registerListener(this, SensorManager.SENSOR_ACCELEROMETER,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() {
		super.onDestroy();
		sm.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// 每100毫秒检测一次
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				x = values[SensorManager.DATA_X];

				y = values[SensorManager.DATA_Y];

				z = values[SensorManager.DATA_Z];
				float speed = Math.abs(x + y + z - last_x - last_y - last_z)
						/ diffTime * 10000;
				if (speed > SHAKE_THRESHOLD) {
					setTitle("x=" + (int) x + "," + "y=" + (int) y + "," + "z="
							+ (int) z);
					tv.setText("摇晃了" + i + "次");
					i++;
					
					audioService = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE); 
					System.out.println(audioService.getRingerMode());
					if (audioService.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) { 
						//shouldPlayBeep = false; 
						getVoiceSource(); 
					} else if(audioService.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
						vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE); 
						vibrator.vibrate(200); 
					}
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	private void getVoiceSource() {
		mediaPlayer = new MediaPlayer(); 
	    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); 
	    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { 
	    @Override 
		    public void onCompletion(MediaPlayer player) { 
	    	System.out.println("完成");
		    	player.seekTo(currentPosition); 
		    	isSingflag = false;
		    } 
	    });
		AssetFileDescriptor file = this.getResources().openRawResourceFd(R.raw.free); 
		System.out.println("File:"+file.getLength());
			try { 
				mediaPlayer.setDataSource(file.getFileDescriptor(), 
				file.getStartOffset(), file.getLength()); 
				file.close(); 
				mediaPlayer.setVolume(audioService.getStreamVolume(AudioManager.STREAM_SYSTEM), audioService.getStreamVolume(AudioManager.STREAM_SYSTEM)); 
				mediaPlayer.prepare(); 
			} catch (IOException ioe) { 
				mediaPlayer = null; 
			} 
		    if (mediaPlayer != null) { 
    			mediaPlayer.start(); 
    			isSingflag = true;
		    } 
		    
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {

	}

	@Override
	public void onClick(View v) {
		System.out.println("Stop");
		if(isSingflag && mediaPlayer != null){
			System.out.println("暂停");
			currentPosition = mediaPlayer.getCurrentPosition();
			mediaPlayer.pause();
			isSingflag = false;
		}else if(!isSingflag && mediaPlayer != null){
			System.out.println("恢复");
			mediaPlayer.seekTo(currentPosition);
			currentPosition = 0;
			mediaPlayer.start();
			isSingflag = true;
		}
	}
}
