package com.textcaptcha.taskmanager.service.impl;

import com.textcaptcha.data.model.task.NerCaptchaTask;
import com.textcaptcha.data.model.task.content.NerCaptchaTaskContent;
import com.textcaptcha.taskmanager.pojo.SolutionCheckerResult;
import com.textcaptcha.taskmanager.service.TaskSolutionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NerTaskSolutionChecker implements TaskSolutionChecker<NerCaptchaTask> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public SolutionCheckerResult checkSolution(NerCaptchaTask task, List<Integer> solution) {

        // should be picked, but were not
        int falseNegatives = 0;
        // should be and were picked
        int truePositives = 0;
        // should not be picked and weren't
        int trueNegatives = 0;
        // should not be picked, but were
        int falsePositives = 0;

        String primaryAnnotation = task.getContent().getPrimaryAnnotation().split("-")[1];
        for (int i = 0; i < task.getContent().getTokens().size(); i++) {
            NerCaptchaTaskContent.Token word = task.getContent().getTokens().get(i);
            boolean isNamedEntity = !word.getAnnotation().equals("O");
            boolean hasPrimaryAnnotation = word.getAnnotation().contains(primaryAnnotation);

            boolean isRelevant = isNamedEntity && hasPrimaryAnnotation;
            boolean isSelected = solution.contains(i);

            if (isRelevant) {
                if (isSelected) {
                    truePositives++;
                } else {
                    falseNegatives++;
                }
            } else {
                if (isSelected) {
                    falsePositives++;
                } else {
                    trueNegatives++;
                }
            }
        }

//        int totalPositives = truePositives + falsePositives;
//        int totalFalse= falseNegatives + falsePositives;
//        String message = "You selected " + truePositives + "/" + totalPositives + " entities and made " + totalFalse + " errors.";

        double sensitivity = (double) truePositives / (falseNegatives + truePositives);
        double specificity = (double) trueNegatives / (trueNegatives + falsePositives);

        logger.trace(String.format("TP=%d, FN=%d, FP=%d, TN=%d, sens=%.2f, spec=%.2f", truePositives, falseNegatives, falsePositives, trueNegatives, sensitivity, specificity));

        return new SolutionCheckerResult(sensitivity, specificity);
    }

}
