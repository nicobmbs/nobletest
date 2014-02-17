package com.thisisnoble.javatest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.List;
//import com.thisisnoble.javatest.events.TradeEvent;
//import com.thisisnoble.javatest.events.RiskEvent;
//import com.thisisnoble.javatest.events.ShippingEvent;
//import com.thisisnoble.javatest.events.MarginEvent;
import com.thisisnoble.javatest.impl.CompositeEvent;

/**
 * s
 *
 * @author nicolaptop
 */
public class Orchestrator {

    private static List<CompositeEvent> listCompEvent;
    private static List<Processor> listProcessor;
    private static Publisher localPub;

    public Orchestrator() {
        this.listCompEvent = new ArrayList();
        this.listProcessor = new ArrayList();
    }

    public void setup(Publisher Pub) {
        localPub = Pub;
    }

    public void register(Processor processor) {
        listProcessor.add(processor);
    }

    public void receive(Event evt) {
        System.out.println("receive evt id :" + evt.getClass().getName());
        int processEvent = 1;
        if (evt instanceof CompositeEvent) {
            CompositeEvent ce = (CompositeEvent) evt;
            System.out.println(">>>>>> publishing comp event: " + evt.getId() + " parent :" + ce.getParent().getId());
            localPub.publish(evt);
            removeCompEvt(ce);
        } else {
            if (evt.getParentId() == null) {
                addCompEvt(new CompositeEvent(evt));
                System.out.println(">> creating compevt for evt id :" + evt.getId());
            } else {
                processEvent = 0;
                CompositeEvent ce = (CompositeEvent) getParentCompEvt(evt.getParentId());
                
                if (ce == null ) {
                    System.out.println("trashing subchild event " + evt.getClass().getName());
                } else {
                    ce.addChild(evt);
                    System.out.println(">>adding child " + evt.getClass().getName() + " to compEvt id : " + ce.getParent().getId());
                }
                if (ce != null) {
                    if (ce.getTrigger() == ce.size()) {
                        this.receive(ce);
                    }
                }
            }
            if (processEvent == 1) {
                for (Processor proc : this.getProcessor()) {
                    if (proc.interestedIn(evt) == true) {
                        addnumberEvent(evt.getId());
                        proc.process(evt);
                    }
                }
            }
        }
    }

    public synchronized List<Processor> getProcessor() {
        return listProcessor;
    }

    public synchronized void addCompEvt(CompositeEvent evt) {
        listCompEvent.add(evt);
    }

    public synchronized List<CompositeEvent> getListCompEvt() {
        return listCompEvent;
    }

    public synchronized int getListCompSize() {
        return listCompEvent.size();
    }

    public synchronized CompositeEvent getParentCompEvt(String evtId) {
        CompositeEvent res = null;
        for (CompositeEvent localCompEvt : getListCompEvt()) {
            if (localCompEvt.getParent().getId().equals(evtId) == true) {
                res = localCompEvt;
            }
        }
        return res;
    }

    public void addnumberEvent(String evtId) {
        for (CompositeEvent localCompEvt : getListCompEvt()) {
            if (localCompEvt.getParent().getId().equals(evtId) == true) {
                localCompEvt.incrTrigger();
            }
        }
    }

    private synchronized void removeCompEvt(CompositeEvent cmpEvt) {
        listCompEvent.remove(cmpEvt);
    }
    /*private synchronized void removeCompEvt(CompositeEvent cmpEvt) {
     for (CompositeEvent localCmpEvt : listCompEvent) {
     if (cmpEvt.getParent().getId().equals(localCmpEvt.getParent().getId())) {
     listCompEvent.remove(cmpEvt);
     }
     }

     }*/

    private Exception NullPointerException(String no_compEvt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
