package ru.edu.spbstu.model.jpa;

import java.util.Date;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
@EqualsAndHashCode
public class MessageJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;

    @Column(length=512)
    private String content;

    private Boolean isDeleted;
    private Boolean isEdited;
    private Boolean isForwarded;

    @OneToOne
    @JoinColumn(name = "sender_id")
    private UserJpa sender;


    @OneToOne
    @JoinColumn(name = "author_id")
    private UserJpa author;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatJpa chat;
}
