
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AgencyAgent extends Agent {
	
	private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


	// The title of the book to buy 
	private String typeOfInsurance;
	// The list of known seller agents 
	private AID[] brokers;
	// Put agent initializations here   

	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		ServiceDescription sd  = new ServiceDescription();
		sd.setType("client-agency");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd );
		}
		catch (FIPAException fe) { fe.printStackTrace(); }

		// Perform the request
		addBehaviour(new BrokerDiscovery());
		addBehaviour(new BrokerAssignment());
	}

	private class BrokerDiscovery extends Behaviour {

		@Override
		public void action() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("agency-broker");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				brokers = new AID[result.length];

				for (int i = 0; i < result.length; ++i) {
					brokers[i] = result[i].getName();
					logger.log(Level.INFO,"Broker agent: " + result[i].getName());                }
			} catch (FIPAException fe) {
				//agency
				logger.log(Level.SEVERE, "No brokers found.");

				fe.printStackTrace();
			}
		}

		@Override
		public boolean done() {
			logger.log(Level.INFO, "Behaviour BrokerDiscovery done.");
			return true;
		}
	}


	private class BrokerAssignment extends Behaviour {

		private AID bestBroker;

		// The agent who provides the best offer
		private double bestComission = 1;

		// The best offered price
		private int repliesCnt = 0; 

		// The counter of replies from seller agents
		private MessageTemplate mtClient;
		private MessageTemplate mtBroker;

		// The template to receive replies
		private int step = 0; 

		public void action() {

			mtClient = MessageTemplate.MatchConversationId("agency-discovery");
			ACLMessage clientMsg = myAgent.receive(mtClient);

			switch (step) {
			case 0:
				if (clientMsg != null) {
					typeOfInsurance = clientMsg.getContent();
					step=1;
				} else {
					block();
				}
				break;
			case 1:
				// Send the cfp to Brokers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < brokers.length; ++i) {
					cfp.addReceiver(brokers[i]);
				}
				cfp.setContent(typeOfInsurance);
				cfp.setConversationId("broker-discovery");
				cfp.setReplyWith("cfp" + System.currentTimeMillis());

				// Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mtBroker = MessageTemplate.and(MessageTemplate.MatchConversationId("broker-discovery"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 2;
				break;
			case 2:
				// Receive all proposals/refusals from seller agents       
				ACLMessage reply = myAgent.receive(mtBroker);
				if (reply != null) {
					// Reply received 
					if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						// This is an offer  
						double brokerComission = Double.parseDouble(reply.getContent());
						if (bestBroker == null || brokerComission < bestComission) {
							// This is the best offer at present             
							bestComission = brokerComission;
							bestBroker = reply.getSender();
						}         
					}         
					repliesCnt++;
					if (repliesCnt >= brokers.length) {
						// We received all replies
						step = 3;
					}       
				} 
				else { 
					block();
				}
				break;
			case 3:
				ACLMessage replyClient = clientMsg.createReply();
				replyClient.setPerformative(ACLMessage.INFORM);
				replyClient.addReplyTo(bestBroker);

				break;
			}           
		} 
		public boolean done() { 
			return ((step==2 && bestBroker == null) || step == 3);
		} 
	}  // End of inner class RequestPerformer

}

