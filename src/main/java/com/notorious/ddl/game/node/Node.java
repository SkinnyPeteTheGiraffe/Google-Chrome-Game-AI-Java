package com.notorious.ddl.game.node;

import com.notorious.ddl.game.ConnectionGene;
import lombok.Getter;
import lombok.Setter;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.math3.util.FastMath.pow;

@Getter
@Setter
public class Node {
    private int number;
    private float inputSum;//current sum i.e. before activation
    private float outputValue; //after activation function is applied
    private final List<ConnectionGene> outputConnections;
    private int layer;
    private PVector drawPos = new PVector();
    private boolean dead;

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public Node(int no) {
        outputConnections = new ArrayList<>();
        dead = no == -1;
        number = no;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //the node sends its output to the inputs of the nodes its connected to
    public void engage() {
        if (layer != 0) {//no sigmoid for the inputs and bias
            outputValue = sigmoid(inputSum);
        }
        outputConnections.forEach(o -> {
            if (o.isEnabled()) {//dont do shit if not enabled
                o.getToNode().inputSum += o.getWeight() * outputValue;//add the weighted output to the sum of the inputs of whatever node this node is connected to
            }
        });
    }

    //----------------------------------------------------------------------------------------------------------------------------------------
//not used
    public float stepFunction(float x) {
        if (x < 0) {
            return 0;
        } else {
            return 1;
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//sigmoid activation function
    public float sigmoid(float x) {
        return 1 / (1 + (float) pow((float) Math.E, -4.9 * x));
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns whether this node connected to the parameter node
    //used when adding a new connection
    public boolean isConnectedTo(Node node) {
        if (node.layer == layer) {//nodes in the same layer cannot be connected
            return false;
        }

        //you get it
        if (node.layer < layer) {
            for (int i = 0; i < node.outputConnections.size(); i++) {
                if (node.outputConnections.get(i).getToNode().equals(this)) {
                    return true;
                }
            }
        } else {
            for (ConnectionGene outputConnection : outputConnections) {
                if (outputConnection.getToNode().equals(node)) {
                    return true;
                }
            }
        }

        return false;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns a copy of this node
    public Node clone() {
        Node clone = new Node(number);
        clone.layer = layer;
        return clone;
    }
}