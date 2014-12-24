package com.fahey.galaxyexp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.*;

/**
 * Created by Tyler on 12/22/2014.
 */
public class MyStage {

    Actor fireButtonactor;
    Actor spawnButtonactor;
    Actor shipUpButtonactor;
    Actor shipDownButtonactor;
    Actor shipLeftButtonactor;
    Actor shipRightButtonactor;

    protected Stage stage;
    protected Label label;
    protected Label label2;
    protected BitmapFont font;
    protected com.badlogic.gdx.utils.StringBuilder stringBuilder;
    Array<Sprite> spriteArray;

    public MyStage(){
        stage = new Stage();
        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        label2 = new Label(" ", new Label.LabelStyle(font, Color.WHITE));

        /*fireButton = new Texture(Gdx.files.internal("fireButton.png"));

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

        fireButtonactor = new MyActor("Fire", sFire, fireButton.getWidth() * 3, 0);
        fireButtonactor.setTouchable(Touchable.enabled);
        spawnButtonactor = new MyActor("Spawn", sSpawn, fireButton.getWidth() * 4, 0);
        spawnButtonactor.setTouchable(Touchable.enabled);
        shipUpButtonactor = new MyActor("shipUpButton", sUp, upButton.getWidth(), upButton.getHeight());
        shipUpButtonactor.setTouchable(Touchable.enabled);
        shipDownButtonactor = new MyActor("shipDownButton", sDown, downButton.getWidth(), 0);
        shipDownButtonactor.setTouchable(Touchable.enabled);
        shipLeftButtonactor = new MyActor("shipLeftButton", sLeft, 0, leftButton.getHeight() / 2);
        shipLeftButtonactor.setTouchable(Touchable.enabled);
        shipRightButtonactor = new MyActor("shipRightButton", sRight, rightButton.getWidth() * 2, rightButton.getHeight() / 2);
        shipRightButtonactor.setTouchable(Touchable.enabled);


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
        spriteArray.add(sAscent);*/
    }


}
