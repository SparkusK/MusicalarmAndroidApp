package functionality;

public class Song {
	

	private String filePathToPlay;
	private String fileName;
	private String artist;
	private String title;
	private long duration;
	
	public String getFilePathToPlay() {
		return filePathToPlay;
	}
	public void setFilePathToPlay(String filePathToPlay) {
		this.filePathToPlay = filePathToPlay;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public Song(String _filePathToPlay, String _fileName, String _artist, String _title, long _duration) {
		this.setFilePathToPlay(_filePathToPlay);
		this.setFileName(_fileName);
		this.setArtist(_artist);
		this.setTitle(_title);
		this.setDuration(_duration);
	}

	

	public String getDurationString() {
		String output = "";
		long duration = this.getDuration();
		int totalSeconds = (int) duration/1000;
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		String seperator = ":";
		String minutesString = Integer.toString(minutes);
		String secondsString = Integer.toString(seconds);
		
		if (minutes < 10) minutesString = "0" + minutes;
		if (seconds < 10) secondsString = "0" + seconds;
		output = minutesString + seperator + secondsString;
		return output;

	}
	
	
	@Override
	public String toString() {
		// values: {fileName, filePath, title, artist, duration}
		return this.getFileName() + "," 
				+ this.getFilePathToPlay() + "," 
				+ this.getTitle() + ","
				+ this.getArtist() + ","
				+ this.getDurationString();		
	}
	
}
