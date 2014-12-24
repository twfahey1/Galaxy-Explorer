package com.fahey.galaxyexp;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.softbody.btSoftBody;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by Tyler on 12/22/2014.
 */
public class ShapeCreator {
    ArrayMap<String, MyGameObjects.GameObject.Constructor> constructors;
    Model model;

    public ShapeCreator() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.node().id = "ground";
        mb.part("ground", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(15f, 1f, 15f);
        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
                .sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
                .box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
                .cone(1f, 2f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.CYAN)))
                .capsule(0.5f, 2f, 10);
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 2f, 1f, 10);
        model = mb.end();

        constructors = new ArrayMap<String, MyGameObjects.GameObject.Constructor>(String.class, MyGameObjects.GameObject.Constructor.class);
        constructors.put("ground", new MyGameObjects.GameObject.Constructor(model, "ground", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
        constructors.put("sphere", new MyGameObjects.GameObject.Constructor(model, "sphere", new btSphereShape(0.5f), 1f));
        constructors.put("box", new MyGameObjects.GameObject.Constructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f)), 2f));
        constructors.put("cone", new MyGameObjects.GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f), 3f));
        constructors.put("capsule", new MyGameObjects.GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f), 4f));
        constructors.put("cylinder", new MyGameObjects.GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f)), 5f));


    }


    /*
    public void fire() {
        //Ray ray = cam.getPickRay(screenW/2, screenH/2);
        Ray ray = cam.getPickRay(shipObject.center.x, shipObject.center.z);
        MyGameObjects.GameObject obj = constructors.values[activeWeapon].construct();
        //GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
        //obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        //obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
        obj.transform.setFromEulerAngles(camLookatMap.getValueAt(currentCamTarget).center.x + 2f, camLookatMap.getValueAt(currentCamTarget).center.y + 2f, camLookatMap.getValueAt(currentCamTarget).center.z + 1f);
        obj.transform.setToTranslation(camLookatMap.getValueAt(currentCamTarget).body.getCenterOfMassPosition().add(0f, 0f, 2f));
        obj.body.proceedToTransform(obj.transform);
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        obj.calculateBoundingBox(bounds);
        obj.shape = new MyShapes.Box(bounds);
        instances.add(obj);
        dynamicsWorld.addRigidBody(obj.body);
        cannonSound.play();
        obj.body.setContactCallbackFlag(OBJECT_FLAG);
        obj.body.setContactCallbackFilter(GROUND_FLAG);
        //obj.body.applyCentralImpulse(fireVector);
        //obj.body.applyCentralImpulse(ray.direction.scl(30f));
        obj.body.applyCentralImpulse(shipObject.center.add(10f, 0f, 1f));
    }*/
}
