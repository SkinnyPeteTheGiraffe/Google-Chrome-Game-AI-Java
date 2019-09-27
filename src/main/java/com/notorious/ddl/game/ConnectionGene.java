package com.notorious.ddl.game;

import com.notorious.ddl.game.node.Node;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import static org.apache.commons.lang3.RandomUtils.nextFloat;

//a connection between 2 nodes
@Getter
public class ConnectionGene {
  private final static JDKRandomGenerator GENERATOR = new JDKRandomGenerator();
  private static final GaussianRandomGenerator GAUSSIAN_RANDOM_GENERATOR = new GaussianRandomGenerator(GENERATOR);

  private Node fromNode;
  private Node toNode;
  private float weight;
  @Setter
  private boolean enabled;
  private int innovationNo;//each connection is given a innovation number to compare genomes
  //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  //constructor
  public ConnectionGene(Node from, Node to, float w, int inno) {
    fromNode = from;
    toNode = to;
    weight = w;
    innovationNo = inno;
    enabled = true;
  }

  //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   //changes the weight
  public void mutateWeight() {
    float rand2 = nextFloat(0f, 1f);
    if (rand2 < 0.1) {//10% of the time completely change the weight
      weight = nextFloat(0f, 1f) > 0.5f ? 1 : -1 * nextFloat(0, 1);
    } else {//otherwise slightly change it
      weight += GAUSSIAN_RANDOM_GENERATOR.nextNormalizedDouble() /50;
      //keep weight between bounds
      if(weight > 1){
        weight = 1;
      }
      if(weight < -1){
        weight = -1;
      }
    }
  }

  //----------------------------------------------------------------------------------------------------------
  //returns a copy of this ConnectionGene
  public ConnectionGene clone(Node from, Node  to) {
    ConnectionGene clone = new ConnectionGene(from, to, weight, innovationNo);
    clone.enabled = enabled;
    return clone;
  }
}