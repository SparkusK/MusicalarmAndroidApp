package shane.kelly.musicalarm;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AlarmReceiverActivity extends Activity {

	private MediaPlayer mMediaPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_receiver);
		Intent song = getIntent();
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(song.getExtras().getString("pathName"));
			final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		} catch (IOException e) {
			Log.d("alarm", "Something went wrong with starting the alarm.");
		}
	}
	
	public void stopAlarm(View _view) {		
		mMediaPlayer.stop();
		finish();
	}
}
