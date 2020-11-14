package com.aiad.agents;

import com.aiad.components.api.EventsTrigger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AgencyAgent extends Agent {
	
	private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private String typeOfInsurance;
	private AID[] brokers;
	private EventsTrigger eventsTrigger;

	protected void setup() {

		// Regitering Agency agents
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		ServiceDescription sdClient  = new ServiceDescription();
		sdClient.setType("agency");
		sdClient.setName(getLocalName());
		dfd.addServices(sdClient);
		System.out.println( "Subscribing Agency Agent.");

		try {
			DFService.register(this, dfd );
		}
		catch (FIPAException fe) { fe.printStackTrace(); }

		// Subscribing to Agency-Broker
		//logger.info( "Setting up DF for Broker Discovery.");
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sdAgency = new ServiceDescription();
		sdAgency.setType("agency-broker");
		template.addServices(sdAgency);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			brokers = new AID[result.length];

			for (int i = 0; i < result.length; ++i) {
				brokers[i] = result[i].getName();
				System.out.println("Broker agent: " + result[i].getName());                }
		} catch (FIPAException fe) {
			logger.log(Level.SEVERE, "No brokers found.");

			fe.printStackTrace();
		}

		addBehaviour(new BrokerAssignment());
	}


	private class BrokerAssignment extends Behaviour {

		private AID bestBroker;
		private double bestComission = 1;
		private int repliesCnt = 0;
		private ACLMessage clientMsg;
		private MessageTemplate mtBroker;
		private int step = 0;

		public void action() {
			//logger.info( "Starting Agency Behaviour BrokerAssignment.");

			switch (step) {
			case 0:
				MessageTemplate mtClient = MessageTemplate.MatchConversationId("agency-discovery");
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
				ACLMessage cfpBroker = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < brokers.length; ++i) {
					cfpBroker.addReceiver(brokers[i]);
				}
				cfpBroker.setContent(typeOfInsurance);
				cfpBroker.setConversationId("broker-discovery");
				cfpBroker.setReplyWith("cfp" + System.currentTimeMillis());

				myAgent.send(cfpBroker);
				mtBroker = MessageTemplate.MatchConversationId("broker-discovery");

				System.out.println( "Agency is looking for interested brokers.");
				step = 2;
				break;
			case 2:
				// Receive all proposals/refusals from seller agents
				System.out.println( "Agency is waiting for all proposals/refusals from broker agents.");

				cfpBroker = myAgent.receive(mtBroker);

				if (cfpBroker != null) {
					//logger.info( "step 2 broker message: " + reply.getContent());
					// Reply received
					if (cfpBroker.getPerformative() == ACLMessage.PROPOSE) {
						double brokerComission = Double.parseDouble(cfpBroker.getContent());
						if (bestBroker == null || brokerComission < bestComission) {
							bestComission = brokerComission;
							bestBroker = cfpBroker.getSender();
							//logger.info("... best broker " + bestBroker.getName());
						}
					}
					repliesCnt++;
					if (repliesCnt >= brokers.length) {
						//logger.info("Best broker " + bestBroker.getName());
						// We received all replies.
						for(AID broker : brokers){
							//logger.info(broker.getLocalName() + " == " + bestBroker.getLocalName());
							if(bestBroker != null) {
								if (broker.getLocalName().equals(bestBroker.getLocalName())) {
									ACLMessage replyBestBroker = cfpBroker.createReply();
									replyBestBroker.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
									replyBestBroker.setConversationId(broker.getLocalName());
									replyBestBroker.addReceiver(bestBroker);
									replyBestBroker.setContent("You are the chosen one.");
									System.out.println("Agency sent ACCEPT PROPOSAL to broker " + bestBroker.getLocalName());// + " + " + replyBestBroker.getContent());
									myAgent.send(replyBestBroker);
								}
								else{
									ACLMessage replyOthersBrokers = cfpBroker.createReply();
									replyOthersBrokers.setPerformative(ACLMessage.REFUSE);
									replyOthersBrokers.setConversationId(broker.getLocalName());
									replyOthersBrokers.addReceiver(broker);
									replyOthersBrokers.setContent("You are NOT the chosen one.");
									myAgent.send(replyOthersBrokers);
									System.out.println("Agency sent REFUSE to broker " + broker.getLocalName());// + " + " + replyOthersBrokers.getContent());
								}
							}
							else{
								ACLMessage replyOthersBrokers = cfpBroker.createReply();
								replyOthersBrokers.setPerformative(ACLMessage.REFUSE);
								replyOthersBrokers.setConversationId(broker.getLocalName());
								replyOthersBrokers.addReceiver(broker);
								replyOthersBrokers.setContent("You are NOT the chosen one.");
								myAgent.send(replyOthersBrokers);
								System.out.println("Agency sent REFUSE to broker " + broker.getLocalName());// + " + " + replyOthersBrokers.getContent());

								ACLMessage replyClient = clientMsg.createReply();
								replyClient.setPerformative(ACLMessage.REFUSE);
								myAgent.send(replyClient);
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
				ACLMessage replyClient = clientMsg.createReply();
				replyClient.setPerformative(ACLMessage.PROPOSE);
				replyClient.addReplyTo(bestBroker);
				myAgent.send(replyClient);
				step=4;

				break;
			}
		}
		public boolean done() {
			if((step==3 && bestBroker == null) || step == 4){
				//logger.info( "Agency:BrokerAssignment is done. Step = " + step);
				return true;
			}
			else{
				//logger.info("BrokerAssignment Step: " + step);
				return false;
			}

		} 
	}

}

