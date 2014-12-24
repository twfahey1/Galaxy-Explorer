package com.fahey.galaxyexp;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyShip {
    public Vector3 upVector;
    public Vector3 downVector, leftVector, rightVector;
    public Vector3 dropDownVector, raiseUpVector;
    public Vector3 rotateVector;

    public MyShip () {
        upVector = new Vector3(0f, 0f, 1f);
        downVector = new Vector3(0f, 0f, -1f);
        leftVector = new Vector3(1f, 0f, 0f);
        rightVector = new Vector3(-1f, 0f, 0f);
        dropDownVector = new Vector3(0f, -1f, 0f);
        raiseUpVector = new Vector3(0f, 1f, 0f);
        rotateVector = new Vector3(0f, 1f, 0f);
    }
}
