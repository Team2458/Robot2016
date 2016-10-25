/*Robot 2016 code v2.0.1 Modified off Ravi Malik's code from last year by Sarah Abowitz and Nick Bhimani
 * last update: 2/6/16, Sarah Abowitz. Added our USB camera and beamCutter. Also Jaguar support for
 * shooters :)
 * 
 * Ravi said we should replace/delete some mecanum stuff because we're not using that shiz.
 * 
 * - improve indicators of shooters (string window!!) (motors running, motors stopped)
 *
*/
package org.usfirst.frc.team2458.robot;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */

public class Robot extends IterativeRobot {
	RobotDrive myRobot;
	Joystick leftStick, rightStick, operStick;
	Jaguar frontShooter, backShooter, armPivot;
	Talon placeholder;
	int autoLoopCounter;
	boolean isGrabberOpen, activated, isOn, triggerButtonPressed, lowButtonPressed, upButtonPressed,
			downButtonPressed, armUp, armDown, isBrakeEngaged;
	//change isGrabberOpen to something better
	boolean rightStickZero, atUpperLimit, atLowerLimit, goingUp;
	boolean isShooting, isLowShooting, loopLoad, didAuto, rampUp, armed;
	double frontShootV = -1.0; // V(elocity). 
    double backShootV = -1.0; //5
    double loadV = 0.25; //25
    double captureV = 0.25;
    double armV = -0.5; //0.25? geez this thing is fast
	CameraServer camera;
	DigitalInput beamCutter, armContact;
	Timer timer = new Timer();
	Timer frontTimer = new Timer();
	Timer backTimer = new Timer();
	Timer autoTimer = new Timer();
	final static boolean DEBUG = false;
	
	
	//these two buttons determine arm angle rn
	int UPBUTTON = 5;		// Button number, change this one!!!
	int DOWNBUTTON = 3;
	
	int HIGHSHOOT = 1;
	int ACTIVATE = 5;
	int LOWSHOOT = 6; //6
	
	
	//final static double HOLD = 0.25;
	// (SARAH) Sam also told me to work on configuring 5 & 3 on new
	// controller for raising/setting gilloutine.
	//final static int UP = 5;
	//final static int DOWN = 3;
	final static double DEADZONE = 0.1; // previously .01
	final static double TWISTDEADZONE = 0.5;
	final static double DEADZONEMACANUM = 0.1; // dead zone - yValue Mecanum
	final static double DRIVESPEEDSCALE = 1.0;
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {
		myRobot = new RobotDrive(0, 1); //0,2
		myRobot.setExpiration(0.1);
		
		//we don't need to invert motors this year but just incase vv
		//myRobot.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
		//myRobot.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
		leftStick = new Joystick(0);
	 	rightStick = new Joystick(1);
	 	operStick = new Joystick(2);
	 	frontShooter = new Jaguar(2);
	 	backShooter = new Jaguar(3);
	 	armPivot = new Jaguar(4);
	 	
		isGrabberOpen = false;
		isBrakeEngaged = false;
		triggerButtonPressed = false;
		rightStickZero = true;
		atUpperLimit = false;
		atLowerLimit = false;
		goingUp = true;
		camera = CameraServer.getInstance();
        camera.setQuality(50);
        camera.startAutomaticCapture("cam0");
        beamCutter = new DigitalInput(0);
        armContact = new DigitalInput(1);
        
        //armContact.requestInterrupts(new InterruptHandlerFunction<Object>() {
	 		//@Override
	 		//public void interruptFired( int mask, Object param ) {
	 			//atUpperLimit = true;
	}
	
	/**
	 * This function is run once each time the robot enters autonomous mode
	 * SARAH: But we should def plan something for this besides vv
	 */
	public void autonomousInit() {
		didAuto = false;
		 while (didAuto == false) {
			autoTimer.start();
			while (autoTimer.hasPeriodPassed(3.6)==false) {
				myRobot.tankDrive(-0.7, -0.7);//-0.6
			}
			if (autoTimer.hasPeriodPassed(3.0) == false) {
				didAuto = true;
			}
		}
		autoTimer.stop();
		
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		//backShooter.set(-frontShootV);
		
		/*if (didAuto == false) {
			autoTimer.start();
		}
		//SmartDashboard.putNumber("DB/String 3", operStick.);
		//SmartDashboard.putNumber("DB/String 4", operStick.getThrottle());
		if (autoTimer.hasPeriodPassed(3.0)== false) {
			//if (operStick.getAxis(3) > 0) {
				didAuto = true;
				myRobot.tankDrive(-0.6, -0.6);
				SmartDashboard.putString("DB/String 2", "Fury Mode");
			//} else {
				//myRobot.tankDrive(-0.5, -0.5); //0.5 x2
				//SmartDashboard.putString("DB/String 2", "Standard Mode");
			//}
		} else {
			autoTimer.stop();
			myRobot.tankDrive(0.0, 0.0);
		}*/
		
	}
	
	/**
	 * This function is called once each time the robot enters tele-operated mode
	 */
	public void teleopInit(){
		isGrabberOpen = false;
		triggerButtonPressed = false;
		rightStickZero = true;
		isBrakeEngaged = false;
		SmartDashboard.putBoolean("DB/LED 2", false);
		activated = false;
		isOn = false;
		upButtonPressed = false;
		downButtonPressed = false; 
		armUp = false;
		armDown = false;
		lowButtonPressed = false;
		loopLoad = false;
		isShooting = false;
		isLowShooting = false;
		//goingUp = true;
		SmartDashboard.putString("DB/String 7", "~Roboticum Mundus Est~");
		SmartDashboard.putString("DB/String 1", "");
		SmartDashboard.putString("DB/String 2", "");
		SmartDashboard.putString("DB/String 4", "");
		SmartDashboard.putString("DB/String 5", "");
		SmartDashboard.putString("DB/String 6", "");
		rampUp = true;
		armed = false;
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {
		//backShooter.set(0.25);

		/*double rValTank = rightStick.getY();
		if(rValTank <= DEADZONEMACANUM && rValTank >= -DEADZONEMACANUM)
			rValTank = 0;
		if(rValTank > 0)
			rValTank = rValTank - DEADZONEMACANUM;
		else if (rValTank < 0)
			rValTank = rValTank + DEADZONEMACANUM;
		rValTank *= DRIVESPEEDSCALE;
			double lValTank = leftStick.getY();
			myRobot.tankDrive(lValTank, rValTank);
		
		*/
		myRobot.tankDrive(leftStick, rightStick); 
        SmartDashboard.putBoolean("DB/LED 0", beamCutter.get());
        
        //if Luke was right about this, uncomment pls
        
       
        	
        if (operStick.getRawButton(CAPTURE)){
        			frontTimer.start();
        			SmartDashboard.putString("DB/String 1", "Front Wheels ON");
        			while (frontTimer.hasPeriodPassed(1.0) == false)
        			{
        			frontShooter.set(captureV);
        			}
        			frontTimer.stop();
        			frontShooter.set(0);
        }
        				
        if(operStick.getRawButton(ACTIVATE)){
        		if (!activated) {
        			activated = true;
        			if (isOn) {
        				isOn = false;
        				frontShooter.set(0);
        				SmartDashboard.putString("DB/String 1", "Front Wheels OFF");
        				SmartDashboard.putString("DB/String 2", "");
        				//SmartDashboard.putBoolean("DB/LED 2", false);
        				//frontTimer.stop();        				
        				//frontTimer.reset();
        				 
        			} else {
        				//this is the stuff nick did cause he is a 1337 hacker. smartdash is meeeee
        				isOn = true;
        				//if (rampUp == true) {
	        				frontTimer.start();
	        				SmartDashboard.putString("DB/String 1", "Front Wheels ON");
	        				while (frontTimer.hasPeriodPassed(10.0)== false)
	        				{
	        				frontShooter.set((frontTimer.get()/-10.0));	
	        				SmartDashboard.putString("DB/String 1", "Front Wheels ON");
	        				}
	        				SmartDashboard.putString("DB/String 2", "AT CRITICAL SPEED");
	        				frontTimer.stop();
	        				frontTimer.reset();
        				//} else {
        					//frontShooter.set(frontShootV);
        				SmartDashboard.putString("DB/String 1", "Front Wheels ON");
        				//frontTimer.start();
        			}
        				//isOn = !isOn;
        		}
        	}else {
	        		if (activated) {
	        		activated = false;
	        		//frontShooter.set(0);
	        	
	        		//SmartDashboard.putBoolean("DB/LED 2", false);
	        		}
        	 }
        	
			
        	
        	/*if (frontTimer.hasPeriodPassed(10.0)) {
        		frontTimer.stop();
        		frontShooter.set(0);
        	}*/
        	
        	
  			if(operStick.getRawButton(HIGHSHOOT)) {
				if( !triggerButtonPressed ) {
				triggerButtonPressed = true;
				if( isShooting ) {
					isShooting = false;
					//frontShooter.set(0);
					backShooter.set(0);
					SmartDashboard.putString("DB/String 6", "Back Wheels OFF");
				} else {
					isShooting = true;
					//if (frontTimer.hasPeriodPassed(5.0)) { 
						//SmartDashboard.putBoolean("DB/LED 3", true);
						SmartDashboard.putString("DB/String 6", "Loading Shooter");
						//SmartDashboard.putBoolean("DB/LED 3", beamCutter.get());		
						timer.start();
						while (loopLoad == false) {
							loopLoad = beamCutter.get();
							backShooter.set(loadV);
							//SmartDashboard.putBoolean("DB/LED 3", beamCutter.get());
							loopLoad = timer.hasPeriodPassed(0.5);
							
						}
						timer.stop();
						//SmartDashboard.putBoolean("DB/LED 3", beamCutter.get());
						//----
						SmartDashboard.putString("DB/String 6", "Shooting!");
						backShooter.set( backShootV );
						//SmartDashboard.putString("DB/String 5", " ");
						//frontShooter.set(0);
						
						//timer.reset();
						//timer.start();
						
						loopLoad = false;
						frontTimer.stop();
						frontTimer.reset();
					//} else {
						//SmartDashboard.putString("DB/String 5", "Shooter Isn't Ready!");
				}
			}
		} 
		else {
			// Trigger not pressed
			if( triggerButtonPressed ){
				triggerButtonPressed = false;
				
			}
		}
  				
  			/*if(timer.hasPeriodPassed(3.0)){
  				timer.stop();
  				timer.reset();
  				backShooter.set(0);
  			}*/
  			
  			if(operStick.getRawButton(LOWSHOOT)) {
				//SmartDashboard.putString("DB/String 4", "Button Pressed");
  				if( !lowButtonPressed ) {
				lowButtonPressed = true;
				//SmartDashboard.putBoolean("DB/LED 3", true);
				if( isLowShooting ) {
					isLowShooting = false;
					//frontShooter.set(0);
					backShooter.set(0);
					SmartDashboard.putString("DB/String 6", "Back Wheels OFF");
				} else {
					isLowShooting = true;
					backShooter.set( -frontShootV );	 		
					SmartDashboard.putString("DB/String 6", "Back Wheels ON");
					//backTimer.start();
				}
			}
		} 
		else {
			//SmartDashboard.putString("DB/String 4", "Button Unpressed");
			if( lowButtonPressed ){
				lowButtonPressed = false;
				//SmartDashboard.putBoolean("DB/LED 3", false);
				//frontShooter.set(0);
				//backShooter.set(0);
			}
		}
        	
  			
  			
  			
  			
  			
  		/*if (backTimer.hasPeriodPassed(10.0)) {
        		backTimer.stop();
        		backShooter.set(0);
  		}*/
  			
		
			
			//arm stuff is commented until we test it

		//if (operStick.getRawButton(UPBUTTON)) && atUpperLimit == false) {
				
		} //end of line
	
				
	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		LiveWindow.run();
	}
	
}
