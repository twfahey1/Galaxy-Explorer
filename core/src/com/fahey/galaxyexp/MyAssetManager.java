package com.fahey.galaxyexp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyAssetManager {

    static AssetManager assets;
    protected boolean loading;
    static Sound clickSound;
    static Sound angelicSound;
    static Sound cannonSound;
    static Sound clinkSound;
    MyModelHandler modelHandler;
    SpriteBatch spriteBatch;
    Array<Sound> soundArray = new Array<Sound>();

    MyGameObjects.GameObject shipObject;
    MyGameObjects.GameObject invader1object;
    protected ModelBatch modelBatch;
    ArrayMap<String, MyGameObjects.GameObject.Constructor> constructors;
    private final static BoundingBox bounds = new BoundingBox();

    public MyAssetManager() {
        spriteBatch = new SpriteBatch();
        spriteArray = new Array<Sprite>();

        assets = new AssetManager();
        assets.load("invaderscene.g3db", Model.class);

        assets.load("click.mp3", Sound.class);
        assets.load("transition_heavenly.mp3", Sound.class);
        assets.load("cannon_fire.mp3", Sound.class);
        assets.load("small_object_strike_metal.ogg", Sound.class);




        if (assets.update()) {
            doneLoading();
        }

    }

    public void doneLoading(){
        modelHandler = new MyModelHandler();

        clickSound = assets.get("click.mp3", Sound.class);
        angelicSound = assets.get("transition_heavenly.mp3", Sound.class);
        cannonSound = assets.get("cannon_fire.mp3", Sound.class);
        clinkSound = assets.get("small_object_strike_metal.ogg", Sound.class);
        //constructors.put("shipObject", new GameObject.Constructor(model, "ship", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
        MyGameObjects.GameObject object = constructors.get("ground").construct();

        object.calculateBoundingBox(bounds);
        object.shape = new MyShapes.Box(bounds);
        object.body.setCollisionFlags(object.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        instances.add(object);

        dynamicsWorld.addRigidBody(object.body);


        //instances.add(instance);


    constructors.put("shipObject", new MyGameObjects.GameObject.Constructor(invaderModelScene, "ship", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 1f));
    constructors.put("invader1", new MyGameObjects.GameObject.Constructor(invaderModelScene, "invader1", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 1f));
    constructors.put("spaceGlobe", new MyGameObjects.GameObject.Constructor(invaderModelScene, "spaceGlobe", new btBoxShape(new Vector3(0f, 0f, 0f)), 0f));

    shipObject = constructors.get("shipObject").construct();
    shipObject.calculateBoundingBox(bounds);
    shipObject.shape = new MyShapes.Box(bounds);
    shipObject.body.setCollisionFlags(shipObject.body.getCollisionFlags()
            | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
    //| btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    shipObject.calculateBoundingBox(bounds);
    shipObject.shape = new Box(bounds);
    dynamicsWorld.addRigidBody(shipObject.body);

    invader1object = constructors.get("invader1").construct();
    invader1object.calculateBoundingBox(bounds);
    invader1object.shape = new MyShapes.Disc(bounds);
    invader1object.body.setCollisionFlags(invader1object.body.getCollisionFlags()
            | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);

    instances.add(shipObject);
    instances.add(invader1object);

    camLookatMap.put("Ship", shipObject);
    camLookatMap.put("Invader", invader1object);

    shipObject.transform.setToTranslation(0f, 0f, 4f);
    shipObject.body.proceedToTransform(shipObject.transform);
    shipObject.center.set(shipObject.body.getCenterOfMassPosition());
    //dynamicsWorld.addRigidBody(shipObject.body);

    invader1object.transform.setToTranslation(0f, 0f, 8f);
    invader1object.body.proceedToTransform(invader1object.transform);
    invader1object.center.set(invader1object.body.getCenterOfMassPosition());
    dynamicsWorld.addRigidBody(invader1object.body);
    loading = false;
    }
}
