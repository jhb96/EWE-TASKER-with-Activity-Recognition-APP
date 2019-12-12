package es.dit.gsi.rulesframework.model;

import java.util.List;

/**
 * Created by afernandez on 26/01/16.
 */
public class Action {
    public String id;
    public String label;
    public String comment;
    public List<Parameter> input_parameters;
    public List<Parameter> output_parameters;

    public Action(String id,String label,String comment, List<Parameter> input_parameters, List<Parameter> output_parameters) {
        this.id = id;
        this.label = label;
        this.input_parameters = input_parameters;
        this.output_parameters = output_parameters;
        this.comment = comment;
    }

    public boolean hasParameters(){
        if((input_parameters.size() > 0)||(output_parameters.size() > 0)){
            return true;
        }else{
            return false;
        }
    }
}
