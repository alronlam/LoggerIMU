package com.example.breadcrumb;

import java.util.TimerTask;

public class SensorRecordTimerTask extends TimerTask {

	private MainActivity mainActivity;
	private long lastTimeSignalWasSentToCamera;

	public SensorRecordTimerTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		this.lastTimeSignalWasSentToCamera = System.currentTimeMillis();
	}

	@Override
	public void run() {
		long currTime = System.currentTimeMillis();

		if (currTime - lastTimeSignalWasSentToCamera >= Constants.MS_FREQUENCY_FOR_CAMERA_CAPTURE) {
			// send signal first to minimize delay between ins and camera
			// recordings
			mainActivity.runOneCycle();
			lastTimeSignalWasSentToCamera = currTime;
		} else
			mainActivity.addCurrentSensorEntryToCurrentBatch();
	}
}
