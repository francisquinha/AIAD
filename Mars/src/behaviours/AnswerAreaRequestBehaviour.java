/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.proto.AchieveREResponder;

/**
 * Answers the request for an area.
 * 
 * @author diogo
 */
public class AnswerAreaRequestBehaviour extends AchieveREResponder {
    
    public AnswerAreaRequestBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
    
}
