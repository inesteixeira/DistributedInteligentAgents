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
                    logger.log(Level.INFO,"Agency agent: " + result[i].getName());                }
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
                    for (int i = 0; i < agency.length; ++i) {
                        cfp.addReceiver(agency[i]);
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
                        bestBroker = reply.getAllReplyTo().hasNext() ? (AID) reply.getAllReplyTo().next() : null;
                        logger.log(Level.INFO, "Broker " + bestBroker.getName() + " is available.");
                        step=2;
                    } else {
                        block();
                    }
                    break;

            }

        }

        @Override
        public boolean done() {
            return step==2 && bestBroker!= null;
        }
    }

    private class InformInsurance extends Behaviour{

        @Override
        public void action() {
            logger.log(Level.INFO, "Client sends info to broker.");
            ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
            informMsg.addReceiver(bestBroker);
            informMsg.setContent(firstCondition  + "|" + secondCondition + "|" + thirdCondition);
            myAgent.send(informMsg);
        }

        @Override
        public boolean done() {

            logger.log(Level.INFO, "Client is ready to negotiate insurance.");
            return true;
        }
    }

    private class BuyInsurance extends Behaviour{



        @Override
        public void action() {
            ACLMessage brokerMsg = myAgent.receive();

            if(brokerMsg != null){
                ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                reply.setContent("7");
                reply.addReceiver(bestBroker);
                myAgent.send(reply);
                logger.log(Level.INFO, "Client accepts message");
            }
            else{
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }


}
