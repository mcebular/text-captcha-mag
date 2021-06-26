package com.textcaptcha.textingest.service.ner;

import com.textcaptcha.annotation.Loggable;
import com.textcaptcha.data.pojo.AnnotatedToken;
import com.textcaptcha.textingest.exception.AnnotatorException;
import com.textcaptcha.textingest.exception.IngestException;
import com.textcaptcha.textingest.pojo.ReceivedArticle;
import com.textcaptcha.textingest.service.IngestService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NerIngestService implements IngestService {

    @Loggable
    private Logger logger;

    private final NerAnnotatorService annotatorService;
    private final NerTaskGeneratorService taskGeneratorService;

    @Autowired
    public NerIngestService(
            NerAnnotatorService annotatorService,
            NerTaskGeneratorService taskGeneratorService
    ) {
        this.annotatorService = annotatorService;
        this.taskGeneratorService = taskGeneratorService;
    }

    @Override
    public void ingest(ReceivedArticle article) throws IngestException {
        if (taskGeneratorService.areTasksGenerated(article)) {
            logger.debug("Tasks for " + article + " already generated.");
            return;
        }

        try {
            List<AnnotatedToken> tokens = annotatorService.annotate(article.getText());
            int generatedTaskCount = taskGeneratorService.generateTasks(article, tokens);
            logger.debug("Generated " + generatedTaskCount + " tasks for " + article + ".");

        } catch (AnnotatorException e) {
            throw new IngestException(e);
        }
    }

}