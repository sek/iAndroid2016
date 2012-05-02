package org.wintrisstech.erik.iaroc;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;

/**
 * A Trabant is an implementation of the IRobotCreateInterface.
 *
 * @author Erik
 */
public class Trabant extends IRobotCreateAdapter
{
    private static final String TAG = "Ferrari";
    private final Dashboard dashboard;
    /*
     * State variables:
     */
    private int speed = 100; // The normal speed of the Trabant when going straight
    private boolean running = true;
    private final int[][] stateTable =
    {
        {
            0, 1, 2, 3
        },
        {
            1, 1, 2, 3
        },
        {
            2, 1, 2, 3
        },
        {
            3, 1, 2, 3
        }
    };//State table to avoid obstacles
    private int statePointer = 0;
    private int presentState = 0;
    private int howFarBacked = 200;
    private int howFarToGoBackWhenBumped = 200;
    private boolean nextTurnDirection = true;
    private Random r;

    /**
     * Constructs a Trabant, an amazing machine!
     *
     * @param ioio the IOIO instance that the Trabant can use to communicate
     * with other peripherals such as sensors
     * @param create an implementation of an iRobot
     * @param dashboard the Dashboard instance that is connected to the Trabant
     * @throws ConnectionLostException
     */
    public Trabant(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard) throws ConnectionLostException
    {
        super(create);
        this.dashboard = dashboard;
        song(0, new int[]
                {
                    58, 10
                });
    }

    /**
     * Main method that gets the Trabant going.
     */
    public void go() throws InterruptedException
    {
        try
        {
            dashboard.log("Running ...");
            driveDirect(speed, speed);
            howFarBacked = howFarToGoBackWhenBumped;
            while (isRunning())
            {
                try
                {
                    stateController();
                } catch (Exception ex)
                {
                    Logger.getLogger(Trabant.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            dashboard.log("Run completed.");
        } catch (ConnectionLostException ex)
        {
            Logger.getLogger(Trabant.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * **************************************************************************
     * Vic's Awesome API
     * **************************************************************************
     */
    public void stateController() throws Exception
    {
        setStatePointer();
        switch (stateTable[presentState][statePointer])
        {
            case 0:
                presentState = 0;
                break;
            case 1:
                presentState = 1;
                backingUp("right");
                break;
            case 2:
                presentState = 2;
                backingUp("left");
                break;
            case 3:
                presentState = 3;
                backingUp("straight");
                break;
        }
    }

    public void setStatePointer() throws ConnectionLostException
    {
        readSensors(SENSORS_GROUP_ID6);

        if (isBumpRight() && !isBumpLeft())//Right
        {
            statePointer = 1;
        }
        if (isBumpLeft() && !isBumpRight())//left
        {
            statePointer = 2;
        }
        if (isBumpRight() && isBumpLeft())//straight
        {
            statePointer = 3;
        }
        if (!isBumpLeft() && !isBumpRight())//none
        {
            statePointer = 0;
        }
    }

    private void backingUp(String direction) throws Exception
    {
        if (direction.equals("right"))
        {
            driveDirect(-(speed), -(speed/4));
        }
        if (direction.equals("left"))
        {
            driveDirect(-(speed/4), -(speed));
        }
        if (direction.equals("straight"))
        {
            if (nextTurnDirection)
            {
                driveDirect(-(speed/4), -(speed));
            } else
            {
                driveDirect(-(speed), -(speed/4));
            }
        }
        howFarBacked += getDistance();
        dashboard.log(howFarBacked + "/" + getDistance());
        if (howFarBacked < 0)
        {
            nextTurnDirection = r.nextBoolean();
            howFarBacked = howFarToGoBackWhenBumped;
            driveDirect(speed, speed);
            statePointer = 0;
            presentState = 0;
        }
    }

    /**
     * Closes down all the connections of the Trabant, including the connection
     * to the iRobot Create and the connections to all the sensors.
     */
    public void shutDown()
    {
        closeConnection(); // close the connection to the Create
    }

    /**
     * Checks if the Trabant is running
     *
     * @return true if the Trabant is running
     */
    public synchronized boolean isRunning()
    {
        return running;
    }

    private synchronized void setRunning(boolean b)
    {
        running = false;
    }
}
