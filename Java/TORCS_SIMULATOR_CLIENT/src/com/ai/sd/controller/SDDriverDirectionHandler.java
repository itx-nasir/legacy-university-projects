package com.ai.sd.controller;

import java.util.ArrayList;
import java.util.List;

import com.ai.sd.model.MySensorModel;
import com.ai.sd.model.StuckTypes;
import com.ai.sd.model.extermumTypes;
import com.ai.sd.model.Controller.Stage;


public class SDDriverDirectionHandler {
	private ControllersDriver myPara;
	boolean jump = false;
	double jumpTime = 0.0;
	List<Double> speeds = new ArrayList<Double>();
	List<Double> times = new ArrayList<Double>();
	private double estimatedTurn = 0.0;
//	double pTentativePos = 0.0;
	double additionSensors = 0;

	
	public SDDriverDirectionHandler (ControllersDriver myPara){
		this.setMyPara(myPara);
		
	}
	
	public SDDriverDirectionHandler (){
		
		
	}
	
	public float calcSteer(MySensorModel sensors, StuckTypes stuck, boolean isOut){
	    float steer = calculateSteer(sensors, stuck, isOut);
	    if(isOut)
	    	if(steer > 0.8 || steer < -0.8)
	    		steer = (float) (Math.signum(steer)*0.8);

        if (steer < -1)
            steer = -1;
        if (steer > 1)
            steer = 1;
        
//        pTentativePos = steer;
        
        return steer;
	}
	
	private float calculateSteer(MySensorModel sensors, StuckTypes stuck, boolean isOut){

		float steer = 0;
		if(stuck != StuckTypes.NoStuck){
			if(isOut){
				if(SDDriverHelper.isOnTheLeftHandSide(sensors)){//left hand side of the track
					if(SDDriverHelper.isTowardsInsideTheTrack(sensors)){//is towards the track, try to enter the track slowly (with a smooth direction)
						if(SDDriverHelper.isInTheCorrectDirection(sensors)){
							steer = (float) 0.0;//g=1
						}else{
							steer = (float) 0.2;//g=1
						}
						
					}else{
						if(SDDriverHelper.isInTheCorrectDirection(sensors)){
							steer = (float) 0.2;//g=-1
						}else{
							steer = (float) 0.2;//g=-1
						}
						
					}
					
				}else{//right hand side of the track
					if(SDDriverHelper.isTowardsInsideTheTrack(sensors)){//is towards the track, try to enter the track slowly (with a smooth direction)
						if(SDDriverHelper.isInTheCorrectDirection(sensors)){
							steer = (float) 0.0;//g=1
						}else{
							steer = (float) -0.2;//g=1
						}
						
					}else{
						if(SDDriverHelper.isInTheCorrectDirection(sensors)){
							steer = (float) -0.2;//g=-1
						}else{
							steer = (float) -0.2;//g=-1
						}
						
					}
				}
			}else{
				if(sensors.getGear() == -1){
					steer = -(float) Math.signum(sensors.getAngleToTrackAxis());
				}else{
					steer = (float) Math.signum(sensors.getAngleToTrackAxis());
				}				
			}
			steer *= 5.0; 

		}else{
			if(!isOut){
				int maxDistanceSensorIndx = SDDriverHelper.extermumIndexAngle(sensors.getTrackEdgeSensors(), extermumTypes.maximization);
				float distInfront = (float) SDDriverHelper.maximumDistanceInfront(sensors.getTrackEdgeSensors());
				
				double maxCorrectionSensors = getMyPara().getParameterByName("y2");
				double minCorrectionSensors = getMyPara().getParameterByName("y1");
				double soarDirection = 0.00;//turnDirection/(minInMiddle-maxIn);//[-1, 1] left or right of the track
				
				double maxSoar = getMyPara().getParameterByName("x2");//the minimum distance that it is still in soar (10 sensors for correction until that)
				double minSoar = getMyPara().getParameterByName("x1");//the maximum distance that it is in soar (3 sensor for correction from that on)
	
				double numberOfCorrectionSensors = SDDriverHelper.logSig(minCorrectionSensors, maxCorrectionSensors, minSoar, maxSoar, 0.99, distInfront);
				
				int correctionSensors = (int) numberOfCorrectionSensors;
				double minv = getMyPara().getParameterByName("a");
				double midv1 = getMyPara().getParameterByName("b");
				double midv2 = getMyPara().getParameterByName("c");
				double maxv = getMyPara().getParameterByName("d");
				
				double s = SDDriverHelper.trapasoide(minv, midv1, midv2, maxv, distInfront);

				if(Math.abs(getEstimatedTurn()) <= 0.1 || (Math.abs(getEstimatedTurn()) > 1.5 && distInfront > 195.0))
					s=0.0;

				if(getEstimatedTurn() < 0.0)//we are going to turn left
					s = -s;
				
//				correctionSensors/=2;

				soarDirection = s;
//				double[] opponentInfo = OpponentController1.opponentSteerReviser(sensors, s, pSteer, correctionSensors, myPara.getStage());
				
				double[] opponentInfo = OpponentController2.opponentsDirectionUpdater(sensors, steer, s, correctionSensors, myPara.getStage(), myPara);

				
				soarDirection = (float) opponentInfo[0];	
								
				correctionSensors =  (int) Math.round(opponentInfo[1]);				
				
				
				if(myPara.getStage().compareTo(Stage.COMPLEXITYMEASURER)==0){
					soarDirection = 0.0;
					correctionSensors = 10;
				}

				
				double steerToTurn = -weightedMean2(SDDriverHelper.angles, sensors.getTrackEdgeSensors(), 
						maxDistanceSensorIndx, correctionSensors, soarDirection, sensors); 
				
				steer = (float) (steerToTurn);
				
			}else{
				steer = -(float) ((float) ((-sensors.getAngleToTrackAxis()*2.0)+Math.signum(sensors.getTrackPosition())*13.0*Math.PI/180));
			}
		}
		
		if(sensors.getZ() > 0.65){//You are jumping, take care, do not steer too much,  
			jumpTime = sensors.getCurrentLapTime();
			jump = true;
		}
		if(jump && jumpTime + 0.5 > sensors.getCurrentLapTime()){
			steer = steer/5.0f;
		}else
			jump = false;
		
		return steer;
	}

	public double weightedMean(float[] angles, double[] distances, int baseAngle, 
			int count, double soarDirection, MySensorModel sensors){
		soarDirection = Math.pow(2.0, 2.0*soarDirection);//[-1, 1] left or right of the track
		double res=0;
		double sumDist = 0;
		for(int i=baseAngle - count; i <= baseAngle + count; ++i){
			int indx = Math.min(i, angles.length - 1);
			indx = Math.max(indx, 0);
			
			if(Math.abs(angles[indx]) < 2 && angles[indx] != 0)
				continue;
			
			double dis = distances[indx];
			if(i<baseAngle){
				dis /= soarDirection;
			}
			if(i>baseAngle){
				dis *= soarDirection;
			}
			if(Math.abs((sensors.getAngleToTrackAxis() + angles[indx]*Math.PI/180.0)) > Math.PI/2 ){
				continue;
			}
			res += ((angles[indx])*(dis));
			sumDist += distances[indx];
			
		}
		
		return SDDriverHelper.degreeToRadian(res/sumDist);
	}

	public double weightedMean2(float[] angles, double[] distances, int baseAngle, 
			int count, double soarDirection, MySensorModel sensors){
		soarDirection = Math.pow(2.0, 2.0*soarDirection);//[-1, 1] left or right of the track
		double resx=0;
		double resy=0.0;
		for(int i=baseAngle - count; i <= baseAngle + count; ++i){
			int indx = Math.min(i, angles.length - 1);
			indx = Math.max(indx, 0);
			
			if(Math.abs(angles[indx]) < 2 && angles[indx] != 0)
				continue;
			
			double dis = distances[indx];
			if(i<baseAngle){
				dis /= soarDirection;
			}
			if(i>=baseAngle){
				dis *= soarDirection;
			}
			if(i == baseAngle){
				dis *= 2.0;
			}
			if(Math.abs((sensors.getAngleToTrackAxis() + angles[indx]*Math.PI/180.0)) > Math.PI/2 ){
				continue;
			}
			resx += (SDDriverHelper.cosAng[indx]*(dis));
			resy += (SDDriverHelper.sinAng[indx]*(dis));
		}
		double res = Math.atan2(resy, resx);//*sumDist/sumDist2;

		return res;
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
