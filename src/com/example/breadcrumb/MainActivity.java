package com.example.breadcrumb;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import bluetooth.server.BluetoothService;
import data.SensorEntry;

public class MainActivity extends Activity implements SensorEventListener {

	// Sensor variables
	private SensorManager sensorManager;
	private Sensor senAccelerometer, senGyroscope, senOrientation, senProximity, senMagnetometer;

	// Time-related variables
	private Timer samplingDataTimer;

	// Bluetooth Bariables
	private BluetoothService bluetooth;

	// Logging variables
	private ArrayList<SensorEntry> sensorEntryBatch;
	private SensorEntry nextSensorEntryToAdd;
	private boolean isLogging;
	private int entriesRecorded;
	private int setsOfEntriesRecorded;
	private String rootFolder = Environment.getExternalStorageDirectory().toString();
	private String currFolder = rootFolder + "/breadcrumb_insdata";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** Bluetooth Related **/
		// Starts listening for Bluetooth connections
		bluetooth = new BluetoothService();
		bluetooth.start();

		/** Sensor Related **/
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		senOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		senGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		senMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		registerListeners();

		/** Others **/
		sensorEntryBatch = new ArrayList<SensorEntry>();
		samplingDataTimer = new Timer();
	}

	/** Methods for recording **/
	public void addCurrentSensorEntryToCurrentBatch() {
		/*
		 * Records the SensorEntry object named nextSensorEntryToAdd. Adds the
		 * object to the batch list, and then initializes a new empty
		 * SensorEntry for the next one.
		 */
		this.nextSensorEntryToAdd.setTimeRecorded(System.nanoTime());
		this.nextSensorEntryToAdd.buildSensorList();

		sensorEntryBatch.add(this.nextSensorEntryToAdd);
		this.nextSensorEntryToAdd = new SensorEntry();
	}

	public void runOneCycle() {
		this.sendSignalToCamera();
		this.logCurrentSensorEntriesBatch();
	}

	// Called when it's time to save the current batch
	private void logCurrentSensorEntriesBatch() {
		entriesRecorded++;

		int targetBatchSize = Constants.MS_FREQUENCY_FOR_CAMERA_CAPTURE / Constants.MS_INS_SAMPLING_FREQUENCY;

		ArrayList<SensorEntry> toProcess = new ArrayList<SensorEntry>(this.sensorEntryBatch.subList(0, targetBatchSize));
		this.sensorEntryBatch = new ArrayList<SensorEntry>(this.sensorEntryBatch.subList(targetBatchSize,
				targetBatchSize));

		this.writeBatchToFile(toProcess);
	}

	// Used to actually write to a file
	private void writeBatchToFile(ArrayList<SensorEntry> batch) {

		File file = new File(currFolder + "/" + entriesRecorded + ".csv");

		try {

			FileOutputStream outputStream = new FileOutputStream(file);

			if (!file.exists()) {
				file.createNewFile();
				outputStream.write(Constants.INS_DATA_HEADER.getBytes());
			}

			for (SensorEntry e : batch)
				outputStream.write((e.toRawString() + "," + e.getTimeRecorded() + "\n").getBytes());

			outputStream.close();

			setsOfEntriesRecorded++;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** Bluetooth related **/
	private void sendSignalToCamera() {
		byte[] msg = { Constants.SIGNAL_CAMERA_TO_TAKE_IMAGE };
		bluetooth.write(msg);
	}

	/** Sensor Listeners **/
	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		/*
		 * Called when there are changes in sensor readings. Simply updates the
		 * SensorEntry object representing the next SensorEntry to be recorded.
		 * 
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
		 * .SensorEvent)
		 */

		// TODO Auto-generated method stub

		/*** Stop if you're not logging anyway ***/
		if (!isLogging)
			return;

		Sensor mySensor = sensorEvent.sensor;

		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			// add to the sensor entry batch if time to add

			float x = sensorEvent.values[0];
			float y = sensorEvent.values[1];
			float z = sensorEvent.values[2];

			nextSensorEntryToAdd.setAcc_x(x);
			nextSensorEntryToAdd.setAcc_y(y);
			nextSensorEntryToAdd.setAcc_z(z);

		} else if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {

			// add to the sensor entry batch if time to add

			float x = sensorEvent.values[0];
			float y = sensorEvent.values[1];
			float z = sensorEvent.values[2];

			nextSensorEntryToAdd.setGyro_x(x);
			nextSensorEntryToAdd.setGyro_y(y);
			nextSensorEntryToAdd.setGyro_z(z);
		} else if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {

			// add to the sensor entry batch if time to add

			float x = sensorEvent.values[0];
			float y = sensorEvent.values[1];
			float z = sensorEvent.values[2];

			nextSensorEntryToAdd.setOrient_x(x);
			nextSensorEntryToAdd.setOrient_y(y);
			nextSensorEntryToAdd.setOrient_z(z);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/*** Start/Stop Logging methods ***/
	public void startLogging() {
		isLogging = true;
		samplingDataTimer.scheduleAtFixedRate(new SensorRecordTimerTask(this), 0, Constants.MS_INS_SAMPLING_FREQUENCY);
	}

	private void stopLogging() {
		samplingDataTimer.cancel();
		isLogging = false;
		sensorEntryBatch.clear();
		entriesRecorded = 0;
		currFolder = rootFolder + "/breadcrumb_insdata" + this.setsOfEntriesRecorded;
	}

	/*** Activity Lifecycle Related Methods ***/
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	protected void onResume() {
		super.onResume();
		registerListeners();
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	private void registerListeners() {
		sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, senOrientation, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(this, senMagnetometer, SensorManager.SENSOR_DELAY_FASTEST);
	}

}
