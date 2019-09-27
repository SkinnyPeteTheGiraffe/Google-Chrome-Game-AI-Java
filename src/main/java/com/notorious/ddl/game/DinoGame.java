package com.notorious.ddl.game;

import lombok.extern.slf4j.Slf4j;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

import static com.notorious.ddl.game.Globals.*;
import static org.apache.commons.lang3.RandomUtils.nextFloat;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.math3.util.FastMath.floor;
import static processing.core.PConstants.*;

/**
 * com.notorious.ddl.game
 *
 * @author Notorious
 * @version 0.0.1
 * @since 9/26/2019
 */
@Slf4j
public class DinoGame extends PApplet {

    //--------------------------------------------------------------------------------------------------------------------------------------------------


    @Override
    public void settings() {
        super.settings();
        Globals.initialize(this);
        size(800, 600);
        fullScreen();
    }

    public void setup() {
        super.setup();
        frameRate(60);
    }
    //--------------------------------------------------------------------------------------------------------------------------------------------------------
    public void draw() {
        drawToScreen(g);
        if (showBestEachGen) {//show the best of each gen
            if (!genPlayerTemp.dead) {//if current gen player is not dead then update it
                genPlayerTemp.updateLocalObstacles(g);
                genPlayerTemp.look();
                genPlayerTemp.think();
                genPlayerTemp.update();
                genPlayerTemp.show(g);
            } else {//if dead move on to the next generation
                upToGen ++;
                if (upToGen >= pop.genPlayers.size()) {//if at the end then return to the start and stop doing it
                    upToGen= 0;
                    showBestEachGen = false;
                } else {//if not at the end then get the next generation
                    genPlayerTemp = pop.genPlayers.get(upToGen).cloneForReplay();
                }
            }
        } else {//if just evolving normally
            if (!pop.done()) {//if any players are alive then update them
                updateObstacles(g);
                pop.updateAlive(g);
            } else {//all dead
                //genetic algorithm
                try {
                    pop.naturalSelection();
                } catch (CloneNotSupportedException e) {
                    log.error("Cloning Error!", e);
                }
                resetObstacles();
            }
        }
    }



    //---------------------------------------------------------------------------------------------------------------------------------------------------------
//draws the display screen
    public void drawToScreen(PGraphics g) {
        if (!showNothing) {
            background(250);
            stroke(0);
            strokeWeight(2);
            line(0, height - groundHeight - 30,
                    width, height - groundHeight - 30);
            drawBrain(g);
            writeInfo(g);
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void drawBrain(PGraphics g) {  //show the brain of whatever genome is currently showing
        int startX = 600;
        int startY = 10;
        int w = 600;
        int h = 400;
        if (showBestEachGen) {
            genPlayerTemp.brain.drawGenome(g, startX, startY, w, h);
        } else {
            for (int i = 0; i< pop.pop.size(); i++) {
                if (!pop.pop.get(i).dead) {
                    pop.pop.get(i).brain.drawGenome(g, startX, startY, w, h);
                    break;
                }
            }
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//writes info about the current player
    public void writeInfo(PGraphics g) {
        fill(200);
        textAlign(LEFT);
        textSize(40);
        if (showBestEachGen) { //if showing the best for each gen then write the applicable info
            text("Score: " + genPlayerTemp.score, 30, height - 30);
            //text(, width/2-180, height-30);
            textAlign(RIGHT);
            text("Gen: " + (genPlayerTemp.gen +1), width -40, height-30);
            textSize(20);
            int x = 580;
            text("Distace to next obstacle", x, 18+44.44444f);
            text("Height of obstacle", x, 18+2*44.44444f);
            text("Width of obstacle", x, 18+3*44.44444f);
            text("Bird height", x, 18+4*44.44444f);
            text("Speed", x, 18+5*44.44444f);
            text("Players Y position", x, 18+6*44.44444f);
            text("Gap between obstacles", x, 18+7*44.44444f);
            text("Bias", x, 18+8*44.44444f);

            textAlign(LEFT);
            text("Small Jump", 1220, 118);
            text("Big Jump", 1220, 218);
            text("Duck", 1220, 318);
        } else { //evolving normally
            text("Score: " + floor(pop.populationLife/3.0f), 30, height - 30);
            //text(, width/2-180, height-30);
            textAlign(RIGHT);

            text("Gen: " + (pop.gen +1), width -40, height-30);
            textSize(20);
            int x = 580;
            text("Distace to next obstacle", x, 18+44.44444f);
            text("Height of obstacle", x, 18+2*44.44444f);
            text("Width of obstacle", x, 18+3*44.44444f);
            text("Bird height", x, 18+4*44.44444f);
            text("Speed", x, 18+5*44.44444f);
            text("Players Y position", x, 18+6*44.44444f);
            text("Gap between obstacles", x, 18+7*44.44444f);
            text("Bias", x, 18+8*44.44444f);

            textAlign(LEFT);
            text("Small Jump", 1220, 118);
            text("Big Jump", 1220, 218);
            text("Duck", 1220, 318);
        }
    }


//--------------------------------------------------------------------------------------------------------------------------------------------------

    public void keyPressed() {
        switch(key) {
            case '+'://speed up frame rate
                frameSpeed += 10;
                frameRate(frameSpeed);
                log.debug("Frame Speed: {}", frameSpeed);
                break;
            case '-'://slow down frame rate
                if (frameSpeed > 10) {
                    frameSpeed -= 10;
                    frameRate(frameSpeed);
                    log.debug("Frame Speed: {}", frameSpeed);
                }
                break;
            case 'g'://show generations
                showBestEachGen = !showBestEachGen;
                upToGen = 0;
                genPlayerTemp = pop.genPlayers.get(upToGen).cloneForReplay();
                break;
            case 'n'://show absolutely nothing in order to speed up computation
                showNothing = !showNothing;
                break;
            case CODED://any of the arrow keys
                switch(keyCode) {
                    case RIGHT://right is used to move through the generations
                        if (showBestEachGen) {//if showing the best player each generation then move on to the next generation
                            upToGen++;
                            if (upToGen >= pop.genPlayers.size()) {//if reached the current generation then exit out of the showing generations mode
                                showBestEachGen = false;
                            } else {
                                genPlayerTemp = pop.genPlayers.get(upToGen).cloneForReplay();
                            }
                            break;
                        }
                        break;
                }
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------
//called every frame
    public void updateObstacles(PGraphics g) {
        obstacleTimer ++;
        speed += 0.002;
        if (obstacleTimer > minimumTimeBetweenObstacles + randomAddition) { //if the obstacle timer is high enough then add a new obstacle
            addObstacle();
        }
        groundCounter ++;
        if (groundCounter> 10) { //every 10 frames add a ground bit
            groundCounter =0;
            grounds.add(new Ground(this));
        }

        moveObstacles();//move everything
        if (!showNothing) {//show everything
            showObstacles(g);
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
//moves obstacles to the left based on the speed of the game
    public void moveObstacles() {
        log.debug("Speed: {}", speed);
        for (int i = 0; i< obstacles.size(); i++) {
            obstacles.get(i).move(speed);
            if (obstacles.get(i).getPosX() < -playerXpos) {
                obstacles.remove(i);
                i--;
            }
        }

        for (int i = 0; i< birds.size(); i++) {
            birds.get(i).move(speed);
            if (birds.get(i).getPosX() < -playerXpos) {
                birds.remove(i);
                i--;
            }
        }
        for (int i = 0; i < grounds.size(); i++) {
            grounds.get(i).move(speed);
            if (grounds.get(i).getPosX() < -playerXpos) {
                grounds.remove(i);
                i--;
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------
//every so often add an obstacle
    public void addObstacle() {
        int lifespan = pop.populationLife;
        int tempInt;
        if (lifespan > 1000 && nextFloat(0f, 1f) < 0.15f) { // 15% of the time add a bird
            tempInt = nextInt(0, 3);
            Bird temp = new Bird(this, tempInt);//floor(random(3)));
            birds.add(temp);
        } else {//otherwise add a cactus
            tempInt = nextInt(0, 3);
            Obstacle temp = new Obstacle(this, tempInt);//floor(random(3)));
            obstacles.add(temp);
            tempInt+=3;
        }
        obstacleHistory.add(tempInt);

        randomAddition = nextInt(0, 50);
        randomAdditionHistory.add(randomAddition);
        obstacleTimer = 0;
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------
//what do you think this does?
    public void showObstacles(PGraphics g) {
        for (int i = 0; i< grounds.size(); i++) {
            grounds.get(i).show(g);
        }
        for (int i = 0; i< obstacles.size(); i++) {
            obstacles.get(i).show(g);
        }

        for (int i = 0; i< birds.size(); i++) {
            birds.get(i).show(g);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
//resets all the obstacles after every dino has died
    public void resetObstacles() {
        randomAdditionHistory = new ArrayList<>();
        obstacleHistory = new ArrayList<>();

        obstacles = new ArrayList<>();
        birds = new ArrayList<>();
        obstacleTimer = 0;
        randomAddition = 0;
        groundCounter = 0;
        speed = 10;
    }
}
