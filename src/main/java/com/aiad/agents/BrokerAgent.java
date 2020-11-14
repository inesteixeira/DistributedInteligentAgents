package com.aiad.agents;

import com.aiad.Health;
import com.aiad.House;
import com.aiad.Vehicle;
import com.aiad.components.api.EventsTrigger;
import com.aiad.components.impl.EventsTriggerImpl;
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

import java.util.*;
import java.util.logging.Logger;

public class BrokerAgent extends Agent {

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private String name;
    private Map<String, Integer> typeOfInsurance;
    private String comission;
    private String insuranceRequested;
    private int priceInsurance;
    private AID client;
    private double rating = 5.0;
    private final List<Integer> ratingHistory = new ArrayList<Integer>();
    private boolean activeBroker;
    private boolean appliesDiscount;
    private int maxDiscount;

    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agency-broker");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        Object[] args = getArguments();
        typeOfInsurance = new HashMap<String, Integer>();

        if (args != null && args.length > 0) {
            name = (String) args[0];

            for (String insurance : Arrays.asList(((String) args[1]).split("\\|"))) {
                typeOfInsurance.put(insurance, 0);
            }
            comission = (String) args[2];
            appliesDiscount = Boolean.parseBoolean((String) args[3]);
            maxDiscount = Integer.parseInt((String) args[4]);
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
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    private class OfferInsurance extends Behaviour {

        private MessageTemplate mtAgency;
        private ACLMessage msg;
        private int step = 0;

        public void action() {
            mtAgency = MessageTemplate.MatchConversationId("broker-discovery");
            msg = myAgent.receive(mtAgency);

            switch (step) {
                case 0:
                    if (msg != null) {
                        // Message received. Process it
                        ACLMessage reply = msg.createReply();
                        reply.setConversationId("broker-discovery");

                        //logger.info("Broker received message.");
                        String clientTypeOfInsurance = msg.getContent();

                        if (typeOfInsurance.containsKey(clientTypeOfInsurance)) {
                            insuranceRequested = clientTypeOfInsurance;
                            reply.setPerformative(ACLMessage.PROPOSE);
                            System.out.println("Broker " + name + " sent a PROPOSAL to Agency.");
                            reply.setContent(comission);
                        } else {
                            reply.setPerformative(ACLMessage.REFUSE);
                            System.out.println("Broker " + name + " sent a REFUSE to Agency.");
                        }

                        myAgent.send(reply);
                        step = 1;
                        break;

                    } else {
                        block();
                    }
                case 1:
                    //logger.info("OfferInsurance Step1 : waiting for agency response to proposal");
                    MessageTemplate mt = MessageTemplate.MatchConversationId(this.getAgent().getLocalName());
                    ACLMessage newMsg = myAgent.receive(mt);
                    if (newMsg != null) {
                        //logger.info(name + ": " + newMsg.getPerformative() + " + " + newMsg.getContent());
                        if (newMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            activeBroker = true;
                        }
                        step = 2;
                        break;
                    } else {
                        block();
                    }
            }
        }

        @Override
        public boolean done() {
            if (step == 2) {
                //logger.info("Broker " + name + " behaviour OfferInsurance is done.");
                return true;
            } else return false;
        }
    }

    private class DetailedInsurance extends Behaviour {

        @Override
        public void action() {
            //logger.info("Broker is ready");

            if (activeBroker) {
                System.out.println("Broker " + name + " is the active Broker.");

                MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("insurance-details"));
                ACLMessage clientMsg = myAgent.receive(template);

                if (clientMsg != null) {
                    String content = clientMsg.getContent();
                    //logger.info("Broker received Client Message: " + content);

                    client = clientMsg.getSender();

                    // Get price from rules engine
                    try{
                        priceInsurance = calculatePrice(
                                insuranceRequested,
                                content.split("\\|")[0],
                                content.split("\\|")[1],
                                content.split("\\|")[2]);
                    } catch (Exception e) {
                        System.out.println("Rules Engine price calculation failed: " + e.getMessage() + ". Set default value of 200.");
                        priceInsurance = 200;
                    }

                } else {
                    block();
                }
            }
        }

        @Override
        public boolean done() {
            //logger.info("DetailedInsurance: Broker " + name + " is done and priceInsurance: " + priceInsurance);
            return !activeBroker || priceInsurance != 0;
        }
    }

    private class SellInsurance extends Behaviour {

        private int step = 0;
        private boolean proposalTerminated;

        @Override
        public void action() {

            if (activeBroker) {
                switch (step) {
                    case 0:
                        System.out.println("Broker " + name + " sends first price proposal to client.");
                        ACLMessage negotiationMsg = new ACLMessage(ACLMessage.CFP);
                        negotiationMsg.setConversationId("round1-negotiation");
                        negotiationMsg.setContent(String.valueOf(priceInsurance));
                        System.out.println("Broker initial price offer: " + priceInsurance);
                        negotiationMsg.addReceiver(client);
                        myAgent.send(negotiationMsg);

                        step = 1;
                        break;
                    case 1:
                        MessageTemplate mt = MessageTemplate.MatchConversationId("round2-negotiation");
                        ACLMessage reply = myAgent.receive(mt);

                        if (reply != null) {
                            if (reply.getPerformative() == ACLMessage.PROPOSE && appliesDiscount && maxDiscount != 0) {
                                double renegotiatedPrice = Double.parseDouble(reply.getContent());
                                double maxDiscountedPrice = priceInsurance * (1 - (maxDiscount / 100.0));
                                if (renegotiatedPrice >= maxDiscountedPrice) {
                                    ACLMessage newReply = reply.createReply();
                                    newReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                    newReply.addReceiver(client);
                                    myAgent.send(newReply);
                                    System.out.println("Broker " + name + " accepts new price proposal from client.");
                                } else {
                                    ACLMessage newReply = reply.createReply();
                                    newReply.setPerformative(ACLMessage.PROPOSE);
                                    newReply.setContent(String.valueOf(maxDiscountedPrice));
                                    newReply.addReceiver(client);
                                    myAgent.send(newReply);
                                    System.out.println("Broker " + name + " proposes new price " + maxDiscountedPrice +  " to client.");
                                }
                                step = 2;
                            } else if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                                int numberOfInsurances = typeOfInsurance.get(insuranceRequested);
                                typeOfInsurance.put(insuranceRequested, numberOfInsurances + 1);
                                updateRating(Integer.parseInt(reply.getContent()));
                                proposalTerminated = true;
                                System.out.println("Broker " + name + " sold insurance to client.");
                            }
                        } else {
                            block();
                        }
                        break;
                    case 2:
                        MessageTemplate messageTemplate = MessageTemplate.MatchConversationId("close-deal");
                        ACLMessage finalReply = myAgent.receive(messageTemplate);

                        if (finalReply != null) {
                            if (finalReply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                                int numberOfInsurances = typeOfInsurance.get(insuranceRequested);
                                typeOfInsurance.put(insuranceRequested, numberOfInsurances + 1);
                                updateRating(Integer.parseInt(finalReply.getContent()));
                                System.out.println("Broker " + name + " sold insurance to client." );
                                proposalTerminated = true;
                            } else {
                                updateRating(Integer.parseInt(finalReply.getContent()));
                                System.out.println("Broker " + name + " did not sold insurance to client.");
                                proposalTerminated = true;
                            }
                        } else {
                            block();
                        }
                        break;
                }
            }
        }

        @Override
        public boolean done() {
            if(proposalTerminated)
                System.out.println("Broker " + name + " updated rating: " + rating);
            return proposalTerminated;
        }
    }


    private void updateRating(int newRating) {
        double sum = 0;
        ratingHistory.add(newRating);

        for (Integer rt : ratingHistory) {
            sum += rt;
        }
        double allRatings = ratingHistory.size();
        rating = sum / allRatings;
    }

    private int calculatePrice(String insuranceRequested, String firstCondition, String secondCondition, String thirdCondition) {

        int resultPrice = 0;
        EventsTrigger eventsTrigger = new EventsTriggerImpl();
        Object triggeredRules;

        switch (insuranceRequested) {
            case "Vehicle":
                Vehicle vehicle = new Vehicle(
                        secondCondition.equals("Car"),
                        secondCondition.equals("Truck"),
                        secondCondition.equals("Moto"),
                        Integer.parseInt(firstCondition) < 25,
                        Integer.parseInt(firstCondition) > 25 && Integer.parseInt(firstCondition) < 60,
                        Integer.parseInt(firstCondition) > 60,
                        Integer.parseInt(thirdCondition) < 2000,
                        Integer.parseInt(thirdCondition) >= 2000,
                        0);

                triggeredRules = eventsTrigger.getTriggeredEvents(vehicle, insuranceRequested.toLowerCase());

                resultPrice = ((Vehicle) triggeredRules).getPrice();

                break;
            case "Health":
                Health health = new Health(
                        secondCondition.equals("High Risk"),
                        secondCondition.equals("Medium Risk"),
                        secondCondition.equals("Low Risk"),
                        Integer.parseInt(firstCondition) < 25,
                        Integer.parseInt(firstCondition) > 25 && Integer.parseInt(firstCondition) < 60,
                        Integer.parseInt(firstCondition) > 60,
                        Boolean.parseBoolean(thirdCondition),
                        !Boolean.parseBoolean(thirdCondition),
                        0);

                triggeredRules = eventsTrigger.getTriggeredEvents(health, insuranceRequested.toLowerCase());

                resultPrice = ((Health) triggeredRules).getPrice();
                break;
            case "House":
                House house = new House(
                        secondCondition.equals("House"),
                        secondCondition.equals("Apartment"),
                        secondCondition.equals("Farm"),
                        Integer.parseInt(firstCondition) < 25,
                        Integer.parseInt(firstCondition) > 25 && Integer.parseInt(firstCondition) < 60,
                        Integer.parseInt(firstCondition) > 60,
                        thirdCondition.equals("Rural"),
                        thirdCondition.equals("Urban"),
                        0);

                triggeredRules = eventsTrigger.getTriggeredEvents(house, insuranceRequested.toLowerCase());

                resultPrice = ((House) triggeredRules).getPrice();
                break;
        }

        //logger.info(String.valueOf(resultPrice));
        return resultPrice;
    }
}
