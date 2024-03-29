package com.textcaptcha.taskmanager.controller;


import com.textcaptcha.data.model.task.CaptchaTask;
import com.textcaptcha.data.model.task.TaskType;
import com.textcaptcha.taskmanager.dto.TaskInstanceDto;
import com.textcaptcha.taskmanager.dto.TaskRequestRequestBody;
import com.textcaptcha.taskmanager.dto.TaskSolutionRequestBody;
import com.textcaptcha.taskmanager.dto.TaskSolutionResponseDto;
import com.textcaptcha.taskmanager.exception.InvalidTaskTypeException;
import com.textcaptcha.taskmanager.exception.TaskSelectionException;
import com.textcaptcha.taskmanager.pojo.IssuedTaskInstance;
import com.textcaptcha.taskmanager.pojo.SolutionProcessorResult;
import com.textcaptcha.taskmanager.service.TaskInstanceKeeper;
import com.textcaptcha.taskmanager.service.TaskSelectionService;
import com.textcaptcha.taskmanager.service.TaskSolutionProcessor;
import com.textcaptcha.taskmanager.service.impl.RandomTaskSelectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TaskInstanceKeeper taskInstanceKeeper;
    private final RandomTaskSelectionService taskSelectionService;
    private final TaskSolutionProcessor taskSolutionProcessor;

    @Autowired
    public TaskController(
            TaskInstanceKeeper taskInstanceKeeper,
            RandomTaskSelectionService taskSelectionService,
            TaskSolutionProcessor taskSolutionProcessor
    ) {
        this.taskInstanceKeeper = taskInstanceKeeper;
        this.taskSelectionService = taskSelectionService;
        this.taskSolutionProcessor = taskSolutionProcessor;
    }

    @PostMapping("/request")
    public TaskInstanceDto getTask(@RequestBody TaskRequestRequestBody body) {
        logger.debug("Received task request: " + body.toString());

        try {
            TaskType.valueOf(body.getTaskType());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidTaskTypeException(e);
        }

        return TaskInstanceDto.fromIssuedTaskInstance(getTaskInstance(body));
    }

    @PostMapping("/response")
    public TaskSolutionResponseDto postSolution(@RequestBody TaskSolutionRequestBody body) {
        logger.debug("Received task response: " + body.toString());
        SolutionProcessorResult result = taskSolutionProcessor.processSolution(body.getId(), body.getContent());
        return new TaskSolutionResponseDto(result.getCheckResult().toString());
    }

    private IssuedTaskInstance getTaskInstance(TaskRequestRequestBody body) {
        CaptchaTask task;
        try {
            task = taskSelectionService.getTaskForArticle(TaskType.valueOf(body.getTaskType()), body.getHashes(), null);
        } catch (TaskSelectionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return taskInstanceKeeper.issue(task);
    }

}
