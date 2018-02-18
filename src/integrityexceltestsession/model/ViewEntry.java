/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package integrityexceltestsession.model;

/**
 *
 * @author veckardt
 */
public class ViewEntry {

    String id;
    String text;
    String verdict;

    public ViewEntry(String id, String text, String verdict) {
        this.id = id;
        this.text = text;
        this.verdict = verdict;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getVerdict() {
        return verdict;
    }
}
