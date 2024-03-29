package com.textcaptcha.taskmanager.service.impl;

import com.textcaptcha.data.model.task.CaptchaTask;
import com.textcaptcha.taskmanager.pojo.IssuedTaskInstance;
import com.textcaptcha.taskmanager.service.TaskInstanceKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskInstanceKeeperImpl implements TaskInstanceKeeper {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long INSTANCE_EXPIRATION = 1000 * 60 * 2;

    private final Map<UUID, IssuedTaskInstance> issuedTasks;

    public TaskInstanceKeeperImpl() {
        this.issuedTasks = new ConcurrentHashMap<>();
    }

    @Override
    public IssuedTaskInstance issue(CaptchaTask task) {
        UUID taskInstanceId = UUID.randomUUID();
        issuedTasks.put(taskInstanceId, new IssuedTaskInstance(taskInstanceId, task));

        logger.debug("Issued task ID " + task.getId() + " with instance ID " + taskInstanceId + ".");
        return new IssuedTaskInstance(taskInstanceId, task);
    }

    @Override
    public IssuedTaskInstance invalidate(UUID instanceId) {
        IssuedTaskInstance i = issuedTasks.get(instanceId);
        if (i != null && !i.isExpired(INSTANCE_EXPIRATION)) {
            issuedTasks.remove(instanceId);
            return i;
        }
        return null;
    }

    @Scheduled(fixedDelay = INSTANCE_EXPIRATION * 4)
    private void cleanupExpiredInstances() {
        int countRemoved = 0;
        for (Iterator<Map.Entry<UUID, IssuedTaskInstance>> i = issuedTasks.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<UUID, IssuedTaskInstance> entry = i.next();
            if (entry.getValue().isExpired(INSTANCE_EXPIRATION)) {
                i.remove();
                countRemoved++;
            }
        }

        if (countRemoved > 0) {
            logger.trace("Cleaned up " + countRemoved + " expired task instances.");
        }
    }


}
