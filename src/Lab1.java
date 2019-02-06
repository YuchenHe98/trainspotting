import java.util.concurrent.Semaphore;

import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

public class Lab1 {
    
    final static int DOWNWARDS = 1;
    final static int UPWARDS = 1;

    private TSimInterface tsi;

    //private AddingArrayList<Semaphore> semaphores;
    Semaphore upperLoopControlLeft, upperLoopControlRight, lowerLoopControlLeft, lowerLoopControlRight;
    
    int[][] sensors;
    int[][] switches;

    public Lab1(int firstSpeed, int SecondSpeed) {
        
        tsi = TSimInterface.getInstance();
                
        upperLoopControlLeft = new Semaphore(1);
        upperLoopControlRight = new Semaphore(1);
        lowerLoopControlLeft = new Semaphore(1);
        lowerLoopControlRight = new Semaphore(1);
        
        initialiseSensors();

        Thread train1 = new Train(1, firstSpeed, true);
        Thread train2 = new Train(2, SecondSpeed, false);

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
        boolean downward;

        public Train(int id, int speed, boolean downward) {
            this.id = id;
            this.speed = speed;
            maxSpeed = 16;
            this.downward = downward;
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
                //tsi.setSpeed(id, 0); // Prevent the speed to become 0 when reaching a station
                if (speed < maxSpeed) {
                    speed++;
                }
                
                while (true) {
                    SensorEvent se = tsi.getSensor(id);
                    

                    if ((se.getStatus() == SensorEvent.ACTIVE)) {
                        
                        
                        /* Downwards detections. */
                        
                        // sensor: 14, 7; switch: 17, 7
                        if (isSensorDetection(se, 11) && downward) {
                            tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                        }
                        
                        // sensor: 19, 9; switch: 15, 9
                        if (isSensorDetection(se, 17) && downward) {
                            tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                        }
                        
                        // sensor: 6, 9; switch: 4, 9
                        if (isSensorDetection(se, 5) && downward) {
                            tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
                        }
                        
                        // sensor: 15, 8; switch: 17, 7
                        if (isSensorDetection(se, 14) && downward) {
                            tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                        }
                        
                        // sensor: 1, 10; switch: 3, 11 
                        if (isSensorDetection(se, 0) && downward) {
                            tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                        }

                        /* Upwards detections. */
                        
                        // sensor: 1, 10; switch: 4, 9
                        if (isSensorDetection(se, 0) && !downward) {
                            tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
                        }

                        // sensor: 13, 10; switch: 15, 9
                        if (isSensorDetection(se, 10) && !downward) {
                            tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                        }

                        // sensor: 5, 11; switch: 3, 11
                        if (isSensorDetection(se, 3) && !downward) {
                            tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
                        }

                        // sensor: 19, 9; switch: 17, 7
                        if (isSensorDetection(se, 17) && !downward) {
                            tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                        }
                        
                        // sensor: 3, 13; switch: 3, 11
                        if (isSensorDetection(se, 2) && !downward) {
                            tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                        }

                        // sensor: 12, 9; switch: 15, 9
                        if (isSensorDetection(se, 9) && !downward) {
                            tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                        }

                        //North station
                        if ((se.getXpos() == 15) && ((se.getYpos() == 5) || se.getYpos() == 3)) {
                            if (se.getStatus() == SensorEvent.ACTIVE) {
                                tsi.setSpeed(se.getTrainId(), 0);
                                downward = true;
                                sleep(1000 + Math.abs(speed) * 15);
                                speed = -speed;
                                tsi.setSpeed(se.getTrainId(), speed);
                            }
                        }

                        //South station
                        if ((se.getXpos() == 15) && ((se.getYpos() == 11) || se.getYpos() == 13)){
                            if (se.getStatus() == SensorEvent.ACTIVE) {
                                tsi.setSpeed(se.getTrainId(), 0);
                                downward = false;
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
