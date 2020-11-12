package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


import java.util.Date;
import java.util.LinkedList;


public class NodeList {

  public String label;
  public LinkedList<NodeList> eventList;
  public int status;
  public Date tstamp;
  public int count;
  
  /*
   0 Attempt
   1 Success
   2 Failure
   3 Already present
   4 Already present and delivered   
   
    */
   
  public NodeList(String l, Date t, int s){
    label = l;
    eventList = new LinkedList<NodeList>();
    status =s;
    tstamp = new Date();
    tstamp.setTime(t.getTime());
  }

  
  public void addEdge(NodeList n ){
    int i=0;
    for(;i<eventList.size();i++){
      NodeList ntemp = eventList.get(i);
      if(ntemp.label.equals(n.label) & ntemp.status==n.status){
        if(ntemp.status==2)
          ntemp.count++;
        break;
      }
    }
    if(i>=eventList.size()){
      eventList.add(n);
      n.count=1;
    }
  }
  public void setStaus(NodeList n){
    
    if(n.status==3 | n.status==1|n.status==2 )
    {
      for(int i=eventList.size()-1;i>-1;i--){
        NodeList ntemp = eventList.get(i);
        if(ntemp.label.equals(n.label) & ntemp.status==0)
        {
          eventList.remove(i);
          addEdge(n);
          return;
        }
      }
    }
      
    if(n.status==4){
      for(int i=eventList.size()-1;i>-1;i--){
        NodeList ntemp = eventList.get(i);
        if(ntemp.label.equals(n.label) & ntemp.status==3)
        {
          ntemp.count--;
          if(ntemp.count==0)
            eventList.remove(i);
          //addEdge(n);
          return;
        }
      }
    }
  }
}
