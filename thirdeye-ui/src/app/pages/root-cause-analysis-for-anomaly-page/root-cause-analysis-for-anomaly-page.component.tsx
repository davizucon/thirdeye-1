import { Card, CardContent, Grid, Paper } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    PageContentsGridV1,
    PageHeaderTextV1,
    PageHeaderV1,
    PageV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AnomalySummaryCard } from "../../components/entity-cards/root-cause-analysis/anomaly-summary-card/anomaly-summary-card.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    createAlertEvaluation,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorSnackbarOption } from "../../utils/snackbar/snackbar.util";
import { RootCauseAnalysisForAnomalyPageParams } from "./root-cause-analysis-for-anomaly-page.interfaces";

export const RootCauseAnalysisForAnomalyPage: FunctionComponent = () => {
    const {
        anomaly,
        getAnomaly,
        status: getAnomalyRequestStatus,
    } = useGetAnomaly();
    const { evaluation, getEvaluation } = useGetEvaluation();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { enqueueSnackbar } = useSnackbar();
    const {
        id: anomalyId,
    } = useParams<RootCauseAnalysisForAnomalyPageParams>();
    const { t } = useTranslation();
    const pageTitle = `${t("label.root-cause-analysis")}: ${t(
        "label.anomaly"
    )} #${anomalyId}`;

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        isValidNumberId(anomalyId) && getAnomaly(toNumber(anomalyId));
    }, [anomalyId]);

    useEffect(() => {
        !!anomaly && setUiAnomaly(getUiAnomaly(anomaly));
    }, [anomaly]);

    useEffect(() => {
        fetchAlertEvaluation();
    }, [uiAnomaly]);

    useEffect(() => {
        if (evaluation && anomaly) {
            const anomaliesDetector =
                evaluation.detectionEvaluations.output_AnomalyDetectorResult_0;
            anomaliesDetector.anomalies = anomaliesDetector.anomalies.filter(
                (anomalyData) => {
                    return (
                        anomalyData.startTime === anomaly.startTime &&
                        anomalyData.endTime === anomaly.endTime
                    );
                }
            );
            setAlertEvaluation(evaluation);
        }
    }, [evaluation]);

    if (!isValidNumberId(anomalyId)) {
        // Invalid id
        enqueueSnackbar(
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: anomalyId,
            }),
            getErrorSnackbarOption()
        );

        setUiAnomaly(null);
    }

    const fetchAlertEvaluation = (): void => {
        if (!uiAnomaly || !uiAnomaly.alertId) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(
                uiAnomaly.alertId,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        );
    };

    return (
        <PageV1>
            <PageHeaderV1>
                <PageHeaderTextV1>{pageTitle}</PageHeaderTextV1>
            </PageHeaderV1>
            <PageContentsGridV1>
                {/* Anomaly Summary */}
                <Grid item xs={12}>
                    <Paper elevation={0}>
                        {getAnomalyRequestStatus === ActionStatus.Working && (
                            <Card variant="outlined">
                                <CardContent>
                                    <AppLoadingIndicatorV1 />
                                </CardContent>
                            </Card>
                        )}
                        {getAnomalyRequestStatus !== ActionStatus.Working &&
                            getAnomalyRequestStatus !== ActionStatus.Error && (
                                <AnomalySummaryCard uiAnomaly={uiAnomaly} />
                            )}
                        {getAnomalyRequestStatus === ActionStatus.Error && (
                            <Card variant="outlined">
                                <CardContent>
                                    <NoDataIndicator />
                                </CardContent>
                            </Card>
                        )}
                    </Paper>
                </Grid>
                {/* Trending */}
                <Grid item xs={12}>
                    <Paper elevation={0}>
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                            alertEvaluationTimeSeriesHeight={500}
                            maximizedTitle={uiAnomaly ? uiAnomaly.name : ""}
                            onRefresh={fetchAlertEvaluation}
                        />
                    </Paper>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
