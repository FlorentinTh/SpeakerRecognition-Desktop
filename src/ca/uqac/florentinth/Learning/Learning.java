package ca.uqac.florentinth.Learning;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

/**
 * Copyright 2016 Florentin Thullier.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Learning {
    private Classifier classifier;
    private double accuracy;
    private double kappaValue;
    private double fmeasureValue;
    private String confusionMatrix;

    public void trainClassifierSuppliedTest(String trainingDataset, String testingDataset) throws
            Exception {
        trainClassifier(trainingDataset, 0, testingDataset);
        outputEvaluationMetrics();
    }

    public void trainClassifierFolds(String trainingDataset, int foldNumber) throws
            Exception {
        trainClassifier(trainingDataset, foldNumber, null);
        outputEvaluationMetrics();
    }

    private void trainClassifier(String trainingDataset, Integer foldNumber, String testingDataset) throws
            Exception {
        Instances trainingInstances = new Instances(new BufferedReader(new FileReader(trainingDataset)));
        classifier = new NaiveBayes();

        if (trainingInstances.classIndex() == -1) {
            trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        }

        classifier.buildClassifier(trainingInstances);
        Evaluation evaluation = new Evaluation(trainingInstances);

        if (foldNumber > 0) {
            evaluation.crossValidateModel(classifier, trainingInstances, foldNumber, new Random(1));
            accuracy = evaluation.pctCorrect();
            kappaValue = evaluation.kappa();
            fmeasureValue = evaluation.weightedFMeasure();
            confusionMatrix = evaluation.toMatrixString("Confusion matrix: ");
        } else if (testingDataset != null) {
            Instances testingInstances = new Instances(new BufferedReader(new FileReader(testingDataset)));

            if (testingInstances.classIndex() == -1) {
                testingInstances.setClassIndex(testingInstances.numAttributes() - 1);
            }

            evaluation.evaluateModel(classifier, testingInstances);
            accuracy = evaluation.pctCorrect();
            kappaValue = evaluation.kappa();
            fmeasureValue = evaluation.weightedFMeasure();
            confusionMatrix = evaluation.toMatrixString("Confusion matrix: ");
        }
    }

    public void outputEvaluationMetrics() {
        System.out.println("Accuracy: " + accuracy + "%");
        System.out.println("Kappa: " + kappaValue);
        System.out.println("FMeasure: " + fmeasureValue);
        System.out.println(confusionMatrix);
    }
}
