package com.project.parking.repository;

import com.project.parking.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreadRepository extends JpaRepository<Thread, Integer> {
}