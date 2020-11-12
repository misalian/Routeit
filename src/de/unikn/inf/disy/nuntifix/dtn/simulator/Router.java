
package de.unikn.inf.disy.nuntifix.dtn.simulator;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Calendar;
import java.util.Enumeration;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;


import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.unikn.inf.disy.nuntifix.dtn.simulator.graph.NGraph;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.CentralityRouting;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.DirectDelivery;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.EBEC;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.FirstContact;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.Flood;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.HistoryMaxFlow;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.MV;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.MaxProp;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.OnDemand;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.Oracles;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.Nile;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.NileAdaptive;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.NileX;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.Prophet;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.SimpleReplication;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.Stats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms.CognitiveDFlood;
//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.BloomValue;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Apoint;
//import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.DVectorUnit;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.History;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.NodeList;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;


public class Router {

  public Hashtable<String, Device> Peerstable;

  // peripheral data holders for indexing of accesspoints, indexing of devices
  // and how many nodes from one access point to another respectivel

  LinkedList<String> tabapList;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Hashtable<String, Apoint> gApTable;

  public File fFile;

  int fcount;// //file merge count

  long flength; // file lenght to help in readin

  // period of mins on which time depost communication is computed
  int deptime;

  int ap_count;

  int rfact;

  int kfact;
  int pathcount;

  String run;

  String trace;

  String seq;
  
  int susers;

  double bandwidth;

  String bw;

  int msgcount;

  int nodecount;

  boolean resume;
  boolean path;

  boolean que;

  public boolean enc;
  
  String []color;
  
  //public LinkedList<NodeList>eventList;
  public NodeList nList;
  public int msgtrack ;
  
  public Date initialTime;
  public Date finalTime;
  
  
  public static void main(String ar[]) {
    if (ar.length < 2) {
      System.out
          .println("Usage Sim Trace Algo [bw=low] [run=1] [repl=1] [eras=1]");
      System.out.println("Usage Create Trace [msg=100] [nodecount=70]");
      System.exit(0);
    }
    Router r = new Router(ar[1]);

    if (ar[0].equalsIgnoreCase("SIM")) {
      for (int i = 3; i < ar.length; i++) {
        if (ar[i].startsWith("bw")) {
          if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
              .equalsIgnoreCase("Med")) {
            r.bandwidth *= 10;
            r.bw = "Med";
          }
          if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
              .equalsIgnoreCase("High")) {
            r.bandwidth *= 100;
            r.bw = "High";
          }
        }

        if (ar[i].startsWith("res"))
          if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
              .equalsIgnoreCase("True"))
            r.resume = true;
        if (ar[i].startsWith("path"))
          if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
              .equalsIgnoreCase("True"))
            r.path = true;
        if (ar[i].startsWith("seq"))
          r.seq = new String(ar[i].substring(ar[i].indexOf("=") + 1, ar[i]
              .length()));

        if (ar[i].startsWith("susers"))
          r.susers= Integer.parseInt(new String(ar[i].substring(ar[i].indexOf("=") + 1, ar[i]
              .length())));
        
        if (ar[i].startsWith("que"))
          if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
              .equalsIgnoreCase("True"))
            r.que = true;

        if (ar[1].startsWith("MIT"))
          if (ar[i].startsWith("run"))
            r.run = new String(ar[i].substring(ar[i].indexOf("=") + 1, ar[i]
                .length()));
        if(ar[2].equalsIgnoreCase("Nile")){
          if (ar[i].startsWith("kfact"))
            r.kfact = Integer.parseInt(ar[i].substring(
                ar[i].indexOf("=") + 1, ar[i].length()));
          if (ar[i].startsWith("enc"))
            if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
                .equalsIgnoreCase("True"))
              r.enc=true;
        }
        if(ar[2].equalsIgnoreCase("NileX")){
          if (ar[i].startsWith("kfact"))
            r.kfact = Integer.parseInt(ar[i].substring(
                ar[i].indexOf("=") + 1, ar[i].length()));
          if (ar[i].startsWith("rfact"))
            r.rfact = Integer.parseInt(ar[i].substring(
                ar[i].indexOf("=") + 1, ar[i].length()));
          if (ar[i].startsWith("enc"))
            if (ar[i].substring(ar[i].indexOf("=") + 1, ar[i].length())
                .equalsIgnoreCase("True"))
              r.enc=true;
        }
        if (ar[2].equals("HSR") | ar[2].equals("SR") | ar[2].equals("EBEC") | ar[2].equals("HBEC") | ar[2].equals("MsgStat") |ar[2].equals("OracleMsgStat")  ) {
          if (ar[i].startsWith("rfact"))
            r.rfact = Integer.parseInt(ar[i].substring(ar[i].indexOf("=") + 1,
                ar[i].length()));
          if (ar[2].equals("EBEC") | ar[2].equals("HBEC")| ar[2].equals("MsgStat")||ar[2].equals("OracleMsgStat") )
            if (ar[i].startsWith("kfact"))
              r.kfact = Integer.parseInt(ar[i].substring(
                  ar[i].indexOf("=") + 1, ar[i].length()));
          if (ar[2].equals("OracleMsgStat") | ar[2].equals("MsgStat") )
            if (ar[i].startsWith("pCount"))
              r.pathcount = Integer.parseInt(ar[i].substring(
                  ar[i].indexOf("=") + 1, ar[i].length()));
          if (r.rfact < 0 | r.kfact < 0) {
            System.out.println("Invalid input for repl and eras. Resetting the values to 1");
            r.rfact = r.kfact = 1;
          }
        }
      }

      //r.newscan();
      //r.loadRAssign();
      ////if(ar[1].startsWith("MITBT"))
        //r.ap_count = r.tabapList.size();
      ////else
       //// r.ap_count = 0;
         
      //r.AddDepFunc();
      r.simulatealgo(ar[2]);
    } else if (ar[0].equalsIgnoreCase("Create")) {
      for (int i = 2; i < ar.length; i++) {
        if (ar[i].startsWith("msg"))
          r.msgcount = Integer.parseInt(ar[i].substring(ar[i].indexOf("=") + 1,
              ar[i].length()));
        if (ar[i].startsWith("node"))
          r.nodecount = Integer.parseInt(ar[i].substring(
              ar[i].indexOf("=") + 1, ar[i].length()));
      }
      r.newscan();
      r.createMsg();
    }else if (ar[0].equalsIgnoreCase("Misc")) {
      r.simulatealgo(ar[2]);     
    }
  }

  public void simulatealgo(String algo) {
    if(trace.equals("MITBT"))
      run = new String("2"+run);
    bw = run + bw;
    if (resume)
      bw = bw + "Res";
    if (que)
      bw = bw + "Que";
    //if(ap_count==0)
      //bw = bw+ "Mule";
    if(path)
      bw = bw+"Path";
    //bw = bw + "s_"+Integer.toString(susers);
    //if(!seq.equals("0"))
      //bw=seq+bw;
    if (algo.equalsIgnoreCase("ConvSQL")) {
      System.out.println("SQl Conv");
      /*convertSQL();
      calcnodeaptimedist(); 
      writeresultxml();*/
      //newscan();
      calcnodeaptimedist(); 
      writeresultxml();
    }
    if (algo.equalsIgnoreCase("CheckBU")) {
      System.out.println(bw + " " + "CheckBu");
      newscan();
      readproxyDatatrace();
      ap_count = tabapList.size();
      System.out.println(tabdevList.size()+" "+tabapList.size());   
      NGraph n = new NGraph(Peerstable,tabdevList,tabapList);
     // n.Centrality(trace,1);
      //n.Centrality(trace,2);
      //n.Centrality(trace,3);
      //n.Centrality(trace,4);

     // n.Betweenusefulness(trace, bw,1);
      //n.Betweenusefulness(trace, bw,2);
      //n.Betweenusefulness(trace, bw,3);
     // n.Betweenusefulness(trace, bw,4);
      
      n.Centrality(trace,0);
      //n.Betweenusefulness(trace, bw,0);     

    }
    if(algo.equalsIgnoreCase("traceStat")){
      newscan();
      System.out.println("traceStat");
      ap_count = tabapList.size();
      readDatatrace();
      AddDepFunc();
      //History h = new History(Peerstable,tabdevList,tabapList,get_initialtime());
      //h.GenHistory(ap_count, bandwidth, trace);
      //h.writentnewaitingtime(ap_count, bandwidth, trace);
      for(long week=1;week<=1;week++){
        calc_peers(new Date((long)(initialTime.getTime()+(long)(week*7*24*60*60*1000))),week);

        //Date d=new Date((long)(initialTime.getTime()+(long)(week*7*24*60*60*1000)));
       //System.out.println(d.toString()+"\t"+initialTime.toString());
        
      }
    }
    if (algo.equalsIgnoreCase("MeetDist")) {
      System.out.println(bw +  " MeetDist");
      readDatatrace();
      History h = new History(Peerstable,tabdevList,tabapList,get_initialtime());
      h.GenHistory(ap_count, bandwidth, trace);

    }
    if (algo.equalsIgnoreCase("GraphStat")) {
      System.out.println(bw + " " + "GraphStat");
      newscan();
      //readproxyDatatrace();
      readDatatrace();
      ap_count = tabapList.size();
      System.out.println(tabdevList.size()+" "+tabapList.size());
      NGraph n = new NGraph(Peerstable,tabdevList,tabapList);
      for(int i=0;i<=0;i++)
      {  
        //initialTime.setTime(initialTime.getTime()+(7*24*60*60*1000));
        n.writegraph(trace, 0,finalTime,i);
      }
  
    }
    if (algo.equalsIgnoreCase("OnDemand")) {
      System.out.println(bw + " " + "OnDemand");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      OnDemand od = new OnDemand(Peerstable, tabdevList, gMsgTable, gApTable);
      od.setParameters( bandwidth,ap_count, resume, que);
      od.OD_initial(get_initialtime(),trace, bw);
      write_replresults(bw + "OnDemand");  
      write_Msgresults(bw + "OnDemand");

    }
    if (algo.equalsIgnoreCase("FLOOD")) {  
      System.out.println(bw + " " + "Flooding");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      verify();
      loadRAssign();
      AddDepFunc();
      Flood f = new Flood(Peerstable, tabdevList, gMsgTable, gApTable);
      f.setParameter(ap_count, susers,bandwidth, resume, que);
      f.flood_initial(initialTime,trace, seq+bw);
      write_replresults(bw + Integer.toString(susers)+"Flooding"); 
      write_Msgresults(bw + Integer.toString(susers)+"Flooding");
    }
    if (algo.equalsIgnoreCase("CentralityRoute")) {     
    System.out.println(bw + " " + "All centrality ");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      System.out.println(bw + " " + "All centrality "+tabdevList.size());

      AddDepFunc();
      System.out.println(bw + " " + "All centrality "+tabdevList.size());

      CentralityRouting c1 = new CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c1.setParameters(ap_count, susers,bandwidth, resume, que);
      c1.centrality_initial(initialTime, trace, bw,"G01betweeness",0);
      write_replresults(bw + "G01betweeness"); 
      write_Msgresults(bw + "G01betweeness");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c2 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c2.setParameters(ap_count, susers,bandwidth, resume, que);
      c2.centrality_initial(initialTime, trace, bw,"GMeetingCntbetweeness",1);
      write_replresults(bw + "GMeetingCntbetweeness"); 
      write_Msgresults(bw + "GMeetingCntbetweeness");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c3 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c3.setParameters(ap_count, susers,bandwidth, resume, que);
      c3.centrality_initial(initialTime, trace, bw,"GWaitTimeDurabetw",2);
      write_replresults(bw + "GWaitTimeDurabetw"); 
      write_Msgresults(bw + "GWaitTimeDurabetw");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c4 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c4.setParameters(ap_count, susers,bandwidth, resume, que);
      c4.centrality_initial(initialTime, trace, bw,"GWaitTimeMeetCntbetw",3);
      write_replresults(bw + "GWaitTimeMeetCntbetw"); 
      write_Msgresults(bw + "GWaitTimeMeetCntbetw");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c5 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c5.setParameters(ap_count, susers,bandwidth, resume, que);
      c5.centrality_initial(initialTime, trace, bw,"GDurationbetw",4);
      write_replresults(bw + "GDurationbetw"); 
      write_Msgresults(bw + "GDurationbetw");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c6 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c6.setParameters(ap_count, susers,bandwidth, resume, que);
      c6.centrality_initial(initialTime, trace, bw,"GWaitDuraMeetCntbetw",5);
      write_replresults(bw + "GWaitDuraMeetCntbetw"); 
      write_Msgresults(bw + "GWaitDuraMeetCntbetw");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c7 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c7.setParameters(ap_count, susers,bandwidth, resume, que);
      c7.centrality_initial(initialTime, trace, bw,"GWaitTimebetw",6);
      write_replresults(bw + "GWaitTimebetw"); 
      write_Msgresults(bw + "GWaitTimebetw");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c8 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c8.setParameters(ap_count, susers,bandwidth, resume, que);
      c8.centrality_initial(initialTime, trace, bw,"G01Degree",7);
      write_replresults(bw + "G01Degree"); 
      write_Msgresults(bw + "G01Degree");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c9 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c9.setParameters(ap_count, susers,bandwidth, resume, que);
      c9.centrality_initial(initialTime, trace, bw,"GMeetCntCenClose",8);
      write_replresults(bw + "GMeetCntCenClose"); 
      write_Msgresults(bw + "GMeetCntCenClose");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c10 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c10.setParameters(ap_count, susers,bandwidth, resume, que);
      c10.centrality_initial(initialTime, trace, bw,"GWaitTimeDuraClose",9);
      write_replresults(bw + "GWaitTimeDuraClose"); 
      write_Msgresults(bw + "GWaitTimeDuraClose");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c11 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c11.setParameters(ap_count, susers,bandwidth, resume, que);
      c11.centrality_initial(initialTime, trace, bw,"GWaitTimeClose",10);
      write_replresults(bw + "GWaitTimeClose"); 
      write_Msgresults(bw + "GWaitTimeClose");
      
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      CentralityRouting c12 = new  CentralityRouting(Peerstable, tabdevList, gMsgTable,gApTable);
      c12.setParameters(ap_count, susers,bandwidth, resume, que);
      c12.centrality_initial(initialTime, trace, bw,"G01Close",11);
      write_replresults(bw + "G01Close"); 
      write_Msgresults(bw + "G01Close");
    }
    if (algo.equalsIgnoreCase("DIRECT")) {

      System.out.println(bw + " " + "DirectDelivery");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      DirectDelivery d = new DirectDelivery(Peerstable, tabdevList, gMsgTable);
      d.setParameters(ap_count,susers, bandwidth, resume, que);
      d.directdel_initial(initialTime, trace, bw);
      write_results(bw + "DirectDelivery");
      write_Msgresults(bw + "DirectDelivery");
    }

    if (algo.equalsIgnoreCase("MV")) {
      System.out.println(bw + " " + "MV");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();

      MV m = new MV(Peerstable, tabdevList, tabapList, gMsgTable);
      m.setParameters(ap_count, bandwidth, resume, que);
      m.MV_Initial(initialTime);
      write_replresults(bw + "MobileVehicle");
      write_Msgresults(bw + "MobileVehicle");
    }
    if (algo.equalsIgnoreCase("STBRP")) {
      System.out.println(bw + " " + "STBRPerfect");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      Oracles t = new Oracles(Peerstable, tabdevList, gMsgTable);
      t.setParameters(ap_count, bandwidth, false, true, true, resume, que);
      t.STBRInitial(trace,initialTime,bw);
      write_results(bw + "STBRPerfect");
      write_Msgresults(bw + "STBRPerfect");
      writeGraphml(bw+"STBRPGraph");
    }
    if (algo.equalsIgnoreCase("STBR")) {
      System.out.println(bw + " " + "STBR");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      Oracles t = new Oracles(Peerstable, tabdevList, gMsgTable);
      t.setParameters(ap_count, bandwidth, false, true, false, resume, que);
      t.STBRInitial(trace,initialTime,bw);
      write_results(bw + "STBR");
      write_Msgresults(bw + "STBR");
    }
    if (algo.equalsIgnoreCase("EPO")) {
      System.out.println(bw + " " + "EPO");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      Oracles t = new Oracles(Peerstable, tabdevList, gMsgTable);
      t.setParameters(ap_count, bandwidth, false, false, true, resume, que);
      t.EPOInitial(trace,initialTime, bw);
      write_results(bw + "EPO");
      write_Msgresults(bw + "EPO");
      writeGraphml(bw+"EPOGraph");
    }
    if (algo.equalsIgnoreCase("EPOX")) {
      System.out.println(bw + " " + "EPOX");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      Oracles t = new Oracles(Peerstable, tabdevList, gMsgTable);
      t.setParameters(ap_count, bandwidth, true, false, true, true, que);
      t.EPOInitial(trace,initialTime,bw);
      write_results(bw + "EPOX");
      write_Msgresults(bw + "EPOX");
      writeGraphml(bw+"EPOXGraph");
    }

    if (algo.equalsIgnoreCase("MAXPROP")) {
      System.out.println(bw + " " + "MaxProp");
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      MaxProp m = new MaxProp(Peerstable, tabdevList, gMsgTable);
      m.setParameters(ap_count, bandwidth, resume, que);
      m.Maxprop_Initial(initialTime,trace,bw);
      write_results(bw + "MaxProp");
      write_Msgresults(bw + "MaxProp");
      writeGraphml(bw+ "MaxPropGraph");
    }
    if (algo.equalsIgnoreCase("NileX")) {
      System.out.println(bw + " " + "Nile frag="+kfact+" rfact="+ rfact+ " encoding="+enc); 
      newscan();
      ap_count = tabapList.size();
      readDatatrace();
      loadRAssign();
      NileX m = new NileX(Peerstable, tabdevList, gMsgTable, gApTable,enc,kfact,rfact);
      m.setParameters(ap_count, bandwidth, resume, que);    
      m.nileXInitial(initialTime, trace, bw);     
      write_fragreplresults(bw + "NileX"+Integer.toString(kfact)+Boolean.toString(enc));
      write_Msgresults(bw + "NileX"+Integer.toString(kfact)+Boolean.toString(enc));      
    }
    if (algo.equalsIgnoreCase("Nile")) {
      System.out.println(bw + " " + "Nile frag="+kfact+" encoding="+enc);      
      loadRAssign();
      Nile m = new Nile(Peerstable, tabdevList, gMsgTable, gApTable,enc,kfact);
      m.setParameters(ap_count, bandwidth, resume, que,msgtrack,false);    
      m.MyAlgo_Initial(initialTime, trace, bw);     
      write_fragreplresults(bw + "Nile"+Integer.toString(kfact)+Boolean.toString(enc));
      writeGraphml(bw+ "NileGraph"+Integer.toString(kfact));
      write_Msgresults(bw + "Nile"+Integer.toString(kfact));
    }
    if (algo.equalsIgnoreCase("SaveHistory")) {
      System.out.println(bw + " " + "History");     
      loadRAssign();
      Nile m = new Nile(Peerstable, tabdevList, gMsgTable, gApTable,enc,kfact);
      m.setParameters(ap_count, bandwidth, resume, que,msgtrack,false);    
      m.saveHistory(initialTime, trace, bw);     
    }
    if (algo.equalsIgnoreCase("conmsg")) {
      System.out.println(bw + "controlled message");     
      loadRAssign();
      Nile m = new Nile(Peerstable, tabdevList, gMsgTable, gApTable,enc,kfact);
      m.setParameters(ap_count, bandwidth, resume, que,msgtrack,false);    
      nodecount = 30;
      m.readHistory(initialTime, trace, bw); 
     // createControlMsg();      
    }
    if (algo.equalsIgnoreCase("MaxFlow")) {
      System.out.println(bw + "MaxFlow");     
      loadRAssign();
      HistoryMaxFlow m = new HistoryMaxFlow(Peerstable, tabdevList, gMsgTable);
      m.setParameters(ap_count, bandwidth, resume, que);    
      m.MaxFlow(initialTime);
      write_resultsMF(bw+"MaxFlow");
    }
    if (algo.equalsIgnoreCase("MaxFlowHistory")) {
      System.out.println(bw + "MaxFlowHistory");     
      loadRAssign();
      HistoryMaxFlow m = new HistoryMaxFlow(Peerstable, tabdevList, gMsgTable);
      m.setParameters(ap_count, bandwidth, resume, que);    
      m.createMaxFlowMsgs(initialTime, bw, trace,path);
      write_resultsMF(bw+"MaxFlowHistory");
    }
    if (algo.equalsIgnoreCase("MaxFlowSave")) {
      System.out.println(bw + "MaxFlowSave");     
      HistoryMaxFlow m = new HistoryMaxFlow(Peerstable, tabdevList, gMsgTable);
      m.setParameters(ap_count, bandwidth, resume, que);    
      m.MaxFlowHistory_Initial(trace, initialTime, bw);
      
    }
    if (algo.equalsIgnoreCase("Nileplus")) {
      System.out.println(bw + " " + "Nileplus" );
      loadRAssign();
      //eventList = new LinkedList<NodeList>();
      NileAdaptive m = new NileAdaptive(Peerstable, tabdevList, gMsgTable, gApTable,false,0);
      m.setParameters(ap_count, bandwidth, resume, que,msgtrack,false);
      //m.MyAlgo_DJPaths(trace);     
     m.MyAlgo_Initial(initialTime, trace, bw);
     // m.MyAlgo_Initialdummy(initialTime, trace, bw);
      //write_replresults(bw + "Nile");
      //write_replpath(bw + "MyAlgopaths.txt");
      write_fragreplresults(bw + "Nileplus");
      write_Msgresults(bw + "Nileplus");
      writeMsgml();
      writeGraphml("result/" + trace + "/"+ bw + "NileGraph"+Integer.toString(kfact)+Boolean.toString(enc));
    }
    if (algo.equalsIgnoreCase("DFlood")) {
      System.out.println(bw + " " + "Dflood" );
      loadRAssign();
      //eventList = new LinkedList<NodeList>();
      Nile m = new Nile(Peerstable, tabdevList, gMsgTable, gApTable,false,1);
      m.setParameters(ap_count, bandwidth, resume, que,msgtrack,true);
      //m.MyAlgo_DJPaths(trace);
     
     m.MyAlgo_Initial(initialTime, trace, bw);
     // m.MyAlgo_Initialdummy(initialTime, trace, bw);
      //write_replresults(bw + "Nile");
      //write_replpath(bw + "MyAlgopaths.txt");
     /*write_replresults(bw + "DFlood");
     write_Msgresults(bw + "DFlood");
     writeGraphml(bw+ "DfloodGraph");*/
    }
    if (algo.equalsIgnoreCase("CDFlood")) {
      System.out.println(bw + " " + "CogDflood" );
      loadRAssign();

      CognitiveDFlood m = new CognitiveDFlood(Peerstable, tabdevList, gMsgTable, gApTable,false,1);
      m.setParameters(ap_count, bandwidth, resume, que,msgtrack,true);
     
     m.MyAlgo_Initial(initialTime, trace, bw);
      write_replresults(bw + "CDFlood");
      //write_replpath(bw + "MyAlgopaths.txt");
     write_replresults(bw + "CDFlood");
     write_Msgresults(bw + "CDFlood");
     writeGraphml(bw+ "CDfloodGraph");
    }
    if (algo.equalsIgnoreCase("PROPHET")) {
      System.out.println(bw + " " + "Prophet");
      loadRAssign();
      Prophet p = new Prophet(Peerstable, tabdevList, gMsgTable, gApTable);
      p.setParameters(ap_count, bandwidth, (float) 0.75, (float) 0.98, resume,que);
      p.Proph_initial(initialTime);
      write_results(bw + "Prophet");
      write_Msgresults(bw + "Prophet");
    }
    if (algo.equalsIgnoreCase("FC")) {
      System.out.println(bw + " " + "FC");
      loadRAssign();
      FirstContact fc = new FirstContact(Peerstable, tabdevList, gMsgTable, gApTable);
      fc.setParameters(ap_count, bandwidth, resume, que);
      fc.FC_initial();
      write_results(bw + "FirstContact");
      write_Msgresults(bw + "FirstContact");
    }
    if (algo.equalsIgnoreCase("HBEC")) {
      System.out.println(bw + " " + "EC " + rfact + " " + kfact);
      loadRAssign();
      SimpleReplication sr = new SimpleReplication(Peerstable, tabdevList,
          gMsgTable, gApTable);
      sr.setParameters(bandwidth, ap_count, rfact, kfact, true/* Histry */,
          true/* EC */, false/* Extend */, resume, que);
      sr.SR_initial(initialTime, trace, bw);        
      write_fragreplresults(bw + "ErasureCoding");
      write_Msgresults(bw + "ErasureCoding");
      writeGraphml(bw+ "HBECGraph");
    }

    if (algo.equalsIgnoreCase("EBEC")) {
      System.out.println(bw + " " + "EBEC " + rfact + " " + kfact);
      loadRAssign();
      EBEC ebec = new EBEC(Peerstable, tabdevList, gMsgTable, gApTable);
      ebec.setParameters(ap_count, bandwidth, rfact, kfact, resume, que);
      ebec.EBECC_initial(initialTime, trace, bw);
      write_fragreplresults(bw + "EBErasureCoding");
      write_Msgresults(bw + "EBErasureCoding");
      writeGraphml(bw+ "EBECGraph");
    }
    if (algo.equalsIgnoreCase("SR")) {
      System.out.println(bw + " " + "SimpleRepli " + rfact);
      loadRAssign();
      SimpleReplication sr = new SimpleReplication(Peerstable, tabdevList,
          gMsgTable, gApTable);
      sr.setParameters(bandwidth, ap_count, rfact, kfact, false/* Histry */,
          false/* EC */, false/* Extend */, resume, que);
      sr.SR_initial(initialTime, trace, bw);
      write_replresults(bw + "SimpleRepli");
      write_Msgresults(bw + "SimpleRepli");
      writeGraphml(bw+ "SimpleRepliGraph");
    }
    if (algo.equalsIgnoreCase("HSR")) {
      System.out.println(bw + " " + "HistSimpleRepli " + rfact);
      loadRAssign();
      SimpleReplication sr = new SimpleReplication(Peerstable, tabdevList,
          gMsgTable, gApTable);
      sr.setParameters(bandwidth, ap_count, rfact, kfact, true/* Histry */,
          false/* EC */, false/* Extend */, resume, que);
      sr.SR_initial(initialTime, trace, bw);
      write_replresults(bw + "HistSimpleRepli");
      write_Msgresults(bw + "HistSimpleRepli");
      writeGraphml(bw+ "HistSimpleRepliGraph");
    }
    if (algo.equalsIgnoreCase("OracleMsgStat")) {
      
      System.out.println(bw + " set="+rfact + " week="+kfact+ " Oracle Message Stat");
      loadRAssign();
      Stats s = new Stats(Peerstable, tabdevList, gMsgTable);
      s.setParameters(ap_count, bandwidth);
      s.Create_InitialEvents(rfact,kfact,pathcount);
      gMsgTable = s.statsOracle_Initial();
      write_resultsStat(bw +Integer.toString(pathcount) + "ForwardingOracleStats"+Integer.toString(kfact));
    }
    if (algo.equalsIgnoreCase("MsgStat")) {
      int oweek = 1;
      System.out.println(bw + " set="+rfact + " week="+kfact+ " Message Stat");
      loadRAssign();
      Stats s = new Stats(Peerstable, tabdevList, gMsgTable);
      s.setParameters(ap_count, bandwidth);
      s.Create_InitialEvents(rfact,kfact,pathcount);
      gMsgTable= s.stats_Initial(trace,bw,rfact,kfact,pathcount,oweek);
      write_resultsStat( bw +Integer.toString(pathcount)+"ForwardingStats"+Integer.toString(kfact)+"_"+Integer.toString(oweek));
    }
    /*if (algo.equalsIgnoreCase("HBECE")) {
    System.out.println(bw + " " + "EC " + rfact + " " + kfact);
    SimpleReplication sr = new SimpleReplication(Peerstable, tabdevList,
        gMsgTable, gApTable);
   // sr.setParameters(bandwidth, ap_count, rfact, kfact, true/* Histry */
    //    true/* EC */, true/* Extend */, resume, que);
   /* sr.SR_initial(initialTime, trace, bw);
    write_replresults(bw + "ErasureCodingExt");
  }*/
        /*if (algo.equalsIgnoreCase("HSRE")) {
        System.out.println(bw + " " + "HistSimpleRepli " + rfact);
        SimpleReplication sr = new SimpleReplication(Peerstable, tabdevList,
            gMsgTable, gApTable);
        sr.setParameters(bandwidth, ap_count, rfact, kfact, true/* Histry */
      //      false/* EC */, true/* Extend */, resume, que);
        /*sr.SR_initial(initialTime, trace, bw);
        write_replresults(bw + "HistSimpleRepliExt");
      }*/
    /*if (algo.equalsIgnoreCase("Disjoint")) {
      System.out.println(bw + " " + "Disjoint ");
      NGraph graph = new NGraph(Peerstable, tabdevList, gMsgTable);
      if(bw.equals("low"))
        graph.writegraph(trace, ap_count);
     // graph.calcDpath(trace, ap_count);
      graph.calcDpathMFlow(trace);
    }*/
    
  }

  public Router(String trace) {

    this.trace = new String(trace);
    rfact = 1;
    kfact = 1;
    //ap_count = 0;
    run = new String("");
    seq = new String("0");
    bandwidth = 1024;
    bw = new String("low");
    msgcount = 100;
    nodecount = 70;
    resume = false;
    msgtrack=0;
    susers=0;
    
  }

  public boolean newscan() {

    //if(trace.equals("IBM"))
      //msgtrack++; 
    try {
      BufferedReader brin;
      tabapList = new LinkedList<String>();
      gApTable = new Hashtable<String, Apoint>();
      tabdevList = new LinkedList<String>();

      if (run.equals("3"))
        brin = new BufferedReader(new FileReader("resource/" + trace
            + "/AP list" + run)); // Continue to read lines while
      else
        brin = new BufferedReader(new FileReader("resource/" + trace
            + "/AP list")); // Continue to read lines while

  
      String line;
      while (brin.ready()) {
        line = brin.readLine();
        tabapList.add(line);
        gApTable.put(line, new Apoint(line));        
      }

       brin.close();

      if (run.equals("3"))
        brin = new BufferedReader(new FileReader("resource/" + trace
            + "/Dev list" + run)); // Continue to read lines while
      else
        brin = new BufferedReader(new FileReader("resource/" + trace
            + "/Dev list")); // Continue to read lines while

      while (brin.ready()) {
        line = brin.readLine();
        tabdevList.add(line);
      }
      brin.close();

    } catch (Exception e) {
      System.out.println("Faild to load ap and dev file at initialse");
    }
      return true;
  }
  public void readDatatrace(){
    Peerstable = new Hashtable<String, Device>();

    XMLAnalyse();
   // filtersegmentation(); // for removeing the device that r not in main
    // cluster.. only for IBM
    initialTime = get_initialtime();
    finalTime =  get_finaltime(initialTime);
    Enumeration <String>gapenum =gApTable.keys();
    while(gapenum.hasMoreElements()){
      Apoint aptemp = gApTable.get(gapenum.nextElement());
      aptemp.contacttimeList.add(new TSlot(initialTime,finalTime));
    }
    
    /*try{
      DataOutputStream out = new DataOutputStream(new FileOutputStream(
          "resource/" + trace + "/Dev list"));
      Enumeration<String> key_enum = Peerstable.keys();
      while (key_enum.hasMoreElements())
        out.writeBytes(key_enum.nextElement()+"\n");
      
      out.close();
    
    }catch (Exception e)
    {
      
    }*/
  }
  public void readproxyDatatrace(){
    Peerstable = new Hashtable<String, Device>();
    for(int i=0;i<tabdevList.size();i++)
      Peerstable.put(tabdevList.get(i), new Device(tabdevList.get(i)));
  }
  public Date get_initialtime() {

    ListIterator dit = tabdevList.listIterator();
    Date T1 = new Date();
    while (dit.hasNext()) {
      if (Peerstable.get(dit.next()).starttime.before(T1))
        T1.setTime(Peerstable.get(tabdevList.get(dit.nextIndex() - 1)).starttime
                .getTime());
    }
    return T1;
  }

  public Date get_finaltime(Date T1) {

    Date T2 = new Date();
    T2.setTime(T1.getTime());
    ListIterator dit = tabdevList.listIterator();
    while (dit.hasNext()) {
      if (Peerstable.get(dit.next()).sorted_slots.getLast().timemax.after(T2))
        T2.setTime(Peerstable.get(tabdevList.get(dit.nextIndex() - 1)).sorted_slots
                .getLast().timemax.getTime());
    }
    return T2;
  }

  // this function adds ap as data musles
  public void AddDepFunc() {

    Device dtemp;
    Date firsttime = new Date();
    Date lasttime = new Date();
    lasttime = get_finaltime(firsttime = initialTime);

    ListIterator apit = tabapList.listIterator();
    while (apit.hasNext()) {
      dtemp = new Device((String) apit.next());
      dtemp.starttime.setTime(firsttime.getTime());
      TSlot ttemp = new TSlot(firsttime, lasttime);
      ttemp.apname = dtemp.mac;
      dtemp.sorted_slots.add(ttemp);
      dtemp.Aptable.put(ttemp.apname, new Apoint(dtemp.mac));
      dtemp.Aptable.get(dtemp.mac).contacttimeList.add(ttemp);
      tabdevList.add(dtemp.mac);
      Peerstable.put(dtemp.mac, dtemp);
      if(ap_count==0)
        gApTable.get(dtemp.mac).devlist.add(dtemp);
    }
  }

  public void verify() {

    for (int i = 0; i < tabdevList.size(); i++) {
      if (!Peerstable.containsKey(tabdevList.get(i)))
        System.err.println("error1");
    }
    Iterator pit = Peerstable.values().iterator();
    while (pit.hasNext()) {
      if (!tabdevList.contains(((Device) pit.next()).mac))
        System.err.println("error2");
    }
  }

  public void XMLAnalyse() {

    try {
      XMLReader parser1 = XMLReaderFactory.createXMLReader();
      ContentHandler handler = new XMLDatahandler(this);
      parser1.setContentHandler(handler);
      if (run.equals("3"))
        parser1.parse("resource/" + trace + "/analysedfinal3.xml");
      else
        parser1.parse("resource/" + trace + "/analysedfinal.xml");

      System.out.println("XML Analysis Complete" + " "
          + Integer.toString(tabdevList.size()) + " "
          + Integer.toString(tabapList.size()) + " "
          + Integer.toString(Peerstable.size()));

    } catch (SAXException e) {
      System.err.println("Input is not well formed");
    } catch (IOException e) {
      System.err.println("IO error at source data reading");
      e.printStackTrace();
    }
  }

  public void addelement(Device dtemp) {
   // System.out.println(Peerstable.size());
    if(trace.equalsIgnoreCase("ibm"))
      removeSmallClusterAP(dtemp);
    dtemp.create_sortedslots();
    if (dtemp.sorted_slots.size() > 0) {
      Peerstable.put(dtemp.mac, dtemp);

      Enumeration<String> key_enum = dtemp.Aptable.keys();
  
      while (key_enum.hasMoreElements()) {
        String apname = key_enum.nextElement();
        Apoint aptemp = gApTable.get(apname);
        if (aptemp != null) {
          if(!aptemp.devlist.contains(dtemp))
            aptemp.devlist.add(dtemp);
          //if (aptemp.Apointname.equals(dtemp.mac))
            //break;
        }
      }
    }
    //System.out.println("Reading data "+Peerstable.size());
   // if(dtemp.mac.equals("7ef7e22370fed14108d954366a4440f2ffd06bf2"))
   //   for(int m=0;m<dtemp.sorted_slots.size();m++){
  //      TSlot ttemp = dtemp.sorted_slots.get(m);
   //     System.out.println("G "+ttemp.timemin.toString()+"\t"+ttemp.timemax.toString()+"\t"+ttemp.apname);

   //   }
  }
  
void removeSmallClusterAP(Device dtemp){
  Enumeration <String> apenum =dtemp.Aptable.keys();
  //if(!tabdevList.contains(dtemp.mac))
   // tabdevList.add(dtemp.mac);
  while(apenum.hasMoreElements()){
    String apname = apenum.nextElement();
    if(!tabapList.contains(apname))
     //tabapList.add(apname);
      dtemp.Aptable.remove(apname);
  }
}
  // this function removes the nodes which were not seen in the primary cluster
  // of IBM trace so it not useful for other cases;
  public void filtersegmentation() {

    try {
      // System.out.println(tabapList.size());
      // BufferedReader in = new BufferedReader( new
      // FileReader("resource/Coordinates0.txt")); // Continue to read lines
      // while
      // DataOutputStream out = new DataOutputStream(new
      // FileOutputStream("resource/AP list")); // Continue to read lines while
      // while (in.ready())
      // out.writeBytes(tabapList.get(Integer.parseInt(in.readLine()))+"\n");
      // out.close();
      // in.close();
      // in = new BufferedReader(new FileReader("resource/AP list")); //
      // Continue to read lines while
      // tabapList=new LinkedList<String>();
      // String line;
      // while(in.ready()){
      // line =in.readLine();
      // tabapList.add(line);
      // }
      // in.close();
      //    
      // System.out.println(tabapList.size());
      
      for (int i = 0; i < tabdevList.size(); i++) {
        // tabaplist contains only the list of large cluster access points
        Device dtemp = Peerstable.get(tabdevList.get(i));
        int j;
        for (j = 0; j < tabapList.size(); j++) {
          if (dtemp.Aptable.containsKey(tabapList.get(j)))
            break;
        }
        if (j == tabapList.size()) {
          Peerstable.remove(dtemp.mac);
          tabdevList.remove(i);
         i--;
        }
      }
      System.out.println(tabdevList.size() + " " + Peerstable.size());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void createMsg() {

    long minute = 1000 * 60;

    
    Date T2 = get_finaltime(initialTime);
    System.out.println(initialTime.toString() + " " + T2.toString());
    long minspan = (T2.getTime() - initialTime.getTime()) / (minute * 2);
    int ttemp = 10;
    while (ttemp < minspan)
      ttemp *= 10;
    int stemp = 10;
    while (stemp < (tabdevList.size() - ap_count))
      stemp *= 10;
    int a = 40;
    //int a = 10;
    double k = 2;
    int ignored_peers = tabdevList.size()
        - (tabdevList.size() * nodecount / 100);

    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(
          "resource/" + trace + "/Routing AssignGraph")); // Continue to read lines
      // while
      out.writeBytes(Integer.toString(nodecount) + "\n");
      for (int i = 0; i < msgcount; i++) {
        int st = (int) ((Math.random() * ttemp) % minspan);
        // T2.setTime(T1.getTime()+st*1000*60);
        // System.out.println(T2.toString());
        int src = (int) ((Math.random() * stemp) % (tabdevList.size() - ignored_peers));
        //int src = (int) ((Math.random() * stemp) % (200));
        int dest;
        do {
          dest = (int) ((Math.random() * stemp) % (tabdevList.size() - ignored_peers));
          //dest = (int) ((Math.random() * stemp) % (200));
        } while (dest == src);
        out.writeBytes(src + " " + dest + " " + 0 + " " + Integer.toString(st)
            + " " + Math.pow(a * (i + 1), k) + "\n");
      }
      out.close();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
 
 /* public void createControlMsg() {

    long minute = 1000 * 60;

    Date T1 = get_initialtime();
    Date T2 = get_finaltime(T1);
    Date Dtime;
    int hop;
    System.out.println(T1.toString() + " " + T2.toString());
    long minspan = (T2.getTime() - T1.getTime()) / (minute * 2);
    int ttemp = 10;
    while (ttemp < minspan)
      ttemp *= 10;
    int stemp = 1;
    while (stemp < (tabdevList.size()-ap_count))
      stemp *= 10;
    
    int peers_toinclude =  (tabdevList.size() * nodecount / 100);

    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream(
          "resource/" + trace + "/"+bw+"ControledRoutingAssign")); // Continue to read lines
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream(
          "result/" + trace + "/"+bw+"MaxFlowHistory.txt")); // Continue to read lines
     
      out.writeBytes(Integer.toString(nodecount) + "\n");
      for (int i = 0; i < msgcount; i++) {
        int st = (int) ((Math.random() * ttemp) % minspan);
        // T2.setTime(T1.getTime()+st*1000*60);
        // System.out.println(T2.toString());
        //int src = (int) ((Math.random() * stemp) % (tabdevList.size() - ignored_peers));
        int src;
        Hashtable<Integer, Integer>soundDest;
        do{
            src = (int) ((Math.random() * stemp) % (peers_toinclude));
            soundDest=checkMeetingcount(Peerstable.get(tabdevList.get(src)));
        }while( soundDest.size()<1);
        
        int dest;
        int dtemp = 1;
        while (dtemp < soundDest.size())
          dtemp *= 10;
        Enumeration<Integer>kenum;
        
        int rand;        
        rand = (int)(Math.random()*dtemp) % soundDest.size();
        kenum = soundDest.keys();
        while(rand-->1)
          kenum.nextElement();          
        dest = soundDest.get(kenum.nextElement());        
        kenum = Peerstable.get(tabdevList.get(src)).meetStat.keys();
        double maxflowsize=0;
        Dtime = new Date(T1.getTime());
        hop = 0;
        
        while(kenum.hasMoreElements()){
          int key = kenum.nextElement();
          
          if(Peerstable.get(tabdevList.get(src)).meetStat.get(key).myNeighbor.get(key).PVector.containsKey(dest)){
            maxflowsize += Peerstable.get(tabdevList.get(src)).meetStat.get(key).myNeighbor.get(key).PVector.get(dest).VirtAvgSize;
            
            if(Dtime.before(new Date(T1.getTime()+(Peerstable.get(tabdevList.get(src)).meetStat.get(key).myNeighbor.get(key).
                PVector.get(dest).VirtAvgDelay.longValue())*60*100)))
              Dtime.setTime(T1.getTime()+(Peerstable.get(tabdevList.get(src)).meetStat.get(key).myNeighbor.get(key).PVector.
                  get(dest).VirtAvgDelay.longValue()*60*1000));      
            
            if(hop<Peerstable.get(tabdevList.get(src)).meetStat.get(key).myNeighbor.get(key).PVector.get(dest).depth)
              hop = Peerstable.get(tabdevList.get(src)).meetStat.get(key).myNeighbor.get(key).PVector.
              get(dest).depth;
          }        
        }  
        out.writeBytes(src + " " + dest + " " + 0 + " " + Integer.toString(st)+ " " + Double.toString(maxflowsize) + "\n"); 
        out2.writeBytes(Integer.toString(i)
            + "\t"+src+ "\t"+dest + "\t" + Double.toString(maxflowsize)+ "\t"
            + Double.toString(Round((double) (Dtime.getTime() - T1.getTime())/ (1000 * 60)+(maxflowsize/bandwidth)*hop,2))
            + "\t" +(hop)+"\n");
      }
      out.close();
      System.out.println("Routing Assignment created with msgcount= "+msgcount+" nodecount="+nodecount);
    } catch (Exception e) {
      System.out.println(e.toString());
      e.printStackTrace();
    }
  }
  public Hashtable<Integer, Integer> checkMeetingcount(Device dtemp){
   Hashtable<Integer, Integer>soundDest = new Hashtable<Integer, Integer>();
   Enumeration<Integer>kenum = dtemp.meetStat.keys();
   while(kenum.hasMoreElements()){
     int key = kenum.nextElement();
     if(dtemp.meetStat.get(key).myNeighbor.get(key).PVector.get(key).VirtAvgDelay<Global.weekMin)
     {
       soundDest.put(key, key);
       Enumeration<Integer>kenum2 = dtemp.meetStat.get(key).myNeighbor.get(key).PVector.keys();
       while(kenum2.hasMoreElements()){
         Integer key2 = kenum2.nextElement();
         if(dtemp.meetStat.get(key).myNeighbor.get(key).PVector.get(key2).VirtAvgDelay<Global.weekMin)
           soundDest.put(key, key2);
       }
     }
   }    
    return soundDest;
  }*/

  public void loadRAssign() {
    gMsgTable =  new Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>();

    int msgsread = Integer.parseInt(seq); // tell whether the whole file is
    // taken as one assignment or for
    // multiple runs

    long et = new Date().getTime();

    try {
    
      BufferedReader in = new BufferedReader(new FileReader("resource/" + trace
          + "/Routing Assign" + run)); // Continue to read lines while
      /*if(trace.equals("IBM"))
        in = new BufferedReader(new FileReader("resource/" + trace
            + "/Routing Assign400" + run)); // Continue to read lines while*/
      
      nodecount = Integer.parseInt(in.readLine());
      int batch=30;
      int count = 0;
      if (msgsread == 0) {
        while (in.ready())
          readMessageFile(in, initialTime, et, count);
      } else {
        for (int i = 0; i < (msgsread - 1) * batch; i++)
          in.readLine();
        for (int i = 0; i < batch; i++) {

          readMessageFile(in, initialTime, et, count++);
        }
      }
      in.close();
      msgcount = gMsgTable.size();
      System.out.println(gMsgTable.size());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void readMessageFile(BufferedReader in, Date T1, long et, int count) {

    long minute = 1000 * 60;
    int a = 40;
    double k = 2;
    try {
      String[] component = in.readLine().split(" ");

      Msg mtemp;
      long st;
      if (component.length == 5) {

       /* st = Peerstable.get(tabdevList.get(Integer.parseInt(component[0]))).starttime
            .getTime()
            + (Integer.parseInt(component[3]) * minute);*/
        st = Peerstable.get(tabdevList.get(Integer.parseInt(component[0]))).starttime
            .getTime();
        // ibm timespan.. msgs r created between the 12th day and 19th day
   
        if (st < (T1.getTime() + minute * 18160))
          st += minute * 18160;
        if (st > (T1.getTime() + 28800 * minute))
         st -= 10700 * minute;

    /*    if (st < (T1.getTime() + minute * 11520))
          st += minute * 7200;
        if (st >= (T1.getTime() + 15840 * minute))
          st -= 15840 * minute;
*/
        et = st + 10080 * minute;
        
        if (seq.equals("0")){
          if(path)
          mtemp = new Msg(Integer.parseInt(component[0]), Integer
              .parseInt(component[1]), Integer.parseInt(component[2]), st, et,
              0, 0, 1);        
        else
         mtemp =       new Msg(Integer.parseInt(component[0]), Integer
              .parseInt(component[1]), Integer.parseInt(component[2]), st, et,
              0, 0, Double.parseDouble(component[4]));
        }
        else if(!path)
          mtemp = new Msg(Integer.parseInt(component[0]), Integer
              .parseInt(component[1]), Integer.parseInt(component[2]), st, et,
              0, 0, Math.pow(a * (count + 1), k));
        else
          mtemp = new Msg(Integer.parseInt(component[0]), Integer
              .parseInt(component[1]), Integer.parseInt(component[2]), st, et,
              0, 0, 1000); 

     
       
        gMsgTable.put(mtemp.ID, new Hashtable<Integer, Hashtable<String,Msg>>());
        gMsgTable.get(mtemp.ID).put(mtemp.custodian, new Hashtable<String, Msg>());
        gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);
        /*if(mtemp.ID.equals(2)){
          gMsgTable.get(mtemp.ID).put(mtemp.dest, new Hashtable<String, Msg>());
          gMsgTable.get(mtemp.ID).get(mtemp.dest).put(mtemp.frag_id, mtemp);
        }*/
        mtemp.hoplist.add(mtemp.src);
        Peerstable.get(tabdevList.get(mtemp.src)).src = true;
        Peerstable.get(tabdevList.get(mtemp.dest)).dest = true;
      
      
        
       // mtemp.hopProxlist.add(new BloomValue(mtemp.src,0,1.0,mtemp.m_interval.life_time.timemin));
        System.out.println(Peerstable.get(tabdevList.get(mtemp.src)).starttime
            .toString()+ " " + mtemp.m_interval.life_time.timemin.toString()        + " "
            + mtemp.m_interval.life_time.timemax.toString() + " " + mtemp.size);
      }
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }
  public void write_fragreplresults(String fname) {

   
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0) ;
    try {
      DataOutputStream out1;
      if (seq.equals("0")){
        out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname+".txt")); // Continue to read lines while
        
      }
      else{
        out1 = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname+".txt")); // Continue t        
        out1.writeBytes(Integer.toString(msgcount) + " "
          + Integer.toString(nodecount) + "\n");
      }
      Enumeration<Integer> gmid = gMsgTable.keys();
      while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        LinkedList<Msg> r_msg = new LinkedList<Msg>();
              
        while(gcid.hasMoreElements()){ 
          Integer cid = gcid.nextElement();
          Enumeration <String>fcid = gMsgTable.get(mid).get(cid).keys();
          while(fcid.hasMoreElements()){
            mtemp = gMsgTable.get(mid).get(cid).get(fcid.nextElement());
             if(gMsgTable.get(mtemp.ID).containsKey(mtemp.dest))
            { 
              //if(gMsgTable.get(mtemp.ID).get(mtemp.dest).keySet().size()>=mtemp.frag_count)
              
              Enumeration <String>fcid2 = gMsgTable.get(mid).get(mtemp.dest).keys();
              while(fcid2.hasMoreElements())
                r_msg.add( gMsgTable.get(mtemp.ID).get(mtemp.dest).get(fcid2.nextElement()));
              if(path)
                mtemp.frag_count=1;
              
              if(mtemp.frag_count>1 & enc )
              {                
                if(codingValidty(r_msg) ){
                  for(int i=0;i<r_msg.size();i++){
                    if(r_msg.get(i).cFrags.length>1)
                      r_msg.remove(i);
                  }
                }
              }
              if(r_msg.size()>=mtemp.frag_count){
                Collections.sort(r_msg, new DeltimeComparator());
                mtemp = r_msg.get(mtemp.frag_count-1);
              }
            }
            break;
          }
          break;          
        }      
        out1
            .writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                + "\t"
                + Double.toString(mtemp.size*mtemp.frag_count)
                + "\t"
                + Double.toString(Round((double) (mtemp.del_time.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2))
                + "\t" +(mtemp.hoplist.size() - 1)+"\n");
        
      }
      out1.writeBytes("\n");
      System.out.println("Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    writeDevstat(fname);
  }
  public void write_maxflowresults(String fname) {

    
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0) ;
    try {
      DataOutputStream out1;
      if (seq.equals("0")){
        out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname+".txt")); // Continue to read lines while
        
      }
      else{
        out1 = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname+".txt")); // Continue t        
        out1.writeBytes(Integer.toString(msgcount) + " "
          + Integer.toString(nodecount) + "\n");
      }
      Enumeration<Integer> gmid = gMsgTable.keys();
      while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        LinkedList<Msg> r_msg = new LinkedList<Msg>();
              
        while(gcid.hasMoreElements()){ 
          Integer cid = gcid.nextElement();
          Enumeration <String>fcid = gMsgTable.get(mid).get(cid).keys();
          while(fcid.hasMoreElements()){
            mtemp = gMsgTable.get(mid).get(cid).get(fcid.nextElement());
             if(gMsgTable.get(mtemp.ID).containsKey(mtemp.dest))
            { 
              //if(gMsgTable.get(mtemp.ID).get(mtemp.dest).keySet().size()>=mtemp.frag_count)
              
              Enumeration <String>fcid2 = gMsgTable.get(mid).get(mtemp.dest).keys();
              while(fcid2.hasMoreElements())
                r_msg.add( gMsgTable.get(mtemp.ID).get(mtemp.dest).get(fcid2.nextElement()));
              if(path)
                mtemp.frag_count=1;             
              
                Collections.sort(r_msg, new DeltimeComparator());
                //mtemp = r_msg.get(mtemp.frag_count-1);
                mtemp.size=0;
                for(int j=0;j<r_msg.size();j++)
                  mtemp.size += r_msg.get(j).size;
            }
            break;
          }
          break;          
        }   
        
        out1.writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                + "\t"
                + Double.toString(mtemp.size)
                + "\t"
                + Double.toString(Round((double) (mtemp.del_time.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2))
                + "\t" +(mtemp.hoplist.size() - 1)+"\n");
        
      }
      out1.writeBytes("\n");
      System.out.println("Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    
  }
  public void writeMsgml(){
    /*String NML = new String("");
    String EML = new String("");*/
    color = new String[5];
    color[0]= "#000000";
    color[1]= "#FF0000";
    color[2]= "#00FF00";
    color[3]= "#CCFFFF";
    color[4]= "#CCCCCC";
    
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0) ;
    Enumeration<Integer>gcid = gMsgTable.get(msgtrack).keys();
    Integer cid = gcid.nextElement();
    Enumeration <String>fcid = gMsgTable.get(msgtrack).get(cid).keys();
    while(fcid.hasMoreElements()){
      mtemp = gMsgTable.get(msgtrack).get(cid).get(fcid.nextElement());
      break;
    }
   nList = gMsgTable.get(msgtrack).get(mtemp.src).get("0").cNode;
    gmlData g = new gmlData();
    //int nodes =0;int edges = 0;
    try{
      DataOutputStream out =  new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bw + "EPOMsgTrace"+Integer.toString(kfact)+Boolean.toString(enc)+".graphml"));
      
       out.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\">\n");
       out.writeBytes("<key for=\"node\" id=\"d0\" yfiles.type=\"nodegraphics\"/><key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d1\"/>  <key for=\"edge\" id=\"d2\" yfiles.type=\"edgegraphics\"/> <key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d3\"/><key for=\"graphml\" id=\"d4\" yfiles.type=\"resources\"/>\n");
       //g.NML=g.NML.concat("<node id=\"n"+Integer.toString(tabdevList.indexOf(nList.label))+" 0\">");
       g.NML=g.NML.concat("<node id=\"n"+Integer.toString(tabdevList.indexOf(nList.label))+"\">");
       g.NML=g.NML.concat("<data key=\"d0\">");
       g.NML = g.NML.concat("<y:ShapeNode> <y:Geometry height=\"30.0\" width=\"30.0\" x=\"313.0\" y=\"140.0\"/><y:Fill color=\"#00FFFF\" transparent=\"false\"/> <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/><y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"13\" fontStyle=\"plain\" hasBackgroundColor=\"false\" ");
       g.NML = g.NML.concat("hasLineColor=\"false\" height=\"19.310546875\" modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\" width=\"31.0\" x=\"-0.5\" y=\"5.3447265625\">"+Integer.toString(tabdevList.indexOf(nList.label))+"</y:NodeLabel><y:Shape type=\"rectangle\"/> </y:ShapeNode>\n");
       g.NML = g.NML.concat(" </data> <data key=\"d1\"/>\n");
       g.NML = g.NML.concat("</node>\n");
       g.nodes++;
    
      
       writeLAyer(nList, g,gMsgTable.get(msgtrack).get(mtemp.dest).get("0"));
       out.writeBytes("<graph edgedefault=\"directed\" id=\"G\" parse.edges=\""+Integer.toString(g.edges) +"\" parse.nodes=\""+Integer.toString(g.nodes) +"\" parse.order=\"free\">\n");
       out.writeBytes(g.NML);
       out.writeBytes(g.EML);
       out.writeBytes("</graph>");
       out.writeBytes("<data key=\"d4\">\n");
       out.writeBytes("<y:Resources/>\n");
       out.writeBytes("</data>");
       out.writeBytes("</graphml>\n");
       out.close();
       System.out.println("MsgMl file written");
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
public void writeLAyer(NodeList  n, gmlData g, Msg mtemp){
    
    
       
    for(int i=0;i<n.eventList.size();i++){
      String nodeColor = "#FFCC00";
      if(tabdevList.indexOf(n.eventList.get(i).label)==mtemp.dest)
        nodeColor = new String("#006666");

      //g.NML=g.NML.concat("<node id=\"n"+Integer.toString(tabdevList.indexOf(n.eventList.get(i).label))+" "+Integer.toString(i)+"\">");
      g.NML=g.NML.concat("<node id=\"n"+Integer.toString(tabdevList.indexOf(n.eventList.get(i).label))+"\">");
      
      g.NML=g.NML.concat("<data key=\"d0\">");
      g.NML=g.NML.concat("<y:ShapeNode> <y:Geometry height=\"30.0\" width=\"30.0\" x=\"313.0\" y=\"140.0\"/><y:Fill color=\""+nodeColor+"\" transparent=\"false\"/> <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/><y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"13\" fontStyle=\"plain\" hasBackgroundColor=\"false\" ");
      g.NML=g.NML.concat("hasLineColor=\"false\" height=\"19.310546875\" modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" visible=\"true\" width=\"31.0\" x=\"-0.5\" y=\"5.3447265625\">"+Integer.toString(tabdevList.indexOf(n.eventList.get(i).label))+"</y:NodeLabel><y:Shape type=\"rectangle\"/> </y:ShapeNode>\n");
      g.NML = g.NML.concat(" </data> <data key=\"d1\"/>\n");
      g.NML = g.NML.concat("</node>\n");
      g.nodes++;
     // g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges)+"\" source=\"n"+Integer.toString(tabdevList.indexOf(n.label))+" "+Integer.toString(j)+"\" target=\"n"+Integer.toString(tabdevList.indexOf(n.eventList.get(i).label))+" "+Integer.toString(i)+"\">\n");
      g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges)+"\" source=\"n"+Integer.toString(tabdevList.indexOf(n.label))+"\" target=\"n"+Integer.toString(tabdevList.indexOf(n.eventList.get(i).label))+"\">\n");
      if(n.eventList.get(i).status==1 &mtemp.hoplist.contains(tabdevList.indexOf(n.label)) & mtemp.hoplist.contains(tabdevList.indexOf(n.eventList.get(i).label)))
          g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\""+color[n.eventList.get(i).status]+"\" type=\"line\" width=\""+n.eventList.get(i).count*4+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle smoothed=\"false\"/></y:PolyLineEdge></data>\n");
      
     else   
        g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\""+color[n.eventList.get(i).status]+"\" type=\"line\" width=\""+n.eventList.get(i).count+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle smoothed=\"false\"/></y:PolyLineEdge></data>\n");
      g.EML=g.EML.concat("<data key=\"d3\"/>\n");
      g.EML=g.EML.concat("</edge>");
      g.edges++;
      writeLAyer(n.eventList.get(i), g,mtemp);
    }    
  }
  public void writeGraphml(String file){
   // Msg mtemp = new Msg(0,0,0,0,0,0,0,0) ;
    Enumeration<Integer>gcid = gMsgTable.get(msgtrack).keys();
    Integer cid = gcid.nextElement();
    Enumeration <String>fcid = gMsgTable.get(msgtrack).get(cid).keys();
    /*while(fcid.hasMoreElements()){
      mtemp = gMsgTable.get(msgtrack).get(cid).get(fcid.nextElement());
      break;
    }*/
    //mtemp = gMsgTable.get(msgtrack).get(mtemp.dest).get("0");
    if(bw.equalsIgnoreCase("Med"))
      bandwidth*=10;
    if(bw.equalsIgnoreCase("High"))
      bandwidth*=20;
    gmlData g = new gmlData();
    //gmlData g2 = new gmlData();
     try{
      DataOutputStream out =  new DataOutputStream(new FileOutputStream("result/" + trace + "/"+file+".graphml"));
     // DataOutputStream out2 =  new DataOutputStream(new FileOutputStream("result/" + trace + "/"+file+"contacts.graphml"));
       
      out.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\" " +
          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" " +
          "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\">\n");
      out.writeBytes("<key for=\"node\" id=\"d0\" yfiles.type=\"nodegraphics\"/><key attr.name=\"description\" attr.type=\"string\" " +
          "for=\"node\" id=\"d1\"/>  <key for=\"edge\" id=\"d2\" yfiles.type=\"edgegraphics\"/> <key attr.name=\"description\" " +
          "attr.type=\"string\" for=\"edge\" id=\"d3\"/><key for=\"graphml\" id=\"d4\" yfiles.type=\"resources\"/>\n");
     /* out2.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns/graphml\" " +
           "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:y=\"http://www.yworks.com/xml/graphml\" " +
           "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\">\n");
      out2.writeBytes("<key for=\"node\" id=\"d0\" yfiles.type=\"nodegraphics\"/><key attr.name=\"description\" attr.type=\"string\" " +
           "for=\"node\" id=\"d1\"/>  <key for=\"edge\" id=\"d2\" yfiles.type=\"edgegraphics\"/> <key attr.name=\"description\" " +
           "attr.type=\"string\" for=\"edge\" id=\"d3\"/><key for=\"graphml\" id=\"d4\" yfiles.type=\"resources\"/>\n");
       */
       Device dtemp;
       for(int i=0;i<tabdevList.size()-ap_count;i++){
       dtemp=Peerstable.get(tabdevList.get(i));
       if(dtemp.rbytes==0&dtemp.dbytes==0)
         continue;
       String nodeColor = "#FFCC00";
       
       if(dtemp.src)
         nodeColor = new String("#00FFFF");
       if(dtemp.dest)
         nodeColor = new String("#006666");
       if(dtemp.dest&dtemp.src)
         nodeColor = new String("#FF6666");
       
      
       g.NML=g.NML.concat("<node id=\"n"+Integer.toString(i)+"\">");
       g.NML=g.NML.concat("<data key=\"d0\">");
       g.NML = g.NML.concat("<y:ShapeNode> <y:Geometry height=\""+Round(Math.log(dtemp.rbytes+dtemp.dbytes)+20,2) +"\" width=\""+
           Round(Math.log(dtemp.dbytes+dtemp.rbytes)+20,2)+"" +"\" x=\"313.0\" y=\"140.0\"/><y:Fill color=\""+ 
           nodeColor+"\" transparent=\"false\"/> <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/><y:NodeLabel alignment=\"center\" " +
              "autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"13\" fontStyle=\"plain\" hasBackgroundColor=\"false\" ");
       g.NML = g.NML.concat("hasLineColor=\"false\" height=\"19\" modelName=\"internal\" modelPosition=\"c\" " +
          "textColor=\"#000000\" visible=\"true\" width=\"12\" x=\"-0.5\" y=\"5.3447265625\">"+Integer.toString(i)+
          "</y:NodeLabel><y:Shape " +
              "type=\"rectangle\"/> </y:ShapeNode>\n");
       g.NML = g.NML.concat(" </data> <data key=\"d1\"/>\n");
       g.NML = g.NML.concat("</node>\n");
       g.nodes++;
       
      /*g2.NML=g2.NML.concat("<node id=\"n"+Integer.toString(i)+"\">");
       g2.NML=g2.NML.concat("<data key=\"d0\">");
       g2.NML = g2.NML.concat("<y:ShapeNode> <y:Geometry height=\""+Round(Math.log(dtemp.rbytes+dtemp.dbytes)+20,2) +"\" width=\""+
           Round(Math.log(dtemp.dbytes+dtemp.rbytes)+20,2)+"" +"\" x=\"313.0\" y=\"140.0\"/><y:Fill color=\""+ 
           nodeColor+"\" transparent=\"false\"/> <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/><y:NodeLabel alignment=\"center\" " +
              "autoSizePolicy=\"content\" fontFamily=\"Dialog\" fontSize=\"13\" fontStyle=\"plain\" hasBackgroundColor=\"false\" ");
       g2.NML = g2.NML.concat("hasLineColor=\"false\" height=\"19\" modelName=\"internal\" modelPosition=\"c\" " +
          "textColor=\"#000000\" visible=\"true\" width=\"12\" x=\"-0.5\" y=\"5.3447265625\">"+Integer.toString(i)+
          "</y:NodeLabel><y:Shape " +
              "type=\"rectangle\"/> </y:ShapeNode>\n");
       g2.NML = g2.NML.concat(" </data> <data key=\"d1\"/>\n");
       g2.NML = g2.NML.concat("</node>\n");
       g2.nodes++;*/
       
       writeEdges(dtemp,g,i,null);
       /*if(g.EML.length()>1){
         out.writeBytes(g.NML);
         out.writeBytes(g.EML);
       }
       g.NML=new String("");
       g.EML = new String("");*/
     }   
         
       out.writeBytes("<graph edgedefault=\"directed\" id=\"G\" parse.edges=\""+Integer.toString(g.edges) +"\" parse.nodes=\""+Integer.toString(g.nodes) +"\" parse.order=\"free\">\n");
       out.writeBytes(g.NML);
       out.writeBytes(g.EML);
       out.writeBytes("</graph>");
       out.writeBytes("<data key=\"d4\">\n");
       out.writeBytes("<y:Resources/>\n");
       out.writeBytes("</data>");
       out.writeBytes("</graphml>\n");
       out.close();
       
     /*  out2.writeBytes("<graph edgedefault=\"directed\" id=\"G\" parse.edges=\""+Integer.toString(g.edges) +"\" parse.nodes=\""+Integer.toString(g.nodes) +"\" parse.order=\"free\">\n");
       out2.writeBytes(g.NML);
       out2.writeBytes(g.EML);
       out2.writeBytes("</graph>");
       out2.writeBytes("<data key=\"d4\">\n");
       out2.writeBytes("<y:Resources/>\n");
       out2.writeBytes("</data>");
       out2.writeBytes("</graphml>\n");
       out2.close();*/
       System.out.println("graphMl file written");
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  public void writeEdges(Device dtemp,gmlData g, int i, Msg mtemp){
    Enumeration<Integer> mskEnum = dtemp.meetStat.keys();
    MeetingStats ms;
    Integer mskey;
    int factor=1;
    String type="line";
    if(bw.equalsIgnoreCase("High"))
      type="dashed";
    while(mskEnum.hasMoreElements()){
      mskey = mskEnum.nextElement();
      ms = dtemp.meetStat.get(mskey);
    
      
      if(ms.msgsAttempt<1)
        continue;
      if(ms.bAttempted>ms.bAvailable){
        g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
        g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#FF0000\" " +
            "type=\""+type+"\" width=\""+Round((Math.log10(ms.bAttempted)-Math.log10(ms.bAvailable))/factor,2)+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle " +
                "smoothed=\"false\"/><y:EdgeLabel>"+Round((Math.log10(ms.bAttempted)-Math.log10(ms.bAvailable))/factor,2)+" "+Round(ms.stabiltiy,2)+"</y:EdgeLabel></y:PolyLineEdge></data>\n");
        g.EML=g.EML.concat("<data key=\"d3\"/>\n");
        g.EML=g.EML.concat("</edge>\n");
        g.edges++;
      }
      else
      {
        g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
        g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#00FF00\" " +
            "type=\""+type+"\" width=\""+Round(Math.log10(ms.bAvailable)-(Math.log10(ms.bAttempted))/factor,2)+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle " +
                "smoothed=\"false\"/><y:EdgeLabel>"+Round(Math.log10(ms.bAvailable)-(Math.log10(ms.bAttempted))/factor,2)+" "+Round(ms.stabiltiy,2)+"</y:EdgeLabel></y:PolyLineEdge></data>\n");
        g.EML=g.EML.concat("<data key=\"d3\"/>\n");
        g.EML=g.EML.concat("</edge>\n");
        g.edges++;
      }
      if(ms.msgsTransf>0){
        g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
        g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#0000FF\" " +
            "type=\""+type+"\" width=\""+ms.msgsTransf+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle smoothed=\"false\"/><y:EdgeLabel>"
            +ms.msgsTransf+"</y:EdgeLabel></y:PolyLineEdge></data>\n");
        g.EML=g.EML.concat("<data key=\"d3\"/>\n");
        g.EML=g.EML.concat("</edge>\n");
        g.edges++;
      }
      if(ms.msgsAttempt>0){
        g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
        g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#000000\" " +
            "type=\""+type+"\" width=\""+ms.msgsAttempt+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle smoothed=\"false\"/><y:EdgeLabel>"
            +ms.msgsAttempt+"</y:EdgeLabel></y:PolyLineEdge></data>\n");
        g.EML=g.EML.concat("<data key=\"d3\"/>\n");
        g.EML=g.EML.concat("</edge>\n");
        g.edges++;
      }
     if(ms.meetingcnt>0 & i>mskey){
      g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
      g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#101010\" type=\"line\" width=\""+
      ms.meetingcnt+"\"/><y:Arrows source=\"none\" target=\"none\"/> <y:BendStyle smoothed=\"false\"/></y:PolyLineEdge></data>\n");
      g.EML=g.EML.concat("<data key=\"d3\"/>\n");
      g.EML=g.EML.concat("</edge>\n");
      g.edges++;
    }
    /* if(ms.bAvailable>0){
        g2.EML=g2.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
        g2.EML=g2.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#0000FF\" " +
            "type=\"line\" width=\""+Round(Math.log(ms.bAvailable)/factor,2)+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle " +
                "smoothed=\"false\"/><y:EdgeLabel>"+Round(ms.bAvailable/factor,0)+"</y:EdgeLabel></y:PolyLineEdge></data>\n");
        g2.EML=g2.EML.concat("<data key=\"d3\"/>\n");
        g2.EML=g2.EML.concat("</edge>\n");
        g2.edges++;
      }
      if(ms.bAttempted>ms.bAvailable){
        g2.EML=g2.EML.concat("<edge id=\"e"+Integer.toString(g.edges+((int)bandwidth/1024))+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
        g.EML=g2.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#FF0000\" " +
            "type=\"line\" width=\""+Round(Math.log(ms.bAttempted-ms.bDelivered)/factor,2)+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle" +
                " smoothed=\"false\"/><y:EdgeLabel>"+Round((ms.bAttempted-ms.bDelivered)/factor,0)+"</y:EdgeLabel></y:PolyLineEdge></data>\n");
        g2.EML=g2.EML.concat("<data key=\"d3\"/>\n");
        g2.EML=g2.EML.concat("</edge>\n");
        g2.edges++;
      }
      
          /* if(mtemp.hoplist.contains(i) & mtemp.hoplist.contains(mskey)){
        if(mtemp.hoplist.indexOf(i)+1==mtemp.hoplist.indexOf(mskey)){
          g.EML=g.EML.concat("<edge id=\"e"+Integer.toString(g.edges)+"\" source=\"n"+Integer.toString(i)+"\" target=\"n"+mskey.toString()+"\">\n");
          g.EML=g.EML.concat("<data key=\"d2\"><y:PolyLineEdge><y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/><y:LineStyle color=\"#0000FF\" type=\"line\" width=\""+4+"\"/><y:Arrows source=\"none\" target=\"standard\"/> <y:BendStyle smoothed=\"false\"/></y:PolyLineEdge></data>\n");
          g.EML=g.EML.concat("<data key=\"d3\"/>\n");
          g.EML=g.EML.concat("</edge>");
          g.edges++;
        }
      }*/
    
    }
    
  }
  public boolean codingValidty(LinkedList<Msg> msgs){
    
    if(msgs.size()==0)
      return false;
    Msg mtemp = msgs.get(0);
   Hashtable<String, String>reqfrags = new Hashtable<String, String>();
   for(int i=0;i<mtemp.frag_count;i++)
     reqfrags.put(Integer.toString(i), Integer.toString(i));
   
   
   for(int i=0;i<msgs.size();i++){
     System.out.println(msgs.get(i).frag_id);
     if(msgs.get(i).cFrags.length>1)
       continue;
     reqfrags.remove(msgs.get(i).cFrags[0]);
   }
   if(reqfrags.size()==0)
     return true;
   Msg mtemp2;
   for(int i=0;i<msgs.size();i++){
     mtemp = msgs.get(i);
     if(mtemp.cFrags.length<3) 
       continue;
     for(int j=0;j!=i & j<msgs.size();j++){
       mtemp2 = msgs.get(j);
       if(mtemp2.cFrags.length<3)
         continue;
       if(Math.abs(mtemp2.cFrags.length-mtemp.cFrags.length)>1)
         continue;
       int valid_len = Math.min(mtemp.cFrags.length, mtemp2.cFrags.length);
       int count=0;
       int k=0,l=0;
       int []diff = new int[2];
       for( k=0;k<valid_len;k++){         
         for( l=0;l<valid_len;l++){           
           if(mtemp.cFrags[k]!=mtemp2.cFrags[l]){
             diff[0] = k;
             diff[1] = l;
             count++;           
           }
         } 
       }
       if(count>1)
         continue;
       if(mtemp2.cFrags.length==mtemp.cFrags.length){
         Msg mtemp3 = new Msg(mtemp);
         mtemp3.cFrags = new String[2];
         mtemp3.cFrags[0] = mtemp.cFrags[k];
         mtemp3.cFrags[1] = mtemp2.cFrags[l];
         mtemp3.frag_id = mtemp3.cFrags[0]+" "+mtemp3.cFrags[1];
       }
       else{
         Msg mtemp3 = mtemp.cFrags.length>mtemp2.cFrags.length ? mtemp:mtemp2;

         if(count==0)         
           reqfrags.remove(mtemp3.cFrags[mtemp.cFrags.length-1]);
         else{
           if(mtemp3.equals(mtemp))
               reqfrags.remove(mtemp3.cFrags[k]);
           else
             reqfrags.remove(mtemp3.cFrags[l]);                               
         }
         
       }
     }      
   }
 
   
   boolean resolved=true ;
   int count=1;
   while(reqfrags.size()>0 & resolved){
      count++;
      resolved=false;
     for(int i=0;i<msgs.size();i++){
       mtemp = msgs.get(i);
       if(mtemp.cFrags.length==count) //encoded message
         resolved=resolve(mtemp, msgs, reqfrags);
     }
   }

   if(reqfrags.size()==0)
     return true;
   return false;
  }
  public boolean resolve(Msg mtemp, LinkedList<Msg>msgs,Hashtable<String,String>reqFrags){
    int count = 0;
    
    for(int i=0;i<msgs.size();i++){
      Msg mtemp2 = msgs.get(i);
      if(mtemp2.cFrags.length!=1)
        continue;
      for(int j=0;j<mtemp.cFrags.length;j++){
        if(mtemp2.cFrags[0].equals(mtemp.cFrags[j])){
          mtemp.cFrags[j]= "";
          count++;
        }
      }
    }
    if(mtemp.cFrags.length-count==0)
      return true;
    if(mtemp.cFrags.length-count==1){
      String resolved= new String("");
      //count=0;
      for(int i=0;i<mtemp.cFrags.length;i++){
        if(mtemp.cFrags[i].length()!=0){
          resolved=mtemp.cFrags[i];
          break;
        }
        
      }
      mtemp.cFrags[0] = resolved;
      System.out.println(reqFrags.remove(mtemp.cFrags[0]));
      mtemp.frag_id = mtemp.cFrags[0];
     return true;      
    }
    
    return false;
  }
  public void write_replresults(String fname) {

    Msg mtemp = new Msg(0,0,0,0,0,0,0,0) ;
    try {
      DataOutputStream out1;
      if (seq.equals("0")){
        out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname+".txt")); // Continue to read lines while
        
      }
      else{
        out1 = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname+".txt")); // Continue t        
        out1.writeBytes(Integer.toString(msgcount) + " "
          + Integer.toString(nodecount) + "\n");
      }
      Enumeration<Integer> gmid = gMsgTable.keys();
      
      while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        //LinkedList<Msg> r_msg = new LinkedList<Msg>();
      
        
        while(gcid.hasMoreElements()){       
          mtemp = gMsgTable.get(mid).get(gcid.nextElement()).get("0");
          if(mtemp!=null)
            break;
        }
        if(gMsgTable.get(mid).containsKey(mtemp.dest))
              mtemp = gMsgTable.get(mid).get(mtemp.dest).get("0");
          //break;
        
         /* if(r_msg.size()>kfact-1){
          Collections.sort(r_msg, new DeltimeComparator());
          mtemp = r_msg.get(kfact - 1);
        }
        else
          mtemp = r_msg.get(0);
        if(fragfactor>0){
          mtemp=r_msg.get((int)mtemp.size/fragfactor+(mtemp.size%fragfactor>1 ? 1:0));
        }*/
        out1
            .writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                + "\t"
                + Double.toString(mtemp.size)
                + "\t"
                + Double.toString(Round((double) (mtemp.del_time.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2))
                + "\t" +(mtemp.hoplist.size() - 1)+"\t");
        //if(mtemp.size==1)
          for(int i=1;i<mtemp.hoplist.size();i++)
            out1.writeBytes(mtemp.hoplist.get(i)+"\t");
        out1.writeBytes("\n");
        
      }
      out1.writeBytes("\n");
      System.out.println("Multiple copy Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    writeDevstat(fname);
    writecognitivecongestion(fname);
  }

  // writes the paths taken by different copies of same message
  public void write_replpath(String fname) {

    Iterator git = gMsgTable.values().iterator();
    Msg mtemp, mtemp2;
    try {
      DataOutputStream out;
      if (seq.equals("0"))
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname)); // Continue to read lines while
      else
        out = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname)); // Continue t
      out.writeBytes(Integer.toString(msgcount) + " "
          + Integer.toString(nodecount) + "\n");
      while (git.hasNext()) {
        mtemp = (Msg) git.next();
        if (mtemp.ID.doubleValue() != mtemp.ID.intValue())
          continue;
        Iterator git2 = gMsgTable.values().iterator();
        LinkedList<Msg> r_msg = new LinkedList<Msg>();
        while (git2.hasNext()) {
          mtemp2 = (Msg) git2.next();
          if (mtemp.ID.intValue() == mtemp2.ID.intValue())
            r_msg.add(mtemp2);
        }
          
        int count3 = 0;
        // Collections.sort(r_msg, new DeltimeComparator());
        for (int l = 0; l < r_msg.size() - 1; l++) {
          mtemp = r_msg.get(l);
          int hlistsize = mtemp.hoplist.getLast()!=mtemp.dest  ? mtemp.hoplist.size(): mtemp.hoplist.size()-1;
          int k = l + 1;
          for (; k < r_msg.size(); k++) {
            mtemp2 = r_msg.get(k);
            int m = 1;
       
            for (; m < hlistsize; m++) {
              if (mtemp2.hoplist.contains(mtemp.hoplist.get(m))) 
                break;              
            }
            if (m != hlistsize) 
              break;            
          }
          if (k == r_msg.size() ) {
            count3++;
          }
        }
        out.writeBytes(mtemp.ID.toString() + "\t" + r_msg.size() + "\t" + count3 + "\n");
      }

      out.writeBytes("\n");
      System.out.println("Disjoint path analysis written");
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }
  public void writeDevstat(String fname){
    try {
      DataOutputStream out;
     // if (seq.equals("0"))
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/DSTAT/DStat"+seq
            + fname+".txt")); // Continue to read lines while
      //else
      //  out = new DataOutputStream(new FileOutputStream("multiresult/" + trace
       //     + "/DStat" + seq + fname)); // Continue to read lines while
      for(int i=0;i<tabdevList.size()-ap_count;i++){
        Device dtemp = Peerstable.get(tabdevList.get(i));
        out.writeBytes(Integer.toString(i)+"\t"+Double.toString(dtemp.rbytes)+"\t"+Double.toString(dtemp.dbytes)+"\t"+
            Double.toString(dtemp.hbytes)+"\t"+Double.toString(dtemp.tbytes)+"\t"+Integer.toString(dtemp.dmsgs)+"\t"+
            Integer.toString(dtemp.channelFail)+"\n");
      }
      out.close();
      System.out.println("Device Statistics written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }

  }
  public void write_results(String fname) {

    Enumeration<Integer> gmid = gMsgTable.keys();
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0);
    try {
      DataOutputStream out;
      if (seq.equals("0"))
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname+".txt")); // Continue to read lines while
      else
        out = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname)); // Continue to read lines while
      out.writeBytes(Integer.toString(msgcount) + " "
          + Integer.toString(nodecount) + "\n");
      while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        while(gcid.hasMoreElements()){
          Integer cid = gcid.nextElement();
          mtemp = gMsgTable.get(mid).get(cid).get("0");
          if(gMsgTable.get(mtemp.ID).containsKey(mtemp.dest))
            mtemp=gMsgTable.get(mtemp.ID).get(mtemp.dest).get("0");
        }        
        out
            .writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                + "\t"
                + Double.toString(mtemp.realSize)
                + "\t"
                + Double.toString(Round((double) (mtemp.del_time.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2)) + "\t" + (mtemp.hoplist.size() - 1)
                + "\n");
      }      
      out.writeBytes("\n");
      System.out.println("Single copy Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    writeDevstat(fname);
  }
  public void write_resultsMF(String fname) {

    Enumeration<Integer> gmid = gMsgTable.keys();
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0);
    double bytes = 0;
    int count=0;
    try {
      DataOutputStream out;
      if (seq.equals("0"))
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname+".txt")); // Continue to read lines while
      else
        out = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname)); // Continue to read lines while
      out.writeBytes(Integer.toString(msgcount) + " "
          + Integer.toString(nodecount) + "\n");
      while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        while(gcid.hasMoreElements()){
          Integer cid = gcid.nextElement();
          mtemp = gMsgTable.get(mid).get(cid).get("0");
          if(gMsgTable.get(mtemp.ID).containsKey(mtemp.dest))
            mtemp=gMsgTable.get(mtemp.ID).get(mtemp.dest).get("0");
        }        
        out.writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                + "\t"
                + Double.toString(mtemp.realSize)
                + "\t"
                + Double.toString(Round((double) (mtemp.del_time.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2)) 
                + "\t" +Double.toString(Round((double) (mtemp.rTime.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2))
                +"\t"+(mtemp.hoplist.size() - 1)
                + "\n");
        
        if(!mtemp.del_time.after(mtemp.m_interval.life_time.timemax)){
          count++;
          bytes += mtemp.realSize;
        }
      }      
      out.writeBytes(Double.toString(bytes)+"\t"+Integer.toString(count)+"\n");
      System.out.println("Single copy Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }
    writeDevstat(fname);
  }
  public void write_resultsStat(String fname) {

    Enumeration<Integer> gmid = gMsgTable.keys();
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0);
    double bytes = 0;
    int count=0;  
    FileLock lock = null;
    try {
      
      DataOutputStream out;
      FileOutputStream fout = new FileOutputStream("result/" + trace + "/"+ fname+".txt",true);
      FileChannel fchannel = fout.getChannel();
      lock = fchannel.tryLock();
      if(lock!=null){
        out = new DataOutputStream(fout); // Continue to read lines while
        
        while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        while(gcid.hasMoreElements()){
          Integer cid = gcid.nextElement();
          mtemp = gMsgTable.get(mid).get(cid).get("0");
          if(gMsgTable.get(mtemp.ID).containsKey(mtemp.dest)){
            mtemp=gMsgTable.get(mtemp.ID).get(mtemp.dest).get("0");
            break;
          }
        }        
        out.writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                + "\t"
                + Double.toString(mtemp.size)
                + "\t"
                + Double.toString(Round((double) (mtemp.del_time.getTime() - mtemp.start_time.getTime())/ (1000 * 60),2)) 
                + "\t" +Double.toString(Round((double) ( mtemp.start_time.getTime()-mtemp.rTime.getTime() )/ (1000 * 60),2))
                +"\t"+(mtemp.hoplist.size() - 1)+"\t");
        
          for(int i=0;i<mtemp.hoplist.size();i++)
            out.writeBytes(mtemp.hoplist.get(i) + "\t");
          out.writeBytes("\n");  
          if(!mtemp.del_time.after(mtemp.m_interval.life_time.timemax)){
            count++;
            bytes += mtemp.size;
          }        
        }
      }
      if(lock!=null){
        lock.release();
        fchannel.close();
      }
   
      System.out.println("Stat Output written ");
    }catch (IOException e) {
      System.out.println(e.toString());
    }  
  }
  public void write_Msgresults(String fname) {

    Enumeration<Integer> gmid = gMsgTable.keys();
    Msg mtemp = new Msg(0,0,0,0,0,0,0,0);
    int attempT, compeleteT = 0;
    double attemptB;
    try {
      DataOutputStream out;
      if (seq.equals("0"))
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + fname+"Msg.txt")); // Continue to read lines while
      else
        out = new DataOutputStream(new FileOutputStream("multiresult/" + trace
            + "/" + seq + fname+"Msg.txt")); // Continue to read lines while
      
      while (gmid.hasMoreElements()) {
        Integer mid = gmid.nextElement();
        attempT = compeleteT = 0;
        attemptB = 0;
        Enumeration<Integer>gcid = gMsgTable.get(mid).keys();
        while(gcid.hasMoreElements()){
          Integer cid = gcid.nextElement();
          Enumeration<String>gfid = gMsgTable.get(mid).get(cid).keys(); 
          while(gfid.hasMoreElements()){
            mtemp = gMsgTable.get(mid).get(cid).get(gfid.nextElement());
            attempT += mtemp.attemptT;
            compeleteT += mtemp.completeT;
            attemptB += mtemp.attemptB;
          }         
        }        
        out.writeBytes(mtemp.ID.toString()
                + "\t"
                + mtemp.src
                + "\t"
                + mtemp.dest
                +"\t"
                + Double.toString(Round((double) (mtemp.start_time.getTime()-initialTime.getTime())/ (1000 * 60),2))
                + "\t"
                + Double.toString(mtemp.realSize)
                + "\t"
                + Integer.toString(attempT) + "\t" + (compeleteT)+"\t"+Double.toString(attemptB)+"\n");
      }      
      
      System.out.println("Msg Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }    
  }
  public void writecognitivecongestion(String fname){
    try {
      DataOutputStream out,out2;

        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"+Integer.toString(susers)
            + fname+"DevCF.txt")); // Continue to read lines while
        for(int i=0; i<tabdevList.size()-ap_count;i++)
        {
          out.writeBytes(Integer.toString(i)+"\t"+Integer.toString(Peerstable.get(tabdevList.get(i)).channelFail)+"\n");
        }
        out.close();
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"+Integer.toString(susers)
            + fname+"APCF.txt")); // Continue to read lines while
        
        for(int i=0; i<tabapList.size();i++)
        {
          Apoint aptemp = gApTable.get(tabapList.get(i));
          out2.writeBytes(Integer.toString(i)+"\t"+Integer.toString(aptemp.congestiontimeList.size())+"\t"+
          Integer.toString(aptemp.channelFail)+"\t");
          for(int j=0;j<aptemp.congestiontimeList.size();j++){
            long diff = aptemp.congestiontimeList.get(j).timemax.getTime()-aptemp.congestiontimeList.get(j).timemin.getTime();
            
            out2.writeBytes(Integer.toString(aptemp.congestiontimeList.get(j).channelFail)
                +"\t"+Double.toString(Round((double) (diff)/ (1000 * 60),2)));
          }
          out2.writeBytes("\n");
        }
        out2.close();
        System.out.println("Congestion Output written ");
    } catch (IOException e) {
      System.out.println(e.toString());
    }    
  }

  public void calcnodeaptimedist() {

    Iterator dit = Peerstable.values().iterator();
    Device dtemp;
    Apoint aptemp;
    TSlot ttemp;
    Hashtable <String,Apoint>Aptable = new Hashtable<String, Apoint>();
    Hashtable <String,Float>Monthtable = new Hashtable<String, Float>();
    Calendar c1 = Calendar.getInstance();
    String highMonth = new String("");
    String[] monthName = {"January", "February",
            "March", "April", "May", "June", "July",
             "August", "September", "October", "November",   "December"};
    LinkedList<Device> devsorted = new LinkedList<Device>();
    try {
      DataOutputStream out3 = new DataOutputStream(new FileOutputStream(
          "resource/" + trace + "/Dev list"));
      float sessionlen;

      while (dit.hasNext()) {
        dtemp = (Device) dit.next();

        Iterator apit = dtemp.Aptable.values().iterator();

        while (apit.hasNext()) {          
          aptemp = (Apoint) apit.next();
          if(!Aptable.containsKey(aptemp.Apointname))
            Aptable.put(aptemp.Apointname,aptemp);
          aptemp.tdevcontime = 0;
          ListIterator cit = aptemp.contacttimeList.listIterator();
          while (cit.hasNext()) {
            ttemp = (TSlot) cit.next();
            c1.setTime(ttemp.timemin);
            if(!Monthtable.containsKey(monthName[c1.get(Calendar.MONTH)]+Integer.toString(c1.get(Calendar.YEAR))))
              Monthtable.put(monthName[c1.get(Calendar.MONTH)]+Integer.toString(c1.get(Calendar.YEAR)),new Float(0));
            
            sessionlen = ((float) (ttemp.timemax.getTime() - ttemp.timemin.getTime()))
                / (float)(1000 * 60);
            Monthtable.put(monthName[c1.get(Calendar.MONTH)]+Integer.toString(c1.get(Calendar.YEAR)),
                Monthtable.get(monthName[c1.get(Calendar.MONTH)]+Integer.toString(c1.get(Calendar.YEAR)))+sessionlen);            
            aptemp.tdevcontime += sessionlen;
          }
          dtemp.tonlinetime += aptemp.tdevcontime;
        }
        devsorted.add(dtemp);
      }
      Collections.sort(devsorted, new ConntimeComparator());
      ListIterator dit2 = devsorted.listIterator();
      while (dit2.hasNext()) {
        dtemp = (Device) dit2.next();
        /*if(dtemp.Aptable.size()<3)
          continue;*/

        out3.writeBytes(dtemp.mac + "\n");// +"
        // "+Float.toString(dtemp.tonlinetime)+"\n");
      }
      out3.close();
      
      out3 = new DataOutputStream(new FileOutputStream(
          "resource/" + trace + "/Ap list"));
      Enumeration<String> apenum =Aptable.keys();
      while(apenum.hasMoreElements()){
        out3.writeBytes(apenum.nextElement().toString() + "\n");// +"
      }
      out3.close();
      sessionlen =0;
      
      Enumeration<String>menum = Monthtable.keys();
      while(menum.hasMoreElements()){
        String month = menum.nextElement();
        System.out.println(month+" "+Monthtable.get(month));
        if(Monthtable.get(month)>sessionlen){
          sessionlen= Monthtable.get(month);
          highMonth = month;          
        }
      }
      System.out.println("AP DEV file written "+ highMonth);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void calc_peers(Date timeLimit, long week) {

  //  if(week<5){
    //  System.out.println(timeLimit.toString()+" "+week);
     // return;
    //}
    Device dtemp, dtemp2;
    TSlot ttemp, ttemp2;
    for (int i = 0; i < tabdevList.size() - ap_count; i++){
      dtemp = Peerstable.get(tabdevList.get(i));
      dtemp.meetingcount = new int[tabdevList.size() - ap_count];
      dtemp.contactperTimeslot = new int[dtemp.sorted_slots.size()];

    }
    System.out.println(Double.toString((finalTime.getTime()-initialTime.getTime())/(double)(1000*60*60*24)));
    int nettotalmeeting=0;
    long netcontacttime=0;
    int netdistintmeeting=0;
    int maxcontactsize=0;;
    try {
      DataOutputStream out1 = new DataOutputStream(new FileOutputStream(
          "result/" + trace + "/NodeWiseSummary"+Long.toString(week)+".txt"));
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream(
          "result/" + trace + "/Node2NodeSummary"+Long.toString(week)+".txt"));

      for (int i = 0; i < tabdevList.size() - ap_count; i++) {
        dtemp = Peerstable.get(tabdevList.get(i));
        dtemp.tonlinetime = 0;
        int list_size1 = dtemp.sorted_slots.size();
        for (int j = 0 ; j < list_size1; j++) {
            ttemp = (TSlot) dtemp.sorted_slots.get(j);
            if(ttemp.timemin.after(timeLimit))
              break;
            for (int k = i + 1; k < tabdevList.size() - ap_count; k++) {
              dtemp2 = Peerstable.get(tabdevList.get(k));
              if (!dtemp2.Aptable.containsKey(ttemp.apname))
                continue;
              int list_size2 = dtemp2.sorted_slots.size();
              for (int l = dtemp2.find_slot(ttemp.timemin) ;l>-1 & l < list_size2; l++) {
                  ttemp2 = (TSlot) dtemp2.sorted_slots.get(l);
                if (ttemp.timemax.before(ttemp2.timemin))
                  break;
                if(!ttemp.apname.equals(ttemp2.apname))
                  continue;
                if (!ttemp.isoverlap(ttemp2))
                  continue;
                
                TSlot itime2 = new TSlot(ttemp2.timemin.getTime(),ttemp2.timemax.getTime(),"");
                
                if (ttemp.timemin.after(itime2.timemin)) 
                  itime2.timemin.setTime(ttemp2.timemin.getTime());
                
                if (ttemp.timemax.before(itime2.timemax))
                  itime2.timemax.setTime(ttemp2.timemax.getTime());
                dtemp.tonlinetime += ((double) (itime2.timemax.getTime() - itime2.timemin
                    .getTime())) / (1000 * 60);
                netcontacttime+=(itime2.timemax.getTime() - itime2.timemin.getTime());
                dtemp.meetingcount[k]++;
                dtemp2.meetingcount[i]++;
               
                
                dtemp.contactperTimeslot[dtemp.sorted_slots.indexOf(ttemp)]++;
                dtemp2.contactperTimeslot[dtemp2.sorted_slots.indexOf(ttemp2)]++;
              }        
            }

        }          
        int totalmeeting = 0;
        int distinctmeeting = 0;
        int totalmeetingnew=0;
        int realContactslotsize=0;
        for (int m = 0; m < tabdevList.size() - ap_count; m++) {
          if (dtemp.meetingcount[m] > 0)
            distinctmeeting++;
          totalmeeting += dtemp.meetingcount[m];
          
        }
        nettotalmeeting+=totalmeeting;
        netdistintmeeting+=distinctmeeting;
        
        out1.writeBytes(tabdevList.indexOf(dtemp.mac)+"\t"+Integer.toString(totalmeeting) + "\t"
            + Integer.toString(distinctmeeting) + "\t" + Double.toString(Round(dtemp.tonlinetime,2))+"\t");
        out2.writeBytes(tabdevList.indexOf(dtemp.mac)+"\t");
        for(int m=0;m<dtemp.sorted_slots.size();m++){
          if(dtemp.contactperTimeslot[m]>0)
            realContactslotsize++;
          totalmeetingnew+=dtemp.contactperTimeslot[m];
          if(maxcontactsize<dtemp.contactperTimeslot[m])
            maxcontactsize=dtemp.contactperTimeslot[m];
          out2.writeBytes(dtemp.contactperTimeslot[m]+"\t");
        }
        out1.writeBytes(totalmeetingnew+"\t"+realContactslotsize+"\n");
        out2.writeBytes("\n");
      
      }
      out1.close();
      out2.close();
      System.out.println("Maxcontact size="+maxcontactsize+" avg distinct meeting="+Double.toString(netdistintmeeting/(tabdevList.size()-ap_count))+
          " Avg meet duration"+Double.toString(netcontacttime/nettotalmeeting)+" total meeting= "+Double.toString(nettotalmeeting/(tabdevList.size()-ap_count)));
      System.out.println("NodeWise Summary previously summary3 written");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void convertSQL(){
    String line ="";
    int i=0;
    BufferedReader in;
    try{
      Peerstable = new Hashtable<String,Device>(); 
      in = new BufferedReader(new FileReader("isf.txt")); // Continue to read lines while*/
         
       while(in.ready()){
           line = in.readLine();
           String []frags1=line.split(",");
           
           Device dtemp = new Device(frags1[8].substring(2,frags1[8].length()-1));
           Apoint aptemp = new Apoint(frags1[9].substring(2,frags1[9].length()-1));
           TSlot ttemp = new TSlot(frags1[11].substring(2,frags1[11].length()-1),frags1[12].substring(2,frags1[12].length()-1));
           
           if(Peerstable.containsKey(dtemp.mac)){
             if(Peerstable.get(dtemp.mac).Aptable.containsKey(aptemp.Apointname))
               Peerstable.get(dtemp.mac).Aptable.get(aptemp.Apointname).contacttimeList.add(ttemp);
             else{
               aptemp.contacttimeList.add(ttemp);
               Peerstable.get(dtemp.mac).Aptable.put(aptemp.Apointname,aptemp);             
             }
           }
           else
           {
             aptemp.contacttimeList.add(ttemp);
             dtemp.Aptable.put(aptemp.Apointname,aptemp);
             Peerstable.put(dtemp.mac,dtemp);             
           }               
         }
         in.close();
         System.out.println(bandwidth+" SQL file reading done");
       
    }catch (Exception e) {
         System.err.println(i+" "+e.getMessage()+" "+e.toString());
         e.printStackTrace();
     }     
 }
  public void writeresultxml(){
    
    Device dtemp;
    Apoint aptemp;
    TSlot ttemp;
    ListIterator contactit;
    Calendar c1 = Calendar.getInstance();
    int dindex=1,apindex=1;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try{
      FileOutputStream fostream = new FileOutputStream("resource/"+trace+"/analysedfinal.xml"); // Convert our input stream to a 
      // DataInputStream 
      DataOutputStream out = new DataOutputStream(fostream); // Continue to read lines while 
      // there are still some left to read
      out.writeBytes("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
      out.writeBytes("<DeviceList>\n");
        for (Enumeration e=Peerstable.elements(); e.hasMoreElements();)
        {
          dtemp = (Device)e.nextElement();
          
          //out.writeBytes("\t<Device id=\""+Integer.toString(dindex)+"\">\n");
          out.writeBytes("\t<Device id=\""+dtemp.mac+"\">\n");
          out.writeBytes("\t\t<APlist>\n");
          for(Enumeration f=dtemp.Aptable.elements(); f.hasMoreElements();){
            aptemp = (Apoint)f.nextElement();
            if(aptemp.contacttimeList.size()<1)
              continue;
            out.writeBytes("\t\t\t<Ap id=\""+aptemp.Apointname+"\">\n");
            out.writeBytes("\t\t\t\t\t\t<ContactTimeList>\n");
            contactit = aptemp.contacttimeList.listIterator();
            while(contactit.hasNext()){
              ttemp = (TSlot)contactit.next();    
              c1.setTime(ttemp.timemin);
              if(c1.get(Calendar.MONTH)==3& c1.get(Calendar.YEAR)==2007)
                out.writeBytes("\t\t\t\t\t<ContactTime Start=\""+format.format(ttemp.timemin)+"\" End=\""+format.format(ttemp.timemax)+"\"/>\n");
              
            }
            out.writeBytes("\t\t\t\t</ContactTimeList>\n");
            out.writeBytes("\t\t\t</Ap>\n");
            apindex++;          
          }
          out.writeBytes("\t\t</APlist>\n");
          out.writeBytes("\t</Device>\n");
          dindex++;
        }
        out.writeBytes("</DeviceList>\n");
      out.close();
    }
    catch(Exception e){
      System.err.println("Unable to write the end file "+e.toString() + e.getMessage());
    }
    System.out.println("XML Data writing complete");
    }

  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
  }
  

  public class DeltimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((Msg) o1).del_time.compareTo(((Msg) o2).del_time);
    }
  }

  public class ConntimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      if (((Device) o2).tonlinetime > ((Device) o1).tonlinetime)
        return 1;
      if (((Device) o2).tonlinetime < ((Device) o1).tonlinetime)
        return -1;

      return 0;
    }
  }

}
