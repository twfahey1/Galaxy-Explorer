package com.fahey.galaxyexp;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyActor extends Actor {
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

                return true;  // must return true for touchUp event to occur
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {

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

