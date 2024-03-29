package com.textcaptcha.data.repository;

import com.textcaptcha.data.model.task.CaptchaTask;
import com.textcaptcha.data.model.task.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CaptchaTaskRepository extends JpaRepository<CaptchaTask, Long> {

    @Query("FROM CaptchaTask c WHERE c.taskType = (:taskType) AND c.articleUrlHash = (:articleUrlHash) AND c.articleTextHash = (:articleTextHash)")
    List<CaptchaTask> getTasks(TaskType taskType, String articleUrlHash, String articleTextHash);

    @Query("SELECT c FROM CaptchaTask c " +
            "LEFT JOIN CaptchaTaskResponse r ON c.id = r.captchaTask.id " +
            "WHERE c.taskType = (:taskType) " +
            "AND c.articleUrlHash = (:articleUrlHash) " +
            "AND c.articleTextHash = (:articleTextHash) " +
            "GROUP BY c ORDER BY COUNT(r) ASC")
    List<CaptchaTask> getTasksByNumberOfResponsesAsc(TaskType taskType, String articleUrlHash, String articleTextHash);

//    @Query("SELECT c FROM CaptchaTask c " +
//            "LEFT JOIN CaptchaTaskResponse r ON c.id = r.captchaTask.id " +
//            "WHERE c.taskType = (:taskType) " +
//            "AND c.articleUrlHash = (:articleUrlHash) " +
//            "AND c.articleTextHash = (:articleTextHash) " +
//            "AND c.id NOT IN (" +
//            "    SELECT DISTINCT ct.captchaTask.id FROM CaptchaTaskResponse ct " +
//            "    WHERE ct.captchaLink.id = (:captchaTaskFlowId)" +
//            ") " +
//            "GROUP BY c ORDER BY COUNT(r) ASC")
//    List<CaptchaTask> getTasksNotInLinkByNumberOfResponsesAsc(TaskType taskType, String articleUrlHash, String articleTextHash, Long captchaTaskFlowId);

    @Query("SELECT COUNT(c) FROM CaptchaTask c WHERE c.taskType = (:taskType) AND c.articleUrlHash = (:articleUrlHash) AND c.articleTextHash = (:articleTextHash)")
    long countTasks(TaskType taskType, String articleUrlHash, String articleTextHash);
}
