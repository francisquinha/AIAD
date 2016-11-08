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
 * Queries all the existing transporters and schedules them to pickup minerals.
 * 
 * @author diogo
 */
public class CallTransportersBehaviour extends ContractNetInitiator {

    public CallTransportersBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }
}
