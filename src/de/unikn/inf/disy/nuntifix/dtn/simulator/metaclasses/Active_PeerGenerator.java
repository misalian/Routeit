
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Date;

public class Active_PeerGenerator {

  Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  LinkedList<Device> active_peers;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Hashtable<String, Apoint> gAptable;

  public Active_PeerGenerator(Hashtable<String, Device> p,
      LinkedList<String> td,   Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm) {

   /* Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
  }

  public void set_aptable(Hashtable<String, Apoint> aptab) {

    gAptable = new Hashtable<String, Apoint>();
    gAptable = aptab;
  }

  
  // obtains all devices hat are active at particular location at given time
 public int[] GetactivePeers(TSlot pttemp, int ap_count) {

    ListIterator dit = tabdevList.listIterator();
    active_peers = new LinkedList<Device>();
    while (dit.hasNext()) {
      Device dtemp = Peerstable.get(dit.next());
      if (dit.nextIndex() > tabdevList.size() - ap_count)
        break;
      if (!dtemp.Aptable.containsKey(pttemp.apname))
        continue;

      dtemp.active_slots = new boolean[dtemp.sorted_slots.size()];

      int lslot = dtemp.find_slot(pttemp.timemax, 0,
          dtemp.sorted_slots.size() - 1);
      for (int i = dtemp.find_slot(pttemp.timemin, 0,
          dtemp.sorted_slots.size() - 1); i > -1 & i <= lslot; i++) {
        TSlot ttemp = dtemp.sorted_slots.get(i);

        if (ttemp.isoverlap(pttemp) & ttemp.apname.equals(pttemp.apname)) {

          dtemp.active_slots[dtemp.sorted_slots.indexOf(ttemp)] = true;
          Device dtemp2 = new Device(dtemp.mac);
          dtemp2.sorted_slots.add(ttemp);
          active_peers.add(dtemp2);
        }
      }
    }
    Collections.sort(active_peers, new ApptimeComparator());
    int[] sorted_list = new int[active_peers.size()];
    for (int i = 0; i < active_peers.size(); i++)
      sorted_list[i] = tabdevList.indexOf(active_peers.get(i).mac);
    return sorted_list;
  }

  // gets the device according to precalulated path of the message
  public int[] GetNexthop(TSlot pttemp, Msg mtemp, int ap_count) {

    Device dtemp = new Device("Null");
    active_peers = new LinkedList<Device>();
    if(mtemp.nhoplist.size()>0){
      for(int i=0;i<mtemp.nhoplist.size();i++){
      dtemp = Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i)));
      if (dtemp.Aptable.containsKey(pttemp.apname))
        findslots(dtemp, pttemp);
      }
      
    }
    /*if (mtemp.nhoplist.getFirst()!=mtemp.dest)
      if(Peerstable.get(tabdevList.get(mtemp.dest)).Aptable.containsKey(pttemp.apname))
        findslots(Peerstable.get(tabdevList.get(mtemp.dest)), pttemp);
*/
   
    return sortpeers();
  }
  public int[] GetStrictNexthop(TSlot pttemp, Msg mtemp, int ap_count) {

    Device dtemp = new Device("Null");
    active_peers = new LinkedList<Device>();
    if(mtemp.nhoplist.size()>0){
     
      dtemp = Peerstable.get(tabdevList.get(mtemp.nhoplist.getFirst()));
      if (dtemp.Aptable.containsKey(pttemp.apname))
        findslots(dtemp, pttemp);      
    }
    return sortpeers();
  }
  public void findslots(Device dtemp, TSlot pttemp){
    
    dtemp.active_slots = new boolean[dtemp.sorted_slots.size()];
   
    int lslot = dtemp.find_slot(pttemp.timemax, 0,
        dtemp.sorted_slots.size() - 1);
    if (lslot == -1)
      lslot = dtemp.sorted_slots.size() - 1;

    for (int i = dtemp.find_slot(pttemp.timemin, 0,
        dtemp.sorted_slots.size() - 1); i > -1 & i <= lslot; i++) {
      TSlot ttemp = dtemp.sorted_slots.get(i);

      if (ttemp.isoverlap(pttemp) & ttemp.apname.equals(pttemp.apname)) {
        dtemp.active_slots[dtemp.sorted_slots.indexOf(ttemp)] = true;
        Device dtemp2 = new Device(dtemp.mac);
        dtemp2.sorted_slots.add(ttemp);
        active_peers.add(dtemp2);
      }
    } 
  }
  public int[] sortpeers(){
    if (active_peers.size() > 1)
      Collections.sort(active_peers, new ApptimeComparator());

    int[] sorted_list = new int[active_peers.size()];
    
    for (int i = 0; i < active_peers.size(); i++)
      sorted_list[i] = tabdevList.indexOf(active_peers.get(i).mac);
    
    return sorted_list;

  }

  // appears same as getnexthop
 /* public int[] GetNexthop2(TSlot pttemp, Msg mtemp, int ap_count) {

    Device dtemp;
    active_peers = new LinkedList<Device>();
    for (int j = 0; j < 2; j++) {
      if (j == 0) {
        if (mtemp.nhoplist.size() > 0) {
          dtemp = Peerstable.get(tabdevList.get(mtemp.nhoplist.getFirst()));
          if (mtemp.dest == tabdevList.indexOf(dtemp.mac))
            j++;
        } else
          continue;
      } else
        dtemp = Peerstable.get(tabdevList.get(mtemp.dest));

      if (!dtemp.Aptable.containsKey(pttemp.apname))
        continue;
      dtemp.active_slots = new boolean[dtemp.sorted_slots.size()];

      int lslot = dtemp.find_slot(pttemp.timemax, 0,
          dtemp.sorted_slots.size() - 1);
      if (lslot == -1)
        lslot = dtemp.sorted_slots.size() - 1;

      for (int i = dtemp.find_slot(pttemp.timemin, 0,
          dtemp.sorted_slots.size() - 1); i > -1 & i <= lslot; i++) {
        TSlot ttemp = dtemp.sorted_slots.get(i);

        if (ttemp.isoverlap(pttemp) & ttemp.apname.equals(pttemp.apname)) {
          dtemp.active_slots[dtemp.sorted_slots.indexOf(ttemp)] = true;
          Device dtemp2 = new Device(dtemp.mac);
          dtemp2.sorted_slots.add(ttemp);
          active_peers.add(dtemp2);
        }
      }
    }
    if (active_peers.size() > 1)
      Collections.sort(active_peers, new ApptimeComparator());

    int[] sorted_list = new int[active_peers.size()];
    for (int i = 0; i < active_peers.size(); i++)
      sorted_list[i] = tabdevList.indexOf(active_peers.get(i).mac);
    return sorted_list;
  }*/

  // next hop for my algo where it looks in hash table of mtemp and sees whether
  // anyof the next hop options is available
  // or not
  public int[] GetMyNexthop(TSlot pttemp, Msg mtemp, int ap_count) {
  Device dtemp;
   // int[] sorted_list=new int[0];
    active_peers = new LinkedList<Device>();
    if(!mtemp.mynhoplist.containsKey(mtemp.dest))
      mtemp.mynhoplist.put(mtemp.dest,new PossibleHop());
    Enumeration <Integer>denum = mtemp.mynhoplist.keys();
    while (denum.hasMoreElements()) {
      int j = denum.nextElement().intValue();
      dtemp = Peerstable.get(tabdevList.get( j));
      
      if (dtemp.Aptable.containsKey(pttemp.apname))
        findslots(dtemp, pttemp);
    
    }    
    return sortpeers();
  }
  public int[] GetMyCogNexthop(TSlot pttemp, Msg mtemp, int ap_count) {
  Device dtemp;
   // int[] sorted_list=new int[0];
    active_peers = new LinkedList<Device>();
    if(!mtemp.mynhoplist.containsKey(mtemp.dest))
      mtemp.mynhoplist.put(mtemp.dest,new PossibleHop());
    Enumeration <Integer>denum = mtemp.mynhoplist.keys();
    while (denum.hasMoreElements()) {
      int j = denum.nextElement().intValue();
      dtemp = Peerstable.get(tabdevList.get( j));
      
      if (dtemp.Aptable.containsKey(pttemp.apname))
        findslots(dtemp, pttemp);
    
    }    
    return sortpeers();
  }
 
  // gets all the devices that have fullfilled the flooding crieteria to be next
  // hop of the message
  public int[] GetPotenPeers(TSlot pttemp, Msg mtemp,  int ap_count, boolean flood,int k) {

    active_peers = new LinkedList<Device>();
    Apoint aptemp1 = gAptable.get(pttemp.apname);
    if (aptemp1 == null)
      return sortpeers();
    /*if(aptemp1.contacttimeList.getFirst().timemin.after(pttemp.timemin)){
      
        //aptemp1.congestiontimeList.add(new TSlot(st, et))
      Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).channelFail++;
      aptemp1.channelFail++;
      aptemp1.congestiontimeList.getLast().channelFail++;
      return sortpeers();
    }*/
    for (int j = 0; j < aptemp1.devlist.size(); j++) {

      Device dtemp = aptemp1.devlist.get(j);
      if(!flood){
        if (dtemp.if_repmsgexist(mtemp)>-1 )//| mtemp.hoplist.contains(tabdevList.indexOf(dtemp.mac)))
          continue;
        if(mtemp.dest != tabdevList.indexOf(dtemp.mac) && dtemp.centrality[k]<mtemp.current_centrality)
          continue;
      }
   
      if (mtemp.hoplist.getLast() == tabdevList.indexOf(dtemp.mac))
         continue;     
      
      Apoint aptemp = dtemp.Aptable.get(pttemp.apname);
      aptemp.activeslots = new boolean[aptemp.contacttimeList.size()];
      int lslot = dtemp.Aptable.get(pttemp.apname).find_slot(pttemp.timemax);
      if (lslot == -1)
        continue;
      for (int i = aptemp.find_slot(pttemp.timemin); i > -1 & i <= lslot; i++) {
        TSlot ttemp = aptemp.contacttimeList.get(i);
        if(tabdevList.indexOf(dtemp.mac)==277 && mtemp.src==mtemp.hoplist.getLast()){
          System.out.println(dtemp.mac);
          System.out.println(Peerstable.get(tabdevList.get(72)).mac);
          //System.out.println(pttemp.timemin.toString()+" "+pttemp.timemax.toString());
          //System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString());
          System.out.println(pttemp.apname);
          for(int gh=0;gh<dtemp.sorted_slots.size();gh++){
              System.out.println(dtemp.sorted_slots.get(gh).apname+" "+dtemp.sorted_slots.get(gh).timemin.toString()+" "+dtemp.sorted_slots.get(gh).timemax.toString());

          }
        }
         if (ttemp.isoverlap(pttemp)) {
     
          aptemp.activeslots[i] = true;
          Device dtemp2 = new Device(dtemp.mac);
          dtemp2.sorted_slots.add(ttemp);
          active_peers.add(dtemp2);

          /*if (ttemp.timemin.after(pttemp.timemin) & t.after(ttemp.timemin))
            t.setTime(ttemp.timemin.getTime());*/
        }
      }
    }
    
    /*if (active_peers.size() > 1) {
      Collections.sort(active_peers, new ApptimeComparator());
    }

    sorted_list = new int[active_peers.size()];
    for (int i = 0; i < active_peers.size(); i++)
      sorted_list[i] = tabdevList.indexOf(active_peers.get(i).mac);
    return sorted_list;*/
    return sortpeers();
  }

  
  // only for first contact to reduce the hot potatoe effect.
  public int[] GetPoten_FCPeers(TSlot pttemp, Msg mtemp, Date t, int ap_count) {

    // ListIterator dit =tabdevList.listIterator();
    active_peers = new LinkedList<Device>();
    Apoint aptemp1 = gAptable.get(pttemp.apname);
   // int[] sorted_list = new int[0];
    if (aptemp1 == null)
      return sortpeers();
    for (int j = 0; j < aptemp1.devlist.size(); j++) {

      Device dtemp = aptemp1.devlist.get(j);
      int potato_check = mtemp.hoplist.size() * 5 / 100;

      if (mtemp.hoplist.size()
          - mtemp.hoplist.lastIndexOf(tabdevList.indexOf(dtemp.mac)) <= potato_check)
        continue;
      Apoint aptemp = dtemp.Aptable.get(pttemp.apname);
      aptemp.activeslots = new boolean[aptemp.contacttimeList.size()];
      int lslot = dtemp.Aptable.get(pttemp.apname).find_slot(pttemp.timemax);
      if (lslot == -1)
        continue;
      for (int i = aptemp.find_slot(pttemp.timemin); i > -1 & i <= lslot; i++) {
        TSlot ttemp = aptemp.contacttimeList.get(i);
        if (ttemp.isoverlap(pttemp)) {
          aptemp.activeslots[i] = true;
          Device dtemp2 = new Device(dtemp.mac);
          dtemp2.sorted_slots.add(ttemp);
          active_peers.add(dtemp2);

          if (ttemp.timemin.after(pttemp.timemin) & t.after(ttemp.timemin))
            t.setTime(ttemp.timemin.getTime());
        }
      }
    }   
    return sortpeers();
  }

  public Msg getnexthopmsg(Device dtemp, Msg mtemp, TSlot itime, Device dtemp2) {

    Msg mtemp2;
    for (int i = 0; i < dtemp.aliveMsglist.size(); i++) {
      mtemp2 = dtemp.aliveMsglist.get(i);
      if(mtemp2.msg_status(itime)!=1)
        continue;
      if(dtemp2.if_repmsgexist(mtemp2)<0){
        if (mtemp2.dest == tabdevList.indexOf(dtemp2.mac) & !mtemp2.m_interval.life_time.timemin.after(itime.timemin)){
          mtemp2.m_interval.life_time.timemin.setTime(itime.timemin.getTime());
          return mtemp2;
        }
      }
    }
    Collections.sort(dtemp.aliveMsglist, new PathVarianceComparator());
    
    mtemp2=getHighPrioMsg(mtemp, dtemp, dtemp2, itime);
    if(mtemp2!=null)
      return mtemp2;
    Collections.sort(dtemp.aliveMsglist, new LifeTimeComparator());
    mtemp2=getHighPrioMsg(mtemp, dtemp, dtemp2, itime);
    if(mtemp2!=null)
      return mtemp2;
    
    return mtemp;
  }
  public Msg getHighPrioMsg(Msg mtemp, Device dtemp, Device dtemp2,TSlot itime){
    Msg mtemp2 = null;
    int mindex = dtemp.aliveMsglist.indexOf(mtemp);
    for (int i = 0; i < mindex; i++) {
      mtemp2 = dtemp.aliveMsglist.get(i);
      if(mtemp2.ID.equals(mtemp.ID) | mtemp2.msg_status(itime)!=1 | !mtemp2.alive | mtemp2.delivered | dtemp2.if_repmsgexist(mtemp2)>-1)
        continue;     
      if (mtemp2.nhoplist.size() > 0){
        if (mtemp2.nhoplist.getFirst() == tabdevList.indexOf(dtemp2.mac)
            & !mtemp2.m_interval.life_time.timemin.after(itime.timemin)){
            mtemp2.m_interval.life_time.timemin.setTime(itime.timemin.getTime());            
            return mtemp2;                
         }
      }
      else if(mtemp2.mynhoplist!=null){
        if (mtemp2.mynhoplist.containsKey(tabdevList.indexOf(dtemp2.mac))
            & !mtemp2.m_interval.life_time.timemin.after(itime.timemin)){
          mtemp2.m_interval.life_time.timemin.setTime(itime.timemin.getTime());            
          return mtemp2;                
        }
      }
      else if(!mtemp2.m_interval.life_time.timemin.after(itime.timemin)){
          mtemp2.m_interval.life_time.timemin.setTime(itime.timemin.getTime());            
          return mtemp2;                
      }      
    } 
    return null;
  }
  public class ApptimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((Device) o1).sorted_slots.getFirst().timemin
          .compareTo(((Device) o2).sorted_slots.getFirst().timemin);
    }
  }

  public class LifeTimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {
      
      TSlot ttemp1 = ((Msg)o1).m_interval.life_time;
      TSlot ttemp2 = ((Msg)o2).m_interval.life_time;
      return (int)((ttemp1.timemax.getTime()-ttemp1.timemin.getTime())-
      (ttemp2.timemax.getTime()-ttemp2.timemin.getTime()));
    }
  }
  public class PathVarianceComparator implements Comparator {

    public int compare(Object o1, Object o2) {
          
      return (int)(((Msg)o1).tag2-((Msg)o2).tag2);
    }
  }
public void adjustCustody(Msg mtemp,Msg mtemp2, Device dtemp,Device dtemp2){
  dtemp2.hbytes+=mtemp.size;
  MeetingStats ms=dtemp.meetStat.get(tabdevList.indexOf(dtemp2.mac));
  ms.msgsTransf++;
  mtemp.completeT++;
  if(!ms.msgStat.containsKey(mtemp.ID))
    ms.msgStat.put(mtemp.ID, new Hashtable<String, Msg>());
  //if(!ms.msgStat.get(mtemp2.ID).containsKey(mtemp2.frag_id))
  if(ms.msgStat.get(mtemp.ID).put(mtemp.frag_id,mtemp)!=null){
    //System.err.println("Msg stat error 2 "+mtemp.ID+" "+mtemp.frag_id+" "+mtemp.m_interval.life_time.timemin);
  }
   
  if(mtemp2==null){
    if(mtemp.size!=mtemp.realSize & mtemp.realSize!=1&mtemp.kfact==1)
      transferCustody(mtemp, dtemp2);
    else{
      dtemp.aliveMsglist.remove(mtemp);
      if(gMsgTable.get(mtemp.ID).contains(mtemp.custodian)){
      if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).remove(mtemp.frag_id)==null){
        System.err.println("Message not in right custody "+mtemp.ID.toString()+" "+mtemp.custodian+" "+mtemp.m_interval.life_time.timemin);
        System.exit(0);
      }    
      if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).size()==0)
        gMsgTable.get(mtemp.ID).remove(mtemp.custodian); 
      }
      dtemp.aliveMsglist.remove(mtemp);
      transferCustody(mtemp, dtemp2);
    }
   }
   else
      transferCustody(mtemp2, dtemp2);       
  }
  public void transferCustody(Msg mtemp,Device dtemp2){
    mtemp.custodian = new Integer(tabdevList.indexOf(dtemp2.mac));
    mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    dtemp2.aliveMsglist.add(mtemp);
    
    if(!gMsgTable.get(mtemp.ID).containsKey(mtemp.custodian)){
      gMsgTable.get(mtemp.ID).put(mtemp.custodian,new Hashtable<String, Msg>());
      gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);      
    }
    else{        
      if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp)!=null){
        if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).get(mtemp.frag_id).size==gMsgTable.get(mtemp.ID).get(mtemp.custodian).get(mtemp.frag_id).realSize){
        //System.err.println("Message already in  custody "+mtemp.ID.toString()+" "+mtemp.custodian+" "+mtemp.m_interval.life_time.timemin);
        //System.exit(0);      
        }
      }        
    }
  }
  public void recordUpdate(Device dtemp, Device dtemp2, Msg mtemp, double bytesTransferable, VolumeStats vs){
    
    double bytesTransfered = bytesTransferable>mtemp.size ? mtemp.size:bytesTransferable;
    mtemp.attemptB+=bytesTransfered;
    if(vs!=null){
      vs.consumed_bw += bytesTransfered;
      vs.bwchange = true;
    }
    MeetingStats ms1= dtemp.meetStat.get(tabdevList.indexOf(dtemp2.mac));
    
    if(!dtemp.meetStat.containsKey(tabdevList.indexOf(dtemp2.mac))){
      
      System.out.println();
      System.out.println(tabdevList.indexOf(dtemp.mac)+" "+tabdevList.indexOf(dtemp2.mac));
      /*for(int i=0;i<mtemp.nhoplist.size();i++)
        System.out.print(mtemp.nhoplist.get(i)+" ");
      System.out.println();
      for(int i=0;i<mtemp.hoplist.size();i++)
        System.out.print(mtemp.hoplist.get(i)+" ");
      System.out.println();  */
      
    }
    ms1.bAttempted+=mtemp.validSize;
    ms1.bDelivered+= bytesTransfered;
    //ms1.bAvailable+=bytesTransferable;
    
    ms1.msgsAttempt++;
    
    dtemp.tbytes+= bytesTransferable;
    dtemp2.tbytes+=bytesTransferable;
    
    dtemp2.rbytes += bytesTransfered;
    dtemp.dbytes+=bytesTransfered;
    /*if(bytesTransfered>=mtemp.validSize & mtemp.dest!=tabdevList.indexOf(dtemp2.mac))
      dtemp2.hbytes+=mtemp.validSize;*/
    
  }
  public void duplicatePath(Device dtemp, Msg mtemp, Date histT){
    
    for(int i=0;i<dtemp.aliveMsglist.size();i++){
      Msg mtemp2 = dtemp.aliveMsglist.get(i);
      if(mtemp2.equals(mtemp))
        continue;
      if(mtemp2.ID.equals(mtemp.ID) & mtemp2.m_interval.life_time.timemin.getTime()-histT.getTime()<=(1000*60*10)){
        mtemp2.mynhoplist = mtemp.mynhoplist;
        mtemp2.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()+(1000*60*60*24*1));
        mtemp.update=false;
        
      }
    }
  }
}
