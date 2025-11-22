package com.kraftlog.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database cleanup utility using JPA EntityManager
 * Provides methods to truncate all tables while maintaining schema
 */
@Slf4j
@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Truncates all tables in the database
     * Warning: This will delete ALL data!
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void truncateAllTables() {
        log.warn("🧹 Starting database cleanup - ALL DATA WILL BE DELETED!");
        
        try {
            // Clear the persistence context to avoid issues
            entityManager.flush();
            entityManager.clear();
            
            // Disable foreign key checks for H2
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
            
            // Truncate all tables
            String[] tables = {"log_sets", "log_exercises", "log_routines", "log_workouts",
                              "workout_exercises", "workout_muscles", "workouts", 
                              "aerobic_activities", "exercise_muscles", "exercises", 
                              "muscles", "routines", "users"};
            
            for (String table : tables) {
                try {
                    entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
                    log.debug("✓ Truncated table: {}", table);
                } catch (Exception e) {
                    log.debug("  Skipped table {} (may not exist): {}", table, e.getMessage());
                }
            }
            
            // Re-enable foreign key checks
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            
            // Flush changes
            entityManager.flush();
            
            log.info("✅ All tables truncated successfully!");
            
        } catch (Exception e) {
            log.error("Failed to truncate tables", e);
            throw new RuntimeException("Database cleanup failed: " + e.getMessage(), e);
        }
        
        logTableCounts();
    }

    /**
     * Logs the count of records in each table
     */
    private void logTableCounts() {
        log.info("📊 Current table counts:");
        String[] tables = {"users", "muscles", "exercises", "routines", 
                          "log_routines", "log_exercises", "log_sets"};
        
        for (String table : tables) {
            try {
                Long count = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM " + table)
                        .getSingleResult();
                log.info("  {} = {} rows", table, count);
            } catch (Exception e) {
                log.debug("Could not count table {}", table);
            }
        }
    }

    /**
     * Deletes all data using entity manager (slower but safer)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllEntities() {
        log.warn("🧹 Starting entity deletion - ALL DATA WILL BE DELETED!");
        
        // Clear the persistence context
        entityManager.flush();
        entityManager.clear();
        
        // Delete in order to respect foreign key constraints
        entityManager.createQuery("DELETE FROM LogSet").executeUpdate();
        entityManager.createQuery("DELETE FROM LogExercise").executeUpdate();
        entityManager.createQuery("DELETE FROM LogRoutine").executeUpdate();
        entityManager.createQuery("DELETE FROM Exercise").executeUpdate();
        entityManager.createQuery("DELETE FROM Muscle").executeUpdate();
        entityManager.createQuery("DELETE FROM Routine").executeUpdate();
        entityManager.createQuery("DELETE FROM User").executeUpdate();
        
        entityManager.flush();
        
        log.info("✅ All entities deleted successfully!");
        logTableCounts();
    }
}
