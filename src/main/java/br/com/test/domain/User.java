package br.com.test.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="users")
public class User implements Serializable {

    @Id
    @Column(name="id", nullable=false, length=11)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name="name", nullable=false, length=300)
    private String name;

    @Column(name="email", nullable=false, length=300, unique = true)
    private String email;

    @Column(name="username", nullable=false, length=300)
    private String username;

    @Column(name="password", nullable=false, length=300)
    private String password;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
