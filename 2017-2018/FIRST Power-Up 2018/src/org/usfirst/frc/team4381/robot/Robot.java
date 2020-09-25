/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4381.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Compressor;


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
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	//PID
	private PIDController m_pid;
	double distance = 40000;
	
	//Gyro
	public ADXRS450_Gyro gyro;
	
	//Joysticks
	public Joystick driveStick_1;
	public Joystick driveStick_2;
	public Joystick specialStick;
	
	//Drive Talons and robot drive + Drive encoders
	private WPI_TalonSRX frontLeft;
	private WPI_TalonSRX frontRight;
	private WPI_TalonSRX backLeft;
	private WPI_TalonSRX backRight;
	public DifferentialDrive roboDrive;
	
	//Sucky uppy
	private WPI_VictorSPX sucky_L;
	private WPI_VictorSPX sucky_R;
	
	//Lifty uppy
	private WPI_TalonSRX lift_ControllerL;
	private WPI_TalonSRX lift_ControllerR;
	
	//Two Speed Shifter
	public DoubleSolenoid TwoSpeed;
	Compressor compress;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		//Encoders
		backLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
		backRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
		lift_ControllerL.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 10);
		
		backLeft.setSelectedSensorPosition(0, 0, 0);
		backRight.setSelectedSensorPosition(0, 0, 0);
		

		//Gyro
		gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
		gyro.reset();

		//Two speed shifter
		TwoSpeed = new DoubleSolenoid(0,1);
		compress = new Compressor(0);
		compress.setClosedLoopControl(true);
		
		//Joysticks
		driveStick_1 = new Joystick(0);
		driveStick_1 = new Joystick(1);
		specialStick = new Joystick(2);
		
		//Drive Talons and robot drive
		frontLeft = new WPI_TalonSRX(1);
		frontRight = new WPI_TalonSRX(3);
		
		backLeft = new WPI_TalonSRX(0);
		backRight = new WPI_TalonSRX(2);
		
		frontLeft.follow(backLeft);
		frontRight.follow(backRight);
		
		roboDrive = new DifferentialDrive(backLeft , backRight);
		
		//Sucky uppy
		sucky_L = new WPI_VictorSPX(5);
		sucky_R = new WPI_VictorSPX(6);
		
		//Lifty uppy
		lift_ControllerL = new WPI_TalonSRX(4);
		lift_ControllerR = new WPI_TalonSRX(7);
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
		
		m_pid = new PIDController(0.0001, 0, 0, new PIDSource() {
			PIDSourceType m_sourceType = PIDSourceType.kDisplacement;

			@Override
			public double pidGet() {
				return backLeft.getSelectedSensorPosition(0);
			}
			
			@Override
			public void setPIDSourceType(PIDSourceType pidSource) {
				m_sourceType = pidSource;
			}

			@Override
			public PIDSourceType getPIDSourceType() {
				return m_sourceType;
			}
		},  d -> roboDrive.tankDrive(-d, d));
	

		m_pid.setAbsoluteTolerance(100);
		m_pid.setSetpoint(distance);
		m_pid.setOutputRange(-.5, .5);
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		switch (m_autoSelected) {
			case kCustomAuto:
				// Put custom auto code here
				break;
			case kDefaultAuto:
			default:
				// Put default auto code here
				break;
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		//Gyro
		SmartDashboard.putNumber("Gyro", gyro.getAngle());
		
		//Encoders
		SmartDashboard.putNumber("EncoderRight", backLeft.getSelectedSensorPosition(0));
		SmartDashboard.putNumber("EncoderLeft", backRight.getSelectedSensorPosition(0));
		SmartDashboard.putNumber("Lift Encoder", lift_ControllerL.getSelectedSensorPosition(0));
		
		
		//Robot Drive
		roboDrive.tankDrive(driveStick_1.getY(), driveStick_2.getY());
		
		
		//Suckyuppy
		if(specialStick.getRawButton(2)) {
			sucky_L.set(1);
			sucky_R.set(1);
		}
		else {
			sucky_L.set(0);
			sucky_R.set(0);
		}
		
		//Pushy Outy
		if(specialStick.getRawButton(3)) {
			sucky_L.set(-1);
			sucky_R.set(-1);
		}
		else {
			sucky_L.set(0);
			sucky_R.set(0);
		}
		
		//Lift up
		if(specialStick.getRawButton(6)) {
			lift_ControllerL.set(1);
			lift_ControllerR.set(1);
		}
		else {
			lift_ControllerL.set(0);
			lift_ControllerR.set(0);
		}
		if(specialStick.getRawButton(11)) {
			lift_ControllerL.set(1);
			lift_ControllerR.set(1);
		}
		else {
			lift_ControllerL.set(0);
			lift_ControllerR.set(0);
		}
		
		//Lower down
		if(specialStick.getRawButton(7)) {
			lift_ControllerL.set(-1);
			lift_ControllerR.set(-1);
		}
		else {
			lift_ControllerL.set(0);
			lift_ControllerR.set(0);
		}
		if(specialStick.getRawButton(10)) {
			lift_ControllerL.set(-1);
			lift_ControllerR.set(-1);
		}
		else {
			lift_ControllerL.set(0);
			lift_ControllerR.set(0);
		}
		
		//Reset Encoders
		if (driveStick_1.getRawButton(8) || driveStick_2.getRawButton(8))
		{
			backRight.setSelectedSensorPosition(0, 0, 0);
			backLeft.setSelectedSensorPosition(0, 0, 0);
		}
		
		//Reset Gyro
		if (driveStick_1.getRawButton(7) || driveStick_2.getRawButton(7))
		{
			gyro.reset();
		}

		//Reset Lift Encoder
		if (driveStick_1.getRawButton(10)|| driveStick_2.getRawButton(10)) {
			lift_ControllerL.setSelectedSensorPosition(0, 0, 0);
		}
		
		
		//Two Speed shifter
				if (driveStick_2.getRawButton(1))
				{
					TwoSpeed.set(DoubleSolenoid.Value.kReverse);
				}
				else if (driveStick_1.getRawButton(1))
				{
					TwoSpeed.set(DoubleSolenoid.Value.kReverse);
				}
				else if (driveStick_1.getRawButton(1) && driveStick_2.getRawButton(1) )
				{
					TwoSpeed.set(DoubleSolenoid.Value.kReverse);
				}
				else
				{
					TwoSpeed.set(DoubleSolenoid.Value.kForward);
				}
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
