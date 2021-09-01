package com.textcaptcha.data.repository;

import com.textcaptcha.data.model.task.CaptchaTask;
import com.textcaptcha.data.model.task.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CaptchaTaskRepository extends JpaRepository<CaptchaTask, Long> {

    @Query("FROM CaptchaTask c WHERE c.taskType = (:taskType) AND c.articleUrlHash = (:articleUrlHash) AND c.articleTextHash = (:articleTextHash)")
    List<CaptchaTask> getTasks(TaskType taskType, String articleUrlHash, String articleTextHash);

    @Query("FROM CaptchaTask c " +
            "WHERE c.taskType = (:taskType) " +
            "AND c.articleUrlHash = (:articleUrlHash) " +
            "AND c.articleTextHash = (:articleTextHash) " +
            "AND c.id IN (" +
            "SELECT DISTINCT ct.captchaTask.id FROM CaptchaTaskResponse ct " +
            "WHERE ct.captchaFlow.id != (:captchaTaskFlowId)" +
            ")")
    List<CaptchaTask> getTasksNotInFlow(TaskType taskType, String articleUrlHash, String articleTextHash, Long captchaTaskFlowId);

    @Query("SELECT COUNT(c) FROM CaptchaTask c WHERE c.taskType = (:taskType) AND c.articleUrlHash = (:articleUrlHash) AND c.articleTextHash = (:articleTextHash)")
    long countTasks(TaskType taskType, String articleUrlHash, String articleTextHash);
}
