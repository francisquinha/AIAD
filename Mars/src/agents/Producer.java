package agents;

import behaviours.AnswerProducerRequestBehaviour;
import behaviours.LogBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import java.awt.Color;

/**
 *
 * @author diogo
 */
public class Producer extends MarsAgent {
    
    public Producer() {
        super(Color.GREEN);
    }
    
    @Override
    protected void setup() {
        this.addBehaviour(new LogBehaviour(this));
        
        MessageTemplate template = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        this.addBehaviour(new AnswerProducerRequestBehaviour(this, template));
        
        System.out.println("A producer was set up!");
    }
    
}
