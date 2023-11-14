package shane.kelly.musicalarm;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import functionality.Playlist;
import functionality.Song;
import functionality.SongArrayAdapter;

public class PlaylistActivity extends Activity {

	private ArrayList<String> songsData;
	private ArrayList<String> songs;
	private String playlistName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);
		songsData = retrieveSongsOnDevice();
		playlistName = "default_playlist";
		ListView listView = (ListView) findViewById(R.id.playlist_listview);
		final SongArrayAdapter adapter = new SongArrayAdapter(this, songsData);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> _parent, View _view, int _position,	long _id) {
				Log.d("Items", "Item was clicked. Position: " + _position + " id: " + _id);	
				if (_view instanceof Button) {
					Log.d("Items", "Button was clicked.");
				} else if (_view instanceof CheckBox) {
					Log.d("Items", "Check Box was clicked.");
				}
			}
			
		});
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SongArrayAdapter adapter = (SongArrayAdapter) ((ListView) findViewById(R.id.playlist_listview)).getAdapter();
		songs = adapter.getSelectedSongs();
		if (songs.size() == 0) {
			// No songs were selected. Return an intent saying "default_playlist", save the default
			// playlist to a file
			Playlist playlist = new Playlist(retrieveSongsOnDevice(), "default_playlist");
			playlist.savePlaylistToFile(getBaseContext());
			Intent data = new Intent();
			data.putExtra("playlistName", "default_playlist");
			setResult(RESULT_CANCELED, data);
		}
		else {
			// At least one song was selected. Return an intent giving this playlist's name,
			// save the playlist to a file.
			Intent fromLauncher = getIntent();
			playlistName = fromLauncher.getExtras().getString("alarmName") + "_playlist";
			Playlist playlist = new Playlist(songs, playlistName);
			playlist.savePlaylistToFile(getApplicationContext());
			setResult(RESULT_OK, new Intent().putExtra("playlistName", playlistName));
		}
	}
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.playlist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
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
	
	public void savePlaylist(View _view) {
		onStop();
		
		
	}


}
