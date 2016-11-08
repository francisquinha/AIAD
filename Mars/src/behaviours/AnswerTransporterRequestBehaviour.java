/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.proto.ContractNetResponder;

/**
 * Answers the request for transporters.
 * 
 * @author diogo
 */
public class AnswerTransporterRequestBehaviour extends ContractNetResponder {
    
    public AnswerTransporterRequestBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
    
}
