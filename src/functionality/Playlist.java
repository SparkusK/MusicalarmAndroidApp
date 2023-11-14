package functionality;

import java.io.FileOutputStream;
import java.util.ArrayList;


import android.content.Context;
import android.util.Log;

public class Playlist {
	
	private ArrayList<String> songs;
	private String name;
	
	public Playlist(ArrayList<String> _songs, String _name) {
		this.setSongs(_songs);
		this.setName(_name);		
	}
	
	public ArrayList<String> getSongs() {
		return songs;
	}
	public void setSongs(ArrayList<String> songs) {
		this.songs = songs;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void savePlaylistToFile(Context _context) {
		String fileName = this.getName();
		String contents = parseContentsFromSongs();
		FileOutputStream outputStream;
		try {
			Log.d("debug", "file name: " + fileName);
			outputStream = _context.openFileOutput(fileName, Context.MODE_PRIVATE);
			outputStream.write(contents.getBytes());
			outputStream.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private String parseContentsFromSongs() {
		String output = "";
		for (String song: this.getSongs()) {
			output += song + ";";
		}
		output.substring(0, output.length() - 1);
		return output;
	}

	
	
}
