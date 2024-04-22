package me.beardedowl.caffeine.singleton.cache;

public class UserDTO {

    public static int count = 0;
    String userName;

    String surName;
    int id;

    public UserDTO(String userName, String surName) {
        this.userName = userName;
        this.surName = surName;
        this.id = UserDTO.count;
        UserDTO.count += 1;
    }
    public String getUserName() {
        return userName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
