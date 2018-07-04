package fr.sictiam.stela.pesservice.model.asalae;

import java.util.List;
import java.util.Map;

public class AsalaeDocument {

    private String id_d;
    private AsalaeInfo info;
    private Map<String, Object> data;
    private List<String> action_possible;
    private AsalaeAction last_action;


    public AsalaeDocument() {
    }

    public String getId_d() {
        return id_d;
    }

    public void setId_d(String id_d) {
        this.id_d = id_d;
    }

    public AsalaeInfo getInfo() {
        return info;
    }

    public void setInfo(AsalaeInfo info) {
        this.info = info;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public List<String> getAction_possible() {
        return action_possible;
    }

    public void setAction_possible(List<String> action_possible) {
        this.action_possible = action_possible;
    }

    public AsalaeAction getLast_action() {
        return last_action;
    }

    public void setLast_action(AsalaeAction last_action) {
        this.last_action = last_action;
    }

    @Override
    public String toString() {
        return "\nAsalaeDocument {" +
                "\n    id_d='" + id_d + '\'' +
                ",\n    info=" + info +
                ",\n    data=" + data +
                ",\n    action_possible=" + action_possible +
                ",\n    last_action=" + last_action +
                "\n}";
    }
}
