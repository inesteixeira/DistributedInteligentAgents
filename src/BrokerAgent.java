import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BrokerAgent extends Agent{
	
	private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	// Put agent initializations here
	private String name;
	private List<String> typeOfInsurance;
	private String comission;

	protected void setup() { 
		
		DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("agency-broker");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {  
            DFService.register(this, dfd );  
        }
        catch (FIPAException fe) { fe.printStackTrace(); }

		Object[] args = getArguments();

		if (args != null && args.length > 0) {
			name = (String) args[0];
			typeOfInsurance = Arrays.asList(((String) args[1]).split("\\|"));
			comission = (String) args[2];
		}

		// Add the behaviour serving requests for offer from agency agent
		addBehaviour(new OfferInsurance());

	} 
	
	// Put agent clean-up operations here 
	protected void takeDown() { 

		// Printout a dismissal message     
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");   
	}


	private class OfferInsurance extends Behaviour {

		private MessageTemplate mtAgency;
		private boolean flagMessage;

		public void action() {
			mtAgency = MessageTemplate.MatchConversationId("broker-discovery");
			ACLMessage msg = myAgent.receive(mtAgency);


			if (msg != null) {
				// Message received. Process it
				ACLMessage reply = msg.createReply();
				reply.setConversationId("broker-discovery");

				logger.log(Level.INFO,"Broker received message.");
				String clientTypeOfInsurance = msg.getContent();

				if (typeOfInsurance.contains(clientTypeOfInsurance)) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(comission);
				} else {
					reply.setPerformative(ACLMessage.REFUSE);
				}

				myAgent.send(reply);
				logger.log(Level.INFO, "Broker " + name + " sent a message to Agency.");
				flagMessage=true;

			} else {
				block();
			}
		}

		@Override
		public boolean done() {
			if (flagMessage){
				logger.log(Level.INFO, "Broker " + name + " behaviour OfferInsurance is done.");
				return true;
			}
			else return false;
		}
	}
}
