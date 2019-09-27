package com.notorious.ddl.game;

import com.rits.cloning.Cloner;
import lombok.Getter;
import lombok.Setter;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

import static com.notorious.ddl.game.Globals.*;

/**
 * com.notorious.ddl.game
 *
 * @author Notorious
 * @version 0.0.1
 * @since 9/25/2019
 */
@Getter
public class Player {

    private static final Cloner CLONER = new Cloner();

    private final PApplet applet;
    float fitness;
    Genome brain;
    boolean replay = false;

    float unadjustedFitness;
    int lifespan = 0;//how long the player lived for fitness
    int bestScore = 0;//stores the score achieved used for replay
    boolean dead;
    int score;
    @Setter
    int gen = 0;

    int genomeInputs = 7;
    int genomeOutputs = 3;

    float[] vision = new float[genomeInputs];//t he input array fed into the neuralNet
    float[] decision = new float[genomeOutputs]; //the out put of the NN
    //-------------------------------------
    float posY = 0;
    float velY = 0;
    float gravity = 1.2f;
    int runCount = -5;
    int size = 20;

    List<Obstacle> replayObstacles = new ArrayList<>();
    List<Bird> replayBirds = new ArrayList<>();
    List<Integer> localObstacleHistory = new ArrayList<>();
    List<Integer> localRandomAdditionHistory = new ArrayList<>();
    int historyCounter = 0;
    int localObstacleTimer = 0;
    float localSpeed = 10;
    int localRandomAddition = 0;

    boolean duck = false;
    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor

    public Player(PApplet applet) {
        this.applet = applet;
        brain = new Genome(genomeInputs, genomeOutputs);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //show the dino
    public void show(PGraphics g) {
        if (duck && posY == 0) {
            drawPlayer(g, dinoDuck, dinoDuck1);
        } else if (posY == 0) {
            drawPlayer(g, dinoRun1, dinoRun2);
        } else {
            g.image(dinoJump, playerXpos - dinoJump.width / 2,
                    applet.height - groundHeight - (posY + dinoJump.height));
        }
        runCount++;
        if (runCount > 5) {
            runCount = -5;
        }
    }

    private void drawPlayer(PGraphics g, PImage dinoRun1, PImage dinoRun2) {
        if (runCount < 0) {
            g.image(dinoRun1, playerXpos - dinoRun1.width / 2,
                    applet.height - groundHeight - (posY + dinoRun1.height));
        } else {
            g.image(dinoRun2, playerXpos - dinoRun2.width / 2,
                    applet.height - groundHeight - (posY + dinoRun2.height));
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------

    public void incrementCounters() {
        lifespan++;
        if (lifespan % 3 == 0) {
            score += 1;
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //checks for collisions and if this is a replay move all the obstacles
    public void move() {
        posY += velY;
        if (posY > 0) {
            velY -= gravity;
        } else {
            velY = 0;
            posY = 0;
        }

        if (!replay) {

            for (Obstacle obstacle : obstacles) {
                if (obstacle.collided(playerXpos, posY + (dinoRun1.height >> 1),
                        dinoRun1.width * 0.5f, dinoRun1.height)) {
                    dead = true;
                }
            }

            itterateBirds(birds);
        } else {//if replayign then move local obstacles
            for (Obstacle replayObstacle : replayObstacles) {
                if (replayObstacle.collided(playerXpos, posY + (dinoRun1.height >> 1),
                        dinoRun1.width * 0.5f, dinoRun1.height)) {
                    dead = true;
                }
            }


            itterateBirds(replayBirds);
        }
    }

    private void itterateBirds(List<Bird> birds) {
        for (Bird value : birds) {
            if (duck && posY == 0) {
                if (value.collided(playerXpos, posY + (dinoDuck.height >> 1),
                        dinoDuck.width * 0.8f, dinoDuck.height)) {
                    dead = true;
                }
            } else {
                if (value.collided(playerXpos, posY + (dinoRun1.height >> 1),
                        dinoRun1.width * 0.5f, dinoRun1.height)) {
                    dead = true;
                }
            }
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------
//what could this do????
    public void jump(boolean bigJump) {
        if (posY == 0) {
            if (bigJump) {
                gravity = 1;
                velY = 20;
            } else {
                gravity = 1.2f;
                velY = 16;
            }
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //if parameter is true and is in the air increase gravity
    void ducking(boolean isDucking) {
        if (posY != 0 && isDucking) {
            gravity = 3;
        }
        duck = isDucking;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //called every frame
    public void update() {
        incrementCounters();
        move();
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------
    //get inputs for Neural network
    public void look() {
        if (!replay) {
            float temp = 0;
            float min = 10000;
            int minIndex = -1;
            boolean berd = false;
            for (int i = 0; i < obstacles.size(); i++) {
                if (obstacles.get(i).getPosX() +
                        (obstacles.get(i).getW() >> 1) -
                        (playerXpos - (dinoRun1.width >> 1)) < min && obstacles.get(i).getPosX() +
                        obstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                    min = obstacles.get(i).getPosX() + obstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2);
                    minIndex = i;
                }
            }

            for (int i = 0; i < birds.size(); i++) {
                if (birds.get(i).getPosX() +
                        birds.get(i).getW() / 2 - (playerXpos - (dinoRun1.width >> 1)) < min &&
                        birds.get(i).getPosX() + birds.get(i).getW() / 2 -
                                (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                    min = birds.get(i).getPosX() + birds.get(i).getW() / 2 -
                            (playerXpos - (dinoRun1.width >> 1));
                    minIndex = i;
                    berd = true;
                }
            }
            vision[4] = speed;
            vision[5] = posY;


            if (minIndex == -1) {//if there are no obstacles
                vision[0] = 0;
                vision[1] = 0;
                vision[2] = 0;
                vision[3] = 0;
                vision[6] = 0;
            } else {

                vision[0] = 1.0f / (min / 10.0f);
                if (berd) {
                    vision[1] = birds.get(minIndex).getH();
                    vision[2] = birds.get(minIndex).getW();
                    if (birds.get(minIndex).getTypeOfBird() == 0) {
                        vision[3] = 0;
                    } else {
                        vision[3] = birds.get(minIndex).getPosY();
                    }
                } else {
                    vision[1] = obstacles.get(minIndex).getH();
                    vision[2] = obstacles.get(minIndex).getW();
                    vision[3] = 0;
                }


                //vision 6 is the gap between the this obstacle and the next one
                int bestIndex = minIndex;
                float closestDist = min;
                min = 10000;
                minIndex = -1;
                for (int i = 0; i < obstacles.size(); i++) {
                    if ((berd || i != bestIndex) &&
                            obstacles.get(i).getPosX() +
                                    (obstacles.get(i).getW() >> 1) -
                                    (playerXpos - (dinoRun1.width >> 1)) < min && obstacles.get(i).getPosX() +
                            obstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                        min = obstacles.get(i).getPosX() + obstacles.get(i).getW() / 2 -
                                (playerXpos - (dinoRun1.width >> 1));
                        minIndex = i;
                    }
                }

                for (int i = 0; i < birds.size(); i++) {
                    if ((!berd || i != bestIndex) && birds.get(i).getPosX() +
                            birds.get(i).getW() / 2 -
                            (playerXpos - (dinoRun1.width >> 1)) < min && birds.get(i).getPosX() +
                            birds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                        min = birds.get(i).getPosX() + birds.get(i).getW() / 2 -
                                (playerXpos - (dinoRun1.width >> 1));
                        minIndex = i;
                    }
                }

                if (minIndex == -1) {//if there is only one obejct on the screen
                    vision[6] = 0;
                } else {
                    vision[6] = 1 / (min - closestDist);
                }
            }
        } else {//if replaying then use local shit
            float temp = 0;
            float min = 10000;
            int minIndex = -1;
            boolean berd = false;
            for (int i = 0; i < replayObstacles.size(); i++) {
                if (replayObstacles.get(i).getPosX() + replayObstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) < min && replayObstacles.get(i).getPosX() + replayObstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                    min = replayObstacles.get(i).getPosX() + replayObstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2);
                    minIndex = i;
                }
            }

            for (int i = 0; i < replayBirds.size(); i++) {
                if (replayBirds.get(i).getPosX() + replayBirds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) < min && replayBirds.get(i).getPosX() + replayBirds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                    min = replayBirds.get(i).getPosX() + replayBirds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2);
                    minIndex = i;
                    berd = true;
                }
            }
            vision[4] = localSpeed;
            vision[5] = posY;


            if (minIndex == -1) {//if there are no replayObstacles
                vision[0] = 0;
                vision[1] = 0;
                vision[2] = 0;
                vision[3] = 0;
                vision[6] = 0;
            } else {

                vision[0] = 1.0f / (min / 10.0f);
                if (berd) {
                    vision[1] = replayBirds.get(minIndex).getH();
                    vision[2] = replayBirds.get(minIndex).getW();
                    if (replayBirds.get(minIndex).getTypeOfBird() == 0) {
                        vision[3] = 0;
                    } else {
                        vision[3] = replayBirds.get(minIndex).getPosY();
                    }
                } else {
                    vision[1] = replayObstacles.get(minIndex).getH();
                    vision[2] = replayObstacles.get(minIndex).getW();
                    vision[3] = 0;
                }


                //vision 6 is the gap between the this obstacle and the next one
                int bestIndex = minIndex;
                float closestDist = min;
                min = 10000;
                minIndex = -1;
                for (int i = 0; i < replayObstacles.size(); i++) {
                    if ((berd || i != bestIndex) && replayObstacles.get(i).getPosX() + replayObstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) < min && replayObstacles.get(i).getPosX() + replayObstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                        min = replayObstacles.get(i).getPosX() + replayObstacles.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2);
                        minIndex = i;
                    }
                }

                for (int i = 0; i < replayBirds.size(); i++) {
                    if ((!berd || i != bestIndex) && replayBirds.get(i).getPosX() + replayBirds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) < min && replayBirds.get(i).getPosX() + replayBirds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2) > 0) {//if the distance between the left of the player and the right of the obstacle is the least
                        min = replayBirds.get(i).getPosX() + replayBirds.get(i).getW() / 2 - (playerXpos - dinoRun1.width / 2);
                        minIndex = i;
                    }
                }

                if (minIndex == -1) {//if there is only one obejct on the screen
                    vision[6] = 0;
                } else {
                    vision[6] = 1 / (min - closestDist);
                }
            }
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //gets the output of the brain then converts them to actions
    public void think() {

        float max = 0;
        int maxIndex = 0;
        //get the output of the neural network
        decision = brain.feedForward(vision);

        for (int i = 0; i < decision.length; i++) {
            if (decision[i] > max) {
                max = decision[i];
                maxIndex = i;
            }
        }

        if (max < 0.7) {
            ducking(false);
            return;
        }

        switch (maxIndex) {
            case 0:
                jump(false);
                break;
            case 1:
                jump(true);
                break;
            case 2:
                ducking(true);
                break;
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns a clone of this player with the same brian
    public Player clone() {
        Player clone = new Player(applet);
        clone.brain = CLONER.deepClone(brain);
        clone.fitness = fitness;
        clone.brain.generateNetwork();
        clone.gen = gen;
        clone.bestScore = score;
        return clone;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //since there is some randomness in games sometimes when we want to replay the game we need to remove that randomness
    //this fuction does that

    public Player cloneForReplay() {
        Player clone = new Player(applet);
        clone.brain = CLONER.deepClone(brain);
        clone.fitness = fitness;
        clone.brain.generateNetwork();
        clone.gen = gen;
        clone.bestScore = score;
        clone.replay = true;
        if (replay) {
            clone.localObstacleHistory = CLONER.deepClone(localObstacleHistory);
            clone.localRandomAdditionHistory = CLONER.deepClone(localRandomAdditionHistory);
        } else {
            clone.localObstacleHistory = CLONER.deepClone(obstacleHistory);
            clone.localRandomAdditionHistory = CLONER.deepClone(randomAdditionHistory);
        }
        return clone;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //fot Genetic algorithm
    public void calculateFitness() {
        fitness = score * score;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    Player crossover(Player parent2) {
        Player child = new Player(applet);
        child.brain = brain.crossover(parent2.brain);
        child.brain.generateNetwork();
        return child;
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------------
    //if replaying then the dino has local obstacles
    void updateLocalObstacles(PGraphics g) {
        localObstacleTimer++;
        localSpeed += 0.002;
        if (localObstacleTimer > minimumTimeBetweenObstacles + localRandomAddition) {
            addLocalObstacle();
        }
        groundCounter++;
        if (groundCounter > 10) {
            groundCounter = 0;
            grounds.add(new Ground(applet));
        }

        moveLocalObstacles();
        showLocalObstacles(g);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    void moveLocalObstacles() {
        for (int i = 0; i < replayObstacles.size(); i++) {
            replayObstacles.get(i).move(localSpeed);
            if (replayObstacles.get(i).getPosX() < -100) {
                replayObstacles.remove(i);
                i--;
            }
        }

        for (int i = 0; i < replayBirds.size(); i++) {
            replayBirds.get(i).move(localSpeed);
            if (replayBirds.get(i).getPosX() < -100) {
                replayBirds.remove(i);
                i--;
            }
        }
        for (int i = 0; i < grounds.size(); i++) {
            grounds.get(i).move(localSpeed);
            if (grounds.get(i).getPosX() < -100) {
                grounds.remove(i);
                i--;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------
    void addLocalObstacle() {
        int tempInt = localObstacleHistory.get(historyCounter);
        localRandomAddition = localRandomAdditionHistory.get(historyCounter);
        historyCounter++;
        if (tempInt < 3) {
            replayBirds.add(new Bird(applet, tempInt));
        } else {
            replayObstacles.add(new Obstacle(applet, tempInt - 3));
        }
        localObstacleTimer = 0;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    void showLocalObstacles(PGraphics g) {
        for (int i = 0; i < grounds.size(); i++) {
            grounds.get(i).show(g);
        }
        for (int i = 0; i < replayObstacles.size(); i++) {
            replayObstacles.get(i).show(g);
        }

        for (int i = 0; i < replayBirds.size(); i++) {
            replayBirds.get(i).show(g);
        }
    }
}
