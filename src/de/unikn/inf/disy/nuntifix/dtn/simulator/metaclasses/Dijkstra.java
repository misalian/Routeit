
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.io.DataOutputStream;


import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator; 




import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;

public class Dijkstra {

  public Hashtable<String, Device> o_Peerstable;

  // peripheral data holders for indexing of accesspoints, indexing of devices
  // and how many nodes from one access point to another respectivel
  Hashtable<Integer, Float> MeetinProbab;

  LinkedList<String> tabdevList;

  Long Fifteendays;

  public Hashtable<String, Device> Peerstable;

  public Dijkstra(Hashtable p, LinkedList td) {

    o_Peerstable = new Hashtable<String, Device>();
    Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    Peerstable = p;
    tabdevList = td;
    MeetinProbab = new Hashtable<Integer, Float>();
    Fifteendays = new Long(1000 * 60 * 60 * 24 * 15);

  }

  public void cloneall(int ap_count, boolean perfect) {

    for (int i = 0; i < tabdevList.size() - ap_count; i++)
      clonetrace(i, perfect);
  }

  public void clonetrace(int i, boolean perfect) {

    o_Peerstable.put(tabdevList.get(i), new Device(Peerstable.get(tabdevList.get(i))));
    if (perfect)
      o_Peerstable.get(tabdevList.get(i)).create_sortedslots();

  }

  public void Create_Oracletrace(int mob_count) {

    // find who is meeting whom mostly.

    for (int i = 0; i < tabdevList.size() - mob_count; i++)
      o_Peerstable.get(tabdevList.get(i)).probab = new double[tabdevList.size()
          - mob_count];

    for (int i = 0; i < tabdevList.size() - mob_count; i++) {
      // System.out.println("calc" +i);
      Device dtemp = o_Peerstable.get(tabdevList.get(i));
      Iterator apit = dtemp.Aptable.values().iterator();
      while (apit.hasNext()) {
        Apoint aptemp = (Apoint) apit.next();
        for (int j = i + 1; j < tabdevList.size() - mob_count; j++) {
          if (j == i)
            continue;
          Device dtemp2 = o_Peerstable.get(tabdevList.get(j));
          if (!dtemp2.Aptable.containsKey(aptemp.Apointname))
            continue;
          for (int k = 0; k < dtemp.Aptable.get(aptemp.Apointname).contacttimeList
              .size(); k++) {
            TSlot ttemp = dtemp.Aptable.get(aptemp.Apointname).contacttimeList
                .get(k);
            for (int l = 0; l < dtemp2.Aptable.get(aptemp.Apointname).contacttimeList
                .size(); l++) {
              TSlot ttemp2 = dtemp2.Aptable.get(aptemp.Apointname).contacttimeList
                  .get(l);
              if (ttemp2.timemin.after(ttemp.timemax))
                break;
              if (ttemp.isoverlap(ttemp2)) {
                dtemp.probab[j]++;
                dtemp2.probab[i]++;
              }
            }
          }
        }
      }
    }
    Random dice = new Random(tabdevList.size());
    LinkedList swapList = new LinkedList<String>();
    LinkedList manipList = new LinkedList<String>();
    for (int i = 0; i < tabdevList.size() - mob_count; i++) {
      float dice_outcome = dice.nextFloat();
      if (dice_outcome <= 0.333)
        swapList.add(tabdevList.get(i));
      else if (dice_outcome > 0.333 & dice_outcome <= 0.666)
        manipList.add(tabdevList.get(i));
    }
    for (int i = 0; i < swapList.size(); i++) {
      Device dtemp = o_Peerstable.get(swapList.get(i));
      Hashtable taptable = dtemp.Aptable;

      int max = 0;
      int max_index = 0;
      for (int j = 0; j < tabdevList.size() - mob_count; j++) {
        if (dtemp.probab[j] > max) {
          max = (int) dtemp.probab[j];
          max_index = j;
        }
      }
      dtemp.Aptable = o_Peerstable.get(tabdevList.get(max_index)).Aptable;
      o_Peerstable.get(tabdevList.get(max_index)).Aptable = taptable;
      swapList.remove(tabdevList.get(max_index));
    }

    for (int i = 0; i < manipList.size(); i++) {
      Device dtemp = o_Peerstable.get(manipList.get(i));
      Iterator apit = dtemp.Aptable.values().iterator();
      while (apit.hasNext()) {
        Apoint aptemp = (Apoint) apit.next();
        for (int j = 0; j < aptemp.contacttimeList.size(); j++) {
          TSlot ttemp = aptemp.contacttimeList.get(j);
          TSlot ttemp2 = dtemp.getnextslot(ttemp);// aptemp.contacttimeList.get(j+1);
          if (ttemp.equals(ttemp2))
            break;
          long slot1m = (ttemp.timemin.getTime() + ttemp.timemax.getTime()) / 2;
          long slot2m = (ttemp2.timemin.getTime() + ttemp2.timemax.getTime()) / 2;

          long pausem = (ttemp.timemax.getTime() + ttemp2.timemin.getTime()) / 2;
          double random = Math.random();
          if (random < 0.5)
            ttemp.timemax.setTime((long) (slot1m + 2 * random
                * (ttemp.timemax.getTime() - slot1m)));
          else
            ttemp.timemax.setTime((long) (pausem - 2 * (1 - random)
                * (pausem - ttemp.timemax.getTime())));
          // random = Math.random();
          if (random < 0.5)
            ttemp2.timemin.setTime((long) (pausem + 2 * random
                * (ttemp2.timemin.getTime() - pausem)));
          else
            ttemp2.timemin.setTime((long) (slot2m - 2 * (1 - random)
                * (slot2m - ttemp.timemin.getTime())));
        }
      }
    }
    for (int i = 0; i < tabdevList.size() - mob_count; i++)
      o_Peerstable.get(tabdevList.get(i)).create_sortedslots();

    System.out.println("Orace trace created");

  }

  public void mergeslots(TSlot ttemp, TSlot ttemp2) {

    if (ttemp.timemin.before(ttemp2.timemin))
      ttemp2.timemin.setTime(ttemp.timemin.getTime());
    if (ttemp.timemax.after(ttemp2.timemax))
      ttemp2.timemax.setTime(ttemp.timemax.getTime());
  }

  public boolean dijkstra(Msg mtemp, int mob_dcount) {

    Device dtemp, dtemp2;
    DijkstraQ qtemp = null, qtemp2;
    TSlot ttemp, ttemp2;
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();
    // Fill up the Queue
    for (int i = 0; i < tabdevList.size() - mob_dcount; i++){
      if(mtemp.hoplist.contains(i))
          continue;
        queue.add(new DijkstraQ(i));
     }
     queue.add(mtemp.hoplist.getLast(),new DijkstraQ(mtemp.hoplist.getLast()));
     queue.get(mtemp.hoplist.getLast()).cost = 0;
    mtemp.nhoplist = new LinkedList<Integer>();
    
    queue.get(mtemp.hoplist.getLast()).T = mtemp.m_interval.life_time.timemin;


    while (queue.size() > 0) {
      // System.out.println(queue.size());
    //  qtemp3 = qtemp;
      int next = find_mincost(queue, mtemp.dest);
      if (next != -1)
        qtemp = queue.remove(next);
      else {
        mtemp.nhoplist.add(mtemp.dest);
        break;
      }
      /*if (qtemp3 != null)
        qtemp3.set_to(qtemp);*/
      
      if (qtemp.dev == mtemp.dest) {
        
        
        if (qtemp.path.size() == 0) {
          mtemp.nhop = qtemp;
          mtemp.nhop.T.setTime(mtemp.m_interval.life_time.timemax.getTime());
          mtemp.nhoplist.add(mtemp.dest);
          break;
        }      
        
        
        /*qtemp2 = qtemp;
        while (qtemp2.from.dev != mtemp.hoplist.getLast())
          qtemp2 = qtemp2.from;
        */
        for(int j=1;j<qtemp.path.size();j++){
          qtemp2 = qtemp.path.get(j);
          if(j+1<qtemp.path.size())
            qtemp2.to = qtemp.path.get(j+1);
          mtemp.nhoplist.add(qtemp.path.get(j).dev);
        }
        if(qtemp.path.size()>1)
          mtemp.nhop=qtemp.path.get(1);
        mtemp.nhoplist.add(qtemp.dev);
        return true;
      }

      dtemp = o_Peerstable.get(tabdevList.get(qtemp.dev));

      for (int i = dtemp.find_slot(qtemp.T, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < dtemp.sorted_slots.size(); i++) {
        ttemp = dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(mtemp.m_interval.life_time.timemax))
          break;

        // System.out.println(dtemp.mac+" "+ttemp.timemin.toString()+"
        // "+ttemp.timemax.toString());
        
        for (int l=0;l<queue.size();l++) {
          
          dtemp2 = o_Peerstable.get(tabdevList.get(queue.get(l).dev));
          // System.out.println("dtemp2= "+dtemp2.mac);

          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < dtemp2.sorted_slots.size(); j++) {
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (ttemp.timemin.after(mtemp.m_interval.life_time.timemax))
              break;
            if (!ttemp2.apname.equals(ttemp.apname))
              continue;
            
            if (!ttemp.isoverlap(ttemp2))
              continue;
            
            
            TSlot itime = new TSlot(
                ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                    : ttemp.timemin,
                ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax
                    : ttemp2.timemax);
            if (itime.timemin.before(qtemp.T))
              itime.timemin = qtemp.T;
            
            if(!itime.timemin.before(mtemp.m_interval.life_time.timemax))
              continue;
            //itime.timemin.setTime(itime.timemin.getTime()+1000);
            
            double g = ((double) (itime.timemin.getTime() - qtemp.T.getTime())) / (1000 * 60);

            int k = 0;
            while (queue.get(k++).dev != tabdevList.indexOf(dtemp2.mac));
            qtemp2 = queue.get(k - 1);
            if (qtemp2.cost > qtemp.cost + g) {
              qtemp2.cost = qtemp.cost + g;
              qtemp2.T.setTime(itime.timemin.getTime());
              qtemp2.set_path(qtemp.path);
              qtemp2.set_from(qtemp);
              //qtemp.set_to(qtemp2);
              qtemp2.path.add(qtemp);
            }
            break;
          }
        }
      }
    }
    return false;
  }
  
  public boolean dijkstraEPO(Msg mtemp, double bw, int mob_dcount,boolean resume) {

    Device dtemp, dtemp2;
    DijkstraQ qtemp = null, qtemp2;//, qtemp3;
    TSlot ttemp, ttemp2;
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();
    // Fill up the Queue  
    for (int i = 0; i < tabdevList.size() - mob_dcount; i++){
      if(mtemp.hoplist.contains(i))
          continue;
        queue.add(new DijkstraQ(i));
     }
    queue.add(0,new DijkstraQ(mtemp.hoplist.getLast()));
    queue.getFirst().cost = 0;
    queue.getFirst().T = mtemp.m_interval.life_time.timemin;
    mtemp.nhoplist = new LinkedList<Integer>();
    

    while (queue.size() > 0) {
      int next = find_mincost(queue, mtemp.dest);
      if (next != -1)
        qtemp = queue.remove(next);
      else {
        mtemp.nhoplist.add(mtemp.dest);
        break;
      }
      if (qtemp.dev == mtemp.dest) {
        if (qtemp.path.size() == 0) {
          mtemp.nhop = qtemp;
          mtemp.nhop.T.setTime(mtemp.m_interval.life_time.timemax.getTime());
          mtemp.nhoplist.add(mtemp.dest);
          return false;
        }
        long duration = Long.MAX_VALUE;
        for(int j=1;j<qtemp.path.size();j++){
         
          qtemp2 = qtemp.path.get(j);
          if(j+1<qtemp.path.size())
            qtemp2.to = qtemp.path.get(j+1);            
          if(qtemp.path.get(j).duration<duration)
            duration=qtemp.path.get(j).duration;
          mtemp.nhoplist.add(qtemp.path.get(j).dev);
          /*if(qtemp2.from!=null & slotBuffer!=null){
            String key = Integer.toString(qtemp2.from.dev)+" "+Integer.toString(qtemp2.ttemp.hashCode());
           // System.out.println(key+" "+qtemp2.ttemp.timemin+" "+qtemp2.ttemp.timemax);
            if(!slotBuffer.containsKey(key))
              slotBuffer.put(key, new TSlot(qtemp2.ttemp.timemin.getTime(),qtemp2.ttemp.timemax.getTime(),qtemp2.ttemp2.apname));
          }
          if(qtemp2.to!=null& slotBuffer!=null){
            String key = Integer.toString(qtemp2.dev)+" "+Integer.toString(qtemp2.ttemp2.hashCode());
          //  System.out.println(key+" "+qtemp2.ttemp2.timemin+" "+qtemp2.ttemp2.timemax);
            if(!slotBuffer.containsKey(key))
              slotBuffer.put(key, new TSlot(qtemp2.ttemp2.timemin.getTime(),qtemp2.ttemp2.timemax.getTime(),qtemp2.ttemp2.apname));
          } */       
        }
        if(qtemp.path.size()>1){
          mtemp.nhop=qtemp.path.get(1);
          
        }
        if(qtemp.duration<duration)
          duration = qtemp.duration;
        mtemp.size = duration/1000*bw;
        mtemp.nhoplist.add(qtemp.dev);
        
        return true;
      }

      dtemp = Peerstable.get(tabdevList.get(qtemp.dev));

      for (int i = dtemp.find_slot(qtemp.T, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < dtemp.sorted_slots.size(); i++) {
        ttemp = dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(mtemp.m_interval.life_time.timemax))
          break;        
        for (int l=0;l<queue.size();l++) {
          
          dtemp2 = Peerstable.get(tabdevList.get(queue.get(l).dev));
          // System.out.println("dtemp2= "+dtemp2.mac);

          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < dtemp2.sorted_slots.size(); j++) {
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (ttemp.timemin.after(mtemp.m_interval.life_time.timemax))
              break;
            if (!ttemp2.apname.equals(ttemp.apname))
              continue;
            

            if (!ttemp.isoverlap(ttemp2))
              continue;
            
            TSlot itime = new TSlot(
                ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                    : ttemp.timemin,
                ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax
                    : ttemp2.timemax);
            if (itime.timemin.before(qtemp.T))
              itime.timemin = qtemp.T;
            if ((itime.timemax.getTime()-itime.timemin.getTime())<2000)
            continue;
            if(!itime.timemin.before(mtemp.m_interval.life_time.timemax))
              break;
            if(!resume)
              if ((itime.timemax.getTime()-itime.timemin.getTime())*bw/1000<mtemp.size)
                continue;
            
            MeetingStats ms1, ms2;
            
            if (!dtemp.meetStat.containsKey(tabdevList.indexOf(dtemp2.mac))| !dtemp2.meetStat.containsKey(tabdevList.indexOf(dtemp.mac))) {
              ms1 = new MeetingStats( tabdevList.indexOf(dtemp2.mac),tabdevList.indexOf(dtemp.mac));
              ms2 = new MeetingStats(tabdevList.indexOf(dtemp.mac),tabdevList.indexOf(dtemp2.mac));
              /*ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime.timemin));
              ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime.timemin));*/
              ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime.timemin));
              ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime.timemin));
              dtemp.meetStat.put(ms1.devindex, ms1);
              dtemp2.meetStat.put(ms2.devindex, ms2);               
            }
            else{
              ms1 = dtemp.meetStat.get(tabdevList.indexOf(dtemp2.mac));
              ms2 = dtemp2.meetStat.get(tabdevList.indexOf(dtemp.mac));             
            }         
                     
            ms1.meetingcnt++;
            ms2.meetingcnt++;
            /*double duration = (itime.timemax.getTime()-itime.timemin.getTime())/(1000)*bw;
            ms1.duration = duration;
            ms2.duration = duration;
            if(!ttemp.recorded)
            ms1.bAvailable+=duration*bw;
            ms2.bAvailable=ms1.bAvailable*/;       
            double g = ((double) (itime.timemin.getTime() - qtemp.T.getTime()))
                / (1000 * 60);

            int k = 0;

            while (queue.get(k++).dev != tabdevList.indexOf(dtemp2.mac));
            qtemp2 = queue.get(k - 1);
            if (qtemp2.cost > qtemp.cost + g) {
              if(qtemp.T.getTime()+qtemp.duration>itime.timemin.getTime()){
               if(itime.timemin.getTime()-qtemp.T.getTime()<2000)
                 continue;
                qtemp.duration=itime.timemin.getTime()-qtemp.T.getTime();
              }
              qtemp2.duration = itime.timemax.getTime()-itime.timemin.getTime();
              
              
              qtemp2.cost = qtemp.cost + g;              
              qtemp2.T.setTime(itime.timemin.getTime());
              qtemp2.set_path(qtemp.path);
              qtemp2.set_from(qtemp);
              qtemp2.path.add(qtemp);
            }
            break;
          }

        }
      }
    }
   
    return false;
  }
  
  public double DisJointDikstra(ArrayList<LinkedList<Integer>> AvailablePath,
      Msg mtemp, int ap_count) {

    Device dtemp, dtemp2 = null;
    DijkstraQ qtemp = null, qtemp2;// ,qtemp3;
    TSlot ttemp, ttemp2;
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();
    // Fill up the Queue
    queue.add(new DijkstraQ(mtemp.src));
    queue.getFirst().cost = 0;

    queue.getFirst().T = Peerstable.get(tabdevList.get(mtemp.src)).starttime;
    queue.add(new DijkstraQ(mtemp.dest));
    mtemp.nhoplist = new LinkedList<Integer>();
    for (int i = 0; i < tabdevList.size() - ap_count; i++) {
      /*
       * if (i == src | i == dest) continue
       */;
      // if (!TraversedNodes.containsKey(i))
      queue.add(new DijkstraQ(i));

    }
    double g;

    while (queue.size() > 0) {
      int next = find_mincost(queue, mtemp.dest);
      if (next != -1)
        qtemp = queue.remove(next);
      else
        break;
      if (dtemp2 != null)
        dtemp2.meetStat.remove(qtemp.dev);
      mtemp.nhoplist.add(qtemp.dev);
      if (qtemp.dev == mtemp.dest)
        break;

      dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
      ListIterator dit2 = queue.listIterator();
      while (dit2.hasNext()) {
        Integer dkey = ((DijkstraQ) dit2.next()).dev;
        if (!dtemp.meetStat.containsKey(dkey))
          continue;

        if (dtemp.meetStat.get(dkey).meetingcnt > 1)
          g = dtemp.meetStat.get(dkey).waitTime
              / (double) dtemp.meetStat.get(dkey).meetingcnt;
        else
          g = 60 * 24 * 7;
        int k = 0;
        while (queue.get(k++).dev != dkey)
          ;
        qtemp2 = queue.get(k - 1);

        if (qtemp2.cost > qtemp.cost + g)
          qtemp2.cost = qtemp.cost + g;
        /*
         * if (qtemp.dev == src & dkey == dest) dtemp.mstat.remove(dkey);
         */
      }
      dtemp2 = dtemp;
      // qtemp3=qtemp;
    }
    AvailablePath.add(mtemp.nhoplist);
    return qtemp.cost;
  }

  public void Maxprop_dijkstra(Msg mtemp, int mob_dcount) {

    Device dtemp, dtemp2;
    DijkstraQ qtemp;
    TSlot ttemp, ttemp2;
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();

    for (int i = 0; i < tabdevList.size() - mob_dcount; i++)
      queue.add(new DijkstraQ(i));
    queue.get(mtemp.src).cost = 0;
    // if(!min_exp_delay)
    queue.get(mtemp.src).T = mtemp.m_interval.life_time.timemin;
    // else queue.get(mtemp.src).T =
    // Peerstable.get(tabdevList.get(mtemp.src)).starttime;

    while (queue.size() > 0) {
      int next = find_mincost(queue, mtemp.dest);
      if (next != -1)
        qtemp = queue.remove(next);
      else {
        mtemp.nhoplist.add(mtemp.dest);
        break;
      }
      if (qtemp.dev == mtemp.dest) {
        
        if (qtemp.path.size() == 0) {          
          mtemp.nhoplist.add(mtemp.dest);
          break;
        }      
        
        for(int j=1;j<qtemp.path.size();j++){
          mtemp.nhoplist.add(qtemp.path.get(j).dev);
        }
        mtemp.nhoplist.add(qtemp.dev);
        break;
        /*qtemp.path.removeFirst();
        for(int j=0;j<qtemp.path.size();j++)
          mtemp.nhoplist.add(qtemp.path.get(j).dev);
        mtemp.nhoplist.add(qtemp.dev);
        break;*/
      }

      dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
      ListIterator cit = dtemp.sorted_slots.listIterator(dtemp.find_slot(
          qtemp.T, 0, dtemp.sorted_slots.size() - 1));
      while (cit.hasNext()) {
        ttemp = (TSlot) cit.next();
        // System.out.println(dtemp.mac+" "+ttemp.timemin.toString()+"
        // "+ttemp.timemax.toString());
        ListIterator dit2 = queue.listIterator();
        while (dit2.hasNext()) {
          dtemp2 = Peerstable
              .get(tabdevList.get(((DijkstraQ) dit2.next()).dev));
          if (dtemp2.mac.equals(dtemp.mac))
            continue;
          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          ListIterator cit2 = dtemp2.sorted_slots.listIterator(dtemp2
              .find_slot(qtemp.T, 0, dtemp2.sorted_slots.size() - 1));

          while (cit2.hasNext()) {
            ttemp2 = (TSlot) cit2.next();
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (!ttemp2.apname.equals(ttemp.apname))
              continue;
            // System.out.println(dtemp2.mac+" "+qtemp.path.size()+"
            // "+tabdevList.get(qtemp.dev));//+" "+ttemp2.timemin.toString()+"
            // "+ttemp2.timemax.toString());

            if (!ttemp.isoverlap(ttemp2))
              continue;
            TSlot itime = new TSlot(
                ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                    : ttemp.timemin,
                ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax
                    : ttemp2.timemax);
            if (itime.timemin.before(qtemp.T))
              itime.timemin = qtemp.T;
            // System.out.println(itime.timemin.toString()+"
            // "+itime.timemax.toString());

            int i = 0;
            double g = (1 - dtemp.probab[tabdevList.indexOf(dtemp2.mac)]);

            while (queue.get(i++).dev != tabdevList.indexOf(dtemp2.mac));
            
            DijkstraQ qtemp2 = queue.get(i - 1);
            if (qtemp2.cost > qtemp.cost + g) {
              qtemp2.cost = qtemp.cost + g;
              qtemp2.T = itime.timemin;
              qtemp2.set_path(qtemp.path);
              qtemp2.path.add(qtemp);

            }
            break;
          }
        }
      }
    }
  }
  
  public void LinkStateDijkstra(Msg mtemp, int devcount,boolean dflood){
    if(!mtemp.update &mtemp.m_interval.life_time.timemin.compareTo(mtemp.rTime)<=0 )
      return;
    Device dtemp=Peerstable.get(tabdevList.get(mtemp.custodian));
    DijkstraQ qtemp;
    mtemp.mynhoplist = new Hashtable<Integer, PossibleHop>();   
    LinkedList<Integer>traversed = new LinkedList<Integer>();
    LinkedList<Integer>Commtraversed = new LinkedList<Integer>();
    Enumeration<Integer>menum2 = dtemp.meetStat.keys();
    while (menum2.hasMoreElements()){
      LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>(); 
      Integer mdev = menum2.nextElement();
      if(mtemp.hoplist.contains(mdev))
        continue;
      if(Peerstable.get(tabdevList.get(mdev)).if_repmsgexist(mtemp)!=-1)
        continue;
      for (int i = 0; i < tabdevList.size() - devcount; i++){
       if(mtemp.hoplist.contains(i)|mtemp.mynhoplist.containsKey(i))
         continue;
        queue.add(new DijkstraQ(i));
      }      
      traversed = new LinkedList<Integer>();
      int index = 0;      
      while (queue.get(index++).dev != mdev);
      
      queue.get(index-1).T = mtemp.m_interval.life_time.timemin;
      queue.get(index-1).cost = 0;
      queue.get(index-1).time = Peerstable.get(tabdevList.get(mtemp.custodian)).meetStat.get(mdev).PVector.get(mdev).VirtAvgDelay;
      while (queue.size() > 0) {
        int next = find_mincost(queue, mtemp.dest);        
        if (next != -1)
          qtemp = queue.remove(next);
        else       
          break;
              
        if (qtemp.dev == mtemp.dest) {
          Commtraversed.addAll(traversed);
          //qtemp.path.removeFirst();
          if(dflood)
            mtemp.mynhoplist.put(mdev, new PossibleHop(0));
          else{
            
              
            for(int i=0;i<qtemp.path.size();i++){
              if(mtemp.value>qtemp.path.get(i).cost)
                continue;            
            if(mtemp.mynhoplist.containsKey(qtemp.path.get(i)))
              mtemp.mynhoplist.get(qtemp.path.get(i)).count*=2;             
            else
              mtemp.mynhoplist.put(qtemp.path.get(i).dev,new PossibleHop(Double.MAX_VALUE-qtemp.cost));   
            }
          }
          break;
        }
        /*if(qtemp.time>Global.weekMin+(3*24*60))
          break;*/
        traversed.add(qtemp.dev);
        dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
        Enumeration<Integer>menum = dtemp.meetStat.keys();
        while(menum.hasMoreElements()){
          MeetingStats ms = dtemp.meetStat.get(menum.nextElement());
          if(mtemp.hoplist.contains(ms.devindex) | traversed.contains(ms.devindex)|mtemp.mynhoplist.containsKey(ms.devindex))
            continue;
          if(Peerstable.get(tabdevList.get(ms.devindex)).if_repmsgexist(mtemp)!=-1)
            continue;
          if(Commtraversed.contains(ms.devindex)){
            mtemp.mynhoplist.put(mdev, new PossibleHop(0));
            queue = new LinkedList<DijkstraQ>();
            break;
          }
          double g=1;
          DVectorUnit dvtemp = ms.PVector.get(ms.devindex);
          Date t = new Date(dvtemp.calcVirAvDTime_Lstate(qtemp.T).getTime());
          if(t.after(qtemp.T))
            continue;
            
          if(!dflood){
            dvtemp.calcAvDTime_Lstate(qtemp.T);                      
            g = dvtemp.getPathMetric();
          }          
          index = 0;
          while (queue.get(index++).dev != ms.devindex);          
          DijkstraQ qtemp2 = queue.get(index - 1);
          
          int count =1;
          if(!dflood)
            if(mtemp.mynhoplist.containsKey(qtemp2.dev))
              count += mtemp.mynhoplist.get(qtemp2.dev).count;
            
          if ((qtemp2.cost+1)*count > qtemp.cost + g) {
            qtemp2.T.setTime(t.getTime());
            qtemp2.cost = qtemp.cost + g;   
            qtemp2.time = qtemp.time + dvtemp.VirtAvgDelay;
            qtemp2.set_path(qtemp.path);
            qtemp2.path.add(qtemp);
          }       
        }      
      }
    }
    /*mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
        + (1000 * 60 * 60 * 24 * 1));*/
    for (int i = 0; i < dtemp.aliveMsglist.size(); i++) {
      Msg mtemp2 = dtemp.aliveMsglist.get(i);
      /*if (mtemp2.equals(mtemp))
        continue;*/
      if (mtemp2.dest == mtemp.dest) {
        mtemp2.mynhoplist = mtemp.mynhoplist;
        // mtemp2.replicate=true;
         mtemp2.update = false;
        // double prob=0.5;
        mtemp2.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
            + (1000 * 60 * 60 * 24 * 1));
      }
    }
  }
  public double MaxflowHistory_dijkstra(Msg mtemp, int mob_dcount,boolean path, boolean queuing, 
      LinkedList<Integer>CollectivePath,double dsize) {
    //delay =true;
    Device dtemp;
    DijkstraQ qtemp,qtemp2;
    if(mtemp.nhoplist==null)
      mtemp.nhoplist = new LinkedList<Integer>();
   
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();
    

    for (int i = 0; i < tabdevList.size() - mob_dcount; i++){
     
      queue.add(new DijkstraQ(i));   
      if(i==mtemp.src) {
        queue.getLast().cost = 0;
        queue.getLast().T = mtemp.m_interval.life_time.timemin;
      }
    }
    double size=0;
    //queue.add(0,new DijkstraQ(mtemp.hoplist.getLast()));
    
    
    try {
      while (queue.size() > 0) {
        int next = find_mincost(queue, mtemp.dest);
        if (next != -1)
          qtemp = queue.remove(next);
        else {
          mtemp.nhoplist.add(mtemp.dest);
          break;
        }
        if (qtemp.dev == mtemp.dest) {
          if (qtemp.path.size() == 0) {  
            mtemp.nhoplist = new LinkedList<Integer>();
            mtemp.nhoplist.add(mtemp.dest);
            break;
          }          
          qtemp.path.add(qtemp);
          mtemp.nhoplist = new LinkedList<Integer>();
          
          /*for(int j=1;j<qtemp.path.size();j++){
            if(size>Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
            .myNeighbor.get(qtemp.path.get(j).dev).PVector.get(qtemp.path.get(j).dev).VirtAvgSize){
              size = Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .myNeighbor.get(qtemp.path.get(j).dev).PVector.get(qtemp.path.get(j).dev).VirtAvgSize;  
            }
            
            mtemp.nhoplist.add(qtemp.path.get(j).dev);
          }           
          for(int j=1;j<qtemp.path.size();j++){
            Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
            .myNeighbor.get(qtemp.path.get(j).dev).PVector.get(qtemp.path.get(j).dev).VirtAvgSize-=size;  
            Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
            .myNeighbor.get(qtemp.path.get(j-1).dev).PVector.get(qtemp.path.get(j-1).dev).VirtAvgSize-=size; 
            
            if(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
            .myNeighbor.get(qtemp.path.get(j).dev).PVector.get(qtemp.path.get(j).dev).VirtAvgSize==0){
              Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .myNeighbor.get(qtemp.path.get(j).dev).PVector.get(qtemp.path.get(j).dev).VirtAvgDelay=Double.MAX_VALUE;
              Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
              .myNeighbor.get(qtemp.path.get(j-1).dev).PVector.get(qtemp.path.get(j-1).dev).VirtAvgDelay=Double.MAX_VALUE;
            }
          }     */   
          size = Double.MAX_VALUE;
          for(int j=1;j<qtemp.path.size();j++){
            if(size>Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
            .PVector.get(qtemp.path.get(j).dev).VirtAvgSize){
              size = Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).VirtAvgSize;  
            }
            qtemp2 = qtemp.path.get(j);
            if(j+1<qtemp.path.size())
              qtemp2.to = qtemp.path.get(j+1);
            
            mtemp.nhoplist.add(qtemp.path.get(j).dev);
          }       
          if(qtemp.path.size()>1)
            mtemp.nhop=qtemp.path.get(1);
          /*System.out.print("Path "+mtemp.ID+" "+size+"\n"+mtemp.src+"\t\t");
          for(int i=0;i<mtemp.nhoplist.size();i++)
            System.out.print(mtemp.nhoplist.get(i)+"\t");
          System.out.println();*/
          //if(!path)
          for(int j=1;j<qtemp.path.size();j++){
           /* Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
            .PVector.get(qtemp.path.get(j).dev).VirtAvgSize-=size;  
            Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
            .PVector.get(qtemp.path.get(j-1).dev).VirtAvgSize-=size; 
            
            if(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
            .PVector.get(qtemp.path.get(j).dev).VirtAvgSize==0){
              Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).VirtAvgDelay=Double.MAX_VALUE;
              Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
              .PVector.get(qtemp.path.get(j-1).dev).VirtAvgDelay=Double.MAX_VALUE;
            }*/            
           // System.out.print(mtemp.nhoplist.get(j)+" "+qtemp.path.get(j-1).dev+" "+qtemp.path.get(j).dev+" ");
            /*System.out.println(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).VirtAvgSize+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).VirtAvgDelay+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).count);
            System.out.println(Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
                .PVector.get(qtemp.path.get(j-1).dev).VirtAvgSize+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
                .PVector.get(qtemp.path.get(j-1).dev).VirtAvgDelay+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
                .PVector.get(qtemp.path.get(j-1).dev).count);
            System.out.println("i");*/
         /*   System.out.print(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).count+","+Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).VirtAvgSize+"\t");*/
           
          /*  if(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).VirtAvgSize==size){
              
              Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).count--;   
              Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
              .PVector.get(qtemp.path.get(j-1).dev).count--;
            }
            else
            {
              Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).VirtAvgSize-=size/Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).count;  
              Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
              .PVector.get(qtemp.path.get(j-1).dev).VirtAvgSize-=size/Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
              .PVector.get(qtemp.path.get(j-1).dev).count;
            }*/
            
           
            //System.out.print(mtemp.nhoplist.get(j)+" "+qtemp.path.get(j-1).dev+" "+qtemp.path.get(j).dev+" ");
           /* System.out.println(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).VirtAvgSize+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).VirtAvgDelay+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
                .PVector.get(qtemp.path.get(j).dev).count);
            System.out.println(Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
                .PVector.get(qtemp.path.get(j-1).dev).VirtAvgSize+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
                .PVector.get(qtemp.path.get(j-1).dev).VirtAvgDelay+" "+Peerstable.get(tabdevList.get(qtemp.path.get(j).dev)).meetStat.get(qtemp.path.get(j-1).dev)
                .PVector.get(qtemp.path.get(j-1).dev).count);
            System.out.println();*/
          }     
          /*System.out.println();
          for(int j=1;j<qtemp.path.size();j++)
            System.out.print(Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).count+","+Peerstable.get(tabdevList.get(qtemp.path.get(j-1).dev)).meetStat.get(qtemp.path.get(j).dev)
              .PVector.get(qtemp.path.get(j).dev).VirtAvgSize+"\t");
          System.out.println();*/
         
          mtemp.rTime.setTime(mtemp.start_time.getTime()+(long)(qtemp.path.getLast().cost*60*1000));
          return size;       
        }
        dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
        LinkedList<Integer>alreadytraversed = new LinkedList<Integer>();
        //if(path){
         for(int i=0;i<CollectivePath.size();i+=2)
         {
           if(CollectivePath.get(i)==qtemp.dev)
             alreadytraversed.add(CollectivePath.get(i+1));
         }
        //}
        
        for (int j = 0; j < queue.size(); j++) {
          
         if(alreadytraversed.contains(queue.get(j).dev))
           continue;
          
          if (!dtemp.meetStat.containsKey(queue.get(j).dev))
            continue;
          if(path)
            if(!dtemp.meetStat.get(queue.get(j).dev).PVector.containsKey(mtemp.dest))
              continue;
          if(dtemp.meetStat.get(queue.get(j).dev).PVector.get(queue.get(j).dev).count<1)
            continue;
          if(dtemp.meetStat.get(queue.get(j).dev).PVector.get(queue.get(j).dev).VirtAvgSize<dsize/10)
            continue;
          Double g;
          if(path){            
            g = dtemp.meetStat.get(queue.get(j).dev).PVector.get(mtemp.dest).VirtAvgDelay;
            if(qtemp.cost+g>(19840))
              continue; 
          }
            else{            
            g = dtemp.meetStat.get(queue.get(j).dev).PVector.get(queue.get(j).dev).VirtAvgDelay;
            if(qtemp.cost+g>(10080))
              continue;
          }
          qtemp2 = queue.get(j);
          if (qtemp2.cost > qtemp.cost + g) {
            qtemp2.cost = qtemp.cost + g;
            qtemp2.set_path(qtemp.path);
            qtemp2.path.add(qtemp);
          }
        }
      }
    } catch (Exception e) {
      System.out.println(mtemp.src + " " + mtemp.dest);
      e.printStackTrace();
    }
    return 0;
  }
  public double Maxflow_dijkstra(Msg mtemp, int mob_dcount,double size,double bw, int kl) {

    Device dtemp,dtemp2;
    TSlot ttemp, ttemp2;
    DijkstraQ qtemp,qtemp2;
    long duration ;
  
    mtemp.nhoplist = new LinkedList<Integer>();
   
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();
    Hashtable<Integer, Slotrecord>relatedSlots = new Hashtable<Integer, Slotrecord>();
    for (int i = 0; i < tabdevList.size() - mob_dcount; i++){
     if(mtemp.hoplist.contains(i))
         continue;
       queue.add(new DijkstraQ(i));
    }
    queue.add(0,new DijkstraQ(mtemp.hoplist.getLast()));
    queue.getFirst().cost = 0;
    queue.getFirst().T = mtemp.m_interval.life_time.timemin;

    try {
      while (queue.size() > 0) {
        int next = find_mincost(queue, mtemp.dest);
        if (next != -1)
          qtemp = queue.remove(next);
        else {
          //mtemp.nhoplist.add(mtemp.dest);
          break;
        }
        if (qtemp.dev == mtemp.dest) {
            if(qtemp.path.size()==0)
              return 0;
            duration = Long.MAX_VALUE;
          qtemp.path.add(qtemp);
          mtemp.nhoplist = new LinkedList<Integer>();         
          //System.out.println("calculated path");
          for(int j=1;j<qtemp.path.size();j++){
              
             //System.out.println(qtemp.path.get(j).dev+" "+qtemp.path.get(j).T.toString()+" "+new Date(qtemp.path.get(j).T.getTime()+qtemp.pduration).toString());
            qtemp2 = qtemp.path.get(j);
            if(j+1<qtemp.path.size())
              qtemp2.to = qtemp.path.get(j+1);
            if(duration>qtemp2.duration)
              duration = qtemp2.duration;
            mtemp.nhoplist.add(qtemp.path.get(j).dev);               
          }      
          if(qtemp.path.size()>1)
            mtemp.nhop=qtemp.path.get(1);
          if(mtemp.ID.equals(47)& kl==25){
            System.out.println("Path "+mtemp.ID+" "+Double.toString(Round(bw*(double)(duration),2))+" ");
            System.out.print(mtemp.src+"\t\t");
            for(int i=0;i<mtemp.nhoplist.size();i++)
              System.out.print(mtemp.nhoplist.get(i)+"\t\t");            System.out.println();
            for(int i=0;i<mtemp.nhoplist.size();i++)
              System.out.print(Double.toString(Round(bw*(double)(qtemp.path.get(i+1).duration),2))+"\t");
            System.out.println();
        
            for(int j=1;j<qtemp.path.size();j++){
              System.out.println(qtemp.path.get(j).T+"\t"+new Date(qtemp.path.get(j).T.getTime()+qtemp.path.get(j).duration));
            
            }
          }
          //mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()+(1000*60*60));
          //mtemp.nhoplist.add(qtemp.dev);
           
          /*for(int j=1;j<qtemp.path.size();j++){
            String d1=tabdevList.get(relatedSlots.get(qtemp.path.get(j).hashCode()).dev1),d2=tabdevList.get(relatedSlots.get(qtemp.path.get(j).hashCode()).dev2);
            ttemp = new TSlot(qtemp.path.get(j).T.getTime(),qtemp.path.get(j).T.getTime()+qtemp.path.get(j).duration,"");
            
            int sindex = Peerstable.get(d1).sorted_slots.indexOf(relatedSlots.get(qtemp.path.get(j).hashCode()).ttemp);
            ttemp2 = relatedSlots.get(qtemp.path.get(j).hashCode()).ttemp;
            if(Math.abs(ttemp.timemin.getTime()-ttemp2.timemin.getTime())<=2000){
              if(Math.abs(ttemp.timemax.getTime()-ttemp2.timemax.getTime())<=2000)
                Peerstable.get(d1).sorted_slots.remove(sindex);
            }
            else if(ttemp2.timemin.getTime()-ttemp.timemin.getTime()>2000 & ttemp.timemax.getTime()-ttemp2.timemax.getTime()<2000){
                ttemp2.timemax.setTime(ttemp.timemin.getTime());
            }
            else if(ttemp2.timemin.getTime()-ttemp.timemin.getTime()<=2000 & ttemp.timemax.getTime()-ttemp2.timemax.getTime()>2000){
                ttemp2.timemin.setTime(ttemp.timemax.getTime());
            }   
            else if(Math.abs(ttemp2.timemin.getTime()-ttemp.timemin.getTime())>2000 & Math.abs(ttemp.timemax.getTime()-ttemp2.timemax.getTime())>2000){              
              Peerstable.get(d1).sorted_slots.add(sindex,new TSlot(ttemp2.timemin.getTime(),ttemp.timemin.getTime(),ttemp2.apname));
              ttemp2.timemin.setTime(ttemp.timemax.getTime());
            }              
              
            sindex = Peerstable.get(d2).sorted_slots.indexOf(relatedSlots.get(qtemp.path.get(j).hashCode()).ttemp2);
            ttemp2 = relatedSlots.get(qtemp.path.get(j).hashCode()).ttemp2;
            if(Math.abs(ttemp.timemin.getTime()-ttemp2.timemin.getTime())<=2000){
              if(Math.abs(ttemp.timemax.getTime()-ttemp2.timemax.getTime())<=2000)
                Peerstable.get(d2).sorted_slots.remove(sindex);
            }
            else if(ttemp2.timemin.getTime()-ttemp.timemin.getTime()>2000 & ttemp.timemax.getTime()-ttemp2.timemax.getTime()<2000){
                ttemp2.timemax.setTime(ttemp.timemin.getTime());
            }
            else if(ttemp2.timemin.getTime()-ttemp.timemin.getTime()<=2000 & ttemp.timemax.getTime()-ttemp2.timemax.getTime()>2000){
                ttemp2.timemin.setTime(ttemp.timemax.getTime());
            }   
            else if(Math.abs(ttemp2.timemin.getTime()-ttemp.timemin.getTime())>2000 & Math.abs(ttemp.timemax.getTime()-ttemp2.timemax.getTime())>2000){              
              Peerstable.get(d2).sorted_slots.add(sindex,new TSlot(ttemp2.timemin.getTime(),ttemp.timemin.getTime(),ttemp2.apname));
              ttemp2.timemin.setTime(ttemp.timemax.getTime());
            }         
          }      */
          //mtemp.del_time.setTime(qtemp.path.getLast().T.getTime()+qtemp.path.getLast().duration);
          return duration/1000;
          
                 
        }
        dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
        
        
        for (int i = dtemp.find_slot(qtemp.T, 0, dtemp.sorted_slots.size() - 1); i > -1
        & i < dtemp.sorted_slots.size(); i++) {
      ttemp = dtemp.sorted_slots.get(i);
      if (ttemp.timemin.after(mtemp.m_interval.life_time.timemax))
        break;

      // System.out.println(dtemp.mac+" "+ttemp.timemin.toString()+"
      // "+ttemp.timemax.toString());
      
      for (int l=0;l<queue.size();l++) {
        
        dtemp2 = Peerstable.get(tabdevList.get(queue.get(l).dev));
        // System.out.println("dtemp2= "+dtemp2.mac);

        if (!dtemp2.Aptable.containsKey(ttemp.apname))
          continue;
        for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
            .size() - 1); j > -1 & j < dtemp2.sorted_slots.size(); j++) {
          ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
          if (ttemp.timemax.before(ttemp2.timemin))
            break;
          if (ttemp.timemin.after(mtemp.m_interval.life_time.timemax))
            break;
          if (!ttemp2.apname.equals(ttemp.apname))
            continue;
          
          if (!ttemp.isoverlap(ttemp2))
            continue;          
          
          TSlot itime = new TSlot(
              ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                  : ttemp.timemin,
              ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax
                  : ttemp2.timemax);
          if (itime.timemin.before(qtemp.T))
            itime.timemin.setTime(qtemp.T.getTime());
         
          
          if(!itime.timemin.before(mtemp.m_interval.life_time.timemax))
            continue;
          
          if(itime.timemax.after(mtemp.m_interval.life_time.timemax))
            itime.timemax.setTime(mtemp.m_interval.life_time.timemax.getTime());
          
          if(itime.timemax.getTime()-itime.timemin.getTime()<1000)
            continue; 
          duration = itime.timemax.getTime()-itime.timemin.getTime();
          if((itime.timemax.getTime()-itime.timemin.getTime())*bw<size/10)
            continue;
            
          double g = ((double) (itime.timemin.getTime() - qtemp.T.getTime())) / (1000 * 60);
                   
          int k = 0;
          while (queue.get(k++).dev != tabdevList.indexOf(dtemp2.mac));
          qtemp2 = queue.get(k - 1);         
          
          if (qtemp2.cost > qtemp.cost + g) {
            if(qtemp.T.getTime()+qtemp.duration>itime.timemin.getTime()){
              if(itime.timemin.getTime()-qtemp.T.getTime()<1000)
                continue;
              // qtemp.duration=itime.timemin.getTime()-qtemp.T.getTime();
             }
            
            qtemp2.cost = qtemp.cost + g;
            qtemp2.T.setTime(itime.timemin.getTime());
            qtemp2.set_path(qtemp.path);
            qtemp2.set_from(qtemp);
            qtemp2.path.add(qtemp);
            qtemp2.duration = duration;
            
            if(qtemp.T.getTime()+qtemp.duration>itime.timemin.getTime())                            
              qtemp.duration=itime.timemin.getTime()-qtemp.T.getTime();
          
            
            /*if(qtemp.T.getTime()+qtemp.duration>itime.timemin.getTime()){
              if(qtemp2.pduration>(itime.timemin.getTime()-qtemp.T.getTime()))
                qtemp2.pduration = itime.timemin.getTime()-qtemp.T.getTime();                
            }
            //qtemp.set_to(qtemp2);
            
            if(qtemp2.pduration>qtemp.pduration)
              qtemp2.pduration = qtemp.pduration;
            qtemp2.duration = itime.timemax.getTime()-itime.timemin.getTime();
            if(qtemp2.pduration>qtemp2.duration)
              qtemp2.pduration = qtemp2.duration;*/
             
             //relatedSlots.put(qtemp2.hashCode(),new Slotrecord(qtemp.dev,qtemp2.dev,ttemp,ttemp2));
          }
          break;
        }
      }
    }
      }
    } catch (Exception e) {
      System.out.println(mtemp.src + " " + mtemp.dest);
      e.printStackTrace();
    }
    return 0;
  }

  public void RMaxprop_dijkstra(Msg mtemp, int mob_dcount) {

    Device dtemp;
    DijkstraQ qtemp;
    if(mtemp.nhoplist==null)
      mtemp.nhoplist = new LinkedList<Integer>();
   
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();

    for (int i = 0; i < tabdevList.size() - mob_dcount; i++){
     if(mtemp.hoplist.contains(i))
         continue;
       queue.add(new DijkstraQ(i));
    }
    queue.add(0,new DijkstraQ(mtemp.hoplist.getLast()));
    queue.getFirst().cost = 0;
    queue.getFirst().T = mtemp.m_interval.life_time.timemin;

    try {
      while (queue.size() > 0) {
        int next = find_mincost(queue, mtemp.dest);
        if (next != -1)
          qtemp = queue.remove(next);
        else {
          mtemp.nhoplist.add(mtemp.dest);
          break;
        }
        if (qtemp.dev == mtemp.dest) {
          if (qtemp.path.size() == 0) {  
            mtemp.nhoplist = new LinkedList<Integer>();
            mtemp.nhoplist.add(mtemp.dest);
            break;
          }          
          if(mtemp.nhoplist.size()>0)
            if(mtemp.value!=Double.MAX_VALUE)
              if(Math.abs(mtemp.value-qtemp.cost)/mtemp.value<0.1)
                return;
          mtemp.nhoplist = new LinkedList<Integer>();
          for(int j=1;j<qtemp.path.size();j++)
            mtemp.nhoplist.add(qtemp.path.get(j).dev);
          mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()+(1000*60*60));
          mtemp.value = qtemp.cost;
          mtemp.nhoplist.add(qtemp.dev);
          break;       
        }
        dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
        for (int j = 0; j < queue.size(); j++) {
          if (dtemp.probab[queue.get(j).dev] == 0)
            continue;
          double g = (1 - dtemp.probab[queue.get(j).dev]);
          DijkstraQ qtemp2 = queue.get(j);
          if (qtemp2.cost > qtemp.cost + g) {
            qtemp2.cost = qtemp.cost + g;
            qtemp2.set_path(qtemp.path);
            qtemp2.path.add(qtemp);
          }
        }
      }
    } catch (Exception e) {
      System.out.println(mtemp.src + " " + mtemp.dest);
    }
  }

  public void FuzzyPVDijkstra(Msg mtemp, DataOutputStream out) {

    // mtemp.replicate=true;
    mtemp.update = false;
    // double prob=0.5;
    mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
        + (1000 * 60 * 60 * 24 * 1));
    mtemp.mynhoplist = new Hashtable<Integer, PossibleHop>();
    // mtemp.bannedHops = new LinkedList<Integer>();

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));

    MeetingStats ms;
    Enumeration<Integer> menum = dtemp.meetStat.keys();
    LinkedList<PossibleHop> PVectorProxList = new LinkedList<PossibleHop>();

    double maxTime = 0;

    ArrayList<Integer> vDevices = new ArrayList<Integer>();// for all the
                                                           // visible devices
                                                           // till destination
    while (menum.hasMoreElements()) {
      ms = dtemp.meetStat.get(menum.nextElement());

      if (mtemp.hoplist.contains(ms.devindex) | ms.devindex == mtemp.dest)
        continue;
      /*Enumeration<Integer> nhkenum = ms.myNeighbor.keys();
      while (nhkenum.hasMoreElements()) {
        int qkey = nhkenum.nextElement();
        NeighborQ nhq = ms.myNeighbor.get(qkey);
        if (!nhq.PVector.containsKey(mtemp.dest))
          continue;*/
      if (!ms.PVector.containsKey(mtemp.dest))
        continue;
      
        if (mtemp.m_interval.life_time.timemin.getTime()
            - ms.PVector.get(mtemp.dest).tstamp.getTime() > Global.weekMSec) {
          // ms.PVector.remove(mtemp.dest);
          continue;
        }
        if(Math.pow(ms.PVector.get(mtemp.dest).AvgSize,1.5)/ms.PVector.get(mtemp.dest).VirtAvgSize>1)
          continue;
        /*
         * ms.PVector.get(mtemp.dest).calcVirAvDTime(mtemp.m_interval.life_time.timemin
         * );
         * ms.PVector.get(mtemp.dest).calcAvDTime(mtemp.m_interval.life_time.timemin
         * );
         */
        /*
         * if(maxDepth<ms.PVector.get(new Integer(mtemp.dest)).depth) maxDepth =
         * ms.PVector.get(new Integer(mtemp.dest)).depth;
         */
        if (maxTime < ms.PVector.get(mtemp.dest).VirtAvgDelay)
          maxTime = ms.PVector.get(mtemp.dest).VirtAvgDelay;
        Enumeration<Integer> pvkeys = ms.PVector.keys();
        while (pvkeys.hasMoreElements()) {
          Integer key = pvkeys.nextElement();
          if (vDevices.contains(key))
            continue;
          // if(ms.PVector.get(key).depth>=ms.PVector.get(mtemp.dest).depth)
          if (ms.PVector.get(key).VirtAvgDelay >= ms.PVector.get(mtemp.dest).VirtAvgDelay)
            continue;
          if (mtemp.m_interval.life_time.timemin.getTime()
              - ms.PVector.get(key).tstamp.getTime() > Global.weekMSec) {
            // ms.PVector.remove(key);
            continue;
          }
          vDevices.add(key);
        }

        PVectorProxList.add(new PossibleHop(ms.devindex, 0, ms.PVector
            .get(mtemp.dest).depth,
            ms.PVector.get(mtemp.dest).getPathMetric(), ms.PVector
                .get(mtemp.dest).stdVDelay, ms.PVector.get(mtemp.dest).stdVSize,
            ms.PVector.get(mtemp.dest).AvgDelay, ms.PVector.get(mtemp.dest).AvgSize,
            ms.PVector.get(mtemp.dest).VirtAvgDelay, ms.PVector.get(mtemp.dest).VirtAvgSize,0));
        //break;
      //}
    }

    if (PVectorProxList.size() == 0)
      return;

    if (PVectorProxList.size() == 1) {
      mtemp.mynhoplist
          .put(PVectorProxList.get(0).index, PVectorProxList.get(0));
      return;
    }

    ArrayList<Double> cosWeights[] = new ArrayList[PVectorProxList.size()];// for all the visible devices till destination

    // normalize(PVectorProxList, false);
    Collections.sort(PVectorProxList, new VAvgTimeComparator());
    for (int i = 0; i < PVectorProxList.size(); i++) {

      cosWeights[i] = new ArrayList<Double>();

      for (int j = 0; j < vDevices.size(); j++) {
        ms = dtemp.meetStat.get(PVectorProxList.get(i).index);
      //  if (ms.myNeighbor.get(PVectorProxList.get(i).qindex).PVector.containsKey(vDevices.get(j))) {
        if (ms.PVector.containsKey(vDevices.get(j))) {

          /*
           * if(dtemp.meetStat.get(PVectorProxList.get(i).index).PVector.get(vDevices
           * .get(j)).depth<maxDepth) cosWeights[i].add(new
           * Double(maxDepth-dtemp
           * .meetStat.get(PVectorProxList.get(i).index).PVector
           * .get(vDevices.get(j)).depth));
           * if(max_time >=
           * (dtemp.meetStat.get(PVectorProxList.get(i).index).PVector
           * .get(vDevices.get(j)).SumDelay/
           * dtemp.meetStat.get(PVectorProxList.get
           * (i).index).PVector.get(vDevices.get(j)).histLen))
           * cosWeights[i].add(max_time -
           * (dtemp.meetStat.get(PVectorProxList.get
           * (i).index).PVector.get(vDevices.get(j)).SumDelay/
           * dtemp.meetStat.get
           * (PVectorProxList.get(i).index).PVector.get(vDevices
           * .get(j)).histLen));
           */
          if (maxTime >= (ms.PVector.get(vDevices.get(j)).VirtAvgDelay))
            cosWeights[i].add(maxTime - ms.PVector.get(vDevices.get(j)).VirtAvgDelay);
          else
            cosWeights[i].add(0.0);
        } else
          cosWeights[i].add(0.0);
      }

      double normFactor = 0;
      for (int j = 0; j < vDevices.size(); j++)
        normFactor += Math.pow(cosWeights[i].get(j), 2);
      if (normFactor == 0)
        continue;
      normFactor = Math.pow(normFactor, 0.5);
      for (int j = 0; j < vDevices.size(); j++)
        cosWeights[i].set(j, cosWeights[i].get(j) / normFactor);
    }
    LinkedList<PossibleHop> tentativePaths = new LinkedList<PossibleHop>();

  
    for (int i = 0; i < PVectorProxList.size(); i++) {
      double similarity = 0;
      double norm1 = 0, norm2 = 0;

      for (int j = i + 1; j < PVectorProxList.size(); j++) {
        similarity = 0;
        norm1 = 0;
        norm2 = 0;
        for (int k = 0; k < vDevices.size(); k++) {
          similarity += cosWeights[i].get(k).doubleValue()
              * cosWeights[j].get(k).doubleValue();
          norm1 += Math.pow(cosWeights[i].get(k).doubleValue(), 2);
          norm2 += Math.pow(cosWeights[j].get(k).doubleValue(), 2);
        }
        similarity /= Math.pow(norm1 * norm2, 0.5);
        //Math.pow(Math.log10(PVectorProxList.get(j).stdDev) * Math.log10(PVectorProxList.get(j).vStdDev),1)
        if (similarity >= (1 -  (Math.pow(PVectorProxList.get(j).AvgSize, 1) / Math.pow(PVectorProxList.get(j).VAvgSize, 1)))) 
        {// *Math.pow(PVectorProxList.get(j).AvgSize,1))){
          // if(!mtemp.bannedHops.contains(PVectorProxList.get(j).index))
          // mtemp.bannedHops.add(PVectorProxList.get(j).index);
          PVectorProxList.remove(j);
          j--;
        }
      }
    }

    // if(mtemp.repCount<=Math.ceil(PVectorProxList.size()/(double)5))
     Collections.sort(PVectorProxList, new VAvgTimeComparator());
    // mtemp.desRcount = PVectorProxList.size();
    // 
    mtemp.div = 1;
    
    int minthresh = 1;
    for (int i = 0; i < PVectorProxList.size(); i++) {
      if (PVectorProxList.size() > minthresh) {
        if ((tentativePaths.size() - minthresh) * mtemp.divRatio > (PVectorProxList
            .size())) {
          /*
           * if(PVectorProxList.get(i).depth<3)
           * tentativePaths.add(PVectorProxList.get(i)); //break;
           */
          if (!mtemp.bannedHops.contains(PVectorProxList.get(i).index))
            mtemp.bannedHops.add(PVectorProxList.get(i).index);
        } else {
          tentativePaths.add(PVectorProxList.get(i));
          mtemp.div = PVectorProxList.get(i).value;
        }
      } else {
        tentativePaths.add(PVectorProxList.get(i));
        mtemp.div = PVectorProxList.get(i).value;
      }
    }

    try {
      for (int i = 0; i < tentativePaths.size(); i++) {
        /*
         * if(tentativePaths.get(i).value>=mtemp.value) continue;
         */
        if (mtemp.bannedHops.contains(tentativePaths.get(i).index))
          continue;

        if (!dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned
            .containsKey(mtemp.ID)) {
          dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned.put(
              mtemp.ID, mtemp);
          dtemp.meetStat.get(tentativePaths.get(i).index).bAssigned += mtemp.size;
        }
        /*
         * DVectorUnit
         * dvtemp=dtemp.meetStat.get(tentativePaths.get(i).index).PVector
         * .get(mtemp.dest);
         * out.writeBytes(mtemp.custodian.toString()+","+mtemp.
         * ID.toString()+","); for(int k=0;k<dvtemp.histLen;k++){
         * out.writeBytes(Round(dvtemp.msgDelays[k],2)+","); }
         * out.writeBytes(Round
         * (tentativePaths.get(i).expDelay,2)+","+Round(dvtemp
         * .getstdDev(),2)+"\n");
         */
        mtemp.mynhoplist
            .put(tentativePaths.get(i).index, tentativePaths.get(i));
      }
    } catch (Exception e) {
      System.err.println("PDF IO error");
      e.printStackTrace();
    }
    mtemp.tag2 = Math.max(mtemp.tag2, mtemp.mynhoplist.size());
    if (mtemp.src != tabdevList.indexOf(dtemp.mac))
      for (int i = 0; i < dtemp.aliveMsglist.size(); i++) {
        Msg mtemp2 = dtemp.aliveMsglist.get(i);
        if (mtemp2.equals(mtemp))
          continue;
        if (mtemp2.dest == mtemp.dest & mtemp2.replicate) {
          mtemp2.mynhoplist = mtemp.mynhoplist;
          // mtemp2.replicate=true;
          // mtemp2.update = false;
          // double prob=0.5;
          mtemp2.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
              + (1000 * 60 * 60 * 12 * 1));
        }
      }
  }

  public void CognitivePVDijkstra(Msg mtemp, DataOutputStream out) {

    mtemp.update = false;
    mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
        + (1000 * 60 * 60 * 24 * 1));
    mtemp.mynhoplist = new Hashtable<Integer, PossibleHop>();
    // mtemp.bannedHops = new LinkedList<Integer>();

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));

    MeetingStats ms;
    Enumeration<Integer> menum = dtemp.meetStat.keys();
    LinkedList<PossibleHop> PVectorProxList = new LinkedList<PossibleHop>();
    double maxTime = 0;

    ArrayList<Integer> vDevices = new ArrayList<Integer>();// for all the
                                                           // visible devices                                                           // till destination
    while (menum.hasMoreElements()) {
      ms = dtemp.meetStat.get(menum.nextElement());

      if (mtemp.hoplist.contains(ms.devindex) | ms.devindex == mtemp.dest)
        continue;
      /*Enumeration<Integer> nhkenum = ms.myNeighbor.keys();
      while (nhkenum.hasMoreElements()) {
        int qkey = nhkenum.nextElement();
        NeighborQ nhq = ms.myNeighbor.get(qkey);
        if (!nhq.PVector.containsKey(mtemp.dest))
          continue;*/
      if (!ms.PVector.containsKey(mtemp.dest))
        continue;
      
        if (mtemp.m_interval.life_time.timemin.getTime()
            - ms.PVector.get(mtemp.dest).tstamp.getTime() > Global.weekMSec) 
          continue;
        
        if(Math.pow(ms.PVector.get(mtemp.dest).AvgSize,1.5)/ms.PVector.get(mtemp.dest).VirtAvgSize>1)
          continue;
        if (maxTime < ms.PVector.get(mtemp.dest).VirtAvgDelay)
          maxTime = ms.PVector.get(mtemp.dest).VirtAvgDelay;
        Enumeration<Integer> pvkeys = ms.PVector.keys();
        while (pvkeys.hasMoreElements()) {
          Integer key = pvkeys.nextElement();
          if (vDevices.contains(key))
            continue;
          if (ms.PVector.get(key).VirtAvgDelay >= ms.PVector.get(mtemp.dest).VirtAvgDelay)
            continue;
          if (mtemp.m_interval.life_time.timemin.getTime()
              - ms.PVector.get(key).tstamp.getTime() > Global.weekMSec) {
            continue;
          }
          vDevices.add(key);
        }

        PVectorProxList.add(new PossibleHop(ms.devindex, 0, ms.PVector
            .get(mtemp.dest).depth,
            ms.PVector.get(mtemp.dest).getPathMetric(), ms.PVector
                .get(mtemp.dest).stdVDelay, ms.PVector.get(mtemp.dest).stdVSize,
            ms.PVector.get(mtemp.dest).AvgDelay, ms.PVector.get(mtemp.dest).AvgSize,
            ms.PVector.get(mtemp.dest).VirtAvgDelay, ms.PVector.get(mtemp.dest).VirtAvgSize,0));
        //break;
      //}
    }

    if (PVectorProxList.size() == 0)
      return;

    if (PVectorProxList.size() == 1) {
      mtemp.mynhoplist
          .put(PVectorProxList.get(0).index, PVectorProxList.get(0));
      return;
    }

    ArrayList<Double> cosWeights[] = new ArrayList[PVectorProxList.size()];// for all the visible devices till destination

    // normalize(PVectorProxList, false);
    Collections.sort(PVectorProxList, new VAvgTimeComparator());
    for (int i = 0; i < PVectorProxList.size(); i++) {

      cosWeights[i] = new ArrayList<Double>();

      for (int j = 0; j < vDevices.size(); j++) {
        ms = dtemp.meetStat.get(PVectorProxList.get(i).index);
      //  if (ms.myNeighbor.get(PVectorProxList.get(i).qindex).PVector.containsKey(vDevices.get(j))) {
        if (ms.PVector.containsKey(vDevices.get(j))) {

          /*
           * if(dtemp.meetStat.get(PVectorProxList.get(i).index).PVector.get(vDevices
           * .get(j)).depth<maxDepth) cosWeights[i].add(new
           * Double(maxDepth-dtemp
           * .meetStat.get(PVectorProxList.get(i).index).PVector
           * .get(vDevices.get(j)).depth));
           * if(max_time >=
           * (dtemp.meetStat.get(PVectorProxList.get(i).index).PVector
           * .get(vDevices.get(j)).SumDelay/
           * dtemp.meetStat.get(PVectorProxList.get
           * (i).index).PVector.get(vDevices.get(j)).histLen))
           * cosWeights[i].add(max_time -
           * (dtemp.meetStat.get(PVectorProxList.get
           * (i).index).PVector.get(vDevices.get(j)).SumDelay/
           * dtemp.meetStat.get
           * (PVectorProxList.get(i).index).PVector.get(vDevices
           * .get(j)).histLen));
           */
          if (maxTime >= (ms.PVector.get(vDevices.get(j)).VirtAvgDelay))
            cosWeights[i].add(maxTime - ms.PVector.get(vDevices.get(j)).VirtAvgDelay);
          else
            cosWeights[i].add(0.0);
        } else
          cosWeights[i].add(0.0);
      }

      double normFactor = 0;
      for (int j = 0; j < vDevices.size(); j++)
        normFactor += Math.pow(cosWeights[i].get(j), 2);
      if (normFactor == 0)
        continue;
      normFactor = Math.pow(normFactor, 0.5);
      for (int j = 0; j < vDevices.size(); j++)
        cosWeights[i].set(j, cosWeights[i].get(j) / normFactor);
    }
    LinkedList<PossibleHop> tentativePaths = new LinkedList<PossibleHop>();

  
    for (int i = 0; i < PVectorProxList.size(); i++) {
      double similarity = 0;
      double norm1 = 0, norm2 = 0;

      for (int j = i + 1; j < PVectorProxList.size(); j++) {
        similarity = 0;
        norm1 = 0;
        norm2 = 0;
        for (int k = 0; k < vDevices.size(); k++) {
          similarity += cosWeights[i].get(k).doubleValue()
              * cosWeights[j].get(k).doubleValue();
          norm1 += Math.pow(cosWeights[i].get(k).doubleValue(), 2);
          norm2 += Math.pow(cosWeights[j].get(k).doubleValue(), 2);
        }
        similarity /= Math.pow(norm1 * norm2, 0.5);
        //Math.pow(Math.log10(PVectorProxList.get(j).stdDev) * Math.log10(PVectorProxList.get(j).vStdDev),1)
        if (similarity >= (1 -  (Math.pow(PVectorProxList.get(j).AvgSize, 1) / Math.pow(PVectorProxList.get(j).VAvgSize, 1)))) 
        {// *Math.pow(PVectorProxList.get(j).AvgSize,1))){
          // if(!mtemp.bannedHops.contains(PVectorProxList.get(j).index))
          // mtemp.bannedHops.add(PVectorProxList.get(j).index);
          PVectorProxList.remove(j);
          j--;
        }
      }
    }

    // if(mtemp.repCount<=Math.ceil(PVectorProxList.size()/(double)5))
     Collections.sort(PVectorProxList, new VAvgTimeComparator());
    // mtemp.desRcount = PVectorProxList.size();
    // 
    mtemp.div = 1;
    
    int minthresh = 1;
    for (int i = 0; i < PVectorProxList.size(); i++) {
      if (PVectorProxList.size() > minthresh) {
        if ((tentativePaths.size() - minthresh) * mtemp.divRatio > (PVectorProxList
            .size())) {
          /*
           * if(PVectorProxList.get(i).depth<3)
           * tentativePaths.add(PVectorProxList.get(i)); //break;
           */
          if (!mtemp.bannedHops.contains(PVectorProxList.get(i).index))
            mtemp.bannedHops.add(PVectorProxList.get(i).index);
        } else {
          tentativePaths.add(PVectorProxList.get(i));
          mtemp.div = PVectorProxList.get(i).value;
        }
      } else {
        tentativePaths.add(PVectorProxList.get(i));
        mtemp.div = PVectorProxList.get(i).value;
      }
    }

    try {
      for (int i = 0; i < tentativePaths.size(); i++) {
        /*
         * if(tentativePaths.get(i).value>=mtemp.value) continue;
         */
        if (mtemp.bannedHops.contains(tentativePaths.get(i).index))
          continue;

        if (!dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned
            .containsKey(mtemp.ID)) {
          dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned.put(
              mtemp.ID, mtemp);
          dtemp.meetStat.get(tentativePaths.get(i).index).bAssigned += mtemp.size;
        }
 
        mtemp.mynhoplist
            .put(tentativePaths.get(i).index, tentativePaths.get(i));
      }
    } catch (Exception e) {
      System.err.println("PDF IO error");
      e.printStackTrace();
    }
    mtemp.tag2 = Math.max(mtemp.tag2, mtemp.mynhoplist.size());
    if (mtemp.src != tabdevList.indexOf(dtemp.mac))
      for (int i = 0; i < dtemp.aliveMsglist.size(); i++) {
        Msg mtemp2 = dtemp.aliveMsglist.get(i);
        if (mtemp2.equals(mtemp))
          continue;
        if (mtemp2.dest == mtemp.dest & mtemp2.replicate) {
          mtemp2.mynhoplist = mtemp.mynhoplist;
          // mtemp2.replicate=true;
          // mtemp2.update = false;
          // double prob=0.5;
          mtemp2.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
              + (1000 * 60 * 60 * 12 * 1));
        }
      }
  }

  /*public double Nileplusfilter(Msg mtemp,
      LinkedList<PossibleHop> PVectorProxList, ArrayList<Integer> vDevices,
      Integer maxDepth) {

    mtemp.replicate = true;
    mtemp.update = false;
    mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
        + (1000 * 60 * 60 * 24 * 1));
    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    mtemp.mynhoplist = new Hashtable<Integer, PossibleHop>();
    MeetingStats ms;
    Enumeration<Integer> menum = dtemp.meetStat.keys();

    while (menum.hasMoreElements()) {
      ms = dtemp.meetStat.get(menum.nextElement());

      if (!ms.PVector.containsKey(new Integer(mtemp.dest)))
        continue;
     
      if (maxDepth < ms.PVector.get(new Integer(mtemp.dest)).depth)
        maxDepth = ms.PVector.get(new Integer(mtemp.dest)).depth;

      Enumeration<Integer> pvkeys = ms.PVector.keys();
      while (pvkeys.hasMoreElements()) {
        Integer key = pvkeys.nextElement();
        if (vDevices.contains(key))
          continue;
        if (ms.PVector.get(key).depth >= ms.PVector.get(mtemp.dest).depth)
          continue;
        vDevices.add(key);
      }
      // PVectorProxList.add(new
      // PossibleHop(ms.devindex,ms.PVector.get(mtemp.dest).SumDelay/ms.PVector.get(mtemp.dest).histLen,ms.PVector.get(mtemp.dest).depth,
      // ms.PVector.get(mtemp.dest).getstdDev(),
      // 0,ms.PVector.get(mtemp.dest).dAtt,ms.PVector.get(mtemp.dest).AvgDelay,ms.PVector.get(mtemp.dest).AvgSize));

    }
    
    if (PVectorProxList.size() == 0)
      return 0;

    if (PVectorProxList.size() == 1) {
      mtemp.mynhoplist
          .put(PVectorProxList.get(0).index, PVectorProxList.get(0));
      return 0;
    }
    return 0;
    // return normalize(PVectorProxList, true);
  }

  public void NileplusDijkstra(LinkedList<PossibleHop> PVectorProxList,
      ArrayList<Integer> vDevices, Device dtemp, Msg mtemp, Integer maxDepth) {

    // double max_time = 60*24*50;
    ArrayList<Double> cosWeights[] = new ArrayList[PVectorProxList.size()];// for
                                                                           // all
                                                                           // the
                                                                           // visible
                                                                           // devices
                                                                           // till
                                                                           // destination

    for (int i = 0; i < PVectorProxList.size(); i++) {
      cosWeights[i] = new ArrayList<Double>();
      for (int j = 0; j < vDevices.size(); j++) {
        if (dtemp.meetStat.get(PVectorProxList.get(i).index).PVector
            .containsKey(vDevices.get(j))) {

          if (dtemp.meetStat.get(PVectorProxList.get(i).index).PVector
              .get(vDevices.get(j)).depth < maxDepth)
            cosWeights[i].add(new Double(maxDepth
                - dtemp.meetStat.get(PVectorProxList.get(i).index).PVector
                    .get(vDevices.get(j)).depth));
          
          else
            cosWeights[i].add(0.0);
        } else
          cosWeights[i].add(0.0);
      }

      double normFactor = 0;
      for (int j = 0; j < vDevices.size(); j++)
        normFactor += Math.pow(cosWeights[i].get(j), 2);
      normFactor = Math.pow(normFactor, 0.5);
      for (int j = 0; j < vDevices.size(); j++)
        cosWeights[i].set(j, cosWeights[i].get(j) / normFactor);
    }
    LinkedList<PossibleHop> tentativePaths = new LinkedList<PossibleHop>();

    for (int i = 0; i < PVectorProxList.size(); i++) {
      double similarity = 0;

      for (int j = i + 1; j < PVectorProxList.size(); j++) {
        if (tentativePaths.contains(PVectorProxList.get(j)))
          continue;

        similarity = 0;

        for (int k = 0; k < vDevices.size(); k++)
          similarity += cosWeights[i].get(k) * cosWeights[j].get(k);

        if (similarity >= (1 - Math.pow(PVectorProxList.get(j).value, 1))) {
          // mtemp.bannedHops.add(PVectorProxList.get(j).index);
          PVectorProxList.remove(j);
          j--;
        }
      }
    }

    for (int i = 0; i < PVectorProxList.size(); i++)
      tentativePaths.add(PVectorProxList.get(i));
    for (int i = 0; i < tentativePaths.size(); i++) {
      if (!dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned
          .containsKey(mtemp.ID)) {
        dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned.put(mtemp.ID,
            mtemp);
        dtemp.meetStat.get(tentativePaths.get(i).index).PVector
            .get(tentativePaths.get(i).index).bAssigned += mtemp.size;
      }
      // if(mtemp.bannedHops.contains(tentativePaths.get(i).index))
      // continue;
      mtemp.mynhoplist.put(tentativePaths.get(i).index, tentativePaths.get(i));
    }

  }
*/
  public void MyPVDijkstra(Msg mtemp, double bw, boolean dFlood, int kfact) {

    mtemp.replicate = true;
    mtemp.update = false;
    mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
        + (1000 * 60 * 60 * 24 * 1));
    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    mtemp.mynhoplist = new Hashtable<Integer, PossibleHop>();

    MeetingStats ms;
    // System.out.println();
    Enumeration<Integer> menum = dtemp.meetStat.keys();
    LinkedList<PossibleHop> PVectorProxList = new LinkedList<PossibleHop>();

    while (menum.hasMoreElements()) {
      ms = dtemp.meetStat.get(menum.nextElement());
      if (mtemp.hoplist.contains(ms.devindex) | ms.devindex == mtemp.dest)
        continue;
     // Enumeration<Integer> nhkenum = ms.myNeighbor.keys();
    //  while (nhkenum.hasMoreElements()) {
     //   int qkey = nhkenum.nextElement();
     //   NeighborQ nhq = ms.myNeighbor.get(qkey);
       // if (!nhq.PVector.containsKey(mtemp.dest))
       //   continue;

       
          if (!ms.PVector.containsKey(mtemp.dest))
            continue;
      
        // System.out.println(mtemp.ID+
        // " "+mtemp.hoplist.getLast()+" "+ms.devindex+" "+ms.PVector.get(mtemp.dest).tsIndex+" "+ms.PVector.get(mtemp.dest).tstamps);
        /*
         * if(mtemp.m_interval.life_time.timemin.getTime()-ms.PVector.get(mtemp.dest
         * ).
         * tstamps[(ms.PVector.get(mtemp.dest).tsIndex-1)%ms.PVector.get(mtemp
         * .dest).tsHistlen].getTime()>Fifteendays){ continue; }
         */
        /*
         * if(dFlood & ms.msgStat.containsKey(mtemp.ID)){
         * if(ms.msgStat.get(mtemp.ID).containsKey(mtemp.frag_id)){ rcount +=
         * ms.msgStat.get(mtemp.ID).get(mtemp.frag_id).count; continue; } } //
         * depth = ms.PVector.get(new Integer(mtemp.dest)).depth; if(dFlood &
         * mtemp.hoplist.contains(ms.devindex)) continue;
         * //PVectorProxList.add(new
         * PossibleHop(ms.devindex,ms.PVector.get(mtemp
         * .dest).proximity/(double)(depth))); /long avgtime=0; int i=0; //
         * Integer loopcount=
         * ms.PVector.get(mtemp.dest).tsIndex>=ms.PVector.get(
         * mtemp.dest).tsHistlen ?
         * ms.PVector.get(mtemp.dest).tsHistlen:ms.PVector
         * .get(mtemp.dest).tsIndex%ms.PVector.get(mtemp.dest).tsHistlen; //for(
         * i=0;i<loopcount;i++)
         * //avgtime+=mtemp.m_interval.life_time.timemin.getTime
         * ()-ms.PVector.get(mtemp.dest).tstamps[i].getTime();
         */
        if (dFlood)
          PVectorProxList.add(new PossibleHop(ms.devindex, ms.devindex, 0, 0, 0, 0, 0, 0, 0, 0,0));
         // PVectorProxList.add(new PossibleHop(ms.devindex, qkey, 0, 0, 0, 0, 0, 0, 0, 0));
        else
        PVectorProxList.add(new PossibleHop(ms.devindex, 0, ms.PVector
             .get(mtemp.dest).depth, 0, 0, 0,ms.PVector.get(mtemp.dest).AvgDelay,
             ms.PVector.get(mtemp.dest).AvgSize, 0, 0,0));
   
         // PVectorProxList.add(new PossibleHop(ms.devindex, qkey, nhq.PVector
           //   .get(mtemp.dest).depth, 0, 0, 0,nhq.PVector.get(mtemp.dest).AvgDelay,
             // nhq.PVector.get(mtemp.dest).AvgSize, 0, 0));
      //}
    }

    if (PVectorProxList.size() == 0)
      return;
    if (dFlood) {
      for (int i = 0; i < PVectorProxList.size(); i++) {
        if (!dtemp.meetStat.get(PVectorProxList.get(i).index).mAssigned
            .containsKey(mtemp.ID)) {
          dtemp.meetStat.get(PVectorProxList.get(i).index).mAssigned.put(
              mtemp.ID, mtemp);
          dtemp.meetStat.get(PVectorProxList.get(i).index).bAssigned += mtemp.size;
        }
        mtemp.mynhoplist.put(PVectorProxList.get(i).index, new PossibleHop());
      }

      return;
    }
   // Collections.sort(PVectorProxList, new TranmissionTimeComparator());
    mtemp.mynhoplist.put(PVectorProxList.get(0).index, PVectorProxList.get(0));

    /*
     * if(mtemp.custodian.equals(mtemp.src) & mtemp.MaxHopLimit==null){ Integer
     * Mindepth =Integer.MAX_VALUE;; for(int i=0;i<PVectorProxList.size();i++){
     * if(PVectorProxList.get(i).depth<Mindepth) Mindepth =
     * PVectorProxList.get(i).depth; } mtemp.MaxHopLimit = new
     * Integer((int)(Mindepth3)); //mtemp.MaxHopLimit = new
     * Integer((int)(PVectorProxList.get(0).depth2.5)); } else {
     * if(mtemp.MaxHopLimit.equals(mtemp.hoplist.size()-1)) return; }
     */
    // int extraEpsilon = (int)(dtemp.meetStat.size()/PVectorProxList.size());
    // long tEpsilon = 60*3*extraEpsilon;
    double tEpsilon = 0;
    for (int i = 0; i < PVectorProxList.size() - 1; i++) {
      tEpsilon += PVectorProxList.get(i + 1).value
          - PVectorProxList.get(i).value;
    }

    tEpsilon /= (PVectorProxList.size() / kfact);

    // tEpsilon/=(1/Math.log10(bw));

    LinkedList<PossibleHop> tentativePaths = new LinkedList<PossibleHop>();

    for (int i = 1; i < PVectorProxList.size(); i++) {
      // long deficit =
      // dtemp.meetStat.get(PVectorProxList.get(i).index).bAttempted-dtemp.meetStat.get(PVectorProxList.get(i).index).bDelivered+1;
      // System.out.println(deficit+" "+Math.log10(deficit));
      /*
       * if(deficit==0) tEpsilon*=10; else
       */
      // tEpsilon*=10/Math.log10(deficit);
      /*
       * if(deficit<1) deficit=1;
       * if(PVectorProxList.get(i).value<(PVectorProxList
       * .get(0).value+tEpsilon1/Math.pow(Math.log10(deficit),2.2)))
       * tentativePaths.add(PVectorProxList.get(i)); else break;
       */
    }
    // if(tentativePaths.size()>3){
    // getDisjoint(tentativePaths,dtemp);
    // }*/
    for (int i = 0; i < tentativePaths.size(); i++) {
      if (!dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned
          .containsKey(mtemp.ID)) {
        dtemp.meetStat.get(tentativePaths.get(i).index).mAssigned.put(mtemp.ID,
            mtemp);
        dtemp.meetStat.get(tentativePaths.get(i).index).bAssigned += mtemp.size;
      }
      mtemp.mynhoplist.put(tentativePaths.get(i).index, tentativePaths.get(i));
    }
    mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()
        + (1000 * 60 * 60 * 24));
    // System.out.println(tEpsilon+" "+mtemp.mynhoplist.size());
  }

  /*
   * public boolean Bfdiff(BitSet bset, MeetingStats ms, int bflen) { if
   * (bset.cardinality() == 0) return true; BitSet b = new BitSet(bflen);
   * b.or(ms.sbf.bitSet); // b.xor(bset); int count = 0; for (int i =
   * b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) { if (bset.get(i))
   * count++; } if (count / (float) b.cardinality() <= 0.5) return true; return
   * false; } public boolean Bfdiff(BitSet bset1, BitSet bset2) { int count=0;
   * for (int i = bset1.nextSetBit(0); i >= 0; i = bset1.nextSetBit(i + 1)) { if
   * (bset2.get(i)){ count++; if (count / (float) bset1.cardinality() > 0.1)
   * return false; } } return true; } public boolean Bfdiffdummy(BitSet bset,
   * MeetingStats ms, int bflen) { if (bset.cardinality() == 0) return true;
   * BitSet b = new BitSet(bflen); b.or(ms.sbf.bitSet); // b.xor(bset); int
   * count = 0; for (int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) {
   * if (bset.get(i)) { count++; } } if (count / (float) b.cardinality() <= 0.5)
   * return true; return false; }
   */

  public class ProxComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((PossibleHop) o1).value.compareTo(((PossibleHop) o2).value);
    }
  }

  /*
   * public class EncounterTimeComparator implements Comparator { public int
   * compare(Object o1, Object o2) { return ((PossibleHop)
   * o1).tstamp.compareTo(((PossibleHop) o2).tstamp); } }
   */
  

  public class AvgTimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((PossibleHop) o1).AvgDtime.compareTo(((PossibleHop) o2).AvgDtime);
    }
  }

  public class VAvgTimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((PossibleHop) o1).VAvgDtime
          .compareTo(((PossibleHop) o2).VAvgDtime);
    }
  }

  public class VChannelFailComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((PossibleHop) o1).channelFailProbab
          .compareTo(((PossibleHop) o2).channelFailProbab);
    }
  }
  public class AvgSizeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return (int) (((PossibleHop) o2).VAvgSize - ((PossibleHop) o1).VAvgSize);
    }
  }

  /*
   * public class DefComparator implements Comparator { public int
   * compare(Object o1, Object o2) { return ((PossibleHop)
   * o1).deficit.compareTo(((PossibleHop) o2).deficit); } }
   */
  public class ProbabComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((PossibleHop) o1).value.compareTo(((PossibleHop) o2).value);
    }
  }

  public class DepthComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((PossibleHop) o1).depth.compareTo(((PossibleHop) o2).depth);
    }
  }

  public double BFS(ArrayList<LinkedList<Integer>> AvailablePath, Msg mtemp,
      int ap_count) {

    Device dtemp, dtemp2 = null;
    DijkstraQ qtemp = null, qtemp2, qtemp3;

    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();
    // Fill up the Queue
    queue.add(new DijkstraQ(mtemp.src));
    queue.getFirst().cost = 0;

    mtemp.nhoplist = new LinkedList<Integer>();
    for (int i = 0; i < tabdevList.size() - ap_count; i++) {
      if (i != mtemp.src)
        queue.add(new DijkstraQ(i));
    }
    double g;
    LinkedList<DijkstraQ> scanlist = new LinkedList<DijkstraQ>();
    LinkedList<DijkstraQ> labellist = new LinkedList<DijkstraQ>();

    scanlist.add(queue.get(0));
    int key;
    MeetingStats ms;
    qtemp3 = Qfind(mtemp.dest, queue);
    int i = 0, j = 0;
    while (j < scanlist.size()) {
      System.out.println(j);
      qtemp = scanlist.get(j++);
      dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
      if (qtemp.dev != mtemp.src) {
        mtemp.nhoplist.add(qtemp.dev);
        dtemp2.meetStat.remove(qtemp.dev);

      }

      dtemp2 = Peerstable.get(tabdevList.get(qtemp.dev));
      Enumeration<Integer> kenum = dtemp.meetStat.keys();
      while (kenum.hasMoreElements()) {
        key = kenum.nextElement();
        ms = dtemp.meetStat.get(key);
        qtemp2 = Qfind(key, queue);
        if (qtemp2 == null)
          continue;

        if (ms.meetingcnt <= 1)
          continue;

        g = qtemp.cost + ms.waitTime / ms.meetingcnt;
        if (qtemp2.cost > g & qtemp3.cost > g) {
          qtemp2.cost = qtemp.cost + g;
          labellist.add(qtemp2);

        }
      }
      for (; i < labellist.size(); i++) {
        if (labellist.get(i).dev != mtemp.dest)
          scanlist.add(labellist.get(i));
      }
    }
    if (qtemp3.cost != Double.MAX_VALUE)
      mtemp.nhoplist.add(mtemp.dest);
    AvailablePath.add(mtemp.nhoplist);
    return qtemp3.cost;

  }

  public DijkstraQ Qfind(int key, LinkedList<DijkstraQ> queue) {

    for (int i = 0; i < queue.size(); i++) {
      if (queue.get(i).dev == key)
        return queue.get(i);
    }
    return null;
  }

  public boolean BFS(ArrayList<ArrayList<boolean[]>> l, int src, int dest,
      int maxsize, boolean nodedisjoint) {

    int qsize = l.get(0).size();
    boolean[] visited = new boolean[qsize];
    ArrayList<Integer> queue = new ArrayList<Integer>();
    ArrayList<Integer> path = new ArrayList<Integer>();
    ArrayList<Integer> tpath = new ArrayList<Integer>();
    if (src == 450)
      src = 450;
    if (nodedisjoint) {
      // queue.add(src+maxsize);
      dest += maxsize;
      // visited[src+maxsize]=true;
    }
    visited[src] = true;
    queue.add(src);

    while (queue.size() > 0) {

      int v = queue.remove(0);

      // if v is connected directly to one of the already visited nodes then
      // connect them directly
      for (int j = 0; j < tpath.size(); j++) {
        if (l.get(tpath.get(j)).get(v)[0]) {
          for (int k = 0; k < j; k++) {
            tpath.remove(0);
            path.remove(path.size() - 1);
          }
          break;
        }
      }
      tpath.add(0, v);
      path.add(v);

      // typical BFS
      for (int i = 0; i < qsize; i++) {
        if (i == v)
          continue;
        if (l.get(v).get(i)[0] & !visited[i]) {

          if (i == dest) {
            path.add(i);
            removepath(l, path);
            return true;
          }

          queue.add(0, i);
          visited[i] = true;

        }
      }
    }

    return false;

  }

  public void removepath(ArrayList<ArrayList<boolean[]>> l,
      ArrayList<Integer> path) {

    boolean disp = false;
    // if(path.get(0)==741)
    // disp = true;
    for (int i = 0; i < path.size() - 1; i++) {
      l.get(path.get(i)).get(path.get(i + 1))[1] = true;
      l.get(path.get(i)).get(path.get(i + 1))[0] = false;
      if (disp)
        System.out.print(path.get(i) + ",");
    }
    if (disp)
      System.out.println(path.get(path.size() - 1));

  }

  public void CalcDisjointPath(int ap_count, int src, int dest) {

    int devlist = tabdevList.size() - ap_count;
    ArrayList<ArrayList<Double>> l = new ArrayList<ArrayList<Double>>();
    Double[] d = new Double[devlist];
    Double[] s = new Double[devlist];
    Integer[] p = new Integer[devlist];

    int prev, minJ = 0;
    Double minD, temp;
    for (int i = 0; i < devlist; i++) {
      d[i] = l.get(src).get(i);
      s[i] = new Double(1);
      p[i] = new Integer(src);
    }
    s[src] = new Double(0);
    prev = src;
    do {
      minD = Double.MAX_VALUE;
      for (int j = 0; j < devlist; j++) {
        if (s[j] > 0 && d[j] < minD) {
          minD = d[j];
          minJ = j;
        }
      }
      temp = s[minJ];
      s[minJ] = new Double(0);
      prev = minJ;
      if (prev == dest)
        break;
      for (int i = 0; i < devlist; i++) {
        if (l.get(prev).get(i) != Double.MAX_VALUE
            && d[i] > d[prev] + l.get(prev).get(i)) {
          d[i] = d[prev] + l.get(prev).get(i);
          p[i] = prev;
          s[i] = temp.doubleValue();
        }
      }
    } while (true);
  }

  public void DisJointAdjustment(ArrayList<ArrayList<Double>> l, Integer[] p,
      int src, int dest) {

    ArrayList temp;
    for (int i = 0; i < p.length; i++) {
      l.get(p[i]).set(dest, 1.2);
      temp = new ArrayList<Double>();
      for (int j = 0; j < l.size() + 1; j++)
        temp.add(Double.MAX_VALUE);
      if (i > 0 & i < p.length - 1) {
        l.add(temp);
        l.get(l.size() - 1).set(dest, l.get(i).get(dest));
        l.get(i).add(new Double(0));
      }
    }
  }

  // error with earlier disjoint djikstra as pointed out by marcel.
  /*
   * public void disjoint_dikstra(Msg mtemp, int ap_count){
   * LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>(); DijkstraQ qtemp,
   * qtemp2; // Fill up the Queue for (int i = 0; i < tabdevList.size() -
   * ap_count; i++) { queue.add(new DijkstraQ(i)); }
   * queue.get(mtemp.hoplist.getLast()).cost = 0; while (queue.size() > 0) { int
   * next = find_mincost(queue, dest); if (next != -1) qtemp =
   * queue.remove(next); else break; if (qtemp.dev == dest) break; dtemp =
   * Peerstable.get(tabdevList.get(qtemp.dev)); ListIterator dit2 =
   * queue.listIterator(); while (dit2.hasNext()) { Integer dkey = ((DijkstraQ)
   * dit2.next()).dev; if (!dtemp.mstat.containsKey(dkey)) continue; if
   * (dtemp.mstat.get(dkey).meetingcnt > 1) g = dtemp.mstat.get(dkey).waitTime /
   * (double) dtemp.mstat.get(dkey).meetingcnt; else g = 60 24 7; int k = 0;
   * while (queue.get(k++).dev != dkey) ; qtemp2 = queue.get(k - 1); if
   * (qtemp2.cost > qtemp.cost + g) qtemp2.cost = qtemp.cost + g; if (qtemp.dev
   * == src & dkey == dest) dtemp.mstat.remove(dkey); } } } Device dtemp =
   * Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())); MeetingStats ms;
   * Enumeration <Integer>denum = dtemp.mstat.keys(); double
   * min=Double.MAX_VALUE; int neigh_key=-1; while(denum.hasMoreElements()){
   * neigh_key =denum.nextElement(); ms= dtemp.mstat.get(neigh_key);
   * if(ms.waitTime<min) min = ms.waitTime; } Collections.sort(pd, new
   * BValComparator()); double tcount = pd.size();// > 5 ? 5 : pd.size();
   * //System.out.println(pd.size()); for (int i = 0; i < tcount; i++)
   * mtemp.mynhoplist.put(pd.get(i).index, pd.get(i).index); / if(nexthop>-1)
   * mtemp.nhoplist.add(nexthop); else { mtemp.nhoplist.add(mtemp.dest);
   * //if(mtemp.nhoplist.size()==0){
   * if(Peerstable.get("debug").msgcount(mtemp.ID)==0)
   * Peerstable.get("debug").Msglist.add(mtemp.ID); }
   */
  /*
   * if (mtemp.mynhoplist.size() == 1) { if (bloomcard.size() > 0)
   * mtemp.mynhoplist.put(bloomcard.get(0).index, bloomcard.get(0).index); if
   * (!Peerstable.get("debug").if_repmsgexist(mtemp.ID))
   * Peerstable.get("debug").Msglist.add(mtemp.ID); } } / public void
   * MyDijkstra(Msg mtemp,int path_length,int bfilterlen){ Device dtemp =
   * Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())); MeetingStats ms ;
   * BloomFilterLibrary bfl = new BloomFilterLibrary(); int [] hashvalues
   * =bfl.GetHash(bfilterlen,tabdevList.get(mtemp.dest)); double max
   * =0,max2=0,min3=5000; long etime=0; String mac="",nexthop="",nexthop2="";
   * mtemp.nhoplist = new LinkedList<Integer>(); Enumeration denum =
   * dtemp.mstat.keys(); while(denum.hasMoreElements()){
   * //System.out.println(h+++"d"); mac =(String) denum.nextElement(); ms =
   * dtemp.mstat.get(mac); double val ;
   * if(mac.equals(tabdevList.get(mtemp.dest))){ //val =
   * BloomFilterLibrary.getBvalue(ms); val =
   * ms.meetingcnt/BloomFilterLibrary.normalisingfact(dtemp); if(val>max){ max=
   * val; nexthop= new String(mac); } if(val>max2) max2=val; } else{
   * if(ms.bloom.size()<hashvalues.length) continue;
   * //System.out.println(ms.bloom.size()); for(int i=0;i<hashvalues.length;i++)
   * { val = ms.getval(hashvalues[i]); if(min3> val & val >0 &
   * !mtemp.hoplist.contains(tabdevList.indexOf(mac))&
   * !Peerstable.get(mac).if_repmsgexist(mtemp.ID)) min3 = val; if(val>max2)
   * max2=val; } if(min3>max & min3!=5000){ max=min3; nexthop= new String(mac);
   * //etime=(long)ms.waitTime/ms.meetingcnt; /for(int i=0;i<bfilterlen;i++)
   * System.out.print(ms.getval(i)+"\t"); System.out.println();
   */
  /*
   * for(int i=0;i<ms.bloom.size()-1;i++){
   * if(ms.bloom.get(i).index+ms.bloom.get(i).count!=ms.bloom.get(i+1).index) {
   * System.out.println("bloom error"); System.exit(0); } }
   */

  // }
  /*
   * if(min2>10/(ms.waitTime/ms.meetingcnt) &
   * !Peerstable.get(mac).if_repmsgexist(mtemp.ID)){ min2 =
   * 100/(ms.waitTime/ms.meetingcnt); nexthop2= new String(mac); }
   */
  /*
   * } } mtemp.nhop=new DijkstraQ(-1); if(nexthop.length()>0){
   * mtemp.nhoplist.add(tabdevList.indexOf(nexthop)); DijkstraQ qtemp = new
   * DijkstraQ(tabdevList.indexOf(nexthop));
   * //qtemp.T.setTime(mtemp.m_interval.life_time.timemin.getTime()+etime1000);
   * qtemp.T.setTime(new Date().getTime()-new Date().getTime());
   * mtemp.nhop=qtemp; } if(max20.01>=max) mtemp.replicate=true;
   * //if(nexthop2.length()>0){
   * /mtemp.nhoplist.add(tabdevList.indexOf(nexthop2)); mtemp.replicate=true;
   */
  // }
  /*
   * if(mtemp.nhoplist.size()==0){
   * if(Peerstable.get("debug").msgcount(mtemp.ID)==0)
   * Peerstable.get("debug").Msglist.add(mtemp.ID); } }
   */
  // to be worked on
  /*public void EMDAA_dijkstra(Msg mtemp, boolean min_exp_delay) {

    Device dtemp, dtemp2;
    DijkstraQ qtemp;
    TSlot ttemp, ttemp2;
    LinkedList<DijkstraQ> queue = new LinkedList<DijkstraQ>();

    for (int i = 0; i < tabdevList.size(); i++)
      queue.add(new DijkstraQ(i));
    queue.get(mtemp.src).cost = 0;
    if (!min_exp_delay)
      queue.get(mtemp.src).T = mtemp.m_interval.life_time.timemin;

    else
      queue.get(mtemp.src).T = Peerstable.get(tabdevList.get(mtemp.src)).starttime;

    while (queue.size() > 0) {
      qtemp = queue.remove();
      if (qtemp.dev == mtemp.dest) {
        qtemp.path.removeFirst();
        for(int j=0;j<qtemp.path.size();j++)
          mtemp.nhoplist.add(qtemp.path.get(j).dev);
        mtemp.nhoplist.add(qtemp.dev);
        break;
      }

      dtemp = Peerstable.get(tabdevList.get(qtemp.dev));
      ListIterator cit = dtemp.sorted_slots.listIterator(dtemp.find_slot(
          qtemp.T, 0, dtemp.sorted_slots.size() - 1));
      while (cit.hasNext()) {
        ttemp = (TSlot) cit.next();
        // System.out.println(dtemp.mac+" "+ttemp.timemin.toString()+"
        // "+ttemp.timemax.toString());
        ListIterator dit2 = queue.listIterator();
        while (dit2.hasNext()) {
          dtemp2 = Peerstable
              .get(tabdevList.get(((DijkstraQ) dit2.next()).dev));
          if (dtemp2.mac.equals(dtemp.mac))
            continue;
          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          ListIterator cit2 = dtemp2.sorted_slots.listIterator(dtemp2
              .find_slot(qtemp.T, 0, dtemp2.sorted_slots.size() - 1));

          while (cit2.hasNext()) {
            ttemp2 = (TSlot) cit2.next();
            if (!ttemp2.apname.equals(ttemp.apname))
              continue;
            // System.out.println(dtemp2.mac+" "+qtemp.path.size()+"
            // "+tabdevList.get(qtemp.dev));//+" "+ttemp2.timemin.toString()+"
            // "+ttemp2.timemax.toString());
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (!ttemp.isoverlap(ttemp2))
              continue;
            TSlot itime = new TSlot(
                ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                    : ttemp.timemin,
                ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax
                    : ttemp2.timemax);
            if (itime.timemin.before(qtemp.T))
              itime.timemin = qtemp.T;
            // System.out.println(itime.timemin.toString()+"
            // "+itime.timemax.toString());

            double g = Double.parseDouble(roundOff(Double
                .toString(((double) (itime.timemin.getTime() - qtemp.T
                    .getTime()))
                    / (1000 * 60))));
            int i = 0;

            while (queue.get(i++).dev != tabdevList.indexOf(dtemp2.mac))
              ;
            DijkstraQ qtemp2 = queue.get(i - 1);
            if (qtemp2.cost > qtemp.cost + g) {
              qtemp2.cost = qtemp.cost + g;
              qtemp2.T = itime.timemin;
              qtemp2.set_path(qtemp.path);
              qtemp2.path.add(qtemp);

            }
            break;
          }

        }
      }
    }
  }*/

  public static double Round(double Rval, int Rpl) {

    double p = (double) Math.pow(10, Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double) tmp / p;
  }

  public int find_mincost(LinkedList queue, int dest) {

    double min = Double.MAX_VALUE;
    int min_index = -1;
    double cost_temp;
    DijkstraQ qtemp;
    ListIterator qit = queue.listIterator();
    while (qit.hasNext()) {
      qtemp = (DijkstraQ) qit.next();
      cost_temp = qtemp.cost;
      if (cost_temp < min) {
        min = cost_temp;
        min_index = queue.indexOf(qtemp);
      }
      if (cost_temp == min & qtemp.dev == dest) {
        min = cost_temp;
        min_index = queue.indexOf(qtemp);
      }
    }
    return min_index;
  } 
  public int find_mincost_path(LinkedList queue, int dest, Msg mtemp) {

    double min = Double.MAX_VALUE;
    int min_index = -1;
    double cost_temp;
    DijkstraQ qtemp;
    ListIterator qit = queue.listIterator();
    while (qit.hasNext()) {
      qtemp = (DijkstraQ) qit.next();
      
      cost_temp = qtemp.cost;
      if (cost_temp < min) {
        min = cost_temp;
        min_index = queue.indexOf(qtemp);
      }
      if (cost_temp == min & qtemp.dev == dest) {
        min = cost_temp;
        min_index = queue.indexOf(qtemp);
      }
    }
    return min_index;
  } 
}
