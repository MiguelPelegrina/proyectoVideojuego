package com.mygdx.game.screens;


import static com.mygdx.game.extras.Utils.SCREEN_HEIGHT;
import static com.mygdx.game.extras.Utils.SCREEN_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mygdx.game.MainGame;
import com.mygdx.game.extras.AssetMan;

/**
 *
 */
public class GameOverScreen extends BaseScreen{
    private int score;

    /**
     * Constructor por parámetros
     * @param mainGame
     */
    public GameOverScreen(MainGame mainGame) {
        super(mainGame);

        this.background = new Image(AssetMan.getInstance().getBackgroundDark());
        prepareMessage();

        this.music = AssetMan.getInstance().getGOMusic();
    }

    /**
     *
     */
    @Override
    public void show(){
        super.show();
        startMusic();
    }

    /**
     *
     */
    @Override
    public void hide() {
        this.music.stop();
    }

    /**
     *
     */
    @Override
    public void dispose() {
        this.music.dispose();
    }

    @Override
    public void render(float delta){
        super.render(delta);

        this.stage.getBatch().setProjectionMatrix(this.fontCamera.combined);
        this.stage.getBatch().begin();
        this.font.draw(this.stage.getBatch(), "You got\n" + this.score + this.text,
                SCREEN_WIDTH*0.3f, SCREEN_HEIGHT*0.8f);
        this.stage.getBatch().end();

        if(Gdx.input.isTouched()){
            this.mainGame.setScreen(new GameScreen(this.mainGame));
        }
    }

    /**
     *
     * @param score
     */
    public void setScore(int score){
        this.score = score;
        if(this.score == 1){
            this.text = " apple! \n\nTouch the \nscreen to \ntry again!";
        }else{
            this.text = " apples! \n\nTouch the \nscreen to \ntry again!";
        }
    }
}
