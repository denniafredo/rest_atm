package com.example.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Table(name = "debts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Debt extends BaseEntity {
    @Id
    @Column
    private long id;

    @ManyToOne
    @JoinColumn(name = "owes_id")
    private User owes;

    @ManyToOne
    @JoinColumn(name = "owed_id")
    private User owed;

    private Double amount;
}
