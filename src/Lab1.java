import java.util.concurrent.Semaphore;

import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

public class Lab1 {
    
    final static int DOWNWARDS = 1;
    final static int UPWARDS = -1;

    private TSimInterface tsi;

    //private AddingArrayList<Semaphore> semaphores;
    Semaphore rightSideControl, leftSideControl, lowerBranchControl, upperBranchControl, lowerStationControl, crossControl;
    
    int[][] sensors;
    int[][] switches;

    public Lab1(int firstSpeed, int SecondSpeed) {
        
        tsi = TSimInterface.getInstance();
                
        rightSideControl = new Semaphore(1);
        leftSideControl = new Semaphore(1);
        lowerBranchControl = new Semaphore(1);
        lowerStationControl = new Semaphore(1);
        upperBranchControl = new Semaphore(1);
        
        initialiseSensors();

        Thread train1 = new Train(1, firstSpeed, DOWNWARDS);
        Thread train2 = new Train(2, SecondSpeed, UPWARDS);

        train1.start();
        train2.start();
    }
    
    private void initialiseSensors() {
        
        int[][] newSensors = {
            {1, 10}, //0                  
            {1, 9}, //1
            {4, 13}, //2
            {6, 11}, //3
            {6, 7}, //4
            {7, 9}, //5
            {6, 10}, //6
            {11, 7}, //7
            {10, 8}, //8
            {12, 9}, //9
            {13, 10}, //10
            {14, 7}, //11
            {15, 3}, //12
            {15, 5}, //13
            {15, 8}, //14
            {15, 11}, //15
            {15, 13}, //16
            {19, 9} //17
            
            //{5, 10} //18
        };
        
        sensors = newSensors;
    }

    public class Train extends Thread {

        int id;
        int speed;
        int maxSpeed;
        int direction;

        public Train(int id, int speed, int direction) {
            this.id = id;
            this.speed = speed;
            maxSpeed = 16;
            this.direction = direction;
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
                
                //speed = maxSpeed;
                tsi.setSpeed(id, speed);///Speed args are 15 and 15.
                //tsi.setSpeed(id, 0); // Prevent the speed to become 0 when reaching a station
                
                while (true) {
                    SensorEvent se = tsi.getSensor(id);
                    
                    System.out.println(upperBranchControl.availablePermits());

                    if ((se.getStatus() == SensorEvent.ACTIVE)) {
                        
                        
                        /* Downwards detections. */
                        
                        if (direction == DOWNWARDS) {
                            
                            // sensor: 14, 7; switch: 17, 7
                            if (isSensorDetection(se, 11)) {

                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                            }
                            
                            // sensor: 15, 8; switch: 17, 7
                            if (isSensorDetection(se, 14)) { 
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                    
                                tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                            }
                            
                            // sensor: 19, 9; switch: 15, 9
                            if (isSensorDetection(se, 17)) {
                                
                                if (lowerBranchControl.tryAcquire()) {
                                    tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                                } else {
                                    tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                                }
                                
                                if (upperBranchControl.availablePermits() == 0) {
                                    upperBranchControl.release();
                                }
                            }
                            
                            // release upper rail of the lower branch
                            if (isSensorDetection(se, 1)) {
                                if (lowerBranchControl.availablePermits() == 0) {
                                    lowerBranchControl.release();
                                }
                            }
                            
                            if (isSensorDetection(se, 5)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                leftSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
                            }
                            
                            // sensor: 6, 9; switch: 4, 9
                            if (isSensorDetection(se, 6)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                leftSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);

                            }
                            
                            // sensor: 12, 9; switch: 15, 9
                            if (isSensorDetection(se, 9)) {
                                if (rightSideControl.availablePermits() == 0) {
                                    rightSideControl.release();
                                }                                
                                //tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }
                            
                            // sensor: 13, 10
                            if (isSensorDetection(se, 10)) {
                                if (rightSideControl.availablePermits() == 0) {
                                    rightSideControl.release();
                                }                            
                            }
                            
                            // left side release
                            if (isSensorDetection(se, 2)) {
                                if (leftSideControl.availablePermits() == 0) {
                                    leftSideControl.release();
                                }                               
                                //tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }
                            
                            // left side release
                            if (isSensorDetection(se, 3)) {
                                if (leftSideControl.availablePermits() == 0) {
                                    leftSideControl.release();
                                }                            
                            }

                            // sensor: 1, 10; switch: 3, 11 
                            if (isSensorDetection(se, 0)) {
                                
                                if (lowerStationControl.tryAcquire()) {
                                    tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                                } else {
                                    tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
                                }
                            }
                        } 
                        
                        /* Upwards detections. */
                        else {
                            
                            if (isSensorDetection(se, 1)) {
                                
                                if (lowerBranchControl.tryAcquire()) {
                                    tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
                                } else {
                                    tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
                                }
                            }
                            
                            // sensor: 19, 9; switch: 17, 7
                            if (isSensorDetection(se, 17)) {
                                
                                if (lowerBranchControl.availablePermits() == 0) {
                                    lowerBranchControl.release();
                                }
                                // tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                                
                                if (upperBranchControl.tryAcquire()) {
                                    tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                                } else {
                                    tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                                }
                            }
                            

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
                                
                                tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                            }
                            
                            
                            // left side release
                            if (isSensorDetection(se, 5)) {
                                if (leftSideControl.availablePermits() == 0) {
                                    leftSideControl.release();
                                }
                                
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);

                            }
                            
                            // left side release
                            if (isSensorDetection(se, 6)) {
                                if (leftSideControl.availablePermits() == 0) {
                                    leftSideControl.release();
                                }
                                
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);

                            }    
                            
                            // sensor: 12, 9; switch: 15, 9
                            if (isSensorDetection(se, 9)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                            }
                            
                            // sensor: 13, 10; switch: 15, 9
                            if (isSensorDetection(se, 10)) {
                                
                                int originalSpeed = speed;
                                tsi.setSpeed(se.getTrainId(), 0);
                                rightSideControl.acquire();
                                tsi.setSpeed(se.getTrainId(), originalSpeed);
                                
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }     
                            
                            if (isSensorDetection(se, 11)) {
                                if (rightSideControl.availablePermits() == 0) {
                                    rightSideControl.release();
                                }                                
                            }
                            
                            if (isSensorDetection(se, 14)) {
                                if (rightSideControl.availablePermits() == 0) {
                                    rightSideControl.release();
                                }                            
                            }
                            
                            if (isSensorDetection(se, 0)) {
                                
                                if (lowerStationControl.availablePermits() == 0) {
                                    lowerStationControl.release();
                                }
                            }
                        }

                        //North station
                        if ((se.getXpos() == 15) && ((se.getYpos() == 5) || se.getYpos() == 3)) {
                            if (se.getStatus() == SensorEvent.ACTIVE) {
                                tsi.setSpeed(se.getTrainId(), 0);
                                direction = DOWNWARDS;
                                sleep(1000 + Math.abs(speed) * 15);
                                speed = -speed;
                                tsi.setSpeed(se.getTrainId(), speed);
                            }
                        }

                        //South station
                        if ((se.getXpos() == 15) && ((se.getYpos() == 11) || se.getYpos() == 13)){
                            if (se.getStatus() == SensorEvent.ACTIVE) {
                                tsi.setSpeed(se.getTrainId(), 0);
                                direction = UPWARDS;
                                sleep(1000 + Math.abs(speed) * 15);
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
