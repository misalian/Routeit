
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

public class Mem_Semantic {

  public TSlot life_time;

  public TSlot mem_interval;

  public Mem_Semantic(long gtime, long dtime) {

    life_time = new TSlot(gtime, dtime, "");
  }

  public Mem_Semantic(TSlot t) {

    life_time = new TSlot();
    life_time.timemin.setTime(t.timemin.getTime());
    life_time.timemax.setTime(t.timemax.getTime());
  }

  void set_meminterval(long min_time, long max_time) {

    mem_interval = new TSlot(min_time, max_time, "");

  }
}
