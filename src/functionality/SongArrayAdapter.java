package functionality;


import java.io.IOException;
import java.util.ArrayList;

import shane.kelly.musicalarm.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SongArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> songs;
	private static final int FIELD_SONGPATH = 1;
	private static final int FIELD_TITLE 	= 2;
	private static final int FIELD_ARTIST 	= 3;
	private static final int FIELD_DURATION = 4;
	public MediaPlayer songPlayer;
	public boolean isPlaying;
	public Button playingButton;
	private static Drawable DRAWABLE_PLAY;
	private static Drawable DRAWABLE_STOP;
	private final ArrayList<String> selectedSongs;
	
	// values: {fileName, filePath, title, artist, duration}
	
	public SongArrayAdapter(Context context, ArrayList<String> songs) {
		super(context, R.layout.playlist_list_item, songs);
		DRAWABLE_PLAY = context.getResources().getDrawable(R.drawable.ic_action_play);
		DRAWABLE_STOP = context.getResources().getDrawable(R.drawable.ic_action_stop);
		this.context = context;
		this.songs = songs;
		songPlayer = new MediaPlayer();
		isPlaying = false;
		selectedSongs = new ArrayList<String>();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.playlist_list_item, parent, false);
		// Set the data for the song! The rowView View represents the song.
		TextView titleView = (TextView) rowView.findViewById(R.id.playlist_item_title);
		titleView.setText(parse(songs.get(position), FIELD_TITLE));
		TextView artistView = (TextView) rowView.findViewById(R.id.playlist_item_artist);
		artistView.setText(parse(songs.get(position), FIELD_ARTIST));
		TextView durationView = (TextView) rowView.findViewById(R.id.playlist_item_duration);
		durationView.setText(parse(songs.get(position), FIELD_DURATION));
		Button playButton = (Button) rowView.findViewById(R.id.playlist_item_button);
		playButton.setOnClickListener(new buttonHandler());
		CheckBox cb = (CheckBox) rowView.findViewById(R.id.playlist_item_selected);
		cb.setOnCheckedChangeListener(new CheckboxHandler());
		TextView invisibleSongPathView = (TextView) rowView.findViewById(R.id.playlist_item_invisible_songpath);
		invisibleSongPathView.setText(parse(songs.get(position), FIELD_SONGPATH));
		rowView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				songPlayer.stop();
				isPlaying = false;
				if (playingButton != null) {
					playingButton.setBackgroundDrawable(DRAWABLE_PLAY);
					playingButton = null;
				}
				return false;
			}
			
		});
		return rowView;
	}
	
	private String parse(String _song, int _field) {
		return _song.split(",")[_field];
	}

	public ArrayList<String> getSelectedSongs() {
		return this.selectedSongs;
	}
	
	public class buttonHandler implements OnClickListener {
		





		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View _view) {
			RelativeLayout parent = (RelativeLayout) _view.getParent();
			String songPath = (String) ((TextView) parent.findViewById(R.id.playlist_item_invisible_songpath)).getText();
			Log.d("Items", "Button was clicked. Song path:" + songPath);
			if (!isPlaying) {
				songPlayer.reset();
				try {
					songPlayer.setDataSource(songPath);
					songPlayer.prepare();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				songPlayer.start();
				isPlaying = true;
				playingButton = (Button) _view;
				playingButton.setBackgroundDrawable(DRAWABLE_STOP);

			} else {
				songPlayer.stop();
				isPlaying = false;
				playingButton.setBackgroundDrawable(DRAWABLE_PLAY);
				playingButton = null;
			}
			
		}
		
	}
	
	public class CheckboxHandler implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton _view, boolean _isChecked) {
			Log.d("Items", "A checkbox was clicked.");
			
			String filePath = "";
			String artist = "";
			String title = "";
			String duration = "";
			RelativeLayout parent = (RelativeLayout) _view.getParent();
			
			filePath = (String) ((TextView) parent.findViewById(R.id.playlist_item_invisible_songpath)).getText();
			title = (String) ((TextView) parent.findViewById(R.id.playlist_item_title)).getText();
			artist = (String) ((TextView) parent.findViewById(R.id.playlist_item_artist)).getText();
			duration = (String) ((TextView) parent.findViewById(R.id.playlist_item_duration)).getText();
			
			String song = title + "," + filePath + "," + title + "," + artist + "," + duration;
			if (_isChecked) {
				selectedSongs.add(song);
			} else {
				selectedSongs.remove(song);
			}
		
			
					// values: {fileName, filePath, title, artist, duration}
//			Song selectedSong = new Song(filePath, fileName, artist, title, duration);
			
		}


		
		
	}
	
	
	
}
