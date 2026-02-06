package models;

public class User {
    private String username;
    private String email;
    private String phone;
    private String status;
    private String department;
    private String memberSince;

    public User(String username, String email, String phone,
                String status, String department, String memberSince) {
        this.username = username;
      
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.department = department;
        this.memberSince = memberSince;
    }

    // getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public String getDepartment() { return department; }
    public String getMemberSince() { return memberSince; }
}

