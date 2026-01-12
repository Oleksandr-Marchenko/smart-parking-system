package com.dev.marchenko.repository;

import com.dev.marchenko.domain.lot.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

}
