package com.notorious.ddl.game;

import com.notorious.ddl.Boot;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

/**
 * com.notorious.ddl.game
 *
 * @author Notorious
 * @version 0.0.1
 * @since 9/25/2019
 */
public class Globals {

    public static int nextConnectionNo = 1000;
    public static Population pop;
    public static int frameSpeed = 60;

    public static boolean showBestEachGen = false;
    public static int upToGen = 0;
    public static Player genPlayerTemp;

    public static boolean showNothing = false;

    //img
    public static PImage dinoRun1;
    public static PImage dinoRun2;
    public static PImage dinoJump;
    public static PImage dinoDuck;
    public static PImage dinoDuck1;
    public static PImage smallCactus;
    public static PImage manySmallCactus;
    public static PImage bigCactus;
    public static PImage bird;
    public static PImage bird1;

    public static List<Obstacle> obstacles = new ArrayList<>();
    public static List<Bird> birds = new ArrayList<>();
    public static List<Ground> grounds = new ArrayList<>();

    public static int obstacleTimer = 0;
    public static int minimumTimeBetweenObstacles = 60;
    public static int randomAddition = 0;
    public static int groundCounter = 0;
    public static float speed = 10;

    public static int groundHeight = 250;
    public static int playerXpos = 150;

    public static List<Integer> obstacleHistory = new ArrayList<>();
    public static List<Integer> randomAdditionHistory = new ArrayList<>();

    public static void initialize(PApplet applet) {

        dinoRun1 = applet.loadImage(getFilename("dinorun0000.png"));
        dinoRun2 = applet.loadImage(getFilename("dinorun0001.png"));
        dinoJump = applet.loadImage(getFilename("dinoJump0000.png"));
        dinoDuck = applet.loadImage(getFilename("dinoduck0000.png"));
        dinoDuck1 = applet.loadImage(getFilename("dinoduck0001.png"));

        smallCactus = applet.loadImage(getFilename("cactusSmall0000.png"));
        bigCactus = applet.loadImage(getFilename("cactusBig0000.png"));
        manySmallCactus = applet.loadImage(getFilename("cactusSmallMany0000.png"));
        bird = applet.loadImage(getFilename("berd.png"));
        bird1 = applet.loadImage(getFilename("berd2.png"));
        pop = new Population(applet, 500); //<<number of dinosaurs in each generation
    }

    private static String getFilename(String relative) {
        return DinoGame.class.getResource(String.format("img/%s", relative)).getPath();
    }

}
