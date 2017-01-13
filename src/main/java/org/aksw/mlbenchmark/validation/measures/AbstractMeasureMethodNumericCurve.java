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

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.aksw.mlbenchmark.validation.measures.exceptions.CurvePointGenerationException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.validation.measures.exceptions.ProbabilisticResultOrderException;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public abstract class AbstractMeasureMethodNumericCurve implements MeasureMethodNumericValued {

    // total number of positive examples
    protected int nPos;
    // total number of negative examples
    protected int nNeg;

    protected List<ConfusionPoint> curvePoints;

    public AbstractMeasureMethodNumericCurve(int nPos, int nNeg, List<ClassificationResult> results) {
        this.nPos = nPos;
        this.nNeg = nNeg;
        try {
            this.curvePoints = convertIntoCurvePoints(results);
        } catch (ProbabilisticResultOrderException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ConfusionPoint> convertIntoCurvePoints(List<ClassificationResult> results)
            throws ProbabilisticResultOrderException {
        List<ConfusionPoint> curvePoints = new LinkedList<>();
        Collections.sort(results, Collections.reverseOrder());
        int truePos = 0;
        int falsePos = 0;
        BigDecimal fPrev = BigDecimal.valueOf(Double.MAX_VALUE);
        for (ClassificationResult res : results) {
            if (res.getProb().compareTo(fPrev) < 0) {
                curvePoints.add(new ConfusionPoint(falsePos, truePos));
                fPrev = res.getProb();
            } else if (res.getProb().compareTo(fPrev) > 0) {
                throw new ProbabilisticResultOrderException("current score: " + res.getProb()
                        + " is greater than previous one: " + fPrev);
            }
            if (res.getClassification() == Constants.ExType.POS) {
                truePos++;
            } else {
                falsePos++;
            }
        }
        return curvePoints;
    }

    /**
     * It returns the area under the curve.
     *
     * @param points
     * @return
     */
    protected static BigDecimal getAUC(List<? extends Point> points) {
        BigDecimal area = new BigDecimal(0);
        BigDecimal x = points.get(0).getX();
        BigDecimal y = points.get(0).getY();
        for (Point p : points.subList(1, points.size())) {
            area = area.add(trapezoidArea(p.getY(), y, p.getX().subtract(x)));
            x = p.getX();
            y = p.getY();
        }
        return area;
    }

    /**
     * It computes the area of a trapezoid.
     *
     * @param base1
     * @param base2
     * @param height
     * @return
     */
    private static BigDecimal trapezoidArea(BigDecimal base1, BigDecimal base2, BigDecimal height) {
        BigDecimal sumBases = base1.add(base2);
        return sumBases.multiply(height).divide(new BigDecimal(2), SCALE, ROUNDINGMODE);
    }

    public abstract List<? extends Point> getCurvePoints();

    public abstract BigDecimal getAUC();

    @Override
    public double getMeasure() {
        return getAUC().setScale(SCALE, ROUNDINGMODE).doubleValue();
    }

}
