package com.fahey.galaxyexp;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;

public class GalaxyExplorerMain extends InputAdapter implements ApplicationListener{
	final static short GROUND_FLAG = 1;
	final static short OBJECT_FLAG = 2;
	final static short ALL_FLAG = 3;
	public float screenH;
	public float screenW;
	public Vector3 upVector;
	public Vector3 downVector, leftVector, rightVector;
	public Vector3 dropDownVector, raiseUpVector;
	public Vector3 rotateVector;
	public int activeWeapon;
	InputMultiplexer inputMultiplexer;

	public boolean fullScreen;
	public Vector2 touchPos;
	public ArrayMap<String, GameObject> randGroundArray;

	public Vector3 camPosition;
	public Vector3 camLookAt;
	public Vector3 camRotate;
	public Vector3 camZoom;

	public float camPanX, camPanY, camPanZ, camDegrees;
	public float camLookAtX, camLookAtY, camLookAtZ;

	public float camRot, camRotX, camRotY, camRotZ;

	public ArrayMap<String, GameObject> camLookatMap;
	public Model invaderModelScene;

	public interface Shape{
		public abstract boolean isVisible(Matrix4 transform, Camera cam);
		public abstract float intersects(Matrix4 transform, Ray ray);
	}

	public static abstract class BaseShape implements Shape{
		protected final static Vector3 position = new Vector3();
		protected final Vector3 center = new Vector3();
		public final Vector3 dimensions = new Vector3();

		public BaseShape(BoundingBox bounds){
			bounds.getCenter(center);
			bounds.getDimensions(dimensions);
		}
	}

	public static class Sphere extends BaseShape {
		public float radius;

		public Sphere(BoundingBox bounds){
			super(bounds);
			radius = dimensions.len() / 2f;
		}

		@Override
		public boolean isVisible(Matrix4 transform, Camera cam) {
			return cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center),radius);
		}

		@Override
		public float intersects(Matrix4 transform, Ray ray) {
			transform.getTranslation(position).add(center);
			final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
			if (len < 0f)
				return -1f;
			float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
			return (dist2 <= radius * radius) ? dist2 : -1f;
		}
	}

	public static class Disc extends BaseShape {
		public float radius;
		public Disc(BoundingBox bounds) {
			super(bounds);
			radius = 0.5f * (dimensions.x > dimensions.z ? dimensions.x : dimensions.z);
		}

		@Override
		public boolean isVisible(Matrix4 transform, Camera cam){
			return cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
		}

		@Override
		public float intersects (Matrix4 transform, Ray ray) {
			transform.getTranslation(position).add(center);
			final float len = (position.y - ray.origin.y) / ray.direction.y;
			final float dist2 = position.dst2(ray.origin.x + len * ray.direction.x, ray.origin.y + len * ray.direction.y, ray.origin.z + len * ray.direction.z);
			return (dist2 < radius * radius) ? dist2 : -1f;
		}
	}

	public static class Box extends BaseShape {
		public Box(BoundingBox bounds) {
			super(bounds);
		}

		@Override
		public boolean isVisible(Matrix4 transform, Camera cam) {
			return cam.frustum.boundsInFrustum(transform.getTranslation(position).add(center), dimensions);
		}

		@Override
		public float intersects(Matrix4 transform, Ray ray){
			transform.getTranslation(position).add(center);
			if (Intersector.intersectRayBoundsFast(ray, position, dimensions)){
				final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
				return position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
			}
			return -1f;
		}
	}

	static class MyMotionState extends btMotionState {
		Matrix4 transform;
		@Override
		public void getWorldTransform(Matrix4 worldTrans) { worldTrans.set(transform);}
		@Override
		public void setWorldTransform(Matrix4 worldTrans) { transform.set(worldTrans);}
	}

	public static class GameObject extends ModelInstance implements Disposable {
		public final btRigidBody body;
		public final MyMotionState motionState;
		public ModelInstance modelInstance;
		public final Vector3 center = new Vector3();
		public final Vector3 dimensions = new Vector3();
		public final float radius;
		public Shape shape;
		public float mass;


		private final static BoundingBox bounds = new BoundingBox();

		public GameObject(Model model, String rootNode, boolean mergeTransform){
			super(model, rootNode, mergeTransform);
			calculateBoundingBox(bounds);
			bounds.getCenter(center);
			bounds.getDimensions(dimensions);
			radius = dimensions.len() / 2f;
			motionState = new MyMotionState();
			motionState.transform = transform;
			body = null;
		}

		public GameObject(Model model, String rootNode,boolean mergeTransform, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
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

		public boolean isVisible(Camera cam){
			return shape == null ? false : shape.isVisible(transform, cam);
		}
		public float intersects(Ray ray){
			return shape == null ? -1f : shape.intersects(transform, ray);
		}

		@Override
		public void dispose(){
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
	/*public static class GameObject extends ModelInstance implements Disposable {
		public final btRigidBody body;
		public final MyMotionState motionState;
		public ModelInstance modelInstance;
		public final Vector3 center = new Vector3();
		public final Vector3 dimensions = new Vector3();
		public final float radius;
		public Shape shape;
		public float mass;

		private final static BoundingBox bounds = new BoundingBox();

		public GameObject(Model model, String rootNode, boolean mergeTransform){
			super(model, rootNode, mergeTransform);
			calculateBoundingBox(bounds);
			bounds.getCenter(center);
			bounds.getDimensions(dimensions);
			radius = dimensions.len() / 2f;
			body = new btRigidBody(new btRigidBody.btRigidBodyConstructionInfo(mass, null, );

			motionState = null;
		}

		public GameObject(Model model, String rootNode, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {

			super(model, rootNode);
			motionState = new MyMotionState();
			motionState.transform = transform;
			body = new btRigidBody(constructionInfo);
			body.setMotionState(motionState);
			calculateBoundingBox(bounds);
			bounds.getCenter(center);
			bounds.getDimensions(dimensions);
			radius = dimensions.len() / 2f;
		}

		public boolean isVisible(Camera cam){
			return shape == null ? false : shape.isVisible(transform, cam);
		}
		public float intersects(Ray ray){
			return shape == null ? -1f : shape.intersects(transform, ray);
		}

		@Override
		public void dispose(){
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
				return new GameObject(model, node, constructionInfo);
			}

			@Override
			public void dispose() {
				shape.dispose();
				constructionInfo.dispose();
			}
		}*/

	}

	class MyContactListener extends ContactListener {
		@Override
		public boolean onContactAdded (int userValue0, int partId0, int index0, boolean match0, int userValue1, int partId1, int index1, boolean match1) {
			if(match0) {
				//((ColorAttribute)instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);
				//clinkSound.play();
				clinkSound.play();
			}
			if(match1) {
				//((ColorAttribute)instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.WHITE);

				instances.get(userValue1).body.applyCentralImpulse(new Vector3(0f, 10f, 0f));

			}
			return true;
		}
	}

	protected PerspectiveCamera cam;
	protected CameraInputController camController;
	protected ModelBatch modelBatch;
	protected AssetManager assets;
	protected Array<GameObject> instances = new Array<GameObject>();
	protected Array<GameObject> blocks = new Array<GameObject>();
	protected Array<GameObject> invaders = new Array<GameObject>();
	protected Array<GameObject> environmentInstances = new Array<GameObject>();
	protected Environment environment;
	protected boolean loading;
	
	protected ModelInstance ship;
	protected ModelInstance space;

	protected Shape blockShape;
	protected Shape invaderShape;
	protected Shape shipShape;
	protected Shape spaceShape;

	ArrayMap<String, GameObject.Constructor> constructors;

	protected Stage stage;
	protected Label label;
	protected Label label2;
	protected BitmapFont font;
	protected StringBuilder stringBuilder;

	private int visibleCount;
	private Vector3 position = new Vector3();

	private int selected = -1, selecting = -1;
	private Material selectionMaterial;
	private Material originalMaterial;

	protected  boolean moving;

	btDynamicsWorld dynamicsWorld;
	btConstraintSolver constraintSolver;
	btCollisionConfiguration collisionConfig;
	btDispatcher dispatcher;
	btBroadphaseInterface broadphase;
	MyContactListener contactListener;
	Model model;
	static Sound clickSound;
	static Sound angelicSound;
	static Sound cannonSound;
	static Sound clinkSound;
	Array<Sound> soundArray = new Array<Sound>();

	Sprite xUp, xDown, yUp, yDown, zUp, zDown;
	Sprite sUp, sDown, sLeft, sRight, sAscent, sDescent, sFire, sSpawn;
	Texture fireButton;
	Texture spawnButton, xUpButton, xDownButton, yUpButton, yDownButton, zUpButton,zDownButton;
	Texture upButton, downButton, leftButton, rightButton, ascentButton, descentButton;

	SpriteBatch spriteBatch;
	Array<Sprite> spriteArray;

	GameObject shipObject;
	GameObject invader1object;

	Actor fireButtonactor;
	Actor spawnButtonactor;
	Actor shipUpButtonactor;
	Actor shipDownButtonactor;
	Actor shipLeftButtonactor;
	Actor shipRightButtonactor;

	float touchedActor = 0;
	String touchedName = "";
	int currentCamTarget;

	float camZoomX, camZoomY, camZoomZ;
	public Vector3 camrotateVector;
	public Vector3 target;
	public Vector3 camPositionVec;

	@Override
	public void create() {
		Bullet.init();

		currentCamTarget = 0;
		
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

		target = new Vector3();



		fullScreen = false;
		camPosition = new Vector3();
		camRotate = new Vector3(3f, 0f, 0f);
		camZoom = new Vector3(5f, 5f, 0f);
		camLookatMap = new ArrayMap<String, GameObject>();

		touchPos = new Vector2();
		activeWeapon = 1;
		spriteBatch = new SpriteBatch();
		spriteArray = new Array<Sprite>();
		fireButton = new Texture(Gdx.files.internal("fireButton.png"));

		spawnButton = new Texture(Gdx.files.internal("spawnButton.png"));

		xUpButton = new Texture(Gdx.files.internal("xplusButton.png"));
		xDownButton = new Texture(Gdx.files.internal("xminusButton.png"));
		zUpButton = new Texture(Gdx.files.internal("zplusButton.png"));
		zDownButton = new Texture(Gdx.files.internal("zminusButton.png"));
		yUpButton = new Texture(Gdx.files.internal("yplusButton.png"));
		yDownButton = new Texture(Gdx.files.internal("yminusButton.png"));
		
		upButton = new Texture(Gdx.files.internal("uparrow.png"));
		downButton = new Texture(Gdx.files.internal("downarrow.png"));
		leftButton = new Texture(Gdx.files.internal("leftarrow.png"));
		rightButton = new Texture(Gdx.files.internal("rightarrow.png"));

		ascentButton = new Texture(Gdx.files.internal("ascent.png"));
		descentButton = new Texture(Gdx.files.internal("descent.png"));

		moving = false;

		upVector = new Vector3(0f, 0f, 1f);
		downVector = new Vector3(0f, 0f, -1f);
		leftVector = new Vector3(1f, 0f, 0f);
		rightVector = new Vector3(-1f, 0f, 0f);
		dropDownVector = new Vector3(0f, -1f, 0f);
		raiseUpVector = new Vector3(0f, 1f, 0f);
		rotateVector = new Vector3(0f, 1f, 0f);
		camrotateVector = new Vector3(1f, 0f, 0f);

		xUp = new Sprite(xUpButton);
		xDown = new Sprite(xDownButton);
		yUp = new Sprite(yUpButton);
		yDown = new Sprite(yDownButton);
		zUp = new Sprite(zUpButton);
		zDown = new Sprite(zDownButton);

		sUp = new Sprite(upButton);
		sDown = new Sprite(downButton);
		sLeft = new Sprite(leftButton);
		sRight = new Sprite(rightButton);
		sAscent = new Sprite(ascentButton);
		sDescent = new Sprite(descentButton);

		sFire = new Sprite(fireButton);
		sSpawn = new Sprite(spawnButton);

		spriteArray.add(sFire);
		spriteArray.add(sSpawn);
		spriteArray.add(xUp);
		spriteArray.add(xDown);
		spriteArray.add(yDown);
		spriteArray.add(yUp);
		spriteArray.add(zDown);
		spriteArray.add(zUp);
		spriteArray.add(sDown);
		spriteArray.add(sLeft);
		spriteArray.add(sRight);
		spriteArray.add(sAscent);
		spriteArray.add(sDescent);
		spriteArray.add(sUp);
		spriteArray.add(sDown);
		spriteArray.add(sLeft);
		spriteArray.add(sRight);
		spriteArray.add(sDescent);
		spriteArray.add(sAscent);

		for (Sprite i : spriteArray) {
			scaleSprite(i);
		}

		//screenH = Gdx.graphics.getHeight();
		//screenW = Gdx.graphics.getWidth();

		screenH = 600f;
		screenW = 800f;

		shotX = 5;
		shotY= 5;
		shotZ = 1;

		stage = new Stage();

		font = new BitmapFont();
		label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
		label2 = new Label(" ", new Label.LabelStyle(font, Color.WHITE));

		stringBuilder = new StringBuilder();
		fireButtonactor = new MyActor("Fire", sFire, fireButton.getWidth()*3, 0);
		fireButtonactor.setTouchable(Touchable.enabled);
		spawnButtonactor = new MyActor("Spawn", sSpawn, fireButton.getWidth()*4, 0);
		spawnButtonactor.setTouchable(Touchable.enabled);
		shipUpButtonactor = new MyActor("shipUpButton", sUp, upButton.getWidth(), upButton.getHeight());
		shipUpButtonactor.setTouchable(Touchable.enabled);
		shipDownButtonactor = new MyActor("shipDownButton", sDown, downButton.getWidth(), 0);
		shipDownButtonactor.setTouchable(Touchable.enabled);
		shipLeftButtonactor = new MyActor("shipLeftButton", sLeft, 0, leftButton.getHeight()/2);
		shipLeftButtonactor.setTouchable(Touchable.enabled);
		shipRightButtonactor = new MyActor("shipRightButton", sRight, rightButton.getWidth()*2, rightButton.getHeight()/2);
		shipRightButtonactor.setTouchable(Touchable.enabled);


		//for (Actor i : stage.getActors()) scaleActorSmall(i);

		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		//cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 7f, 10f);
		cam.lookAt(0,0,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		camController = new CameraInputController(cam);
		inputMultiplexer = new InputMultiplexer(camController);
		Gdx.input.setInputProcessor(inputMultiplexer);
		inputMultiplexer.addProcessor(camController);
		inputMultiplexer.addProcessor(stage);

		//Gdx.input.setInputProcessor(new InputMultiplexer(this, camController));
		//Gdx.input.setInputProcessor(stage);
		selectionMaterial = new Material();
		selectionMaterial.set(ColorAttribute.createDiffuse(Color.ORANGE));
		originalMaterial = new Material();

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

		constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
		constructors.put("ground", new GameObject.Constructor(model, "ground", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
		constructors.put("sphere", new GameObject.Constructor(model, "sphere", new btSphereShape(0.5f), 1f));
		constructors.put("box", new GameObject.Constructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f)), 2f));
		constructors.put("cone", new GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f), 3f));
		constructors.put("capsule", new GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f), 4f));
		constructors.put("cylinder", new GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f)), 5f));

		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		broadphase = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
		dynamicsWorld.setGravity(new Vector3(0, -9f, 0));
		contactListener = new MyContactListener();

		assets = new AssetManager();
		assets.load("invaderscene.g3db", Model.class);

		assets.load("click.mp3", Sound.class);
		assets.load("transition_heavenly.mp3", Sound.class);
		assets.load("cannon_fire.mp3", Sound.class);
		assets.load("small_object_strike_metal.ogg", Sound.class);
		loading = true;

	}
	private void doneLoading() {
		clickSound = assets.get("click.mp3", Sound.class);
		angelicSound = assets.get("transition_heavenly.mp3", Sound.class);
		cannonSound = assets.get("cannon_fire.mp3", Sound.class);
		clinkSound = assets.get("small_object_strike_metal.ogg", Sound.class);
		//constructors.put("shipObject", new GameObject.Constructor(model, "ship", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 0f));
		randGroundArray = new ArrayMap<String, GameObject>();
		GameObject object = constructors.get("ground").construct();
		for (int i = 0; i < 50; i++){
			GameObject obj = constructors.get("ground").construct();
			randGroundArray.put("obj"+i, obj);
		}
		object.calculateBoundingBox(bounds);
		object.shape = new Box(bounds);
		object.body.setCollisionFlags(object.body.getCollisionFlags()
				| btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		instances.add(object);

		dynamicsWorld.addRigidBody(object.body);

		invaderModelScene = assets.get("invaderscene.g3db", Model.class);
		for (int i = 0; i < invaderModelScene.nodes.size; i++) {
			String id = invaderModelScene.nodes.get(i).id;
			GameObject instance = new GameObject(invaderModelScene, id, true);
			if (id.equals("space")) {
				//space = new ModelInstance(instance.model);
				instance.calculateBoundingBox(bounds);
				spaceShape = new Sphere(bounds);
				instance.shape = spaceShape;
				environmentInstances.add(instance);
				//continue;
			}
			else if (id.equals("ship")) {
				instance.calculateBoundingBox(bounds);
				shipShape = new Sphere(bounds);
				instance.shape = shipShape;
				ship = instance;
			} else if (id.startsWith("block")) {
				if (blockShape == null) {
					instance.calculateBoundingBox(bounds);
					blockShape = new Box(bounds);
				}
				instance.shape = blockShape;
				blocks.add(instance);
			} else if (id.startsWith("invader")) {
				if (invaderShape == null) {
					instance.calculateBoundingBox(bounds);
					invaderShape = new Disc(bounds);
				}
				instance.shape = invaderShape;
				invaders.add(instance);

			}
			//instances.add(instance);

		}
		constructors.put("shipObject", new GameObject.Constructor(invaderModelScene, "ship", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 1f));
		constructors.put("invader1", new GameObject.Constructor(invaderModelScene, "invader1", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)), 1f));
		constructors.put("spaceGlobe", new GameObject.Constructor(invaderModelScene, "spaceGlobe", new btBoxShape(new Vector3(0f, 0f, 0f)), 0f));

		shipObject = constructors.get("shipObject").construct();
		shipObject.calculateBoundingBox(bounds);
		shipObject.shape = new Box(bounds);
		shipObject.body.setCollisionFlags(shipObject.body.getCollisionFlags()
				| btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
		//| btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		shipObject.calculateBoundingBox(bounds);
		shipObject.shape = new Box(bounds);
		dynamicsWorld.addRigidBody(shipObject.body);

		invader1object = constructors.get("invader1").construct();
		invader1object.calculateBoundingBox(bounds);
		invader1object.shape = new Disc(bounds);
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

	public void fire(){
		//Ray ray = cam.getPickRay(screenW/2, screenH/2);
		Ray ray = cam.getPickRay(shipObject.center.x, shipObject.center.z);
		GameObject obj = constructors.values[activeWeapon].construct();
		//GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
		//obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
		//obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
		obj.transform.setFromEulerAngles(camLookatMap.getValueAt(currentCamTarget).center.x+2f, camLookatMap.getValueAt(currentCamTarget).center.y+2f, camLookatMap.getValueAt(currentCamTarget).center.z+1f);
		obj.transform.setToTranslation(camLookatMap.getValueAt(currentCamTarget).body.getCenterOfMassPosition().add(0f, 0f, 2f));
		obj.body.proceedToTransform(obj.transform);
		obj.body.setUserValue(instances.size);
		obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		obj.calculateBoundingBox(bounds);
		obj.shape = new Box(bounds);
		instances.add(obj);
		dynamicsWorld.addRigidBody(obj.body);
		cannonSound.play();
		obj.body.setContactCallbackFlag(OBJECT_FLAG);
		obj.body.setContactCallbackFilter(GROUND_FLAG);
		//obj.body.applyCentralImpulse(fireVector);
		//obj.body.applyCentralImpulse(ray.direction.scl(30f));
		obj.body.applyCentralImpulse(shipObject.center.add(10f, 0f, 1f));
	}
	public void scaleSprite(Sprite sprite){
		float W = Gdx.graphics.getWidth();
		float H = Gdx.graphics.getHeight();
		sprite.setSize(W/12, H/8);

	}

	public void scaleActor(Actor sprite){
		float W = Gdx.graphics.getWidth();
		float H = Gdx.graphics.getHeight();
		sprite.setSize(W/12, H/8);
		sprite.setWidth(sprite.getWidth());
		sprite.setHeight(sprite.getHeight());
		sprite.setBounds(sprite.getX(),sprite.getY(), sprite.getWidth(),sprite.getHeight());

	}

	public void scaleActorSmall(Actor sprite){
		float W = screenW;
		float H = screenH;
		sprite.setWidth(sprite.getWidth());
		sprite.setHeight(sprite.getHeight());
		sprite.setSize(W / 36, H / 24);
	}


	public void spawn () {
		GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 3)].construct();
		obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
		obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
		obj.body.proceedToTransform(obj.transform);
		obj.body.setUserValue(instances.size);
		obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		obj.calculateBoundingBox(bounds);
		obj.shape = new Box(bounds);
		instances.add(obj);
		dynamicsWorld.addRigidBody(obj.body);
		obj.body.setContactCallbackFlag(OBJECT_FLAG);
		obj.body.setContactCallbackFilter(GROUND_FLAG);
	}
	public btBoxShape newBtBox(float x, float y, float z){
		return new btBoxShape(new Vector3(x,y,z));
	}

	private BoundingBox bounds = new BoundingBox();



	float shotX, shotY, shotZ;
	Vector3 fireVector;

	@Override
	public void render() {
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
		fireVector = new Vector3(shotX, shotY, shotZ);
		if (loading && assets.update()) {
			doneLoading();
		}
		dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		visibleCount = 0;
		//if (space != null) modelBatch.render(space);

		if (!loading) {

			camController.forwardButton = Input.Keys.K;
			for (final GameObject instance : environmentInstances){
				if (instance.isVisible(cam)){
					modelBatch.render(instance, environment);

					visibleCount++;
				}
			}
			for (final GameObject instance : instances) {
				if (instance.isVisible(cam)) {
					modelBatch.render(instance, environment);
					visibleCount++;
				}
			}

			if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
					&& Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
				if (!fullScreen) {
					if (Gdx.graphics.supportsDisplayModeChange()) {
						Graphics.DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
						Gdx.graphics.setDisplayMode(displayMode.width, displayMode.height, true);
						fullScreen = true;
					} else {
						Gdx.graphics.setDisplayMode(1080, 720, false);
					}
				}
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)){
				if (activeWeapon < constructors.size-2) {
					activeWeapon += 1;
				} else {
					activeWeapon = 0;
				}
			}
			
			if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) spawn();
			if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) fire();

			if (touchedActor == 1) {
				if (touchedName.equals("Fire")) {
					fire();
				}
				if (touchedName.equals("Spawn")) {
					spawn();
				}
				if (touchedName.equals("shipRightButton"))
					shipObject.transform.translate(rightVector);
				if (touchedName.equals("shipLeftButton"))
					shipObject.transform.translate(leftVector);
				if (touchedName.equals("shipUpButton"))
					shipObject.transform.translate(upVector);
				if (touchedName.equals("shipDownButton"))
					shipObject.transform.translate(downVector);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.UP)){
				moveGameObject(camLookatMap.getValueAt(currentCamTarget), upVector);

			}
			if (Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				moveGameObject(camLookatMap.getValueAt(currentCamTarget), downVector);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				moveGameObject(camLookatMap.getValueAt(currentCamTarget), leftVector);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
				moveGameObject(camLookatMap.getValueAt(currentCamTarget), rightVector);

			}

			if (Gdx.input.isKeyPressed(Input.Keys.X)) {
				camLookatMap.getValueAt(currentCamTarget).transform.translate(dropDownVector);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
				camLookatMap.getValueAt(currentCamTarget).transform.translate(raiseUpVector);
				//camLookatMap.getValueAt(currentCamTarget).body.proceedToTransform(camLookatMap.getValueAt(currentCamTarget).transform);
				//cam.rotateAround(camLookatMap.getValueAt(currentCamTarget).center, rotateVector, 45f);
				//cam.update();
			}
			/*if (Gdx.input.isKeyPressed(Input.Keys.COMMA)){
				camLookatMap.getValueAt(currentCamTarget).transform.rotate(rotateVector, 5f);
				cam.rotate(camrotateVector, 5f);
				//cam.rotateAround(camLookatMap.getValueAt(currentCamTarget).center, rotateVector, 45f);
				cam.update();

			}*/


			if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)){
				currentCamTarget = currentCamTarget < camLookatMap.size - 1 ? 1 : 0;
				/*if (currentCamTarget < camLookatMap.size-1){
					currentCamTarget += 1;
				} else {
					currentCamTarget = 0;
				}*/
			}

			/*if (Gdx.input.isKeyPressed(Input.Keys.LEFT_BRACKET)) {
				camZoomX -= 1;
				camZoomY -= 1;
				camZoomZ -= 1;
			}*/

			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_6)) camLookAtX += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_4)) camLookAtX -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_8)) camLookAtY += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_2)) camLookAtY -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1)) camLookAtZ -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_3)) camLookAtZ += 1;

			//if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_7)) camPanX -= 1;
			//if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_9)) camPanX += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) camZoomY -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.NUM_0)) camZoomY += 1;

			if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_5)) camDegrees = camDegrees < 360 ? camDegrees += 1 : 0;


			if (Gdx.input.isKeyPressed(Input.Keys.T)) camRotX -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.G)) camRotY -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.B)) camRotZ -= 1;
			if (Gdx.input.isKeyPressed(Input.Keys.Y)) camRotX += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.H)) camRotY += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.N)) camRotZ += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.R)) camRot += 1;
			if (Gdx.input.isKeyPressed(Input.Keys.F)) camRot -= 1;

			camLookatMap.getValueAt(currentCamTarget).body.proceedToTransform(camLookatMap.getValueAt(currentCamTarget).transform);
			camLookatMap.getValueAt(currentCamTarget).center.set(camLookatMap.getValueAt(currentCamTarget).body.getCenterOfMassPosition());
			//Vector3 lookatVal = camPositionVec.add(camLookAtX, camLookAtY, camLookAtZ);
			Vector3 lookatVal = camLookatMap.getValueAt(currentCamTarget).center;
			Vector3 zoomValue = new Vector3(camZoomX, camZoomY, camZoomZ);
			camPositionVec.set(lookatVal.add(zoomValue));
			//cam.position.set(new Vector3(shipObject.center.x, shipObject.center.y, shipObject.center.z));
			//cam.position.sub(0f, -5f, 5f);.
			//cam.position.add(0f, 5f, 0f);
			//cam.project(shipObject.center);
			cam.position.set(camPositionVec);
			cam.position.rotate(camRot, camRotX, camRotY, camRotZ);
			//cam.rotateAround(lookatVal, lookatVal.add(lookatVal), 180f);
			//cam.position.rotate(camDegrees, camPanX, camPanY, camPanZ);
			cam.lookAt(lookatVal);
			//cam.update();
			//cam.position.add(camZoomX, camZoomY, camZoomZ);
			cam.update();

			stringBuilder.setLength(0);
			stringBuilder.append(" Weapon: ").append(activeWeapon).append("\n");
			stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
			stringBuilder.append(" Visible: ").append(visibleCount);
			stringBuilder.append(" Selected: ").append(selected);
			stringBuilder.append("\n");
			stringBuilder.append(" Size of instances: ").append(instances.size);
			stringBuilder.append(" camZoomX: ").append(camZoomX).append(" camZoomY: ").append(camZoomY).append(" camZoomZ:").append(camZoomZ);
			stringBuilder.append('\n').append(" camLookAtX: ").append(camLookAtX).append(" camLookAtY: ").append(camLookAtY).append(" camLookAtZ: ").append(camLookAtZ);
			stringBuilder.append('\n').append(" camRotX: ").append(camRotX).append(" camRotY: ").append(camRotY).append(" camRotZ: ").append(camRotZ).append(" camRot degs: ").append(camRot);

			label.setText(stringBuilder);
			label.setSize(screenW / 3, screenH / 4);
			label.setPosition(screenW-label.getWidth(), label.getHeight());

			stringBuilder = new StringBuilder();
			stringBuilder.append("Left Ctrl: Fire || Left Shift: Spawn blocks \n" )
					.append("Tab: Switch Object Control \n Arrow Keys: Move along X, Y || X and Z : Up and Down \n")
			.append("CamLookAt X up/down: 6/4 \n").append("CamLookAt Y: 8/2 \n CamLookAt Z: 3/1 \n" +
							"camRot: R/F \n camRotX : Y/T \n camRotY: H/G \n camRotZ: N/B \n");
					label2.setText(stringBuilder);
			label2.setSize(screenW / 3, screenH / 4);
			label2.setPosition(0, label.getHeight());
			//label.setSize(screenW, screenH/4);

		}
		modelBatch.end();
		spriteBatch.begin();

		xUp.setPosition(screenW-xUp.getWidth(), screenH-xUp.getHeight());
		xDown.setPosition(screenW-xUp.getWidth()*2, screenH-xUp.getHeight());
		yUp.setPosition(screenW-xUp.getWidth(), screenH-xUp.getHeight()*2);
		yDown.setPosition(screenW-xUp.getWidth()*2, screenH-xUp.getHeight()*2);
		zUp.setPosition(screenW-xUp.getWidth(), screenH-xUp.getHeight()*3);
		zDown.setPosition(screenW-xUp.getWidth()*2, screenH-xUp.getHeight()*3);
		//sUp.setPosition(sDown.getWidth(), sUp.getHeight());
		sUp.setPosition(sUp.getWidth(), sUp.getHeight());
		sDown.setPosition(sDown.getWidth(), 0);
		sLeft.setPosition(0, sLeft.getHeight()/2);
		sRight.setPosition(sRight.getWidth()*2, sRight.getHeight()/2);
		sAscent.setPosition(sAscent.getWidth()*2, sAscent.getHeight());
		sDescent.setPosition(sDescent.getWidth()*2, 0f);
		spriteBatch.end();

		switch(Gdx.app.getType()) {
			case Android:
				stage.addActor(fireButtonactor);
				stage.addActor(spawnButtonactor);
				stage.addActor(shipUpButtonactor);
				stage.addActor(shipDownButtonactor);
				stage.addActor(shipLeftButtonactor);
				stage.addActor(shipRightButtonactor);
				// desktop specific code
			case Desktop:
				stage.addActor(label);
				stage.addActor(label2);
				/// HTML5 specific code
		}
		stage.draw();
	}

	Vector3 touchPoint = new Vector3();

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button){
		shotX = screenX;
		shotY = screenY;
		target.set(screenX, screenY, 0);
		selecting = getObject(screenX, screenY);
		return selecting >= 0;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer){
		if (selecting < 0)
			return false;
		if (selected == selecting) {
			Ray ray = cam.getPickRay(screenX, screenY);
			final float distance = -ray.origin.y / ray.direction.y;
			position.set(ray.direction).scl(distance).add(ray.origin);
			instances.get(selected).transform.setTranslation(position);
			if(instances.get(selected).body != null){
				instances.get(selected).body.proceedToTransform(instances.get(selected).transform);
				instances.get(selected).body.setCenterOfMassTransform(instances.get(selected).transform);
			}
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button){
		if (selecting >= 0){
			if (selecting == getObject(screenX, screenY)) {
				setSelected(selecting);
				clickSound.play();
			}
			selecting = -1;
			return true;
		}
		return false;
	}

	public void setSelected(int value){
		if (selected == value) return;
		if (selected >= 0) {
			Material mat = instances.get(selected).materials.get(0);
			mat.clear();
			mat.set(originalMaterial);
		}
		selected = value;
		if (selected >= 0){
			Material mat = instances.get(selected).materials.get(0);
			originalMaterial.clear();
			originalMaterial.set(mat);
			mat.clear();
			mat.set(selectionMaterial);
		}
	}

	public int getObject(int screenX, int screenY){
		Ray ray = cam.getPickRay(screenX, screenY);
		int result = -1;
		float distance = -1;
		for (int i = 0; i < instances.size; ++i){
			final float dist2 = instances.get(i).intersects(ray);
			if (dist2 >= 0f && (distance < 0f || dist2 <= distance)){
				result = i;
				distance = dist2;
			}
		}
		return result;
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		instances.clear();
		assets.dispose();
		clickSound.dispose();
		angelicSound.dispose();
		clinkSound.dispose();
		for(int i = 0;i<soundArray.size;i++){
			soundArray.get(i).dispose();
		}
		spriteBatch.dispose();
		fireButton.dispose();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	public void moveGameObject (GameObject obj, Vector3 movVec){
		obj.transform.translate(movVec);
		obj.body.proceedToTransform(obj.transform);

	}
	public boolean getClick(Actor actor){
		int x1 = Gdx.input.getX();
		int y1 = Gdx.input.getY();
		Vector2 input = new Vector2();
		actor.stageToLocalCoordinates(input);

		if(input.x > actor.getX() && input.x < actor.getX() + actor.getWidth()
				&& input.y > actor.getY() && input.y < actor.getY() + actor.getHeight()){
			return true;
		}
		return false;
	}
	public class MyActor extends Actor{
		public boolean clicked;
		float actorX, actorY;
		String identifier;
		Texture texture;
		Sprite sprite;
		Rectangle bounds = new Rectangle();

		public MyActor(final String identifier, final Sprite sprite, float actorX, float actorY){
			this.sprite = sprite;
			this.texture = sprite.getTexture();
			setWidth(sprite.getWidth());
			setHeight(sprite.getHeight());
			setBounds(actorX, actorY, sprite.getWidth(), sprite.getHeight());
			this.actorX = actorX;
			this.actorY = actorY;
			this.texture = texture;
			this.identifier = identifier;
			addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					touchedActor = 1;
					touchedName = identifier;
					return true;  // must return true for touchUp event to occur
				}

				public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
					touchedActor = 0;
				}

				public void draw(SpriteBatch batch, float parentAlpha){
					batch.draw(sprite, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
				}
			});
		}

		@Override
		public void draw(Batch batch, float alpha){
			batch.draw(texture, actorX, actorY);
		}

		@Override
		public void act(float delta){

		}
	}
}
