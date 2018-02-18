/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ptc.services.common.gateway.baseline;

import com.mks.api.response.Item;
import java.util.Date;

/**
 *
 * @author peisenha
 */
public class Baseline implements Comparable<Baseline> {

    String label;
    String asof;
    Date asOfDate;
    String state;

    public Baseline(Item item) {
        if (item.contains("AsOf")) {
            this.asof = item.getField("AsOf").getValueAsString();
            this.asOfDate = item.getField("AsOf").getDateTime();
        }
        if (item.contains("Label")) {
            this.label = item.getField("Label").getValueAsString();
        }
        if (item.contains("State")) {
            this.state = item.getField("State").getValueAsString();
        }
    }

    public Baseline(String _label, String _asOf) {
        this.asof = _asOf;
        this.label = _label;
    }

    public String getAsof() {
        return asof;
    }

    public Date getAsOfDate() {
        return asOfDate;
    }

    public String getLabel() {
        return label;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    /**
     * compares two Objecs of type Baseline, needed for sorting the baseline list
     * @param o object to compare
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compareTo(Baseline o) {
        int compare;
        if (asOfDate.after(o.getAsOfDate())) {
            compare = 1;
        } else if (asOfDate.before(o.getAsOfDate())) {
            compare = -1;
        } else {
            compare = 0;
        }
        return compare;
    }

    @Override
    public String toString() {
        return new StringBuffer().append("Baseline Label: ").append(label).append("\tBaseline Date (asOf): ").append(asof).append("\tState: ").append(state).toString();
    }
}

/*
 * output of the api
 * im viewissue --showLabels --xmlapi 79429
 *
<Field name="MKSIssueLabels">
<List elementType="item">
<Item id="Version 1.0, BzR (Baselinegesetzt)" context="79429" displayId="Version 1.0, BzR (Baselinegesetzt)" modelType="im.Issue.Label">
<Field name="Label">
<Value dataType="string">Version 1.0, BzR (Baselinegesetzt)</Value>
</Field>
<Field name="AsOf">
<Value dataType="datetime">2010-06-09T12:37:16</Value>
</Field>
</Item>
<Item id="Version 1.1 BzA" context="79429" displayId="Version 1.1 BzA" modelType="im.Issue.Label">
<Field name="Label">
<Value dataType="string">Version 1.1 BzA</Value>
</Field>
<Field name="AsOf">
<Value dataType="datetime">2010-06-09T12:50:49</Value>
</Field>
</Item>
<Item id="Version 1.3 BzA" context="79429" displayId="Version 1.3 BzA" modelType="im.Issue.Label">
<Field name="Label">
<Value dataType="string">Version 1.3 BzA</Value>
</Field>
<Field name="AsOf">
<Value dataType="datetime">2010-06-09T15:18:12</Value>
</Field>
</Item>
<Item id="Aufwand FSL geschätzt" context="79429" displayId="Aufwand FSL geschätzt" modelType="im.Issue.Label">
<Field name="Label">
<Value dataType="string">Aufwand FSL geschätzt</Value>
</Field>
<Field name="AsOf">
<Value dataType="datetime">2010-06-10T07:33:31</Value>
</Field>
</Item>
</List>
</Field>
</WorkItem>
</WorkItems>
</Response>
 */
