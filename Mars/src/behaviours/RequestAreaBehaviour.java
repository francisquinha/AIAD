/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.lang.acl.ACLMessage;
import sajas.core.Agent;
import sajas.proto.AchieveREInitiator;

/**
 * Requests an area of Mars to itself by confirming with everyone else.
 * 
 * @author diogo
 */
public class RequestAreaBehaviour extends AchieveREInitiator {    

    public RequestAreaBehaviour(Agent a, ACLMessage msg) {
        super(a, msg);
    }
}
