package shane.kelly.musicalarm;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import functionality.Alarm;
import functionality.DBControl;
import functionality.Playlist;
import functionality.Song;
import functionality.SongArrayAdapter;

@SuppressLint("NewApi")
public class EditAlarmActivity extends Activity implements OnCheckedChangeListener {

	private DBControl db;
	
	private static Drawable ACTIVE;
	private static Drawable INACTIVE;
	
	private String alarmName;
	private String playlistName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		INACTIVE = getResources().getDrawable(R.drawable.toggle_background_no);
		ACTIVE = getResources().getDrawable(R.drawable.toggle_background_yes);
		playlistName = "default_playlist.txt";
		Intent thisActivityLauncherIntent = getIntent();
		setContentView(R.layout.activity_edit_alarm);
		Button saveAlarm = (Button) findViewById(R.id.saveAlarm);
		if (thisActivityLauncherIntent.getExtras() == null) {
			// If there are no extras given to this activity,
			// it means that the activity should proceed as if
			// it must create a new alarm.
			this.setTitle(R.string.title_activity_create_alarm);
			saveAlarm.setOnClickListener(new EditAlarmActivity.CreateAlarm());			
		} else {
			// If there are extras given to this activity,
			// it means that the activity should proceed as if 
			// to update an existing alarm. The alarm is given as
			// extras. Update the components inside in order to reflect
			// the data of the alarm. The saveAlarm button should also
			// be set to update the existing alarm, instead of adding a new 
			// one.
			this.setTitle(R.string.title_activity_edit_alarm);
			saveAlarm.setOnClickListener(new EditAlarmActivity.UpdateAlarm());
			Bundle alarmData = thisActivityLauncherIntent.getExtras();
			Alarm alarmFromIntent = new Alarm(alarmData.getString("Name"),
											  alarmData.getInt("Hours"),
											  alarmData.getInt("Minutes"),
											  alarmData.getBoolean("Enabled"),
											  alarmData.getBooleanArray("Schedule"),
											  alarmData.getString("Playlist"),
											  alarmData.getInt("Id"));
			EditText textField = (EditText) findViewById(R.id.alarmNameText);
			textField.setText(alarmFromIntent.getName());
			TimePicker time = (TimePicker) findViewById(R.id.pickTime);
			time.setCurrentHour(alarmFromIntent.getHours());
			time.setCurrentMinute(alarmFromIntent.getMinutes());
			ToggleButton[] days = new ToggleButton[7];
			(days[0] = (ToggleButton) findViewById(R.id.sunday)).setChecked(alarmFromIntent.getSunday());
			(days[1] = (ToggleButton) findViewById(R.id.monday)).setChecked(alarmFromIntent.getMonday());
			(days[2] = (ToggleButton) findViewById(R.id.tuesday)).setChecked(alarmFromIntent.getTuesday());
			(days[3] = (ToggleButton) findViewById(R.id.wednesday)).setChecked(alarmFromIntent.getWednesday());
			(days[4] = (ToggleButton) findViewById(R.id.thursday)).setChecked(alarmFromIntent.getThursday());
			(days[5] = (ToggleButton) findViewById(R.id.friday)).setChecked(alarmFromIntent.getFriday());
			(days[6] = (ToggleButton) findViewById(R.id.saturday)).setChecked(alarmFromIntent.getSaturday());			
			for (ToggleButton day: days) toggleClicked(day);	
			alarmName = alarmFromIntent.getName();
			playlistName = alarmName + "_playlist";
		}		
		Switch selectAllSongsButton = (Switch) findViewById(R.id.allSongsSwitch);
		selectAllSongsButton.setOnCheckedChangeListener(this);
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_alarm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void editPlaylist(View view) {
		Intent viewPlaylist = new Intent(this, PlaylistActivity.class);

		viewPlaylist.putExtra("alarmName", alarmName);
		startActivityForResult(viewPlaylist, 1);		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				// The playlist was saved fine.
				playlistName = data.getExtras().getString("playlist");
			}else if (resultCode == RESULT_CANCELED) {
				// The playlist was canceled somehow.
				playlistName = "default_playlist.txt";
			}
		}
		
	}

	
	public void toggleClicked(View view) {
		ToggleButton toggle = (ToggleButton) view;
		Drawable icon = null;
		if (toggle.isChecked()) icon = ACTIVE;
		else if (!toggle.isChecked()) icon = INACTIVE;
		toggle.setBackground(icon);		
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		final Button allSongs = (Button) findViewById(R.id.selectSongs);
		if (isChecked) { // If it's enabled, user wants to select all songs from the playlist. So, disable the button:
			allSongs.setVisibility(View.GONE);			
		} else {  // Else the user wants to select only some of the songs on the device. so enable it:
			allSongs.setVisibility(View.VISIBLE);
		}
		
	}
	
	protected Alarm retrieveAlarm() {
		if (getIntent().getExtras() != null) {
			Bundle alarmData = getIntent().getExtras();
			Alarm alarmFromIntent = new Alarm(alarmData.getString("Name"),
					  alarmData.getInt("Hours"),
					  alarmData.getInt("Minutes"),
					  alarmData.getBoolean("Enabled"),
					  alarmData.getBooleanArray("Schedule"),
					  alarmData.getString("Playlist"),
					  alarmData.getInt("Id"));
			return alarmFromIntent;
		}
		return null;
	}
	// -----------------------------------------------------------------
	
	public ArrayList<String> retrieveSongsOnDevice() {
		
		ArrayList<String> songs = new ArrayList<String>();
		
		String SELECTION = MediaStore.Audio.Media.MIME_TYPE + " LIKE ? AND " 
							+ MediaStore.Audio.Media.IS_MUSIC + " != 0";
		String[] SELECTION_ARGS = {"audio%"};
		String[] PROJECTION = {
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DURATION	
		};
		
		// First retrieve from the device:
		Cursor cursor = getContentResolver().query( MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
						PROJECTION, SELECTION, SELECTION_ARGS, null );
		addSongs(cursor, songs);
		
		// Then retrieve from the "primary" external storage on the device (such as an SD card):
		cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				PROJECTION, SELECTION, SELECTION_ARGS, null);
		addSongs(cursor, songs);
		
		return songs;
	}

	public String constructSong(Cursor _cursor) {
		String filePath = _cursor.getString(_cursor.getColumnIndex(MediaStore.Audio.Media.DATA        ));
		String fileName = _cursor.getString(_cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
		String artist 	= _cursor.getString(_cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST      ));
		String title 	= _cursor.getString(_cursor.getColumnIndex(MediaStore.Audio.Media.TITLE       ));
		long   duration = _cursor.getLong  (_cursor.getColumnIndex(MediaStore.Audio.Media.DURATION    ));
		Song song = new Song(filePath, fileName, artist, title, duration);
		return song.toString();
	}
	
	public void addSongs(Cursor _cursor, ArrayList<String> _songs) {
		if (_cursor.moveToFirst()) {
			do {
				_songs.add(constructSong(_cursor));
			} while (_cursor.moveToNext());
		}
	}
	
	// --------------------------------------------------------------------------------------------------

	public class CreateAlarm implements OnClickListener {

		@Override
		public void onClick(View view) {			
			
				db = new DBControl(getApplicationContext());
				// First retrieve the data from the various components. Use it to setup an alarm.
				TimePicker time = (TimePicker) findViewById(R.id.pickTime);
				ToggleButton[] days = new ToggleButton[7];
				days[0] = (ToggleButton) findViewById(R.id.sunday);
				days[1] = (ToggleButton) findViewById(R.id.monday);
				days[2] = (ToggleButton) findViewById(R.id.tuesday);
				days[3] = (ToggleButton) findViewById(R.id.wednesday);
				days[4] = (ToggleButton) findViewById(R.id.thursday);
				days[5] = (ToggleButton) findViewById(R.id.friday);
				days[6] = (ToggleButton) findViewById(R.id.saturday);
				EditText nameEdit = (EditText) findViewById(R.id.alarmNameText);
				Alarm savedAlarm;
				
				String name = nameEdit.getText().toString();		
				int hours = time.getCurrentHour();
				int minutes = time.getCurrentMinute();
				boolean enabled = true;
				boolean schedule[] = new boolean[7];
				for (int index = 0; index < 7; index++) {
					schedule[index] = days[index].isChecked();
				}
				
				Switch selectAllSwitch = (Switch) findViewById(R.id.allSongsSwitch);
				if (selectAllSwitch.isChecked()) {
					Playlist playlist = new Playlist(retrieveSongsOnDevice(), "default_playlist.txt");
					playlist.savePlaylistToFile(getApplicationContext());
				} else {
					if (playlistName.equals("default_playlist.txt")) {
						Playlist playlist = new Playlist(retrieveSongsOnDevice(), playlistName);
						playlist.savePlaylistToFile(getApplicationContext());
					} 
				}
				 
				int id = db.getAlarmCount() + 1;
				
				savedAlarm = new Alarm(name, hours, minutes, enabled, schedule, playlistName, id);
				db.addAlarm(savedAlarm, true);
				db.close();
				
				Intent returnHome = new Intent(getApplicationContext(), Home.class);
				startActivity(returnHome);
			
		}
	} 
	
	public class UpdateAlarm implements OnClickListener {

		@Override
		public void onClick(View view) {			
			
				db = new DBControl(getApplicationContext());
				// First retrieve the data from the various components. Use it to setup an alarm.
				// However, set the ID (and playlist for now) to be the same as the alarm that
				// was given to this Activity by the Intent.
				TimePicker time = (TimePicker) findViewById(R.id.pickTime);
				ToggleButton[] days = new ToggleButton[7];
				days[0] = (ToggleButton) findViewById(R.id.sunday);
				days[1] = (ToggleButton) findViewById(R.id.monday);
				days[2] = (ToggleButton) findViewById(R.id.tuesday);
				days[3] = (ToggleButton) findViewById(R.id.wednesday);
				days[4] = (ToggleButton) findViewById(R.id.thursday);
				days[5] = (ToggleButton) findViewById(R.id.friday);
				days[6] = (ToggleButton) findViewById(R.id.saturday);
				EditText nameEdit = (EditText) findViewById(R.id.alarmNameText);
				
				Alarm alarmFromIntent = retrieveAlarm();
				Alarm updatedAlarm;
				
				String name = nameEdit.getText().toString();		
				int hours = time.getCurrentHour();
				int minutes = time.getCurrentMinute();
				boolean enabled = true;
				boolean schedule[] = new boolean[7];
				for (int index = 0; index < 7; index++) {
					schedule[index] = days[index].isChecked();
				}
				
				int id = alarmFromIntent.getId();
				String playlist = alarmFromIntent.getPlaylist();
				
				updatedAlarm = new Alarm(name, hours, minutes, enabled, schedule, playlist, id);
				db.updateAlarm(updatedAlarm);
				db.close();
				
				Intent returnHome = new Intent(getApplicationContext(), Home.class);
				startActivity(returnHome);
			
		}
	} 
}
