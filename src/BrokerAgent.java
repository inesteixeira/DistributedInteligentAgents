import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.AID;
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
	private Map<String,Integer> typeOfInsurance;
	private String comission;
	private String insuranceRequested;
	private String firstCondition;
	private String secondCondition;
	private String thirdCondition;
	private int priceInsurance;
	private AID client;
	private int numberOfServedClients = 0;
	private double rating = 5.0;
	private List<Integer> ratingHistory = new ArrayList<Integer>();

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
		typeOfInsurance = new HashMap<String, Integer>();

		if (args != null && args.length > 0) {
			name = (String) args[0];
			//typeOfInsurance = Arrays.asList(((String) args[1]).split("\\|"));
			for (String insurance : Arrays.asList(((String) args[1]).split("\\|"))){
				typeOfInsurance.put(insurance,0);
			}
			comission = (String) args[2];
		}

		// Default rating
		ratingHistory.add(5);

		// Add the behaviour serving requests for offer from agency agent
		SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
		sequentialBehaviour.addSubBehaviour(new OfferInsurance());
		sequentialBehaviour.addSubBehaviour(new DetailedInsurance());
		sequentialBehaviour.addSubBehaviour(new SellInsurance());
		addBehaviour(sequentialBehaviour);


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

				if (typeOfInsurance.containsKey(clientTypeOfInsurance) ) {
					insuranceRequested = clientTypeOfInsurance;
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(comission /*+ "|" +
							numberOfServedClients + "|" +
							typeOfInsurance.get(insuranceRequested) + "|" +
							rating*/);
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

	private class DetailedInsurance extends Behaviour {


		@Override
		public void action() {
			ACLMessage clientMsg = myAgent.receive();
			logger.log(Level.INFO, "Broker is ready");

			if(clientMsg != null){
				String content = clientMsg.getContent();
				logger.log(Level.INFO, "Broker received Client Message: " + content);
				firstCondition = content.split("\\|")[0];
				secondCondition = content.split("\\|")[1];
				thirdCondition = content.split("\\|")[2];
				client = clientMsg.getSender();

				numberOfServedClients++;

				// get price
				priceInsurance = 100;
			}
			else {
				block();
			}
		}

		@Override
		public boolean done() {
			logger.log(Level.INFO, "Broker is done");

			return true;
		}
	}

	private class SellInsurance extends Behaviour {

		private int step = 0;

		@Override
		public void action() {
			switch (step){
				case 0:
					ACLMessage negotiationMsg = new ACLMessage(ACLMessage.CFP);
					negotiationMsg.setContent(String.valueOf(priceInsurance));
					negotiationMsg.addReceiver(client);
					myAgent.send(negotiationMsg);

					step=1;
					break;
				case 1:
					ACLMessage reply = myAgent.receive();

					if (reply != null){
						if(reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
							int numberOfInsurances = typeOfInsurance.get(insuranceRequested);
							typeOfInsurance.put(insuranceRequested, numberOfInsurances + 1);
						}

						updateRating(Integer.parseInt(reply.getContent()));
					}
					else {
						block();
					}
			}




		}

		@Override
		public boolean done() {
			return true;
		}
	}

	private void updateRating(int newRating){
		int sum = 0;

		for(Integer rt : ratingHistory){
			sum += rt;
		}
		rating = sum / ratingHistory.size();
	}
}
