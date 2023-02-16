package com.mygdx.game.actors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BaseActor extends Actor {
    protected World world;
    protected Vector2 position;
    protected Body body;
    protected Fixture fixture;

    public BaseActor(World world, Vector2 position) {
        this.world = world;
        this.position = position;
    }


    /**
     * Método encargado de indicar cuando actor se encuentra fuera de la pantalla
     * @return Devuelve true si la posición actual del actor está fuera de la pantalla o false si se
     * encuentra dentro
     */
    public boolean isOutOfScreen(float height){
        return this.body.getPosition().y <= -height - 0.1f;
    }

    /**
     *
     */
    public void detach(){
        this.body.destroyFixture(this.fixture);
        this.world.destroyBody(this.body);
    }
}