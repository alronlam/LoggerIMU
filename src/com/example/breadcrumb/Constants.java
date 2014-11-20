package com.example.breadcrumb;

public class Constants {

	// Bluetooth related
	public static final int REQUEST_ENABLE_BT = 1;
	public static final String SERVER_DEVICE_NAME = "SERVER";

	// Bluetooth Message related
	public static final byte SIGNAL_SERVER_START_MSG = 0;
	public static final byte SIGNAL_CAMERA_TO_TAKE_IMAGE = 2;
	public static final byte SIGNAL_SERVER_STOP_MSG = 3;

	// Time related
	public static final int MS_INS_SAMPLING_FREQUENCY = 10;
	public static final int MS_FREQUENCY_FOR_CAMERA_CAPTURE = 350;

	// Others
	public static final String INS_DATA_HEADER = "Acc_x,Acc_y,Acc_z,Gyro_x,Gyro_y,Gyro_z,Orient_x,Orient_y,Orient_z,Time\n";

}
