package com.aiad.agents;

import com.aiad.utils.Constants;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientAgent extends Agent {
    private String name;
    private String typeOfInsurance;
    private AID[] agency;
    private AID bestBroker;

    private String firstCondition;
    private String secondCondition;
    private String thirdCondition;
    private Boolean isNegotiator;
    private int maxDiscount;
    private int minDiscount;

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected void setup() {

        Object[] args = getArguments();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (args != null && args.length > 0) {

            name = (String) args[0];
            typeOfInsurance = (String) args[1];
            firstCondition = (String) args[2];
            secondCondition = (String) args[3];
            thirdCondition = (String) args[4];
            isNegotiator = Boolean.parseBoolean((String) args[5]);
            maxDiscount = Integer.parseInt((String) args[6]);
            minDiscount = Integer.parseInt((String) args[7]);
            //logger.info("isNegotiator: " + isNegotiator + " | maxDiscount: " + maxDiscount + " | minDiscount: " + minDiscount);

            System.out.println("Hello! I'm " + name + " and I need a " + typeOfInsurance + " insurance, please.");

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("agency");
            template.addServices(sd);


            try {
                DFAgentDescription[] result = DFService.search(this, template);
                agency = new AID[result.length];

                for (int i = 0; i < result.length; ++i) {
                    agency[i] = result[i].getName();
                    System.out.println("Agency agent: " + result[i].getName());                }
            } catch (FIPAException fe) {
                //agency
                logger.log(Level.SEVERE, "No agency found.");

                fe.printStackTrace();
            }

            SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
            sequentialBehaviour.addSubBehaviour(new RequestInsurance());
            sequentialBehaviour.addSubBehaviour(new InformInsurance());
            sequentialBehaviour.addSubBehaviour(new BuyInsurance());
            addBehaviour(sequentialBehaviour);
        }


    }

    protected void takeDown() {

        // Printout a dismissal message
        System.out.println("Client " + name + " terminated.");
    }


    private class RequestInsurance extends Behaviour {

        private int step = 0;

        // The counter of replies from seller agents
        private MessageTemplate mt;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to Agency
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID aid : agency) {
                        cfp.addReceiver(aid);
                    }
                    cfp.setContent(typeOfInsurance);
                    cfp.setConversationId("agency-discovery");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());

                    // Unique value
                    myAgent.send(cfp);

                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("agency-discovery"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);

                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            bestBroker = reply.getAllReplyTo().hasNext() ? (AID) reply.getAllReplyTo().next() : null;
                        }
                        else {
                            takeDown();
                        }
                        //logger.info( "Broker " + bestBroker.getName() + " is available.");
                        step = 2;
                    } else {
                        block();
                    }
                    break;

            }

        }

        @Override
        public boolean done() {
            return step == 2 && bestBroker != null;
        }
    }

    private class InformInsurance extends Behaviour{

        @Override
        public void action() {
            System.out.println( "Client sends info to broker: " + firstCondition  + "|" + secondCondition + "|" + thirdCondition);
            ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
            informMsg.setConversationId("insurance-details");
            informMsg.addReceiver(bestBroker);
            informMsg.setContent(firstCondition  + "|" + secondCondition + "|" + thirdCondition);
            myAgent.send(informMsg);
        }

        @Override
        public boolean done() {

            System.out.println( "Client is ready to negotiate insurance.");
            return true;
        }
    }

    private class BuyInsurance extends Behaviour{

        private boolean proposalTerminated;
        private int step = 0;

        @Override
        public void action() {

            double firstPrice=0;

            switch (step){

                case 0:
                    MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("round1-negotiation");
                    ACLMessage brokerMsg = myAgent.receive(messageTemplate);

                    if(brokerMsg != null){
                        firstPrice = Double.parseDouble(brokerMsg.getContent());

                        if(isNegotiator && maxDiscount != 0){
                            double newPrice = firstPrice * (1 - (maxDiscount / 100.0));

                            ACLMessage reply = brokerMsg.createReply();
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setConversationId("round2-negotiation");
                            reply.setContent(String.valueOf(newPrice));
                            reply.addReceiver(bestBroker);
                            myAgent.send(reply);
                            System.out.println("Client sent a new price proposal of: " + newPrice);
                            step = 1;
                        }
                        else{
                            ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            // Give broker a rating
                            reply.setConversationId("round2-negotiation");
                            reply.setContent(Constants.MEDIUM_HIGH_RATING);
                            reply.addReceiver(bestBroker);
                            myAgent.send(reply);
                            System.out.println( "Client buys insurance by fixed price.");
                            System.out.println( "Client rated the experience with: " + Constants.MEDIUM_HIGH_RATING);
                            proposalTerminated = true;
                        }
                    }
                    else{
                        block();
                    }
                    break;
                case 1:

                    ACLMessage newBrokerMsg = myAgent.receive();

                    if(newBrokerMsg != null){

                        if(newBrokerMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                            ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            reply.setConversationId("close-deal");
                            // Give broker a rating
                            reply.setContent(Constants.HIGH_RATING);
                            reply.addReceiver(bestBroker);
                            myAgent.send(reply);
                            System.out.println( "Client buys insurance with discount proposed.");
                            System.out.println( "Client rated the experience with: " + Constants.HIGH_RATING);
                            proposalTerminated = true;
                        }
                        else {
                            System.out.println("New Price Proposal: " + newBrokerMsg.getContent());
                            double newPriceProposal = Double.parseDouble(newBrokerMsg.getContent());
                            double maxPrice = firstPrice * (1 - (minDiscount / 100.0));

                            if(newPriceProposal <= maxPrice){
                                ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                reply.setConversationId("close-deal");
                                // Give broker a rating
                                reply.setContent(Constants.MEDIUM_RATING);
                                reply.addReceiver(bestBroker);
                                myAgent.send(reply);
                                System.out.println( "Client accepts new price proposal.");
                                System.out.println( "Client rated the experience with: " + Constants.MEDIUM_RATING);
                                proposalTerminated = true;
                            }
                            else {
                                ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
                                reply.setConversationId("close-deal");
                                // Give broker a rating
                                reply.setContent(Constants.LOW_RATING);
                                reply.addReceiver(bestBroker);
                                myAgent.send(reply);
                                System.out.println( "Client refuses new price proposal.");
                                System.out.println( "Client rated the experience with: " + Constants.LOW_RATING);
                                proposalTerminated = true;
                            }
                        }
                    }
                    else{
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return proposalTerminated;
        }
    }


}
