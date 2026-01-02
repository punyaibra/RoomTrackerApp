package com.example.roomtracker.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String role; // "ADMIN", "STAFF", "MAHASISWA"
    private String ktm; // Stores KTM Image URI
    private String profileImage; // Stores Profile Image URI
    private String fakultas;
    private String prodi;
    private String angkatan; // String to handle years like "2023"
    private int isVerified; // 0: Pending, 1: Verified, 2: Rejected

    public User() {
    }

    public User(int id, String name, String email, String password, String role, String ktm, String profileImage,
            String fakultas, String prodi, String angkatan, int isVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.ktm = ktm;
        this.profileImage = profileImage;
        this.fakultas = fakultas;
        this.prodi = prodi;
        this.angkatan = angkatan;
        this.isVerified = isVerified;
    }

    // Constructor for Registration (initially profileImage is null)
    public User(String name, String email, String password, String role, String ktm,
            String fakultas, String prodi, String angkatan) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.ktm = ktm;
        this.profileImage = null; // Default empty
        this.fakultas = fakultas;
        this.prodi = prodi;
        this.angkatan = angkatan;
        this.isVerified = 0; // Default pending
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getKtm() {
        return ktm;
    }

    public void setKtm(String ktm) {
        this.ktm = ktm;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFakultas() {
        return fakultas;
    }

    public void setFakultas(String fakultas) {
        this.fakultas = fakultas;
    }

    public String getProdi() {
        return prodi;
    }

    public void setProdi(String prodi) {
        this.prodi = prodi;
    }

    public String getAngkatan() {
        return angkatan;
    }

    public void setAngkatan(String angkatan) {
        this.angkatan = angkatan;
    }

    public int getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(int isVerified) {
        this.isVerified = isVerified;
    }
}
