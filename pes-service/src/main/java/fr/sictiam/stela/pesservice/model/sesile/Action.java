package fr.sictiam.stela.pesservice.model.sesile;

import java.util.Date;

public class Action {
    /*
    {
      "id": 6432,
      "username": "Jean Claude RUSSO",
      "date": "2019-01-16T10:26:52+0100",
      "action": "Signature",
      "observation": null
    }
     */
    private int id;

    private String username;

    private Date date;

    private String action;

    private String commentaire;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
