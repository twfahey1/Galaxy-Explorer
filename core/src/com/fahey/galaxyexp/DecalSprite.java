package com.fahey.galaxyexp;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;

/**
 * Created by Tyler on 12/15/2014.
 */
public class DecalSprite {

    public Decal sprite;

    public DecalSprite() {

    }

    public DecalSprite build(String imgPath){
        Texture.TextureWrap texWrap = Texture.TextureWrap.ClampToEdge;
        return build(imgPath, texWrap);
    }

    public DecalSprite build(String imgPath, Texture.TextureWrap texWrap){
        Texture image = new Texture(Gdx.files.internal(imgPath));
        image.setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
        image.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        float w = image.getWidth();
        float h = image.getHeight();
        sprite = Decal.newDecal(w, h, new TextureRegion(image), true);
        return this;
    }

    public void faceCamera(Camera oCam){
        sprite.lookAt(oCam.position.cpy(), oCam.up.cpy().nor());

    }
}
