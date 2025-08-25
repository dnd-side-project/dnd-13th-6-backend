package com.runky.cheer.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runky.cheer.domain.Cheer;

public interface CheerJpaRepository extends JpaRepository<Cheer, Long> {
}
