package com.mygdx.game.actors;

import static com.mygdx.game.extras.Utils.USER_APPLE;
import static com.mygdx.game.extras.Utils.USER_FLAMMIE;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.extras.AssetMan;

public class Apple extends BaseActor {
    // Atributos de la clase
    private static final float APPLE_WIDTH = 0.5f;
    private static final float APPLE_HEIGHT = 0.5f;
    private static final float APPLE_FIXTURE_RADIUS = 0.15f;
    private static final float SPEED = 1f;

    // Atributos de la instancia
    private TextureRegion appleTR;

    public Apple(World world, Vector2 position){
        super(world, position);
        this.appleTR = AssetMan.getInstance().getApple();

        createBody();
        createFixture();
    }



    // Métodos auxiliares
    private void createBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.KinematicBody;

        this.body = this.world.createBody(bodyDef);
        this.body.setLinearVelocity(0, SPEED);
    }

    private void createFixture(){
        CircleShape circle = new CircleShape();
        circle.setRadius(APPLE_FIXTURE_RADIUS);
        this.fixture = this.body.createFixture(circle, 8);
        this.fixture.setUserData(USER_APPLE);
        circle.dispose();
    }
}