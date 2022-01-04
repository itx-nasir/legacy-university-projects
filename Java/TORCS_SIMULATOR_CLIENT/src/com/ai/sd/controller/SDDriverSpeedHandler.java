package com.ai.sd.controller;

import com.ai.sd.model.Controller.Stage;
import com.ai.sd.model.MySensorModel;
import com.ai.sd.model.StuckTypes;

public class SDDriverSpeedHandler {
//	boolean stuck = false;
	private ControllersDriver myPara;
	private double estimatedTurn = 0.0;
//	private double pSpeed = 0.0;
	
	public SDDriverSpeedHandler(ControllersDriver myPara){
		this.setMyPara(myPara);
//		this.stuck = stuck;
	}
	
	public SDDriverSpeedHandler(){
		
	}
	
	public float [] calcBrakeAndAccelPedals(MySensorModel sensors, double currentSteer, StuckTypes isStuck, boolean isOut){
		float accel_and_brake = calcAccel(sensors, currentSteer, isOut, isStuck);
		float accel,brake;
        if (accel_and_brake>0)
        {
            accel = filterASR(sensors, accel_and_brake);
            brake = 0;
        }
        else
        {
            accel = 0;
            // apply ABS to brake
            brake = filterABS(sensors,-accel_and_brake);
        }
//System.out.println(accel);
        float [] res = {brake, accel};
        return res;
	}
	
	float clutching(MySensorModel sensors, float clutch)
	{
		  	 
	  float maxClutch = SDDriverHelper.clutchMax;
	  
	  // Check if the current situation is the race start
	  if ((sensors.getCurrentLapTime()<SDDriverHelper.clutchDeltaTime  && 
			  sensors.getDistanceRaced()<SDDriverHelper.clutchDeltaRaced))
	    {
		  clutch = maxClutch;
	    }

	  // Adjust the current value of the clutch
	  if(clutch > 0)
	  {
	    double delta = SDDriverHelper.clutchDelta;
	    if (sensors.getGear() < 2)
		{
	      // Apply a stronger clutch output when the gear is one and the race is just started
		  delta /= 2.0;
	      maxClutch *= SDDriverHelper.clutchMaxModifier;
	      if (sensors.getCurrentLapTime() < SDDriverHelper.clutchMaxTime)
	        clutch = maxClutch;
		}

	    // check clutch is not bigger than maximum values
		clutch = Math.min(maxClutch,clutch);

		// if clutch is not at max value decrease it quite quickly
		if (clutch!=maxClutch)
		{
		  clutch -= delta;
		  clutch = Math.max((float) 0.0,clutch);
		}
		// if clutch is at max value decrease it very slowly
		else
			clutch -= SDDriverHelper.clutchDec;
	  }
	  return clutch;
	}

	
	private float filterASR(MySensorModel sensors,float accel){
		float asrSlip = (float) SDDriverHelper.asrSlip;
		float asrRange = (float) SDDriverHelper.asrRange;
		float asrMinSpeed = (float) SDDriverHelper.asrMinSpeed;
		
		// convert speed to m/s
		float speed = (float) (sensors.getSpeed() / 3.6);
		// when spedd lower than min speed for abs do nothing
	    if (speed > asrMinSpeed)
	        return accel;
	    
	    // compute the speed of wheels in m/s
	    float slip = 0.0f;
	    for (int i = 0; i < 4; i++)
	    {
	    	double wheelsSpeed = sensors.getWheelSpinVelocity()[i] * SDDriverHelper.wheelRadius[i];
	        slip += wheelsSpeed;
	    }
	    	    	    
	    // slip is the difference between actual speed of car and average speed of wheels
	    slip = speed - slip/4.0f;
	    
	    // when slip too high apply ASR
	    if (-slip > asrSlip)
	    {
	        accel = accel + (slip + asrSlip)/asrRange;
	    }
	    // check brake is not negative, otherwise set it to zero
	    if (accel<0)
	    	return 0;
	    else
	    	return accel;
	}

	private float filterABS(MySensorModel sensors,float brake){
		float absSlip = (float) getMyPara().getParameterByName("absSlip");//DriverControllerHelperE5.absSlip;
		float absRange = (float) getMyPara().getParameterByName("absRange");//DriverControllerHelperE5.absRange;
		float absMinSpeed = (float) getMyPara().getParameterByName("absMinSpeed");//DriverControllerHelperE5.absMinSpeed;
		
		// convert speed to m/s
		float speed = (float) (sensors.getSpeed() / 3.6);
		// when spedd lower than min speed for abs do nothing
	    if (speed < absMinSpeed)
	        return brake;
	    
	    // compute the speed of wheels in m/s
	    float slip = 0.0f;
	    for (int i = 0; i < 4; i++)
	    {
	    	double wheelsSpeed = sensors.getWheelSpinVelocity()[i] * SDDriverHelper.wheelRadius[i];
	        slip += wheelsSpeed;
	    }
	    
	    // slip is the difference between actual speed of car and average speed of wheels
	    slip = speed - slip/4.0f;
	    // when slip too high applu ABS
	    if (slip > absSlip)
	    {
	        brake = brake - (slip - absSlip)/absRange;
	    }
	    
	    // check brake is not negative, otherwise set it to zero
	    if (brake<0)
	    	return 0;
	    else
	    	return brake;
	}
	
	public float calcAccel(MySensorModel sensors, double currentSteer, boolean isOut, StuckTypes isStuck){
		double targetSpeed = 0.0;
		double coef = 1.0;
		double coefT = .5;//Math.pow(2.0,(-targetSpeed+sensors.getSpeed())/10.0);//0.05
		if(isStuck != StuckTypes.NoStuck){
			coef = coefT;
			if(sensors.getGear() < 0){
				targetSpeed = myPara.minSpeed;
				
			}else
				targetSpeed = myPara.minSpeed*2.0;
			
			if(isOut){
				if(Math.abs(currentSteer)<0.1){//we are in a good direction, push the pedal
					coef = 0.9;
				}else{
					coef = coefT;
				}
			}
		}else{
			if(isOut){
				if(sensors.getGear() < 0){
					targetSpeed = myPara.minSpeed;
					coef = coefT;
				}else
				{	
				
					if(Math.abs(currentSteer)<0.1){//we are in a good direction, push the pedal aggressively
						targetSpeed = Math.max(myPara.minSpeed*4.0, sensors.getSpeed());
						coef = 0.6;//.7;
					}else{//dont be aggressive, otherwise you will start turning
						targetSpeed = myPara.minSpeed*3.0;
						coef = 0.03;//0.1;
					}
					
				}
			}else{
				targetSpeed = targetSpeed(sensors, currentSteer);
				coef = 1.0;
			}
		}
//		System.out.println(isStuck + ", " + targetSpeed);
		return (float) (2.0/(1.0+Math.exp(coef*(Math.abs(sensors.getSpeed()) - targetSpeed))) - 1.0);

	}
	
	private double targetSpeed(MySensorModel sensors, double currentSteer){
		if(sensors.getGear() < 0){
			return myPara.minSpeed;
		}
		
		double minSpeed = myPara.minSpeed;//if -90 or 90 has the maximum distances, we need to go slow
		double maxSpeed = myPara.maxNormalSpeed;//if 0 has the maximum distance, it depends on the distance
		double maxRange = SDDriverHelper.maxSensorRangeProximity;

		float distInfront = (float) SDDriverHelper.maximumDistanceInfront(sensors.getTrackEdgeSensors());		

		double minAggTurn = -getMyPara().getParameterByName("e2");
		double maxAggTurn = getMyPara().getParameterByName("e2");
		double minAggV = getMyPara().getParameterByName("lambda1");
		double maxAggV = getMyPara().getParameterByName("lambda2");
		
//		System.out.println(maxAggV);
		
		double aggressionSpeed = SDDriverHelper.logSig(minAggV, maxAggV, minAggTurn, maxAggTurn, 0.99, Math.abs(getEstimatedTurn()));
//		System.out.println(aggressionSpeed + " " + Math.abs(getEstimatedTurn()) + " " + maxAggV);
		
		double res = ((Math.pow(distInfront/maxRange, aggressionSpeed))*((maxSpeed-minSpeed)))
				+minSpeed;
		
		res = OpponentController2.opponentSpeedReviser(sensors, res, estimatedTurn, distInfront, currentSteer, myPara);
		
		double speed = Math.min(res, maxSpeed);

		speed=myPara.dangZoneSpeedUpdate(sensors.getDistanceFromStartLine(), speed);

		if(myPara.getStage().compareTo(Stage.COMPLEXITYMEASURER)==0)
			speed = 50.0;
		
		if(speed<myPara.minSpeed){
			speed = myPara.minSpeed;
		}
		
		return speed;
	}
	
	public int calculateGear(MySensorModel sensors, StuckTypes stuck){
		
	    int currentGear = sensors.getGear();
	    double rpm  = sensors.getRPM();

      
	    if(stuck == StuckTypes.AngularStuck || stuck == StuckTypes.WallStuck || stuck == StuckTypes.OpponentStuck){
	    	int gear=sensors.getGear(); // gear R
	    	if(stuck == StuckTypes.WallStuck || stuck == StuckTypes.OpponentStuck)
	        	gear = -1;
	    	else
	    		if(stuck == StuckTypes.AngularStuck)
	    			gear = 1;
	    	
	        return gear;
	    }else{
		    // if gear is 0 (N) or -1 (R) just return 1 
		    if (currentGear<1)
		        return 1;
		    // check if the RPM value of car is greater than the one suggested 
		    // to shift up the gear from the current one     
		    if (currentGear <6 && rpm >= SDDriverHelper.gearUp[currentGear-1])
		        return currentGear + 1;
		    else
		    	// check if the RPM value of car is lower than the one suggested 
		    	// to shift down the gear from the current one
		        if (currentGear > 1 && rpm <= SDDriverHelper.gearDown[currentGear-1])
		            return currentGear - 1;
		        else // otherwhise keep current gear
		            return currentGear;
	    }
	}

	public double getEstimatedTurn() {
		return estimatedTurn;
	}

	public void setEstimatedTurn(double estimatedTurn) {
		this.estimatedTurn = estimatedTurn;
	}

	public ControllersDriver getMyPara() {
		return myPara;
	}

	public void setMyPara(ControllersDriver myPara) {
		this.myPara = myPara;
	}
}
