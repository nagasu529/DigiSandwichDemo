package agent;

import database.DatabaseConn;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class customerAgent extends Agent {
    //customerUI myGui;

    // Put agent initializations here
    ArrayList<customerInfo> customerInfo = new ArrayList<>();
    DatabaseConn app = new DatabaseConn();
    DecimalFormat df = new DecimalFormat("#.##");

    //calcMethod.customerInfo randInput = customerInfo.randCustomerInput(getLocalName());
    int orderTimer = 10000;
    int timePeriod = 0;
    int weekCount = 0;
    int initOrder = 0;
    //int[] orderTimerArray = {20000,60000,180000,300000,4200000};

    //calcMethod.customerInfo randInput = customerInfo.customerInfo(getLocalName(), " ", " ", 0, 0, 0, "  ", 0);

    protected void setup() {
        //Initialize
        //customerInfo.add(getLocalName(),"HamSandwich","general",100,app.selectProductPrice("HamSandwich","general"),0,0,0);
        customerInfo.add(new customerInfo(getLocalName(),"HamSandwich","general",100, app.selectProductPrice("HamSandwich","general"),0,0,0));

    	// Register service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("customer");
        sd.setName(getAID().getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    	
        //put agent name to ArrayList
        //randInput.agentName = getLocalName();
        //Timing for agent environment
        //orderTimer = orderTimerArray[customerInfo.getRandIntRange(0, orderTimerArray.length - 1)];
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //GUI active
        //myGui = new customerUI(this);
        //myGui.show();

        //myGui.displayUI(getLocalName());
        //System.out.println(randInput.toUpdateService());
        // Printout a welcome message

        //System.out.println("Hello! Customer-agent " + getAID().getName() + " is ready.");
        // Customer information detail

        //addBehaviour(new customerAgent.ReceivedOrderRequest());

        addBehaviour(new TickerBehaviour(this, orderTimer){
            protected void onTick() {
                initOrder = customerInfo.get(0).numOfOrder;
                customerInfo.get(0).numOfOrder = timePeriodShift(2, initOrder,10);
                initOrder = customerInfo.get(0).numOfOrder;
                //orderTimer = orderTimerArray[customerInfo.getRandIntRange(0, orderTimerArray.length - 1)];
                //update current stock on list to suppliers.
                //addBehaviour(new responseToCustomers());
                //int dayCountIncomming = orderTimer/20000;
                timePeriod = timePeriod + 1;
                if(timePeriod == 7){
                    weekCount = weekCount + 1;
                    System.out.println("weekly" + weekCount);
                    //customerInfo.get(0).numOfOrder = timePeriodShift(2, initOrder,10);
                    //initOrder = customerInfo.get(0).numOfOrder;
                    timePeriod = 0;
                }
                addBehaviour(new customerAgent.RequestPerformer());
            }
        } );

    }

    // Put agent to request sandwich here
    protected void takeDown() {
        // Printout a dismissal message
        //myGui.dispose();
        //System.out.println("Customer-agent "+getAID().getName()+" terminating.");
    }

    private class RequestPerformer extends OneShotBehaviour{
        private AID[] specialistAgent; //The specialist location contain
        public void action() {
            //Searching for current specialist.
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sdSearch = new ServiceDescription();
            sdSearch.setType("specialist");
            template.addServices(sdSearch);
            try {
                DFAgentDescription[] searchResult = DFService.search(myAgent, template);
                specialistAgent = new AID[searchResult.length];
                for (int i = 0; i < searchResult.length; ++i) {
                    specialistAgent[i] = searchResult[i].getName();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (int j = 0; j < specialistAgent.length; j++) {
                cfp.addReceiver(specialistAgent[j]);
            }
            cfp.setContent(customerInfo.get(0).toUpdateService());
            cfp.setConversationId("customer");
            cfp.setReplyWith("cfp" + System.currentTimeMillis());
            myAgent.send(cfp);
            //System.out.println(cfp);

            //myGui.displayUI("Sending request to buy message to specialist:");
            //myGui.displayUI("Request order: " + randInput.toStringOutput());

        }
    }

    private class ReceivedOrderRequest extends CyclicBehaviour {
        public void action() {
            MessageTemplate mtAccept = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            MessageTemplate mtReject = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);

            ACLMessage accMsg = myAgent.receive(mtAccept);
            ACLMessage rejMsg = myAgent.receive(mtReject);

            if (accMsg != null && accMsg.getConversationId().equals("reply-to-customer")) {
                //myGui.displayUI(accMsg.toString());
                String tempContent = accMsg.getContent();
                String[] arrOfStr = tempContent.split("-");
                int numReplyFromSpeciailst = Integer.parseInt(arrOfStr[4]);
                int replyStatus = Integer.parseInt(arrOfStr[5]);
                customerInfo.get(0).numReply = numReplyFromSpeciailst;
                customerInfo.get(0).orderStatus = replyStatus;

                //Date and time testing.
                LocalDate A = java.time.LocalDate.now();
                LocalDate expired = java.time.LocalDate.now().plusDays(21);
                long p = ChronoUnit.DAYS.between(A,expired);
                int a  = Integer.parseInt(String.valueOf(p));

                Period period = Period.between(A, expired);
                //myGui.displayUI(period.toString());
                //myGui.displayUI(expired.toString());

                //Updating agent status (dispose or re-send request)
                if(customerInfo.get(0).numReply == customerInfo.get(0).numOfOrder){
                    //myGui.displayUI("Received all order requirement");
                    //myAgent.doSuspend();

                }else {
                    //myGui.displayUI(String.format("\n The reserved order is %d from %d request",randInput.numReply,randInput.numOfOrder));
                    customerInfo.get(0).numOfOrder = customerInfo.get(0).numOfOrder - customerInfo.get(0).numReply;
                    //myAgent.doSuspend();
                    //myGui.displayUI(randInput.toStringOutput());
                }
            }
            if(rejMsg != null && rejMsg.getConversationId().equals("reply-to-customer")){
                //myGui.displayUI("The request order cannot accepted today.... please try again tomorrow");
                //myAgent.doSuspend();
            }
            else {
                block();
            }
        }
    }

    public class customerInfo{
        public String agentName;
        public String orderName;
        public String ingredientGrade;
        public int numOfOrder;
        public double pricePerUnit;
        public int numReply;
        public int orderStatus;
        public double utilityValue;

        public customerInfo(String agentName, String orderName, String ingredientGrade, int numOfOrder, double pricePerUnit,int numReply, int orderStatus, double utilityValue) {
            this.agentName = agentName;
            this.orderName = orderName;
            this.ingredientGrade = ingredientGrade;
            this.numOfOrder = numOfOrder;
            this.pricePerUnit = pricePerUnit;
            this.numReply   = numReply;
            this.orderStatus = orderStatus;
            this.utilityValue = utilityValue;
        }

        public String toStringOutput() {
            String status;
            if(this.orderStatus == 0) {
                status = "Waiting queue";
            }else if(this.orderStatus == 1){
                status = "Process";
            }else if(this.orderStatus == 2){
                status = "process some order";
            }
            else{
                status = "Done";
            }
            return "Agent name: " + this.agentName + " Order name: " + this.orderName + "   Quality: " + this.ingredientGrade +
                    "   Order requested: " + this.numOfOrder + "  Price per unit: " +  this.pricePerUnit  + "  Order replied: " + this.numReply + "  Order status: " + status + "  Utility value: " + df.format(utilityValue);
        }
        public String toUpdateService(){

            return this.orderName + "-" + this.ingredientGrade + "-" + this.numOfOrder + "-" + df.format(this.pricePerUnit) + "-" + this.numReply + "-" + this.orderStatus + "-" + df.format(this.utilityValue);
        }
    }
    private int timePeriodShift(int shiftStatus, int initialUnit, int shiftUnit){
        int unitPerWeek = 0;
        if(shiftStatus == 0 & shiftUnit != 0){
            System.out.println("Shift up status");
             unitPerWeek = initialUnit + shiftUnit;
        }else if((shiftStatus == 0 || shiftStatus == 1) & (shiftUnit == 0)){
            System.out.println("Stable frequency status");
            unitPerWeek = initialUnit + shiftUnit;
            if(unitPerWeek >= 160){
                unitPerWeek = 100;
            }
        } else {
            System.out.println("Shift down");
            unitPerWeek = initialUnit - shiftUnit;
            if(unitPerWeek <=40){
                unitPerWeek = 100;
            }
        }
        return unitPerWeek;
    }
}