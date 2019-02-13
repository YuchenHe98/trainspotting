import java.util.concurrent.Semaphore;

import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

public class Lab1 {
    
    final static int DOWNWARDS = 1;
    final static int UPWARDS = -1;

    private TSimInterface tsi;

    Semaphore rightSideControl, leftSideControl, lowerBranchControl, upperBranchControl, lowerStationControl, crossControl; 
    
    int[][] sensors;

    public Lab1(int firstSpeed, int SecondSpeed) {
        
        tsi = TSimInterface.getInstance();
                
        rightSideControl = new Semaphore(1);
        leftSideControl = new Semaphore(1);
        lowerBranchControl = new Semaphore(1);
        lowerStationControl = new Semaphore(1);
        upperBranchControl = new Semaphore(1);
        crossControl = new Semaphore(1);
        
        initialiseSensors();

        Thread firstTrain = new Train(1, firstSpeed, DOWNWARDS);
        Thread secondTrain = new Train(2, SecondSpeed, UPWARDS);

        firstTrain.start();
        secondTrain.start();
    }
    
    private void initialiseSensors() {
        
        int[][] newSensors = {
            {1, 10}, //0                  
            {1, 9}, //1
            {4, 13}, //2
            {6, 11}, //3
            {6, 6}, //4 cross left
            {7, 9}, //5
            {6, 10}, //6
            {11, 7}, //7 cross right
            {10, 8}, //8 cross down
            {12, 9}, //9
            {13, 10}, //10
            {14, 7}, //11
            {15, 3}, //12
            {15, 5}, //13
            {15, 8}, //14
            {15, 11}, //15
            {15, 13}, //16
            {19, 9}, //17    
            {9, 5} //18 cross up
        };
        
        sensors = newSensors;
    }

    public class Train extends Thread {

        int id;
        int speed;
        int direction;
        boolean releaseUpperBranchPermit;
        boolean releaseLowerStationPermit;
        boolean releaseLowerBranchLeftPermit;
        boolean releaseLowerBranchRightPermit;

        public Train(int id, int speed, int direction) {
            this.id = id;
            this.speed = speed;
            this.direction = direction;
            this.releaseUpperBranchPermit = false;
            this.releaseLowerStationPermit = false;
            this.releaseLowerBranchRightPermit = false;
            this.releaseLowerBranchLeftPermit = false;
        }
        
        private boolean isSensorDetection(SensorEvent se, int index) {
            if (se.getXpos() == sensors[index][0] && se.getYpos() == sensors[index][1]) {
                return true;
            }
            
            return false;
        }
        
        @Override
        public void run() {
            try {
                
                tsi.setSpeed(id, speed);///Speed args are 15 and 15.
                
                while (true) {
                        
                    //System.out.println(lowerBranchControl.availablePermits());

                    SensorEvent se = tsi.getSensor(id);
                    
                    if ((se.getStatus() == SensorEvent.ACTIVE)) {
                        
                        /**********************
                         Downwards detections. 
                        **********************/
                        if (direction == DOWNWARDS) {
                            
                            /* Block the right side rail */
                            if (isSensorDetection(se, 11)) {

                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                            }
                            
                            if (isSensorDetection(se, 14)) { 
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                 
                                releaseUpperBranchPermit = true;
                                
                                tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                            }
                            
                            /* Release upperBranchControl and make the train go to the lower road
                               of the lower branch
                            */
                            if (isSensorDetection(se, 17)) {
                                
                                if (releaseUpperBranchPermit) {
                                    upperBranchControl.release();
                                    releaseUpperBranchPermit = false;
                                }
                                
                                if (lowerBranchControl.tryAcquire()) {
                                    tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                                } else {
                                    tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                                }
                                
                                // start only if the opposite train enters the branch
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                            }
                            
                            /* Release upper rail of the lower branch */
                            if (isSensorDetection(se, 1)) {
                                if (releaseLowerBranchLeftPermit) {
                                    lowerBranchControl.release();
                                    releaseLowerBranchLeftPermit = false;
                                }
                            }
                            
                            /* Block the left side rail */
                            if (isSensorDetection(se, 5)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                leftSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
                            }
                            
                            if (isSensorDetection(se, 6)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                leftSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                releaseLowerBranchLeftPermit = true;
                                    
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);

                            }
                            
                            /* Release the right side rail */
                            if (isSensorDetection(se, 9) || isSensorDetection(se, 10)) {
                                rightSideControl.release();
                            }
                            
                            /* left side release */
                            if (isSensorDetection(se, 2) || isSensorDetection(se, 3)) {
                                leftSideControl.release();
                            }
                            
                            /* block the lower branch of lower station */ 
                            if (isSensorDetection(se, 0)) {
                                //isOnDefaultPath = lowerStationControl.tryAcquire();
                                if (lowerStationControl.tryAcquire()) {
                                    tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                                } else {
                                    tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
                                }
                            }
                            
                            /* block the crossing */ 
                            if (isSensorDetection(se, 4) || isSensorDetection(se, 18)) {
                    
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                crossControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                            }
                            
                            /* release the crossing */ 
                            if (isSensorDetection(se, 7) || isSensorDetection(se, 8)) {
                                crossControl.release();
                            }
                        } 
                        
                        
                        /**********************
                         Upwards detections. 
                        **********************/                        
                        else {
                            
                            /* Block lower branch */
                            if (isSensorDetection(se, 1)) {
                                
                                if (lowerBranchControl.tryAcquire()) {
                                    tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
                                } else {
                                    tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
                                }
                                
                                // start only if the opposite train enters the branch
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                            }
                            
                            /* Release lower branch and block upper branch */
                            if (isSensorDetection(se, 17)) {
                                
                                if (releaseLowerBranchRightPermit) {
                                    lowerBranchControl.release();
                                    releaseLowerBranchRightPermit = false;
                                }
                                
                                //System.out.println("permits: " + upperBranchControl.availablePermits());
                                if (upperBranchControl.tryAcquire()) {
                                    tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                                } else {
                                    tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                                }
                            }
                            
                            /* Block the left side rail. */
                            if (isSensorDetection(se, 3)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                leftSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
                            }                    
                            
                            
                            if (isSensorDetection(se, 2)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                leftSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                releaseLowerStationPermit = true;
                                tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                            }
                            
                            
                            /* Release the left side rail and opposite coming train */
                            // 5 set to right
                            if (isSensorDetection(se, 5) || isSensorDetection(se, 6)) {
                                leftSideControl.release();
                            }
                            
                            /* Block the right side rail. */
                            if (isSensorDetection(se, 9)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                            }
                            
                            if (isSensorDetection(se, 10)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                releaseLowerBranchRightPermit = true;

                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }     
                            
                            /* Release right side control */
                            if (isSensorDetection(se, 11) || isSensorDetection(se, 14)) {
                                rightSideControl.release();
                            }
                            
                            /* Release lower station control */
                            if (isSensorDetection(se, 0)) {
                                
                                if (releaseLowerStationPermit) {
                                    lowerStationControl.release();
                                    releaseLowerStationPermit = false;
                                }
                            }
                            
                            /* Block crossing */
                            if (isSensorDetection(se, 8) || isSensorDetection(se, 7)) {
                    
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                crossControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                            }
                            
                            /* Release crossing */
                            if (isSensorDetection(se, 4) || isSensorDetection(se, 18)) {
                    
                                crossControl.release();
                            }
                        }
                        
                        /* Upper station */
                        if (isSensorDetection(se, 12) || isSensorDetection(se, 13)) {
                            if (se.getStatus() == SensorEvent.ACTIVE) {
                                tsi.setSpeed(se.getTrainId(), 0);
                                direction = DOWNWARDS;
                                sleep(1000 + Math.abs(speed) * 20);
                                speed = -speed;
                                tsi.setSpeed(se.getTrainId(), speed);
                            }
                        }

                        /* Lower station */
                        if (isSensorDetection(se, 15) || isSensorDetection(se, 16)){
                            if (se.getStatus() == SensorEvent.ACTIVE) {
                                tsi.setSpeed(se.getTrainId(), 0);
                                direction = UPWARDS;
                                sleep(1000 + Math.abs(speed) * 20);
                                speed = -speed;
                                tsi.setSpeed(se.getTrainId(), speed);
                            }
                        }        
                    }
                }
                            
            } catch (CommandException e) {
                e.printStackTrace();
                System.exit(1);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
