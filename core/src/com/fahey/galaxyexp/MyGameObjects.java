package com.fahey.galaxyexp;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyGameObjects {
    protected Array<MyGameObjects.GameObject> instances = new Array<MyGameObjects.GameObject>();

    public static class GameObject extends ModelInstance implements Disposable {
        public final btRigidBody body;
        public final MyMotionState motionState;
        public ModelInstance modelInstance;
        public final Vector3 center = new Vector3();
        public final Vector3 dimensions = new Vector3();
        public final float radius;
        public MyShapes.Shape shape;
        public float mass;

        private final static BoundingBox bounds = new BoundingBox();

        public GameObject(Model model, String rootNode, boolean mergeTransform) {
            super(model, rootNode, mergeTransform);
            calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
            motionState = new MyMotionState();
            motionState.transform = transform;
            body = null;
        }

        public GameObject(Model model, String rootNode, boolean mergeTransform, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
            super(model, rootNode, mergeTransform);
            calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
            motionState = new MyMotionState();
            motionState.transform = transform;
            body = new btRigidBody(constructionInfo);
            body.setMotionState(motionState);

        }

        public boolean isVisible(Camera cam) {
            return shape == null ? false : shape.isVisible(transform, cam);
        }

        public float intersects(Ray ray) {
            return shape == null ? -1f : shape.intersects(transform, ray);
        }

        @Override
        public void dispose() {
            body.dispose();
            motionState.dispose();
        }

        static class Constructor implements Disposable {
            public final Model model;
            public final String node;
            public final btCollisionShape shape;
            public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
            private static Vector3 localInertia = new Vector3();

            public Constructor(Model model, String node, btCollisionShape shape, float mass) {
                this.model = model;
                this.node = node;
                this.shape = shape;
                if (mass > 0f)
                    shape.calculateLocalInertia(mass, localInertia);
                else
                    localInertia.set(0, 0, 0);
                this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
            }

            public GameObject construct() {
                return new GameObject(model, node, true, constructionInfo);
            }

            @Override
            public void dispose() {
                shape.dispose();
                constructionInfo.dispose();
            }
        }

        static class MyMotionState extends btMotionState {
            Matrix4 transform;

            @Override
            public void getWorldTransform(Matrix4 worldTrans) {
                worldTrans.set(transform);
            }

            @Override
            public void setWorldTransform(Matrix4 worldTrans) {
                transform.set(worldTrans);
            }
        }

        class MyContactListener extends ContactListener {
            @Override
            public boolean onContactAdded(int userValue0, int partId0, int index0, boolean match0, int userValue1, int partId1, int index1, boolean match1) {
                if (match0) {
                    //((ColorAttribute)instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
                    //clinkSound.play();
                    //clinkSound.play();
                }
                if (match1) {
                    //((ColorAttribute)instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);

                    //instances.get(userValue1).body.applyCentralImpulse(new Vector3(0f, 10f, 0f));

                }
                return true;
            }
        }
    }
}
