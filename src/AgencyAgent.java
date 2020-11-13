
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
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

		// Regitering Agency agents
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		ServiceDescription sdClient  = new ServiceDescription();
		sdClient.setType("agency");
		sdClient.setName(getLocalName());
		dfd.addServices(sdClient);
		logger.log(Level.INFO, "Subscribing Agency Agent.");

		try {
			DFService.register(this, dfd );
		}
		catch (FIPAException fe) { fe.printStackTrace(); }

		// Subscribing to Agency-Broker
		logger.log(Level.INFO, "Setting up DF for Broker Discovery.");
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sdAgency = new ServiceDescription();
		sdAgency.setType("agency-broker");
		template.addServices(sdAgency);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			brokers = new AID[result.length];

			for (int i = 0; i < result.length; ++i) {
				brokers[i] = result[i].getName();
				logger.log(Level.INFO,"Broker agent: " + result[i].getName());                }
		} catch (FIPAException fe) {
			//agency
			logger.log(Level.SEVERE, "No brokers found.");

			fe.printStackTrace();
		}

		addBehaviour(new BrokerAssignment());
	}


	private class BrokerAssignment extends Behaviour {

		private AID bestBroker;

		// The agent who provides the best offer
		private double bestComission = 1;

		// The best offered price
		private int repliesCnt = 0; 

		// The counter of replies from seller agents
		private ACLMessage clientMsg;
		private ACLMessage cfpBroker;
		private MessageTemplate mtClient;
		private MessageTemplate mtBroker;

		// The template to receive replies
		private int step = 0; 

		public void action() {
			//ACLMessage reply;

			logger.log(Level.INFO, "Starting Agency Behaviour BrokerAssignment.");

			switch (step) {
			case 0:
				mtClient = MessageTemplate.MatchConversationId("agency-discovery");
				clientMsg = myAgent.receive(mtClient);

				if (clientMsg != null) {
					System.out.println("Agency received client request.");
					typeOfInsurance = clientMsg.getContent();
					step=1;
				} else {
					block();
				}
				break;
			case 1:
				// Send the cfp to Brokers
				cfpBroker = new ACLMessage(ACLMessage.CFP);

				for (int i = 0; i < brokers.length; ++i) {
					cfpBroker.addReceiver(brokers[i]);
				}
				cfpBroker.setContent(typeOfInsurance);
				cfpBroker.setConversationId("broker-discovery");
				cfpBroker.setReplyWith("cfp" + System.currentTimeMillis());

				// Unique value
				myAgent.send(cfpBroker);
				// Prepare the template to get proposals
				mtBroker = MessageTemplate.MatchConversationId("broker-discovery");

				logger.log(Level.INFO, "Agency is looking for interested brokers.");
				step = 2;
				break;
			case 2:
				// Receive all proposals/refusals from seller agents
				logger.log(Level.INFO, "Agency is waiting for all proposals/refusals from broker agents.");

				cfpBroker = myAgent.receive(mtBroker);

				if (cfpBroker != null) {
					//logger.log(Level.INFO, "step 2 broker message: " + reply.getContent());
					// Reply received 
					if (cfpBroker.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						double brokerComission = Double.parseDouble(cfpBroker.getContent());
						if (bestBroker == null || brokerComission < bestComission) {
							// This is the best offer at present             
							bestComission = brokerComission;
							bestBroker = cfpBroker.getSender();
						}         
					}         
					repliesCnt++;
					if (repliesCnt >= brokers.length) {
						// We received all replies.
						for(AID broker : brokers){
							if(broker.equals(bestBroker)){
								ACLMessage replyBestBroker = cfpBroker.createReply();
								replyBestBroker.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								replyBestBroker.addReceiver(bestBroker);
								logger.log(Level.INFO, "Agency sent ACCEPT PROPOSAL to Best Broker " + bestBroker.getLocalName());
								myAgent.send(replyBestBroker);
							}
							else{
								ACLMessage replyOthersBrokers = cfpBroker.createReply();
								replyOthersBrokers.setPerformative(ACLMessage.REFUSE);
								replyOthersBrokers.addReceiver(broker);
								myAgent.send(replyOthersBrokers);
								logger.log(Level.INFO, "Agency sent REFUSE to broker: " + broker.getName());
							}
						}
						step = 3;
					}
				} 
				else { 
					block();
				}
				break;
			case 3:
					logger.log(Level.INFO, "Best Broker is choosen: " + bestBroker.getName());
					ACLMessage replyClient = clientMsg.createReply();
					replyClient.setPerformative(ACLMessage.PROPOSE);
					replyClient.addReplyTo(bestBroker);
					myAgent.send(replyClient);
					step=4;
			}           
		}

		public boolean done() { 
			if((step==3 && bestBroker == null) || step == 4){
				logger.log(Level.INFO, "Agency:BrokerAssignment is done. Step = " + step);
				return true;
			}
			else{
				logger.log(Level.INFO,"BrokerAssignment Step: " + step);
				return false;
			}

		} 
	}

}

