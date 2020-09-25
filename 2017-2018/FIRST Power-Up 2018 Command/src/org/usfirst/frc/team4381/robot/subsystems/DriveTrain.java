package org.usfirst.frc.team4381.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;


/**
 * 
 */
public class DriveTrain extends Subsystem {
	private WPI_TalonSRX frontLeft = new WPI_TalonSRX(1);
	private WPI_TalonSRX frontRight = new WPI_TalonSRX(3);
	private WPI_TalonSRX backLeft = new WPI_TalonSRX(0);
	private WPI_TalonSRX backRight = new WPI_TalonSRX(4);
	private DifferentialDrive roboDrive = new DifferentialDrive(backLeft , backRight);
	
    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    public DriveTrain() {
	frontLeft.follow(backLeft);
	frontRight.follow(backRight);
    }
    
    public void tankDrive(double leftSpeed , double rightSpeed) {
    	roboDrive.tankDrive(leftSpeed, rightSpeed);
    }
}
