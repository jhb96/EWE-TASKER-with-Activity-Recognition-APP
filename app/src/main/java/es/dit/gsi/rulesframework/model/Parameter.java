package es.dit.gsi.rulesframework.model;

import java.util.List;

/**
 * Created by afernandez on 26/01/16.
 */
public class Parameter {
    public String id;
    public String label;
    public String comment;
    public String datatype;

    public Parameter(String id, String label, String comment, String datatype) {
        this.id = id;
        this.label = label;
        this.datatype = datatype;
        this.comment = comment;
    }

}
