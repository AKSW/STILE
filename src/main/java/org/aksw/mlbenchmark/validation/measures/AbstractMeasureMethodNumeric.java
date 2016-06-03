/*
 * Copyright 2016 AKSW.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aksw.mlbenchmark.validation.measures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.Constants;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public abstract class AbstractMeasureMethodNumeric implements MeasureMethodNumericValued {
    // total number of positive examples
    protected int nPos;
    // total number of negative examples
    protected int nNeg;
    
    public AbstractMeasureMethodNumeric(int nPos, int nNeg) {
        this.nPos = nPos;
        this.nNeg = nNeg;
    }
    
    public List<CurvePoint> convertIntoCurvePoints(List<ClassificationResult> results) throws CurvePointGenerationException {
        List<CurvePoint> curvePoints = new LinkedList<>();
        Collections.sort(results,Collections.reverseOrder());
        int truePos = 0;
        int falsePos = 0;
        double fPrev = Double.MAX_VALUE;
        for(ClassificationResult res : results) {
            if (res.getProb() < fPrev ) {
                curvePoints.add(new CurvePoint(truePos, falsePos));
                fPrev = res.getProb();
            } else {
                if (res.getProb() > fPrev){
                    throw new CurvePointGenerationException("current score: " + res.getProb() +
                    " is greater than previous one: " + fPrev);
                }
            }
            if (res.getClassification() == Constants.ExType.POS) {
                truePos++;
            } else {
                falsePos++;
            }
        }
        return curvePoints;
    }
}
