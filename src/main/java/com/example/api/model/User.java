package com.example.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @Column
    private long id;

    @Column
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
