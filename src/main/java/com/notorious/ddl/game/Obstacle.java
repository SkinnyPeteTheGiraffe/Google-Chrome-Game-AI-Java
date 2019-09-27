package com.notorious.ddl.game;

import lombok.Getter;
import processing.core.PApplet;
import processing.core.PGraphics;

import static com.notorious.ddl.game.Globals.*;
import static processing.core.PConstants.CENTER;

@Getter
public class Obstacle {
  private final PApplet applet;
  private float posX;
  private int w ;
  private int h ;
  private int type;

  //------------------------------------------------------------------------------------------------------------------------------------------------------
  //constructor
  public Obstacle(PApplet applet, int t) {
    this.applet = applet;
    posX = this.applet.width;
    type = t;
    switch(type) {
    case 0://small cactus
      w = 40;
      h = 80;
      break;
    case 1://big cactus
      w = 60;
      h = 120;
      break;
    case 2://small cacti
      w = 120;
      h = 80;
      break;
    }
  }

  //------------------------------------------------------------------------------------------------------------------------------------------------------
  //show the cactus
  public void show(PGraphics g) {
    g.fill(0);
    g.rectMode(CENTER);
    switch(type) {
    case 0:
      g.image(smallCactus, posX - (smallCactus.width >> 1),
              applet.height - groundHeight - smallCactus.height);
      break;
    case 1:
      g.image(bigCactus, posX - (bigCactus.width >> 1),
              applet.height - groundHeight - bigCactus.height);
      break;
    case 2:
      g.image(manySmallCactus, posX - (manySmallCactus.width >> 1),
              applet.height - groundHeight - manySmallCactus.height);
      break;
    }
  }
  //------------------------------------------------------------------------------------------------------------------------------------------------------
  // move the obstacle
  public void move(float speed) {
    posX -= speed;
  }
  //------------------------------------------------------------------------------------------------------------------------------------------------------
  //returns whether or not the player collides with this obstacle
  public boolean collided(float playerX, float playerY, float playerWidth, float playerHeight) {

    float playerLeft = playerX - playerWidth/2;
    float playerRight = playerX + playerWidth/2;
    float thisLeft = posX - (w >> 1);
    float thisRight = posX + (w >> 1);

    if ((playerLeft<= thisRight && playerRight >= thisLeft ) ||
            (thisLeft <= playerRight && thisRight >= playerLeft)) {
      float playerDown = playerY - playerHeight/2;
      float thisUp = h;
      return playerDown <= thisUp;
    }
    return false;
  }
}