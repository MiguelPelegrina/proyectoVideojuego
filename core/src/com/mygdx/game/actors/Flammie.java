package com.mygdx.game.actors;

import static com.mygdx.game.extras.Utils.USER_FLAMMIE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.extras.AssetMan;

public class Flammie extends BaseActor {
    // Atributos de la clase
    // Solo se requieren en la propio clase
    private static final float FLAMMIE_WIDTH = 1f;
    private static final float FLAMMIE_HEIGHT = 1f;
    private static final float FLAMMIE_FIXTURE_RADIUS = 0.3f;
    private static final float SPEED_FACTOR = 1.5f;
    //Atributos de la instancia
    private int state;
    private Animation<TextureRegion> animation;
    private Animation<TextureRegion> animationStraight;
    private Animation<TextureRegion> animationLeft;
    private Animation<TextureRegion> animationRight;
    private float stateTime;
    // Esta variable es necesaria para obtener las coordenadas correctas de la posición de la
    // pantalla que se pulsa
    private Vector3 auxCoordinates;

    /**
     * Constructor por parámetros
     * @param world Mundo en el que se instancia el actor
     * @param position Posición en la que se instancia el actor
     */
    public Flammie(World world, Vector2 position){
        //Inicialización de atributos
        super(world, position);
        this.animationStraight = AssetMan.getInstance().getFlammieAnimation();
        this.animationLeft = AssetMan.getInstance().getFlammieAnimationL();
        this.animationRight = AssetMan.getInstance().getFlammieAnimationR();
        this.stateTime = 0f;
        this.state = STATE_ALIVE;
        // Lo instanciamos con valores por defecto para no realizar una instanciación en el act,
        // donde el tiempo es crucial
        this.auxCoordinates = new Vector3(position.x, position.y, 0);

        createDinamicBody(this.position);
        createCircularFixture(FLAMMIE_FIXTURE_RADIUS, USER_FLAMMIE);
    }

    /**
     * Método encargado de que el actor actue cuando el escenario se lo indica
     * @param delta Tiempo desde la última llamada al método render()
     */
    @Override
    public void act(float delta) {
        // Averiguamos si se está pulsando la pantalla
        boolean moving = Gdx.input.isTouched();
        // Si se está pulsando la pantalla y el actor vive
        if(moving && this.state == STATE_ALIVE){
            // Modificamos las coordenadas en función del input
            auxCoordinates.set(Gdx.input.getX(),Gdx.input.getY(), 0);
            // La cámara transforma las coordenadas de la pantalla a las coordenadas del mundo
            this.getStage().getCamera().unproject(auxCoordinates);
            // Modificamos la animación en función de las coordenadas de X, es decir, si nos
            // queremos mover hacia la izquierda utilizamos la animación de la izquierda y viceversa
            if(auxCoordinates.x > this.body.getPosition().x){
                this.animation = animationRight;
            }else{
                this.animation = animationLeft;
            }
            // Modificiamos la velocidad en función de la distancia entre la posición del cursor y
            // la del actor de tal forma que cuanto más lejos se encuentran el uno del otro más
            // acelera, tanto en el eje X como Y
            this.body.setLinearVelocity(
                    (auxCoordinates.x - this.body.getPosition().x) * SPEED_FACTOR,
                    (auxCoordinates.y - this.body.getPosition().y) * SPEED_FACTOR
            );
        }else{
            // Movimiento por defecto cuando no se toca la pantalla
            this.body.setLinearVelocity(0,0);
            this.animation = animationStraight;
        }
    }

    /**
     * Método encargado de pintar Flammie
     * @param batch Lote que se encarga de pintar el actor
     * @param parentAlpha Indica el nivel de transparencia del padre
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        setPosition(body.getPosition().x - FLAMMIE_WIDTH/2, body.getPosition().y - FLAMMIE_HEIGHT/2);
        batch.draw(this.animation.getKeyFrame(stateTime, true), getX(), getY(), FLAMMIE_WIDTH,FLAMMIE_HEIGHT);
        stateTime += Gdx.graphics.getDeltaTime();
    }

    /**
     * Método encargado de devolver el estado de Flammie
     * @return Devuelve el estado de Flammie
     */
    public int getState(){
        return this.state;
    }

    /**
     * Método encargado de cambiar el estado del personaje a muerto cuando colisiona con un enemigo.
     */
    public void dies(){
        state = STATE_DEAD;
        stateTime = 0f;
    }
}
