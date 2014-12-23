package com.fahey.galaxyexp;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyModelHandler {
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


    public MyModelHandler() {
        invaderModelScene = MyAssetManager.assets.get("invaderscene.g3db", Model.class);
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
    }
}
