package com.notorious.ddl.game;

import com.notorious.ddl.game.node.Node;
import com.notorious.ddl.game.node.impl.EmptyNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import processing.core.PVector;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.notorious.ddl.game.Globals.nextConnectionNo;
import static org.apache.commons.lang3.RandomUtils.nextFloat;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.math3.util.FastMath.abs;
import static processing.core.PApplet.map;
import static processing.core.PConstants.CENTER;

/**
 * com.notorious.ddl.game
 *
 * @author Notorious
 * @version 0.0.1
 * @since 9/25/2019
 */
@Getter
@Slf4j
public class Genome {

    private final List<ConnectionGene> genes;//a list of connections between nodes which represent the NN
    private final List<Node> nodes;//list of nodes
    private List<Node> network;
    private int inputs;
    private int outputs;
    private int layers = 2;
    private int nextNode = 0;
    private int biasNode;

    //----------------------------------------------------------------------------------------------------------------------------------------
    //create an empty genome
    public Genome(int in, int out, boolean crossover) {
        //set input number and output number
        inputs = in;
        outputs = out;
        genes = new ArrayList<>();
        nodes = new ArrayList<>();
        network = new ArrayList<>();
    }

    public Genome(int in, int out) {
        genes = new ArrayList<>();
        nodes = new ArrayList<>();
        network = new ArrayList<>();
        //set input number and output number
        inputs = in;
        outputs = out;

        //create input nodes
        for (int i = 0; i < inputs; i++) {
            nodes.add(new Node(i));
            nextNode++;
            nodes.get(i).setLayer(0);
        }

        //create output nodes
        for (int i = 0; i < outputs; i++) {
            nodes.add(new Node(i + inputs));
            nodes.get(i + inputs).setLayer(1);
            nextNode++;
        }

        nodes.add(new Node(nextNode));//bias node
        biasNode = nextNode;
        nextNode++;
        nodes.get(biasNode).setLayer(0);
    }

    //called when this Genome is better that the other parent
    public Genome crossover(Genome parent2) {
        Genome child = new Genome(inputs, outputs, true);
        child.genes.clear();
        child.nodes.clear();
        child.layers = layers;
        child.nextNode = nextNode;
        child.biasNode = biasNode;
        List<ConnectionGene> childGenes = new ArrayList<>();//list of genes to be inherrited form the parents
        List<Boolean> isEnabled = new ArrayList<>();
        //all inherrited genes
        for (int i = 0; i < genes.size(); i++) {
            boolean setEnabled = true;//is this node in the chlid going to be enabled

            int parent2gene = matchingGene(parent2, genes.get(i).getInnovationNo());
            if (parent2gene != -1) {//if the genes match
                if (!genes.get(i).isEnabled() || !parent2.genes.get(parent2gene).isEnabled()) {//if either of the matching genes are disabled

                    if (nextFloat(0f, 1f) < 0.75) {//75% of the time disabel the childs gene
                        setEnabled = false;
                    }
                }
                float rand = nextFloat(0f, 1f);
                if (rand < 0.5) {
                    childGenes.add(genes.get(i));

                    //get gene from this fucker
                } else {
                    //get gene from parent2
                    childGenes.add(parent2.genes.get(parent2gene));
                }
            } else {//disjoint or excess gene
                childGenes.add(genes.get(i));
                setEnabled = genes.get(i).isEnabled();
            }
            isEnabled.add(setEnabled);
        }


        //since all excess and disjoint genes are inherrited from the more fit parent (this Genome) the childs structure is no different from this parent | with exception of dormant connections being enabled but this wont effect nodes
        //so all the nodes can be inherrited from this parent
        for (int i = 0; i < nodes.size(); i++) {
            child.nodes.add(nodes.get(i).clone());
        }

        //clone all the connections so that they connect the childs new nodes

        for (int i = 0; i < childGenes.size(); i++) {
            child.genes.add(childGenes.get(i).clone(child.getNode(childGenes.get(i).getFromNode().getNumber()),
                    child.getNode(childGenes.get(i).getToNode().getNumber())));
            child.genes.get(i).setEnabled(isEnabled.get(i));
        }

        child.connectNodes();
        return child;
    }

    public Node getNode(int nodeNumber) {
        return nodes.stream().filter(n ->
                n.getNumber() == nodeNumber).findFirst().orElse(new EmptyNode());
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //adds the conenctions going out of a node to that node so that it can acess the next node during feeding forward
    public void connectNodes() {
        //clear the connections
        nodes.forEach(node -> node.getOutputConnections().clear());
        //for each connectionGene
        //add it to node
        genes.forEach(gene -> gene.getFromNode().getOutputConnections().add(gene));
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //feeding in input values into the NN and returning output array
    public float[] feedForward(float[] inputValues) {
        //set the outputs of the input nodes
        for (int i = 0; i < inputs; i++) {
            nodes.get(i).setOutputValue(inputValues[i]);
        }
        nodes.get(biasNode).setOutputValue(1);//output of bias is 1
        //for each node in the network engage it(see node class for what this does)
        network.forEach(Node::engage);
        //the outputs are nodes[inputs] to nodes [inputs+outputs-1]
        float[] outs = new float[outputs];
        for (int i = 0; i < outputs; i++) {
            outs[i] = nodes.get(inputs + i).getOutputValue();
        }
        //reset all the nodes for the next feed forward
        nodes.forEach(node -> node.setInputSum(0));
        return outs;
    }

    //----------------------------------------------------------------------------------------------------------------------------------------
    //sets up the NN as a list of nodes in order to be engaged
    public void generateNetwork() {
        connectNodes();
        network = new ArrayList<>();
        //for each layer add the node in that layer, since layers cannot connect to themselves there is no need to order the nodes within a layer
        //for each layer
        IntStream.range(0, layers).forEach(l -> {
            //for each node
            //if that node is in that layer
            nodes.stream().filter(node -> node.getLayer() == l).forEach(node -> network.add(node));
        });
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    //returns the innovation number for the new mutation
    //if this mutation has never been seen before then it will be given a new unique innovation number
    //if this mutation matches a previous mutation then it will be given the same innovation number as the previous one
    int getInnovationNumber(List<ConnectionHistory> innovationHistory, Node from, Node to) {
        boolean isNew = true;
        int connectionInnovationNumber = nextConnectionNo;
        for (int i = 0; i < innovationHistory.size(); i++) {//for each previous mutation
            if (innovationHistory.get(i).matches(this, from, to)) {//if match found
                isNew = false;//its not a new mutation
                connectionInnovationNumber = innovationHistory.get(i).getInnovationNumber(); //set the innovation number as the innovation number of the match
                break;
            }
        }

        if (isNew) {//if the mutation is new then create an arrayList of integers representing the current state of the genome
            ArrayList<Integer> innoNumbers = new ArrayList<>();
            for (int i = 0; i < genes.size(); i++) {//set the innovation numbers
                innoNumbers.add(genes.get(i).getInnovationNo());
            }

            //then add this mutation to the innovationHistory
            innovationHistory.add(new ConnectionHistory(from.getNumber(), to.getNumber(), connectionInnovationNumber, innoNumbers));
            nextConnectionNo++;
        }
        return connectionInnovationNumber;
    }


    //returns whether the network is fully connected or not
    public boolean fullyConnected() {
        int maxConnections = 0;
        int[] nodesInLayers = new int[layers];//array which stored the amount of nodes in each layer
        //populate array
        //populate array
        nodes.forEach(node -> nodesInLayers[node.getLayer()] += 1);

        //for each layer the maximum amount of connections is the number in this layer * the number of nodes infront of it
        //so lets add the max for each layer together and then we will get the maximum amount of connections in the network
        for (int i = 0; i < layers - 1; i++) {
            int nodesInFront = 0;
            for (int j = i + 1; j < layers; j++) {//for each layer infront of this layer
                nodesInFront += nodesInLayers[j];//add up nodes
            }
            maxConnections += nodesInLayers[i] * nodesInFront;
        }

        //if the number of connections is equal to the max number of connections possible then it is full
        return maxConnections == genes.size();
    }

    //------------------------------------------------------------------------------------------------------------------
    //adds a connection between 2 nodes which aren't currently connected
    void addConnection(List<ConnectionHistory> innovationHistory) {
        //cannot add a connection to a fully connected network
        if (fullyConnected()) {
            log.warn("connection failed!");
            return;
        }
        //get random nodes
        int randomNode1 = nextInt(0, nodes.size());
        int randomNode2 = nextInt(0, nodes.size());
        if (!nodes.isEmpty()){
            while (randomConnectionNodesAreShit(randomNode1, randomNode2)) {//while the random nodes are no good
                //get new ones
                randomNode1 = nextInt(0, nodes.size());
                randomNode2 = nextInt(0, nodes.size());
            }
            int temp;
            if (nodes.get(randomNode1).getLayer() > nodes.get(randomNode2).getLayer()) {//if the first random node is after the second then switch
                temp = randomNode2;
                randomNode2 = randomNode1;
                randomNode1 = temp;
            }
        }

        //get the innovation number of the connection
        //this will be a new number if no identical genome has mutated in the same way
        int connectionInnovationNumber = getInnovationNumber(innovationHistory, nodes.get(randomNode1), nodes.get(randomNode2));
        //add the connection with a random array

        genes.add(new ConnectionGene(
                nodes.get(randomNode1),
                nodes.get(randomNode2),
                nextFloat(0f, 1f) > 0.5f ? 1 : -1 * nextFloat(0, 1),
                connectionInnovationNumber)
        );//changed this so if error here
        connectNodes();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public boolean randomConnectionNodesAreShit(int r1, int r2) {
        if (nodes.get(r1).getLayer() == nodes.get(r2).getLayer()) return true; // if the nodes are in the same layer
        if (nodes.get(r1).isConnectedTo(nodes.get(r2))) return true; //if the nodes are already connected
        return false;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------
    //mutate the NN by adding a new node
    //it does this by picking a random connection and disabling it then 2 new connections are added
    //1 between the input node of the disabled connection and the new node
    //and the other between the new node and the output of the disabled connection
    public void addNode(List<ConnectionHistory> innovationHistory) {
        //pick a random connection to create a node between
        if (genes.size() == 0) {
            addConnection(innovationHistory);
            return;
        }
        int randomConnection = nextInt(0, genes.size());

        while (genes.get(randomConnection).getFromNode() == nodes.get(biasNode) &&
                genes.size() != 1) {//dont disconnect bias
            randomConnection = nextInt(0, genes.size());
        }

        genes.get(randomConnection).setEnabled(false);//disable it

        int newNodeNo = nextNode;
        nodes.add(new Node(newNodeNo));
        nextNode++;
        //add a new connection to the new node with a weight of 1
        int connectionInnovationNumber = getInnovationNumber(innovationHistory,
                genes.get(randomConnection).getFromNode(), getNode(newNodeNo));
        genes.add(new ConnectionGene(genes.get(randomConnection).getFromNode(),
                getNode(newNodeNo), 1, connectionInnovationNumber));


        connectionInnovationNumber = getInnovationNumber(innovationHistory, getNode(newNodeNo), genes.get(randomConnection).getToNode());
        //add a new connection from the new node with a weight the same as the disabled connection
        genes.add(new ConnectionGene(getNode(newNodeNo), genes.get(randomConnection).getToNode(),
                genes.get(randomConnection).getWeight(), connectionInnovationNumber));
        getNode(newNodeNo).setLayer(genes.get(randomConnection).getFromNode().getLayer() + 1);


        connectionInnovationNumber = getInnovationNumber(innovationHistory, nodes.get(biasNode),
                getNode(newNodeNo));
        //connect the bias to the new node with a weight of 0
        genes.add(new ConnectionGene(nodes.get(biasNode), getNode(newNodeNo), 0,
                connectionInnovationNumber));

        //if the layer of the new node is equal to the layer of the output node of the old connection then a new layer needs to be created
        //more accurately the layer numbers of all layers equal to or greater than this new node need to be incrimented
        if (getNode(newNodeNo).getLayer() == genes.get(randomConnection).getToNode().getLayer()) {
            for (int i = 0; i < nodes.size() - 1; i++) {//dont include this newest node
                Node node = nodes.get(i);
                if (node.getLayer() >= getNode(newNodeNo).getLayer()) {
                    node.setLayer(node.getLayer() + 1);
                }
            }
            layers++;
        }
        connectNodes();
    }

    //-------------------------------------------------------------------------------------------------------------------------------
    //mutates the genome
    public void mutate(List<ConnectionHistory> innovationHistory) {
        if (genes.size() == 0) {
            addConnection(innovationHistory);
        }

        float rand1 = nextFloat(0f, 1f);
        if (rand1 < 0.8) { // 80% of the time mutate weights
            IntStream.range(0, genes.size()).forEach(i -> genes.get(i).mutateWeight());
        }
        //5% of the time add a new connection
        float rand2 = nextFloat(0f, 1f);
        if (rand2 < 0.08) {
            addConnection(innovationHistory);
        }


        //1% of the time add a node
        float rand3 = nextFloat(0f, 1f);
        if (rand3 < 0.02) {
            addNode(innovationHistory);
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------------------
    //returns whether or not there is a gene matching the input innovation number  in the input genome
    public int matchingGene(Genome parent2, int innovationNumber) {
        for (int i =0; i < parent2.genes.size(); i++) {
            if (parent2.genes.get(i).getInnovationNo() == innovationNumber) {
                return i;
            }
        }
        return -1; //no matching gene found
    }

    //---------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------
    //draw the genome on the screen
    public void drawGenome(PGraphics g, int startX, int startY, int w, int h) {
        //i know its ugly but it works (and is not that important) so I'm not going to mess with it
        ArrayList<ArrayList<Node>> allNodes = new ArrayList<ArrayList<Node>>();
        ArrayList<PVector> nodePoses = new ArrayList<PVector>();
        ArrayList<Integer> nodeNumbers= new ArrayList<Integer>();

        //get the positions on the screen that each node is supposed to be in


        //split the nodes into layers
        for (int i = 0; i< layers; i++) {
            ArrayList<Node> temp = new ArrayList<Node>();
            for (Node node : nodes) {
                if (node.getLayer() == i) {//check if it is in this layer
                    temp.add(node); //add it to this layer
                }
            }
            allNodes.add(temp);//add this layer to all nodes
        }

        //for each layer add the position of the node on the screen to the node posses arraylist
        for (int i = 0; i < layers; i++) {
            g.fill(255, 0, 0);
            float x = startX + (float)((i)*w)/(float)(layers-1);
            for (int j = 0; j< allNodes.get(i).size(); j++) {//for the position in the layer
                float y = startY + ((j + 1.0f) * h)/(allNodes.get(i).size() + 1.0f);
                nodePoses.add(new PVector(x, y));
                nodeNumbers.add(allNodes.get(i).get(j).getNumber());
                if(i == layers -1){
                    log.debug("[i: {}, j: {}] : [x: {}, y: {}]", i, j, x, y);
                }
            }
        }

        //draw connections
        g.stroke(0);
        g.strokeWeight(2);
        for (ConnectionGene gene : genes) {
            if (gene.isEnabled()) {
                g.stroke(0);
            } else {
                g.stroke(100);
            }
            PVector from;
            PVector to;
            from = nodePoses.get(nodeNumbers.indexOf(gene.getFromNode().getNumber()));
            to = nodePoses.get(nodeNumbers.indexOf(gene.getToNode().getNumber()));
            if (gene.getWeight() > 0) {
                g.stroke(255, 0, 0);
            } else {
                g.stroke(0, 0, 255);
            }
            g.strokeWeight(map(abs(gene.getWeight()), 0, 1, 0, 5));
            g.line(from.x, from.y, to.x, to.y);
        }

        //draw nodes last so they appear ontop of the connection lines
        for (int i = 0; i < nodePoses.size(); i++) {
            g.fill(255);
            g.stroke(0);
            g.strokeWeight(1);
            g.ellipse(nodePoses.get(i).x, nodePoses.get(i).y, 20, 20);
            g.textSize(10);
            g.fill(0);
            g.textAlign(CENTER, CENTER);
            g.text(nodeNumbers.get(i), nodePoses.get(i).x, nodePoses.get(i).y);
        }
    }
}
