package main.cloudfilestorage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class User {
    @Id
    private Long id;

    @Column(name = "user-name")
    private String userName;

    private String password;
}
