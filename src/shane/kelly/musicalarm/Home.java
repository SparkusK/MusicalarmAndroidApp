package shane.kelly.musicalarm;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import functionality.Alarm;
import functionality.DBControl;

public class Home extends Activity implements OnClickListener, OnLongClickListener {
	
	private DBControl db;
	
	private static final int MAXIMUM_ALARMS = 100;
	private static final int ALARM_BACKGROUND_COLOR = 0xFF9A9ABC;
	private static final int SCHEDULE_COLOR_IF_YES = 0xFF107F10;
	
	private static int ordinalCountAlarms = 1; // Starting point for alarms.	
	private static int ordinalCountSchedule = 1 + MAXIMUM_ALARMS + ((ordinalCountAlarms - 1) * 7); // Starting point for schedule views.
	private static int ordinalCountHours = 1 + MAXIMUM_ALARMS + (ordinalCountAlarms * 7); // Starting point for the hours views.
	private static int ordinalCountButtons = 2 + MAXIMUM_ALARMS + (ordinalCountAlarms * 7); // Starting point for the enabled buttons. 
	
	private Drawable inactive;
	private Drawable active;
    
	private final String[] daysText = {"Mo", "Tu", "We", "Th", "Fr", "Sa"};

	private ArrayList<Alarm> alarmList;
	
	// --------------------------------------------------------- //
	// =========== Android required/system subroutines ========= //	
	// --------------------------------------------------------- //
	
	// -------------- Activity Lifecycle Methods --------------- //
	
	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout baseLayout = (LinearLayout) findViewById(R.layout.activity_home);
		setContentView(R.layout.activity_home);		
		if (baseLayout == null) {			
			baseLayout = (LinearLayout) findViewById(R.id.HomeScreenLayout);
		}
		inactive = getResources().getDrawable(R.drawable.ic_power_inactive);
		active = getResources().getDrawable(R.drawable.ic_power_active);
		db = new DBControl(this);
		
		alarmList = db.getAllAlarms();

		db.close();
		inflateAlarms(baseLayout, alarmList);
		
		setAllAlarms(alarmList);
			    
	}

	private void setAllAlarms(ArrayList<Alarm> alarmList2) {
		Calendar cal = Calendar.getInstance();
		int dayToday = cal.get(Calendar.DAY_OF_WEEK);
		int hourToday = cal.get(Calendar.HOUR_OF_DAY);
		int minuteToday = cal.get(Calendar.MINUTE);
		for (Alarm alarm : alarmList2) {
			boolean[] weekSchedule = alarm.getSchedule();
			int firstDay = -1;
			for (int i = 0; i < 7; i++) {
				if (weekSchedule[i]) {
					firstDay = i;
					break;
				}
			}
			if (firstDay >= 0) {
				firstDay++; // the Calendar class starts counting the days from 1.
				if (firstDay == dayToday) {
					// The alarm should go off today. Check the time.
					if (hourToday < alarm.getHours()) {
						// Too late for the alarm. Try the next one
					} else if (hourToday == alarm.getHours()) {
						// It's within the hour. Check the minutes.
						if (minuteToday <= alarm.getMinutes()) {
							// It's too late for the alarm. Try the next one
						} else {
							// Schedule the alarm for today.
							Calendar calForAlarm = Calendar.getInstance();
							calForAlarm.add(Calendar.MINUTE, alarm.getMinutes() - minuteToday);
							Intent intent = new Intent(getApplicationContext(), AlarmReceiverActivity.class);
							intent.putExtra("pathName", alarm.getRandomSong(getApplicationContext()));
							PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(),  alarm.getId(), intent, PendingIntent.FLAG_ONE_SHOT);
							AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
							am.set(AlarmManager.RTC_WAKEUP, calForAlarm.getTimeInMillis(), pendIntent);
						}
					} else {
						// It's going to be in the next couple of hours. Schedule the alarm.
						Calendar calForAlarm = Calendar.getInstance();
						calForAlarm.add(Calendar.MINUTE, alarm.getMinutes() - minuteToday);
						calForAlarm.add(Calendar.HOUR, alarm.getHours() - hourToday);
						Intent intent = new Intent(getApplicationContext(), AlarmReceiverActivity.class);
						intent.putExtra("pathName", alarm.getRandomSong(getApplicationContext()));
						PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), alarm.getId(), intent, PendingIntent.FLAG_ONE_SHOT);
						AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
						am.set(AlarmManager.RTC_WAKEUP, calForAlarm.getTimeInMillis(), pendIntent);
					}
				} if (firstDay > dayToday) {
					// The alarm should go off in this week still.
					Calendar calForAlarm = Calendar.getInstance();
					calForAlarm.add(Calendar.MINUTE, alarm.getMinutes() - minuteToday);
					calForAlarm.add(Calendar.HOUR, alarm.getHours() - hourToday);
					calForAlarm.add(Calendar.DAY_OF_WEEK, firstDay - dayToday);
					Intent intent = new Intent(getApplicationContext(), AlarmReceiverActivity.class);
					intent.putExtra("pathName", alarm.getRandomSong(getApplicationContext()));
					PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), alarm.getId(), intent, PendingIntent.FLAG_ONE_SHOT);
					AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
					am.set(AlarmManager.RTC_WAKEUP, calForAlarm.getTimeInMillis(), pendIntent);
				} else {
					// The alarm should go off next week.
					Calendar calForAlarm = Calendar.getInstance();
					calForAlarm.add(Calendar.MINUTE, alarm.getMinutes() - minuteToday);
					calForAlarm.add(Calendar.HOUR, alarm.getHours() - hourToday);
					Intent intent = new Intent(getApplicationContext(), AlarmReceiverActivity.class);
					intent.putExtra("pathName", alarm.getRandomSong(getApplicationContext()));
					PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), alarm.getId(), intent, PendingIntent.FLAG_ONE_SHOT);
					AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
					am.set(AlarmManager.RTC_WAKEUP, calForAlarm.getTimeInMillis(), pendIntent);
				}
			}
			
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		LinearLayout baseLayout = (LinearLayout) findViewById(R.layout.activity_home);
		setContentView(R.layout.activity_home);		
		if (baseLayout == null) {			
			baseLayout = (LinearLayout) findViewById(R.id.HomeScreenLayout);
		}
		db = new DBControl(this);
		alarmList = db.getAllAlarms();
		inflateAlarms(baseLayout, alarmList);
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		db.close();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		db.close();
	}
	
	
	// ------------ GUI initialization methods ---------------- //
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_activity_actionbar, menu);
		return true;
	}
	
	// ----------- Navigation methods --------------------- //
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_new:			
			openNewAlarm();
			return true;
		case R.id.action_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		
		}
	}


	private void openSettings() {
		Intent openSettings = new Intent(this, SettingsActivity.class);
		startActivity(openSettings);		
	}

	private void openNewAlarm() {
		Intent openNewAlarm = new Intent(this, EditAlarmActivity.class);
		startActivity(openNewAlarm);
	}
	
	// --------------------- Event handling methods ----------------- //

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onClick(View view) {
//		String toastMessage = "";
		if (view.getId() - MAXIMUM_ALARMS <= 0) {			
//			toastMessage = " An alarm was clicked. ";
			// Start the Edit Alarm Activity with the data from the alarm that should be edited.
			Intent editAlarm = new Intent(this, EditAlarmActivity.class);
			editAlarm.setType(Intent.ACTION_EDIT);
			Alarm alarmToEdit = alarmList.get(view.getId() - 1);
			Bundle alarmBundle = new Bundle();
			alarmBundle.putInt("Id", alarmToEdit.getId());
			alarmBundle.putString("Name", alarmToEdit.getName());
			alarmBundle.putInt("Hours", alarmToEdit.getHours());
			alarmBundle.putInt("Minutes", alarmToEdit.getMinutes());
			alarmBundle.putBoolean("Enabled", alarmToEdit.isEnabled());
			alarmBundle.putBooleanArray("Schedule", alarmToEdit.getSchedule());
			alarmBundle.putString("Playlist", alarmToEdit.getPlaylist());
			editAlarm.putExtras(alarmBundle);
			startActivity(editAlarm);


		} else if ((view.getId() - MAXIMUM_ALARMS - 2) % 7 == 0) {
//			// Enabled button was clicked. Toggle the enabled state both on the 
			// gui and on the db, db first:
			db = new DBControl(getBaseContext());
			int alarmId = (view.getId() - MAXIMUM_ALARMS - 2) / 7;
			boolean alarmIsEnabled = alarmList.get(alarmId - 1).isEnabled();
			db.toggleEnabled(alarmId, alarmIsEnabled);			
			// After it's been toggled, update the state of the alarms in this activity:
			alarmList = db.getAllAlarms();
			// Now close the db.
			db.close();
			// Now update the GUI.
			Alarm alarm = alarmList.get(alarmId - 1); // db starts at 1, arraylist starts at 0, therefore - 1
			Button clickedButton = (Button) view;
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (alarm.isEnabled()) clickedButton.setBackgroundDrawable(active);
				else clickedButton.setBackgroundDrawable(inactive);
			}
			else {
				if (alarm.isEnabled()) clickedButton.setBackground(active);
				else clickedButton.setBackground(inactive);
			}
		} else {
//			toastMessage = "Something unexpected was clicked: " + String.valueOf(view.getId());
		}
//		Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
//		toast.show();		
	}
	
	@Override
	public boolean onLongClick(final View view) {
//		String toastMessage = "An alarm was long-clicked.";
//		Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
//		toast.show();
		
		AlertDialog dialog 
		= new AlertDialog.Builder(this)
			.setTitle(R.string.alarm_dialog_title)
			.setItems(R.array.alarm_dialog_array, new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						// Delete button was pressed.
						db = new DBControl(getBaseContext());
						db.deleteAlarm(alarmList.get(view.getId() - 1));
						Log.d("Dialogs", "delete button pressed, alarm deleted. ID = " + view.getId());
						setContentView(R.layout.activity_home);
						LinearLayout baseLayout = (LinearLayout)findViewById(R.layout.activity_home);
						if (baseLayout == null) {			
							baseLayout = (LinearLayout) findViewById(R.id.HomeScreenLayout);
						}
						baseLayout.invalidate();
						alarmList = db.getAllAlarms();
						inflateAlarms(baseLayout, alarmList);
					} else if (which == 1) {
						// Test button was pressed.
					}
				}
				
		    }).show();
		if (!dialog.isShowing()) {

		}
		return false;		
	}	
	
	
	// ------------------------------------------- //
	// ========== Custom subroutines ============= //
	// ------------------------------------------- //

	private void inflateAlarms(LinearLayout _baseLayout, ArrayList<Alarm> _alarmList) {
		ordinalCountAlarms = 1;
		for (Alarm alarm: _alarmList) {
			_baseLayout.addView(inflateAlarm(alarm));
			_baseLayout.addView(createDivider());
			ordinalCountAlarms++;			
		}
	}

	private TextView createDivider() {
		LinearLayout.LayoutParams divideParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 7);
		TextView divider = new TextView(this);
		divider.setText("");
		divider.setLayoutParams(divideParams);
		return divider;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressWarnings("deprecation")
	private RelativeLayout inflateAlarm(Alarm _alarm) {
		
		// First setup the RelativeLayout representing the Alarm.
		RelativeLayout alarmLayoutViewgroup = new RelativeLayout(this);
		alarmLayoutViewgroup.setId(ordinalCountAlarms);
		// When the alarm is clicked, it should take the user to the Edit
		// Alarm Activity with the alarm, so that the user can update the alarm.
		alarmLayoutViewgroup.setOnClickListener(this);
		// When the alarm is tapped-and-held, it should popup a dialog allowing the
		// user to delete or test the alarm.
		alarmLayoutViewgroup.setOnLongClickListener(this);
		
		// Add the various components to the RelativeLayout.		
		// First the hours view at the top left:
		TextView hours = new TextView(this);
		ordinalCountHours = 1 + MAXIMUM_ALARMS + (ordinalCountAlarms * 7); 
		hours.setId(ordinalCountHours);
		hours.setText(_alarm.getHoursText(0));
		hours.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		RelativeLayout.LayoutParams hoursParams = createLayoutParams();
		hours.setLayoutParams(hoursParams);
		alarmLayoutViewgroup.addView(hours);
		
		// Then the minutes view, just to the right (on the same bottom line):
		TextView minutes = new TextView(this);
		minutes.setText(_alarm.getMinutesText(0));
		minutes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		RelativeLayout.LayoutParams minutesParams = createLayoutParams();
		minutesParams.addRule(RelativeLayout.RIGHT_OF, ordinalCountHours);
		minutesParams.addRule(RelativeLayout.ALIGN_BOTTOM, ordinalCountHours);
		minutes.setPadding(2, 2, 2, 10);
		minutes.setLayoutParams(minutesParams);
		alarmLayoutViewgroup.addView(minutes);
		
		// Then the name view, on top on the y axis and in the middle on the x axis:
		TextView name = new TextView(this);
		name.setText(_alarm.getName());
		name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
		RelativeLayout.LayoutParams nameParams = createLayoutParams();
		nameParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		nameParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		name.setLayoutParams(nameParams);
		alarmLayoutViewgroup.addView(name);
				
		// Now we do the clickable button at the right, to disable/enable the alarm.
		Button enabled = new Button(this);
		// If the platform that this is running on, uses Ice cream sandwich or previous versions,
		// then the "deprecated" method setBackgroundDrawable(icon) should be used.
		// else, use setBackground(icon).
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (_alarm.isEnabled()) enabled.setBackgroundDrawable(active);
			else enabled.setBackgroundDrawable(inactive);
		}
		else {
			if (_alarm.isEnabled()) enabled.setBackground(active);
			else enabled.setBackground(inactive);
		}
		// Register an action listener to the button so that when the user clicks on it, it toggles the alarm's state
		// from inactive to active or vice versa. Note that this change should reflect in the underlying data structure too.
		// Might have to add SQL statements here, or something. For now I'll just change it in the alarms array
		enabled.setOnClickListener(this);
		RelativeLayout.LayoutParams enabledParams = createLayoutParams();
		enabledParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		enabledParams.addRule(RelativeLayout.CENTER_VERTICAL);
		enabled.setLayoutParams(enabledParams);
		ordinalCountButtons = 2 + MAXIMUM_ALARMS + (ordinalCountAlarms * 7);  
		enabled.setId(ordinalCountButtons);
		enabled.setPadding(1, 10, 3, 10);
		alarmLayoutViewgroup.addView(enabled);
		alarmLayoutViewgroup.setBackgroundColor(ALARM_BACKGROUND_COLOR);
		
		// Now do the schedule for the alarm. I'm placing 7 TextViews, the first one 
		// below the time, then the rest of them in line with the first one.
		TextView sunday = new TextView(this);
		sunday.setText("Su");
		ordinalCountSchedule = 1 + MAXIMUM_ALARMS + ((ordinalCountAlarms - 1) * 7);		
		sunday.setId(ordinalCountSchedule);
		ordinalCountSchedule++;
		sunday.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		if (_alarm.getSchedule()[0]) sunday.setTextColor(SCHEDULE_COLOR_IF_YES);
	    RelativeLayout.LayoutParams sundayParams = createLayoutParams();
	    sundayParams.addRule(RelativeLayout.BELOW, ordinalCountHours);
	    sunday.setLayoutParams(sundayParams);
	    sunday.setPadding(6, 2, 6, 2);
	    alarmLayoutViewgroup.addView(sunday);
	    
	    // Now the other days:
	    TextView[] otherDays = new TextView[6];
	    RelativeLayout.LayoutParams otherParams = null;	  
	    for (int i = 0; i < 6; i++) {
	    	otherDays[i] = new TextView(this);
	    	otherDays[i].setText(daysText[i]);
	    	otherDays[i].setId(ordinalCountSchedule);
	    	otherDays[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
	    	if (_alarm.getSchedule()[i + 1]) otherDays[i].setTextColor(0xFF107F10);
	    	otherParams = createLayoutParams();
	    	otherParams.addRule(RelativeLayout.RIGHT_OF, ordinalCountSchedule - 1);
	    	otherParams.addRule(RelativeLayout.ALIGN_TOP, ordinalCountSchedule - 1);
	    	otherDays[i].setLayoutParams(otherParams);
	    	otherDays[i].setPadding(6, 2, 6, 2);
	    	alarmLayoutViewgroup.addView(otherDays[i]);
	    	ordinalCountSchedule++;
	    }
	    
	    
		return alarmLayoutViewgroup;
	}

	private RelativeLayout.LayoutParams createLayoutParams() {
		return new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
	}
}
