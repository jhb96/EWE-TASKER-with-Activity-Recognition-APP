package es.dit.gsi.rulesframework.model;

import java.util.List;

/**
 * Created by afernandez on 26/01/16.
 */
public class Channel {
    public String id;
    public String label;
    public String comment;
    public List<Event> events;
    public List<Action> actions;

    public Channel(String id,String label,String comment, List<Action> actions, List<Event> events) {
        this.id = id;
        this.label = label;
        this.actions = actions;
        this.events = events;
        this.comment = comment;
    }

    public boolean hasEvents(){
        if(events.size()>0 && !events.get(0).id.equals("")){
            return true;
        }else{
            return false;
        }
    }

    public boolean hasActions(){
        if(actions.size()>0 && !actions.get(0).id.equals("")){
            return true;
        }else{
            return false;
        }
    }
}
