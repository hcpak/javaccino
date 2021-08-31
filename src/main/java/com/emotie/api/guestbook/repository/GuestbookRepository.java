package com.emotie.api.guestbook.repository;

import com.emotie.api.guestbook.domain.Guestbook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestbookRepository extends JpaRepository<Guestbook, String> {

    List<Guestbook> findByNickname(String nickname);
}
