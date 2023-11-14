package functionality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import android.content.Context;
import android.util.Log;

public class Alarm {

	/**
	 * Schedule for a weekly basis on which to repeat.
	 * schedule[0] - Sunday
	 *         [1] - Monday
	 *         [2] - Tuesday
	 *         [3] - Wednesday
	 *         [4] - Thursday
	 *         [5] - Friday
	 *         [6] - Saturday
	 */
	
	private boolean[] schedule = {false, false, false, false, false, false, false};	

	private String name;
	private String playlist;
	private boolean set; 
	private int minutes;
	private int hours;
	private int id;
		
	public Alarm() {}
	
	public Alarm(String _name, int _hours, int _minutes, boolean _enabled, boolean[] _schedule, String _playlist, int _id) {
		super();
		this.setName(_name);
		this.setHours(_hours);
		this.setMinutes(_minutes);
		this.setEnabled(_enabled);
		this.setSchedule(_schedule);
		this.setPlaylist(_playlist);
		this.setId(_id);
	}

	public void setId(int _id) {
		this.id = _id;		
	}

	public void setSchedule(boolean[] _schedule) {
		// First check if this is a valid array input.
		// It's invalid when there's more than 7 boolean values in it.
		if (_schedule.length != 7) return;
		else this.schedule = _schedule;
	}
	
	public String getPlaylist() {
		return playlist;
	}

	public void setPlaylist(String playlist) {
		this.playlist = playlist;
	}

	public void toggleSet() {
		this.set = !this.set;	
	}

	private void setEnabled(boolean _enabled) {
		this.set = _enabled;		
	}
	
	private void setMinutes(int _minutes) {
		if (_minutes >= 0 && _minutes <= 59) this.minutes = _minutes;
		else this.minutes = 0;		
	}

	private void setName(String _name) {
		this.name = _name;		
	}

	private void setHours(int _hours) {
		if (_hours >= 0 && _hours <= 23) this.hours = _hours; 
		else this.hours = 0;		
	}

	public boolean[] getSchedule() {
		return this.schedule;		
	}
	
	public int getHours() {
		return this.hours;
	}
	
	public int getMinutes() {
		return this.minutes;
	}
	
	public String getHoursText(int _format) {
		String hoursText = "";
		int hoursNumber = getHours();
		if (hoursNumber < 10) hoursText += "0";
		hoursText += Integer.toString(hoursNumber);
		return hoursText;
	}
	
	public String getMinutesText(int _format) {
		String minutesText = ":";
		int minutesNumber = getMinutes();
		if (minutesNumber < 10) minutesText += "0";
		minutesText += Integer.toString(minutesNumber);
		return minutesText;		
	}

	public String getName() {
		return this.name;
	}

	public boolean isEnabled() {
		return this.set;
	}

	public int getId() {
		return this.id;
	}

	public boolean getSunday() {
		return this.getSchedule()[0];
	}
	public boolean getMonday() {
		return this.getSchedule()[1];
	}
	public boolean getTuesday() {
		return this.getSchedule()[2];
	}
	public boolean getWednesday() {
		return this.getSchedule()[3];
	}
	public boolean getThursday() {
		return this.getSchedule()[4];
	}
	public boolean getFriday() {
		return this.getSchedule()[5];
	}
	public boolean getSaturday() {
		return this.getSchedule()[6];
	}

	public String getRandomSong(Context context) {
		String fileName = this.getPlaylist();
		Log.d("files", "null? File: " + fileName);
		File playlistFile = new File(context.getFilesDir(), fileName);
		
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(playlistFile));
			String line;
			while ((line = br.readLine()) != null) {
				text.append(line);
			}
			br.close();
		} catch(IOException e) {
			Log.d("files", "Error reading file. File:" + playlistFile.getAbsolutePath());
			
		}
		String songs = text.toString();
		String[] songsArray = songs.split(";");
		// values: {fileName, filePath, title, artist, duration}
		int randomSongNumber = new Random().nextInt(songsArray.length - 1);
		return songsArray[randomSongNumber].split(",")[1];
	
	}
	

}
