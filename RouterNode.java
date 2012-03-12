import javax.swing.*;        

public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] costs = new int[RouterSimulator.NUM_NODES];
    private int[] route = new int[RouterSimulator.NUM_NODES];
    private int[][] allCosts = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
    private int[] sendList = new int[RouterSimulator.NUM_NODES];
    private boolean poison = true;

    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator sim, int[] costs) {
	myID = ID;
	this.sim = sim;
	myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

	System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
	
	for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
	    for(int j = 0; j < RouterSimulator.NUM_NODES; j++)
		allCosts[i][j] = 999;

	allCosts[myID] = costs;
	
	for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
	    route[i] = i;

	for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
	    if ((i != myID) && (i != 999)) {
		RouterPacket pkt = new RouterPacket(myID, i, costs);
		sendUpdate(pkt);
	    }
	}
	printDistanceTable();
    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {
	allCosts[pkt.sourceid] = pkt.mincost;
	for(int i = 0; i < RouterSimulator.NUM_NODES; i++)
	    calculateCost(i);
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
    public void updateLinkCost(int dest, int newcost) {
	costs[dest] = newcost;
	allCosts[myID][dest] = newcost;
	for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
	    RouterPacket pkt = new RouterPacket(myID, i, allCosts[myID]);
	    sendUpdate(pkt);
	}
	    
    }
    
    public int[] min(int[] list) {
	int[] low = {999, 999};
	//low[0] = 9999;
	//low[1] = 9999;
	for(int i=0; i<RouterSimulator.NUM_NODES; i++)
	    if(list[i] < low[0]) {
		low[0] = list[i];
		low[1] = i;
	    }
	//for(int i=0; i<RouterSimulator.NUM_NODES; i++)
	//    myGUI.println("Cost for "+i+" = "+list[i]);
       
	return low;
    }
    
    public void calculateCost(int dest) {
	if (dest == myID)
	    return;
	int[] list = new int[RouterSimulator.NUM_NODES];
	for(int i=0; i<RouterSimulator.NUM_NODES; i++) {
	    if(i == myID)
		list[i] = 999;
	    else
		list[i] = costs[i] + allCosts[i][dest];
	}
	myGUI.println("Dest: "+dest);
	if(min(list)[0] != allCosts[myID][dest]) {
	    allCosts[myID][dest] = min(list)[0];
	    route[dest] = min(list)[1];
	    for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
		if ((i != myID) && (i != 999)) {
		    if(poison == true) {
			for(int j = 0; j < RouterSimulator.NUM_NODES; j++) {
			    if((route[j] == i) && (j != i)) {
				sendList[j] = RouterSimulator.INFINITY;
			    }
			    else {
				sendList[j] = allCosts[myID][j];
			    }
			}
		    }
		    else {
			sendList = allCosts[myID];
		    }
		    RouterPacket pkt = new RouterPacket(myID, i, sendList);
		    sendUpdate(pkt);
		}
	    }
	    }
    }
}