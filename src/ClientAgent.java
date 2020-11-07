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

public class ClientAgent extends Agent {
    private String name;
    private String typeOfInsurance;
    private AID[] agency;
    private AID bestBroker;

    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected void setup() {

        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            name = (String) args[0];
            typeOfInsurance = (String) args[1];

            System.out.println("Hello! I'm " + name + " and I need a " + typeOfInsurance + "insurance, please.");

            addBehaviour(new AgencyDiscovery());
            addBehaviour(new RequestInsurance());
        }
    }

    private class AgencyDiscovery extends Behaviour {

        @Override
        public void action() {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("client-agency");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                agency = new AID[result.length];

                for (int i = 0; i < result.length; ++i) {
                    agency[i] = result[i].getName();
                    logger.log(Level.INFO,"Agency agent: " + result[i].getName());                }
            } catch (FIPAException fe) {
                //agency
                logger.log(Level.SEVERE, "No agency found.");

                fe.printStackTrace();
            }

        }

        @Override
        public boolean done() {
            logger.log(Level.INFO, "Behaviour AgencyDiscovery done.");
            return true;
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
                    } else {
                        block();
                    }
                    break;

            }

        }

        @Override
        public boolean done() {
            return bestBroker != null;
        }
    }

}
