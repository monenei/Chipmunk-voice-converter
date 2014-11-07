package com.audiorecoderchipmunk;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.chipmunkrecord.ExtAudioRecorder;
import com.chipmunkrecord.WavFile;

public class CaptureAudioScreen extends Activity{

	
	private File outputFile ;
	private MediaPlayer myPlayer;
	private Button startBtn;
	private Button stopBtn;
	private Button playBtn;
	private Button convertchipmunk;
	private boolean isChipmunkVoice = false;
	
	
	private boolean isStopped= false;
	private  ExtAudioRecorder exRecorder = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.captureaudioscreen);
		initView();
		attachListener();
		readFromBundle();
		initializeRecorder();
	}
	
	
	private void attachListener(){
		
	      startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				start(v);
			}
	      });
	      
	     
	      stopBtn.setOnClickListener(new OnClickListener() {
	  		
	  		@Override
	  		public void onClick(View v) {
	  			stop(v);
	  		}
	      });
	      
	     
	      playBtn.setOnClickListener(new OnClickListener() {
	  		
	  		@Override
	  		public void onClick(View v) {
					play(v);	
	  		}
	      });
	      
	      convertchipmunk = (Button)findViewById(R.id.convertchipmunk);
	      convertchipmunk.setOnClickListener(new OnClickListener() {
	  		
	  		@Override
	  		public void onClick(View v) {
	  			 convertchipmunk.setEnabled(false);
	  			if(!isChipmunkVoice)
	  				new ConvertChipmunk().execute();
	  			else
	  				Toast.makeText(CaptureAudioScreen.this, "Record new voice", 1000).show();
	  		}
	      });
	}
	
	
	
	
	
	private class ConvertChipmunk extends AsyncTask<Void , Void, Void>{

		private ProgressDialog pd;
		
		public ConvertChipmunk(){
			
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pd = new ProgressDialog(CaptureAudioScreen.this);
			pd.setCancelable(false);
			pd.setMessage("Converting file...     ");
			pd.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			playModifiedVoice();	
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(pd != null && pd.isShowing())
				pd.dismiss();
			Toast.makeText(CaptureAudioScreen.this, "Audio has been successfully converted into chipmunk voice.", 2000).show();
		}
	}
	
	private void playModifiedVoice(){
		File outputFileChipmunk = null;
		try {
			outputFileChipmunk = new File(Environment.getExternalStorageDirectory() , "chipmunk.wav");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		WavFile.convertChipMunkVoice(outputFile.getAbsolutePath(), outputFileChipmunk.getAbsolutePath());
		
		
		outputFile = outputFileChipmunk;
		
		try{
//			File  file = new File(outputFileChipmunk.getAbsolutePath());
//			if(file.exists())
//				file.delete();
		}catch(Exception e){}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		try {
			stopPlay(stopBtn);
			if(!isStopped)
			{
				Toast.makeText(getApplicationContext(), "Your recording is not saved", Toast.LENGTH_LONG).show();
			}
			myPlayer.release();
		} catch (Exception e) {
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("path", outputFile);
		System.out.println("path "+ outputFile);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void initView(){
		 startBtn = (Button)findViewById(R.id.start);
		 stopBtn = (Button)findViewById(R.id.stop);
		 playBtn = (Button)findViewById(R.id.play);
		 convertchipmunk = (Button)findViewById(R.id.convertchipmunk);
	}
	
	private void readFromBundle(){
		try {
			outputFile = new File(Environment.getExternalStorageDirectory() , "recordFile.wav");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void initializeRecorder(){
		exRecorder = ExtAudioRecorder.getInstanse(false);
		exRecorder.setOutputFile(outputFile.getAbsolutePath());
	}
	
	
	
	public void start(View view){
		   try {
			   isStopped = false;
			   isChipmunkVoice = false;
			   exRecorder.prepare();
				exRecorder.start();
	       } catch (IllegalStateException e) {
	          e.printStackTrace();
	       } catch (Exception e) {
	           e.printStackTrace();
	        }
		   
	       startBtn.setEnabled(false);
	       stopBtn.setEnabled(true);
	       convertchipmunk.setEnabled(false);
	       Toast.makeText(getApplicationContext(), "Start recording...",  		   Toast.LENGTH_SHORT).show();
	   }

	   public void stop(View view){
		   try {
			  isStopped= true;
			  exRecorder.stop();
			  exRecorder.release();
			  
		      stopBtn.setEnabled(false);
		      playBtn.setEnabled(true);
		      convertchipmunk.setEnabled(true);
		      Toast.makeText(getApplicationContext(), "Stop recording...",   		  Toast.LENGTH_SHORT).show();
		   } catch (IllegalStateException e) {
				e.printStackTrace();
		   } catch (RuntimeException e) {
				e.printStackTrace();
		   }
	   }
	  
	   public void play(View view) {
		   try{
			   myPlayer = new MediaPlayer();
			   myPlayer.setDataSource(outputFile.getAbsolutePath());
			   myPlayer.prepare();
			   myPlayer.start();
			   myPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					playBtn.setEnabled(true);
				}
			});
			   
			   playBtn.setEnabled(false);
			   
			   Toast.makeText(getApplicationContext(), "Start play the recording...", 
					   Toast.LENGTH_SHORT).show();
		   } catch (Exception e) {
				e.printStackTrace();
			}
	   }
	   
	   public void stopPlay(View view) {
		   try {
		       if (myPlayer != null) {
		    	   myPlayer.stop();
		           myPlayer.release();
		           myPlayer = null;
		           playBtn.setEnabled(true);
		           
		           Toast.makeText(getApplicationContext(), "Stop playing the recording...", 
						   Toast.LENGTH_SHORT).show();
		       }
		   } catch (Exception e) {
				e.printStackTrace();
			}
	   }
	
	
}
