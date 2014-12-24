package com.fahey.galaxyexp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyCam {
    public Vector3 camPosition;
    public Vector3 camLookAt;
    public Vector3 camRotate;
    public Vector3 camZoom;

    public float camPanX, camPanY, camPanZ, camDegrees;
    public float camLookAtX, camLookAtY, camLookAtZ;

    public float camRot, camRotX, camRotY, camRotZ;


    protected PerspectiveCamera cam;
    protected CameraInputController camController;

    public ArrayMap<String, MyGameObjects.GameObject> camLookatMap;

    float camZoomX, camZoomY, camZoomZ;
    public Vector3 camrotateVector;
    public Vector3 target;
    public Vector3 camPositionVec;
    InputMultiplexer inputMultiplexer;

    public MyCam() {
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 7f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        camPositionVec = new Vector3(0,0,0);

        camZoomX = 5;
        camZoomY = 5;
        camZoomZ = 3;

        camLookAtX = -6;
        camLookAtY = 5;
        camLookAtZ = -18;

        camPanX = 0;
        camPanY = 0;
        camPanZ = 0;
        camDegrees = 0;

        camRot = 45f;
        camRotX = 0;
        camRotY = 0;
        camRotZ = 0;

        camController = new CameraInputController(cam);
        inputMultiplexer = new InputMultiplexer(camController);
        Gdx.input.setInputProcessor(inputMultiplexer);
        inputMultiplexer.addProcessor(camController);
        //inputMultiplexer.addProcessor(stage);

        camPosition = new Vector3();
        camRotate = new Vector3(3f, 0f, 0f);
        camZoom = new Vector3(5f, 5f, 0f);
        camLookatMap = new ArrayMap<String, MyGameObjects.GameObject>();

        camrotateVector = new Vector3(1f, 0f, 0f);

    }

    public void updateCam(){
        camController.update();


    }



}
