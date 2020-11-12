
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.BloomValue;

public class Msg {

  public Integer ID;
  
  public Integer custodian;

  public int src;

  public int dest;

  public int grp_semantic;

  public Date del_time;

  public Date start_time;

  public Mem_Semantic m_interval;

  public int repCount;
  
  public boolean replicate;

  public boolean born;

  public boolean delivered;
  
  public LinkedList<Integer> hoplist;
  
  public HashMap<Integer, Integer>BList;
 // public LinkedList<Integer> cloneHoplist;

  public LinkedList<Integer> nhoplist;

  //public LinkedList<BloomValue> hopProxlist;
  
  public Hashtable<Integer, PossibleHop> mynhoplist;
  

  public Hashtable<String, Double> Ptrans;

  public DijkstraQ nhop;

  public double size;
  
  public double realSize;
  
  public double validSize;
  
  public double value; 

  public Integer hindex[];
  
  public int rfact;
  
  public int kfact;  
  
  public HashMap<Integer, Integer>PeerCarry;  

  public Integer frag_count;
  
  public String frag_id;  

  public Date ArrivalTime;
  
  public String []cFrags;

  public boolean alive;
  
  public boolean fragmented;
  
  public int codeCount;
  
  public NodeList cNode;
  
  public LinkedList<MsgFrag> fragList;
  
  public Date rTime;
  
  public boolean update;
  
  public LinkedList<Integer>bannedHops;
  
  public double probab;
  
  public int desRcount;
  
  public int maxPath;
  
  public double div;
  
  public double divRatio;
  
  public int tag;
  public int tag2;
  
  public boolean reconst;
  
  public int attemptT;
  
  public int completeT;
  
  public double attemptB;
  
  public double completeB;
  
  public double current_centrality;
  
  
  public Msg(int s, int d, int gsem, long g_time, long l_time, int rf, int ecf,
      double siz) {
    tag=0;
    tag2=0;
    divRatio = 1.2;
    ID = new Integer(count++);
    src = s;
    dest = d;
    custodian = new Integer(s);
    m_interval = new Mem_Semantic(g_time, l_time);
    grp_semantic = gsem;
    del_time = new Date();
    start_time = new Date();
    start_time.setTime(g_time);
    rTime = new Date();
    rTime.setTime(g_time);
    // cur_time= m_interval.life_time.timemin;
    // r_factor=rf;
    // k_factor=ecf;
    hoplist = new LinkedList<Integer>();
   // cloneHoplist = new LinkedList<Integer>();
    nhoplist = new LinkedList<Integer>();
   // hopProxlist = new LinkedList<BloomValue>();
    bannedHops = new LinkedList<Integer>();
    
    ArrivalTime = new Date();   
    size = siz;
    validSize = siz;
    born = false;
    Ptrans = new Hashtable<String, Double>();
    BList = new HashMap<Integer, Integer>();
    replicate = false;
    
    delivered=false;
   
    rfact=1;
    kfact=1;
    value = Double.MAX_VALUE;
    realSize=size;
    alive=false;
    frag_count = new Integer(1);
    frag_id = new String("0");
    cFrags = new String[1];
    cFrags[0]=frag_id;
    //coded = false;
    codeCount=0;
    repCount = 0;
    update = true;
    fragmented = false;
    mynhoplist = new Hashtable<Integer, PossibleHop>();
    fragList = new LinkedList<MsgFrag>();
    fragList.add(new MsgFrag(1,size));
    probab = 1;
    desRcount = 1;
    attemptT = completeT = 0;
    attemptB = completeB = 0;
  }

  public Msg(Msg m) {
    tag = 0;
    this.tag2=0;
    divRatio = (m.hoplist.size())*m.divRatio;
    this.src = m.src;
    this.dest = m.dest;
    this.value = Double.MAX_VALUE;
    this.custodian = new Integer(m.hoplist.getLast());
    this.m_interval = new Mem_Semantic(m.m_interval.life_time);
    this.m_interval.life_time.timemin.setTime(this.m_interval.life_time.timemin
        .getTime() );
    rTime = new Date();
    rTime.setTime(this.m_interval.life_time.timemin.getTime()-1000);
    this.grp_semantic = m.grp_semantic;
    this.del_time = new Date();
    this.start_time = new Date();
    this.start_time.setTime(m.start_time.getTime());
    // this.cur_time = new Date();
    // this.cur_time.setTime(m.m_interval.life_time.timemin.getTime());
    this.hoplist = new LinkedList<Integer>();
    //cloneHoplist = new LinkedList<Integer>();
    //this.hopProxlist = new LinkedList<BloomValue>();
    // this.r_factor=m.r_factor;
    // this.k_factor=m.k_factor;
    this.size = m.size;
    this.validSize = m.size;
    for (int i = 0; i < m.hoplist.size(); i++)
      this.hoplist.add(new Integer(m.hoplist.get(i)));
    //for (int i = 0; i < m.hopProxlist.size(); i++)      
    //  this.hopProxlist.add(new BloomValue(m.hopProxlist.get(i)));
    //for (int i = 0; i < m.cloneHoplist.size(); i++)      
    //  this.cloneHoplist.add(m.cloneHoplist.get(i));
    this.bannedHops = new LinkedList<Integer>();
  
    
    this.BList = m.BList;
    this.ArrivalTime = new Date();
    this.nhoplist = new LinkedList<Integer>();
    this.ID = m.ID;
    this.realSize = m.realSize;
    this.hindex = m.hindex;
    Ptrans = new Hashtable<String, Double>();
    replicate = false;
    born=true;
    delivered=false;
    this.rfact=1;
    this.kfact=1;
    this.frag_count = m.frag_count;
    
    update = true;
   
    //if(m.cFrags!=null){
     // this.coded=true;
      this.cFrags=new String[m.cFrags.length];
      for(int i=0;i<m.cFrags.length;i++)
        this.cFrags[i]=m.cFrags[i];
    //}
    //else
      //this.codeCount=m.codeCount;
      codeCount=0;
    this.frag_id= new String(m.frag_id);
    this.alive=true;
    this.cNode = m.cNode;
    
    this.mynhoplist = new Hashtable<Integer, PossibleHop>();
    /*for(int i=0;i<m.bannedHops.size();i++){
      this.bannedHops.add(m.bannedHops.get(i));
    }*/
    fragList = new LinkedList<MsgFrag>();
    
    probab =1;
    this.repCount=0;
    desRcount = 1;
    attemptT = completeT = 0;
    attemptB = completeB = 0;
  
   /* if(m.MaxHopLimit!=null)
      this.MaxHopLimit = new Integer(m.MaxHopLimit);*/
  }
   

  public static Integer count = new Integer(0);

  public int msg_status(TSlot T) {

    if(m_interval.life_time.timemax.before(m_interval.life_time.timemin))
        return -1;
    if (m_interval.life_time.timemax.before(T.timemin))
      return -1;
    // if(m_interval.life_time.timemin.after(T.timemax))
    // return 0;
    if (m_interval.life_time.isoverlap(T))
      return 1;
    return 0;
  }

}
