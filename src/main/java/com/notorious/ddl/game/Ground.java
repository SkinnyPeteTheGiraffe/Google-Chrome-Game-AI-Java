package com.notorious.ddl.game;//this class is useless it just shows some dots on the ground

import lombok.Getter;
import processing.core.PApplet;
import processing.core.PGraphics;

import static com.notorious.ddl.game.Globals.groundHeight;
import static org.apache.commons.lang3.RandomUtils.nextInt;

@Getter
class Ground {

    private float posX;
    private float posY;
    private int w;

    Ground(PApplet applet) {
        posX = applet.width;
        posX = applet.width;
        posY = applet.height - nextInt(groundHeight - 20, groundHeight + 30);
        w = nextInt(1, 10);
    }

    void show(PGraphics g) {
        g.stroke(0);
        g.strokeWeight(3);
        g.line(posX, posY, posX + w, posY);

    }

    void move(float speed) {
        posX -= speed;
    }
}