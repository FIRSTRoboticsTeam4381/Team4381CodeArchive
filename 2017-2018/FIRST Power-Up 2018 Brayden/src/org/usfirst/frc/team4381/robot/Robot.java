/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4381.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import edu.wpi.first.wpilibj.Ultrasonic;

import edu.wpi.first.wpilibj.SerialPort;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private static final String BlueAuto = "Blue Auto";
	private static final String RedAuto = "Red Auto";
	private static final String CrossAuto = "Crossline Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	private WPI_TalonSRX BotLeft;
	private WPI_TalonSRX BotRight;
	
	private WPI_VictorSPX TopLeft;
	private WPI_VictorSPX TopRight;
	
	private WPI_VictorSPX suckyUppy_R;
	private WPI_VictorSPX suckyUppy_L;
	
	/*private WPI_TalonSRX BotLeft;
	private WPI_TalonSRX BotRight;*/
	
	/*private WPI_TalonSRX TopLeftTest;
	private WPI_TalonSRX TopRightTest;
	private WPI_TalonSRX BotLeftTest;
	private WPI_TalonSRX BotRightTest;*/
	
	public DoubleSolenoid TwoSpeed;
	
	public String gameData;
	
	public Joystick stickL;
	public Joystick stickR;
	public Joystick spstick;
	public Joystick launch;
	
	public DifferentialDrive robodrive;
	
	//public DifferentialDrive robodriveTest;

	public float EncoderValueMax;
	public float EncoderValueMin;
	
	long startTime = 0;
	
	SpeedControllerGroup controlL;
	SpeedControllerGroup controlR;
	
	/*SpeedControllerGroup controlLTest;
	SpeedControllerGroup controlRTest;*/
	
	int count;
	double distance;
	double period;
	double rate;
	boolean direction;
	boolean stopped;
	
	double TurnAngle;
	double TurnAngleDEF;
	double StartAngle;
	double SlowdownAngle;
	
	public ADXRS450_Gyro gyro;
	
	Compressor compress;
	
	boolean latch;
	
	
	private static final double kHoldDistance = 12.0;
	
	private static final double kValueToInches = 0.125;
	
	private static final double kP = 0.5;

	private static final int kUltrasonicPort = 0;
	

	private AnalogInput ultrasonic;
	double currentSpeed;
	double currentDistance;
	
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		m_chooser.addDefault("Blue Auto", BlueAuto);
		m_chooser.addObject("RedAuto", RedAuto);
		m_chooser.addObject("Cross Line", CrossAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		SmartDashboard.putNumber("Turn Angle", TurnAngleDEF);
		
		//Intake
		suckyUppy_L = new WPI_VictorSPX(5);
		suckyUppy_R = new WPI_VictorSPX(6);
		
		
		//Camera
		CameraServer.getInstance().startAutomaticCapture();
		
		//Drive Stuff
		stickL = new Joystick(0);
		stickR = new Joystick(1);
		
		spstick = new Joystick(2);
		launch = new Joystick(3);
				
		BotLeft = new WPI_TalonSRX(0);
		BotRight = new WPI_TalonSRX(2);
				
		TopLeft = new WPI_VictorSPX(1);
		TopRight = new WPI_VictorSPX(3);
				
		TopLeft.follow(BotLeft);
		TopRight.follow(BotRight);
		
		robodrive = new DifferentialDrive(BotRight, BotLeft);
		
		
		//Gyro Stuff
		gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
		gyro.reset();
		
		TurnAngleDEF = 90;
		TurnAngle = 90;
		SlowdownAngle = 45;
		latch = false;
		
		StartAngle = gyro.getAngle();
		
		
		//Encoder Stuff
		BotLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		BotRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		
		BotLeft.setSelectedSensorPosition(0, 0, 0);
		BotRight.setSelectedSensorPosition(0, 0, 0);
		
		EncoderValueMax = 5000;
		
		
		//Compressor Stuff
		compress = new Compressor(0);
		
		compress.setClosedLoopControl(true);
		
		TwoSpeed = new DoubleSolenoid(0,1);
		
		//ultrasonic
		ultrasonic = new AnalogInput(kUltrasonicPort);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
		startTime = System.currentTimeMillis();
		
		TopRight.setSelectedSensorPosition(0, 0, 0);
		
		SmartDashboard.putString("Runing Encoders", "Out");
		SmartDashboard.putString("Runing time", "Out");
		
		gyro.reset();
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case BlueAuto:
				
				//inits timer
				SmartDashboard.putNumber("Timer", System.currentTimeMillis() - startTime);
				
				// Put custom auto code here
				
			/*	if(System.currentTimeMillis() - startTime < 3000) {
					
					if(TopRight.getSelectedSensorPosition(0) < EncoderValueMax) 
					{
						robodrive.tankDrive(.5, -gyro.getAngle() * 0.03);
						SmartDashboard.putString("Runing Encoders", "In");
					}
					else 
					{
						robodrive.tankDrive(0, 0);
						TopRight.setSelectedSensorPosition(0, 0, 0);
					}
				}
				else if(System.currentTimeMillis() - startTime > 3300 && System.currentTimeMillis() - startTime < 4000) 
				{
					if(gyro.getAngle() > 85 && gyro.getAngle() < 90) 
					{
						robodrive.tankDrive(0, 0);
					}
					else
					{
						robodrive.tankDrive(0, .4);
					}
				}
				
				else 
				{
					robodrive.tankDrive(0, 0);
				}
			}*/		

			
				break;
			case RedAuto:
			default:
				// Put default auto code here
				
				SmartDashboard.putNumber("Timer", System.currentTimeMillis() - startTime);
				
				/*if(gameData.charAt(0) == 'L')
				{
					if(System.currentTimeMillis() - startTime < 1500) {
						//Put left auto code here
					}
					else 
					{
						robodrive.tankDrive(0, 0);
					}
					
				} 
				else {
					//Put right auto code here
					
					robodrive.tankDrive(0, 0);
					
				}*/
			
				break;
		}
	}
	

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		SmartDashboard.putNumber("EncoderRight", BotLeft.getSelectedSensorPosition(0));
		SmartDashboard.putNumber("EncoderLeft", BotRight.getSelectedSensorPosition(0));
		
		SmartDashboard.putNumber("Gyro", gyro.getAngle());
		
		
		
		robodrive.tankDrive(-stickL.getY(), -stickR.getY());
		
		
		
		currentDistance = ultrasonic.getValue() * kValueToInches;
		
		SmartDashboard.putNumber("UltraSonic", currentDistance);

		currentSpeed = (kHoldDistance - currentDistance) * kP;

		if(launch.getRawButton(5))
		{
			robodrive.tankDrive(.5, 0);
		}
		else if(launch.getRawButton(6))
		{
			robodrive.tankDrive(-1, 0);
		}
		else if(launch.getRawButton(7))
		{
			robodrive.tankDrive(1, .7);
		}
		else
		{
			robodrive.tankDrive(0, 0);
			latch = false;
		}
		
		//Two Speed shifter
		if (spstick.getRawButton(1))
		{
			TwoSpeed.set(DoubleSolenoid.Value.kForward);
			SmartDashboard.putNumber("Soliniod", 1);
		}
		else
		{
			TwoSpeed.set(DoubleSolenoid.Value.kReverse);
			SmartDashboard.putNumber("Soliniod", 0);
		}
		
		//Sets encoders to 0
		if (spstick.getRawButton(11))
		{
			TopRight.setSelectedSensorPosition(0, 0, 0);
			TopLeft.setSelectedSensorPosition(0, 0, 0);
		}
		
		//Resets Gyro
		if (spstick.getRawButton(12))
		{
			gyro.reset();
		}
		
		//Intake and Output
		if(spstick.getRawButton(2))
		{
			suckyUppy_L.set(-1);
			suckyUppy_R.set(-1);
		}
		else
		{
			suckyUppy_L.set(0);
			suckyUppy_R.set(0);
		}
		
		if(spstick.getRawButton(3))
		{
			suckyUppy_L.set(1);
			suckyUppy_R.set(1);
		}
		else
		{
			suckyUppy_L.set(0);
			suckyUppy_R.set(0);
		}
	}


	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}