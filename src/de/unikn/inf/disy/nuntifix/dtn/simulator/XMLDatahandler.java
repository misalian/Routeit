package de.unikn.inf.disy.nuntifix.dtn.simulator;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Apoint;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;

public class XMLDatahandler extends DefaultHandler {

  Device dtemp;

  TSlot ttemp;

  Apoint aptemp;

  int check;

  Router Tanalyser;

  public XMLDatahandler(Router t) {

    Tanalyser = t;
  }

  public void startElement(String namespaceURI, String localName,
      String qualifiedName, Attributes atts) {

    if (qualifiedName.equals("Device")) {
      dtemp = new Device(atts.getValue("", "id"));
    }

    if (qualifiedName.equals("Ap")) {
      aptemp = new Apoint(atts.getValue("", "id"));
      // dtemp.apcount++;
    }
    if (qualifiedName.equals("ContactTime")) {
      ttemp = new TSlot(atts.getValue("", "Start"), atts.getValue("", "End"));
      if (ttemp.timemax.getTime() - ttemp.timemin.getTime() > 1999) {
        ttemp.apname = new String(aptemp.Apointname);
        // dtemp.totalcount++;
        aptemp.contacttimeList.add(ttemp);
      }
      /*if(dtemp.mac.equals("3") & aptemp.Apointname.equalsIgnoreCase("38AP"))
      {
       
        Calendar c1 = Calendar.getInstance();
        c1.setTime(ttemp.timemin);
       
        if(c1.get(Calendar.DAY_OF_MONTH)==22 & (c1.get(Calendar.HOUR_OF_DAY)==11 | c1.get(Calendar.HOUR_OF_DAY)==25))
          System.out.println(ttemp.timemin+" "+ttemp.timemax);
      }
      // System.out.println(ttemp.timemin.toString()+ "
      // "+ttemp.timemax.toString());
      /*
       * pttemp=Tanalyser.getspanslot(ttemp,(Tanalyser.ptslotlist.size()-1)/2);
       * if(pttemp==null) { if(Tanalyser.ptslotlist.size()>0){ do{ pttemp=new
       * PTSlot(Tanalyser.ptslotlist.getLast(),Tanalyser.spanhrs);
       * Tanalyser.ptslotlist.add(pttemp); }while(!pttemp.isoverlap(ttemp));
       * }else{ pttemp=new PTSlot(ttemp,Tanalyser.spanhrs);
       * Tanalyser.ptslotlist.add(pttemp); } }
       */
    }
  }

  public void endElement(String namespaceURI, String localName,
      String qualifiedName) {

    if (qualifiedName.equals("Ap")) {
      if (aptemp.contacttimeList.size() > 0)
        dtemp.Aptable.put(aptemp.Apointname, aptemp);
      // dtemp.Aplist.add(aptemp);
      // if(!Tanalyser.tabapList.contains(aptemp.Apointname))
      // Tanalyser.tabapList.add(aptemp.Apointname);
    }

    if (qualifiedName.equals("Device")) {
      // if(!Tanalyser.tabdevList.contains(dtemp.mac))
      // Tanalyser.tabdevList.add(dtemp.mac);
      if (dtemp.Aptable.size() > 0)
        Tanalyser.addelement(dtemp);
    }
  }

  public void characters(char[] text, int start, int length)
      throws SAXException {

    /*
     * if (buffer != null) { buffer.append(text, start, length); }
     */

  }

}
