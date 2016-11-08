/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.lang.acl.ACLMessage;
import sajas.core.Agent;
import sajas.proto.ContractNetInitiator;

/**
 * Calls a producer to a mineral.
 * 
 * @author diogo
 */
public class CallProducerBehaviour extends ContractNetInitiator {

    public CallProducerBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }
    
}
