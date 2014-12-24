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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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

    public AssetManager assets;
    public boolean loading;
    static Sound clickSound;
    static Sound angelicSound;
    static Sound cannonSound;
    static Sound clinkSound;
    SpriteBatch spriteBatch;
    Array<Sound> soundArray = new Array<Sound>();

    MyGameObjects.GameObject shipObject;
    MyGameObjects.GameObject invader1object;
    protected ModelBatch modelBatch;
    ArrayMap<String, MyGameObjects.GameObject.Constructor> constructors;
    public Model invaderModelScene;

    protected MyShapes.Shape blockShape;
    protected MyShapes.Shape invaderShape;
    protected MyShapes.Shape shipShape;
    protected MyShapes.Shape spaceShape;
    protected ModelInstance ship;
    protected ModelInstance space;
    private final static BoundingBox bounds = new BoundingBox();

    protected Array<MyGameObjects.GameObject> environmentInstances = new Array<MyGameObjects.GameObject>();
    protected Array<MyGameObjects.GameObject> blocks = new Array<MyGameObjects.GameObject>();
    protected Array<MyGameObjects.GameObject> invaders = new Array<MyGameObjects.GameObject>();
    public ShapeCreator shapeCreator;

    public MyAssetManager() {
        spriteBatch = new SpriteBatch();
        //spriteArray = new Array<Sprite>();

        assets = new AssetManager();
        assets.load("invaderscene.g3db", Model.class);

        assets.load("click.mp3", Sound.class);
        assets.load("transition_heavenly.mp3", Sound.class);
        assets.load("cannon_fire.mp3", Sound.class);
        assets.load("small_object_strike_metal.ogg", Sound.class);

        shapeCreator = new ShapeCreator();
        constructors = new ArrayMap<String, MyGameObjects.GameObject.Constructor>(String.class, MyGameObjects.GameObject.Constructor.class);


    }

    public Model loadModelAsset(String name){
        Model model = assets.get(name, Model.class);
        return model;
    }
    public void doneLoading(){
        invaderModelScene = assets.get("invaderscene.g3db", Model.class);
        for (int i = 0; i < invaderModelScene.nodes.size; i++) {
            String id = invaderModelScene.nodes.get(i).id;
            MyGameObjects.GameObject instance = new MyGameObjects.GameObject(invaderModelScene, id, true);
            if (id.equals("space")) {
                //space = new ModelInstance(instance.model);
                instance.calculateBoundingBox(bounds);
                spaceShape = new MyShapes.Sphere(bounds);
                instance.shape = spaceShape;
                environmentInstances.add(instance);
                //continue;
            } else if (id.equals("ship")) {
                instance.calculateBoundingBox(bounds);
                shipShape = new MyShapes.Sphere(bounds);
                instance.shape = shipShape;
                ship = instance;
            } else if (id.startsWith("block")) {
                if (blockShape == null) {
                    instance.calculateBoundingBox(bounds);
                    blockShape = new MyShapes.Box(bounds);
                }
                instance.shape = blockShape;
                blocks.add(instance);
            } else if (id.startsWith("invader")) {
                if (invaderShape == null) {
                    instance.calculateBoundingBox(bounds);
                    invaderShape = new MyShapes.Disc(bounds);
                }
                instance.shape = invaderShape;
                invaders.add(instance);

            }
        }
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
        shipObject.shape = new MyShapes.Box(bounds);
        GalaxyExplorerMain.world.dynamicsWorld.addRigidBody(shipObject.body);

        invader1object = constructors.get("invader1").construct();
        invader1object.calculateBoundingBox(bounds);
        invader1object.shape = new MyShapes.Disc(bounds);
        invader1object.body.setCollisionFlags(invader1object.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);

        //instances.add(shipObject);
        //instances.add(invader1object);

        //camLookatMap.put("Ship", shipObject);
        //camLookatMap.put("Invader", invader1object);

        shipObject.transform.setToTranslation(0f, 0f, 4f);
        shipObject.body.proceedToTransform(shipObject.transform);
        shipObject.center.set(shipObject.body.getCenterOfMassPosition());
        //dynamicsWorld.addRigidBody(shipObject.body);

        invader1object.transform.setToTranslation(0f, 0f, 8f);
        invader1object.body.proceedToTransform(invader1object.transform);
        invader1object.center.set(invader1object.body.getCenterOfMassPosition());
        GalaxyExplorerMain.world.dynamicsWorld.addRigidBody(invader1object.body);
        loading = false;

        clickSound = assets.get("click.mp3", Sound.class);
        angelicSound = assets.get("transition_heavenly.mp3", Sound.class);
        cannonSound = assets.get("cannon_fire.mp3", Sound.class);
        clinkSound = assets.get("small_object_strike_metal.ogg", Sound.class);
        //constructors.put("shipObject", new GameObject.Constructor(model, "ship", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
        MyGameObjects.GameObject object = shapeCreator.constructors.get("ground").construct();
        object.calculateBoundingBox(bounds);
        object.shape = new MyShapes.Box(bounds);
        object.body.setCollisionFlags(object.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        //instances.add(object);

        //dynamicsWorld.addRigidBody(object.body);


        //instances.add(instance);



    }
}
