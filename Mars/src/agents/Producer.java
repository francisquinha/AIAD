package agents;

import behaviours.AnswerProducerRequestBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import java.awt.Color;
import uchicago.src.sim.space.Discrete2DSpace;

/**
 *
 * @author diogo
 */
public class Producer extends MarsAgent {
    
    public Producer(Discrete2DSpace space) {
        super(Color.GREEN, space);
    }
    
    @Override
    protected void setup() {
        MessageTemplate template = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        this.addBehaviour(new AnswerProducerRequestBehaviour(this, template));
    }
    
}
