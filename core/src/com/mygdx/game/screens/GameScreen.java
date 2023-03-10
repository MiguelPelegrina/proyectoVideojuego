package com.mygdx.game.screens;

import static com.mygdx.game.actors.Apple.APPLE_HEIGHT;
import static com.mygdx.game.actors.Apple.APPLE_WIDTH;
import static com.mygdx.game.actors.BaseActor.STATE_DEAD;
import static com.mygdx.game.actors.Bat.BAT_HEIGHT;
import static com.mygdx.game.actors.Bat.BAT_WIDTH;
import static com.mygdx.game.extras.Utils.SCREEN_HEIGHT;
import static com.mygdx.game.extras.Utils.SCREEN_WIDTH;
import static com.mygdx.game.extras.Utils.USER_APPLE;
import static com.mygdx.game.extras.Utils.USER_BAT;
import static com.mygdx.game.extras.Utils.USER_BOTTOMBORDER;
import static com.mygdx.game.extras.Utils.USER_FLAMMIE;
import static com.mygdx.game.extras.Utils.USER_LEFTBORDER;
import static com.mygdx.game.extras.Utils.USER_RIGHTBORDER;
import static com.mygdx.game.extras.Utils.USER_TOPBORDER;
import static com.mygdx.game.extras.Utils.WORLD_HEIGHT;
import static com.mygdx.game.extras.Utils.WORLD_WIDTH;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MainGame;
import com.mygdx.game.actors.Apple;
import com.mygdx.game.actors.AppleIdentifier;
import com.mygdx.game.actors.Bat;
import com.mygdx.game.actors.Flammie;
import com.mygdx.game.extras.AssetMan;

/**
 * Clase que gestiona la pantalla principal del juego
 */
public class GameScreen extends BaseScreen implements ContactListener {
    // Atributos de la clase
    private static final float BAT_SPAWN_TIME = 2f;
    private static final float APPLE_SPAWN_TIME = 3f;
    // Atributos de la instancia
    private float timeToCreateBat;
    private float timeToCreateApple;

    private Flammie flammie;
    private Sound hitSound;
    private Sound chompSound;

    private Body leftBorder;
    private Body rightBorder;
    private Body topBorder;
    private Body bottomBorder;
    private Fixture leftBorderFixture;
    private Fixture rightBorderFixture;
    private Fixture topBorderFixture;
    private Fixture bottomBorderFixture;

    private int scoreNumber;

    private Array<Bat> arrayBats;
    private Array<Apple> arrayApples;

    private OrthographicCamera worldCamera;

    /**
     * Constructor por par??metros
     * @param mainGame Instancia del juego
     */
    public GameScreen(MainGame mainGame) {
        super(mainGame);
        this.world.setContactListener(this);

        this.background = new Image(AssetMan.getInstance().getBackgroundPlay());
        prepareActors();

        prepareGameSound();

        this.scoreNumber = 0;
        this.text = "Apples";
        prepareMessage();

        this.worldCamera = (OrthographicCamera) this.stage.getCamera();
    }

    @Override
    public void render(float delta) {
        // Colocamos los murci??lagos
        addBats(delta);
        addApples(delta);
        // Configuramos el lote del escenario de tal forma que representa los elementos en funci??n
        // del tama??o del mundo
        this.stage.getBatch().setProjectionMatrix(worldCamera.combined);
        this.stage.act();
        this.world.step(delta,6,2);
        this.stage.draw();

        // Liberamos el espacio de la gr??fica destinado a las rocas que se encuentra ya fuera de la
        // pantalla
        removeBats();
        removeApples();

        // Configuramos el lote del escenario de tal forma que representa solo la fuente en funci??n
        // de la resoluci??n de la pantalla en p??xeles
        this.stage.getBatch().setProjectionMatrix(this.fontCamera.combined);
        this.stage.getBatch().begin();
        this.font.draw(this.stage.getBatch(), "Apples: " + this.scoreNumber, SCREEN_WIDTH*0.1f, SCREEN_HEIGHT*0.95f);
        this.stage.getBatch().end();
    }

    @Override
    public void show(){
        super.show();
        addFlammie();
        addBorder(leftBorder, leftBorderFixture, USER_LEFTBORDER, new Vector2(0,0),
                new Vector2(0, WORLD_HEIGHT));
        addBorder(rightBorder, rightBorderFixture, USER_RIGHTBORDER, new Vector2(WORLD_WIDTH,0),
                new Vector2(WORLD_WIDTH, WORLD_HEIGHT));
        addBorder(topBorder, topBorderFixture, USER_TOPBORDER, new Vector2(0,WORLD_HEIGHT),
                new Vector2(WORLD_WIDTH,WORLD_HEIGHT));
        addBorder(bottomBorder,bottomBorderFixture,USER_BOTTOMBORDER, new Vector2(0,0),
                new Vector2(WORLD_WIDTH,0));
        startMusic();
    }

    @Override
    public void hide() {
        this.flammie.detach();
        this.flammie.remove();
        this.background.remove();
        this.music.stop();
    }

    @Override
    public void dispose() {
        this.stage.dispose();
        this.world.dispose();
        this.music.dispose();
    }

    @Override
    public void beginContact(Contact contact) {
        // Para poder diferenciar una manzana de otra es necesario primero, averiguar si el fixture
        // de los objetos que establecen contacto son manzanas o no
        AppleIdentifier appleIdentifier = null;
        if(contact.getFixtureA().getUserData() instanceof AppleIdentifier){
            appleIdentifier = (AppleIdentifier) contact.getFixtureA().getUserData();
        }
        if(contact.getFixtureB().getUserData() instanceof AppleIdentifier){
            appleIdentifier = (AppleIdentifier) contact.getFixtureB().getUserData();
        }
        // Si los objetos son manzanas y se chocan con nuestro actor principal
        if(appleIdentifier != null && appleIdentifier.getName().equals(USER_APPLE)){
            eatApple(appleIdentifier);
        }else{
            if (areColliding(contact, USER_FLAMMIE, USER_BAT)) {
                endGame();
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    // M??todos auxiliares
    /**
     * M??todo encargado de instanciar los atributos implicados en la gesti??n de los actores
     */
    private void prepareActors(){
        this.arrayBats = new Array<>();
        this.arrayApples = new Array<>();
        this.timeToCreateBat= 0f;
        this.timeToCreateApple = 0f;
    }

    /**
     * M??todo encargado de instanciar el actor principal y a??adirlo al escenario
     */
    private void addFlammie(){
        this.flammie = new Flammie(this.world, new Vector2(WORLD_WIDTH/2f,WORLD_HEIGHT/4f));
        this.stage.addActor(this.flammie);
    }

    /**
     * M??todo encargado seguir a??adiendo manzanas al escenario mientras que el actor principal est??
     * vivo
     * @param delta Tiempo de desde la ??ltima llamada al m??todo render
     */
    private void addApples(float delta){
        // Comprobamos si el actor sigue vivo
        if(flammie.getState() == Flammie.STATE_ALIVE){
            // Aumentamos el contador de delta para gestionar la generaci??n de nuevas manzanas
            this.timeToCreateApple += delta;
            // Si ha pasado un tiempo predeterminado a??adimos una nueva manzana fuera de la visi??n
            // del jugador
            if (this.timeToCreateApple >= APPLE_SPAWN_TIME){
                this.timeToCreateApple -= APPLE_SPAWN_TIME;
                Apple apple = new Apple(this.world, new Vector2(MathUtils.random(APPLE_WIDTH,
                        WORLD_WIDTH - APPLE_WIDTH),WORLD_HEIGHT + APPLE_HEIGHT + 0.1f));
                arrayApples.add(apple);
                this.stage.addActor(apple);
            }
        }
    }

    /**
     * M??todo encargado de eliminar las manzanas del escenario
     */
    private void removeApples(){
        for(Apple apple : this.arrayApples){
            // Mientras que no se est?? actualizando el mundo en este momento
            if(!world.isLocked()){
                // Comprobamos si la manzana se encuentra dentro de la pantalla o se ha comido
                if(apple.isOutOfScreen(APPLE_HEIGHT) || apple.getState() == STATE_DEAD){
                    // Liberamos el espacio en la gr??fica
                    apple.detach();
                    // Quitamos la manzana de la escena
                    apple.remove();
                    // Sacamos la manzana del array
                    arrayApples.removeValue(apple,false);
                }
            }
        }
    }

    /**
     * M??todo encargado de a??adir murci??lagos al escenario mientras que el actor principal siga vivo
     * @param delta Tiempo de desde la ??ltima llamada al m??todo render
     */
    private void addBats(float delta){
        // Mientras que el actor principal est?? vivo
        if(flammie.getState() == Flammie.STATE_ALIVE){
            // Acumulamos el tiempo entre un fotograma y el siguiente
            this.timeToCreateBat += delta;
            // Si el tiempo acumulado supera el establecido
            if(this.timeToCreateBat >= BAT_SPAWN_TIME){
                // Reiniciamos el contador
                this.timeToCreateBat -= BAT_SPAWN_TIME;
                // Instanciamos un murci??lago fuera de la pantalla en funci??n de la posici??n
                // actual de nuestro protagonista
                Bat bat = new Bat(this.world, new Vector2(flammie.getX() + BAT_WIDTH/2f, WORLD_HEIGHT + 0.5f));
                arrayBats.add(bat);
                // A??adimos el murci??lago a la escena
                this.stage.addActor(bat);
            }
        }
    }

    /**
     * M??todo encargado eliminar los murci??lagos que hayan salido fuera de la visi??n del jugador
     */
    private void removeBats(){
        // Por cada uno de los murci??lagos
        for(Bat bat : this.arrayBats){
            // Mientras que no se est?? actualizando el mundo en este momento
            if(!world.isLocked()){
                // Comprobamos si el murci??lago se encuentra visibles
                if(bat.isOutOfScreen(BAT_HEIGHT)){
                    // Liberamos el espacio en la gr??fica
                    bat.detach();
                    // Quitamos el murci??lago de la escena
                    bat.remove();
                    // Sacamos los murci??lagos de la colecci??n
                    arrayBats.removeValue(bat,false);
                }
            }
        }
    }

    /**
     * M??todo encargado de "comer" una manzana
     * @param appleIdentifier Identificador de la manzana. Identifica a cada manzana de forma
     *                        independiente.
     */
    private void eatApple(AppleIdentifier appleIdentifier){
        // Incrementamos la puntuaci??n
        this.scoreNumber++;
        // Hacemos el sonido de comer
        this.chompSound.play();
        // Borramos del array de manzanas la manzana que corresponde
        for(Apple apple : this.arrayApples){
            // Averiguamos que manzana ha sido comida
            if(apple.getIdentifier().getNumber() == appleIdentifier.getNumber()){
                // Eliminamos la manzana
                apple.getsEaten();
            }
        }
    }

    /**
     * M??todo encargado de acabar el juego
     */
    private void endGame(){
        // Cambiamos el estado del actor principal
        this.flammie.dies();
        // Hacemos el sonido de morir
        this.hitSound.play();
        this.music.stop();
        // Paramos los dem??s actores involucrados
        for (Bat bat : arrayBats) {
            bat.stopActor();
        }
        for(Apple apple : this.arrayApples){
            apple.stopActor();
        }

        // Para que el sonido de morir del actor principal se juega
        this.stage.addAction(Actions.sequence(
                // Ralentizamos el cambio de una pantalla a otra durante tres segundos
                Actions.delay(3f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        // Pasamos la puntuaci??n a la pantalla de GameOver
                        mainGame.gameOverScreen.setScore(scoreNumber);
                        // Cambiamos a la pantalla de GameOver
                        mainGame.setScreen(mainGame.gameOverScreen);
                    }
                })
        ));
    }

    /**
     * M??todo encargado de configurar la m??sica y los sonidos
     */
    private void prepareGameSound() {
        this.music = AssetMan.getInstance().getBGMusic();
        this.hitSound = AssetMan.getInstance().getHitSound();
        this.chompSound = AssetMan.getInstance().getChompSound();
    }
}
