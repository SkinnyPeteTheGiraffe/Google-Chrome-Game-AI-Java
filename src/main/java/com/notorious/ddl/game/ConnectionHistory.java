package com.notorious.ddl.game;

import com.notorious.ddl.game.node.Node;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * com.notorious.ddl.game
 *
 * @author Notorious
 * @version 0.0.1
 * @since 9/25/2019
 */
@Getter
public class ConnectionHistory {
    private int fromNode;
    private int toNode;
    private int innovationNumber;

    private List<Integer> innovationNumbers;//the innovation Numbers from the connections of the genome which first had this mutation
    //this represents the genome and allows us to test if another genoeme is the same
    //this is before this connection was added

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public ConnectionHistory(int from, int to, int inno, ArrayList<Integer> innovationNos) {
        fromNode = from;
        toNode = to;
        innovationNumber = inno;
        innovationNumbers = (ArrayList)innovationNos.clone();
        if (innovationNumbers == null) {
            innovationNumbers = new ArrayList<>();
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns whether the genome matches the original genome and the connection is between the same nodes
    public boolean matches(Genome genome, Node from, Node to) {
        if (genome.getGenes().size() == innovationNumbers.size()) { //if the number of connections are different then the genoemes aren't the same
            if (from.getNumber() == fromNode && to.getNumber() == toNode) {
                //next check if all the innovation numbers match from the genome
                for (int i = 0; i< genome.getGenes().size(); i++) {
                    if (!innovationNumbers.contains(genome.getGenes().get(i).getInnovationNo())) {
                        return false;
                    }
                }
                //if reached this far then the innovationNumbers match the genes innovation numbers and the connection is between the same nodes
                //so it does match
                return true;
            }
        }
        return false;
    }
}
