import javax.swing.*;        

public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] costs = new int[RouterSimulator.NUM_NODES];
    private int[] route = new int[RouterSimulator.NUM_NODES];
    private int[][] allCosts = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];

    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator sim, int[] costs) {
	myID = ID;
	this.sim = sim;
	myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

	System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
	printDistanceTable();
	
	allCosts[myID] = costs;
	
	for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
	    route[i] = i;

	for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
	    if ((i != myID) && (i != 999)) {
		RouterPacket pkt = new RouterPacket(myID, i, costs);
		sendUpdate(pkt);
	    }
	}
    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {
	allCosts[pkt.sourceid] = pkt.mincost;
	printDistanceTable();
    }
  

    //--------------------------------------------------
    private void sendUpdate(RouterPacket pkt) {
	sim.toLayer2(pkt);

    }
    

    //--------------------------------------------------
    public void printDistanceTable() {
	F f = new F();
	myGUI.println("Current table for " + myID +
		      "  at time " + sim.getClocktime());
	myGUI.println();
	myGUI.println("Neighbors costs");
	myGUI.println("-----------------------------");
	myGUI.println("    dst |   0    1    2    3");
	myGUI.println("-----------------------------");
	for(int i=0; i<RouterSimulator.NUM_NODES; i++) {
	    if((costs[i] != 0) && (costs[i] != 999)) {
		myGUI.print("        "+i);
		for(int j=0; j<RouterSimulator.NUM_NODES; j++) {
		    myGUI.print(f.format(allCosts[i][j], 5));
		}
		myGUI.println();
	    }
	}
	myGUI.println("Distance vector and routes");
	myGUI.println("-----------------------------");
	myGUI.println(" dst   |    0    1    2    3");
	myGUI.print  (" Costs");
	for(int i=0; i<RouterSimulator.NUM_NODES; i++)
	    myGUI.print(f.format(allCosts[myID][i], 5));
	myGUI.println();
	myGUI.print  (" Route");
	for(int i=0; i<RouterSimulator.NUM_NODES; i++)
	    myGUI.print(f.format(route[i], 5));
	myGUI.println();
    }

    //--------------------------------------------------
    public void updateLinkCost(int dest, int newcost, int route) {
	
    }
    
    public int[] min(int[] list) {
	int[] low = new int[2];
	low[0] = 999;
	low[1] = 999;
	for(int i=0; i<RouterSimulator.NUM_NODES; i++)
	    if(list[i] < low[0]) {
		low[0] = list[i];
		low[1] = i;
	    }
	return low;
    }
    
    public int calculateCost(int dest) {
	int[] list = new int[RouterSimulator.NUM_NODES];
	for(int i=0; i<RouterSimulator.NUM_NODES; i++)
	    if(i != myID)
		list[i] = allCosts[myID][i] + allCosts[i][dest];
	    else
		list[i] = allCosts[i][dest];
	if(min(list)[0] <= allCosts[i][dest])
	    updateLinkCost(dest, min(list)[0], min(list)[1]);
}
