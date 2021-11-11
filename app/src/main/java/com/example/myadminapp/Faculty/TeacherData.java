package com.example.myadminapp.Faculty;

public class TeacherData {
    private String name, email, post, iamge, key;

    public TeacherData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getIamge() {
        return iamge;
    }

    public void setIamge(String iamge) {
        this.iamge = iamge;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TeacherData(String name, String email, String post, String iamge, String key) {
        this.name = name;
        this.email = email;
        this.post = post;
        this.iamge = iamge;
        this.key = key;
    }

}
