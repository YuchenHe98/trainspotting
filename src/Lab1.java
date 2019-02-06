import java.util.concurrent.Semaphore;

import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

public class Lab1 {
    
    final static int DOWNWARDS = 1;
    final static int UPWARDS = -1;

    private TSimInterface tsi;

    //private AddingArrayList<Semaphore> semaphores;
    Semaphore upperStationControl, rightSideControl, lowerBranchControl, leftSideControl, lowerStationControl;
    
    int[][] sensors;
    int[][] switches;

    public Lab1(int firstSpeed, int SecondSpeed) {
        
        tsi = TSimInterface.getInstance();
                
        rightSideControl = new Semaphore(1);
        leftSideControl = new Semaphore(1);
        lowerBranchControl = new Semaphore(1);
        lowerStationControl = new Semaphore(1);
        
        initialiseSensors();

        Thread train1 = new Train(1, firstSpeed, DOWNWARDS);
        Thread train2 = new Train(2, SecondSpeed, UPWARDS);

        train1.start();
        train2.start();
    }
    
    private void initialiseSensors() {
        
        int[][] newSensors = {
            {1, 11}, //0                  
            {2, 9}, //1
            {3, 13}, //2
            {5, 11}, //3
            {6, 7}, //4
            {6, 9}, //5
            {6, 10}, //6
            {10, 7}, //7
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
                                    tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                                } else {
                                    tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                                }
                            }
                            
                            // release upper rail of the lower branch
                            if (isSensorDetection(se, 1)) {
                                lowerBranchControl.release();
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
                                rightSideControl.release();
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }
                            
                            // sensor: 13, 10
                            if (isSensorDetection(se, 10)) {
                                rightSideControl.release();
                            }
                            
                            // left side release
                            if (isSensorDetection(se, 2)) {
                                leftSideControl.release();
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }
                            
                            // left side release
                            if (isSensorDetection(se, 3)) {
                                leftSideControl.release();
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
                            
                            // sensor: 1, 10; switch: 4, 9
                            if (isSensorDetection(se, 0)) {
                                tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
                            }

                            // sensor: 13, 10; switch: 15, 9
                            if (isSensorDetection(se, 10)) {
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                            }

                            // sensor: 5, 11; switch: 3, 11
                            if (isSensorDetection(se, 3)) {
                                tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
                            }

                            // sensor: 19, 9; switch: 17, 7
                            if (isSensorDetection(se, 17)) {
                                tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                            }

                            // sensor: 3, 13; switch: 3, 11
                            if (isSensorDetection(se, 2)) {
                                tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                            }

                            // sensor: 12, 9; switch: 15, 9
                            if (isSensorDetection(se, 9)) {
                                tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
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
