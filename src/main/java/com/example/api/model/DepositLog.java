package com.example.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "deposit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositLog extends BaseEntity {
    @Id
    @Column
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

    private String status;

}
