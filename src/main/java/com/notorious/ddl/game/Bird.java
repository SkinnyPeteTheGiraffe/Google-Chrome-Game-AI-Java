package com.notorious.ddl.game;

import lombok.Getter;
import processing.core.PApplet;
import processing.core.PGraphics;

import static com.notorious.ddl.game.Globals.*;

@Getter
public class Bird {
    private final PApplet applet;
    private float w = 60;
    private float h = 50;
    private float posX;
    private float posY;
    private int flapCount = 0;
    private int typeOfBird;

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public Bird(PApplet applet, int type) {
        this.applet = applet;
        posX = this.applet.width;
        typeOfBird = type;
        switch (type) {
            case 0://flying low
                posY = 10 + h / 2;
                break;
            case 1://flying middle
                posY = 100;
                break;
            case 2://flying high
                posY = 180;
                break;
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //show the birf
    public void show(PGraphics g) {
        flapCount++;

        if (flapCount < 0) {//flap the berd
            g.image(bird, posX - (bird.width >> 1),
                    applet.height - groundHeight - (posY + bird.height - 20));
        } else {
            g.image(bird1, posX - (bird1.width >> 1),
                    applet.height - groundHeight - (posY + bird1.height - 20));
        }
        if (flapCount > 15) {
            flapCount = -15;

        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //move the bard
    public void move(float speed) {
        posX -= speed;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns whether or not the bird collides with the player
    public boolean collided(float playerX, float playerY, float playerWidth, float playerHeight) {

        float playerLeft = playerX - playerWidth / 2;
        float playerRight = playerX + playerWidth / 2;
        float thisLeft = posX - w / 2;
        float thisRight = posX + w / 2;

        if ((playerLeft <= thisRight && playerRight >= thisLeft) || (thisLeft <= playerRight && thisRight >= playerLeft)) {
            float playerUp = playerY + playerHeight / 2;
            float playerDown = playerY - playerHeight / 2;
            float thisUp = posY + h / 2;
            float thisDown = posY - h / 2;
            return playerDown <= thisUp && playerUp >= thisDown;
        }
        return false;
    }
}