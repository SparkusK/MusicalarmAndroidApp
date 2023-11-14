package functionality;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This is the class that will manage the DB functionality of the application.
 * 
 * @author Shane Kelly
 * @date June 2013
 */
public class DBControl extends SQLiteOpenHelper{
	

	// increment the database version if the database schema is changed.
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "Alarms.db";
	
	private static final String TABLE_ALARMS = "alarms";
	
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_HOUR = "hour";
	private static final String KEY_MINUTE = "minute";
	private static final String KEY_ENABLED = "enabled";
	private static final String KEY_SUNDAY = "sunday";
	private static final String KEY_MONDAY = "monday";
	private static final String KEY_TUESDAY = "tuesday";
	private static final String KEY_WEDNESDAY = "wednesday";
	private static final String KEY_THURSDAY = "thursday";
	private static final String KEY_FRIDAY = "friday";
	private static final String KEY_SATURDAY = "saturday";
	private static final String KEY_PLAYLIST = "playlist";

	private static final String columns[] = {KEY_ID, KEY_NAME, KEY_HOUR, KEY_MINUTE, KEY_ENABLED,
		KEY_SUNDAY, KEY_MONDAY, KEY_TUESDAY, KEY_WEDNESDAY, KEY_THURSDAY, KEY_FRIDAY, KEY_SATURDAY,
		KEY_PLAYLIST};
	
	public DBControl(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public int getAlarmCount() {
		Cursor amountOfRows = this.getWritableDatabase().rawQuery("SELECT COUNT(*) FROM " + TABLE_ALARMS, null);
		amountOfRows.moveToFirst();
		int rows = amountOfRows.getInt(0);
		return rows;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_ALARM_TABLE = "CREATE TABLE alarms ( "
							      + "id INTEGER PRIMARY KEY, " 
							      + "name TEXT NOT NULL, "
							      + "hour INTEGER NOT NULL, "
							      + "minute INTEGER NOT NULL, "
							      + "enabled INTEGER NOT NULL, "
							      + "sunday INTEGER NOT NULL, "
							      + "monday INTEGER NOT NULL, "
							      + "tuesday INTEGER NOT NULL, "
							      + "wednesday INTEGER NOT NULL, "
							      + "thursday INTEGER NOT NULL, "
							      + "friday INTEGER NOT NULL, "
							      + "saturday INTEGER NOT NULL, "
							      + "playlist TEXT NOT NULL)";
		db.execSQL(CREATE_ALARM_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS alarms");
		this.onCreate(db);
	}

	
	public Alarm getAlarm(int _id) {
		Alarm alarm;
		// Get the database
		SQLiteDatabase readableDatabase = this.getReadableDatabase();
		// Query the database for the alarm with id _id, retrieve
		// a cursor over the result set.
		Cursor dbCursor = readableDatabase.query(TABLE_ALARMS, 
												columns, 
												" id = ?", 
												new String[] { String.valueOf(_id)}, 
												null, 
												null, 
												null);
		// If there's a result, move to the first one. (there shouldn't be duplicates here,
		// if there are, I made a mistake somewhere.)
		if (dbCursor != null) dbCursor.moveToFirst();		
		alarm = buildAlarmFromPreconfiguredCursor(dbCursor);		
		return alarm;
		
	}
	
	/**
	 * Retrieves all the alarms from the DB, places them in an array list
	 * and returns it.
	 *  
	 */
	public ArrayList<Alarm> getAllAlarms() {
		ArrayList<Alarm> alarms = new ArrayList<Alarm>();
		Alarm alarm;
		String query = "SELECT * FROM " + TABLE_ALARMS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			do {
				Log.d("DBControl", "Getting alarm.");
				alarm = buildAlarmFromPreconfiguredCursor(cursor);
				alarms.add(alarm);
				Log.d("DBControl", "Finished.");
			} while (cursor.moveToNext());			
		}		
		Log.d("DBControl", "There are " + alarms.size() + " alarms.");
		return alarms;
		
	}

	
	/**
	 * This method will update an alarm in the DB. If the IDs correspond,
	 * this should work as intended. It will simply "rewrite" the alarm (once
	 * it's found in the db) based on the information given to the method.
	 * @param _alarm
	 * @return The number of rows affected.
	 */
	public int updateAlarm(Alarm _alarm) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = buildContentValuesFromAlarm(_alarm, false);
		int numRowsAffected = db.update(TABLE_ALARMS, values, 
				KEY_ID + " = ?", new String[] { String.valueOf(_alarm.getId())});
		db.close();
		return numRowsAffected;
	}
	
	/**
	 * Adding an alarm must ensure that the ID of the added alarm is 
	 * one more than the previous alarm. This ensures that the IDs are 
	 * sequential.
	 * 
	 * When accessing this method from outside this class, set the alarm's ID 
	 * to alarmCount + 1, and pass "true" to the boolean parameter.
	 * @param _alarm
	 */
	public void addAlarm(Alarm _alarm, boolean _includeID) {		
		SQLiteDatabase writeableDatabase = this.getWritableDatabase();
		ContentValues content = buildContentValuesFromAlarm(_alarm, _includeID);
		writeableDatabase.insert(TABLE_ALARMS, null, content);
		writeableDatabase.close();
		Log.d("DBControl", "An alarm was added.");
	}

	
	/**
	 * Deleting an alarm must ensure that the IDs remain sequential.
	 * @param _alarm
	 */
	public void deleteAlarm(Alarm _alarm) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ALARMS, " id = ?", new String[] {Integer.toString(_alarm.getId())});
		cleanTable(db);
		db.close();
	}
	
	private void cleanTable(SQLiteDatabase _writeableDatabase) {
		// First get an array containing all the IDs from the table.
		// To do this we need a cursor over a result set. We will only return the KEY_ID column to save time.
		Cursor db_ids = _writeableDatabase.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_ALARMS, null);
		ArrayList<Integer> al_ids = new ArrayList<Integer>();
		if ( db_ids.moveToFirst() ) {
			do {
				al_ids.add(db_ids.getInt(0));				
			} while (db_ids.moveToNext());
		}
		// Now, first check if it's already sequential. If it is, leave it.
		boolean isClean = true;
		int amountOfRows = al_ids.size();
		for (int index = 2; index < amountOfRows; index++) {
			if (al_ids.get(index) != al_ids.get(index - 1) + 1) {
				isClean = false;
			}
		}
		if (!isClean) {
			// Do the cleaning.
			// First get all the alarms.
			ArrayList<Alarm> alarms = getAllAlarms();
			// Now clear the table.
			clearTable(_writeableDatabase);
			// Now make the IDs proper.
			for (int id = 1; id <= amountOfRows; id++) {
				alarms.get(id - 1).setId(id);
			}
			// Now re-add all the fixed alarms.
			for (Alarm alarm: alarms) {
				addAlarm(alarm, true);
			}
		}		
	}
	

	
	public void clearTable(SQLiteDatabase _writeableDatabase) {
		_writeableDatabase.delete(TABLE_ALARMS, null, null);	
	}

	private ContentValues buildContentValuesFromAlarm(Alarm _alarm, boolean _includeID) {
		ContentValues content = new ContentValues();
		if (_includeID) content.put(KEY_ID, _alarm.getId() );
		content.put(KEY_NAME, _alarm.getName() );
		content.put(KEY_HOUR, _alarm.getHours() );
		content.put(KEY_MINUTE, _alarm.getMinutes() );
		content.put(KEY_ENABLED, _alarm.isEnabled() );
		content.put(KEY_SUNDAY, _alarm.getSunday() );
		content.put(KEY_MONDAY, _alarm.getMonday() );
		content.put(KEY_TUESDAY, _alarm.getTuesday() );
		content.put(KEY_WEDNESDAY, _alarm.getWednesday() );
		content.put(KEY_THURSDAY, _alarm.getThursday() );
		content.put(KEY_FRIDAY, _alarm.getFriday() );
		content.put(KEY_SATURDAY, _alarm.getSaturday() );
		content.put(KEY_PLAYLIST, _alarm.getPlaylist() );
		return content;
		
	}
	
	
	
	private Alarm buildAlarmFromPreconfiguredCursor(Cursor _cursor) {
		// Build the alarm from the db table information.
		Alarm alarm;
		int     id 			=  Integer.parseInt(_cursor.getString(0));
		String  name 		=  _cursor.getString(1);
		int     hours		=  Integer.parseInt(_cursor.getString(2 ));
		int     minutes		=  Integer.parseInt(_cursor.getString(3 ));
	    boolean enabled 	= (Integer.parseInt(_cursor.getString(4 )) == 1) ? true : false;
	    boolean sunday 		= (Integer.parseInt(_cursor.getString(5 )) == 1) ? true : false;
	    boolean monday 		= (Integer.parseInt(_cursor.getString(6 )) == 1) ? true : false;
	    boolean tuesday 	= (Integer.parseInt(_cursor.getString(7 )) == 1) ? true : false;
	    boolean wednesday 	= (Integer.parseInt(_cursor.getString(8 )) == 1) ? true : false;
	    boolean thursday 	= (Integer.parseInt(_cursor.getString(9 )) == 1) ? true : false;
	    boolean friday 		= (Integer.parseInt(_cursor.getString(10)) == 1) ? true : false;
	    boolean saturday	= (Integer.parseInt(_cursor.getString(11)) == 1) ? true : false;
	    String  playlist 	=  _cursor.getString(12);
	    boolean schedule[]  = {sunday, monday, tuesday, wednesday, thursday, friday, saturday};
		alarm = new Alarm( name, hours, minutes, enabled, schedule, playlist, id );
		return alarm;
	}
	
	@SuppressWarnings("unused")
	private Alarm buildAlarmFromPreconfiguredCursor(Cursor _cursor, int _id) {
		// Build the alarm from the db table information, but add a custom id.
		Alarm alarm = buildAlarmFromPreconfiguredCursor(_cursor);
		alarm.setId(_id);
		return alarm;
		

	}

	public void toggleEnabled(int _alarmId, boolean alarmIsEnabled) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ENABLED, !alarmIsEnabled );
		db.update(TABLE_ALARMS, values, KEY_ID + " = ?", new String[] {String.valueOf(_alarmId)});
		db.close();
		
	}
}
