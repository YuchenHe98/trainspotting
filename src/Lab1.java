/*
import TSim.*;

public class Lab1 {

    public Lab1(int speed1, int speed2) {
        
        TSimInterface tsi = TSimInterface.getInstance();

        try {
            tsi.setSpeed(1,speed1);
            tsi.setSpeed(2,speed2);
            try {
                System.out.println(tsi.getSensor(1).getStatus());
            }
            catch (InterruptedException e) {
                e.printStackTrace();    // or only e.getMessage() for the error
                System.exit(1);
            }
        }
        catch (CommandException e) {
            e.printStackTrace();    // or only e.getMessage() for the error
            System.exit(1);
        }
        
    }
} */


//import TSim.AddingArrayList;
import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;
import java.util.concurrent.Semaphore;


public class Lab1 {

  private TSimInterface tsi;
  //private AddingArrayList<Semaphore> semaphores;

  public Lab1(Integer speed1, Integer speed2) {
    tsi = TSimInterface.getInstance();
    //semaphores = new AddingArrayList<>();

    Thread train1 = new Train(1, speed1, true);
    Thread train2 = new Train(2, speed2, false);

    train1.start();
    train2.start();
  }

  public class Train extends Thread {

    int id;
    int speed;
    boolean downward;

    public Train(int id, int speed, boolean downward) {
      this.id = id;
      this.speed = speed;
      this.downward = downward;
    }

    @Override
    public void run() {
      try {

        tsi.setSpeed(id, speed);///Speed args are 15 and 15.
        //tsi.setSpeed(id, 0); // Prevent the speed to become 0 when reaching a station

        while (true) {
          SensorEvent se = tsi.getSensor(id);
          //Coming from north

          if ((se.getStatus() == SensorEvent.ACTIVE)) {
            if ((se.getXpos() == 14) && (se.getYpos() == 7) && downward) {
              tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
            }

            if ((se.getXpos() == 19) && (se.getYpos() == 9) && downward) {
              tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
            }

            if ((se.getXpos() == 6) && (se.getYpos() == 9) && downward) {
              tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
            }

            if ((se.getXpos() == 15) && (se.getYpos() == 8) && downward) {
              tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
            }

            if ((se.getXpos() == 1) && (se.getYpos() == 10) && downward) {
              tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
            }

            //Coming from south
            if ((se.getXpos() == 1) && (se.getYpos() == 10) && !downward) {
              tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
            }

            if ((se.getXpos() == 13) && (se.getYpos() == 10) && !downward) {
              tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
            }

            if ((se.getXpos() == 5) && (se.getYpos() == 11) && !downward) {
              tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
            }

            if ((se.getXpos() == 19) && (se.getYpos() == 9) && !downward) {
              tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
            }

            if ((se.getXpos() == 4) && (se.getYpos() == 13) && !downward) {
              tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
            }

            if ((se.getXpos() == 12) && (se.getYpos() == 9) && !downward) {
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
            if ((se.getXpos() == 15) && ((se.getYpos() == 11) || se.getYpos() == 13)) {
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
