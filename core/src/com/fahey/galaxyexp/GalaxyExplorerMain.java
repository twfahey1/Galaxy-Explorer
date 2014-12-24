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

public class GalaxyExplorerMain extends InputAdapter implements ApplicationListener {
	MyAssetManager assetManager;
	static MyWorld world;
	MyCam cam;
	public boolean loading;

	@Override
	public void create() {
		Bullet.init();
		world = new MyWorld();
		assetManager = new MyAssetManager();
		cam = new MyCam();

		loading = true;

	}
	@Override
	public void render() {
		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
		if (loading && assetManager.assets.update()) {
			assetManager.doneLoading();
		}
		if (assetManager.loading = false){
			world.stepSim(delta);
			cam.updateCam();
			Gdx.gl.glViewport(0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			assetManager.modelBatch.begin(cam.cam);
			for(MyGameObjects.GameObject i : assetManager.environmentInstances){

				if (i.isVisible(cam.cam)){
					assetManager.modelBatch.render(i,world.environment);
				}
			}

		}
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	@Override
	public void dispose() {

	}
	@Override
	public void resize(int width, int height) {
		//stage.getViewport().update(width, height, true);
	}
	@Override
	public void pause() {
	}
	@Override
	public void resume() {
	}
}